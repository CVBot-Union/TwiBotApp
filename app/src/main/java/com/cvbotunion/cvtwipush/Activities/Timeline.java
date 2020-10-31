package com.cvbotunion.cvtwipush.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.Uri;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cvbotunion.cvtwipush.Adapters.TweetDetailCardAdapter;
import com.cvbotunion.cvtwipush.CustomViews.GroupPopupWindow;
import com.cvbotunion.cvtwipush.DBModel.DBTwitterMedia;
import com.cvbotunion.cvtwipush.DBModel.DBTwitterStatus;
import com.cvbotunion.cvtwipush.DBModel.DBTwitterUser;
import com.cvbotunion.cvtwipush.Fragments.LoginFragment;
import com.cvbotunion.cvtwipush.Model.Job;
import com.cvbotunion.cvtwipush.Model.RTGroup;
import com.cvbotunion.cvtwipush.Model.TwitterMedia;
import com.cvbotunion.cvtwipush.Model.TwitterStatus;
import com.cvbotunion.cvtwipush.Model.User;
import com.cvbotunion.cvtwipush.R;
import com.cvbotunion.cvtwipush.Service.MyServiceConnection;
import com.cvbotunion.cvtwipush.Service.WebService;
import com.cvbotunion.cvtwipush.Utils.RSACrypto;
import com.cvbotunion.cvtwipush.Utils.RefreshTask;
import com.cvbotunion.cvtwipush.Adapters.TweetCardAdapter;
import com.danikula.videocache.StorageUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

import org.litepal.LitePal;
import org.litepal.LitePalDB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Timeline extends AppCompatActivity {
    //每次从数据库和服务器获取的最大推文数目
    public static final int LIMIT = 20;
    public static MyServiceConnection connection = new MyServiceConnection();

    private RecyclerView tweetListRecyclerView;
    private TweetCardAdapter tAdapter;
    private RefreshLayout refreshLayout;
    private ChipGroup chipGroup;

    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private Boolean isConnectivityLost;

    private MaterialToolbar mdToolbar;
    private TextView title;

    private ArrayList<TwitterStatus> dataSet = new ArrayList<>();
    private ArrayList<TwitterStatus> usedDataSet = new ArrayList<>();
    private static User currentUser;
    private static RTGroup currentGroup;
    private Map<Integer, String> chipIdToName;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        //动态权限申请
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        initBackground();
        initView();
        currentUser = User.readFromDisk();
        if(currentUser==null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_fragment_container, new LoginFragment())
                    .addToBackStack(null)
                    .commit();
        }
        Log.i("Timeline.onCreate","FRAGMENT FINISHED!");
        if(currentUser==null) { onBackPressed(); }
        initData();
        initRecyclerView();
        initConnectivityReceiver();

        title.setText(currentGroup.name);
        mdToolbar.setOnMenuItemClickListener(item -> {
            int menuID = item.getItemId();
            if (menuID == R.id.group_menu_item){
                View view = getLayoutInflater().inflate(R.layout.group_switch_menu, (ViewGroup)getWindow().getDecorView(),false);
                GroupPopupWindow popupWindow = new GroupPopupWindow(
                        view, ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        true,
                        currentUser, currentGroup.id);
                popupWindow.showAsDropDown(findViewById(R.id.group_menu_item),0, 0, Gravity.END);
                popupWindow.dimBehind();
            }
            return true;
        });

        for(int i=0;i<currentGroup.following.size();i++){
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_view,chipGroup,false);
            chip.setText(currentGroup.following.get(i).name_in_group);
            int viewId = ViewCompat.generateViewId();
            chip.setId(viewId);
            chipIdToName.put(viewId, currentGroup.following.get(i).name_in_group);
            chipGroup.addView(chip);
        }

        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            usedDataSet.clear();
            String checkedName = chipIdToName.getOrDefault(checkedId, null);
            for (TwitterStatus s : dataSet) {
                if (checkedName == null || s.user.name.equals(checkedName))
                    usedDataSet.add(s);
            }
            tAdapter.notifyDataSetChanged();
        });

        refreshLayout.setHeaderTriggerRate(0.7f);  //触发刷新距离 与 HeaderHeight 的比率
        refreshLayout.setOnRefreshListener(refreshlayout -> netRefresh(chipGroup.getCheckedChipId(),refreshlayout, RefreshTask.REFRESH));
        refreshLayout.setOnLoadMoreListener(refreshlayout -> netRefresh(chipGroup.getCheckedChipId(),refreshlayout, RefreshTask.LOADMORE));

        mdToolbar.setOnClickListener(view -> {
            tweetListRecyclerView.smoothScrollToPosition(0);
            refreshLayout.autoRefresh();
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
        unbindService(connection);
        chipIdToName.clear();
        if(connectivityManager != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
        if(Math.random()>0.9) {  //十分之一的概率
            StorageUtils.deleteFile(VideoViewer.cacheDir);  //删除整个目录
            StorageUtils.deleteFiles(TwitterMedia.internalFilesDir);  //删除子文件
        }
    }

    private  void initData() {
        dataSet = new ArrayList<>();
        usedDataSet = new ArrayList<>();
        chipIdToName = new HashMap<>();

        Intent intent = getIntent();
        if(intent == null) {
            currentGroup = currentUser.jobs.get(0).group;
        } else {
            String groupId = null;
            Bundle bundle;
            Uri uri;
            if ((bundle = intent.getExtras()) != null) {
                groupId = bundle.getString("groupId");
            } else if((uri = intent.getData()) != null) {
                groupId = uri.getQueryParameter("groupId");
                // userScreenName = uri.getQueryParameter("user");
                // TODO use statusId instead
                String statusId = uri.getQueryParameter("statusId");
            }
            if(groupId!=null) {
                for (Job j : currentUser.jobs) {
                    if (j.group.id.equals(groupId)) {
                        currentGroup = j.group;
                        break;
                    }
                }
            }
        }

        if(currentGroup==null) {
            Toast.makeText(getApplicationContext(), "获取转推组信息失败", Toast.LENGTH_LONG).show();
            onBackPressed();
        }

        StringBuilder condition = new StringBuilder();
        for(int i=0;i<currentGroup.following.size();i++) {
            condition.append(currentGroup.following.get(i).id);
            if(i!=currentGroup.following.size()-1) { condition.append(","); }
        }
        List<DBTwitterStatus> dbStatusList = LitePal.where("tuid in (?)", condition.toString()).order("tsid desc").find(DBTwitterStatus.class);
        for(DBTwitterStatus dbStatus:dbStatusList) {
            dataSet.add(dbStatus.toTwitterStatus());
            if(dataSet.size()>=LIMIT) { break; }
        }
        usedDataSet.addAll(dataSet);
    }

    private void initRecyclerView(){
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this) {
            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        };

        tweetListRecyclerView.setLayoutManager(layoutManager);
        tAdapter = new TweetCardAdapter(usedDataSet,this, currentGroup.tweetFormat);
        tweetListRecyclerView.setAdapter(tAdapter);
        ((SimpleItemAnimator) tweetListRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    private void initView(){
        tweetListRecyclerView = (RecyclerView) findViewById(R.id.tweet_list_recycler_view);
        mdToolbar = (MaterialToolbar) findViewById(R.id.top_app_bar);
        title = (TextView) findViewById(R.id.title);
        chipGroup = (ChipGroup) findViewById(R.id.group_chip_group);
        refreshLayout = (RefreshLayout) findViewById(R.id.refresh_layout);
    }

    private void initBackground() {
        RSACrypto.init();

        Intent serviceIntent = new Intent(this, WebService.class);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

        LitePalDB litePalDB = new LitePalDB("twipushData", 8);
        litePalDB.addClassName(DBTwitterStatus.class.getName());
        litePalDB.addClassName(DBTwitterUser.class.getName());
        litePalDB.addClassName(DBTwitterMedia.class.getName());
        LitePal.use(litePalDB);
        db = LitePal.getDatabase();
    }

    private void initConnectivityReceiver() {
        connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if(connectivityManager != null) {
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onLost(@NonNull Network network) {
                    super.onLost(network);
                    TweetCardAdapter.isConnected = false;
                    TweetDetailCardAdapter.isConnected = false;
                    Snackbar.make(tweetListRecyclerView, "网络连接丢失", 3000).show();
                }

                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    TweetCardAdapter.isConnected = true;
                    TweetDetailCardAdapter.isConnected = true;
                    refreshLayout.autoRefresh();
                }
            };
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        }
    }

    public void netRefresh(int checkedId, RefreshLayout refreshlayout, int mode) {
        // 每个AsyncTask实例只能execute()一次
        RefreshTask task = new RefreshTask(refreshlayout, tAdapter, mode);
        String checkedName = chipIdToName.getOrDefault(checkedId, null);
        task.setData(usedDataSet, dataSet, checkedName);
        task.execute();
    }

    public static RTGroup getCurrentGroup() {
        return currentGroup;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        currentUser = user;
    }
}
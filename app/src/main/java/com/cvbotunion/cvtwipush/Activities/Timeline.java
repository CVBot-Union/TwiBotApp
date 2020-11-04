package com.cvbotunion.cvtwipush.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cvbotunion.cvtwipush.Adapters.TweetDetailCardAdapter;
import com.cvbotunion.cvtwipush.CustomViews.GroupPopupWindow;
import com.cvbotunion.cvtwipush.DBModel.DBTwitterMedia;
import com.cvbotunion.cvtwipush.DBModel.DBTwitterStatus;
import com.cvbotunion.cvtwipush.DBModel.DBTwitterUser;
import com.cvbotunion.cvtwipush.Model.Job;
import com.cvbotunion.cvtwipush.Model.RTGroup;
import com.cvbotunion.cvtwipush.Model.TwitterMedia;
import com.cvbotunion.cvtwipush.Model.TwitterStatus;
import com.cvbotunion.cvtwipush.Model.TwitterUser;
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
    private FrameLayout fragmentContainer;  // TODO 保留待定

    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private Boolean isConnectivityLost;

    private MaterialToolbar mdToolbar;
    private TextView title;

    private ArrayList<TwitterStatus> dataSet = new ArrayList<>();
    private ArrayList<TwitterStatus> usedDataSet = new ArrayList<>();
    private static User currentUser;
    private static RTGroup currentGroup;
    private Map<Integer, String> chipIdToUid;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        if(!TwitterMedia.mediaFilesDir.exists()) {
            TwitterMedia.mediaFilesDir.mkdirs();
        }
        //动态权限申请
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        initBackground();
        initView();
        currentUser = User.readFromDisk();
        if(currentUser==null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, 123);
        } else {
            getReady();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==123) {
            if(resultCode==123) getReady();
        }
    }

    private void getReady() {
        WebService.setAuth(currentUser.getAuth());
        currentUser.update();
        initData();
        initRecyclerView();
        initConnectivityReceiver();
        title.setText(currentGroup.name);
        mdToolbar.setOnMenuItemClickListener(item -> {
            int menuID = item.getItemId();
            if (menuID == R.id.group_menu_item) {
                View view = getLayoutInflater().inflate(R.layout.group_switch_menu, (ViewGroup) getWindow().getDecorView(), false);
                GroupPopupWindow popupWindow = new GroupPopupWindow(
                        view, ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        true,
                        currentUser, currentGroup.id);
                popupWindow.showAsDropDown(findViewById(R.id.group_menu_item), 0, 0, Gravity.END);
                popupWindow.dimBehind();
            }
            return true;
        });

        for (int i = 0; i < currentGroup.following.size(); i++) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_view, chipGroup, false);
            chip.setText(currentGroup.following.get(i).name_in_group);
            int viewId = ViewCompat.generateViewId();
            chip.setId(viewId);
            chipIdToUid.put(viewId, currentGroup.following.get(i).id);
            chipGroup.addView(chip);
        }

        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            usedDataSet.clear();
            String checkedUid = chipIdToUid.getOrDefault(checkedId, null);
            for (TwitterStatus s : dataSet) {
                if (checkedUid == null || s.user.id.equals(checkedUid))
                    usedDataSet.add(s);
            }
            tAdapter.notifyDataSetChanged();
        });

        refreshLayout.setHeaderTriggerRate(0.7f);  //触发刷新距离 与 HeaderHeight 的比率
        refreshLayout.setOnRefreshListener(refreshlayout -> netRefresh(chipGroup.getCheckedChipId(), refreshlayout, RefreshTask.REFRESH));
        refreshLayout.setOnLoadMoreListener(refreshlayout -> netRefresh(chipGroup.getCheckedChipId(), refreshlayout, RefreshTask.LOADMORE));

        mdToolbar.setOnClickListener(view -> {
            tweetListRecyclerView.smoothScrollToPosition(0);
            refreshLayout.autoRefresh();
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(currentUser==null) {
            onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // TODO 如果不点返回键而是直接点击任务键，侧滑杀死进程来关闭应用，应用将没有机会调用onDestroy
        //  但其他activity抢占前台（即Timeline不在栈顶的情况）也会调用onStop
        //  请谨慎取舍在此处的操作

        // 保存已登录的user
        if(currentUser!=null) currentUser.writeToDisk();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
        unbindService(connection);
        if(chipIdToUid !=null) chipIdToUid.clear();
        if(connectivityManager != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
        if(Math.random()>0.9) {  //十分之一的概率
            StorageUtils.deleteFiles(VideoViewer.videoCacheDir);
            StorageUtils.deleteFiles(TwitterMedia.mediaFilesDir);  //删除子文件
        }
    }

    private void initData() {
        dataSet = new ArrayList<>();
        usedDataSet = new ArrayList<>();
        chipIdToUid = new HashMap<>();

        Intent intent = getIntent();
        if(intent.getExtras()==null && intent.getData()==null) {
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
            finish();
            return;
        }

        List<DBTwitterStatus> dbStatusList = new ArrayList<>();
        for(TwitterUser tu:currentGroup.following) {
            List<DBTwitterStatus> tmpList = LitePal.where("tuid = ?", tu.id).order("tsid desc").find(DBTwitterStatus.class);
            if(!tmpList.isEmpty()) dbStatusList.addAll(tmpList);
        }
        // 按TwitterStatus Id降序排列
        dbStatusList.sort((o1, o2) -> Long.compare(o2.getTsid(),o1.getTsid()));
        // TODO
        System.out.println("initData 数据库中取得的量:"+dbStatusList.size());
        for(DBTwitterStatus dbStatus:dbStatusList) {
            TwitterStatus tweet =dbStatus.toTwitterStatus();
            currentGroup.following.forEach(tu -> {
                if(tu.id.equals(tweet.user.id)) tweet.user.name_in_group = tu.name_in_group;
            });
            dataSet.add(tweet);
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
        tweetListRecyclerView = findViewById(R.id.tweet_list_recycler_view);
        mdToolbar = findViewById(R.id.top_app_bar);
        title = findViewById(R.id.title);
        chipGroup = findViewById(R.id.group_chip_group);
        refreshLayout = findViewById(R.id.refresh_layout);
        // fragmentContainer = findViewById(R.id.main_fragment_container);
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
        String checkedUid = chipIdToUid.getOrDefault(checkedId, null);
        task.setData(usedDataSet, dataSet, checkedUid);
        task.execute();
    }

    public static RTGroup getCurrentGroup() {
        return currentGroup;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }
}
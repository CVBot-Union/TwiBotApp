package com.cvbotunion.cvtwipush.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import com.cvbotunion.cvtwipush.Utils.RefreshTask;
import com.cvbotunion.cvtwipush.Adapters.TweetCardAdapter;
import com.danikula.videocache.StorageUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import org.litepal.LitePal;
import org.litepal.LitePalDB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Timeline extends AppCompatActivity {
    //每次从数据库和服务器获取的最大推文数目
    public static final int LIMIT = 20;
    public static MyServiceConnection connection = new MyServiceConnection();

    private RecyclerView tweetListRecyclerView;
    private TweetCardAdapter tAdapter;
    private RecyclerView.LayoutManager layoutManager;
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
        setContentView(R.layout.activity_tweet_list);

        //动态权限申请
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        initBackground();
        initView();
        initData();
        initRecyclerView();
        initConnectivityReceiver();

        title.setText(currentGroup.name);
        mdToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
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
            }
        });

        for(int i=0;i<currentGroup.following.size();i++){
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_view,chipGroup,false);
            chip.setText(currentGroup.following.get(i).name_in_group);
            int viewId = ViewCompat.generateViewId();
            chip.setId(viewId);
            chipIdToName.put(viewId, currentGroup.following.get(i).name_in_group);
            chipGroup.addView(chip);
        }

        chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                usedDataSet.clear();
                String checkedName = chipIdToName.getOrDefault(checkedId, null);
                for (TwitterStatus s : dataSet) {
                    if (checkedName == null || s.user.name.equals(checkedName))
                        usedDataSet.add(s);
                }
                tAdapter.notifyDataSetChanged();
            }
        });

        refreshLayout.setHeaderTriggerRate(0.7f);  //触发刷新距离 与 HeaderHeight 的比率
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshlayout) {
                netRefresh(chipGroup.getCheckedChipId(),refreshlayout, RefreshTask.REFRESH);
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshlayout) {
                netRefresh(chipGroup.getCheckedChipId(),refreshlayout, RefreshTask.LOADMORE);
            }
        });

        mdToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tweetListRecyclerView.smoothScrollToPosition(0);
                refreshLayout.autoRefresh();
            }
        });

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
        unbindService(connection);
        chipIdToName.clear();
        if(Math.random()>0.9) {  //十分之一的概率
            StorageUtils.deleteFile(VideoViewer.cacheDir);  //删除整个目录
            StorageUtils.deleteFiles(TwitterMedia.internalFilesDir);  //删除子文件
        }
        if(connectivityManager != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }

    private  void initData() {
        dataSet = new ArrayList<>();
        usedDataSet = new ArrayList<>();
        chipIdToName = new HashMap<>();
        // TODO initData实际应用
        //readData()
        //if not found, netRefresh()

        //List<DBTwitterStatus> dbStatusList = LitePal.limit(LIMIT).order("tsid desc").find(DBTwitterStatus.class);
        //for(DBTwitterStatus s : dbStatusList) {
        //"0"使得最新的放上面
        //dataSet.add(0, s.toTwitterStatus());
        //}
        //usedDataSet = (ArrayList<TwitterStatus>) dataSet.clone();

        Intent intent = getIntent();
        String groupId;
        String userScreenName;
        if(intent != null){
            try {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    groupId = bundle.getString("groupId");
                    //group = LitePal.where("gid = ?",groupId).findFirst(DBRTGroup.class).toRTGroup();
                }
            } catch (Exception e) {
                Uri uri = intent.getData();
                if (uri == null) {
                    return;
                }
                groupId = uri.getQueryParameter("groupId");
                userScreenName = uri.getQueryParameter("user");
            }
        } else {
            //currentUser = LitePal.findFirst(DBUser.class).toUser();
            //String firstGid = (String) currentUser.jobs.keySet().toArray()[0];
            //currentGroup = LitePal.where("gid = ?",firstGid).findFirst(DBRTGroup.class).toRTGroup();
        }

        //start 模拟数据
        TwitterUser user = new TwitterUser("1", "sb", "sbsb", "SB", "http://101.200.184.98:8080/media/MO4FkO4N_400x400.jpg");
        TwitterMedia media = new TwitterMedia("1", "http://101.200.184.98:8080/media/MO4FkO4N_400x400.jpg", TwitterMedia.IMAGE, "http://101.200.184.98:8080/media/MO4FkO4N_400x400.jpg");
        ArrayList<TwitterMedia> newList = new ArrayList<>();
        newList.add(media);
        newList.add(media);
        newList.add(media);
        TwitterStatus status = new TwitterStatus("11:14", "1", "测试，https://github.com", user, newList);

        TwitterMedia videoMedia = new TwitterMedia("10","http://101.200.184.98:8080/abe.mp4",TwitterMedia.VIDEO,"http://101.200.184.98:8080/227组标.jpg");
        ArrayList<TwitterMedia> videoList = new ArrayList<>();
        videoList.add(videoMedia);
        TwitterStatus status1 = new TwitterStatus("2:06","10","视频推文",user,videoList);

        //由于不同推文可能会使用同一个media，所以没有给media设置UNIQUE字段，
        //  使用DBTwitterMedia.save()方法前请通过statusId和tid字段进行查重
        if (LitePal.where("tsid = ?", status.id).find(DBTwitterStatus.class).isEmpty()) {
            DBTwitterStatus dbTweet = new DBTwitterStatus(status);
            dbTweet.save();
        }
        if (LitePal.where("tsid = ?", status1.id).find(DBTwitterStatus.class).isEmpty()) {
            DBTwitterStatus dbTweet = new DBTwitterStatus(status1);
            dbTweet.save();
        }

        dataSet.add(status1);
        dataSet.add(status);
        dataSet.add(status);
        dataSet.add(status);
        dataSet.add(status);
        usedDataSet = (ArrayList<TwitterStatus>) dataSet.clone();

        ArrayList<TwitterUser> following = new ArrayList<>();
        following.add(new TwitterUser("3", "相羽あいな", "aibaaiai", "相羽爱奈", "http://101.200.184.98:8080/aiai.jpg"));
        following.add(new TwitterUser("4", "工藤晴香", "kudoharuka910", "工藤晴香", ""));
        following.add(new TwitterUser("5", "中島由貴", "Yuki_Nakashim", "中岛由贵", ""));
        following.add(new TwitterUser("6", "櫻川めぐ", "sakuragawa_megu", "樱川惠", ""));
        following.add(new TwitterUser("7", "志崎樺音", "Kanon_Shizaki", "志崎桦音", ""));
        currentGroup = new RTGroup("1", "蔷薇之心", "", following, null);

        Job job = new Job("翻译/搬运", 1, currentGroup);
        currentUser = new User("1", "用户1", null, null,null);
        currentUser.addJob(job);
        //end 模拟数据
    }

    private void initRecyclerView(){
        layoutManager = new LinearLayoutManager(this){
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
        Intent serviceIntent = new Intent(this, WebService.class);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

        LitePalDB litePalDB = new LitePalDB("twipushData", 8);
        litePalDB.addClassName(DBTwitterStatus.class.getName());
        litePalDB.addClassName(DBTwitterUser.class.getName());
        litePalDB.addClassName(DBTwitterMedia.class.getName());
        LitePal.use(litePalDB);
        db = LitePal.getDatabase();
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

}
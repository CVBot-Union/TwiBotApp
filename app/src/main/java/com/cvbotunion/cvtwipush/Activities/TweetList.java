package com.cvbotunion.cvtwipush.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.cvbotunion.cvtwipush.CustomViews.GroupPopupWindow;
import com.cvbotunion.cvtwipush.DBModel.DBFollow;
import com.cvbotunion.cvtwipush.DBModel.DBJob;
import com.cvbotunion.cvtwipush.DBModel.DBRTGroup;
import com.cvbotunion.cvtwipush.DBModel.DBTwitterMedia;
import com.cvbotunion.cvtwipush.DBModel.DBTwitterStatus;
import com.cvbotunion.cvtwipush.DBModel.DBTwitterUser;
import com.cvbotunion.cvtwipush.DBModel.DBUser;
import com.cvbotunion.cvtwipush.Model.RTGroup;
import com.cvbotunion.cvtwipush.Model.TwitterMedia;
import com.cvbotunion.cvtwipush.Model.TwitterStatus;
import com.cvbotunion.cvtwipush.Model.TwitterUser;
import com.cvbotunion.cvtwipush.Model.User;
import com.cvbotunion.cvtwipush.Utils.NetworkStateReceiver;
import com.cvbotunion.cvtwipush.R;
import com.cvbotunion.cvtwipush.Utils.RefreshTask;
import com.cvbotunion.cvtwipush.Adapters.TweetCardAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.litepal.LitePal;
import org.litepal.LitePalDB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TweetList extends AppCompatActivity {

    private CoordinatorLayout coordinatorLayout;
    private RecyclerView tweetListRecyclerView;
    private TweetCardAdapter tAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NetworkStateReceiver networkStateReceiver;
    private ChipGroup chipGroup;
    private Chip all;

    private MaterialToolbar mdToolbar;
    private TextView title;

    private ArrayList<TwitterStatus> dataSet = new ArrayList<>();
    private ArrayList<TwitterStatus> usedDataSet = new ArrayList<>();
    private User currentUser;
    private RTGroup group;
    private ArrayList<String> followingName;
    private Map<Integer, String> idToName = new HashMap<>();
    private SQLiteDatabase db;

    public TweetList() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_list);

        //动态权限申请
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        initBackground();
        initData();
        initView();
        initRecyclerView();

        title.setText(group.name);
        mdToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int menuID = item.getItemId();
                if (menuID == R.id.group_menu_item){
                    View view = getLayoutInflater().inflate(R.layout.group_switch_menu,null);
                    GroupPopupWindow popupWindow = new GroupPopupWindow(
                            view, ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            true,
                            currentUser);
                    popupWindow.showAsDropDown(findViewById(R.id.group_menu_item),0, 0, Gravity.END);
                    dimBehind(popupWindow);
                }
                return true;
            }
        });

        for(String twitterUserName:followingName){
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_view,chipGroup,false);
            chip.setText(twitterUserName);
            int viewId = ViewCompat.generateViewId();
            chip.setId(viewId);
            idToName.put(viewId, twitterUserName);
            chipGroup.addView(chip);
        }
        chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                usedDataSet.clear();
                String checkedName = idToName.getOrDefault(checkedId, null);
                for (TwitterStatus s : dataSet) {
                    if (checkedName == null || s.user.name.equals(checkedName))
                        usedDataSet.add(s);
                }
                tAdapter.notifyDataSetChanged();
            }
        });

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                netRefresh(chipGroup.getCheckedChipId());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
        idToName.clear();
        unregisterReceiver(networkStateReceiver);
        getCacheDir().delete();
    }

    private  void initData() {
        //List<DBTwitterStatus> dbStatusList = LitePal.findAll(DBTwitterStatus.class);
        //for(DBTwitterStatus s : dbStatusList) {
        //"0"使得最新的放上面
        //dataSet.add(0, s.toTwitterStatus());
        //}
        //usedDataSet = (ArrayList<TwitterStatus>) dataSet.clone();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String groupId = bundle.getString("groupId");
            //group = LitePal.where("gid = ?",groupId).findFirst(DBRTGroup.class).toRTGroup();
        } else {
            //currentUser = LitePal.findFirst(DBUser.class).toUser();
            //String firstGid = (String) currentUser.jobs.keySet().toArray()[0];
            //group = LitePal.where("gid = ?",firstGid).findFirst(DBRTGroup.class).toRTGroup();
        }
        //for(TwitterUser u : group.following) {
        //    followingName.add(u.name);
        //}

        //start 模拟数据
        TwitterUser user = new TwitterUser("1", "sb", "sbsb", "SB", "http://101.200.184.98:8080/media/MO4FkO4N_400x400.jpg");
        TwitterMedia media = new TwitterMedia("1", "http://101.200.184.98:8080/media/MO4FkO4N_400x400.jpg", TwitterMedia.IMAGE, "http://101.200.184.98:8080/media/MO4FkO4N_400x400.jpg");
        ArrayList<TwitterMedia> newList = new ArrayList<>();
        newList.add(media);
        newList.add(media);
        newList.add(media);
        TwitterStatus status = new TwitterStatus("11:14", "1", "测试", user, newList);

        TwitterMedia videoMedia = new TwitterMedia("10","http://101.200.184.98:8080/abe.mp4",TwitterMedia.VIDEO,"http://101.200.184.98:8080/227组标.jpg");
        ArrayList<TwitterMedia> videoList = new ArrayList<>();
        videoList.add(videoMedia);
        TwitterStatus status1 = new TwitterStatus("2:06","10","视频推文",user,videoList);

        //由于不同推文可能会使用同一个media，所以没有给media设置UNIQUE字段，
        //  使用DBTwitterMedia.save()方法前请通过statusId和tid字段进行查重
        if (LitePal.where("tid = ?", status.id).find(DBTwitterStatus.class).isEmpty()) {
            DBTwitterStatus dbTweet = new DBTwitterStatus(status);
            dbTweet.save();
        }
        if (LitePal.where("tid = ?", status1.id).find(DBTwitterStatus.class).isEmpty()) {
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
        group = new RTGroup("group1", "蔷薇之心", "", following);
        followingName = new ArrayList<>();
        for (TwitterUser u : group.following) {
            followingName.add(u.name);
        }
        if (LitePal.where("gid = ?", group.id).find(DBRTGroup.class).isEmpty()) {
            DBRTGroup dbrtGroup = new DBRTGroup(group);
            dbrtGroup.save();
        }

        RTGroup.Job job = new RTGroup.Job("翻译/搬运", 1);
        HashMap<String, RTGroup.Job> jobMap = new HashMap<>();
        jobMap.put(group.id, job);
        currentUser = new User("1", "用户1", null, null, jobMap);
        if(LitePal.where("uid = ?", currentUser.id).find(DBUser.class).isEmpty()) {
            DBUser dbUser = new DBUser(currentUser);
            dbUser.save();
        }
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
        tAdapter = new TweetCardAdapter(usedDataSet,this);
        tweetListRecyclerView.setAdapter(tAdapter);
        ((SimpleItemAnimator) tweetListRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    private void initView(){
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.tweet_list_parent_view);
        tweetListRecyclerView = (RecyclerView) findViewById(R.id.tweet_list_recycler_view);
        mdToolbar = (MaterialToolbar) findViewById(R.id.top_app_bar);
        title = (TextView) findViewById(R.id.title);
        chipGroup = (ChipGroup) findViewById(R.id.group_chip_group);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
    }

    private void initBackground() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        //自定义广播
        //intentFilter.addAction("android.net.conn.CONNECTIVITY_STATE");
        networkStateReceiver = new NetworkStateReceiver();
        registerReceiver(networkStateReceiver, intentFilter);

        LitePalDB litePalDB = new LitePalDB("twitterData", 6);
        litePalDB.addClassName(DBTwitterStatus.class.getName());
        litePalDB.addClassName(DBTwitterUser.class.getName());
        litePalDB.addClassName(DBTwitterMedia.class.getName());
        litePalDB.addClassName(DBRTGroup.class.getName());
        litePalDB.addClassName(DBUser.class.getName());
        litePalDB.addClassName(DBJob.class.getName());
        litePalDB.addClassName(DBFollow.class.getName());
        LitePal.use(litePalDB);
        db = LitePal.getDatabase();
    }

    public void netRefresh(int checkedId) {
        RefreshTask task = new RefreshTask(coordinatorLayout, swipeRefreshLayout, tAdapter);
        String checkedName = idToName.getOrDefault(checkedId, null);
        task.setData(usedDataSet, dataSet, checkedName);
        task.execute();
    }

    public void dimBehind(PopupWindow popupWindow) {
        View container;
        if (popupWindow.getBackground() == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                container = (View) popupWindow.getContentView().getParent();
            } else {
                container = popupWindow.getContentView();
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                container = (View) popupWindow.getContentView().getParent().getParent();
            } else {
                container = (View) popupWindow.getContentView().getParent();
            }
        }
        Context context = popupWindow.getContentView().getContext();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.3f;
        if(wm != null) {
            wm.updateViewLayout(container, p);
        }
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.update();
    }
}
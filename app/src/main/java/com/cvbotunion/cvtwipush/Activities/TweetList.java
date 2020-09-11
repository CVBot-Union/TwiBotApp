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
import android.widget.Toast;

import com.cvbotunion.cvtwipush.CustomViews.GroupPopupWindow;
import com.cvbotunion.cvtwipush.DBModel.DBTwitterMedia;
import com.cvbotunion.cvtwipush.DBModel.DBTwitterStatus;
import com.cvbotunion.cvtwipush.DBModel.DBTwitterUser;
import com.cvbotunion.cvtwipush.Model.TwitterMedia;
import com.cvbotunion.cvtwipush.Model.TwitterStatus;
import com.cvbotunion.cvtwipush.Model.TwitterUser;
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
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_list);

        //动态权限申请
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        initBackground();

        //start 模拟数据
        TwitterUser user = new TwitterUser("1","sb","sbsb","SB","http://101.200.184.98:8080/media/MO4FkO4N_400x400.jpg");
        TwitterMedia media = new TwitterMedia("1","http://101.200.184.98:8080/media/MO4FkO4N_400x400.jpg",TwitterMedia.IMAGE,"http://101.200.184.98:8080/media/MO4FkO4N_400x400.jpg");
        ArrayList<TwitterMedia> newList = new ArrayList<>();
        newList.add(media);
        newList.add(media);
        newList.add(media);
        TwitterStatus status = new TwitterStatus("11:14","1","测试",user,newList);

        //由于不同推文可能会使用同一个media，所以没有给它设置UNIQUE字段，
        //  使用DBTwitterMedia.save()方法前请通过statusId和tid字段进行查重
        DBTwitterStatus dbTweet = new DBTwitterStatus(status);
        dbTweet.save();

        dataSet.add(status);
        dataSet.add(status);
        dataSet.add(status);
        dataSet.add(status);

        String groupName = "蔷薇之心";

        ArrayList<String> userList = new ArrayList<>();
        userList.add("相羽あいな");
        userList.add("工藤晴香");
        userList.add("中島由貴");
        userList.add("櫻川めぐ");
        userList.add("志崎樺音");
        //end 模拟数据

        initView();
        initRecyclerView();


        title.setText(groupName);//待更改
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
                            null);//待更改
                    popupWindow.showAsDropDown(findViewById(R.id.group_menu_item),0, 0, Gravity.END);
                    dimBehind(popupWindow);
                }
                return true;
            }
        });

        final TwitterStatus newStatus = status;
        for(String twitterUser:userList){//待更改
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_view,chipGroup,false);
            chip.setText(twitterUser);
            chip.setId(ViewCompat.generateViewId());
            chipGroup.addView(chip);
        }

        chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                dataSet = new ArrayList<>();
                dataSet.add(newStatus);
                initRecyclerView();
            }
        });

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                netRefresh(chipGroup.getCheckedChipId());
                initRecyclerView();
            }
        });
        DBTwitterStatus status1 = LitePal.find(DBTwitterStatus.class, 1);
        TwitterStatus status2 = status1.toTwitterStatus();
        Toast.makeText(this, status2.user.profile_image_url, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
        unregisterReceiver(networkStateReceiver);
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
        tAdapter = new TweetCardAdapter(dataSet,this);
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
        LitePalDB litePalDB = new LitePalDB("twitterData", 4);
        litePalDB.addClassName(DBTwitterStatus.class.getName());
        litePalDB.addClassName(DBTwitterUser.class.getName());
        litePalDB.addClassName(DBTwitterMedia.class.getName());
        LitePal.use(litePalDB);
        db = LitePal.getDatabase();
    }

    public void netRefresh(int checkedId) {
        RefreshTask task = new RefreshTask(coordinatorLayout, swipeRefreshLayout, dataSet);
        task.setCheckedId(checkedId);
        task.execute();
    }

    public void dimBehind(PopupWindow popupWindow) {
        View container;
        if (popupWindow.getBackground() == null) {
            container = popupWindow.getContentView();
        } else {
            container = (View) popupWindow.getContentView().getParent();
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
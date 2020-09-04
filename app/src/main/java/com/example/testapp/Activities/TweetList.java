package com.example.testapp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.example.testapp.Model.TwitterMedia;
import com.example.testapp.Model.TwitterStatus;
import com.example.testapp.Model.TwitterUser;
import com.example.testapp.NetworkStateReceiver;
import com.example.testapp.R;
import com.example.testapp.RefreshTask;
import com.example.testapp.TweetCardAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class TweetList extends AppCompatActivity {

    private RecyclerView tweetListView;
    private TweetCardAdapter tAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private SwipeRefreshLayout refreshLayout;
    private NetworkStateReceiver networkStateReceiver;
    private ChipGroup chipGroup;
    private Chip all;

    private MaterialToolbar mdToolbar;

    private SwipeRefreshLayout swipeRefreshLayout;

    private ArrayList<TwitterStatus> dataSet = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_list);
        initBackground();

        refreshLayout = findViewById(R.id.swipe_refresh_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                netRefresh();
            }
        });

        //start 模拟数据
        TwitterUser user = new TwitterUser("1","sb","sbsb","SB","https://pbs.twimg.com/profile_images/1206156308602163200/MO4FkO4N_400x400.jpg");
        TwitterMedia media = new TwitterMedia("1","https://pbs.twimg.com/profile_images/1206156308602163200/MO4FkO4N_400x400.jpg",TwitterMedia.IMAGE,"https://pbs.twimg.com/profile_images/1206156308602163200/MO4FkO4N_400x400.jpg");
        ArrayList<TwitterMedia> newList = new ArrayList<>();
        newList.add(media);
        newList.add(media);
        newList.add(media);
        TwitterStatus status = new TwitterStatus("11:14","1","测试",user,newList);

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

        mdToolbar.setTitle(groupName);//待更改

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

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                dataSet = new ArrayList<>();
                dataSet.add(newStatus);
                initRecyclerView();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

        tweetListView.setLayoutManager(layoutManager);
        tAdapter = new TweetCardAdapter(dataSet,this);
        tweetListView.setAdapter(tAdapter);
        ((SimpleItemAnimator) tweetListView.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    private void initView(){
        tweetListView = (RecyclerView) findViewById(R.id.tweet_list_recycler_view);
        mdToolbar = (MaterialToolbar) findViewById(R.id.top_app_bar);
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
    }

    private void netRefresh() {
        RefreshTask task = new RefreshTask(this, refreshLayout, tAdapter);
        task.execute();
    }
}
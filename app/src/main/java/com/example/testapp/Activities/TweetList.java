package com.example.testapp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.testapp.Model.TwitterMedia;
import com.example.testapp.Model.TwitterStatus;
import com.example.testapp.Model.TwitterUser;
import com.example.testapp.NetworkStateReceiver;
import com.example.testapp.R;
import com.example.testapp.RefreshTask;
import com.example.testapp.TweetCardAdapter;

import java.util.ArrayList;

public class TweetList extends AppCompatActivity {

    private RecyclerView tweetListView;
    private TweetCardAdapter tAdapter;
    private SwipeRefreshLayout refreshLayout;
    private NetworkStateReceiver networkStateReceiver;

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

        //最终实现从数据库和网络（刷新操作）获取推文
        TwitterUser user = new TwitterUser("1","sb","sbsb","SB","http://101.200.184.98:8080/media/MO4FkO4N_400x400.jpg");
        TwitterMedia media = new TwitterMedia("1","",TwitterMedia.IMAGE,"http://101.200.184.98:8080/media/MO4FkO4N_400x400.jpg");
        ArrayList<TwitterMedia> newList = new ArrayList<>();
        newList.add(media);
        newList.add(media);
        newList.add(media);
        TwitterStatus status = new TwitterStatus("11:14","1","测试",user,newList);

        dataSet.add(status);
        dataSet.add(status);
        dataSet.add(status);
        dataSet.add(status);

        tweetListView = (RecyclerView) findViewById(R.id.tweet_list_recycler_view);
        tweetListView.setLayoutManager(new LinearLayoutManager(this));
        tAdapter = new TweetCardAdapter(dataSet);
        tweetListView.setAdapter(tAdapter);
        //((SimpleItemAnimator) tweetListView.getItemAnimator()).setSupportsChangeAnimations(false);;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkStateReceiver);
    }

    private void initBackground() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        //自定义广播
        //intentFilter.addAction("android.net.conn.CONNECTIVITY_STATE");
        networkStateReceiver = new NetworkStateReceiver();
        registerReceiver(networkStateReceiver, intentFilter);
        //另有数据库初始化
    }

    private void netRefresh() {
        RefreshTask task = new RefreshTask(this, refreshLayout, tAdapter);
        task.execute();
    }
}
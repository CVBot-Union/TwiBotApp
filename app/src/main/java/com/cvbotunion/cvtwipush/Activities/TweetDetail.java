package com.cvbotunion.cvtwipush.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.cvbotunion.cvtwipush.Adapters.TweetDetailCardAdapter;
import com.cvbotunion.cvtwipush.Model.TwitterMedia;
import com.cvbotunion.cvtwipush.Model.TwitterStatus;
import com.cvbotunion.cvtwipush.Model.TwitterUser;
import com.cvbotunion.cvtwipush.R;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.Collections;

public class TweetDetail extends AppCompatActivity {
    private MaterialToolbar mdToolbar;
    public SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView tweetDetailRecyclerView;
    private TweetDetailCardAdapter tAdapter;

    private String statusID;
    public ArrayList<TwitterStatus> dataSet = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_detail);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        statusID = bundle.getString("twitterStatusId");

        //start 模拟数据
        TwitterUser user = new TwitterUser("1","sb","sbsb","SB","http://101.200.184.98:8080/media/MO4FkO4N_400x400.jpg");
        TwitterMedia media = new TwitterMedia("1","http://101.200.184.98:8080/rami.jpg",TwitterMedia.IMAGE,"http://101.200.184.98:8080/rami.jpg");
        ArrayList<TwitterMedia> newList = new ArrayList<>();
        newList.add(media);
        newList.add(media);
        newList.add(media);
        TwitterStatus status1 = new TwitterStatus("11:14","1","测试",user,newList);
        TwitterStatus status2 = new TwitterStatus("11:15","2","这是一条回复",user,newList, TwitterStatus.REPLY, "12345");
        dataSet.add(status1);
        dataSet.add(status2);
        //end 模拟数据

        initView();
        initRecyclerView();

    }

    private void initView(){
        tweetDetailRecyclerView = findViewById(R.id.tweet_detail_recycler_view);
        mdToolbar = findViewById(R.id.detail_top_tool_bar);
        //setSupportActionBar(mdToolbar);
        mdToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        swipeRefreshLayout = findViewById(R.id.tweet_detail_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //netRefresh(chipGroup.getCheckedChipId());
                swipeRefreshLayout.setRefreshing(true);
                initRecyclerView();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void initRecyclerView(){
        layoutManager = new LinearLayoutManager(this);
        tweetDetailRecyclerView.setLayoutManager(layoutManager);
        //Collections.reverse(dataSet);
        tAdapter = new TweetDetailCardAdapter(dataSet,this);
        tweetDetailRecyclerView.setAdapter(tAdapter);
        tweetDetailRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        ((SimpleItemAnimator) tweetDetailRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        tweetDetailRecyclerView.scrollToPosition(dataSet.size()-1);
    }
}
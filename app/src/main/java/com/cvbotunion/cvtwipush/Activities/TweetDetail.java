package com.cvbotunion.cvtwipush.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.cvbotunion.cvtwipush.Adapters.TweetCardAdapter;
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
    private RecyclerView tweetDetailList;
    private TweetDetailCardAdapter tAdapter;

    private String statusID;
    public ArrayList<TwitterStatus> dataSet = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_detail);

        //start 模拟数据
        TwitterUser user = new TwitterUser("1","sb","sbsb","SB","http://101.200.184.98:8080/media/MO4FkO4N_400x400.jpg");
        TwitterMedia media = new TwitterMedia("1","http://101.200.184.98:8080/media/MO4FkO4N_400x400.jpg",TwitterMedia.IMAGE,"http://101.200.184.98:8080/media/MO4FkO4N_400x400.jpg");
        ArrayList<TwitterMedia> newList = new ArrayList<>();
        newList.add(media);
        newList.add(media);
        newList.add(media);
        TwitterStatus status = new TwitterStatus("11:14","1","测试",user,newList);

        dataSet.add(status);
        dataSet.add(status);
        //end 模拟数据

        initView();
        initRecyclerView();
    }

    private void initView(){
        tweetDetailList = findViewById(R.id.tweet_detail_recycler_view);
        mdToolbar = findViewById(R.id.detail_top_app_bar);
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
                initRecyclerView();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void initRecyclerView(){
        layoutManager = new LinearLayoutManager(this);
        tweetDetailList.setLayoutManager(layoutManager);
        Collections.reverse(dataSet);
        tAdapter = new TweetDetailCardAdapter(dataSet,this);
        tweetDetailList.setAdapter(tAdapter);
        tweetDetailList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        ((SimpleItemAnimator) tweetDetailList.getItemAnimator()).setSupportsChangeAnimations(false);
        tweetDetailList.scrollToPosition(dataSet.size()-1);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Bundle bundle = intent.getExtras();
        statusID = bundle.getString("twitterStatusID");
    }

}
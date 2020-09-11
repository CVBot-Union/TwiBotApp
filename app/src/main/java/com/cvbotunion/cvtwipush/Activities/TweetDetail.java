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
import com.cvbotunion.cvtwipush.DBModel.DBTwitterStatus;
import com.cvbotunion.cvtwipush.Model.TwitterMedia;
import com.cvbotunion.cvtwipush.Model.TwitterStatus;
import com.cvbotunion.cvtwipush.Model.TwitterUser;
import com.cvbotunion.cvtwipush.R;
import com.google.android.material.appbar.MaterialToolbar;

import org.litepal.LitePal;

import java.util.ArrayList;

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
        DBTwitterStatus dbStatus = LitePal.where("tid = ?", statusID).findFirst(DBTwitterStatus.class);

        TwitterStatus status = dbStatus.toTwitterStatus();
        dataSet.add(status);
        if(status.getTweetType() == TwitterStatus.REPLY) {
            //dbStatus = LitePal.where("tid = ?", status.in_reply_to_status_id).findFirst(DBTwitterStatus.class);
            //TwitterStatus replyToStatus = dbStatus.toTwitterStatus();
            TwitterStatus replyToStatus = new TwitterStatus("11:15", "2", "被回复推文", status.user, status.media);
            dataSet.add(0, replyToStatus);
        }

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
        tAdapter = new TweetDetailCardAdapter(dataSet,this);
        tweetDetailRecyclerView.setAdapter(tAdapter);
        tweetDetailRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        ((SimpleItemAnimator) tweetDetailRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        tweetDetailRecyclerView.scrollToPosition(dataSet.size()-1);
    }
}
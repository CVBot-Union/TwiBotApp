package com.cvbotunion.cvtwipush.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.cvbotunion.cvtwipush.Adapters.TweetDetailCardAdapter;
import com.cvbotunion.cvtwipush.DBModel.DBTwitterStatus;
import com.cvbotunion.cvtwipush.Model.TwitterStatus;
import com.cvbotunion.cvtwipush.R;
import com.cvbotunion.cvtwipush.Service.WebService;
import com.google.android.material.appbar.MaterialToolbar;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

import org.json.JSONObject;
import org.litepal.LitePal;

import java.util.ArrayList;

import okhttp3.Response;

public class TweetDetail extends AppCompatActivity {
    private MaterialToolbar mdToolbar;
    public RefreshLayout refreshLayout;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView tweetDetailRecyclerView;
    private TweetDetailCardAdapter tAdapter;

    private String statusID;
    private String tweetFormat;
    public ArrayList<TwitterStatus> dataSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_detail);
        dataSet = new ArrayList<>();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        statusID = bundle.getString("twitterStatusId");
        tweetFormat = bundle.getString("tweetFormat");
        DBTwitterStatus dbStatus = LitePal.where("tsid = ?", statusID).findFirst(DBTwitterStatus.class);

        TwitterStatus status = dbStatus.toTwitterStatus();
        dataSet.add(status);
        if(status.getTweetType() == TwitterStatus.REPLY) {
            TwitterStatus replyToStatus;
            //DBTwitterStatus dbReplyToStatus = LitePal.where("tsid = ?", status.in_reply_to_status_id).findFirst(DBTwitterStatus.class);
            //if(dbReplyToStatus != null) {
            //    replyToStatus = dbReplyToStatus.toTwitterStatus();
            //} else {
            //    getStatusNotInDB(status.in_reply_to_status_id);
            //}
            replyToStatus = new TwitterStatus("11:15", "3", "被回复推文", status.user, status.media);
            if(LitePal.where("tsid = ?", replyToStatus.id).find(DBTwitterStatus.class).isEmpty()) {
                DBTwitterStatus dbTwitterStatus = new DBTwitterStatus(replyToStatus);
                dbTwitterStatus.save();
            }
            dataSet.add(0, replyToStatus);
        }

        initView();
        initRecyclerView();

    }

    private void initView(){
        tweetDetailRecyclerView = findViewById(R.id.tweet_detail_recycler_view);
        mdToolbar = findViewById(R.id.detail_top_tool_bar);
        mdToolbar.setNavigationOnClickListener(view -> onBackPressed());
        refreshLayout = findViewById(R.id.tweet_detail_refresh_layout);
        refreshLayout.setEnableLoadMore(false);  //关闭上拉加载功能
        refreshLayout.setEnableScrollContentWhenRefreshed(false);  //在刷新完成时不滚动列表，避免与initRecyclerView的滚动操作冲突
        refreshLayout.setHeaderTriggerRate(0.7f);  //触发刷新距离 与 HeaderHeight 的比率
        refreshLayout.setOnRefreshListener(refreshlayout -> dataSet.get(dataSet.size()-1).queryTranslations(new Handler(),tAdapter, refreshlayout));
    }

    private void initRecyclerView(){
        layoutManager = new LinearLayoutManager(this);
        tweetDetailRecyclerView.setLayoutManager(layoutManager);
        tAdapter = new TweetDetailCardAdapter(dataSet,this, tweetFormat);
        tweetDetailRecyclerView.setAdapter(tAdapter);
        tweetDetailRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        ((SimpleItemAnimator) tweetDetailRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        tweetDetailRecyclerView.scrollToPosition(dataSet.size()-1);
    }

    public void getStatusNotInDB(final String statusId) {
        refreshLayout.autoRefreshAnimationOnly();
        new Thread() {
            @Override
            public void run() {
                try {
                    Response response = Timeline.connection.webService.get(WebService.SERVER_API+"tweet/"+statusId);
                    if(response.code()==200) {
                        JSONObject resJson = new JSONObject(response.body().string());
                        response.close();
                        if(!resJson.getBoolean("success")) {
                            Log.e("TweetDetail.getStatusNotInDB", resJson.toString());
                        }
                        dataSet.add(0, new TwitterStatus(resJson.getJSONObject("response").getJSONObject("tweet"),true));
                    } else {
                        Log.e("TweetDetail.getStatusNotInDB", response.message());
                        response.close();
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                } finally {
                    refreshLayout.autoRefresh();
                }
            }
        }.start();
    }
}

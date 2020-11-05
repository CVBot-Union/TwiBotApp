package com.cvbotunion.cvtwipush.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.cvbotunion.cvtwipush.Adapters.TweetDetailCardAdapter;
import com.cvbotunion.cvtwipush.DBModel.DBTwitterStatus;
import com.cvbotunion.cvtwipush.Model.TwitterStatus;
import com.cvbotunion.cvtwipush.R;
import com.cvbotunion.cvtwipush.Service.WebService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

import org.json.JSONObject;
import org.litepal.LitePal;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import okhttp3.Response;
// TODO 考虑修改为Fragment
public class TweetDetail extends AppCompatActivity {
    private MaterialToolbar mdToolbar;
    public RefreshLayout refreshLayout;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView tweetDetailRecyclerView;
    private TweetDetailCardAdapter tAdapter;

    private String tweetFormat;
    public ArrayList<TwitterStatus> dataSet;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_detail);
        handler = new Handler();
        dataSet = new ArrayList<>();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        TwitterStatus status = bundle.getParcelable("twitterStatus");
        tweetFormat = bundle.getString("tweetFormat");

        dataSet.add(status);

        initView();
        initRecyclerView();

        if(status.getTweetType() == TwitterStatus.REPLY) {
            DBTwitterStatus dbReplyToStatus = LitePal.where("tsid = ?", status.in_reply_to_status_id).findFirst(DBTwitterStatus.class);
            if(dbReplyToStatus != null) {
                dataSet.add(0, dbReplyToStatus.toTwitterStatus());
                tAdapter.notifyDataSetChanged();
            } else {
                getStatusNotInDB(status.in_reply_to_status_id);
            }
        } else if(status.getTweetType() == TwitterStatus.QUOTE) {
            DBTwitterStatus dbQuotedStatus = LitePal.where("tsid = ?", status.quoted_status_id).findFirst(DBTwitterStatus.class);
            if(dbQuotedStatus != null) {
                dataSet.add(0, dbQuotedStatus.toTwitterStatus());
                tAdapter.notifyDataSetChanged();
            } else {
                getStatusNotInDB(status.quoted_status_id);
            }
        }
    }

    private void initView(){
        tweetDetailRecyclerView = findViewById(R.id.tweet_detail_recycler_view);
        mdToolbar = findViewById(R.id.detail_top_tool_bar);
        mdToolbar.setNavigationOnClickListener(view -> onBackPressed());
        refreshLayout = findViewById(R.id.tweet_detail_refresh_layout);
        refreshLayout.setEnableLoadMore(false);  //关闭上拉加载功能
        refreshLayout.setEnableScrollContentWhenRefreshed(false);  //在刷新完成时不滚动列表，避免与initRecyclerView的滚动操作冲突
        refreshLayout.setHeaderTriggerRate(0.7f);  //触发刷新距离 与 HeaderHeight 的比率
        refreshLayout.setOnRefreshListener(refreshlayout -> dataSet.get(dataSet.size()-1).queryTranslations(handler ,tAdapter, refreshlayout));
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
        new Thread(() -> {
            boolean success = false;
            try {
                Response response = Timeline.connection.webService.get(WebService.SERVER_API+"tweet/"+statusId+"?groupID="+Timeline.getCurrentGroup().id);
                if(response.code()==200) {
                    JSONObject resJson = new JSONObject(response.body().string());
                    response.close();
                    if(!resJson.getBoolean("success")) {
                        Log.e("TweetDetail.getStatusNotInDB", resJson.toString());
                    }
                    dataSet.add(0, new TwitterStatus(resJson.getJSONObject("response"),true));
                    success = true;
                } else {
                    Log.e("TweetDetail.getStatusNotInDB", response.message());
                    response.close();
                }
            } catch(Exception e) {
                Log.e("TweetDetail.getStatusNotInDB", e.toString());
            } finally {
                boolean finalSuccess = success;
                handler.post(() -> {
                    refreshLayout.finishRefresh();
                    if(finalSuccess) {
                        tAdapter.notifyDataSetChanged();
                    } else {
                        Snackbar.make(tweetDetailRecyclerView, "获取父推文失败", 1000).show();
                    }
                });
            }
        }).start();
    }
}

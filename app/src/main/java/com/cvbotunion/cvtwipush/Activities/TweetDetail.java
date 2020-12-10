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
import com.google.android.material.snackbar.Snackbar;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import okhttp3.Response;
// TODO 考虑修改为Fragment
public class TweetDetail extends AppCompatActivity {
    private MaterialToolbar mdToolbar;
    public RefreshLayout refreshLayout;
    private RecyclerView tweetDetailRecyclerView;
    private TweetDetailCardAdapter tAdapter;

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
        dataSet.add(status);

        initView();
        initRecyclerView();

        getParentTweets(status);
        tweetDetailRecyclerView.scrollToPosition(dataSet.size()-1);
    }

    private void initView(){
        tweetDetailRecyclerView = findViewById(R.id.tweet_detail_recycler_view);
        mdToolbar = findViewById(R.id.detail_top_tool_bar);
        mdToolbar.setNavigationOnClickListener(view -> onBackPressed());
        refreshLayout = findViewById(R.id.tweet_detail_refresh_layout);
        refreshLayout.setEnableLoadMore(false);  //关闭上拉加载功能
        refreshLayout.setEnableScrollContentWhenRefreshed(false);  //在刷新完成时不滚动列表，避免与initRecyclerView的滚动操作冲突
        refreshLayout.setHeaderTriggerRate(0.7f);  //触发刷新距离 与 HeaderHeight 的比率
        refreshLayout.setOnRefreshListener(refreshlayout -> {
            tAdapter.notifyDataSetChanged();
            refreshlayout.finishRefresh(true);
        });
    }

    private void initRecyclerView(){
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        tweetDetailRecyclerView.setLayoutManager(layoutManager);
        tAdapter = new TweetDetailCardAdapter(dataSet,this);
        tweetDetailRecyclerView.setAdapter(tAdapter);
        tweetDetailRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        ((SimpleItemAnimator) tweetDetailRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    private void getParentTweets(final TwitterStatus mainStatus) {
        refreshLayout.autoRefreshAnimationOnly();
        new Thread(() -> {
            boolean success = false;
            TwitterStatus childStatus = mainStatus;
            String parentID;
            try {
                while(childStatus!=null) {
                    parentID = childStatus.quoted_status_id!=null?childStatus.quoted_status_id:childStatus.in_reply_to_status_id;
                    if(parentID!=null) {
                        DBTwitterStatus dbParentStatus = LitePal.where("tsid = ?", parentID).findFirst(DBTwitterStatus.class);
                        if(dbParentStatus!=null) {
                            childStatus = dbParentStatus.toTwitterStatus();
                            dataSet.add(0, childStatus);
                        } else {
                            childStatus = getStatusByID(parentID);
                            if (childStatus != null) dataSet.add(0, childStatus);
                        }
                    } else {
                        success = true;
                        break;
                    }
                }
            } catch(Exception e) {
                Log.e("TweetDetail.getParentTweets", e.toString());
            } finally {
                boolean finalSuccess = success;
                handler.post(() -> {
                    refreshLayout.finishRefresh();
                    tAdapter.notifyDataSetChanged();
                    if(!finalSuccess) {
                        Snackbar.make(tweetDetailRecyclerView, "获取父推文失败", 1000).show();
                    }
                });
            }
        }).start();
    }

    @Nullable
    private TwitterStatus getStatusByID(final String statusID) throws IOException, JSONException, ParseException {
        Response response = Timeline.connection.webService.get(WebService.SERVER_API+"/tweet/"+statusID+"?groupID="+Timeline.getCurrentGroup().id);
        if(response.code()==200) {
            JSONObject resJson = new JSONObject(response.body().string());
            response.close();
            if(!resJson.getBoolean("success")) {
                Log.e("TweetDetail.getStatusByID", resJson.toString());
            } else {
                return new TwitterStatus(resJson.getJSONObject("response"), true);
            }
        } else {
            Log.e("TweetDetail.getStatusByID", response.message());
            response.close();
        }
        return null;
    }
}

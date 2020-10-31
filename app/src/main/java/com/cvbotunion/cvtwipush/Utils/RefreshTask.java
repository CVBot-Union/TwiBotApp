package com.cvbotunion.cvtwipush.Utils;

import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.IntRange;

import com.cvbotunion.cvtwipush.Activities.Timeline;
import com.cvbotunion.cvtwipush.Adapters.TweetCardAdapter;
import com.cvbotunion.cvtwipush.DBModel.DBTwitterStatus;
import com.cvbotunion.cvtwipush.Model.RTGroup;
import com.cvbotunion.cvtwipush.Model.TwitterStatus;
import com.cvbotunion.cvtwipush.Service.WebService;
import com.cvbotunion.cvtwipush.TwiPush;
import com.google.android.material.snackbar.Snackbar;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

import org.json.JSONArray;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Response;

public class RefreshTask extends AsyncTask<String,Void,Boolean> {
    public final static int REFRESH = 0;
    public final static int LOADMORE = 1;

    private String url = WebService.SERVER_API+"tweet/range?";

    private final WeakReference<RefreshLayout> refreshLayoutRef;
    private final TweetCardAdapter tAdapter;
    private ArrayList<TwitterStatus> usedDataSet;
    private ArrayList<TwitterStatus> dataSet;
    //用于在特定chip下刷新
    private String checkedName;
    private final int mode;

    public RefreshTask(RefreshLayout refreshLayout, TweetCardAdapter tAdapter, @IntRange(from=0,to=1) int mode) {
        super();
        this.refreshLayoutRef=new WeakReference<>(refreshLayout);
        this.tAdapter = tAdapter;
        this.mode = mode;
    }

    public void setData(ArrayList<TwitterStatus> usedDataSet, ArrayList<TwitterStatus> dataSet, String checkedName) {
        this.usedDataSet = usedDataSet;
        this.dataSet = dataSet;
        this.checkedName = checkedName;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        try {
            if(mode == REFRESH) {
                url += ("group="+ Timeline.getCurrentGroup().id+"&afterID="+dataSet.get(0).id+"&sortKey=ASC");
                Response response = Timeline.connection.webService.get(url);
                if(response.code()==200) {
                    JSONObject resJson = new JSONObject(response.body().string());
                    response.close();
                    if(resJson.getBoolean("success")) {
                        JSONArray tweets = resJson.getJSONArray("response");
                        for(int i=0;i<tweets.length();i++) {
                            TwitterStatus tweet = new TwitterStatus(tweets.getJSONObject(i), true);
                            if (checkedName == null || tweet.user.name_in_group.equals(checkedName))
                                usedDataSet.add(0, tweet);
                            dataSet.add(0, tweet);
                        }
                    } else {
                        Log.e(TwiPush.TAG+":RefreshTask-REFRESH", resJson.toString());
                        return false;
                    }
                } else {
                    Log.e(TwiPush.TAG+":RefreshTask-REFRESH", response.message());
                    response.close();
                    return false;
                }
            } else if(mode == LOADMORE) {
                String lastId = dataSet.get(dataSet.size()-1).id;
                // 过滤得当前转推组的追踪
                StringBuilder condition = new StringBuilder();
                RTGroup cGroup = Timeline.getCurrentGroup();
                for(int i=0;i<cGroup.following.size();i++) {
                    condition.append(cGroup.following.get(i).id);
                    if(i!=cGroup.following.size()-1) { condition.append(","); }
                }
                List<DBTwitterStatus> dbTweets = LitePal.where("tuid in (?) AND tsid < ?", condition.toString(), lastId).order("tsid desc").find(DBTwitterStatus.class);
                for(DBTwitterStatus dbTweet : dbTweets) {
                    TwitterStatus tweet = dbTweet.toTwitterStatus();
                    if (checkedName == null || tweet.user.name_in_group.equals(checkedName))
                        usedDataSet.add(tweet);
                    dataSet.add(tweet);
                }
                if(dbTweets.size()< Timeline.LIMIT) {
                    lastId = dataSet.get(dataSet.size()-1).id;
                    int leftNeed = Timeline.LIMIT-dbTweets.size();
                    url += ("page=1&limit="+leftNeed+"&group="+ Timeline.getCurrentGroup().id+"&beforeID="+lastId+"&sortKey=DESC");
                    Response response = Timeline.connection.webService.get(url);
                    if(response.code()==200) {
                        JSONObject resJson = new JSONObject(response.body().string());
                        response.close();
                        if(resJson.getBoolean("success")) {
                            JSONArray tweets = resJson.getJSONArray("response");
                            for(int i=0;i<tweets.length();i++) {
                                TwitterStatus tweet = new TwitterStatus(tweets.getJSONObject(i), true);
                                if (checkedName == null || tweet.user.name_in_group.equals(checkedName))
                                    usedDataSet.add(tweet);
                                dataSet.add(tweet);
                            }
                        } else {
                            Log.e(TwiPush.TAG+":RefreshTask-LOADMORE", resJson.toString());
                            return false;
                        }
                    } else {
                        Log.e(TwiPush.TAG+":RefreshTask-LOADMORE", response.message());
                        response.close();
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        switch(mode) {
            case REFRESH:
                refreshLayoutRef.get().finishRefresh(result);
                break;
            case LOADMORE:
                refreshLayoutRef.get().finishLoadMore(result);
                break;
            default:
                Log.e(TwiPush.TAG,"wrong RefreshTask mode");
                break;
        }

        if(!result) {
            Snackbar.make(refreshLayoutRef.get().getLayout(),"加载失败",Snackbar.LENGTH_SHORT).show();
        } else {
            tAdapter.notifyDataSetChanged();
        }
    }
}

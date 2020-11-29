package com.cvbotunion.cvtwipush.Utils;

import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.IntRange;

import com.cvbotunion.cvtwipush.Activities.Timeline;
import com.cvbotunion.cvtwipush.Adapters.TweetCardAdapter;
import com.cvbotunion.cvtwipush.Model.TwitterStatus;
import com.cvbotunion.cvtwipush.Service.WebService;
import com.cvbotunion.cvtwipush.TwiPush;
import com.google.android.material.snackbar.Snackbar;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import okhttp3.Response;

public class RefreshTask extends AsyncTask<Void, Void, String> {
    public final static int REFRESH = 0;
    public final static int LOADMORE = 1;

    private String url = WebService.SERVER_API+"/tweet/range?";

    private final WeakReference<RefreshLayout> refreshLayoutRef;
    private final TweetCardAdapter tAdapter;
    private ArrayList<TwitterStatus> usedDataSet;
    private ArrayList<TwitterStatus> dataSet;
    //用于在特定chip下刷新
    private String checkedUid;
    private final int mode;

    public RefreshTask(RefreshLayout refreshLayout, TweetCardAdapter tAdapter, @IntRange(from=0,to=1) int mode) {
        super();
        this.refreshLayoutRef=new WeakReference<>(refreshLayout);
        this.tAdapter = tAdapter;
        this.mode = mode;
    }

    public void setData(ArrayList<TwitterStatus> usedDataSet, ArrayList<TwitterStatus> dataSet, String checkedUid) {
        this.usedDataSet = usedDataSet;
        this.dataSet = dataSet;
        this.checkedUid = checkedUid;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            if(mode == REFRESH) {
                String beforeID = dataSet.isEmpty()?"0":dataSet.get(0).id;
                if(beforeID.equals("0")) {
                    url += ("page=1&limit="+Timeline.LIMIT+"&group="+ Timeline.getCurrentGroup().id+"&beforeID=0&sortKey=DESC");
                } else {
                    int leftTweetsCount = countBehind(beforeID);
                    if(leftTweetsCount>2*Timeline.LIMIT) {
                        dataSet.clear();
                        usedDataSet.clear();
                        url += ("page=1&limit=" + 2*Timeline.LIMIT + "&group=" + Timeline.getCurrentGroup().id + "&beforeID=" + beforeID + "&sortKey=ASC");
                    } else {
                        url += ("page=1&limit=" + leftTweetsCount + "&group=" + Timeline.getCurrentGroup().id + "&beforeID=" + beforeID + "&sortKey=ASC");
                    }
                }
                Response response = Timeline.connection.webService.get(url);
                if(response.code()==200) {
                    JSONObject resJson = new JSONObject(response.body().string());
                    response.close();
                    if(resJson.getBoolean("success")) {
                        JSONArray tweets = resJson.getJSONArray("response");
                        for(int i=0;i<tweets.length();i++) {
                            TwitterStatus tweet = new TwitterStatus(tweets.getJSONObject(i), true);
                            Timeline.getCurrentGroup().following.forEach(tu -> {
                                if(tu.id.equals(tweet.user.id)) tweet.user.name_in_group = tu.name_in_group;
                            });
                            if (checkedUid == null || tweet.user.id.equals(checkedUid)) {
                                if (beforeID.equals("0")) {
                                    usedDataSet.add(tweet);
                                } else {
                                    usedDataSet.add(0, tweet);
                                }
                            }
                            if (beforeID.equals("0")) {
                                dataSet.add(tweet);
                            } else {
                                dataSet.add(0, tweet);
                            }
                        }
                    } else {
                        Log.e(TwiPush.TAG+":RefreshTask-REFRESH", resJson.toString());
                        return "刷新失败："+resJson.getJSONObject("response").toString();
                    }
                } else {
                    Log.e(TwiPush.TAG+":RefreshTask-REFRESH", response.code()+" "+response.message());
                    int code = response.code();
                    response.close();
                    return "刷新失败，请检查网络连接("+code+")";
                }
            } else if(mode == LOADMORE) {
                String lastId = dataSet.isEmpty() ? String.valueOf(Long.MAX_VALUE) : dataSet.get(dataSet.size() - 1).id;
                url += ("page=1&limit=" + Timeline.LIMIT + "&group=" + Timeline.getCurrentGroup().id + "&afterID=" + lastId + "&sortKey=DESC");
                Response response = Timeline.connection.webService.get(url);
                if (response.code() == 200) {
                    JSONObject resJson = new JSONObject(response.body().string());
                    response.close();
                    if (resJson.getBoolean("success")) {
                        JSONArray tweets = resJson.getJSONArray("response");
                        for (int i = 0; i < tweets.length(); i++) {
                            TwitterStatus tweet = new TwitterStatus(tweets.getJSONObject(i), true);
                            Timeline.getCurrentGroup().following.forEach(tu -> {
                                if (tu.id.equals(tweet.user.id))
                                    tweet.user.name_in_group = tu.name_in_group;
                            });
                            if (checkedUid == null || tweet.user.id.equals(checkedUid))
                                usedDataSet.add(tweet);
                            dataSet.add(tweet);
                        }
                    } else {
                        Log.e(TwiPush.TAG + ":RefreshTask-LOADMORE", resJson.toString());
                        return "加载失败："+resJson.getJSONObject("response").toString();
                    }
                } else {
                    Log.e(TwiPush.TAG + ":RefreshTask-LOADMORE", response.code()+" "+response.message());
                    int code = response.code();
                    response.close();
                    return "加载失败，请检查网络连接("+code+")";
                }
            }
        } catch (Exception e) {
            Log.e(TwiPush.TAG+":RefreshTask", e.toString());
            return "刷新/加载失败";
        }
        return "success";
    }

    private int countBehind(String tweetID) throws Exception {
        String cntUrl = WebService.SERVER_API+"/timeline-behind-count?group="+Timeline.getCurrentGroup().id+"&afterID="+tweetID;
        Response response = Timeline.connection.webService.get(cntUrl);
        if(response.code()==200) {
            JSONObject resJson = new JSONObject(response.body().string());
            response.close();
            return resJson.getInt("response");
        } else {
            int code = response.code();
            String msg = response.message();
            response.close();
            throw new Exception(code+" "+msg);
        }
    }

    @Override
    protected void onPostExecute(String result) {
        boolean flag = result.equals("success");
        switch(mode) {
            case REFRESH:
                refreshLayoutRef.get().finishRefresh(flag);
                break;
            case LOADMORE:
                refreshLayoutRef.get().finishLoadMore(flag);
                break;
            default:
                Log.e(TwiPush.TAG,"wrong RefreshTask mode");
                break;
        }

        if(!flag) {
            Snackbar.make(refreshLayoutRef.get().getLayout(),result,Snackbar.LENGTH_SHORT).show();
        } else {
            tAdapter.notifyDataSetChanged();
        }
    }
}

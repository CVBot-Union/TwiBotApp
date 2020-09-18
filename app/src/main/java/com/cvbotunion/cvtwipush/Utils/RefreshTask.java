package com.cvbotunion.cvtwipush.Utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.IntRange;

import com.cvbotunion.cvtwipush.Adapters.TweetCardAdapter;
import com.cvbotunion.cvtwipush.DBModel.DBTwitterStatus;
import com.cvbotunion.cvtwipush.Model.TwitterMedia;
import com.cvbotunion.cvtwipush.Model.TwitterStatus;
import com.cvbotunion.cvtwipush.Model.TwitterUser;
import com.cvbotunion.cvtwipush.TwiPush;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

import org.litepal.LitePal;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class RefreshTask extends AsyncTask<String,Void,Boolean> {
    public final static int REFRESH = 0;
    public final static int LOAD_MORE = 1;

    private WeakReference<Context> contextRef;
    private WeakReference<RefreshLayout> refreshLayoutRef;
    private TweetCardAdapter tAdapter;
    private ArrayList<TwitterStatus> usedDataSet;
    private ArrayList<TwitterStatus> dataSet;
    //用于在特定chip下刷新
    private String checkedName;
    private int mode;

    public RefreshTask(Context context, RefreshLayout refreshLayout, TweetCardAdapter tAdapter, @IntRange(from=0,to=1) int mode) {
        super();
        this.contextRef = new WeakReference<>(context);
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
        //实际应用中，此处与服务器通信以获取数据
        try {
            if(mode == REFRESH) {
                TwitterUser user = new TwitterUser("3", "相羽あいな", "aibaaiai", "相羽爱奈", "http://101.200.184.98:8080/aiai.jpg");
                TwitterMedia media = new TwitterMedia("4", "http://101.200.184.98:8080/rami.jpg", TwitterMedia.IMAGE, "http://101.200.184.98:8080/rami.jpg");
                TwitterMedia media1 = new TwitterMedia("3", "http://101.200.184.98:8080/nana.jpg", TwitterMedia.IMAGE, "http://101.200.184.98:8080/nana.jpg");
                ArrayList<TwitterMedia> mediaList = new ArrayList<>();
                mediaList.add(media);
                mediaList.add(media1);
                TwitterStatus tweet = new TwitterStatus("12:34", "5", "新增项", user, mediaList, TwitterStatus.REPLY, "123456");
                if (LitePal.where("tid = ?", tweet.id).find(DBTwitterStatus.class).isEmpty()) {
                    DBTwitterStatus dbStatus = new DBTwitterStatus(tweet);
                    dbStatus.save();
                }
                if (checkedName == null || tweet.user.name.equals(checkedName))
                    usedDataSet.add(0, tweet);
                dataSet.add(0, tweet);
            } else if(mode == LOAD_MORE) {
                // 初步设想
                // 获取顺序：数据库 -> 服务器
                // 每次最大数目：TweetList.EVERY_COUNT
                // 有必要实现一个按id_str排序的类/方法
            }
            Thread.sleep(1000);
        } catch (InterruptedException e) {
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
            case LOAD_MORE:
                refreshLayoutRef.get().finishLoadMore(result);
                break;
            default:
                Log.e(TwiPush.TAG,"wrong RefreshTask mode");
                break;
        }
        if(!result) {
            Toast.makeText(contextRef.get(), "刷新失败", Toast.LENGTH_SHORT).show();
        } else {
            tAdapter.notifyDataSetChanged();
        }
    }
}

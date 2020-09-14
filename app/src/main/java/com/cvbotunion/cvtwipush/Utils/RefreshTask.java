package com.cvbotunion.cvtwipush.Utils;

import android.os.AsyncTask;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cvbotunion.cvtwipush.DBModel.DBTwitterStatus;
import com.cvbotunion.cvtwipush.Model.TwitterMedia;
import com.cvbotunion.cvtwipush.Model.TwitterStatus;
import com.cvbotunion.cvtwipush.Model.TwitterUser;
import com.google.android.material.snackbar.Snackbar;

import org.litepal.LitePal;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class RefreshTask extends AsyncTask<String,Void,Boolean> {
    private WeakReference<CoordinatorLayout> parentViewRef;
    private WeakReference<SwipeRefreshLayout> refreshLayoutRef;
    private ArrayList<TwitterStatus> usedDataSet;
    private ArrayList<TwitterStatus> dataSet;
    //用于在特定chip下刷新
    private String checkedName;

    public RefreshTask(CoordinatorLayout coordinatorLayout, SwipeRefreshLayout refreshLayout) {
        super();
        this.parentViewRef=new WeakReference<>(coordinatorLayout);
        this.refreshLayoutRef=new WeakReference<>(refreshLayout);
    }
    public void setData(ArrayList<TwitterStatus> usedDataSet, ArrayList<TwitterStatus> dataSet, String checkedName) {
        this.usedDataSet = usedDataSet;
        this.dataSet = dataSet;
        this.checkedName = checkedName;
    }

    @Override
    protected void onPreExecute() {
        refreshLayoutRef.get().setRefreshing(true);
    }

    @Override
    protected Boolean doInBackground(String... params) {
        //实际应用中，此处与服务器通信以获取数据
        try {
            TwitterUser user = new TwitterUser("3","相羽あいな","aibaaiai","相羽爱奈","http://101.200.184.98:8080/aiai.jpg");
            TwitterMedia media = new TwitterMedia("2","http://101.200.184.98:8080/media/aqua.jpg",TwitterMedia.IMAGE,"http://101.200.184.98:8080/media/aqua.jpg");
            TwitterMedia media1 = new TwitterMedia("3","http://101.200.184.98:8080/nana.jpg",TwitterMedia.IMAGE,"http://101.200.184.98:8080/nana.jpg");
            ArrayList<TwitterMedia> mediaList = new ArrayList<>();
            mediaList.add(media);
            mediaList.add(media1);
            TwitterStatus tweet=new TwitterStatus("12:34", "4", "新增项", user, mediaList, TwitterStatus.REPLY,"123456");
            if(LitePal.where("tid = ?", tweet.id).find(DBTwitterStatus.class).isEmpty()) {
                DBTwitterStatus dbStatus = new DBTwitterStatus(tweet);
                dbStatus.save();
            }
            if(checkedName == null || tweet.user.name.equals(checkedName))
                usedDataSet.add(0, tweet);
            dataSet.add(0, tweet);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        refreshLayoutRef.get().setRefreshing(false);
        if(!result)
            Snackbar.make(parentViewRef.get(), "刷新失败", Snackbar.LENGTH_SHORT).show();
    }
}

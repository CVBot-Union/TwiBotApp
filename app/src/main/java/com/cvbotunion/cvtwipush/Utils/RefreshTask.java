package com.cvbotunion.cvtwipush.Utils;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cvbotunion.cvtwipush.Adapters.TweetCardAdapter;
import com.cvbotunion.cvtwipush.Model.TwitterMedia;
import com.cvbotunion.cvtwipush.Model.TwitterStatus;
import com.cvbotunion.cvtwipush.Model.TwitterUser;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class RefreshTask extends AsyncTask<String,Void,Boolean> {
    private WeakReference<Context> contextRef;
    private WeakReference<SwipeRefreshLayout> refreshLayoutRef;
    private ArrayList<TwitterStatus> dataSet;
    //用于在特定chip下刷新
    private int checkedId;

    public RefreshTask(Context context, SwipeRefreshLayout refreshLayout, ArrayList<TwitterStatus> dataSet) {
        super();
        this.contextRef=new WeakReference<>(context);
        this.refreshLayoutRef=new WeakReference<>(refreshLayout);
        this.dataSet=dataSet;
    }
    public void setCheckedId(int checkedId) {
        this.checkedId=checkedId;
    }

    @Override
    protected void onPreExecute() {
        refreshLayoutRef.get().setRefreshing(true);
    }

    @Override
    protected Boolean doInBackground(String... params) {
        //实际应用中，此处与服务器通信以获取数据
        try {
            TwitterUser user = new TwitterUser("2","大亏","sbsb","大亏","http://101.200.184.98:8080/media/aqua.jpg");
            TwitterMedia media = new TwitterMedia("2","http://101.200.184.98:8080/media/aqua.jpg",TwitterMedia.IMAGE,"http://101.200.184.98:8080/media/aqua.jpg");
            ArrayList<TwitterMedia> newList = new ArrayList<>();
            newList.add(media);
            TwitterStatus twitter=new TwitterStatus("12:34", "2", "新增项", user, newList, "123456");
            dataSet.add(0, twitter);
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
            Toast.makeText(contextRef.get(), "刷新失败", Toast.LENGTH_SHORT).show();
    }
}

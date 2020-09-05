package com.example.testapp;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.testapp.Adapters.TweetCardAdapter;

import java.lang.ref.WeakReference;

public class RefreshTask extends AsyncTask<String,Void,Boolean> {
    private WeakReference<Context> contextRef;
    private WeakReference<SwipeRefreshLayout> refreshLayoutRef;
    private TweetCardAdapter adapter;

    public RefreshTask(Context context, SwipeRefreshLayout refreshLayout, TweetCardAdapter adapter) {
        super();
        this.contextRef=new WeakReference<>(context);
        this.refreshLayoutRef=new WeakReference<>(refreshLayout);
        this.adapter=adapter;
    }

    @Override
    protected void onPreExecute() {
        refreshLayoutRef.get().setRefreshing(true);
    }

    @Override
    protected Boolean doInBackground(String... params) {
        //实际应用中，此处与服务器通信以获取数据
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        refreshLayoutRef.get().setRefreshing(false);
        if(result)
            //此处变相实现了断网重连后的重新加载资源
            adapter.notifyDataSetChanged();
        else
            Toast.makeText(contextRef.get(), "刷新失败", Toast.LENGTH_SHORT).show();
    }
}

package com.example.testapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.LitePal;
import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<String> twiContents = new ArrayList<>(Arrays.asList("apple", "bannner", "orange", "watermelon", "pear", "grape",
            "pineapple", "strawberry", "cherry", "mango", "apple", "bannner", "orange",
            "watermelon", "pear", "grape", "pineapple", "strawberry", "cherry", "mango"));
    private List<String[]> imgPathsList = new ArrayList<>(Arrays.asList(
            new String[]{"https://i0.hdslb.com/bfs/archive/22650682fd25a4a5aa96dd9ef53190c6b8d54912.png"},
            new String[]{"https://himg.bdimg.com/sys/portrait/item/5815b7e7b7c9d8df9020.jpg", "https://upload-images.jianshu.io/upload_images/677256-1fb6afa3593d1b2c.jpg"},
            new String[]{""}));
    private List<String> videoPathList = new ArrayList<>(Arrays.asList(
            "http://101.200.184.98:8080/media/ms7Igd7pov9gBxfy.mp4", "", "http://101.200.184.98:8080/media/7v1nYn8AjwwP-O4B.mp4"));
    private String s = "斎藤ニコル\n08-05 18:02:28\n\u6700\u8fd1\u3059\u3063\u3054\u304f\u6691\u3044\u306d(>_<)\n\n\u79c1\u306f\u4eca\u65e5\u30b9\u30a4\u30ab\u3092\u98df\u3079\u305f\u3088\ud83c\udf49\n\u307f\u306a\u3055\u3093\u3082\u590f\u3063\u307d\u3044\u3053\u3068\u4f55\u304b\u3057\u307e\u3057\u305f\u304b\u30fc\uff1f\ud83d\udc93";
    private NetworkChangeReceiver networkChangeReceiver;
    private SwipeRefreshLayout refreshLayout;
    private TwiAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setTitle("Home");
        if(twiContents.size()!=imgPathsList.size()||twiContents.size()!=videoPathList.size())
            Toast.makeText(MainActivity.this,"推文与媒体列表长度不一致\n请与开发者联系", Toast.LENGTH_LONG).show();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, intentFilter);
        //LitePal.getDatabase();
        //读取数据库中的数据
        //queryTwiData();
        twiContents.set(0, s);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        adapter = new TwiAdapter();
        recyclerView.setAdapter(adapter);
        refreshLayout = findViewById(R.id.swipe_refresh);
        refreshLayout.setColorSchemeResources(R.color.colorPrimary);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                netRefresh();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DialogActivity.cachedBmp.clear();
        unregisterReceiver(networkChangeReceiver);
    }

    public void netRefresh() {
        RefreshTask task = new RefreshTask();
        task.execute();
        //更新数据库数据
        //updateTwiData();
    }

    public void updateTwiData() {}

    public void queryTwiData() {}

    class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
            assert connectivityManager != null;
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if(networkInfo == null || !networkInfo.isAvailable()) Toast.makeText(context, "无网络连接", Toast.LENGTH_LONG).show();
        }
    }

    public class TwiAdapter extends RecyclerView.Adapter<TwiAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder{
            TextView textView;

            public ViewHolder(View view) {
                super(view);
                textView = view.findViewById(android.R.id.text1);
            }
        }

        public TwiAdapter() {}

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1,parent,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
            holder.textView.setText(twiContents.get(position));
            holder.textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent= new Intent(MainActivity.this, DialogActivity.class);
                    intent.putExtra("twi_content",twiContents.get(position));
                    intent.putExtra("img_paths", imgPathsList.get(position));
                    intent.putExtra("video_path", videoPathList.get(position));
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() { return twiContents.size(); }
    }

    public class TwiData extends LitePalSupport {
        @Column(nullable = false)
        private String content;

        private String[] imgPaths;

        @Column(defaultValue = "")
        private String videoPath;

        public TwiData(String content, String[] imgPaths, String videoPath) {
            this.content = content;
            this.imgPaths = imgPaths.clone();
            this.videoPath = videoPath;
        }

        public String getContent() {return content;}
        public void setContent(String content) {this.content = content;}

        public String[] getImgPaths() {return imgPaths;}
        public void setImgPaths(String[] imgPaths) {this.imgPaths = imgPaths.clone();}

        public String getVideoPath() {return videoPath;}
        public void setVideoPath(String videoPath) {this.videoPath = videoPath;}
    }

    class RefreshTask extends AsyncTask<String,Void,Boolean> {

        @Override
        protected void onPreExecute() {
            refreshLayout.setRefreshing(true);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            //实际应用中，此处与服务器通信以获取数据
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
            TwiData[] newTwiData = new TwiData[]{new TwiData("新增条目", new String[]{""}, ""), new TwiData("新增条目2", new String[]{""}, "")};
            for(TwiData singleTwi:newTwiData) {
                twiContents.add(0, singleTwi.getContent());
                imgPathsList.add(0, singleTwi.getImgPaths());
                videoPathList.add(0, singleTwi.getVideoPath());
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            refreshLayout.setRefreshing(false);
            if(result)
                adapter.notifyDataSetChanged();
            else
                Toast.makeText(MainActivity.this, "刷新失败", Toast.LENGTH_SHORT).show();
        }
    }
}
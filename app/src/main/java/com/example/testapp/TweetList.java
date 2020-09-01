package com.example.testapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.os.Bundle;

import com.example.testapp.Model.TwitterMedia;
import com.example.testapp.Model.TwitterStatus;
import com.example.testapp.Model.TwitterUser;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class TweetList extends AppCompatActivity {

    private RecyclerView tweetList;
    private TweetCardAdapter tAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private ArrayList<TwitterStatus> dataSet = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_list);

        TwitterUser user = new TwitterUser("1","sb","sbsb","SB","https://pbs.twimg.com/profile_images/1206156308602163200/MO4FkO4N_400x400.jpg");
        TwitterMedia media = new TwitterMedia("1","https://pbs.twimg.com/profile_images/1206156308602163200/MO4FkO4N_400x400.jpg",TwitterMedia.IMAGE,"https://pbs.twimg.com/profile_images/1206156308602163200/MO4FkO4N_400x400.jpg");
        ArrayList<TwitterMedia> newList = new ArrayList<>();
        newList.add(media);
        newList.add(media);
        newList.add(media);
        TwitterStatus status = new TwitterStatus("11:14","1","测试",user,newList);

        dataSet.add(status);
        dataSet.add(status);
        dataSet.add(status);
        dataSet.add(status);

        tweetList = (RecyclerView) findViewById(R.id.tweet_list_recycler_view);
        layoutManager = new LinearLayoutManager(this){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        tweetList.setLayoutManager(layoutManager);
        tAdapter = new TweetCardAdapter(dataSet);
        tweetList.setAdapter(tAdapter);
        ((SimpleItemAnimator) tweetList.getItemAnimator()).setSupportsChangeAnimations(false);
    }
}
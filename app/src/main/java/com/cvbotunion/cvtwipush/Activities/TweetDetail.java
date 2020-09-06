package com.cvbotunion.cvtwipush.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.cvbotunion.cvtwipush.R;
import com.google.android.material.appbar.MaterialToolbar;

public class TweetDetail extends AppCompatActivity {
    private MaterialToolbar mdToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_detail);
        mdToolbar = findViewById(R.id.detail_top_app_bar);
        //setSupportActionBar(mdToolbar);
        mdToolbar.setTitle("推文详情");
        mdToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
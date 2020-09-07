package com.cvbotunion.cvtwipush.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.cvbotunion.cvtwipush.Model.TwitterStatus;
import com.cvbotunion.cvtwipush.R;
import com.google.android.material.appbar.MaterialToolbar;

public class TweetDetail extends AppCompatActivity {
    private MaterialToolbar mdToolbar;
    private String statusID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_detail);
        mdToolbar = findViewById(R.id.detail_top_app_bar);
        //setSupportActionBar(mdToolbar);
        mdToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Bundle bundle = intent.getExtras();
        statusID = bundle.getString("twitterStatusID");
    }

}
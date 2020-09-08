package com.cvbotunion.cvtwipush.Adapters;

import android.content.Context;

import com.cvbotunion.cvtwipush.Model.TwitterStatus;

import java.util.ArrayList;

public class TweetDetailCardAdapter extends TweetCardAdapter {
    public TweetDetailCardAdapter(ArrayList<TwitterStatus> tweets, Context context) {
        super(tweets, context);
    }
}

package com.cvbotunion.cvtwipush.Adapters;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.cvbotunion.cvtwipush.Activities.ImageViewer;
import com.cvbotunion.cvtwipush.Activities.VideoViewer;
import com.cvbotunion.cvtwipush.CustomViews.TweetDetailCard;
import com.cvbotunion.cvtwipush.Model.TwitterMedia;
import com.cvbotunion.cvtwipush.Model.TwitterStatus;
import com.cvbotunion.cvtwipush.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;


public class TweetDetailCardAdapter extends RecyclerView.Adapter<TweetDetailCardAdapter.TweetDetailCardViewHolder> {

    public ArrayList<TwitterStatus> tweets;

    public static final int GET_DATA_SUCCESS = 1;
    public static final int NETWORK_ERROR = 2;
    public static final int SERVER_ERROR = 3;

    public boolean isConnected = true;
    public Handler handler;
    public Context context;
    private String tweetFormat;

    public TweetDetailCardAdapter(ArrayList<TwitterStatus> tweets,Context context,String tweetFormat){
        this.tweets = tweets;
        handler = new Handler();
        this.context = context;
        this.tweetFormat = tweetFormat;
    }

    public static class TweetDetailCardViewHolder extends RecyclerView.ViewHolder{
        public TweetDetailCard tweetCard;

        public TweetDetailCardViewHolder(@NonNull View itemView) {
            super(itemView);
            this.tweetCard = new TweetDetailCard(itemView.getContext(),itemView);
        }
    }

    @NonNull
    @Override
    public TweetDetailCardAdapter.TweetDetailCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tweet_detail_card,parent,false);
        return new TweetDetailCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final TweetDetailCardAdapter.TweetDetailCardViewHolder holder, final int position) {
        final CardView card = holder.itemView.findViewById(R.id.tweet_detail_card);

        holder.tweetCard.setBtn1OnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TwitterStatus tweet = tweets.get(position);
                ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("tweet", tweet.getFullText(tweetFormat));
                clipboardManager.setPrimaryClip(mClipData);
                //保存媒体
                String result = "成功";
                if(tweet.media != null && !tweet.media.isEmpty()) {
                    for (TwitterMedia singleMedia : tweet.media) {
                        if (!singleMedia.saveToFile(v.getContext())) {
                            result = "失败";
                            break;
                        }
                    }
                }
                Snackbar.make(v, "保存" + result, 1000).show();
            }
        });

        holder.tweetCard.setName(tweets.get(position).getUser().name_in_group);

        holder.tweetCard.setTweetText(tweets.get(position).getText());

        holder.tweetCard.getTweetStatusTextView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(tweets.get(position).getText() != null) {
                    ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData mClipData = ClipData.newPlainText("tweet", tweets.get(position).getText());
                    clipboardManager.setPrimaryClip(mClipData);
                    Snackbar.make(view, "已复制", 1000).show();
                }
                return true;
            }
        });

        switch(tweets.get(position).getTweetType()){
            case TwitterStatus.REPLY:
                holder.tweetCard.setType("回复");
                break;
            case TwitterStatus.QUOTE:
                holder.tweetCard.setType("转推");
                break;
            default:
                break;
        }

        holder.tweetCard.setTime(tweets.get(position).getCreated_at());

        if(tweets.get(position).user.cached_profile_image_preview != null){
            holder.tweetCard.setAvatarImg(tweets.get(position).user.cached_profile_image_preview);
        } else {
            tweets.get(position).user.downloadAvatar(this, handler, position);
        }

        int i=1;
        if(tweets.get(position).media != null && !tweets.get(position).media.isEmpty()) {
                if (tweets.get(position).media.size() <= 4 && tweets.get(position).media.get(0).type == TwitterMedia.IMAGE) {
                    holder.tweetCard.tweetImageInit(tweets.get(position).media.size());
                }

                for (final TwitterMedia media : tweets.get(position).media) {
                    switch (media.type) {
                        case TwitterMedia.IMAGE:
                            if (media.cached_image_preview != null) {
                                final int page = i;
                                holder.tweetCard.setImageOnClickListener(tweets.get(position).media.size(), i, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(v.getContext(), ImageViewer.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putInt("page", page);
                                        bundle.putString("twitterStatusId", tweets.get(position).id);
                                        intent.putExtras(bundle);
                                        v.getContext().startActivity(intent);
                                    }
                                });
                                holder.tweetCard.setTweetImage(tweets.get(position).media.size(), i, media.cached_image_preview);
                            } else {
                                media.loadImage(true,this, handler, position);
                            }
                            break;
                        case TwitterMedia.VIDEO:
                            holder.tweetCard.initVideo();
                            if (media.cached_image_preview != null) {
                                holder.tweetCard.setVideoOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(v.getContext(), VideoViewer.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putString("url", media.url);
                                        intent.putExtras(bundle);
                                        v.getContext().startActivity(intent);
                                    }
                                });
                                holder.tweetCard.setVideoBackground(media.cached_image_preview);
                            } else {
                                media.loadImage(true,this, handler, position);
                            }
                            break;
                    }
                    i += 1;
                }
        }

        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                try {
                    imm.hideSoftInputFromWindow(holder.itemView.getWindowToken(), 0);
                } catch(Exception e){
                    Log.i("warning",e.toString());
                }
                View view = ((Activity) context).getWindow().getCurrentFocus();
                if(view != null) {
                    view.clearFocus();
                }
            }
        });

        if(position == tweets.size()-1){
            holder.tweetCard.setTranslationMode(true);
            holder.tweetCard.translationTextInputLayout.setVisibility(View.VISIBLE);
            holder.tweetCard.copyToTextField.setVisibility(View.VISIBLE);
            holder.tweetCard.doneButton.setVisibility(View.VISIBLE);
            holder.tweetCard.setOnClickDoneButtonListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TwitterStatus tweet = tweets.get(tweets.size()-1);
                    ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData mClipData = ClipData.newPlainText("tweetAndTranslation", tweet.getFullText(tweetFormat, holder.tweetCard.getTranslatedText()));
                    clipboardManager.setPrimaryClip(mClipData);
                    String result = "成功";
                    if(tweet.media != null && !tweet.media.isEmpty()){
                        for (TwitterMedia singleMedia : tweet.media) {
                            if (!singleMedia.saveToFile(v.getContext())) {
                                result = "失败";
                                break;
                            }
                        }
                    }
                    Snackbar.make(v,"保存"+result,1000).show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }
}

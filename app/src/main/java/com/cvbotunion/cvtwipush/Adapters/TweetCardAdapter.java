package com.cvbotunion.cvtwipush.Adapters;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.cvbotunion.cvtwipush.Activities.ImageViewer;
import com.cvbotunion.cvtwipush.Activities.Timeline;
import com.cvbotunion.cvtwipush.Activities.TweetDetail;
import com.cvbotunion.cvtwipush.Activities.VideoViewer;
import com.cvbotunion.cvtwipush.DBModel.DBTwitterStatus;
import com.cvbotunion.cvtwipush.Model.TwitterMedia;
import com.cvbotunion.cvtwipush.Model.TwitterStatus;
import com.cvbotunion.cvtwipush.R;
import com.cvbotunion.cvtwipush.CustomViews.TweetCard;
import com.cvbotunion.cvtwipush.Utils.ImageLoader;
import com.google.android.material.snackbar.Snackbar;

import org.litepal.LitePal;

import java.util.ArrayList;

public class TweetCardAdapter extends RecyclerView.Adapter<TweetCardAdapter.TweetCardViewHolder> {

    public ArrayList<TwitterStatus> tweets;

    public static boolean isConnected = true;

    public Handler handler;
    public Context context;
    private final String tweetFormat;

    public TweetCardAdapter(ArrayList<TwitterStatus> tweets,Context context){
        this.tweets = tweets;
        this.handler = new Handler();
        this.context = context;
        this.tweetFormat = Timeline.getCurrentGroup().tweetFormat;
    }

    public static class TweetCardViewHolder extends RecyclerView.ViewHolder{
        public TweetCard tweetCard;

        public TweetCardViewHolder(@NonNull View itemView) {
            super(itemView);
            this.tweetCard = new TweetCard(itemView.getContext(),itemView);
        }
    }

    @NonNull
    @Override
    public TweetCardAdapter.TweetCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tweet_card,parent,false);
        return new TweetCardViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final TweetCardAdapter.TweetCardViewHolder holder, final int position) {
        //卡片
        holder.tweetCard.setOnClickListener(v -> {
            //参数传递
            AppCompatActivity activity = (AppCompatActivity) context;
            Intent intent = new Intent(v.getContext(), TweetDetail.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("twitterStatus",tweets.get(position));
            intent.putExtras(bundle);
            // TODO 考虑更换动画
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,holder.tweetCard.card,"activityOption");
            v.getContext().startActivity(intent,optionsCompat.toBundle());
        });

        holder.tweetCard.setQSButtonOnClickListener(v -> {
            TwitterStatus tweet = tweets.get(position);
            ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData mClipData = ClipData.newPlainText("tweet", tweet.getFullText(tweetFormat));
            clipboardManager.setPrimaryClip(mClipData);
            String result = context.getString(R.string.success);
            //保存媒体
            if (tweet.media != null && !tweet.media.isEmpty()) {
                for (TwitterMedia singleMedia : tweet.media) {
                    if (!singleMedia.saveToFile(v.getContext())) {
                        result = context.getString(R.string.failure);
                        break;
                    }
                }
            }
            Snackbar.make(v, context.getString(R.string.save) + result, Snackbar.LENGTH_SHORT).show();
        });

        //姓名
        holder.tweetCard.setName(tweets.get(position).getUser().name_in_group);

        //正文
        holder.tweetCard.setTweetText(tweets.get(position).getText());

        if(holder.tweetCard.getTweetStatusTextView().getUrls().length!=0) {
            holder.tweetCard.getTweetStatusTextView().setOnClickListener(view -> holder.tweetCard.performClick());
        } else {
            holder.tweetCard.setOnTouchListener((v, event) -> false);
        }

        //推文类型
        switch(tweets.get(position).getTweetType()){
            case TwitterStatus.REPLY:
                holder.tweetCard.setType(context.getString(R.string.reply));
                break;
            case TwitterStatus.QUOTE:
                holder.tweetCard.setType(context.getString(R.string.quoted));
                if(tweets.get(position).getText()==null||tweets.get(position).getText().equals("")){
                    holder.tweetCard.setTweetText(context.getString(R.string.retweet_only));
                }
                break;
            default:
                holder.tweetCard.setType("");
                break;
        }

        //发推时间
        holder.tweetCard.setTime(tweets.get(position).getCreated_at());

        //头像
        if(tweets.get(position).user.cached_profile_image != null){
            holder.tweetCard.setAvatarImg(tweets.get(position).user.cached_profile_image);
        } else if(isConnected && !tweets.get(position).user.avatarUnderProcessing) {
            ImageLoader.setAdapter(this, position).load(tweets.get(position).user);
        }

        //媒体
        int i=1;
        if(tweets.get(position).media != null && !tweets.get(position).media.isEmpty()) {
                if (tweets.get(position).media.size() <= 4 && tweets.get(position).media.get(0).type == TwitterMedia.IMAGE) {
                    holder.tweetCard.tweetImageInit(tweets.get(position).media.size());
                }

                for (final TwitterMedia media : tweets.get(position).media) {
                    switch (media.type) {
                        //图片
                        case TwitterMedia.IMAGE:
                            if (media.cached_image_preview != null) {
                                final int page = i;
                                holder.tweetCard.setImageOnClickListener(tweets.get(position).media.size(), i, v -> {
                                    Intent intent = new Intent(v.getContext(), ImageViewer.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("page", page);
                                    bundle.putParcelable("twitterStatus", tweets.get(position));
                                    intent.putExtras(bundle);
                                    v.getContext().startActivity(intent);
                                });
                                holder.tweetCard.setTweetImage(tweets.get(position).media.size(), i, media.cached_image_preview);
                            } else if(isConnected && !media.underProcessing) {
                                ImageLoader.setAdapter(this, position).load(media,true);
                            }
                            break;
                        //视频
                        case TwitterMedia.VIDEO:
                            holder.tweetCard.initVideo();
                            if (media.cached_image_preview != null) {
                                holder.tweetCard.setVideoOnClickListener(v -> {
                                    Intent intent = new Intent(v.getContext(), VideoViewer.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putString("url", media.url);
                                    intent.putExtras(bundle);
                                    v.getContext().startActivity(intent);
                                });
                                holder.tweetCard.setVideoBackground(media.cached_image_preview);
                            } else if(isConnected && !media.underProcessing) {
                                ImageLoader.setAdapter(this, position).load(media, true);
                            }
                            break;
                    }
                    i += 1;
                }
        }
        else {
            //无媒体的情况
            holder.tweetCard.hideAllMediaView();
        }
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }
}

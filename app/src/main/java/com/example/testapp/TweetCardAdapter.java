package com.example.testapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapp.Model.TwitterMedia;
import com.example.testapp.Model.TwitterStatus;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

public class TweetCardAdapter extends RecyclerView.Adapter<TweetCardAdapter.TweetCardViewHolder> {

    private ArrayList<TwitterStatus> tweets;

    public static final int GET_DATA_SUCCESS = 1;
    public static final int NETWORK_ERROR = 2;
    public static final int SERVER_ERROR = 3;

    public boolean isConnected = true;

    public TweetCardAdapter(ArrayList<TwitterStatus> tweets){
        this.tweets = tweets;
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
        TweetCardViewHolder tweetCardViewHolder = new TweetCardViewHolder(view);
        return tweetCardViewHolder;
    }

    public void downloadImage(final int type,final String path, final int position,@Nullable final Integer picturePosition) {
        if(isConnected) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        //把传过来的路径转成URL
                        URL url = new URL(path);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(10000);
                        int code = connection.getResponseCode();
                        if (code == 200) {
                            InputStream inputStream = connection.getInputStream();
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            switch (type) {
                                case TwitterMedia.AVATAR:
                                    tweets.get(position).user.cached_profile_image_preview = bitmap;
                                    break;
                                case TwitterMedia.VIDEO:
                                    tweets.get(position).media.get(picturePosition - 1).cached_image_preview = bitmap;
                                    break;
                                default:
                                    tweets.get(position).media.get(picturePosition - 1).cached_image_preview = bitmap;
                                    break;
                            }
                            notifyItemChanged(position);
                            inputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull TweetCardAdapter.TweetCardViewHolder holder, final int position) {

        holder.tweetCard.setName(tweets.get(position).getUser().nameInGroup);

        holder.tweetCard.setTweetText(tweets.get(position).getText());

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
            downloadImage(TwitterMedia.AVATAR,tweets.get(position).user.profile_image_url,position,null);
        }


        int i=1;
        if(!tweets.get(position).media.isEmpty()) {
            if(tweets.get(position).media.size()<=4 && tweets.get(position).media.get(0).type==TwitterMedia.IMAGE) {
                holder.tweetCard.tweetImageInit(tweets.get(position).media.size());
            }
            for (TwitterMedia media : tweets.get(position).media) {
                switch (media.type) {
                    case TwitterMedia.IMAGE:
                        if (media.cached_image_preview != null) {
                            holder.tweetCard.setImageOnClickListener(tweets.get(position).media.size(), i, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            });
                            holder.tweetCard.setTweetImage(tweets.get(position).media.size(), i, media.cached_image_preview);
                        } else {
                            downloadImage(TwitterMedia.IMAGE,media.reviewImageURL, position, i);
                        }
                        break;
                    case TwitterMedia.VIDEO:
                        holder.tweetCard.initVideo();
                        if (media.cached_image_preview != null) {
                            holder.tweetCard.setVideoOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View v) {

                                }
                            });
                            holder.tweetCard.setVideoBackground(media.cached_image_preview);
                            break;
                        } else {
                            downloadImage(TwitterMedia.VIDEO,media.reviewImageURL,position,1);
                        }
                        break;
                }
                i += 1;
            }
        }
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }
}

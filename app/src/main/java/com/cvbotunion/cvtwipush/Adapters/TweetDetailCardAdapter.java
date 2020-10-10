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
import com.cvbotunion.cvtwipush.Activities.TweetList;
import com.cvbotunion.cvtwipush.Activities.VideoViewer;
import com.cvbotunion.cvtwipush.CustomViews.TweetDetailCard;
import com.cvbotunion.cvtwipush.Model.TwitterMedia;
import com.cvbotunion.cvtwipush.Model.TwitterStatus;
import com.cvbotunion.cvtwipush.R;
import com.cvbotunion.cvtwipush.Service.WebService;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

import okhttp3.Response;


public class TweetDetailCardAdapter extends RecyclerView.Adapter<TweetDetailCardAdapter.TweetDetailCardViewHolder> {

    public ArrayList<TwitterStatus> tweets;

    public static final int GET_DATA_SUCCESS = 1;
    public static final int NETWORK_ERROR = 2;
    public static final int SERVER_ERROR = 3;
    public static boolean isConnected = true;

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
        } else if(isConnected && !tweets.get(position).user.avatarUnderProcessing) {
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
                            } else if(isConnected && !media.underProcessing) {
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
                            } else if(isConnected && !media.underProcessing) {
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
            final TwitterStatus lastTweet = tweets.get(position);
            holder.tweetCard.setTranslationMode(true);
            holder.tweetCard.translationTextInputLayout.setVisibility(View.VISIBLE);
            holder.tweetCard.copyToTextField.setVisibility(View.VISIBLE);
            holder.tweetCard.uploadButton.setVisibility(View.VISIBLE);

            holder.tweetCard.historyButton.setText(lastTweet.translations!=null ? String.valueOf(lastTweet.translations.size()) : "0");
            // 阻止更新界面时反复查询
            if(!lastTweet.hadQueried || holder.tweetCard.getHistoryAdapter()==null) {
                holder.tweetCard.initHistoryTranslationView(context, lastTweet.translations);
                lastTweet.queryTranslations(handler, holder.tweetCard);
            } else {
                holder.tweetCard.getHistoryAdapter().notifyDataSetChanged();
            }
            holder.tweetCard.historyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 显示历史翻译区
                    holder.tweetCard.historyTranslationsView.setVisibility(View.VISIBLE);
                }
            });

            if(lastTweet.media==null || lastTweet.media.isEmpty()) holder.tweetCard.setBtn1Invisible();
            else holder.tweetCard.setBtn1OnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //保存媒体
                    String result = "成功";
                    for (TwitterMedia singleMedia : lastTweet.media) {
                        if (!singleMedia.saveToFile(v.getContext())) {
                            result = "失败";
                            break;
                        }
                    }
                    Snackbar.make(v, "保存媒体" + result, 1000).show();
                }
            });

            holder.tweetCard.copyTextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //复制原文+翻译
                    ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData mClipData = ClipData.newPlainText("tweetAndTranslation", lastTweet.getFullText(tweetFormat, holder.tweetCard.getTranslatedText()));
                    clipboardManager.setPrimaryClip(mClipData);
                    Snackbar.make(v,"已复制文本",1000).show();
                }
            });
            holder.tweetCard.uploadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    // 上传翻译
                    if(isConnected) new Thread() {
                        @Override
                        public void run() {
                            String result = "失败";
                            try {
                                String data = "translationContent="
                                        +URLEncoder.encode(holder.tweetCard.getTranslatedText(),"UTF-8")
                                        +"&sessionGroupID="+TweetList.getCurrentGroup().id;
                                Response response= TweetList.connection.webService.request(
                                        "PUT", WebService.SERVER_API+"tweet/"+lastTweet.id+"/translation", data, WebService.FORM_URLENCODED);
                                if(response.code()==200) {
                                    JSONObject resJson = new JSONObject(response.body().string());
                                    response.close();
                                    if(resJson.getBoolean("success")) {
                                        result = "成功";
                                    } else {
                                        Log.e("uploadTranslation", resJson.toString());
                                    }
                                } else {
                                    Log.e("uploadTranslation", response.message());
                                    response.close();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                final String finalResult = result;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Snackbar.make(view, "上传翻译"+ finalResult, 1000).show();
                                    }
                                });
                            }
                        }
                    }.start();
                    else Snackbar.make(view, "请检查网络连接", 1000).show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }
}

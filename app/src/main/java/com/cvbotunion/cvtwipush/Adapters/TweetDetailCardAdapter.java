package com.cvbotunion.cvtwipush.Adapters;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cvbotunion.cvtwipush.Activities.ImageViewer;
import com.cvbotunion.cvtwipush.Activities.Timeline;
import com.cvbotunion.cvtwipush.Activities.VideoViewer;
import com.cvbotunion.cvtwipush.CustomViews.TweetDetailCard;
import com.cvbotunion.cvtwipush.Model.TwitterMedia;
import com.cvbotunion.cvtwipush.Model.TwitterStatus;
import com.cvbotunion.cvtwipush.R;
import com.cvbotunion.cvtwipush.Service.WebService;
import com.cvbotunion.cvtwipush.Utils.ImageLoader;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

import okhttp3.Response;

public class TweetDetailCardAdapter extends RecyclerView.Adapter<TweetDetailCardAdapter.TweetDetailCardViewHolder> {

    public ArrayList<TwitterStatus> tweets;

    public static boolean isConnected = true;

    public Handler handler;
    public Context context;
    private final String tweetFormat;

    public TweetDetailCardAdapter(ArrayList<TwitterStatus> tweets,Context context){
        this.tweets = tweets;
        this.handler = new Handler();
        this.context = context;
        this.tweetFormat = Timeline.getCurrentGroup().tweetFormat;
    }

    public static class TweetDetailCardViewHolder extends RecyclerView.ViewHolder{
        public TweetDetailCard tweetDetailCard;

        public TweetDetailCardViewHolder(@NonNull View itemView) {
            super(itemView);
            this.tweetDetailCard = new TweetDetailCard(itemView.getContext(),itemView);
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
        holder.tweetDetailCard.setName(tweets.get(position).getUser().name_in_group);

        holder.tweetDetailCard.setTweetText(tweets.get(position).getText());

        holder.tweetDetailCard.getTweetStatusTextView().setOnLongClickListener(view -> {
            if(tweets.get(position).getText() != null) {
                ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("tweet", tweets.get(position).getText());
                clipboardManager.setPrimaryClip(mClipData);
                Snackbar.make(view, context.getString(R.string.original_tweet_copied), 1000).show();
                holder.tweetDetailCard.getTweetStatusTextView().setBackgroundColor(Color.TRANSPARENT);
            }
            return true;
        });

        switch(tweets.get(position).getTweetType()){
            case TwitterStatus.REPLY:
                holder.tweetDetailCard.setType(context.getString(R.string.reply));
                break;
            case TwitterStatus.QUOTE:
                holder.tweetDetailCard.setType(context.getString(R.string.quoted));
                break;
            default:
                holder.tweetDetailCard.setType("");
                break;
        }

        holder.tweetDetailCard.setTime(tweets.get(position).getCreated_at());

        if(tweets.get(position).user.cached_profile_image != null){
            holder.tweetDetailCard.setAvatarImg(tweets.get(position).user.cached_profile_image);
        } else if(isConnected && !tweets.get(position).user.avatarUnderProcessing) {
            ImageLoader.setAdapter(this, position).load(tweets.get(position).user);
        }

        int i=1;
        if(tweets.get(position).media != null && !tweets.get(position).media.isEmpty()) {
                if (tweets.get(position).media.size() <= 4 && tweets.get(position).media.get(0).type == TwitterMedia.IMAGE) {
                    holder.tweetDetailCard.tweetImageInit(tweets.get(position).media.size());
                }

                for (final TwitterMedia media : tweets.get(position).media) {
                    switch (media.type) {
                        case TwitterMedia.IMAGE:
                            if (media.cached_image_preview != null) {
                                final int page = i;
                                holder.tweetDetailCard.setImageOnClickListener(tweets.get(position).media.size(), i, v -> {
                                    Intent intent = new Intent(v.getContext(), ImageViewer.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("page", page);
                                    bundle.putParcelable("twitterStatus", tweets.get(position));
                                    intent.putExtras(bundle);
                                    v.getContext().startActivity(intent);
                                });
                                holder.tweetDetailCard.setTweetImage(tweets.get(position).media.size(), i, media.cached_image_preview);
                            } else if(isConnected && !media.underProcessing) {
                                ImageLoader.setAdapter(this, position).load(media,true);
                            }
                            break;
                        case TwitterMedia.VIDEO:
                            holder.tweetDetailCard.initVideo();
                            if (media.cached_image_preview != null) {
                                holder.tweetDetailCard.setVideoOnClickListener(v -> {
                                    Intent intent = new Intent(v.getContext(), VideoViewer.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putString("url", media.url);
                                    intent.putExtras(bundle);
                                    v.getContext().startActivity(intent);
                                });
                                holder.tweetDetailCard.setVideoBackground(media.cached_image_preview);
                            } else if(isConnected && !media.underProcessing) {
                                // isPreview为true时对应加载封面
                                ImageLoader.setAdapter(this, position).load(media,true);
                            }
                            break;
                    }
                    i += 1;
                }
        }
        else {
            //无媒体的情况
            holder.tweetDetailCard.hideAllMediaView();
        }

        if(tweets.get(position).media==null || tweets.get(position).media.isEmpty()) holder.tweetDetailCard.saveMediaButton.setVisibility(View.GONE);
        else holder.tweetDetailCard.saveMediaButton.setOnClickListener(v -> {
            //保存媒体
            String result = context.getString(R.string.success);
            for (TwitterMedia singleMedia : tweets.get(position).media) {
                if (!singleMedia.saveToFile(v.getContext())) {
                    result = context.getString(R.string.failure);
                    break;
                }
            }
            Snackbar.make(v, context.getString(R.string.media_save) + result, 1000).show();
        });

        holder.tweetDetailCard.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            try {
                imm.hideSoftInputFromWindow(holder.itemView.getWindowToken(), 0);
            } catch(Exception e){
                Log.w("TweetDetailCardAdapter",e.toString());
            }
            View view = ((Activity) context).getWindow().getCurrentFocus();
            if(view != null) {
                view.clearFocus();
            }
        });

        if(position == tweets.size()-1){
            final TwitterStatus lastTweet = tweets.get(position);
            holder.tweetDetailCard.setTranslationMode(true);
            if(lastTweet.getText().equals("")) holder.tweetDetailCard.copyToTextField.setVisibility(View.GONE);

            holder.tweetDetailCard.historyButton.setText(lastTweet.translations!=null ? String.valueOf(lastTweet.translations.size()) : "0");

            holder.tweetDetailCard.initHistoryTranslationView(context, lastTweet.translations);
            if(!lastTweet.hadQueried) lastTweet.queryTranslations(handler, holder.tweetDetailCard);
            holder.tweetDetailCard.historyButton.setOnClickListener(view -> {
                // 显示或隐藏历史翻译区
                if(holder.tweetDetailCard.historyTranslationsView.getVisibility()==View.VISIBLE) {
                    holder.tweetDetailCard.historyTranslationsView.setVisibility(View.GONE);
                } else {
                    lastTweet.queryTranslations(handler, holder.tweetDetailCard);
                    holder.tweetDetailCard.historyTranslationsView.setVisibility(View.VISIBLE);
                }
            });

            holder.tweetDetailCard.copyTextButton.setOnClickListener(v -> {
                //复制原文+翻译
                ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("tweetAndTranslation", lastTweet.getFullText(tweetFormat, holder.tweetDetailCard.getTranslatedText()));
                clipboardManager.setPrimaryClip(mClipData);
                Snackbar.make(v,context.getString(R.string.original_tweet_and_translationCopied),1000).show();
            });

            holder.tweetDetailCard.uploadButton.setOnClickListener(view -> {
                // 上传翻译
                if(isConnected) new Thread(() -> {
                    String result = context.getString(R.string.failure);
                    try {
                        String data = "translationContent="
                                +URLEncoder.encode(holder.tweetDetailCard.getTranslatedText(),"UTF-8")
                                +"&sessionGroupID="+ Timeline.getCurrentGroup().id;
                        Response response= Timeline.connection.webService.request(
                                "PUT", WebService.SERVER_API+"/tweet/"+lastTweet.id+"/translation", data, WebService.FORM_URLENCODED);
                        if(response.code()==200) {
                            JSONObject resJson = new JSONObject(response.body().string());
                            response.close();
                            if(resJson.getBoolean("success")) {
                                result = context.getString(R.string.success);
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
                        handler.post(() -> Snackbar.make(view, context.getString(R.string.upload_translation)+ finalResult, 1000).show());
                    }
                }).start();
                else Snackbar.make(view, context.getString(R.string.please_check_the_Internet), 1000).show();
            });
        } else { // 刷新UI需要隐藏之前显示的翻译区
            holder.tweetDetailCard.setTranslationMode(false);
            holder.tweetDetailCard.copyToTextField.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }
}

package com.cvbotunion.cvtwipush.CustomViews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.util.AttributeSet;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.cvbotunion.cvtwipush.R;
import com.google.android.material.imageview.ShapeableImageView;

public class TweetCard extends CardView{
    private String tweetText;
    private String tweetNameText;
    private String tweetTypeText;
    private String tweetTimeText;

    public CardView card;
    protected ShapeableImageView avatarImg; //头像
    protected TextView nameTextView; //姓名
    protected TextView tweetTypeTextView; //推文类型
    protected TextView tweetTimeTextView; //发推时间
    protected LinearLayout imageSetLayout; //推文图片/视频
    protected LinearLayout leftImageLayout;
    protected LinearLayout rightImageLayout;
    protected ImageView leftTopImageView;//图1
    protected ImageView rightTopImageView;//图2
    protected ImageView leftBottomImageView;//图3
    protected ImageView rightBottomImageView;//图4
    protected FrameLayout videoSet;
    protected ImageView defaultVideoBackground;
    protected ImageView videoNotLoading;
    protected TextView tweetStatusTextView; //推文正文文本
    private Button quickSaveButton; //快速保存按钮

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        card.setOnClickListener(l);
    }

    @Override
    public boolean performClick() {
        return card.performClick()||super.performClick();
    }

    public void setQSButtonOnClickListener(OnClickListener listener){
        quickSaveButton.setOnClickListener(listener);
    }

    public void setVideoOnClickListener(OnClickListener listener){
        videoSet.setOnClickListener(listener);
    }

    public TweetCard(@NonNull Context context,View view) {
        super(context);
        initView(context,view);
    }

    public TweetCard(@NonNull Context context,AttributeSet attrs) {
        super(context,attrs);
        //加载界面控件
        initAttr(context,attrs);
        initView(context,null);
        loadAttr();
    }

    public CharSequence getStatusText(){
        return tweetStatusTextView.getText();
    }

    protected void initAttr(Context context, AttributeSet attrs){
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TweetCard);
        tweetText = ta.getString(R.styleable.TweetCard_text);
        tweetNameText = ta.getString(R.styleable.TweetCard_name);
        tweetTypeText = ta.getString(R.styleable.TweetCard_type);
        tweetTimeText = ta.getString(R.styleable.TweetCard_time);
        ta.recycle();
    }

    protected void initView(Context context,View view) {
        View thisView;
        if(view != null){
            thisView = view;
        } else {
            thisView = LayoutInflater.from(context).inflate(R.layout.tweet_card,this,true);
        }
        card = thisView.findViewById(R.id.tweet_card);
        avatarImg = thisView.findViewById(R.id.avatar_img);
        nameTextView = thisView.findViewById(R.id.name_text);
        tweetTypeTextView = thisView.findViewById(R.id.tweet_type_text);
        tweetTimeTextView = thisView.findViewById(R.id.tweet_time);
        imageSetLayout = thisView.findViewById(R.id.img_set_layout);
        leftImageLayout = thisView.findViewById(R.id.left_linear_layout);
        rightImageLayout = thisView.findViewById(R.id.right_linear_layout);
        leftTopImageView = thisView.findViewById(R.id.left_top_image);
        leftBottomImageView = thisView.findViewById(R.id.left_bottom_image);
        rightTopImageView = thisView.findViewById(R.id.right_top_image);
        rightBottomImageView = thisView.findViewById(R.id.right_bottom_image);
        videoSet = thisView.findViewById(R.id.video_set);
        defaultVideoBackground = thisView.findViewById(R.id.video_background);
        videoNotLoading = thisView.findViewById(R.id.video_not_loading_image);
        tweetStatusTextView = thisView.findViewById(R.id.status_text);
        quickSaveButton = thisView.findViewById(R.id.quick_save_btn);
    }

    protected void loadAttr(){
        setName(tweetNameText);
        setType(tweetTypeText);
        setTime(tweetTimeText);
        setTweetText(tweetText);
    }

    public void setName(String name){
        nameTextView.setVisibility(VISIBLE);
        nameTextView.setText(name);
    }

    public void setType(String type){
        tweetTypeTextView.setVisibility(VISIBLE);
        tweetTypeTextView.setText(type);
    }

    public void setTime(String time){
        tweetTimeTextView.setVisibility(VISIBLE);
        tweetTimeTextView.setText(time);
    }

    public void setTweetText(String tweet){
        if(tweet != null) {
            tweetStatusTextView.setVisibility(VISIBLE);
            tweetStatusTextView.setText(tweet);
        }
    }

    public TextView getTweetStatusTextView() {
        return tweetStatusTextView;
    }

    public void setAvatarImg(Bitmap bmp){
        avatarImg.setVisibility(VISIBLE);
        avatarImg.setImageBitmap(bmp);
    }

    public void tweetImageInit(int size){
        videoSet.setVisibility(GONE);
        hideAllImageView();
        imageSetLayout.setVisibility(VISIBLE);
        switch(size) {
            case 1:
                leftImageLayout.setVisibility(VISIBLE);
                leftTopImageView.setVisibility(VISIBLE);
                break;
            case 2:
                leftImageLayout.setVisibility(VISIBLE);
                leftTopImageView.setVisibility(VISIBLE);
                rightImageLayout.setVisibility(VISIBLE);
                rightTopImageView.setVisibility(VISIBLE);
                break;
            case 3:
                leftImageLayout.setVisibility(VISIBLE);
                leftTopImageView.setVisibility(VISIBLE);
                rightImageLayout.setVisibility(VISIBLE);
                rightTopImageView.setVisibility(VISIBLE);
                rightBottomImageView.setVisibility(VISIBLE);
                break;
            default:
                leftImageLayout.setVisibility(VISIBLE);
                leftTopImageView.setVisibility(VISIBLE);
                leftBottomImageView.setVisibility(VISIBLE);
                rightImageLayout.setVisibility(VISIBLE);
                rightTopImageView.setVisibility(VISIBLE);
                rightBottomImageView.setVisibility(VISIBLE);
                break;
        }
    }

    private void getImage(Bitmap image, ImageView iv){
        if(image != null){
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setImageBitmap(image);
        }
    }

    public void setImageOnClickListener(int size,int position,OnClickListener listener){
        switch(position){
            case 1:
                leftTopImageView.setOnClickListener(listener);
                break;
            case 2:
                rightTopImageView.setOnClickListener(listener);
                break;
            case 3:
                if (size == 3) {
                    rightBottomImageView.setOnClickListener(listener);
                } else {
                    leftBottomImageView.setOnClickListener(listener);
                }
            case 4:
                rightBottomImageView.setOnClickListener(listener);
        }
    }

    public void setTweetImage(int size,int position,Bitmap image){
        switch(position){
            case 1:
                getImage(image,leftTopImageView);
                break;
            case 2:
                getImage(image,rightTopImageView);
                break;
            case 3:
                if (size == 3) {
                    getImage(image, rightBottomImageView);
                } else {
                    getImage(image, leftBottomImageView);
                }
            case 4:
                getImage(image,rightBottomImageView);
        }
    }

    public void hideAllImageView() {
        imageSetLayout.setVisibility(GONE);
        leftImageLayout.setVisibility(GONE);
        leftTopImageView.setVisibility(GONE);
        leftTopImageView.setImageResource(R.drawable.ic_baseline_image_24);
        leftBottomImageView.setVisibility(GONE);
        leftBottomImageView.setImageResource(R.drawable.ic_baseline_image_24);

        rightImageLayout.setVisibility(GONE);
        rightTopImageView.setVisibility(GONE);
        rightTopImageView.setImageResource(R.drawable.ic_baseline_image_24);
        rightBottomImageView.setVisibility(GONE);
        rightBottomImageView.setImageResource(R.drawable.ic_baseline_image_24);
    }

    public void initVideo(){
        hideAllImageView();
        videoSet.setVisibility(VISIBLE);
        defaultVideoBackground.setVisibility(VISIBLE);
        videoNotLoading.setVisibility(VISIBLE);
    }

    public void setVideoBackground(Bitmap bitmapImg){
        defaultVideoBackground.setImageBitmap(bitmapImg);
        defaultVideoBackground.setScaleType(ImageView.ScaleType.CENTER);
    }

    public void resetVideoBackground(Context context){
        defaultVideoBackground.getLayoutParams().height=194;
        defaultVideoBackground.setColorFilter(ContextCompat.getColor(context,R.color.colorBlack));
    }

    public void hideVideo(Context context){
        if(videoSet != null){
            videoSet.setVisibility(GONE);
            resetVideoBackground(context);
        }
    }

    public void hideAllMediaView() {
        hideAllImageView();
        videoSet.setVisibility(GONE);
    }
}
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
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.cvbotunion.cvtwipush.R;
import com.google.android.material.imageview.ShapeableImageView;

public class TweetCard extends CardView{
    public static int IMAGE = 0;
    public static int VIDEO = 1;

    private String tweetText;
    private String tweetNameText;
    private String tweetTypeText;
    private String tweetTimeText;

    private ShapeableImageView avatarImg; //头像
    private TextView nameTextView; //姓名
    private TextView tweetTypeTextView; //推文类型
    private TextView tweetTimeTextView; //发推时间
    private LinearLayout imageSetLayout; //推文图片/视频
    private LinearLayout leftImageLayout;
    private LinearLayout rightImageLayout;
    private ImageView leftTopImageView;//图1
    private ImageView rightTopImageView;//图2
    private ImageView leftBottomImageView;//图3
    private ImageView rightBottomImageView;//图4
    private FrameLayout videoSet;
    private ImageView defaultVideoBackground;
    private ImageView videoNotLoading;
    private TextView tweetStatusTextView; //推文正文文本
    private Button btn1; //快速保存按钮
    private OnClickListener btn1Listener;
    private OnClickListener videoListener;

    public void setBtn1OnClickListener(OnClickListener listener){
        this.btn1Listener = listener;
        btn1.setOnClickListener(btn1Listener);
    }

    public void setVideoOnClickListener(OnClickListener listener){
        this.videoListener = listener;
        videoSet.setOnClickListener(videoListener);
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
        if(tweetStatusTextView.getText() != null){
            return tweetStatusTextView.getText();
        } else {
            return null;
        }
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
        avatarImg = (ShapeableImageView) thisView.findViewById(R.id.avatar_img);
        nameTextView = (TextView) thisView.findViewById(R.id.name_text);
        tweetTypeTextView = (TextView) thisView.findViewById(R.id.tweet_type_text);
        tweetTimeTextView = (TextView) thisView.findViewById(R.id.tweet_time);
        imageSetLayout = (LinearLayout) thisView.findViewById(R.id.img_set_layout);
        leftImageLayout = (LinearLayout) thisView.findViewById(R.id.left_linear_layout);
        rightImageLayout = (LinearLayout) thisView.findViewById(R.id.right_linear_layout);
        leftTopImageView = (ImageView) thisView.findViewById(R.id.left_top_image);
        leftBottomImageView = (ImageView) thisView.findViewById(R.id.left_bottom_image);
        rightTopImageView = (ImageView) thisView.findViewById(R.id.right_top_image);
        rightBottomImageView = (ImageView) thisView.findViewById(R.id.right_bottom_image);
        videoSet = (FrameLayout) thisView.findViewById(R.id.video_set) ;
        defaultVideoBackground = (ImageView) thisView.findViewById(R.id.video_background);
        videoNotLoading = (ImageView) thisView.findViewById(R.id.video_not_loading_image);
        tweetStatusTextView = (TextView) thisView.findViewById(R.id.status_text);
        btn1 = (Button) thisView.findViewById(R.id.quick_save_btn);
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

    public void setAvatarImg(Bitmap bmp){
        avatarImg.setVisibility(VISIBLE);
        avatarImg.setImageBitmap(bmp);
    }

    public void tweetImageInit(int size){
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
}
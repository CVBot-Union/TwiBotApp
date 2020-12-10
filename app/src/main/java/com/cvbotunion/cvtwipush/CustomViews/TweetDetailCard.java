package com.cvbotunion.cvtwipush.CustomViews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.cvbotunion.cvtwipush.Adapters.TranslationCardAdapter;
import com.cvbotunion.cvtwipush.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;

public class TweetDetailCard extends TweetCard {
    public Button copyToTextField;
    public Button historyButton;
    public Button copyTextButton;
    public Button saveMediaButton;
    public Button uploadButton;
    public TextInputLayout translationTextInputLayout;
    public TextInputEditText translationTextInputEditText;
    public RecyclerView historyTranslationsView;
    private TranslationCardAdapter historyAdapter;

    public boolean isTranslationMode = true;

    public TweetDetailCard(@NonNull final Context context, View view) {
        super(context, view);
        copyToTextField = view.findViewById(R.id.copy_to_input);
        copyToTextField.setOnClickListener(v -> {
            if(getStatusText() != null) {
                translationTextInputEditText.setText(getStatusText());
            }
        });
        historyButton = view.findViewById(R.id.history_btn);
        copyTextButton = view.findViewById(R.id.copy_text_btn);
        uploadButton = view.findViewById(R.id.upload_btn);
        translationTextInputLayout = view.findViewById(R.id.translation_text_field);
        translationTextInputEditText = view.findViewById(R.id.translation_edit_textview);
        historyTranslationsView = view.findViewById(R.id.history_translations_view);
        historyTranslationsView.setNestedScrollingEnabled(false);
    }

    public TweetDetailCard(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void initView(Context context, View view) {
        View thisView;
        if(view != null){
            thisView = view;
        } else {
            thisView = LayoutInflater.from(context).inflate(R.layout.tweet_detail_card,this,true);
        }
        card = thisView.findViewById(R.id.tweet_detail_card);
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
        saveMediaButton = thisView.findViewById(R.id.quick_save_btn);
    }

    @Override
    public void setQSButtonOnClickListener(OnClickListener listener) {
        this.saveMediaButton.setOnClickListener(listener);
    }

    public void initHistoryTranslationView(Context context, ArrayList<HashMap<String,String>> translations) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        historyTranslationsView.setLayoutManager(layoutManager);
        historyAdapter = new TranslationCardAdapter(context, translations);
        historyTranslationsView.setAdapter(historyAdapter);
        ((SimpleItemAnimator) historyTranslationsView.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    public TranslationCardAdapter getHistoryAdapter() {
        return historyAdapter;
    }

    public String getTranslatedText(){
        return translationTextInputEditText.getText().toString();
    }

    public void setTranslatedText(String text){
        translationTextInputEditText.setText(text);
    }

    public void hideTranslationTextField(){
        translationTextInputLayout.setVisibility(GONE);
    }

    public void showTranslationTextField(){
        translationTextInputLayout.setVisibility(VISIBLE);
    }

    public void setTranslationMode(Boolean bool){
        if(bool) {
            isTranslationMode = true;
            showTranslationTextField();
        } else {
            isTranslationMode = false;
            hideTranslationTextField();
        }
    }
}

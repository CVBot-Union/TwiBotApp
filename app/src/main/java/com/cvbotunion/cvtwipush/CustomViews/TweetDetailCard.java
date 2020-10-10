package com.cvbotunion.cvtwipush.CustomViews;

import android.content.Context;
import android.util.AttributeSet;
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
    public Button uploadButton;
    public TextInputLayout translationTextInputLayout;
    public TextInputEditText translationTextInputEditText;
    public RecyclerView historyTranslationsView;
    private TranslationCardAdapter tAdapter;

    public Boolean isTranslationMode = true;

    public TweetDetailCard(@NonNull final Context context, View view) {
        super(context, view);
        copyToTextField = view.findViewById(R.id.copy_to_input);
        copyToTextField.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getStatusText() != null) {
                    translationTextInputEditText.setText(getStatusText());
                }
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

    public void initHistoryTranslationView(Context context, ArrayList<HashMap<String,String>> translations) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        historyTranslationsView.setLayoutManager(layoutManager);
        tAdapter = new TranslationCardAdapter(translations, context);
        historyTranslationsView.setAdapter(tAdapter);
        ((SimpleItemAnimator) historyTranslationsView.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    public TranslationCardAdapter getHistoryAdapter() {
        return tAdapter;
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

    public void hideDoneButton(){
        uploadButton.setVisibility(GONE);
    }

    public void showDoneButton(){
        uploadButton.setVisibility(VISIBLE);
    }

    public void setTranslationMode(Boolean bool){
        if(bool) {
            isTranslationMode = true;
            showTranslationTextField();
            showDoneButton();
        } else {
            isTranslationMode = false;
            hideTranslationTextField();
            hideDoneButton();
        }
    }
}

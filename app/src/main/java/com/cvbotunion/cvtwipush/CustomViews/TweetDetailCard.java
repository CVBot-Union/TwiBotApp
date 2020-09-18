package com.cvbotunion.cvtwipush.CustomViews;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.cvbotunion.cvtwipush.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class TweetDetailCard extends TweetCard {

    public Button copyToTextField;
    public Button copyTranslation;
    public Button doneButton;
    public TextInputLayout translationTextInputLayout;
    public TextInputEditText translationTextInputEditText;

    public Boolean isTranslationMode = true;

    public TweetDetailCard(@NonNull final Context context, View view) {
        super(context, view);
        doneButton = view.findViewById(R.id.done_btn);
        copyToTextField = view.findViewById(R.id.copy_to_input);
        copyToTextField.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getStatusText() != null) {
                    translationTextInputEditText.setText(getStatusText());
                }
            }
        });
        translationTextInputLayout = view.findViewById(R.id.translation_text_field);
        translationTextInputEditText = view.findViewById(R.id.translation_edit_textview);
        copyTranslation = view.findViewById(R.id.copy_translation_only);
        copyTranslation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(translationTextInputEditText.getText() != null) {
                    ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData mClipData = ClipData.newPlainText("translation", translationTextInputEditText.getText());
                    clipboardManager.setPrimaryClip(mClipData);
                    Toast.makeText(v.getContext(), "已复制", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public TweetDetailCard(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnClickDoneButtonListener(OnClickListener listener){
        doneButton.setOnClickListener(listener);
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
        doneButton.setVisibility(GONE);
    }

    public void showDoneButton(){
        doneButton.setVisibility(VISIBLE);
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

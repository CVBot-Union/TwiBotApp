package com.cvbotunion.cvtwipush.CustomViews;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.cvbotunion.cvtwipush.R;

public class TranslationCard extends CardView {
    public CardView card;
    public TextView userNameView;
    public TextView groupNameView;
    public TextView contentView;

    public TranslationCard(@NonNull final Context context) {
        super(context);
        card = findViewById(R.id.history_translation_card);
        userNameView = findViewById(R.id.history_translation_user_name);
        groupNameView = findViewById(R.id.history_translation_group_name);
        contentView = findViewById(R.id.history_translation_content);
    }

    public void setUserName(String userName) {
        if(userName!=null)  userNameView.setText(userName);
    }

    public void setGroupName(String groupName) {
        if(groupName!=null) groupNameView.setText(groupName);
    }

    public void setContent(String content) {
        if(content!=null) contentView.setText(content);
    }
}

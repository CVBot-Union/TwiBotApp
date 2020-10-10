package com.cvbotunion.cvtwipush.Adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.cvbotunion.cvtwipush.CustomViews.TranslationCard;
import com.cvbotunion.cvtwipush.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;

public class TranslationCardAdapter extends RecyclerView.Adapter<TranslationCardAdapter.TranslationCardViewHolder> {
    public ArrayList<HashMap<String,String>> translations;
    public Context context;

    public TranslationCardAdapter(ArrayList<HashMap<String,String>> translations, Context context) {
        this.translations = translations;
        this.context = context;
    }

    public static class TranslationCardViewHolder extends RecyclerView.ViewHolder {
        public TranslationCard translationCard;

        public TranslationCardViewHolder(@NonNull View itemView) {
            super(itemView);
            this.translationCard = new TranslationCard(itemView.getContext());
        }
    }

    @NonNull
    @Override
    public TranslationCardAdapter.TranslationCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.translation_card,parent,false);
        return new TranslationCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TranslationCardAdapter.TranslationCardViewHolder holder, final int position) {
        final CardView card = holder.itemView.findViewById(R.id.history_translation_card);
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "长按复制翻译", 1000).show();
            }
        });
        card.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("translation", translations.get(position).get("content"));
                clipboardManager.setPrimaryClip(mClipData);
                Snackbar.make(view, "已复制翻译", 1000).show();
                return true;  // 不再调用其他回调函数
            }
        });

        holder.translationCard.setUserName(translations.get(position).get("userName"));
        holder.translationCard.setGroupName(translations.get(position).get("groupName"));
        holder.translationCard.setContent(translations.get(position).get("content"));
    }

    @Override
    public int getItemCount() {
        return translations.size();
    }
}

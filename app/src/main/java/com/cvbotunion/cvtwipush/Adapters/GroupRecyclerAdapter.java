package com.cvbotunion.cvtwipush.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.cvbotunion.cvtwipush.Activities.TweetList;
import com.cvbotunion.cvtwipush.CustomViews.GroupPopupWindow;
import com.cvbotunion.cvtwipush.Model.RTGroup;
import com.cvbotunion.cvtwipush.R;

import java.util.ArrayList;

public class GroupRecyclerAdapter extends RecyclerView.Adapter<GroupRecyclerAdapter.ViewHolder> {
    public Context context;
    public GroupPopupWindow popupWindow;
    public ArrayList<RTGroup.Job> jobs;

    public GroupRecyclerAdapter(Context context, GroupPopupWindow popupWindow, ArrayList<RTGroup.Job> jobs){
        this.context = context;
        this.popupWindow = popupWindow;
        this.jobs = jobs;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView groupImageView;
        public TextView groupName;
        public TextView myJobInGroup;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            groupImageView = itemView.findViewById(R.id.group_image_view);
            groupName = itemView.findViewById(R.id.group_text_view);
            myJobInGroup = itemView.findViewById(R.id.job_text_view);
        }
    }

    @NonNull
    @Override
    public GroupRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_group_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupRecyclerAdapter.ViewHolder holder, final int position) {
        LinearLayout layout = (LinearLayout) holder.itemView.findViewById(R.id.group_item_layout);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                Intent intent = new Intent(context, TweetList.class);
                Bundle bundle = new Bundle();
                bundle.putString("groupId",jobs.get(position).getGroup().id);
                intent.putExtras(bundle);
                ((AppCompatActivity) context).startActivityForResult(intent,1);
                ((AppCompatActivity) context).finish();
            }
        });
        if(jobs.get(position).getGroup().avatar != null){
            holder.groupImageView.setImageBitmap(jobs.get(position).getGroup().avatar);
        }
        if(jobs.get(position).getGroup().name != null) {
            holder.groupName.setText(jobs.get(position).getGroup().name);
        }
        if(jobs.get(position).job != null) {
            holder.myJobInGroup.setText(jobs.get(position).job);
        }
    }

    @Override
    public int getItemCount() {
        return jobs.size();
    }
}

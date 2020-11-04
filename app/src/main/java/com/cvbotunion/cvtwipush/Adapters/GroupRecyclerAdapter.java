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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.cvbotunion.cvtwipush.Activities.Timeline;
import com.cvbotunion.cvtwipush.CustomViews.GroupPopupWindow;
import com.cvbotunion.cvtwipush.Model.Job;
import com.cvbotunion.cvtwipush.Model.RTGroup;
import com.cvbotunion.cvtwipush.R;
import com.cvbotunion.cvtwipush.Utils.ImageLoader;

import java.util.ArrayList;

public class GroupRecyclerAdapter extends RecyclerView.Adapter<GroupRecyclerAdapter.ViewHolder> {
    public Context context;
    public GroupPopupWindow popupWindow;
    public ArrayList<Job> jobs;
    public String groupNow;

    public GroupRecyclerAdapter(Context context, GroupPopupWindow popupWindow, ArrayList<Job> jobs, String groupNow){
        this.context = context;
        this.popupWindow = popupWindow;
        this.jobs = jobs;
        this.groupNow = groupNow;
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
        final RTGroup currentGroup = jobs.get(position).group;

        LinearLayout layout = holder.itemView.findViewById(R.id.group_item_layout);
        if(groupNow.equals(currentGroup.id)){
            holder.groupName.setTextColor(context.getColor(R.color.colorPrimary));
            int alphaBackground = ColorUtils.setAlphaComponent(context.getColor(R.color.colorPrimary), 25);
            layout.setBackgroundColor(alphaBackground);
            layout.setEnabled(false);  // 当前组禁止点击
        } else {
            holder.groupName.setTextColor(context.getColor(R.color.colorGray));
            layout.setBackgroundColor(context.getColor(R.color.colorWhite));
            layout.setEnabled(true);
        }
        layout.setOnClickListener(v -> {
            popupWindow.dismiss();
            Intent intent = new Intent(context, Timeline.class);
            Bundle bundle = new Bundle();
            bundle.putString("groupId",currentGroup.id);
            intent.putExtras(bundle);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            try {
                ((AppCompatActivity) context).startActivityForResult(intent, 1);
                ((AppCompatActivity) context).overridePendingTransition(0, 0);
                ((AppCompatActivity) context).finish();
            } catch (Exception e){
                Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();
            }
        });

        if(currentGroup.avatar != null){
            holder.groupImageView.setImageBitmap(currentGroup.avatar);
        } else {
            new ImageLoader().setAdapter(this, position).load(currentGroup);
        }

        holder.groupName.setText(currentGroup.name);
        holder.myJobInGroup.setText(jobs.get(position).jobName);
    }

    @Override
    public int getItemCount() {
        return jobs.size();
    }
}

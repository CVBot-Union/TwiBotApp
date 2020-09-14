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
import com.cvbotunion.cvtwipush.DBModel.DBRTGroup;
import com.cvbotunion.cvtwipush.Model.RTGroup;
import com.cvbotunion.cvtwipush.R;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.HashMap;

public class GroupRecyclerAdapter extends RecyclerView.Adapter<GroupRecyclerAdapter.ViewHolder> {
    public Context context;
    public GroupPopupWindow popupWindow;
    public ArrayList<String> gidList = new ArrayList<>();
    public ArrayList<RTGroup.Job> jobList = new ArrayList<>();

    public GroupRecyclerAdapter(Context context, GroupPopupWindow popupWindow, HashMap<String,RTGroup.Job> jobs){
        this.context = context;
        this.popupWindow = popupWindow;
        for(HashMap.Entry<String, RTGroup.Job> e : jobs.entrySet()) {
            gidList.add(e.getKey());
            jobList.add(e.getValue());
        }
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
        RTGroup currentGroup = LitePal.where("gid = ?",gidList.get(position)).findFirst(DBRTGroup.class).toRTGroup();
        LinearLayout layout = (LinearLayout) holder.itemView.findViewById(R.id.group_item_layout);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                Intent intent = new Intent(context, TweetList.class);
                Bundle bundle = new Bundle();
                bundle.putString("groupId",gidList.get(position));
                intent.putExtras(bundle);
                ((AppCompatActivity) context).startActivityForResult(intent,1);
                ((AppCompatActivity) context).finish();
            }
        });
        if(currentGroup.avatar != null){
            holder.groupImageView.setImageBitmap(currentGroup.avatar);
        }
        if(currentGroup.name != null) {
            holder.groupName.setText(currentGroup.name);
        }
        if(jobList.get(position) != null) {
            holder.myJobInGroup.setText(jobList.get(position).jobName);
        }
    }

    @Override
    public int getItemCount() {
        return gidList.size();
    }
}

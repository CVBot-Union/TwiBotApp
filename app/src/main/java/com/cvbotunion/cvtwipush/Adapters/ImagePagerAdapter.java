package com.cvbotunion.cvtwipush.Adapters;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cvbotunion.cvtwipush.Model.TwitterMedia;
import com.cvbotunion.cvtwipush.R;

import java.util.ArrayList;

public class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ViewHolder> {
    private ArrayList<TwitterMedia> twitterMediaArrayList;
    private Context context;
    private Handler handler;

    public ImagePagerAdapter(Context context, ArrayList<TwitterMedia> mediaArrayList) {
        this.context = context;
        this.twitterMediaArrayList = mediaArrayList;
        handler = new Handler();
    }

    @NonNull
    @Override
    public ImagePagerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.linear_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImagePagerAdapter.ViewHolder holder, int position) {
        holder.imageView.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_image_24));
        holder.imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        if(twitterMediaArrayList.get(position).cached_image != null){
            holder.imageView.setImageBitmap(twitterMediaArrayList.get(position).cached_image);
        } else if(twitterMediaArrayList.get(position).cached_image_preview != null){
            holder.imageView.setImageBitmap(twitterMediaArrayList.get(position).cached_image_preview);
        } else {
            //downloadImage(twitterMediaArrayList.get(position).url,position);
            twitterMediaArrayList.get(position).downloadImage(this, handler, position);
        }
    }

    @Override
    public int getItemCount() {
        return twitterMediaArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.loading_image_view);
        }
    }
}

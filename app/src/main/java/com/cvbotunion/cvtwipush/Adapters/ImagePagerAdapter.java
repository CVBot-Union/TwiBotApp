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
import com.github.chrisbanes.photoview.PhotoView;

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
        holder.photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        if(twitterMediaArrayList.get(position).cached_image != null){
            holder.photoView.setImageBitmap(twitterMediaArrayList.get(position).cached_image);
        } else if(twitterMediaArrayList.get(position).cached_image_preview != null){
            holder.photoView.setImageBitmap(twitterMediaArrayList.get(position).cached_image_preview);
        } else {
            twitterMediaArrayList.get(position).loadImage(false,this, handler, position);
        }
    }

    @Override
    public int getItemCount() {
        return twitterMediaArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        PhotoView photoView;

        ViewHolder(View itemView) {
            super(itemView);
            photoView = itemView.findViewById(R.id.loading_photo_view);
        }
    }
}

package com.cvbotunion.cvtwipush.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cvbotunion.cvtwipush.Model.TwitterMedia;
import com.cvbotunion.cvtwipush.R;
import com.cvbotunion.cvtwipush.Utils.ImageLoader;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;

public class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ViewHolder> {
    private ArrayList<TwitterMedia> twitterMediaArrayList;
    private Context context;
    private ArrayList<ViewHolder> holders;

    public ImagePagerAdapter(Context context, ArrayList<TwitterMedia> mediaArrayList) {
        this.context = context;
        this.twitterMediaArrayList = mediaArrayList;
        this.holders = new ArrayList<>();
    }

    public void resetScale() {
        for(ImagePagerAdapter.ViewHolder h : holders) {
            h.photoView.setScale(h.photoView.getMinimumScale(),true);
        }
    }

    @NonNull
    @Override
    public ImagePagerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.linear_layout,parent,false);
        ViewHolder holder = new ViewHolder(view);
        holders.add(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ImagePagerAdapter.ViewHolder holder, int position) {
        holder.photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        if(twitterMediaArrayList.get(position).cached_image != null){
            holder.photoView.setImageBitmap(twitterMediaArrayList.get(position).cached_image);
        } else if(!twitterMediaArrayList.get(position).underProcessing){
            new ImageLoader().setAdapter(this, position).load(twitterMediaArrayList.get(position), false);
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

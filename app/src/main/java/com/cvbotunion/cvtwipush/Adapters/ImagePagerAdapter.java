package com.cvbotunion.cvtwipush.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cvbotunion.cvtwipush.Model.TwitterMedia;
import com.cvbotunion.cvtwipush.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
            downloadImage(twitterMediaArrayList.get(position).url,position);
        }
    }

    protected void downloadImage(final String path, final int position) {
        new Thread() {
            @Override
            public void run() {
                try {
                    //把传过来的路径转成URL
                    URL url = new URL(path);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(10000);
                    int code = connection.getResponseCode();
                    if (code == 200) {
                        InputStream inputStream = connection.getInputStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        twitterMediaArrayList.get(position).cached_image = bitmap;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                notifyItemChanged(position);
                            }
                        });
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
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

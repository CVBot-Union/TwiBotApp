package com.cvbotunion.cvtwipush.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.cvbotunion.cvtwipush.Activities.Timeline;
import com.cvbotunion.cvtwipush.Model.RTGroup;
import com.cvbotunion.cvtwipush.Model.TwitterMedia;
import com.cvbotunion.cvtwipush.Model.TwitterUser;
import com.cvbotunion.cvtwipush.Model.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;

import okhttp3.Response;

public class ImageLoader {
    private final Handler handler;
    private static final LinkedHashMap<String, Bitmap> avatarCachedPool = new LinkedHashMap<String, Bitmap>() {
        @Override
        protected boolean removeEldestEntry(Entry eldest) {
            return size()>15;
        }
    };

    private RecyclerView.Adapter<?> tAdapter;
    private Integer position;
    private WeakReference<ImageView> imageViewRef;

    private ImageLoader() {
        this.handler = new Handler();
    }

    public static ImageLoader setAdapter(RecyclerView.Adapter<?> tAdapter, Integer position) {
        ImageLoader instance = new ImageLoader();
        instance.tAdapter = tAdapter;
        instance.position = position;
        return instance;
    }

    public static ImageLoader setAdapter(RecyclerView.Adapter<?> tAdapter) {
        ImageLoader instance = new ImageLoader();
        instance.tAdapter = tAdapter;
        return instance;
    }

    public static ImageLoader setImageView(ImageView imageView) {
        ImageLoader instance = new ImageLoader();
        instance.imageViewRef = new WeakReference<>(imageView);
        return instance;
    }

    public void load(final TwitterMedia media, final boolean isPreview) {
        media.underProcessing = true;
        new Thread(() -> {
            if (isPreview && media.previewImageURL != null) {
                File file = new File(TwitterMedia.mediaFilesDir, TwitterMedia.previewTag + Uri.parse(media.previewImageURL).getLastPathSegment());
                if (file.exists()) {
                    media.cached_image_preview = readFromFile(file);
                } else {
                    media.cached_image_preview = download(media.previewImageURL, file);
                }
            } else if (!isPreview && media.url != null) {
                File savedFile = new File(TwitterMedia.savePath, Uri.parse(media.url).getLastPathSegment());
                File cachedFile = new File(TwitterMedia.mediaFilesDir, Uri.parse(media.url).getLastPathSegment());
                if (savedFile.exists()) {
                    media.cached_image = readFromFile(savedFile);
                } else if (cachedFile.exists()) {
                    media.cached_image = readFromFile(cachedFile);
                } else {
                    media.cached_image = download(media.url, cachedFile);
                }
            }
            this.notifyUI(media.cached_image!=null ? media.cached_image : media.cached_image_preview);
            media.underProcessing = false;
        }).start();
    }

    public void load(final TwitterUser twitterUser) {
        twitterUser.avatarUnderProcessing = true;
        if((twitterUser.cached_profile_image=avatarCachedPool.getOrDefault(twitterUser.id, null))
                !=null) {
            this.notifyUI(twitterUser.cached_profile_image);
            twitterUser.avatarUnderProcessing = false;
        } else {
            new Thread(() -> {
                twitterUser.cached_profile_image = download(twitterUser.profile_image_url, null);
                avatarCachedPool.put(twitterUser.id, twitterUser.cached_profile_image);
                this.notifyUI(twitterUser.cached_profile_image);
                twitterUser.avatarUnderProcessing = false;
            }).start();
        }
    }

    public void load(final User user) {
        if((user.avatar=avatarCachedPool.getOrDefault(user.id, null))!=null) {
            this.notifyUI(user.avatar);
        } else {
            new Thread(() -> {
                user.avatar = download(user.avatarURL, null);
                avatarCachedPool.put(user.id, user.avatar);
                this.notifyUI(user.avatar);
            }).start();
        }
    }

    public void load(final RTGroup rtGroup) {
        if((rtGroup.avatar=avatarCachedPool.getOrDefault(rtGroup.id, null))!=null) {
            this.notifyUI(rtGroup.avatar);
        } else {
            new Thread(() -> {
                rtGroup.avatar = download(rtGroup.avatarURL, null);
                avatarCachedPool.put(rtGroup.id, rtGroup.avatar);
                this.notifyUI(rtGroup.avatar);
            }).start();
        }
    }

    private Bitmap download(String downloadURL, File file) {
        try {
            if (Timeline.connection.webService == null) {
                synchronized (Timeline.connection.flag) { Timeline.connection.flag.wait(); }
            }
            Response response = Timeline.connection.webService.get(downloadURL);
            if (response.code() == 200) {
                byte[] data = response.body().bytes();
                response.close();
                if(file!=null) new Thread(() -> {
                    try {
                        if(!file.exists()) { file.createNewFile(); }
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(data);
                        fos.close();
                    } catch (IOException e) {
                        Log.w("ImageLoader.download", e.toString());
                    }
                }).start();
                return BitmapFactory.decodeByteArray(data,0,data.length);
            } else {
                Log.w("ImageLoader.download", response.message());
                response.close();
                return null;
            }
        } catch(Exception e) {
            Log.w("ImageLoader.download", e.toString());
            return null;
        }
    }

    private Bitmap readFromFile(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            fis.close();
            return bitmap;
        } catch (Exception e) {
            Log.w("ImageLoader.readFromFile", e.toString());
            return null;
        }
    }

    private void notifyUI(final Bitmap bitmap) {
        if(this.tAdapter!=null) {
            handler.post(() -> {
                if(position==null) {
                    tAdapter.notifyDataSetChanged();
                } else {
                    tAdapter.notifyItemChanged(position);
                }
            });
        } else if(this.imageViewRef!=null && bitmap!=null){
            handler.post(() -> {
                imageViewRef.get().setImageBitmap(bitmap);
            });
        }
    }
}

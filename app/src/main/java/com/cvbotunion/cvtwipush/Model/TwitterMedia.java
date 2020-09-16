package com.cvbotunion.cvtwipush.Model;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.cvbotunion.cvtwipush.TwiPush;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TwitterMedia implements Parcelable{
    public final static String savePath = Environment.getExternalStorageDirectory().getPath() + "/DCIM/CVTwiPush/";
    public final static File internalFilesDir = TwiPush.getContext().getFilesDir();
    public final static int AVATAR=0;
    public final static int IMAGE=1;
    public final static int VIDEO=2;

    public String id;
    public String url;
    public String previewImageURL;
    public int type;
    @Nullable public Bitmap cached_image_preview;
    @Nullable public Bitmap cached_image;

    public TwitterMedia(){

    }

    public TwitterMedia(String id,String url,int type,String previewImageURL){
        this.id=id;
        this.url=url;
        this.type=type;
        this.previewImageURL=previewImageURL;
    }

    public TwitterMedia(String id, String url, int type, String previewImageURL, @Nullable Bitmap cached_image_preview){
        this(id,url,type,previewImageURL);
        this.cached_image_preview=cached_image_preview;
    }
    public TwitterMedia(String id, String url, int type, String previewImageURL, Bitmap cached_image_preview, @Nullable Bitmap cached_image){
        this(id,url,type,previewImageURL);
        this.cached_image = cached_image;
    }

    protected TwitterMedia(Parcel in) {
        id = in.readString();
        url = in.readString();
        previewImageURL = in.readString();
        type = in.readInt();
        cached_image_preview = in.readParcelable(Bitmap.class.getClassLoader());
        cached_image = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public void loadImage(boolean isPreview, RecyclerView.Adapter tAdapter, Handler handler, @Nullable Integer position) {
        if(isPreview && previewImageURL != null) {
            File file = new File(internalFilesDir, Uri.parse(previewImageURL).getLastPathSegment());
            if(file.exists())
                readImageFromFile(true, tAdapter, handler, position);
            else
                downloadImage(true, tAdapter, handler, position);
        }
        else if(!isPreview && url != null) {
            File file = new File(savePath, Uri.parse(url).getLastPathSegment());
            if(file.exists())
                readImageFromFile(false, tAdapter, handler, position);
            else
                downloadImage(false, tAdapter, handler, position);
        }
    }

    private void downloadImage(final boolean isPreview, final RecyclerView.Adapter tAdapter, final Handler handler, final Integer position) {
        final String downloadURL;
        if(isPreview)
            downloadURL = previewImageURL;
        else
            downloadURL = url;
        new Thread() {
            @Override
            public void run() {
                try {
                    URL url0 = new URL(downloadURL);
                    HttpURLConnection connection = (HttpURLConnection) url0.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(10000);
                    int code = connection.getResponseCode();
                    if (code == 200) {
                        InputStream inputStream = connection.getInputStream();
                        if(isPreview)
                            cached_image_preview = BitmapFactory.decodeStream(inputStream);
                        else
                            cached_image = BitmapFactory.decodeStream(inputStream);
                        inputStream.close();
                        if(tAdapter != null && handler != null) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (position != null)
                                        tAdapter.notifyItemChanged(position);
                                    else
                                        tAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                        if(isPreview) {
                            File file = new File(internalFilesDir, Uri.parse(downloadURL).getLastPathSegment());
                            if(!file.exists())
                                file.createNewFile();
                            FileOutputStream fos = new FileOutputStream(file);
                            cached_image_preview.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                            fos.close();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void readImageFromFile(final boolean isPreview, final RecyclerView.Adapter tAdapter, final Handler handler, final Integer position) {
        final File file;
        if(isPreview) {
            file = new File(internalFilesDir, Uri.parse(previewImageURL).getLastPathSegment());
        } else {
            file = new File(savePath, Uri.parse(url).getLastPathSegment());
        }
        new Thread() {
            @Override
            public void run() {
                try {
                    FileInputStream fis = new FileInputStream(file);
                    if(isPreview)
                        cached_image_preview = BitmapFactory.decodeStream(fis);
                    else
                        cached_image = BitmapFactory.decodeStream(fis);
                    fis.close();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (position != null)
                                tAdapter.notifyItemChanged(position);
                            else
                                tAdapter.notifyDataSetChanged();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public boolean saveToFile(Context context) {
        boolean result = true;
        switch(type) {
            case IMAGE:
                result = saveBitmap2file(context);
                break;
            case VIDEO:
                result = saveVideo(context);
                break;
            default:
                break;
        }
        return result;
    }

    private boolean saveBitmap2file(Context context) {
        String fileName = Uri.parse(url).getLastPathSegment();
        File file = new File(savePath, fileName);
        if(file.exists())
            return true;
        try {
            if(!file.getParentFile().exists())
                file.getParentFile().mkdirs();
            if(cached_image == null) {
                DownloadManager downloadManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                request.allowScanningByMediaScanner();
                request.setVisibleInDownloadsUi(true);
                request.setTitle("转推图片");
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DCIM, "/CVTwiPush/"+fileName);
                downloadManager.enqueue(request);
            } else {
                file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);
                cached_image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.getAbsolutePath())));
            }
            return true;
        } catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private boolean saveVideo(Context context) {
        String fileName = Uri.parse(url).getLastPathSegment();
        File file = new File(savePath, fileName);
        if(file.exists())
            return true;
        if(!file.getParentFile().exists())
            file.getParentFile().mkdirs();
        try {
            DownloadManager downloadManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.allowScanningByMediaScanner();
            request.setVisibleInDownloadsUi(true);
            request.setTitle("转推视频");
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DCIM, "/CVTwiPush/"+fileName);
            downloadManager.enqueue(request);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(url);
        dest.writeString(previewImageURL);
        dest.writeInt(type);
        dest.writeParcelable(cached_image_preview, flags);
        dest.writeParcelable(cached_image, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TwitterMedia> CREATOR = new Creator<TwitterMedia>() {
        @Override
        public TwitterMedia createFromParcel(Parcel in) {
            return new TwitterMedia(in);
        }

        @Override
        public TwitterMedia[] newArray(int size) {
            return new TwitterMedia[size];
        }
    };
}

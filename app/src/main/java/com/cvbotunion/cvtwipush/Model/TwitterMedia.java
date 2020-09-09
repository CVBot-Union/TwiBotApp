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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TwitterMedia implements Parcelable{
    public final static String savePath = Environment.getExternalStorageDirectory().getPath() + "/DCIM/CVTwiPush/";
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
        String[] tmp;
        if(url != null)
            tmp = url.split("/");
        else
            tmp = previewImageURL.split("/");
        String fileName = tmp[tmp.length-1].split("\\.")[0];
        String filePath = savePath + fileName + ".jpeg";
        try {
            File file = new File(filePath);
            if(!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            if(cached_image != null)
                cached_image.compress(Bitmap.CompressFormat.JPEG,100, fos);
            else if(cached_image_preview!=null)
                cached_image_preview.compress(Bitmap.CompressFormat.JPEG,100, fos);
            else {
                fos.close();
                return false;
            }
            fos.flush();
            fos.close();
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + filePath)));
            return true;
        } catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private boolean saveVideo(Context context) {
        String[] tmp;
        tmp = url.split("/");
        String fileName = tmp[tmp.length-1].split("\\?")[0];
        try {
            DownloadManager downloadManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.allowScanningByMediaScanner();
            request.setVisibleInDownloadsUi(true);
            request.setTitle("转推视频下载");
            request.setDestinationInExternalPublicDir("/DCIM/CVTwiPush/", fileName);
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

package com.cvbotunion.cvtwipush.Model;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.cvbotunion.cvtwipush.Service.WebService;
import com.cvbotunion.cvtwipush.TwiPush;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class TwitterMedia implements Parcelable {
    public static final String savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/CVTwiPush/";
    public static final File mediaFilesDir = new File(TwiPush.getContext().getFilesDir(), "media");  // 图片缓存目录
    public static final String previewTag = "preview_";  //推特同一张图的不同尺寸可能具有相同名称，预览图文件名添加此tag以避免覆盖
    public static final String urlPreviewParam = "x-oss-process=image/auto-orient,1/resize,p_70/quality,q_70";
    public static final int AVATAR=0;
    public static final int IMAGE=1;
    public static final int VIDEO=2;

    public boolean underProcessing = false;

    public String id;
    public String url;
    public String previewImageURL;
    public int type;
    @Nullable public Bitmap cached_image_preview;
    @Nullable public Bitmap cached_image;

    public TwitterMedia(){ }

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
        this(id,url,type,previewImageURL,cached_image_preview);
        this.cached_image = cached_image;
    }

    public TwitterMedia(JSONObject media) throws JSONException {
        this.id = media.getString("id_str");
        switch(media.getString("type")) {
            case "photo":
                this.type = IMAGE;
                this.url = WebService.SERVER_IMAGE+id+".png";
                this.previewImageURL = url+"?"+urlPreviewParam;
                break;
            case "video":
                this.type = VIDEO;
                this.url = WebService.SERVER_VIDEO+id+".mp4";
                this.previewImageURL = WebService.SERVER_IMAGE+id+".png";
                break;
            default:
                break;
        }
    }

    protected TwitterMedia(Parcel in) {
        id = in.readString();
        url = in.readString();
        previewImageURL = in.readString();
        type = in.readInt();
        //cached_image_preview = in.readParcelable(Bitmap.class.getClassLoader());
        //cached_image = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public boolean equals(Object obj) {
        if(this==obj) return true;
        if(obj instanceof TwitterMedia) {
            TwitterMedia anotherMedia = (TwitterMedia)obj;
            return id.equals(anotherMedia.id) && url.equals(anotherMedia.url)
                    && previewImageURL.equals(anotherMedia.previewImageURL) && type==anotherMedia.type;
        }
        return false;
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
        File cachedFile = new File(mediaFilesDir, fileName);
        if(file.exists())
            return true;
        if(cachedFile.exists()) {
            if(!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try {
                Files.copy(cachedFile.toPath(), file.toPath());
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.getAbsolutePath())));
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
        //dest.writeParcelable(cached_image_preview, flags);
        //dest.writeParcelable(cached_image, flags);
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

package com.example.testapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testapp.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SingleTweet extends AppCompatActivity {
    private String twiContent, videoPath;
    private String[] imgPaths;
    public static Map<String, Bitmap> cachedBmp = new HashMap<>();
    private ViewPager2 viewPager2;
    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_tweet);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
        this.setTitle("推文内容");
        Intent intent = getIntent();
        twiContent = intent.getStringExtra("twi_content");
        imgPaths = intent.getStringArrayExtra("img_paths");
        videoPath = intent.getStringExtra("video_path");
        final TextView textView = findViewById(R.id.twi_content);
        textView.setText(twiContent);
        if(!Arrays.equals(imgPaths, new String[]{""})) {
            viewPager2 = findViewById(R.id.viewpager2);
            viewPager2.setVisibility(View.VISIBLE);

            viewPager2.setAdapter(new MyAdapter());
            TextView textView1 = findViewById(R.id.img_page);
            textView1.setVisibility(View.VISIBLE);
            textView1.setText("共"+imgPaths.length+"页");
        }
        webView = findViewById(R.id.twi_video);
        if(!videoPath.equals("")) {
            webView.setVisibility(View.VISIBLE);
            WebSettings setting = webView.getSettings();
            setting.setJavaScriptEnabled(true);
            setting.setUseWideViewPort(true);
            setting.setLoadWithOverviewMode(true);
            setting.setAllowFileAccess(true);
            setting.setSupportZoom(true);
            setting.setJavaScriptCanOpenWindowsAutomatically(true);
            setting.setMediaPlaybackRequiresUserGesture(true);
            webView.setWebChromeClient(new WebChromeClient());
            webView.loadUrl(videoPath);
            System.out.println(webView.getContentHeight());
        }
        Button copy_button = findViewById(R.id.copy_button);
        copy_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                assert clipboardManager != null;
                clipboardManager.setPrimaryClip(ClipData.newPlainText(null,textView.getText()));
                Toast.makeText(SingleTweet.this,"复制成功",Toast.LENGTH_SHORT).show();
            }
        });
        Button download_button = findViewById(R.id.download_media);
        download_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                for(int i=0;i<imgPaths.length;i++) if(!imgPaths[i].equals("")) {
                    String imgPath = imgPaths[i];
                    String scheme = imgPath.split(":")[0];
                    switch(scheme) {
                        case "file":
                            Toast.makeText(SingleTweet.this, "文件已存在于\n"+imgPath, Toast.LENGTH_LONG).show();
                            break;
                        case "http":
                        case "https":
                            Bitmap bitmap = cachedBmp.get(imgPath);
                            Toast.makeText(SingleTweet.this, "图片"+(i+1)+"保存"+saveBitmap2file(bitmap, i), Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(SingleTweet.this, "图片URI格式错误", Toast.LENGTH_SHORT).show();
                    }
                }
                if(!videoPath.equals("")) {
                    String scheme = videoPath.split(":")[0];
                    switch(scheme) {
                        case "file":
                            Toast.makeText(SingleTweet.this, "文件已存在于\n"+videoPath, Toast.LENGTH_LONG).show();
                            break;
                        case "http":
                        case "https":
                            String[] tmp = videoPath.split("/");
                            String fileName = tmp[tmp.length-1];
                            File file1 = new File(Environment.getExternalStorageDirectory().getPath()+"/DCIM/TwiBot/"+fileName);
                            if(!file1.exists()) {
                                Toast.makeText(SingleTweet.this, "正在下载视频", Toast.LENGTH_SHORT).show();
                                downloadVideo(videoPath, fileName);
                            }
                            else Toast.makeText(SingleTweet.this,"文件已存在于\n"+file1.getPath(), Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(SingleTweet.this, "视频URI格式错误", Toast.LENGTH_SHORT).show();
                    }
                }
                //Log.d("DialogActivity","下载");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(webView != null) {
            webView.stopLoading();
            webView.clearHistory();
            webView.clearCache(true);
            webView.loadUrl("about:blank"); // clearView() should be changed to loadUrl("about:blank"), since clearView() is deprecated now
            webView.pauseTimers();
            webView.destroy(); // Note that mWebView.destroy() and mWebView = null do the exact same thing
        }
    }

    public Bitmap getURLImage(final String url) {
        ExecutorService pool = Executors.newCachedThreadPool();
        Future<Bitmap> future = pool.submit(new Callable<Bitmap>() {
            @Override
            public Bitmap call() {
                Bitmap bmp = null;
                try {
                    URL myUrl = new URL(url);
                    // 获得连接
                    HttpURLConnection conn = (HttpURLConnection) myUrl.openConnection();
                    conn.setConnectTimeout(6000);//设置超时
                    conn.setDoInput(true);
                    conn.setUseCaches(false);//不缓存
                    conn.connect();
                    InputStream is = conn.getInputStream();//获得图片的数据流
                    bmp = BitmapFactory.decodeStream(is);//读取图像数据
                    //读取文本数据
                    //byte[] buffer = new byte[100];
                    //inputStream.read(buffer);
                    //text = new String(buffer);
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return bmp;
        }});
        try {
            return future.get();
        } catch(ExecutionException|InterruptedException e){
            e.printStackTrace();
            return null;
        }
    }

    public String saveBitmap2file(Bitmap bmp, int index) {
        String[] tmp = imgPaths[index].split("/");
        String fileName = tmp[tmp.length-1].split("\\.")[0];
        try {
            String filePath = Environment.getExternalStorageDirectory().getPath() + "/DCIM/TwiBot/" + fileName + ".jpeg";
            File file = new File(filePath);
            if(!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG,100, fos);
            fos.flush();
            fos.close();
            SingleTweet.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + filePath)));
            this.imgPaths[index] = "file://" + filePath;
            return "成功";
        } catch(Exception e){
            e.printStackTrace();
            return "失败";
        }
    }

    public void downloadVideo(final String url, final String fileName) {
        try {
            DownloadManager downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.allowScanningByMediaScanner();
            request.setVisibleInDownloadsUi(true);
            request.setTitle("转推视频下载");
            request.setDestinationInExternalPublicDir("/DCIM/TwiBot/", fileName);
            downloadManager.enqueue(request);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(SingleTweet.this, "视频下载失败", Toast.LENGTH_SHORT).show();
        }
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            public ViewHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.twi_img);
            }
        }

        public MyAdapter() {}

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.img_item, parent,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String imgPath = imgPaths[position];
            if(!imgPath.equals("")) {
                holder.imageView.setVisibility(View.VISIBLE);
                String scheme = imgPath.split(":")[0];
                switch(scheme) {
                    case "file":
                        holder.imageView.setImageURI(Uri.parse(imgPath));
                        break;
                    case "http":
                    case "https":
                        if(!cachedBmp.containsKey(imgPath)) cachedBmp.put(imgPath,getURLImage(imgPath));
                        holder.imageView.setImageBitmap(cachedBmp.get(imgPath));
                        break;
                    default:
                        Log.e("DialogActivity", "ImageURI格式错误");
                }
            }
        }
        @Override
        public int getItemCount() {
            return imgPaths.length;
        }
    }
}
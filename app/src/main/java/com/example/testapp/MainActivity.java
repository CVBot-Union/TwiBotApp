package com.example.testapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private String[] data = {"apple","bannner","orange","watermelon","pear","grape",
            "pineapple","strawberry","cherry","mango","apple","bannner","orange",
            "watermelon","pear","grape", "pineapple","strawberry","cherry","mango"
    };
    private String s = "斎藤ニコル\n08-05 18:02:28\n\u6700\u8fd1\u3059\u3063\u3054\u304f\u6691\u3044\u306d(>_<)\n\n\u79c1\u306f\u4eca\u65e5\u30b9\u30a4\u30ab\u3092\u98df\u3079\u305f\u3088\ud83c\udf49\n\u307f\u306a\u3055\u3093\u3082\u590f\u3063\u307d\u3044\u3053\u3068\u4f55\u304b\u3057\u307e\u3057\u305f\u304b\u30fc\uff1f\ud83d\udc93";
    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setTitle("Home");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, intentFilter);
        data[0] = s;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this,android.R.layout.simple_list_item_1,data);
        ListView listView = findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent= new Intent(MainActivity.this,DialogActivity.class);
                intent.putExtra("twi_content",data[position]);
                intent.putExtra("img","@mipmap/ic_launcher_round");
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeReceiver);
    }

    class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
            assert connectivityManager != null;
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if(networkInfo == null || networkInfo.isAvailable()) Toast.makeText(context, "无网络连接", Toast.LENGTH_LONG).show();
        }
    }
}
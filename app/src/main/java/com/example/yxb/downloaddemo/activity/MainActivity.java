package com.example.yxb.downloaddemo.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

import com.example.yxb.downloaddemo.R;
import com.example.yxb.downloaddemo.javaBean.FileInfo;
import com.example.yxb.downloaddemo.service.DownloadService;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<FileInfo> fileInfos;
    MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listView);

        String url = "http://111.7.130.168/cache/dl.cdn.wandoujia.com/files/release2/WanDouJia_2.80.1.7144_homepage.exe?ich_args=e1b667a91fc5d51d17785d4c7a8af8f4_1_0_0_4_7f4c84d47911a2eef313f6bf1641856cc4b10e43fc25b13b69c4bc8db35faed1_3f22b2ca70a6f5131e1813731acfc981_1_0&ich_ip=WanDouJia_2.80.1.7144_homepage .exe";
        fileInfos = new ArrayList<>();
        for (int i = 0; i < 10; i++){
            FileInfo fileInfo = new FileInfo(0, url, "豌豆荚.apk", 0,0);
            fileInfos.add(fileInfo);
        }

        adapter = new MyAdapter(this, fileInfos);
        listView.setAdapter(adapter);



        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.ACTION_UPDATE);
        filter.addAction(DownloadService.ACTION_FINISHED);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadService.ACTION_UPDATE)){
                int finished = intent.getIntExtra("finished", 0);
                int id = intent.getIntExtra("id", 0);
                adapter.updateProgress(id, finished);
            }else if(intent.getAction().equals(DownloadService.ACTION_FINISHED)){
                FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
                adapter.updateProgress(fileInfo.getId(), 0);
                Toast.makeText(MainActivity.this, fileInfos.get(fileInfo.getId()).getFileName() + "下载完毕", Toast.LENGTH_SHORT).show();
            }

        }
    };




}

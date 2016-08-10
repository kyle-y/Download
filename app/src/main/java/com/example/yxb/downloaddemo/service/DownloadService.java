package com.example.yxb.downloaddemo.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.example.yxb.downloaddemo.javaBean.FileInfo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * PACKAGE_NAME:com.example.yxb.downloaddemo.service
 * FUNCTIONAL_DESCRIPTION
 * CREATE_BY:xiaobo
 * CREATE_TIME:2016/8/1
 * MODIFY_BY:
 */
public class DownloadService extends Service{

    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_UPDATE = "ACTION_UPDATE";
    public static final String ACTION_FINISHED = "ACTION_FINISHED";
    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory() + "/downloads/";
    private static final int MSG_INIT = 0;
    private DownlaodTask downlaodTask = null;
    private Map<Integer, DownlaodTask> taskMap = new LinkedHashMap<>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(ACTION_START)){
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            //启动初始化线程,取得文件大小，并创建相应大小的文件
            new InitThread(fileInfo).start();
        }else if (intent.getAction().equals(ACTION_PAUSE)){
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            DownlaodTask task = taskMap.get(fileInfo.getId());
            if (task != null){
                task.setPause(true);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_INIT:
                    FileInfo fileInfo = (FileInfo) msg.obj;
                    //已获得初始化结果，开始下载任务
                    downlaodTask = new DownlaodTask(fileInfo, DownloadService.this, 3);
                    downlaodTask.download();
                    taskMap.put(fileInfo.getId(), downlaodTask);
                    break;
            }

        }
    };

    /**
     * 初始化子线程，请求网络，用来获得文件的长度，以及设置本地文件的长度
     */

    class InitThread extends Thread{
        private FileInfo fileInfo = null;

        public InitThread(FileInfo fileInfo) {
            this.fileInfo = fileInfo;
        }

        @Override
        public void run() {
            HttpURLConnection connection = null;
            RandomAccessFile raf = null;
            try {
                //获取文件长度
                URL url = new URL(fileInfo.getUrl());
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setRequestMethod("GET");
                int length = -1;
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                    length = connection.getContentLength();
                }
                if (length <= 0 ){
                    return;
                }

                //创建相应大小的本地文件
                File dir = new File(DOWNLOAD_PATH);
                if (!dir.exists()){
                    dir.mkdir();
                }
                File file = new File(dir,fileInfo.getFileName());

                raf = new RandomAccessFile(file, "rwd");
                raf.setLength(length);
                fileInfo.setLength(length);
                Message messenger = handler.obtainMessage(MSG_INIT, fileInfo);
                messenger.sendToTarget();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                try {
                    connection.disconnect();
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

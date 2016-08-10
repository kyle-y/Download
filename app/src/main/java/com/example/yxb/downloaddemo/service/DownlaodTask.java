package com.example.yxb.downloaddemo.service;

import android.content.Context;
import android.content.Intent;

import com.example.yxb.downloaddemo.db.ThreadDAOImpl;
import com.example.yxb.downloaddemo.javaBean.FileInfo;
import com.example.yxb.downloaddemo.javaBean.ThreadInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * PACKAGE_NAME:com.example.yxb.downloaddemo.service
 * FUNCTIONAL_DESCRIPTION
 * CREATE_BY:xiaobo
 * CREATE_TIME:2016/8/3
 * MODIFY_BY:
 */
public class DownlaodTask {

    private FileInfo fileInfo = null;
    private Context context = null;
    private ThreadDAOImpl threadDAO = null;
    private int finished = 0;
    private boolean isPause = false;
    private int threadCount = 1;
    private ArrayList<DownloadThread> threads = null;


    public DownlaodTask(FileInfo fileInfo, Context context, int threadCount) {
        this.fileInfo = fileInfo;
        this.context = context;
        this.threadCount = threadCount;
        threadDAO = new ThreadDAOImpl(context);
    }

    /**
     * 下载方法
     */

    public void download(){
        ArrayList<ThreadInfo> list = (ArrayList<ThreadInfo>) threadDAO.getThreads(fileInfo.getUrl());
        //第一次下载，创建线程信息集合
        if (list.size() == 0){
            int length = fileInfo.getLength() / threadCount;
            for (int i = 0; i < threadCount; i++){
                ThreadInfo threadInfo = new ThreadInfo(i, fileInfo.getUrl(), i * length, (i + 1) * length - 1, 0);
                if (i == threadCount - 1){
                    threadInfo.setEnd(fileInfo.getLength());
                }
                list.add(threadInfo);
            }
        }
        threads = new ArrayList<>();
        //分配线程并开启
        for (ThreadInfo info : list){
            DownloadThread thread = new DownloadThread(info);
            thread.start();
            threads.add(thread);
        }
    }

    //判断是否都下载完毕，并通知UI进行相应操作
    private synchronized void checkAllThreadsFinished(){
        boolean allfinished = true;
        for (DownloadThread thread : threads){
            if (!thread.isFinished){
                allfinished = false;
                break;
            }
        }
        if (allfinished){
            Intent intent = new Intent(DownloadService.ACTION_FINISHED);
            intent.putExtra("fileInfo", fileInfo);
            context.sendBroadcast(intent);
        }
    }


    /**
     * 下载类
     */
    class DownloadThread extends Thread{
        private ThreadInfo threadInfo;
        public boolean isFinished = false;
        public DownloadThread(ThreadInfo threadInfo) {
            this.threadInfo = threadInfo;
        }

        @Override
        public void run() {
            //插入线程信息
            if (!threadDAO.isExist(threadInfo.getUrl(),threadInfo.getId())){
                threadDAO.insertThreadInfo(threadInfo);
            }
            //获取下载位置
            HttpURLConnection connection = null;
            RandomAccessFile raf = null;
            InputStream inputStream = null;
            try {
                URL url = new URL(threadInfo.getUrl());
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setRequestMethod("GET");
                int start = threadInfo.getStart() + threadInfo.getFinished();
                connection.setRequestProperty("Range", "bytes=" + start + "-" + threadInfo.getEnd());
                //获得文件的写入位置
                File file = new File(DownloadService.DOWNLOAD_PATH, fileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");
                raf.seek(start);
                //开始下载
                finished += threadInfo.getFinished();
                Intent intent = new Intent(DownloadService.ACTION_UPDATE);
                if (connection.getResponseCode() == HttpURLConnection.HTTP_PARTIAL){
                    inputStream = connection.getInputStream();
                    int length = -1;
                    byte[] buffer = new byte[1024];
                    long time = System.currentTimeMillis();
                    while((length = inputStream.read(buffer)) != -1){
                        raf.write(buffer,0,length);
                        finished += length;
                        threadInfo.setFinished(threadInfo.getFinished() + length);

                        if (System.currentTimeMillis() - time > 500){
                            time = System.currentTimeMillis();
                            intent.putExtra("finished", (int)(finished / (float)fileInfo.getLength() * 100));
                            intent.putExtra("id",fileInfo.getId());
                            context.sendBroadcast(intent);
                        }
                        //暂停保存进度到数据库
                        if (isPause){
                            threadDAO.updateThread(threadInfo.getUrl(),threadInfo.getId(),threadInfo.getFinished());
                            return;
                        }
                    }

                    //下载完成，删除线程信息
                    isFinished = true;
                    threadDAO.deleteThread(threadInfo.getUrl(),threadInfo.getId());
                    //检查是否全部下载完毕
                    checkAllThreadsFinished();
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                try {
                    connection.disconnect();
                    inputStream.close();
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public void setPause(boolean pause) {
        isPause = pause;
    }

}

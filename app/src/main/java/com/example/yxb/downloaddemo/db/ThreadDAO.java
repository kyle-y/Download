package com.example.yxb.downloaddemo.db;

import com.example.yxb.downloaddemo.javaBean.ThreadInfo;

import java.util.List;

/**
 * PACKAGE_NAME:com.example.yxb.downloaddemo.db
 * FUNCTIONAL_DESCRIPTION
 * CREATE_BY:xiaobo
 * CREATE_TIME:2016/8/3
 * MODIFY_BY:
 */
public interface ThreadDAO {
    //插入线程信息
    public void insertThreadInfo(ThreadInfo threadInfo);

    //删除线程
    public void deleteThread(String url, int thread_id);

    //更新线程下载进度
    public void updateThread(String url, int thread_id, int finished);

    //查询文件的线程信息
    public List<ThreadInfo> getThreads(String url);

    //判断一个线程是否存在
    public boolean isExist(String url, int thread_id);
}

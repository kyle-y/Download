package com.example.yxb.downloaddemo.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.yxb.downloaddemo.javaBean.ThreadInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * PACKAGE_NAME:com.example.yxb.downloaddemo.db
 * FUNCTIONAL_DESCRIPTION
 * CREATE_BY:xiaobo
 * CREATE_TIME:2016/8/3
 * MODIFY_BY:
 */
public class ThreadDAOImpl implements ThreadDAO{

    private DBHelper dbHelper = null;

    public ThreadDAOImpl(Context context) {
        dbHelper = DBHelper.getInstance(context);
    }

    @Override
    public synchronized void insertThreadInfo(ThreadInfo threadInfo) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.execSQL("insert into thread_info (thread_id, url, start, end, finished) values (?,?,?,?,?)"
        , new Object[]{threadInfo.getId(), threadInfo.getUrl(), threadInfo.getStart(), threadInfo.getEnd(),threadInfo.getFinished()});
        database.close();
    }

    @Override
    public synchronized void deleteThread(String url, int thread_id) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.execSQL("delete from thread_info where url = ? and thread_id = ?"
                , new Object[]{url, thread_id});
        database.close();
    }

    @Override
    public synchronized void updateThread(String url, int thread_id, int finished) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.execSQL("update thread_info set finished = ? where url = ? and thread_id = ?"
                , new Object[]{finished, url, thread_id});
        database.close();
    }

    @Override
    public List<ThreadInfo> getThreads(String url) {
        ArrayList<ThreadInfo> list = new ArrayList<>();
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = database.rawQuery("select * from thread_info where url = ?",
                new String[]{url});
        if (cursor.moveToFirst()){
            do {
                ThreadInfo threadInfo = new ThreadInfo();
                threadInfo.setId(cursor.getInt(cursor.getColumnIndex("thread_id")));
                threadInfo.setUrl(cursor.getString(cursor.getColumnIndex("url")));
                threadInfo.setStart(cursor.getInt(cursor.getColumnIndex("start")));
                threadInfo.setEnd(cursor.getInt(cursor.getColumnIndex("end")));
                threadInfo.setFinished(cursor.getInt(cursor.getColumnIndex("finished")));
                list.add(threadInfo);
            }while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return list;
    }

    @Override
    public boolean isExist(String url, int thread_id) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = database.rawQuery("select * from thread_info where url = ? and thread_id = ?",
                new String[]{url, thread_id + ""});
        boolean isExist = cursor.moveToNext();
        cursor.close();
        database.close();
        return isExist;
    }
}

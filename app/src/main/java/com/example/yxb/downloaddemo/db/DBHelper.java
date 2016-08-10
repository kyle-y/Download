package com.example.yxb.downloaddemo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * PACKAGE_NAME:com.example.yxb.downloaddemo.db
 * FUNCTIONAL_DESCRIPTION
 * CREATE_BY:xiaobo
 * CREATE_TIME:2016/8/3
 * MODIFY_BY:
 */
public class DBHelper extends SQLiteOpenHelper{
    private static DBHelper dbHelper = null;
    private static final String DB_NAME = "download.db";
    private static final int VERSION = 1;
    private static final String TABLE_CREATE = "create table thread_info(_id integer primary key autoincrement, " +
            "thread_id integer, url text, start integer, end integer, finished integer)";
    private static final String TABLE_DROP = "drop table if exists thread_info";

    private DBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    public static DBHelper getInstance(Context context){
        if (dbHelper == null){
            dbHelper = new DBHelper(context);
        }
        return dbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(TABLE_DROP);
        db.execSQL(TABLE_CREATE);
    }
}

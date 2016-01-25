package com.niceguy.app.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.system.StructPollfd;
import android.util.Log;

import java.sql.SQLException;

/**
 * Created by qiumeilin on 2016/1/23.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "visitor.db";
//    public static final String DATABASE_NAME = "test.db";
    public static final String TABLE_VISIT_LOG = "visit_log";
    public static final String TABLE_USER = "user";
    public static final String TABLE_DEPARTMENT = "department";
    public static final String TABLE_DUTY_USER = "duty_user";
    public static final String TABLE_VISIT_REASON = "visit_reason";
    public static final String TABLE_USER_DEPARTMENT = "user_department";
    public static final int DATABASE_VERSION = 1;


    public SQLiteDatabase db = null;
    public DBHelper(Context context) {
        //CursorFactory设置为null,使用默认值
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = this.getWritableDatabase();
    }

    public SQLiteDatabase getDB(){
        return db;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("YYX", "---------------------start db oncreate------------new");
        db.execSQL("CREATE TABLE IF NOT EXISTS user" +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, sex INTEGER , code_num TEXT," +
                " position TEXT, phone TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS visit_log" +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, visit_reason TEXT, reason_id INTEGER, visited_user_id INTEGER,"+
                "visited_username TEXT,visited_dept_id INTEGER,visited_dept_name TEXT," +
                "visited_sex INTEGER,visited_user_position TEXT,visited_user_phone TEXT,"+
                "visitor_name TEXT,visitor_sex INTEGER,visitor_avatar TEXT,idcard_avatar TEXT,"+
                "visitor_phone TEXT,visitor_ethnic TEXT,visitor_birthday TEXT,"+
                "visitor_address TEXT,visitor_idno TEXT,visitor_count INTEGER,"+
                "idcard_police TEXT,idcard_deadline TEXT,duty_user_id INTEGER,"+
                "duty_username TEXT,visit_time INTEGER,leave_time INTEGER,visit_status INTEGER,barcode TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS department" +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, dept_name TEXT, code_num TEXT, desc TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS duty_user" +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, sex INTEGER , code_num TEXT, position TEXT, phone TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS user_department" +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, dept_id INTEGER, user_id INTEGER,duty_user_id INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS visit_reason" +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, reason TEXT)");

        Log.d("YYX", "---------------------end db oncreate------------new");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void close(){
        if(db!=null){
            db.close();
        }
    }

    public boolean delete(String table,long id){

        return db.delete(table,"_id="+id,null) > 0;

    }

    public long insert(String table,ContentValues cv){
        return db.insert(table,null,cv);
    }

    public boolean update(String table,ContentValues cv,long _id){
        return db.update(table,cv,"_id="+_id,null) > 0;
    }

    public Cursor fetch(String table,long id) throws SQLException{

        String[] fields = getTableFields(table);
        Cursor c = db.query(true,table,fields,"_id="+id,null,null,null,null,null);
        if(c!=null){
            c.moveToFirst();
        }
        return c;

    }

    public Cursor fetchAll(String table,int offset,int limit){
//        String[] fields = getTableFields(table);
        return db.rawQuery("SELECT * FROM " + table + " ORDER BY _id DESC LIMIT ?,?", new String[]{String.valueOf(offset),String.valueOf(limit)});
    }

    public long getCount(String table) {
        String sql = "select count(*) from "+table;
        Cursor c = db.rawQuery(sql, null);
        c.moveToFirst();
        long length = c.getLong(0);
        c.close();
        return length;
    }

    public String[] getTableFields(String table){
        String[] fields = null;
        switch (table){
            case TABLE_DEPARTMENT:
                fields = new String[]{"_id","dept_name", "code_num", "desc"};
                break;
            case TABLE_DUTY_USER:
                fields = new String[]{"_id","username", "sex", "code_num","position","phone"};
                break;
            case TABLE_USER:
                fields = new String[]{"_id","username", "sex", "code_num","position","phone"};
                break;
            case TABLE_USER_DEPARTMENT:
                break;
            case TABLE_VISIT_LOG:
                fields = new String[]{"_id","visit_reason","reason_id","visited_user_id","visited_username",
                                        "visited_dept_id","visited_dept_name","visited_sex","visited_user_position",
                                        "visited_user_phone","visitor_phone","visitor_ethnic","visitor_birthday",
                                        "visitor_address","visitor_idno","visitor_count","idcard_police",
                                        "idcard_deadline","duty_user_id","duty_username","visit_time","leave_time",
                                        "visit_status","barcode"};
                break;
            case TABLE_VISIT_REASON:
                fields = new String[]{"_id","reason"};
                break;
        }
        return fields;
    }
}

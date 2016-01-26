package com.niceguy.app.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.system.StructPollfd;
import android.util.Log;

import com.niceguy.app.visitor.EmployeeList;

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
                " position TEXT, phone TEXT, user_type INTEGER DEFAULT 1 )");
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
        db.execSQL("CREATE TABLE IF NOT EXISTS user_department" +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, dept_id INTEGER, user_id INTEGER)");
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
        Cursor c = db.query(true, table, fields, "_id=" + id, null, null, null, null, null);
        if(c!=null){
            c.moveToFirst();
        }
        return c;

    }

    public Cursor fetchDepartmentByName(String name) throws SQLException{

        Cursor c = db.rawQuery("SELECT * FROM department WHERE dept_name=?", new String[]{name});
        if(c!=null){
            c.moveToFirst();
        }
        return c;
    }


    public Cursor fetchAll(String table,int offset,int limit){
//        String[] fields = getTableFields(table);
        return db.rawQuery("SELECT * FROM " + table + " ORDER BY _id DESC LIMIT ?,?", new String[]{String.valueOf(offset),String.valueOf(limit)});
    }

    public Cursor fetchAllVisitLog(int offset,int limit){
        return db.rawQuery("SELECT _id,visit_reason,visited_username,visited_dept_name,visitor_name," +
                "datetime(visit_time/1000,'unixepoch', 'localtime') AS visit_time,(CASE leave_time WHEN 0 THEN '' ELSE datetime(leave_time/1000,'unixepoch', 'localtime') END) AS leave_time,(CASE visit_status WHEN 0 THEN '未离开' WHEN 1 THEN '已离开' END) AS visit_status" +
                " FROM "+TABLE_VISIT_LOG+" ORDER BY _id DESC LIMIT ?,?", new String[]{String.valueOf(offset),String.valueOf(limit)});
    }

    public Cursor fetchAllVisitLog(String condition,int offset,int limit ){
        return db.rawQuery("SELECT _id,visit_reason,visited_username,visited_dept_name,visitor_name," +
                "datetime(visit_time/1000,'unixepoch', 'localtime') AS visit_time,(CASE leave_time WHEN 0 THEN '' ELSE datetime(leave_time/1000,'unixepoch', 'localtime') END) AS leave_time,(CASE visit_status WHEN 0 THEN '未离开' WHEN 1 THEN '已离开' END) AS visit_status" +
                " FROM "+TABLE_VISIT_LOG+" WHERE "+condition+" ORDER BY _id DESC LIMIT ?,?", new String[]{String.valueOf(offset),String.valueOf(limit)});
    }

    public Cursor fetchAllUser(int type,int offset,int limit){
//        String[] fields = getTableFields(table);
        return db.rawQuery("SELECT u._id,u.username,u.code_num,u.position,u.phone,CASE u.sex WHEN 1 THEN '男' WHEN 2 THEN '女' END AS sex,d.dept_name,d.code_num AS dept_code,d._id AS dept_id,ud._id AS ud_id FROM user u,department d,user_department ud WHERE u._id = ud.user_id AND d._id=ud.dept_id AND user_type=? ORDER BY u._id DESC LIMIT ?,?", new String[]{String.valueOf(type),String.valueOf(offset),String.valueOf(limit)});
    }

    public Cursor fetchAllUserByDeptName(int type,String dept_name){
        return db.rawQuery("SELECT u._id,u.username,u.code_num,u.position,u.phone,CASE u.sex WHEN 1 THEN '男' WHEN 2 THEN '女' END AS sex,d.dept_name,d.code_num AS dept_code,d._id AS dept_id,ud._id AS ud_id FROM user u,department d,user_department ud WHERE u._id = ud.user_id AND d._id=ud.dept_id AND user_type=? AND dept_name=? ORDER BY u._id DESC", new String[]{String.valueOf(type),String.valueOf(dept_name)});
    }

    public String[] getUserNamesByDeptName(int type,String dept_name){
        Cursor cur = db.rawQuery("SELECT u._id,u.username,u.code_num,u.position,u.phone,CASE u.sex WHEN 1 THEN '男' WHEN 2 THEN '女' END AS sex,d.dept_name,d.code_num AS dept_code,d._id AS dept_id,ud._id AS ud_id FROM user u,department d,user_department ud WHERE u._id = ud.user_id AND d._id=ud.dept_id AND user_type=? AND dept_name=? ORDER BY u._id DESC", new String[]{String.valueOf(type),String.valueOf(dept_name)});
        int len = cur.getCount();
        String[] data = new String[len];
        if(cur!=null){
            int i = 0;
            while (cur.moveToNext()){
                data[i] = cur.getString(cur.getColumnIndex("username"))+"-("+cur.getString(cur.getColumnIndex("dept_name"))+")";
                i++;
            }
        }
        return data;
    }

    public Cursor fetchUserByDeptNameAndUserName(String user_name,String dept_name){
        Cursor c = db.rawQuery("SELECT u._id,u.username,u.code_num,u.position,u.phone,CASE u.sex WHEN 1 THEN '男' WHEN 2 THEN '女' END AS sex,d.dept_name,d.code_num AS dept_code,d._id AS dept_id,ud._id AS ud_id FROM user u,department d,user_department ud WHERE u._id = ud.user_id AND d._id=ud.dept_id AND u.username=? AND d.dept_name=? ORDER BY u._id DESC", new String[]{String.valueOf(user_name), String.valueOf(dept_name)});
        Log.v("YYY", c.getCount() + "------------------------" + "SELECT u._id,u.username,u.code_num,u.position,u.phone,CASE u.sex WHEN 1 THEN '男' WHEN 2 THEN '女' END AS sex,d.dept_name,d.code_num AS dept_code,d._id AS dept_id,ud._id AS ud_id FROM user u,department d,user_department ud WHERE u._id = ud.user_id AND d._id=ud.dept_id AND u.username='" + user_name + "' AND d.dept_name='" + dept_name + "' ORDER BY u._id DESC");
        return c;
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

    public String[] getDeptNames(){
        Cursor cur = fetchAll(TABLE_DEPARTMENT, 0, 10000);
        int len = cur.getCount();
        String[] deptNames = new String[len];
        if(cur!=null){
            int i = 0;
            while (cur.moveToNext()){
                deptNames[i] = cur.getString(cur.getColumnIndex("dept_name"));
                i++;
            }
        }
        return deptNames;
    }

    public String[] getVisitReasons(){
        Cursor cur = fetchAll(TABLE_VISIT_REASON, 0, 10000);
        int len = cur.getCount();
        String[] data = new String[len];
        if(cur!=null){
            int i = 0;
            while (cur.moveToNext()){
                data[i] = cur.getString(cur.getColumnIndex("reason"));
                i++;
            }
        }
        return data;
    }


    public String[] getUserNames(int type){
        Cursor cur =  fetchAllUser(type, 0, 1000000);
        int len = cur.getCount();
        String[] data = new String[len];
        if(cur!=null){
            int i = 0;
            while (cur.moveToNext()){
                data[i] = cur.getString(cur.getColumnIndex("username"))+"("+cur.getString(cur.getColumnIndex("dept_name"))+")";
                i++;
            }
        }

        return data;

    }

    public Cursor getRecentVisitLogByIdNumber(String id_number){
        return db.rawQuery("SELECT * FROM "+TABLE_VISIT_LOG+" WHERE visitor_idno=? ORDER BY _id DESC LIMIT 0,3",new String[]{id_number});
    }

    public Cursor getVisitLogByBarcode(String barcode){
        return db.rawQuery("SELECT * FROM "+TABLE_VISIT_LOG+" WHERE barcode=? ORDER BY _id DESC",new String[]{barcode});
    }

    /*public Cursor searchVisitLogByCondition(String a){
    }*/

    public void closeDB(){
        if(db!=null)
            db.close();
    }


}

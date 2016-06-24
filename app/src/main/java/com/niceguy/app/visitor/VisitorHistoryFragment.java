package com.niceguy.app.visitor;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.niceguy.app.utils.DBHelper;
import com.niceguy.app.utils.DateTimePicker;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by qiumeilin on 2016/1/9.
 */
public class VisitorHistoryFragment extends Fragment implements View.OnClickListener{

    private static String TAG = "YYX";
    private EditText visitor_name;
    private EditText visitor_id_num;
    private EditText visited_name;
    private EditText visit_time;
    private EditText leave_time;
    private EditText visitor_address;
    private Button search,clear;
    private Activity inActivity;
    private String initStartDateTime = "2016年1月1日 14:44"; // 初始化开始时间
    private String initEndDateTime = "2016年1月1日 17:44"; // 初始化结束时间
    private Spinner certificate_type;
    private ArrayAdapter<String> adapter;


    private Activity activity = null;
    private TextView curpage,page,total,page1;
    private Button add,first,next,pre,last;
    private AlertDialog detailDialog;
    private DBHelper helper = null;
    private int pagesize = 10,total_page = 0,curpage_num=1;
    private long count = 0;

    private ListView listView = null;
    private String condition = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.v(TAG,"-------"+Thread.currentThread().getStackTrace()[2].getMethodName()+"--------------");
        View view  = inflater.inflate(R.layout.visitor_history_tab,container,false);
        visitor_name = (EditText) view.findViewById(R.id.visitor_name);
        visitor_id_num = (EditText) view.findViewById(R.id.visitor_id_num);
        visit_time = (EditText) view.findViewById(R.id.visit_time);
        leave_time = (EditText) view.findViewById(R.id.leave_time);
        visited_name = (EditText) view.findViewById(R.id.visited_name);
        visitor_address = (EditText) view.findViewById(R.id.visitor_address);
        search = (Button) view.findViewById(R.id.visit_search);
        clear = (Button) view.findViewById(R.id.search_clear);
        inActivity = getActivity();

        connectDB();

        initViews(view);
        initEvents();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.v(TAG, "-------" + Thread.currentThread().getStackTrace()[2].getMethodName() + "--------------");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.v(TAG, "-------" + Thread.currentThread().getStackTrace()[2].getMethodName() + "--------------");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.v(TAG, "-------" + Thread.currentThread().getStackTrace()[2].getMethodName() + "--------------");
    }

    private void initViews(View view){
        activity = getActivity();
        listView = (ListView) view.findViewById(R.id.visit_log_listview);

        certificate_type = (Spinner) view.findViewById(R.id.visitor_certificate);
        String[] certificate_list = {"全部证件类型","身份证","警官证","学生证","教师证","驾驶证","其他"};
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, certificate_list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        certificate_type.setAdapter(adapter);

        curpage = (TextView) view.findViewById(R.id.log_curpage);
        total = (TextView) view.findViewById(R.id.log_total);
        page = (TextView) view.findViewById(R.id.log_page);
        page1 = (TextView) view.findViewById(R.id.log_page1);

        first = (Button) view.findViewById(R.id.log_first_page);
        last = (Button) view.findViewById(R.id.log_last_page);
        next = (Button) view.findViewById(R.id.log_next_page);
        pre = (Button) view.findViewById(R.id.log_pre_page);

    }

    private void initEvents(){

        last.setOnClickListener(this);
        first.setOnClickListener(this);
        next.setOnClickListener(this);
        pre.setOnClickListener(this);
        visit_time.setOnClickListener(this);
        leave_time.setOnClickListener(this);
        search.setOnClickListener(this);
        clear.setOnClickListener(this);
        listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LayoutInflater layoutInflater = getActivity().getLayoutInflater();
                View detailView = layoutInflater.inflate(R.layout.visit_log_detail, null);
                Cursor c = (Cursor) parent.getItemAtPosition(position);
                showDetailDialog(detailView, c.getInt(c.getColumnIndex("_id")));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LayoutInflater layoutInflater = getActivity().getLayoutInflater();
                View v = layoutInflater.inflate(R.layout.visit_log_detail, null);

                Cursor c = (Cursor) parent.getItemAtPosition(position);

                TextView detail_leave_time = (TextView) v.findViewById(R.id.detail_leave_time);
                detail_leave_time.setText(c.getString(c.getColumnIndex("leave_time")));

                TextView detail_visit_time = (TextView) v.findViewById(R.id.detail_visit_time);
                detail_visit_time.setText(c.getString(c.getColumnIndex("visit_time")));

                TextView detail_valid_date = (TextView) v.findViewById(R.id.detail_valid_date);
                detail_valid_date.setText(c.getString(c.getColumnIndex("idcard_deadline")));

                TextView detail_visit_reason = (TextView) v.findViewById(R.id.detail_visit_reason);
                detail_visit_reason.setText(c.getString(c.getColumnIndex("visit_reason")));

                TextView detail_visited_name = (TextView) v.findViewById(R.id.detail_visited_name);
                detail_visited_name.setText(c.getString(c.getColumnIndex("visited_username")));

                TextView detail_visited_dept = (TextView) v.findViewById(R.id.detail_visited_dept);
                detail_visited_dept.setText(c.getString(c.getColumnIndex("visited_dept_name")));

                TextView detail_visited_sex = (TextView) v.findViewById(R.id.detail_visited_sex);
                detail_visited_sex.setText(c.getString(c.getColumnIndex("visited_sex")));

                TextView detail_visitor_sex = (TextView) v.findViewById(R.id.detail_visitor_sex);
                detail_visitor_sex.setText(c.getString(c.getColumnIndex("visitor_sex")));

                TextView detail_visited_pos = (TextView) v.findViewById(R.id.detail_visited_pos);
                detail_visited_pos.setText(c.getString(c.getColumnIndex("visited_user_position")));

                TextView detail_visited_phone = (TextView) v.findViewById(R.id.detail_visited_phone);
                detail_visited_phone.setText(c.getString(c.getColumnIndex("visited_user_phone")));

                TextView detail_visitor_ethnic = (TextView) v.findViewById(R.id.detail_visitor_ethnic);
                detail_visitor_ethnic.setText(c.getString(c.getColumnIndex("visitor_ethnic")));

                TextView detail_visitor_birthday = (TextView) v.findViewById(R.id.detail_visitor_birthday);
                detail_visitor_birthday.setText(c.getString(c.getColumnIndex("visitor_birthday")));

                TextView detail_visitor_address = (TextView) v.findViewById(R.id.detail_visitor_address);
                detail_visitor_address.setText(c.getString(c.getColumnIndex("visitor_address")));

                TextView detail_visitor_idno = (TextView) v.findViewById(R.id.detail_visitor_idno);
                detail_visitor_idno.setText(c.getString(c.getColumnIndex("visitor_idno_str")));

                TextView detail_visitor_count = (TextView) v.findViewById(R.id.detail_visitor_count);
                detail_visitor_count.setText(c.getString(c.getColumnIndex("visitor_count")));

                TextView detail_visitor_name = (TextView) v.findViewById(R.id.detail_visitor_name);
                detail_visitor_name.setText(c.getString(c.getColumnIndex("visitor_name")));

                TextView detail_visitor_police = (TextView) v.findViewById(R.id.detail_visitor_police);
                detail_visitor_police.setText(c.getString(c.getColumnIndex("idcard_police")));

                TextView detail_visit_status = (TextView) v.findViewById(R.id.detail_visit_status);
                detail_visit_status.setText(c.getString(c.getColumnIndex("visit_status")));

                TextView detail_visit_take = (TextView) v.findViewById(R.id.detail_visit_take);
                detail_visit_take.setText(c.getString(c.getColumnIndex("visitor_take")));

                TextView detail_visit_carnum = (TextView) v.findViewById(R.id.detail_car_num);
                detail_visit_carnum.setText(c.getString(c.getColumnIndex("visitor_car_num")));

                TextView detail_duty_user = (TextView) v.findViewById(R.id.detail_duty_user);
                String in_duty_username = c.getString(c.getColumnIndex("duty_username"))+"(进)";
                String leave_duty_username = c.getString(c.getColumnIndex("duty_username_leave"));
                if(leave_duty_username != null){
                    leave_duty_username = "，"+c.getString(c.getColumnIndex("duty_username_leave"))+"(出)";
                }else{
                    leave_duty_username = "";
                }
                detail_duty_user.setText(in_duty_username+leave_duty_username);

                ImageView detail_idcard_avatar = (ImageView) v.findViewById(R.id.detail_idcard_avatar);
                String a1 = c.getString(c.getColumnIndex("idcard_avatar"));
                if ("".equals(a1)) {
                    detail_idcard_avatar.setImageResource(R.mipmap.photo);
                } else {
                    Bitmap idcard_avatar_bitmap = BitmapFactory.decodeFile(a1);
                    detail_idcard_avatar.setImageBitmap(idcard_avatar_bitmap);
                }

                ImageView detail_cameraTake_avatar = (ImageView) v.findViewById(R.id.detail_cameraTake_avatar);
                if ("".equals(a1)) {
                    detail_idcard_avatar.setImageResource(R.mipmap.photo);
                } else {
                    Bitmap cameraTake_avatar_bitmap = BitmapFactory.decodeFile(c.getString(c.getColumnIndex("visitor_avatar")));
                    detail_cameraTake_avatar.setImageBitmap(cameraTake_avatar_bitmap);
                }

                showDetailDialog(v, c.getInt(c.getColumnIndex("_id")));
                releaseDB();
            }

        });

    }

    @Override
    public void onClick(View v) {

        total_page = getTotalPage();
        switch (v.getId()){
            case R.id.log_first_page:
                updateList(1);
                break;
            case R.id.log_last_page:
                if(curpage_num != total_page && total_page>1){
                    updateList(total_page);
                }
                break;
            case R.id.log_pre_page:
                if(curpage_num > 1){
                    updateList(curpage_num - 1);
                }
                break;
            case R.id.log_next_page:

                if( curpage_num != total_page){
                    updateList(curpage_num+1);
                }
                break;
            case R.id.visit_time:
                DateTimePicker visitTimeDialog = new DateTimePicker(inActivity, initStartDateTime);
                visitTimeDialog.dateTimePicKDialog(visit_time);
                break;
            case R.id.leave_time:
                DateTimePicker leaveTimeDialog = new DateTimePicker(inActivity, initEndDateTime);
                leaveTimeDialog.dateTimePicKDialog(leave_time);
                break;
            case R.id.visit_search:
                updateList(1);
                break;
            case R.id.search_clear:
                clear();
                break;
        }
    }

    private void showDetailDialog(final View view, final int _id) {
        String title = "详情";
        String positiveButtonText = "登记离开" ;

        TextView detail_visit_status = (TextView)view.findViewById(R.id.detail_visit_status);
        if("未离开".equals(detail_visit_status.getText().toString())){

        }else {
            positiveButtonText = "确定";
        }
        final String finalPositiveButtonText = positiveButtonText;
        detailDialog = new AlertDialog.Builder(getActivity())
                .setTitle(title).setView(view)
                .setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if(finalPositiveButtonText.equals("确定")){
                            dialog.dismiss();
                        }else{
                            ContentValues cv = new ContentValues();
                            cv.put("visit_status",1);
                            cv.put("leave_time",new Date().getTime());
                            connectDB();
                            helper.update(helper.TABLE_VISIT_LOG, cv, _id);
                            releaseDB();
                            updateList(1);
                            Toast.makeText(getActivity(), "更新成功", Toast.LENGTH_LONG).show();
                        }

                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        detailDialog.dismiss();
                    }
                }).create();
        detailDialog.show();

    }

    private int getTotalPage(){
        connectDB();
        if("".equals(condition)){
            count = helper.getCount(helper.TABLE_VISIT_LOG);
        }else{
            count = helper.getCount(helper.TABLE_VISIT_LOG,condition);
        }
        Log.v("YYX",count+"---count-------");
        Log.v("YYX",pagesize+"---pagesize-------");
        releaseDB();

        if(count > 0 && count <pagesize){
            total_page = 1;
        }else{
            double d = (Double.parseDouble(String.valueOf(count)) / Double.parseDouble(String.valueOf(pagesize)));
            Log.v("YYX",d+"----(count / pagesize)------");
            total_page = (int) Math.ceil(d);
            Log.v("YYX",Math.ceil(d)+"----Math.ceil(d)------");
        }
        Log.v("YYX", total_page + "----total_page------");
        return total_page;
    }

    public void updateList(int page_num){

        ArrayList<String> list = new ArrayList<String>(5);
        long leave_time_long = 0;
        long visit_time_long = 0;
        String idno = visitor_id_num.getText().toString().trim();
        String address = visitor_address.getText().toString().trim();
        String name = visitor_name.getText().toString().trim();
        String visitor_certificate = certificate_type.getSelectedItem().toString();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日 HH:mm");
        try {
            leave_time_long = sdf.parse(leave_time.getText().toString()).getTime();
            visit_time_long = sdf.parse(visit_time.getText().toString()).getTime();

        } catch (ParseException e) {
            e.printStackTrace();
        }
        String s1="",s2="",s3="",s4="",s5="",s6="";
        if(leave_time_long>0){
            s1 = " leave_time < "+leave_time_long;
            list.add(s1);
        }
        if(visit_time_long>0){
            s2 = " visit_time > "+visit_time_long;
            list.add(s2);
        }
        if(!"".equals(idno)){
            s3 = " visitor_idno LIKE '%"+idno+"%'";
            list.add(s3);
        }

        if(!"".equals(address)){
            s4 = " visitor_address LIKE '%"+address+"%'";
            list.add(s4);
        }

        if(!"".equals(name)){
            s5 = " visitor_name LIKE '%"+name+"%'";
            list.add(s5);
        }

        if(!"".equals(visitor_certificate) && !"全部证件类型".equals(visitor_certificate)){
            s6 = " certificate_type LIKE '%"+visitor_certificate+"%'";
            list.add(s6);
        }


        Object[] condition_arr = list.toArray();
        condition = StringUtils.join(condition_arr, " AND ");

        Log.v("YYX", condition+"---------condition---");
        Log.v("YYX", page_num+"---------page_num---");

        curpage_num = page_num;
        connectDB();

        if(page_num == 1){
            page_num = 0;
        }
        int offset = 0;
        if(page_num>0){
            offset = (page_num-1)*pagesize;
        }else{
            offset = page_num*pagesize;
        }
        Cursor cur;
        if("".equals(condition)){
            cur = helper.fetchAllVisitLog(offset, pagesize);
        }else{
            cur = helper.fetchAllVisitLog(condition, offset, pagesize);
        }
        total_page = getTotalPage();
        page.setText(String.valueOf(total_page));
        page1.setText(String.valueOf(total_page));
        curpage.setText(String.valueOf(curpage_num));
        total.setText(String.valueOf(count));

        if(cur!=null && cur.getCount()>=0){
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), R.layout.visit_log_item, cur,
                    new String[]{"_id", "visitor_name","visitor_idno_str","visit_reason","visited_dept_name","visited_username","visit_time","leave_time","visit_status"},
                    new int[]{R.id.item_log_id,R.id.item_visitor_name,R.id.item_visitor_idno_str, R.id.item_visit_reason,R.id.item_visited_dept,R.id.item_visited_name,R.id.item_visit_time,R.id.item_leave_time,R.id.item_visit_status});
            //实现列表的显示
            Log.v("YYX", "in setAdapter");
            listView.setAdapter(adapter);
        }
        releaseDB();

    }

    public void clear(){
        visit_time.setText("");
        leave_time.setText("");
        visited_name.setText("");
        visitor_address.setText("");
        visitor_id_num.setText("");
        visitor_name.setText("");
        ArrayList<HashMap<String,Object>> list = new ArrayList<HashMap<String, Object>>();
        if(list.size()>0){
            list.clear();
        }
        SimpleAdapter simpleAdapter = new SimpleAdapter(getActivity(), list,R.layout.visit_log_item,
                new String[]{"_id", "visitor_name","visit_reason","visited_dept_name","visited_username","visit_time","leave_time","visit_status"},
                new int[]{R.id.item_log_id,R.id.item_visitor_name, R.id.item_visit_reason,R.id.item_visited_dept,R.id.item_visited_name,R.id.item_visit_time,R.id.item_leave_time,R.id.item_visit_status});
        listView.setAdapter(simpleAdapter);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(hidden){
            clear();
        }
    }

    private void connectDB(){
        if(helper==null){
            helper = DBHelper.getInstance(getActivity());
        }
    }
    private void releaseDB(){
        /*if(helper!=null){
            helper.close();
        }*/
    }

}

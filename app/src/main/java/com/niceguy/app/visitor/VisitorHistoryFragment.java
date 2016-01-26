package com.niceguy.app.visitor;


import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.niceguy.app.utils.DBHelper;
import com.niceguy.app.utils.DateTimePicker;

import java.sql.SQLException;

/**
 * Created by qiumeilin on 2016/1/9.
 */
public class VisitorHistoryFragment extends Fragment implements View.OnClickListener{

    private EditText visitor_name;
    private EditText visitor_id_num;
    private EditText visited_name;
    private EditText visit_time;
    private EditText leave_time;
    private EditText visitor_address;
    private Button search;
    private Activity inActivity;
    private String initStartDateTime = "2013年9月3日 14:44"; // 初始化开始时间
    private String initEndDateTime = "2014年8月23日 17:44"; // 初始化结束时间


    private Activity activity = null;
    private TextView curpage,page,total,page1;
    private Button add,first,next,pre,last;
    private AlertDialog detailDialog;
    private DBHelper helper = null;
    private int pagesize = 10,total_page = 0,curpage_num=1;
    private long count = 0;

    private ListView listView = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view  = inflater.inflate(R.layout.visitor_history_tab,container,false);
        visitor_name = (EditText) view.findViewById(R.id.visitor_name);
        visitor_id_num = (EditText) view.findViewById(R.id.visitor_id_num);
        visit_time = (EditText) view.findViewById(R.id.visit_time);
        leave_time = (EditText) view.findViewById(R.id.leave_time);
        visited_name = (EditText) view.findViewById(R.id.visited_name);
        visitor_address = (EditText) view.findViewById(R.id.visitor_address);
        search = (Button) view.findViewById(R.id.visit_search);
        inActivity = getActivity();

        initViews(view);
        initEvents();
        return view;
    }


    private void initViews(View view){
        activity = getActivity();
        listView = (ListView) view.findViewById(R.id.visit_log_listview);

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

        visit_time.setOnClickListener(this);
        leave_time.setOnClickListener(this);
        search.setOnClickListener(this);
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

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.visit_time:
                DateTimePicker visitTimeDialog = new DateTimePicker(inActivity, initStartDateTime);
                visitTimeDialog.dateTimePicKDialog(visit_time);
                break;
            case R.id.leave_time:
                DateTimePicker leaveTimeDialog = new DateTimePicker(inActivity, initEndDateTime);
                leaveTimeDialog.dateTimePicKDialog(leave_time);
                break;
            case R.id.visit_search:
                Toast.makeText(inActivity,"开发中",Toast.LENGTH_SHORT).show();
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
                            helper.update(helper.TABLE_VISIT_LOG, cv, _id);
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
        count = helper.getCount(helper.TABLE_VISIT_LOG);

        if(count > 0 && count <pagesize){
            total_page = 1;
        }else{
            double d = (double) (count / pagesize);
            total_page = (int) Math.ceil(d);
        }

        return total_page;
    }

    public void updateList(int page_num){

        curpage_num = page_num;
        count = helper.getCount(helper.TABLE_VISIT_LOG);
        total.setText(String.valueOf(count));
        total_page = getTotalPage();
        page.setText(String.valueOf(total_page));
        page1.setText(String.valueOf(total_page));
        curpage.setText(String.valueOf(curpage_num));
        if(page_num == 1){
            page_num = 0;
        }
        int offset = page_num*pagesize;
        String condition = "";
        Cursor cur = helper.fetchAllVisitLog(condition, offset, pagesize);
        if(cur != null){
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(activity, R.layout.user_item, cur,
                    new String[]{"_id", "username","sex","code_num","dept_name","position","phone"}, new int[]{R.id.user_id, R.id.user_name,R.id.user_sex,R.id.user_code,R.id.user_dept,R.id.user_position,R.id.user_phone});
            //实现列表的显示
            listView.setAdapter(adapter);
        }
        cur.close();

    }

}

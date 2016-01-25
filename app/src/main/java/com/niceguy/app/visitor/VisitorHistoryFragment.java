package com.niceguy.app.visitor;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.niceguy.app.utils.DateTimePicker;

/**
 * Created by qiumeilin on 2016/1/9.
 */
public class VisitorHistoryFragment extends Fragment implements View.OnClickListener,View.OnTouchListener{

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
        initEvents();
        return view;
    }

    private void initEvents(){
//        visit_time.setOnTouchListener(this);
//        leave_time.setOnTouchListener(this);
        visit_time.setOnClickListener(this);
        leave_time.setOnClickListener(this);
        search.setOnClickListener(this);

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

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        /*int touch_count = 0;

        switch (v.getId()){
            case R.id.visit_time:

                DateTimePicker visitTimeDialog = new DateTimePicker(inActivity, initStartDateTime);
                visitTimeDialog.dateTimePicKDialog(visit_time);
                touch_count ++;
                break;
            case R.id.leave_time:
                DateTimePicker leaveTimeDialog = new DateTimePicker(inActivity, initEndDateTime);
                leaveTimeDialog.dateTimePicKDialog(leave_time);
                touch_count ++;
                break;
        }*/
        return false;
    }
}

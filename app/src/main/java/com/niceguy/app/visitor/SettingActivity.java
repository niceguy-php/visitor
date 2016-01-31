package com.niceguy.app.visitor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class SettingActivity extends AppCompatActivity {

    private Fragment departmentList,dutyUserList,employeeList,visitReasonList,backupAndRestore;
    private RadioButton dept_tab_btn,employee_tab_btn,visit_reason_tab_btn,duty_user_tab_btn;
    private RadioGroup radioGroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        initViews();
        initEvents();
        setSelected(0);
    }

    private void initViews(){
        dept_tab_btn = (RadioButton) findViewById(R.id.dept_tab_btn);
        employee_tab_btn = (RadioButton) findViewById(R.id.employee_tab_btn);
        visit_reason_tab_btn = (RadioButton) findViewById(R.id.visit_reason_tab_btn);
        duty_user_tab_btn = (RadioButton) findViewById(R.id.duty_user_tab_btn);
        radioGroup = (RadioGroup) findViewById(R.id.radio_group);
    }

    private void initEvents(){
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.dept_tab_btn:
                        setSelected(0);
                        break;
                    case R.id.employee_tab_btn:
                        setSelected(1);
                        break;
                    case R.id.duty_user_tab_btn:
                        setSelected(2);
                        break;
                    case R.id.visit_reason_tab_btn:
                        setSelected(3);
                        break;
                    case R.id.backup_and_restore:
                        setSelected(4);
                        break;
                    case R.id.go_home:
                        finish();
                        break;
                }
            }
        });
    }

    private void setSelected(int i){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        hideFragment(transaction);

        /*dept_tab_btn.setTextColor(Color.BLACK);
        employee_tab_btn.setTextColor(Color.BLACK);
        visit_reason_tab_btn.setTextColor(Color.BLACK);
        duty_user_tab_btn.setTextColor(Color.BLACK);*/

        switch (i){
            case 0:
                if(departmentList == null) {
                    departmentList = new DepartmentList();
                    transaction.add(R.id.settings_fragment,departmentList);
                }else{

                    transaction.show(departmentList);
                }

                break;
            case 1:
                if( employeeList== null) {
                    employeeList = new EmployeeList();
                    transaction.add(R.id.settings_fragment,employeeList);
                }else {

                    transaction.show(employeeList);
                }
                break;
            case 2:
                if(dutyUserList == null) {
                    dutyUserList = new DutyUserList();
                    transaction.add(R.id.settings_fragment,dutyUserList);
                }else {

                    transaction.show(dutyUserList);
                }
                break;
            case 3:
                if(visitReasonList == null) {
                    visitReasonList = new VisitReasonList();
                    transaction.add(R.id.settings_fragment,visitReasonList);
                }else{

                    transaction.show(visitReasonList);
                }
                break;
            case 4:
                if(backupAndRestore == null) {
                    backupAndRestore = new Backup();
                    transaction.add(R.id.settings_fragment,backupAndRestore);
                }else{

                    transaction.show(backupAndRestore);
                }
                break;
        }
        transaction.commit();
    }

    private void hideFragment(FragmentTransaction transiction){

        if(visitReasonList != null) transiction.hide(visitReasonList);
        if(backupAndRestore != null) transiction.hide(backupAndRestore);
        if(dutyUserList != null) transiction.hide(dutyUserList);
        if(departmentList != null) transiction.hide(departmentList);
        if(employeeList != null) transiction.hide(employeeList);

    }
}

package com.niceguy.app.visitor;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Toast;

public class SettingActivity extends AppCompatActivity {

    private Fragment departmentList,dutyUserList,employeeList,visitReasonList,backupAndRestore,Auth;
    private RadioButton dept_tab_btn,employee_tab_btn,visit_reason_tab_btn,duty_user_tab_btn,backup_and_restore_btn,authorization_btn;
    private RadioGroup radioGroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*if(savedInstanceState!=null){
            FragmentManager fm = getSupportFragmentManager();
            departmentList = fm.findFragmentByTag("departmentList");
            dutyUserList = fm.findFragmentByTag("dutyUserList");
            employeeList = fm.findFragmentByTag("employeeList");
            visitReasonList = fm.findFragmentByTag("visitReasonList");
            backupAndRestore = fm.findFragmentByTag("backupAndRestore");
            Auth = fm.findFragmentByTag("Auth");
        }*/
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        initViews();
        initEvents();
        SharedPreferences config = getSharedPreferences("config", Activity.MODE_PRIVATE);
        if(config.getInt("auth_status",0) == 0){
//            setSelected(5);//auth page
            authorization_btn.performClick();
        }else{
//            setSelected(0);
            dept_tab_btn.performClick();
        }
    }

    private void initViews(){
        dept_tab_btn = (RadioButton) findViewById(R.id.dept_tab_btn);
        employee_tab_btn = (RadioButton) findViewById(R.id.employee_tab_btn);
        visit_reason_tab_btn = (RadioButton) findViewById(R.id.visit_reason_tab_btn);
        duty_user_tab_btn = (RadioButton) findViewById(R.id.duty_user_tab_btn);
        backup_and_restore_btn = (RadioButton) findViewById(R.id.backup_and_restore);
        authorization_btn = (RadioButton) findViewById(R.id.authorization);
        radioGroup = (RadioGroup) findViewById(R.id.radio_group);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if(departmentList == null && fragment instanceof DepartmentList){
            departmentList = (DepartmentList)fragment;
        }

        if(dutyUserList == null && fragment instanceof DutyUserList){
            dutyUserList = (DutyUserList)fragment;
        }

        if(employeeList == null && fragment instanceof EmployeeList){
            employeeList = (EmployeeList)fragment;
        }

        if(visitReasonList == null && fragment instanceof VisitReasonList){
            visitReasonList = (VisitReasonList)fragment;
        }

        if(backupAndRestore == null && fragment instanceof Backup){
            backupAndRestore = (Backup)fragment;
        }

        if(Auth == null && fragment instanceof Authorization){
            Auth = (Authorization)fragment;
        }
    }

    private void initEvents(){
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                SharedPreferences config = getSharedPreferences("config", Activity.MODE_PRIVATE);
                if((
                        checkedId==R.id.dept_tab_btn
                        ||checkedId == R.id.duty_user_tab_btn
                        ||checkedId == R.id.employee_tab_btn
                        ||checkedId == R.id.visit_reason_tab_btn
                        ||checkedId == R.id.backup_and_restore
                   )
                    && config.getInt("auth_status",0) == 0){
                    Toast.makeText(SettingActivity.this, getString(R.string.please_authorize_first), Toast.LENGTH_SHORT).show();
                    authorization_btn.performClick();
                    return;
                }
                switch (checkedId){
                    case R.id.dept_tab_btn:
                        Log.v("YYX","setting");
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
                    case R.id.authorization:
                        setSelected(5);
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

        switch (i){
            case 0:
                if(departmentList == null) {
                    departmentList = new DepartmentList();
                    transaction.add(R.id.settings_fragment,departmentList,"departmentList");
                }else{

                    transaction.show(departmentList);
                }

                break;
            case 1:
                if( employeeList== null) {
                    employeeList = new EmployeeList();
                    transaction.add(R.id.settings_fragment,employeeList,"employeeList");
                }else {

                    transaction.show(employeeList);
                }
                break;
            case 2:
                if(dutyUserList == null) {
                    dutyUserList = new DutyUserList();
                    transaction.add(R.id.settings_fragment,dutyUserList,"dutyUserList");
                }else {

                    transaction.show(dutyUserList);
                }
                break;
            case 3:
                if(visitReasonList == null) {
                    visitReasonList = new VisitReasonList();
                    transaction.add(R.id.settings_fragment,visitReasonList,"visitReasonList");
                }else{

                    transaction.show(visitReasonList);
                }
                break;
            case 4:
                if(backupAndRestore == null) {
                    backupAndRestore = new Backup();
                    transaction.add(R.id.settings_fragment,backupAndRestore,"backupAndRestore");
                }else{

                    transaction.show(backupAndRestore);
                }
                break;
            case 5:
                if(Auth == null) {
                    Auth = new Authorization();
                    transaction.add(R.id.settings_fragment,Auth,"Auth");
                }else{

                    transaction.show(Auth);
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
        if(Auth != null) transiction.hide(Auth);

    }
}

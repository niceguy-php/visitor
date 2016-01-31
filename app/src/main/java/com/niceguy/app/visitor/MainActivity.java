package com.niceguy.app.visitor;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.niceguy.app.utils.DBHelper;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private final static int SCANNIN_GREQUEST_CODE = 1;
    private String TAG = "Main";
    private String tabSelectedColor = "#65a5ef";
    private LinearLayout homeTab;
    private LinearLayout visitorHistoryTab;
    private LinearLayout visitorLeaveTab;
    private LinearLayout visitorRegisterTab;
    private LinearLayout existTab;

    private TextView homeTxt;
    private TextView historyTxt;
    private TextView leaveTxt;
    private TextView registerTxt;

    private Fragment homeFragment;
    private Fragment historyFragment;
    private Fragment leaveFragment;
    private Fragment registerFragment;

    private static final int RE_CAPTURE = 0;
    private static final int CAPTURE_OK = 1;

    private DBHelper helper = null;
    private SQLiteDatabase db = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        responseIntent(getIntent().getIntExtra("status", -1));

        initView();
        initEvent();
        setSelected(0);

//        visitLog();
    }

    private void responseIntent(int status){
        if(status != -1){
            setSelected(1);
            if( status== RE_CAPTURE){
                Toast.makeText(MainActivity.this,"重拍",Toast.LENGTH_LONG).show();
            }else if(status == CAPTURE_OK){
                Toast.makeText(MainActivity.this,"保存成功",Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initView(){
        homeTab = (LinearLayout)findViewById(R.id.id_tab_home);
        visitorHistoryTab = (LinearLayout)findViewById(R.id.id_tab_visitor_history);
        visitorLeaveTab = (LinearLayout)findViewById(R.id.id_tab_visitor_leave);
        visitorRegisterTab = (LinearLayout)findViewById(R.id.id_tab_visitor_register);
        existTab = (LinearLayout)findViewById(R.id.id_tab_exit);

        homeTxt = (TextView)findViewById(R.id.homeTxt);
        historyTxt = (TextView)findViewById(R.id.historyTxt);
        registerTxt = (TextView)findViewById(R.id.registerTxt);
        leaveTxt = (TextView)findViewById(R.id.leaveTxt);

    }

    private void initEvent(){
        homeTab.setOnClickListener(this);
        visitorHistoryTab.setOnClickListener(this);
        visitorLeaveTab.setOnClickListener(this);
        visitorRegisterTab.setOnClickListener(this);
        existTab.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.id_tab_exit:
//            case R.id.exit_fragment_click:
                finish();
                break;
            case R.id.id_tab_home:
                setSelected(0);
                break;
            case R.id.id_tab_visitor_register:
                setSelected(1);
                break;
            case R.id.id_tab_visitor_leave:
                /*Intent intent = new Intent();
                intent.setClass(MainActivity.this, QrCodeActivity.class);
                startActivity(intent);*/
                setSelected(3);
                break;
            case R.id.id_tab_visitor_history:
                setSelected(2);
                break;
            case R.id.history_fragment_click:
//                visitorHistoryTab.performClick();
                break;
            case R.id.register_fragment_click:
//                visitorRegisterTab.performClick();
                break;
            case R.id.setting_fragment_click:
//                homeTab.performClick();
                break;
            case R.id.leave_fragment_click:
//                homeTab.performClick();
                break;
            default:
                break;
        }
    }

    private void setSelected(int i){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        hideFragment(transaction);

        homeTxt.setTextColor(Color.BLACK);
        historyTxt.setTextColor(Color.BLACK);
        leaveTxt.setTextColor(Color.BLACK);
        registerTxt.setTextColor(Color.BLACK);

        homeTab.setBackgroundResource(R.drawable.menu_border_color);
        visitorHistoryTab.setBackgroundResource(R.drawable.menu_border_color);
        visitorRegisterTab.setBackgroundResource(R.drawable.menu_border_color);
        visitorLeaveTab.setBackgroundResource(R.drawable.menu_border_color);

        Log.v(TAG, "------------------" + String.valueOf(i));
        switch (i){
            case 0:
                if(homeFragment == null) {
                    homeFragment = new HomeFragment();
                    transaction.add(R.id.id_fragment_content,homeFragment);
                }else{

                    transaction.show(homeFragment);
                }

                homeTab.setBackgroundColor(Color.parseColor(tabSelectedColor));
                homeTxt.setTextColor(Color.WHITE);
                break;
            case 1:
                if( registerFragment== null) {
                    registerFragment = new VisitorRegisterFragment();
                    transaction.add(R.id.id_fragment_content,registerFragment);
                }else {

                    transaction.show(registerFragment);
                }
                visitorRegisterTab.setBackgroundColor(Color.parseColor(tabSelectedColor));
                registerTxt.setTextColor(Color.WHITE);
                break;
            case 2:
                if(historyFragment == null) {
                    historyFragment = new VisitorHistoryFragment();
                    transaction.add(R.id.id_fragment_content,historyFragment);
                }else {

                    transaction.show(historyFragment);
                }
                visitorHistoryTab.setBackgroundColor(Color.parseColor(tabSelectedColor));
                historyTxt.setTextColor(Color.WHITE);
                break;
            case 3:
                if(leaveFragment == null) {
                    leaveFragment = new VisitorLeaveFragment();
                    transaction.add(R.id.id_fragment_content,leaveFragment);
                }else{

                    transaction.show(leaveFragment);
                }
                visitorLeaveTab.setBackgroundColor(Color.parseColor(tabSelectedColor));
                leaveTxt.setTextColor(Color.WHITE);
                break;
        }
        transaction.commit();
    }

    private void hideFragment(FragmentTransaction transiction){

        if(historyFragment != null) transiction.hide(historyFragment);
        if(homeFragment != null) transiction.hide(homeFragment);
        if(leaveFragment != null) transiction.hide(leaveFragment);
        if(registerFragment != null) transiction.hide(registerFragment);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*switch (requestCode) {
            case SCANNIN_GREQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    // 显示扫描到的内容
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction transaction = fm.beginTransaction();
                    leaveFragment.setArguments(bundle);
                    transaction.commit();
                }
                break;
        }*/
    }


    private void visitLog(){

        //生成ContentValues对象 //key:列名，value:想插入的值
        ContentValues cv = new ContentValues();
        //往ContentValues对象存放数据，键-值对模式
//        cv.put("_id",1);
        cv.put("reason", "这是一个测试");

        connectDB();
        helper.insert(helper.TABLE_VISIT_LOG,  cv);
        helper.insert(helper.TABLE_VISIT_LOG,  cv);
        helper.insert(helper.TABLE_VISIT_LOG,  cv);
        helper.insert(helper.TABLE_VISIT_LOG,  cv);
        helper.insert(helper.TABLE_VISIT_LOG,  cv);
        helper.insert(helper.TABLE_VISIT_LOG,  cv);
        helper.insert(helper.TABLE_VISIT_LOG,  cv);
        helper.insert(helper.TABLE_VISIT_LOG,  cv);
        //关闭数据库
//        db.close();
        Cursor cursor = helper.fetchAll(helper.TABLE_VISIT_LOG,0,1);
        while(cursor.moveToNext()){
            String reason = cursor.getString(cursor.getColumnIndex("reason"));
            System.out.println("query------->" + "reason：" + reason);
            Toast.makeText(this,reason,Toast.LENGTH_LONG).show();
        }
        cursor.close();
        releaseDB();
    }
    private void connectDB(){
        if(helper==null){
            helper = DBHelper.getInstance(this);
        }
    }
    private void releaseDB(){
        /*if(helper!=null){
            helper.close();
        }*/
    }


}

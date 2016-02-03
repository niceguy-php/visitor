package com.niceguy.app.visitor;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.niceguy.app.utils.DateTimePicker;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;


/**
 * Created by qiumeilin on 2016/1/9.
 */
public class Authorization extends Fragment implements View.OnClickListener{

    private final static int SCANNIN_GREQUEST_CODE = 1;

    public static String key = "";
    public static String some = "";
    public static Activity activity = null;
    public static SharedPreferences config = null;
    private Button scan = null,manual = null,auth_cancel=null;
    public static TextView auth_status,auth_time;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.auth, container, false);
        activity = getActivity();
        config = activity.getSharedPreferences("config", Activity.MODE_PRIVATE);
        some = getString(R.string.some_key);
        initViews(view);
        initEvents();

        return view;
    }

    private void initViews(View view){


        auth_status = (TextView) view.findViewById(R.id.auth_status);
        auth_time = (TextView) view.findViewById(R.id.auth_time);
        auth_cancel = (Button) view.findViewById(R.id.auth_cancel);
        scan = (Button) view.findViewById(R.id.scan_key);
        manual = (Button) view.findViewById(R.id.input_key);
        int status = config.getInt("auth_status",0);
        String time = config.getString("auth_time","");
        setAuthStatus(status);
        auth_time.setText(time);


    }

    private void initEvents(){
        scan.setOnClickListener(this);
        manual.setOnClickListener(this);
        auth_cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.scan_key:
                Intent intent = new Intent();
                intent.putExtra("from","auth");
                intent.setClass(getActivity(), CaptureActivity.class);
                startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
                break;
            case R.id.input_key:
                final EditText input = new EditText(activity);
                new AlertDialog.Builder(activity)
                        .setTitle("请输入授权码")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setView(input)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                key = input.getText().toString();
                                Log.v("YYX",key);
                                callBack();
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
                break;
            case R.id.auth_cancel:
                config.edit().clear().commit();
                setAuthStatus(0);
                break;
        }
    }


    public static void callBack(){


        if(key != null && !"".equals(key)){

            if(some.equals(md5(key))) {
                SharedPreferences.Editor editor = config.edit();
                int status = 1;
                editor.putString("key", key);
                editor.putInt("auth_status",status);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timeStr = sdf.format(new Date().getTime());
                editor.putString("auth_time", timeStr);
                editor.commit();
                setAuthStatus(status);
                Toast.makeText(activity, "授权认证成功", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(activity, "授权认证失败，秘钥信息不正确", Toast.LENGTH_SHORT).show();
            }
        }
    }



    public static String md5(String str){

        String encode_str = null;
        MessageDigest md5=null;
        try {
           md5 = MessageDigest.getInstance("MD5");
            if(md5 != null){
                try {
                    byte[] b = md5.digest(str.getBytes("utf-8"));
                    StringBuilder sb = new StringBuilder(40);
                    for(byte x:b) {
                        if((x & 0xff)>>4 == 0) {
                            sb.append("0").append(Integer.toHexString(x & 0xff));
                        } else {
                            sb.append(Integer.toHexString(x & 0xff));
                        }
                    }
                    encode_str = sb.toString();

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        Log.v("YYX",encode_str+'='+key);
        return encode_str;

    }

    public static void setAuthStatus(int status){
        if(status == 0){
            auth_status.setText("未授权");
            auth_status.setTextColor(Color.RED);
            auth_time.setText("");
        }else{
            auth_status.setText("已授权");
            auth_status.setTextColor(Color.GREEN);
            auth_time.setText(config.getString("auth_time",""));
        }
    }



}

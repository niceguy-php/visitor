package com.niceguy.app.visitor;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.niceguy.app.utils.DBHelper;
import com.zkc.pc700.helper.BarcodeCreater;

import java.util.Date;

/**
 * Created by qiumeilin on 2016/1/9.
 */
public class VisitorLeaveFragment extends Fragment{

    private final static int SCANNIN_GREQUEST_CODE = 1;

    public static Bitmap bitmapRec=null;
    public static String barCode = null;
    public static Activity activity = null;
    public static LinearLayout register_sign = null;

    public static DBHelper helper;

    /**
     * 显示扫描结果
     */
    private static TextView mTextView;
    /**
     * 显示扫描拍的图片
     */
    private static ImageView mImageView;
    private static ImageView barCodeImage;
    private static ImageView avatarImage;

    private static TextView visitor_name,visited_name,visit_time,visited_dept,visitor_count,visit_reason,visit_status;
    public static Bitmap avatarBitmap = null;
    private Spinner duty_person_in_leave = null;
    private static int log_id=0;

//    private DBHelper helper;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_scan,container,false);

        connectDB();

        activity = getActivity();
//        mTextView = (TextView) view.findViewById(R.id.result);
        mImageView = (ImageView) view.findViewById(R.id.qrcode_bitmap);
        barCodeImage = (ImageView) view.findViewById(R.id.bar_code);
        avatarImage = (ImageView) view.findViewById(R.id.print_avatar);
        duty_person_in_leave = (Spinner) view.findViewById(R.id.duty_person_in_leave);

        visit_reason  = (TextView) view.findViewById(R.id.preview_visit_reason);
        visitor_count  = (TextView) view.findViewById(R.id.preview_visitor_count);
        visitor_name  = (TextView) view.findViewById(R.id.preview_visitor_name);
        visit_time  = (TextView) view.findViewById(R.id.preview_visit_time);
        visited_dept  = (TextView) view.findViewById(R.id.preview_visited_dept);
        visited_name  = (TextView) view.findViewById(R.id.preview_visited_name);
        visit_status  = (TextView) view.findViewById(R.id.leave_visit_status);

        String[] duty_persons = helper.getUserNames(EmployeeList.USER_TYPE_DUTY);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, duty_persons);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        duty_person_in_leave.setAdapter(adapter);

        register_sign = (LinearLayout) view.findViewById(R.id.register_sign);


        Intent intent = new Intent();
        intent.setClass(getActivity(), CaptureActivity.class);
        startActivityForResult(intent, SCANNIN_GREQUEST_CODE);

        // 点击按钮跳转到二维码扫描界面，这里用的是startActivityForResult跳转
        // 扫描完了之后调到该界面
        Button mButton = (Button) view.findViewById(R.id.btn_barcode_read);
        Button mButtonIdCardReadLeave = (Button) view.findViewById(R.id.btn_idcard_read);
        Button mFinishVisit = (Button) view.findViewById(R.id.finish_visit);

        mButtonIdCardReadLeave.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "开发中.....", Toast.LENGTH_SHORT).show();
            }
        });

        mButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), CaptureActivity.class);
                startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
            }
        });

        mFinishVisit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues cv = new ContentValues();
                cv.put("visit_status",1);
                cv.put("leave_time",new Date().getTime());
                if(helper.update(helper.TABLE_VISIT_LOG,cv,log_id)){
                    log_id = 0;
                    visit_status.setText("已登记离开");
                    visit_status.setTextColor(Color.GREEN);
                    Toast.makeText(getActivity(),"操作成功",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getActivity(),"操作失败，请重试",Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    public static void callBack(){

        register_sign.setVisibility(View.INVISIBLE);
        if(barCode != null){
//            mTextView.setText(barCode);
            Cursor cur = helper.getVisitLogByBarcode(barCode);
            if(cur!=null && cur.getCount()>0){
                cur.moveToFirst();
                log_id = cur.getInt(cur.getColumnIndex("_id"));
                avatarImage.setImageBitmap(BitmapFactory.decodeFile(cur.getString(cur.getColumnIndex("idcard_avatar"))));
                visit_reason.setText(cur.getString(cur.getColumnIndex("visit_reason")));
                visitor_name.setText(cur.getString(cur.getColumnIndex("visitor_name")));
                visitor_count.setText(cur.getString(cur.getColumnIndex("visitor_count")));
                visited_dept.setText(cur.getString(cur.getColumnIndex("visited_dept_name")));
                visited_name.setText(cur.getString(cur.getColumnIndex("visited_username")));
                visit_time.setText(cur.getString(cur.getColumnIndex("visit_time")));
                int status = cur.getInt(cur.getColumnIndex("visit_status"));
                Log.v("YYX","-----"+status);
                if( status== 0){
                    visit_status.setText("未离开");
                }else{
                    visit_status.setText("已经登记离开");
                    visit_status.setTextColor(Color.GREEN);
                }
            }
        }
        if(bitmapRec !=null ){
            mImageView.setImageBitmap(bitmapRec);
        }
        Bitmap barCodePic = null;
        barCodePic = BarcodeCreater.creatBarcode(activity,
                barCode, 380, 70, true, 1);// 最后一位参数是条码格式

        if(barCodePic != null){
            barCodeImage.setImageBitmap(barCodePic);
        }

        if(avatarBitmap != null){
            avatarImage.setImageBitmap(avatarBitmap);
        }
        barCode = null;
        bitmapRec = null;
    }

    private void connectDB(){
        if(helper==null){
            helper = new DBHelper(getContext());
        }
    }
}

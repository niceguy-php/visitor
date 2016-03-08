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
import android.os.Environment;
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

import com.hdos.idCardUartDevice.publicSecurityIDCardLib;
import com.niceguy.app.utils.DBHelper;
import com.synjones.bluetooth.DecodeWlt;
import com.synjones.sdt.IDCard;
import com.synjones.sdt.SerialPort;
import com.zkc.pc700.helper.BarcodeCreater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;
import java.util.Date;
import java.util.UUID;

import hdx.HdxUtil;

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

    private static TextView visitor_name,visited_name,visit_time,visited_dept,visitor_count,visit_reason,visit_status,visit_take,visit_car_num;
    public static Bitmap avatarBitmap = null;
    private Spinner duty_person_in_leave = null;
    private static int log_id=0;


    private publicSecurityIDCardLib iDCardDevice;
    String port="/dev/ttyS1";

    protected android_serialport_api.SerialPort mSerialPort;

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
        visit_car_num  = (TextView) view.findViewById(R.id.preview_car_num);
        visit_take  = (TextView) view.findViewById(R.id.preview_visit_take);

        final String[] duty_persons = helper.getUserNames(EmployeeList.USER_TYPE_DUTY);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, duty_persons);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        duty_person_in_leave.setAdapter(adapter);

        register_sign = (LinearLayout) view.findViewById(R.id.register_sign);


        /*Intent intent = new Intent();
        intent.setClass(getActivity(), CaptureActivity.class);
        startActivityForResult(intent, SCANNIN_GREQUEST_CODE);*/

        // 点击按钮跳转到二维码扫描界面，这里用的是startActivityForResult跳转
        // 扫描完了之后调到该界面
        final Button mButton = (Button) view.findViewById(R.id.btn_barcode_read);
        final Button mButtonIdCardReadLeave = (Button) view.findViewById(R.id.btn_idcard_read);
        Button mFinishVisit = (Button) view.findViewById(R.id.finish_visit);

        mButtonIdCardReadLeave.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                mButtonIdCardReadLeave.setBackgroundColor(Color.parseColor("#3F51B5"));
                mButtonIdCardReadLeave.setTextColor(Color.WHITE);
                mButton.setBackgroundColor(Color.parseColor("#D4D6D5"));
                mButton.setTextColor(Color.BLACK);
                readCard();
            }
        });

        mButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mButton.setBackgroundColor(Color.parseColor("#3F51B5"));
                mButton.setTextColor(Color.WHITE);
                mButtonIdCardReadLeave.setBackgroundColor(Color.parseColor("#D4D6D5"));
                mButtonIdCardReadLeave.setTextColor(Color.BLACK);
                Intent intent = new Intent();
                intent.setClass(getActivity(), CaptureActivity.class);
                startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
            }
        });

        mFinishVisit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(log_id>0){
                    ContentValues cv = new ContentValues();
                    cv.put("visit_status",1);
                    cv.put("duty_username_leave",duty_person_in_leave.getSelectedItem().toString());
                    cv.put("leave_time",new Date().getTime());
                    if(helper.update(helper.TABLE_VISIT_LOG,cv,log_id)){
                        log_id = 0;
                        visit_status.setText("已离开");
                        visit_status.setTextColor(Color.GREEN);
                        Toast.makeText(getActivity(),"操作成功",Toast.LENGTH_SHORT).show();
                        getActivity().findViewById(R.id.id_tab_home).performClick();
                    }else{
                        Toast.makeText(getActivity(),"操作失败，请重试",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getActivity(),"请扫描条码或放入访客身份证识别登记离开",Toast.LENGTH_SHORT).show();
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
                visit_take.setText(cur.getString(cur.getColumnIndex("visitor_take")));
                visit_car_num.setText(cur.getString(cur.getColumnIndex("visitor_car_num")));

                String status = cur.getString(cur.getColumnIndex("visit_status"));
                visit_status.setText(status);
                Log.v("YYX", "-----" + status);
                if( status== "未离开"){
                    visit_status.setTextColor(Color.RED);
                }else{
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

    private void readCard() {

        byte[] bname = new byte[32];
        byte[] bsex = new byte[6];
        byte[] bbirth = new byte[18];
        byte[] bnation = new byte[12];
        byte[] baddress = new byte[72];
        byte[] bDepartment = new byte[32];
        byte[] bIDNo = new byte[38];
        byte[] bEffectDate = new byte[18];
        byte[] bExpireDate = new byte[18];
        byte[] bpErrMsg = new byte[20];
        byte[] bBmpFile = new byte[38556];

        int retval;
        String pkName="",soPath="";
        pkName=getActivity().getPackageName();
        soPath = "/data/app-lib/"+pkName+"-1/"+"libwlt2bmp.so";
        File so = new File(soPath);
        if(so.exists()){
            Log.v("YYX","---------so exist------");
        }else{
            soPath = "/data/app-lib/"+pkName+"-2/"+"libwlt2bmp.so";
        }
        iDCardDevice = new publicSecurityIDCardLib();
        HdxUtil.SetIDCARDPower(1);
        HdxUtil.SwitchSerialFunction(HdxUtil.SERIAL_FUNCTION_IDCARD);
        String id_number = "";
        try {
            retval = iDCardDevice.readBaseMsg(port,soPath,bBmpFile, bname, bsex, bnation, bbirth, baddress, bIDNo, bDepartment,
                    bEffectDate, bExpireDate,bpErrMsg);

            if (retval < 0) {
                Toast.makeText(getActivity(), "读卡错误，原因：" + new String(bpErrMsg, "Unicode"), Toast.LENGTH_SHORT);
            } else {
                id_number = new String(bIDNo, "Unicode");
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (!"".equals(id_number)) {
            Cursor cur = helper.getVisitLogByIdNumber(id_number);
            register_sign.setVisibility(View.INVISIBLE);

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
                visit_take.setText(cur.getString(cur.getColumnIndex("visitor_take")));
                visit_car_num.setText(cur.getString(cur.getColumnIndex("visitor_car_num")));
                String status = cur.getString(cur.getColumnIndex("visit_status"));
                visit_status.setText(status);
                Log.v("YYX","-----"+status);
                if( status== "未离开"){
                    visit_status.setTextColor(Color.RED);
                }else{
                    visit_status.setTextColor(Color.GREEN);
                }

                Bitmap barCodePic = null;
                barCodePic = BarcodeCreater.creatBarcode(getActivity(),
                        cur.getString(cur.getColumnIndex("barcode")), 380, 70, true, 1);// 最后一位参数是条码格式

                if(barCodePic != null){
                    barCodeImage.setImageBitmap(barCodePic);
                }

            }
            cur.close();


        }else{
            Toast.makeText(getActivity(),"请放入访客的身份证",Toast.LENGTH_SHORT).show();
        }

    }

    private void connectDB(){
        if(helper==null){
            helper = DBHelper.getInstance(getActivity());
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(hidden){
            visit_reason.setText("");
            visitor_name.setText("");
            visitor_count.setText("");
            visited_dept.setText("");
            visited_name.setText("");
            visit_time.setText("");
            visit_car_num.setText("");
            visit_take.setText("");
            visit_status.setText("");

            avatarImage.setImageResource(R.mipmap.photo);
        }

    }
}

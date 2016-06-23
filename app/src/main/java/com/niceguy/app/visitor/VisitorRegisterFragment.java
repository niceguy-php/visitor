package com.niceguy.app.visitor;


import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.LayoutInflaterFactory;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

import com.hdos.idCardUartDevice.publicSecurityIDCardLib;
import com.niceguy.app.utils.DBHelper;
import com.synjones.bluetooth.DecodeWlt;
import com.synjones.sdt.IDCard;
//import_view com.synjones.sdt.SerialPort;
import android_serialport_api.SerialPort;
import hdx.HdxUtil;

import com.zkc.helper.printer.PrintService;
import com.zkc.helper.printer.PrinterClass;
import com.zkc.pc700.helper.BarcodeCreater;
import com.zkc.pc700.helper.PrinterClassSerialPort;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by qiumeilin on 2016/1/9.
 */
public class VisitorRegisterFragment extends Fragment implements SurfaceHolder.Callback, OnClickListener {

    private static final String TAG = "PRINT";
    private static final String STROE_IDCARD_AVATAR_PATH = "/sicheng/idcard_avatar/";
    private static final String STORE_TAKE_PICTURE_PATH = "/sicheng/take_avatar/";
    private static final int AVATAR_CAMERA = 0;//拍头像的
    private static final int CARD_CAMEAR = 1;//拍证件身份证等卡片的
    private int cameraId = AVATAR_CAMERA;
    private PrinterClassSerialPort printerClass = null;
    private Camera mCamera,mCamera1,tmpCamera;
    private SurfaceView cameraPreview;
    private SurfaceHolder mHolder;

    private publicSecurityIDCardLib iDCardDevice;
    private IDCard idcard = null;
    private Spinner visit_reason;
    private Spinner be_visited_dept, duty_person, certificate_type;
    private Button read_id_card, capture,cardCapture, clear_register, print_preview, visitor_register;
    private ArrayAdapter<String> adapter;
    private TextView birthday, birthplace, police, valid_date;
    private EditText name,address,id_number,ethnic;
    Spinner sex;
    private ImageView avatar;
    private String wltPath, bmpPath;
    private View dialog_layout;

    private AlertDialog capturePreviewDialog,detailDialog,cardCapturePreview;
    private AlertDialog printPreviewDialog;
    private String cameraTakeAvatarPath = null, idCardAvatarPath = null;
    private Bitmap barCodePic = null, idCardAvatarPic = null;
    private String barCodeString = null;

    private EditText visitorCount = null;
    private EditText visitorPhone = null;
    private EditText visitedPhone = null;
    private EditText visitedName = null;
    private EditText visitorCarNum = null;
    private EditText visitorTake = null;
    private EditText visitedPos = null;
    private DBHelper helper = null;
    private AutoCompleteTextView be_visited_name = null;

    private RadioButton visitedMale,visitedFemale;

    public ProgressDialog progressDialog = null;

    private ListView recent_visit_log_listview = null;

    private long visit_time = 0;
    String port="/dev/ttyS1";

    protected SerialPort mSerialPort;
    protected OutputStream mOutputStream;
    protected ReadThread mReadThread;
    private int n = 0;

    MyHandler handler;
    private boolean stop = false;
    private final int PRINTIT =1;
    ExecutorService pool = Executors.newSingleThreadExecutor();
    PowerManager.WakeLock lock;
    int printer_status=0;
    private InputStream mInputStream;

    public void chooseCamera(int camera_id){
        this.cameraId = camera_id;
    }

    private class MyHandler extends Handler {
        public void handleMessage(Message msg) {
            if(stop == true) return;
            switch (msg.what) {
                case PRINTIT:
                    final ArrayList<String> rInfoList = new ArrayList<String>();
    				/*rInfoList.add("       点评团");
    				rInfoList.add("       t.dianping.com");
    				rInfoList.add("--------------------------------------------");
    				rInfoList.add("验证消费成功");
    				rInfoList.add("套餐名称: " + "套餐名称: "+"套餐名称: "+"套餐名称: "+"套餐名称: ");
    				rInfoList.add("售价: " + "111111.11");
    				rInfoList.add("上线时间: " + "2013年7月11日");
    				rInfoList.add("商户名称: " + "商户名称商户名称商户名称");
    				rInfoList.add("终端编号:  " + "12345678901234567890");
    				rInfoList.add(atMiddleNormal("-------------------------------"));
    				rInfoList.add("序列号:  " + "12345678901234567890");
    				rInfoList.add("验证时间: "+ "2013年7月11日10时10分10秒");*/


                    new WriteThread(rInfoList).start();
    		/*		pool.submit(new Runnable() {

    					public void run() {
    						sendCharacterDemo(rInfoList);
    						ConsoleActivity.this.sleep(1000);
    						sendCharacterDemo(rInfoList);
    						ConsoleActivity.this.sleep(5000);
    						handler.sendMessage(handler.obtainMessage(PRINTIT, 1, 0,null));
    					}

    				});*/
                    //	handler.sendMessageDelayed(handler.obtainMessage(PRINTIT, 1, 0,null), 3000);
                    break;
                default:
                    break;
            }
        }
    }

    class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            while(!isInterrupted()) {
                int size;
                try {
                    byte[] buffer = new byte[64];

                    if (mInputStream == null) return;
                    size = mInputStream.read(buffer);
                    if (size > 0) {
                        onDataReceived(buffer, size,n);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.visitor_register_tab, container, false);

        connectDB();
        initViews(view);
        initEvents();

        initNewPrinter();

        String companyFolder = Environment.getExternalStorageDirectory().getPath()
                + STROE_IDCARD_AVATAR_PATH;// 配置文件文件夹
        File config = new File(companyFolder);
        if (!config.exists()) {
            config.mkdirs();
        }
        wltPath = companyFolder + "kk.jpg";
        bmpPath = companyFolder + "kk1.jpg";
        return view;
    }

    private void initViews(View view) {

        connectDB();
        cameraPreview = (SurfaceView) view.findViewById(R.id.camera_preview);
        mHolder = cameraPreview.getHolder();
        visit_reason = (Spinner) view.findViewById(R.id.visit_reason);
        be_visited_dept = (Spinner) view.findViewById(R.id.be_visited_dept);
        duty_person = (Spinner) view.findViewById(R.id.duty_person);
        certificate_type = (Spinner) view.findViewById(R.id.certificate_type);
        read_id_card = (Button) view.findViewById(R.id.read_id_card);
        capture = (Button) view.findViewById(R.id.btn_capture);
        cardCapture = (Button) view.findViewById(R.id.btn_card_capture);
        mHolder.addCallback(this);

        String[] visit_reasons = helper.getVisitReasons();
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, visit_reasons);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        visit_reason.setAdapter(adapter);

        String[] dept = helper.getDeptNames();
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, dept);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        be_visited_dept.setAdapter(adapter);


        String[] duty_persons = helper.getUserNames(EmployeeList.USER_TYPE_DUTY);
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, duty_persons);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        duty_person.setAdapter(adapter);
        duty_person.setSelection(0,true);

        avatar = (ImageView) view.findViewById(R.id.avatar_on_id_card);
        name = (EditText) view.findViewById(R.id.name_on_id_card);
        sex = (Spinner) view.findViewById(R.id.sex_on_id_card);
        String[] sex_list = {"男","女"};
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, sex_list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sex.setAdapter(adapter);

        String[] certificate_list = {"身份证","警官证","学生证","教师证","驾驶证","其他"};
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, certificate_list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        certificate_type.setAdapter(adapter);

        birthday = (TextView) view.findViewById(R.id.birthday_on_id_card);
//        birthplace = (TextView) view.findViewById(R.id.birthplace_on_id_card);
        address = (EditText) view.findViewById(R.id.address_on_id_card);
        police = (TextView) view.findViewById(R.id.police_on_id_card);
        valid_date = (TextView) view.findViewById(R.id.valid_date_on_id_card);
        id_number = (EditText) view.findViewById(R.id.number_on_id_card);
        ethnic = (EditText) view.findViewById(R.id.ethnic_on_id_card);

        clear_register = (Button) view.findViewById(R.id.clear_register_btn);
        print_preview = (Button) view.findViewById(R.id.print_preview_btn);
        visitor_register = (Button) view.findViewById(R.id.visitor_register_btn);
        clear_register = (Button) view.findViewById(R.id.clear_register_btn);

        visitorCount = (EditText) view.findViewById(R.id.visitor_num);
        visitorPhone = (EditText) view.findViewById(R.id.visitor_tel);
        visitedPhone = (EditText) view.findViewById(R.id.be_visited_tel);
        visitorTake = (EditText) view.findViewById(R.id.visitor_take);
        visitorCarNum = (EditText) view.findViewById(R.id.visitor_car_num);
//        visitedName = (EditText) view.findViewById(R.id.be_visited_name);
        visitedPos = (EditText) view.findViewById(R.id.be_visited_pos);
        visitedFemale = (RadioButton) view.findViewById(R.id.visitedSexFemale);
        visitedMale = (RadioButton) view.findViewById(R.id.visitedSexMale);

        be_visited_name = (AutoCompleteTextView) view.findViewById(R.id.be_visited_name);

        recent_visit_log_listview = (ListView) view.findViewById(R.id.recent_visit_log);
        releaseDB();

    }

    private void initEvents() {
        read_id_card.setOnClickListener(this);
        capture.setOnClickListener(this);
        cardCapture.setOnClickListener(this);
        visitor_register.setOnClickListener(this);
        print_preview.setOnClickListener(this);
        clear_register.setOnClickListener(this);

        be_visited_dept.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String name = be_visited_dept.getSelectedItem().toString();
                connectDB();
                String[] deptUsers = helper.getUserNamesByDeptName(EmployeeList.USER_TYPE_EMPLOYEE, name);
                Log.v("YYX", StringUtils.join(deptUsers,','));
                releaseDB();
                ArrayAdapter<String> av = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_dropdown_item_1line, deptUsers);
                be_visited_name.setAdapter(av);
                if (deptUsers.length > 0) {
                    be_visited_name.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String n = (String) parent.getItemAtPosition(position);
//                            toast(n);
                            int p = n.indexOf("-(");
                            if (p != -1) {
                                String username = n.substring(0, p);
                                String dept_name = n.substring(p + 2, n.length() - 1);
                                connectDB();
                                Cursor c = helper.fetchUserByDeptNameAndUserName(username, dept_name);
                                if (c.getCount() > 0) {
                                    c.moveToFirst();
                                    String sex = c.getString(c.getColumnIndex("sex"));
                                    String phone = c.getString(c.getColumnIndex("phone"));
                                    String pos = c.getString(c.getColumnIndex("position"));

                                    visitedPos.setText(pos);
                                    visitedPhone.setText(phone);
                                    if ("男".equals(sex)) {
                                        visitedMale.setChecked(true);
                                        visitedFemale.setChecked(false);
                                    } else {
                                        visitedMale.setChecked(false);
                                        visitedFemale.setChecked(true);
                                    }
                                }
                                c.close();
                                releaseDB();
                            }
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        be_visited_dept.setSelection(0, true);

        recent_visit_log_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

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
                detail_visitor_idno.setText(c.getString(c.getColumnIndex("visitor_idno")));

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
                String in_duty_username = c.getString(c.getColumnIndex("duty_username")) + "(进)";
                String leave_duty_username = c.getString(c.getColumnIndex("duty_username_leave"));
                if (leave_duty_username != null) {
                    leave_duty_username = "，" + c.getString(c.getColumnIndex("duty_username_leave")) + "(出)";
                } else {
                    leave_duty_username = "";
                }
                detail_duty_user.setText(in_duty_username + leave_duty_username);

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
    public void onResume() {
        super.onResume();
        if (mCamera == null) {
            chooseCamera(AVATAR_CAMERA);
            mCamera = getCamera();


            if (mHolder != null) {
                setStartPreview(mCamera, mHolder);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDestroyView() {
        try
        {
           if (progressDialog != null) progressDialog.dismiss();
           if (printPreviewDialog != null) printPreviewDialog.dismiss();
           if (capturePreviewDialog != null) capturePreviewDialog.dismiss();
           if (mReadThread != null) mReadThread.interrupt();
           if (mSerialPort != null) {
               mSerialPort.close();
               mSerialPort = null;
           }

            if (mOutputStream != null)  mOutputStream.close();
            if (mInputStream != null)   mInputStream.close();
        } catch (IOException e) {
        }

        super.onDestroyView();
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseCamera();
    }

    public void capture() {
//        if(dialog != null) dialog.dismiss();
        loading(getString(R.string.capturing));
        releaseCamera1();
        if (mCamera == null) {
            chooseCamera(AVATAR_CAMERA);
            mCamera = getCamera();
        }
        setStartPreview(mCamera,mHolder);
        Camera.Parameters param = mCamera.getParameters();
        param.setPictureFormat(ImageFormat.JPEG);
        param.setPreviewSize(800, 400);
//        param.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
//        mCamera.autoFocus(new Camera.AutoFocusCallback() {
//            @Override
//            public void onAutoFocus(boolean success, Camera camera) {
//                if (success) {
                    mCamera.takePicture(null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {

                            String filename = UUID.randomUUID().toString() + ".jpg";
                            Log.v("YYX",filename);
                            Log.v("YYX","=================="+data.length);
                            String directory = Environment.getExternalStorageDirectory() + STORE_TAKE_PICTURE_PATH;
                            String path = directory + filename;
                            File dir = new File(directory);
                            File file = new File(path);
                            if (!dir.exists()) {
                                dir.mkdirs();
                            }
                            FileOutputStream fos = null, fos1 = null;
                            try {
                                fos = new FileOutputStream(file);
                                fos.write(data);
                                LayoutInflater factory = LayoutInflater.from(getActivity());
                                dialog_layout = factory.inflate(R.layout.avatar_preview, null);
                                Bitmap b = BitmapFactory.decodeFile(file.getAbsolutePath());
//                                Matrix matrix = new Matrix();
//                                matrix.setRotate(180);
//                                Bitmap bitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
                                ImageView iv = (ImageView) dialog_layout.findViewById(R.id.mAvatar);
                                fos1 = new FileOutputStream(file);
                                b.compress(Bitmap.CompressFormat.JPEG, 90, fos1);
                                fos.flush();
                                if(cameraId == AVATAR_CAMERA){
                                    cameraTakeAvatarPath = path;
                                }else{
                                    idCardAvatarPath = path;
                                }
                                iv.setImageBitmap(b);
                                hideLoading();
                                showCapturePreviewDialog(dialog_layout, path);
                                /*Intent intent = new Intent();
                                intent.putExtra("path", file.getAbsolutePath());
                                intent.setClass(getActivity(), PreviewActivity.class);
                                getActivity().startActivity(intent);*/
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                try {
                                    if (fos != null) fos.close();
                                    if (fos1 != null) fos1.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
//                }
//            }
//        });

    }

    public void card_capture() {
//        if(dialog != null) dialog.dismiss();
        loading(getString(R.string.capturing));
        releaseCamera();
        if (mCamera1 == null) {
            chooseCamera(CARD_CAMEAR);
            mCamera1 = getCamera();
        }
        this.toast("mCamera1 not null");
        Camera.Parameters param = mCamera1.getParameters();
        param.setPictureFormat(ImageFormat.JPEG);
        param.setPreviewSize(800, 400);
//        param.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
//        mCamera.autoFocus(new Camera.AutoFocusCallback() {
//            @Override
//            public void onAutoFocus(boolean success, Camera camera) {
//                if (success) {
        mCamera1.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

                String filename = UUID.randomUUID().toString() + ".jpg";
                Log.v("YYX",filename);
                Log.v("YYX","=================="+data.length);
                String directory = Environment.getExternalStorageDirectory() + STORE_TAKE_PICTURE_PATH;
                String path = directory + filename;
                File dir = new File(directory);
                File file = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                FileOutputStream fos = null, fos1 = null;
                try {
                    fos = new FileOutputStream(file);
                    fos.write(data);
                    LayoutInflater factory = LayoutInflater.from(getActivity());
                    dialog_layout = factory.inflate(R.layout.avatar_preview, null);
                    Bitmap b = BitmapFactory.decodeFile(file.getAbsolutePath());
//                                Matrix matrix = new Matrix();
//                                matrix.setRotate(180);
//                                Bitmap bitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
                    ImageView iv = (ImageView) dialog_layout.findViewById(R.id.mAvatar);
                    fos1 = new FileOutputStream(file);
                    b.compress(Bitmap.CompressFormat.JPEG, 90, fos1);
                    fos.flush();
                    if(cameraId == AVATAR_CAMERA){
                        cameraTakeAvatarPath = path;
                    }else{
                        idCardAvatarPath = path;
                    }
                    iv.setImageBitmap(b);
                    hideLoading();
                    showCapturePreviewDialog(dialog_layout, path);
                                /*Intent intent = new Intent();
                                intent.putExtra("path", file.getAbsolutePath());
                                intent.setClass(getActivity(), PreviewActivity.class);
                                getActivity().startActivity(intent);*/
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (fos != null) fos.close();
                        if (fos1 != null) fos1.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
//                }
//            }
//        });

    }

    public Camera getCamera() {
        Camera camera;
        try {
            camera = Camera.open(this.cameraId);//0代表前置摄像头(扫描用)，1代表后置摄像头（拍照用）
        } catch (Exception e) {
            camera = null;
            Log.v("YYX","open camera1 fail");
            e.printStackTrace();
        }
        return camera;
    }

    public void setStartPreview(Camera camera, SurfaceHolder holder) {

        if (camera != null) {
            try {
                camera.setPreviewDisplay(holder);
//                camera.setDisplayOrientation(90);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    public void releaseCamera1(){
        if (mCamera1 != null) {
            mCamera1.release();
            mCamera1 = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setStartPreview(mCamera, mHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if(mCamera !=null){
            mCamera.stopPreview();
            setStartPreview(mCamera, mHolder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }

    @Override
    public void onClick(View v) {
        this.visit_time = 0;
        switch (v.getId()) {
            case R.id.read_id_card:
                loading(getString(R.string.read_card));
                readCard();
                break;
            case R.id.btn_capture:
                this.chooseCamera(AVATAR_CAMERA);
                this.capture();
                break;
            case R.id.btn_card_capture:
                this.chooseCamera(CARD_CAMEAR);
                this.card_capture();
                break;
            case R.id.clear_register_btn:
//                addVisitorLog();
//                showRecentVisitLog();
                this.clear();
                break;
            case R.id.visitor_register_btn:
                this.createBarCodeAndSetAvatar();
                if(checkInput()==false)return;
                Log.v("YYX",valid_date.getText().toString().trim()+"--"+isManualInput());
                if (    (idCardAvatarPath != null
                        && idCardAvatarPic != null
                        && cameraTakeAvatarPath!=null)
                        || isManualInput()//以是否有证件有效期为手动填写的依据
                        ) {
                    if(barCodeString!=null){
                        print();
                    }else{
                        this.toast(getString(R.string.barcode_not_exits));
                    }
                }else{
                    Log.v("YYX","in toast");
                    this.toast(getString(R.string.please_put_card));
                }
                break;
            case R.id.print_preview_btn:
                this.createBarCodeAndSetAvatar();
                if(checkInput()==false)return;
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View print_preview_view = inflater.inflate(R.layout.print_preview, null);
                ImageView print_avatar = (ImageView) print_preview_view.findViewById(R.id.print_avatar);
                ImageView print_barcode = (ImageView) print_preview_view.findViewById(R.id.bar_code);

                TextView preview_visit_reason  = (TextView) print_preview_view.findViewById(R.id.preview_visit_reason);
                preview_visit_reason.setText(visit_reason.getSelectedItem().toString());
                TextView preview_visitor_count  = (TextView) print_preview_view.findViewById(R.id.preview_visitor_count);
                preview_visitor_count.setText(visitorCount.getText().toString());
                TextView preview_visitor_name  = (TextView) print_preview_view.findViewById(R.id.preview_visitor_name);
                preview_visitor_name.setText(name.getText().toString());
                TextView preview_visitor_take  = (TextView) print_preview_view.findViewById(R.id.preview_visit_take);
                preview_visitor_take.setText(visitorTake.getText().toString());
                TextView preview_car_num  = (TextView) print_preview_view.findViewById(R.id.preview_car_num);
                preview_car_num.setText(visitorCarNum.getText().toString());
                TextView preview_visit_time  = (TextView) print_preview_view.findViewById(R.id.preview_visit_time);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                if(this.visit_time == 0){
                    visit_time = new Date().getTime();
                }
                preview_visit_time.setText(sdf.format(visit_time));
                TextView preview_visited_dept  = (TextView) print_preview_view.findViewById(R.id.preview_visited_dept);
                preview_visited_dept.setText(be_visited_dept.getSelectedItem().toString());
                TextView preview_visited_name  = (TextView) print_preview_view.findViewById(R.id.preview_visited_name);
                preview_visited_name.setText(parseName(be_visited_name.getText().toString()));

                print_barcode.setImageBitmap(barCodePic);
                if (idCardAvatarPath != null && idCardAvatarPic != null) {
                    print_avatar.setImageBitmap(idCardAvatarPic);
                    showPrintPreviewDialog(print_preview_view);
                }else{
                    if(isManualInput()){

                    }else{
                        this.toast(getString(R.string.please_put_card));
                    }
                }
                break;

        }
    }

    private void readCard() {

        idCardAvatarPic=null;
        idCardAvatarPath = null;
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
//        pkName="/data/data/"+pkName+"/libs/armeabi/libwlt2bmp.so";
        try {
            retval = iDCardDevice.readBaseMsg(port,soPath,bBmpFile, bname, bsex, bnation, bbirth, baddress, bIDNo, bDepartment,
                    bEffectDate, bExpireDate,bpErrMsg);


            Log.v("YYX",pkName);
            if (retval < 0) {
//                clear();
                avatar.setImageResource(R.mipmap.photo);
                this.toast("未能成功识别，请重新放入身份证" + new String(bpErrMsg, "Unicode"));
            } else {

//                -----------------------------
                /*if (bBmpFile ==null){
                    return;
                }
                try {
                    Log.v("YYX","k-----"+wltPath);
                    Log.v("YYX","k1-----"+bmpPath);
                    File wltFile = new File(wltPath);
                    FileOutputStream fos = new FileOutputStream(wltFile);
                    fos.write(bBmpFile);
                    fos.close();
                    DecodeWlt dw = new DecodeWlt();
                    int result = dw.Wlt2Bmp(wltPath, bmpPath);

                    if (result==1) {
                        File f = new File(bmpPath);
                        if (f.exists())
                            avatar.setImageBitmap(BitmapFactory.decodeFile(bmpPath));

                    } else {
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }*/
                //end test 2016-3-7 ------------------------------

                int []colors = iDCardDevice.convertByteToColor(bBmpFile);
                Bitmap bm = Bitmap.createBitmap(colors, 102, 126, Config.ARGB_8888);//102 126
                avatar.setScaleType(ImageView.ScaleType.MATRIX);
                avatar.setImageBitmap(bm);

                this.name.setText(new String(bname, "Unicode"));
                String sexStr = new String(bsex, "Unicode");
                this.setSex(sexStr.trim());
//                this.sex.setText(new String(bsex, "Unicode"));
                ethnic.setText(new String(bnation, "Unicode"));
                birthday.setText(new String(bbirth, "Unicode"));
                this.address.setText(new String(baddress, "Unicode"));
                id_number.setText(new String(bIDNo, "Unicode"));
                police.setText(new String(bDepartment, "Unicode"));
                valid_date.setText(new String(bEffectDate, "Unicode") + "-" + new String(bExpireDate, "Unicode"));

                // Bitmap bm1=Bitmap.createScaledBitmap(bm, (int)(102*1),(int)(126*1), false); //这里你可以自定义它的大小
                /*ImageView imageView = new ImageView(getActivity());
                imageView.setScaleType(ImageView.ScaleType.MATRIX);
                imageView.setImageBitmap(bm);*/
                File dir = new File(Environment.getExternalStorageDirectory() + STROE_IDCARD_AVATAR_PATH);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                idCardAvatarPath = Environment.getExternalStorageDirectory() + STROE_IDCARD_AVATAR_PATH + UUID.randomUUID() + ".jpg";
                idCardAvatarPic = bm;
                try {
                    FileOutputStream writeIdCardAvatar = null;
                    writeIdCardAvatar = new FileOutputStream(new File(idCardAvatarPath));
                    bm.compress(Bitmap.CompressFormat.JPEG, 90, writeIdCardAvatar);
                    if(writeIdCardAvatar!=null){
                        writeIdCardAvatar.flush();
                        writeIdCardAvatar.close();
                    }
                } catch (FileNotFoundException e) {
                    Log.v("YYX","write err1");
                    idCardAvatarPath = null;
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.v("YYX","write err1");
                    idCardAvatarPath = null;
                    e.printStackTrace();
                }

                showRecentVisitLog();
            }

        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        hideLoading();
    }

    /*private SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException {
        if (mSerialPort == null) {
            mSerialPort = new SerialPort(new File("/dev/ttySAC1"), 115200, 0);
            mSerialPort.setMaxRFByte((byte) 0x50);
        }
        return mSerialPort;
    }*/

    private void showCapturePreviewDialog(View view, final String filepath) {
        String title = "拍照头像预览" ;
        if(cameraId == CARD_CAMEAR){
            title = "证件图像预览";
            tmpCamera = mCamera1;
        }else{
            tmpCamera = mCamera;
        }
        capturePreviewDialog = new AlertDialog.Builder(getActivity())
                .setTitle(title).setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "保存成功", Toast.LENGTH_LONG).show();
                        setStartPreview(tmpCamera,mHolder);
                    }
                }).setNegativeButton("重拍", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "重拍", Toast.LENGTH_LONG).show();
                        File file = new File(filepath);

                        if (file.exists() && file.isFile()) file.delete();
                        dialog.dismiss();

                        if(cameraId == CARD_CAMEAR){
                            card_capture();
                        }else{
                            capture();
                        }

                    }
                }).create();
        capturePreviewDialog.show();
    }


    private void initPrinter() {

        String path =   "/dev/ttyS4";
        int baudrate = 9600;//Integer.decode(sp.getString("BAUDRATE", "-1"));

        try {
            mSerialPort = new SerialPort(new File(path), baudrate, 0);
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();

			/* Create a receiving thread */
            mReadThread = new ReadThread();
            mReadThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initNewPrinter(){
        handler = new MyHandler();
        PowerManager pm = (PowerManager)getActivity().getApplicationContext().getSystemService(Context.POWER_SERVICE);
        lock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, TAG);

        iDCardDevice = new publicSecurityIDCardLib();
        HdxUtil.SetIDCARDPower(1);
        HdxUtil.SwitchSerialFunction(HdxUtil.SERIAL_FUNCTION_IDCARD);
    }


    private void print() {

        if(cameraTakeAvatarPath!= null && idCardAvatarPath!= null && barCodeString!=null){
            if(!addVisitorLog()){
                toast("登记失败，请重试！");
                return;
            }
        }else{
            if(isManualInput()){
                if(address.getText().toString().trim().equals("")){
                    toast("请输入访客住址");
                    return;
                }
                if(!addVisitorLog()){
                    toast("登记失败，请重试！");
                    return;
                }
            }else{
                toast(getString(R.string.please_put_card));
            }
        }

        if(be_visited_name.getText().toString().trim().equals("")){
            toast("请选择被访部门和被访的人员姓名！");
            return;
        }
        initPrinter();
        ArrayList<String> head = new ArrayList<String>();
        try {
            mOutputStream.write("           ".getBytes("cp936"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        head.add("访客单");
        sendCommand(0x1b, 0x21, 0x10);
        sendCommand(0x1b, 0x21, 0x20); //double width
        sendCharacterDemo(head);
        sendCommand(0x1b, 0x21, 0x00); //cancel double height
        sendCommand(0x1D, 0x21, 0x00); //cancel double width
        if (mSerialPort != null) {
//            toast(getPrintText());
//            if (idCardAvatarPic != null) {
                /*new Thread() {

                    public void run() {
                        File f = new File(idCardAvatarPath);
                        if(f.exists()){*/
                            Log.v("YYX","-------------test==="+idCardAvatarPath);
                            /*idCardAvatarPic = BitmapFactory.decodeFile(idCardAvatarPath);
                            Bitmap b = this.changeImage(idCardAvatarPic);
                            new BmpThread(b,20).start();*/
//                            new BmpThread(barCodePic).start();
                        /*}
                    }
                }.start();*/
            /*} else {
                toast("未找到需要打印的访客身份证头像");
            }*/

            new Thread() {
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    sendCharacterDemo(getPrintTextArray());
                }
            }.start();

            if (barCodePic != null) {
                new Thread() {
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        new BmpThread(barCodePic).start();
                    }
                }.start();
                /*new Thread() {
                    public void run() {
                        try {
                            Thread.sleep(2200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        sendCommand(0x0a);
                        sendCommand(0x0a);
                        sendCommand(0x0a);
                    }
                }.start();*/
            } else {
                toast("生成条码失败，请进入打印预览进行打印");
            }

            new Thread() {
                public void run() {
                    try {
                        Thread.sleep(7000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ArrayList<String> end = new ArrayList<String>();
                    end.add("    ");

                    sendCharacterDemo(end);
                }
            }.start();

        } else {
            toast("打印机(NULL)未准备就绪，请重新打印");
        }


    }

    private void showPrintPreviewDialog(View view) {
        printPreviewDialog = new AlertDialog.Builder(getActivity())
                .setTitle("打印预览").setView(view)
                .setPositiveButton("打印", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        print();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "取消", Toast.LENGTH_LONG).show();
                        printPreviewDialog.dismiss();
                    }
                }).create();
        printPreviewDialog.show();
    }

    private void toast(String text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
    }

    private String getPrintText() {
        String visitorName = this.name.getText().toString();
        String visitor_count = visitorCount.getText().toString();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String visitTime = sdf.format(new Date(this.visit_time));
        String visitedName = parseName(be_visited_name.getText().toString());
        String dept = be_visited_dept.getSelectedItem().toString();
        String visitReason = visit_reason.getSelectedItem().toString();
        String space = "   ";
        String nextLine = "\r\n";

        String text = space + "宾客姓名：" + visitorName + nextLine
                + space + "来访人数：" + visitor_count + nextLine
                + space + "被访部门：" + dept + nextLine
                + space + "被访人员：" + visitedName + nextLine
                + space + "进入时间：" + visitTime + nextLine
                + space + "来访事由：" + visitReason + nextLine
                + space + "被访签名：" + nextLine
                + space + "保安签名：" + nextLine + nextLine;
        return text;
    }

    private ArrayList<String> getPrintTextArray() {
        String visitorName = this.name.getText().toString();
        String visitor_count = visitorCount.getText().toString();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String visitTime = sdf.format(new Date(this.visit_time));
        String visitedName = parseName(be_visited_name.getText().toString());
        String dept = be_visited_dept.getSelectedItem().toString();
        String visitReason = visit_reason.getSelectedItem().toString();
        String visitTake = visitorTake.getText().toString();
        String visitCarNum = visitorCarNum.getText().toString();
        String space = "   ";

        ArrayList<String> a = new ArrayList<String>();
        a.add(space + "宾客姓名：" + visitorName);
        a.add(space + "来访人数：" + visitor_count);
        a.add(space + "被访部门：" + dept);
        a.add(space + "被访人员：" + visitedName);
        a.add(space + "进入时间：" + visitTime);
        a.add(space + "来访事由：" + visitReason);
        a.add(space + "车辆牌号：" + visitCarNum);
        a.add(space + "随身物品：" + visitTake);
        a.add(space + "被访签名：");
        a.add(space + "保安签名：");

        return a;
    }

    private void createBarCodeAndSetAvatar(){
        barCodeString = String.valueOf(new Date().getTime());
        barCodePic = BarcodeCreater.creatBarcode(getActivity(),
                barCodeString, 386, 84, true, 1);// 最后一位参数是条码格式
        if (idCardAvatarPic!=null) {
            idCardAvatarPic = BitmapFactory.decodeFile(idCardAvatarPath);
            VisitorLeaveFragment.avatarBitmap = idCardAvatarPic;
        } else {
            if(isManualInput()){

            }else{
                this.toast(getString(R.string.please_put_card));
                readCard();
            }
        }
    }

    public void clear(){
        visit_time = 0;
        idCardAvatarPath = null;
        idCardAvatarPic = null;
        barCodePic = null;
        barCodeString = null;

        visitedPhone.setText("");
        be_visited_name.setText("");
        visitorCount.setText("1");
        visitorPhone.setText("");
        visitorTake.setText("");
        visitorCarNum.setText("");
        visitedPos.setText("");

        name.setText("");
        setSex("");
        ethnic.setText("");
        birthday.setText("");
        address.setText("");
        id_number.setText("");
        police.setText("");
        valid_date.setText("");
        avatar.setImageResource(R.mipmap.photo);

        ArrayList<HashMap<String,Object>> list = new ArrayList<HashMap<String, Object>>();
        if(list.size()>0){
            list.clear();
        }
        SimpleAdapter simpleAdapter = new SimpleAdapter(getActivity(), list,R.layout.visit_log_item,
                new String[]{"_id", "visitor_name","visit_reason","visited_dept_name","visited_username","visit_time","leave_time","visit_status"},
                new int[]{R.id.item_log_id,R.id.item_visitor_name, R.id.item_visit_reason,R.id.item_visited_dept,R.id.item_visited_name,R.id.item_visit_time,R.id.item_leave_time,R.id.item_visit_status});
        recent_visit_log_listview.setAdapter(simpleAdapter);

    }

    public void loading(String text){
        if(progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
        }
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//this.toast("in loading");
        progressDialog.setTitle("提示");
        if(text == null){
            text = "正在加载...";
        }
        progressDialog.setMessage(text);
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(true);
        progressDialog.setButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialog.cancel();
            }
        });
        progressDialog.show();
    }

    public void hideLoading(){
        if(progressDialog !=null){
            progressDialog.hide();
        }
    }

    public boolean addVisitorLog(){

        if(visit_time == 0){
            this.visit_time = new Date().getTime();
        }
        ContentValues cv = new ContentValues();
        cv.put("barcode",barCodeString);
        cv.put("visit_status",0);
        cv.put("leave_time",0);
        cv.put("visit_time",visit_time);
        cv.put("duty_username",duty_person.getSelectedItem().toString());
        cv.put("duty_user_id",0);
        cv.put("idcard_deadline",valid_date.getText().toString());
        cv.put("idcard_police",police.getText().toString());
        cv.put("visitor_count",visitorCount.getText().toString());
        cv.put("visitor_idno",id_number.getText().toString());
        cv.put("visitor_address", address.getText().toString());
        cv.put("visitor_birthday", birthday.getText().toString());
        cv.put("visitor_ethnic", ethnic.getText().toString());
        cv.put("visitor_phone", visitorPhone.getText().toString());
        cv.put("idcard_avatar", idCardAvatarPath);
        cv.put("visitor_avatar", cameraTakeAvatarPath);

        if(sex.getSelectedItemPosition() == 0) {
            cv.put("visitor_sex", 1);
        }else{
            cv.put("visitor_sex", 2);
        }
        cv.put("visitor_name", name.getText().toString());
        cv.put("visited_user_phone", visitedPhone.getText().toString());
        cv.put("visited_user_position", visitedPos.getText().toString());
        cv.put("visitor_car_num",visitorCarNum.getText().toString());
        cv.put("visitor_take",visitorTake.getText().toString());
        cv.put("certificate_type", certificate_type.getSelectedItem().toString());
        if(visitedFemale.isChecked()){
            cv.put("visited_sex", 2);
        }else{
            cv.put("visited_sex",1);
        }
        cv.put("visited_dept_name", be_visited_dept.getSelectedItem().toString());
        cv.put("visited_dept_id", 0);
        cv.put("visited_username", parseName(be_visited_name.getText().toString()));
        cv.put("visited_user_id", 0);
        cv.put("reason_id", 0);
        cv.put("visit_reason", visit_reason.getSelectedItem().toString());
        connectDB();
        boolean flag = helper.insert(helper.TABLE_VISIT_LOG,cv) > 0;
        releaseDB();
        return flag;

    }

    private void showRecentVisitLog(){
        Log.v("YYX","in showRecentVisitLog ");
        String idno = id_number.getText().toString();
        Log.v("YYX",idno);
        if(idno!=null && !"".equals(idno)){
//            Cursor cur = helper.getRecentVisitLogByIdNumber(idno);
        Cursor cur = helper.fetchAllVisitLog("visitor_idno='" + idno + "'", 0, 5);
//        Cursor cur = helper.fetchAllVisitLog(0, 3);
        connectDB();
            Log.v("YYX",cur.getCount()+"");
            if(cur.getCount()>0){
                SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), R.layout.visit_log_item, cur,
                        new String[]{"_id", "visitor_name","visit_reason","visited_dept_name","visited_username","visit_time","leave_time","visit_status"},
                        new int[]{R.id.item_log_id,R.id.item_visitor_name, R.id.item_visit_reason,R.id.item_visited_dept,R.id.item_visited_name,R.id.item_visit_time,R.id.item_leave_time,R.id.item_visit_status});
                //实现列表的显示
                Log.v("YYX", "in setAdapter");
                recent_visit_log_listview.setAdapter(adapter);
            }
        releaseDB();
        }
    }

    private void showDetailDialog(final View view, final int _id) {
        Log.v("YYX","------------showDetailDialog-----------");
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
                            showRecentVisitLog();
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

    private boolean checkInput(){

        String msg = "\r\n";
        boolean flag = true;
        String nextline = "\r\n";

        if(duty_person.getCount() == 0){
            msg +="请先配置值班人员"+nextline;
            flag = false;
        }
        if(be_visited_dept.getCount() == 0){
            msg +="请先配置部门"+nextline;
            flag = false;
        }

        if(visit_reason.getCount() == 0){
            msg +="请先配置来访事由"+nextline;
            flag = false;
        }

        if(be_visited_name.getText().toString().trim().equals("")){
            msg += "请输入被访人的姓名"+nextline;
            flag = false;
        }


        /*if(cameraTakeAvatarPath == null){
            msg += "请给访客拍照"+nextline;
            flag = false;
        }*/

        if(isManualInput()){
            if("".equals(address.getText().toString().trim())){
                msg += "请输入来访人住址" + nextline;
                flag = false;
            }
        }else{
            if(idCardAvatarPath == null){
                msg += getString(R.string.please_put_card) + nextline;
                flag = false;
            }
        }


        if(barCodeString==null){
            msg += "生成条码失败，请重试"+nextline;
            flag = false;
        }

        if(flag == false) {
            toast(msg);
        }
        return flag;

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(hidden){
            clear();
        }
    }
    public static String parseName(String str){
        int p = str.indexOf("-(");
        String uname="";
        if (p != -1) {
            uname = str.substring(0, p);
//            String dept_name = str.substring(p + 2, str.length() - 1);
        }
        return uname;
    }


    public void PrintBmp(int startx,Bitmap bitmap) throws IOException
    {
        byte[] start1 = { 0x0d,0x0a};
        byte[] start2 = { 0x1D,0x76,0x30,0x30,0x00,0x00,0x01,0x00};

        int width = bitmap.getWidth()+startx;
        int height = bitmap.getHeight();

        if(width>384)
            width=384;
        int tmp = (width+7)/8;
        byte[] data = new byte[tmp];
        byte xL = (byte) (tmp % 256);
        byte xH = (byte) (tmp / 256);
        start2[4]=xL;
        start2[5]=xH;
        start2[6]=(byte) (height % 256);
        start2[7]=(byte) (height / 256);
        Log.v("YYX","width="+width+",height="+height+",height="+height);
        mOutputStream.write(start2);
        for (int i = 0; i < height; i++) {


            for (int x = 0; x < tmp; x++)
                data[x]=0;
            for (int x = startx; x < width; x++) {
                int pixel = bitmap.getPixel(x-startx, i);
                if (Color.red(pixel) == 0 || Color.green(pixel) == 0 || Color.blue(pixel) == 0) {
                    // 高位在左，所以使用128 右移
                    data[x/8] += 128>>(x%8);//(byte) (128 >> (y % 8));
                }
            }
            Log.e(TAG,"printer_status="+printer_status);
			/* while((printer_status&0x13)!=0){
				 try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
				}
			 }*/
            mOutputStream.write(data);
			 /*try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
			}*/
        }
    }

    private class WriteThread extends Thread
    {
        ArrayList<String> arr ;
        public WriteThread(ArrayList<String> str)
        {
            arr = str;
        }


        public void run() {
            super.run();

            lock.acquire();
            try
            {

                //ConsoleActivity.this.sleep(500);
                sendCharacterDemo(arr);
                sendCommand(0x0a);
//	sendCommand(0x1d,0x56,0x42,0x20); //
                //sendCommand(0x1d,0x56,0x30);
                //ConsoleActivity.this.sleep(4000);
//	ConsoleActivity.this.sleep(2000);
            }finally{
                lock.release();

            }

        }
    }

    private class BmpThread extends Thread
    {
        private Bitmap bm=null;
        private int alignLeft = 10;
        public BmpThread(Bitmap bm)
        {
            this.bm = bm;
        }

        public BmpThread(Bitmap bm,int align)
        {
            this.bm = bm;
            this.alignLeft = align;
        }


        public void run() {
            super.run();

            //ConsoleActivity.this.sleep(500);
            lock.acquire();
            try {
                if(bm!=null){
                    PrintBmp(alignLeft,bm);
                    sendCommand(0x0a);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally{
                lock.release();
                //	ConsoleActivity.this.sleep(2000);

            }
        }
    }

    private  void sendCharacterDemo(ArrayList<String> arr) {

        try {
            for(int i=0; i<arr.size();i++){
                String str = arr.get(i).toString();
                Log.v("YYX","========================"+str);
                mOutputStream.write(str.getBytes("cp936"));
                sendCommand(0x0a);
            }

        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        sendCommand(0x0a);

    }

    private void sendCommand(int...command) {
        try {
            for(int i = 0; i < command.length; i++){
                mOutputStream.write(command[i]);
                //	Log.e(TAG,"command["+i+"] = "+Integer.toHexString(command[i]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
///	sleep(1);
    }

    protected void onDataReceived(final byte[] buffer, final int size,final int n) {
        printer_status = buffer[0];
//		Log.e(TAG, "onDataReceived= "+printer_status);
/*		runOnUiThread(new Runnable() {

			public void run() {
				Log.e(TAG, "onDataReceived==============");

				StringBuilder sn=new StringBuilder();
				for(int i=0;i<size;i++)
				{
					sn.append(String.format("%02x", buffer[i]));
				}
				TVSerialRx.setText("*recive:"+sn.toString()+"\n");
			}
		});*/
    }


    public int[] convertByteToColor(byte[] data){
        int size = data.length;
        if (size == 0){
            return null;
        }

        int arg = 0;
        if (size % 3 != 0){
            arg = 1;
        }

        int []color = new int[size / 3 + arg];
        int red, green, blue;

        if (arg == 0){
            for(int i = 0; i < color.length; ++i){
                red = convertByteToInt(data[i * 3]);
                green = convertByteToInt(data[i * 3 + 1]);
                blue = convertByteToInt(data[i * 3 + 2]);

                color[i] = (red << 16) | (green << 8) | blue | 0xFF000000;
            }
        }else{
            for(int i = 0; i < color.length - 1; ++i){
                red = convertByteToInt(data[i * 3]);
                green = convertByteToInt(data[i * 3 + 1]);
                blue = convertByteToInt(data[i * 3 + 2]);
                color[i] = (red << 16) | (green << 8) | blue | 0xFF000000;
            }

            color[color.length - 1] = 0xFF000000;
        }

        return color;
    }


    public int convertByteToInt(byte data){

        int heightBit = (int) ((data>>4) & 0x0F);
        int lowBit = (int) (0x0F & data);

        return heightBit * 16 + lowBit;
    }

    private Bitmap changeImage(Bitmap mBitmap) {

        int width = mBitmap.getWidth();
        int height = mBitmap.getHeight();

        Bitmap grayImg = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(grayImg);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter( colorMatrix);
        paint.setColorFilter(colorMatrixFilter);
        canvas.drawBitmap(mBitmap, 0, 0, paint);
        return grayImg;
    }


    private void setSex(String sex){

        if("男".equals(sex)){
            this.sex.setSelection(0);
        }else{
            this.sex.setSelection(1);
        }

    }

    private boolean isManualInput(){
        return "".equals(valid_date.getText().toString().trim());
    }




}

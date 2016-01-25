package com.niceguy.app.visitor;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.synjones.bluetooth.DecodeWlt;
import com.synjones.sdt.IDCard;
import com.synjones.sdt.SerialPort;
import com.zkc.helper.printer.PrintService;
import com.zkc.helper.printer.PrinterClass;
import com.zkc.pc700.helper.BarcodeCreater;
import com.zkc.pc700.helper.PrinterClassSerialPort;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Date;
import java.util.UUID;


/**
 * Created by qiumeilin on 2016/1/9.
 */
public class VisitorRegisterFragment extends Fragment implements SurfaceHolder.Callback, OnClickListener {

    private static final String TAG = "PRINT";
    private static final String STROE_IDCARD_AVATAR_PATH = "/sicheng/idcard_avatar/";
    private static final String STORE_TAKE_PICTURE_PATH = "/sicheng/take_avatar/";
    private PrinterClassSerialPort printerClass = null;
    private Camera mCamera;
    private SurfaceView cameraPreview;
    private SurfaceHolder mHolder;

    protected SerialPort mSerialPort;
    private IDCard idcard = null;
    private Spinner visit_reason;
    private Spinner be_visited_dept, duty_person;
    private Button read_id_card, capture, clear_register, print_preview, visitor_register;
    private ArrayAdapter<String> adapter;
    private TextView name, sex, address, birthday, birthplace, police, valid_date, id_number, ethnic;
    private ImageView avatar;
    private String wltPath, bmpPath;
    private View dialog_layout;

    private AlertDialog capturePreviewDialog;
    private AlertDialog printPreviewDialog;
    private String cameraTakeAvatarPath = null, idCardAvatarPath = null;
    private Bitmap barCodePic = null, idCardAvatarPic = null;
    private String barCodeString = null;

    private EditText visitorCount = null;
    private EditText visitorPhone = null;
    private EditText visitedPhone = null;
    private EditText visitedName = null;
    private EditText visitedPos = null;

    public ProgressDialog progressDialog = null;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.visitor_register_tab, container, false);

        initViews(view);
        initEvents();

        /**
         * 读卡
         */
        try {
            mSerialPort = getSerialPort();
        } catch (SecurityException se) {
        } catch (IOException ioe) {
        } catch (InvalidParameterException ipe) {
        }
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

        cameraPreview = (SurfaceView) view.findViewById(R.id.camera_preview);
        mHolder = cameraPreview.getHolder();
        visit_reason = (Spinner) view.findViewById(R.id.visit_reason);
        be_visited_dept = (Spinner) view.findViewById(R.id.be_visited_dept);
        duty_person = (Spinner) view.findViewById(R.id.duty_person);
        read_id_card = (Button) view.findViewById(R.id.read_id_card);
        capture = (Button) view.findViewById(R.id.btn_capture);
        mHolder.addCallback(this);

        String[] visit_reasons = new String[]{"送货", "办事", "找人"};
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, visit_reasons);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        visit_reason.setAdapter(adapter);

        String[] dept = new String[]{"财务部", "行政部", "领导办公室"};
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, dept);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        be_visited_dept.setAdapter(adapter);

        String[] duty_persons = new String[]{"张三", "李四", "王五"};
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, duty_persons);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        duty_person.setAdapter(adapter);

        avatar = (ImageView) view.findViewById(R.id.avatar_on_id_card);
        name = (TextView) view.findViewById(R.id.name_on_id_card);
        sex = (TextView) view.findViewById(R.id.sex_on_id_card);
        birthday = (TextView) view.findViewById(R.id.birthday_on_id_card);
//        birthplace = (TextView) view.findViewById(R.id.birthplace_on_id_card);
        address = (TextView) view.findViewById(R.id.address_on_id_card);
        police = (TextView) view.findViewById(R.id.police_on_id_card);
        valid_date = (TextView) view.findViewById(R.id.valid_date_on_id_card);
        id_number = (TextView) view.findViewById(R.id.number_on_id_card);
        ethnic = (TextView) view.findViewById(R.id.ethnic_on_id_card);

        clear_register = (Button) view.findViewById(R.id.clear_register_btn);
        print_preview = (Button) view.findViewById(R.id.print_preview_btn);
        visitor_register = (Button) view.findViewById(R.id.visitor_register_btn);
        clear_register = (Button) view.findViewById(R.id.clear_register_btn);

        visitorCount = (EditText) view.findViewById(R.id.visitor_num);
        visitorPhone = (EditText) view.findViewById(R.id.visitor_tel);
        visitedPhone = (EditText) view.findViewById(R.id.be_visited_tel);
        visitedName = (EditText) view.findViewById(R.id.be_visited_name);
        visitedPos = (EditText) view.findViewById(R.id.be_visited_pos);

    }

    private void initEvents() {
        read_id_card.setOnClickListener(this);
        capture.setOnClickListener(this);
        visitor_register.setOnClickListener(this);
        print_preview.setOnClickListener(this);
        clear_register.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCamera == null) {
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
        super.onDestroyView();
        if (progressDialog != null)progressDialog.dismiss();
        if (printPreviewDialog != null) printPreviewDialog.dismiss();
        if (capturePreviewDialog != null) capturePreviewDialog.dismiss();
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseCamera();
    }

    public void capture() {
//        if(dialog != null) dialog.dismiss();
        loading(getString(R.string.capturing));
        Camera.Parameters param = mCamera.getParameters();
        param.setPictureFormat(ImageFormat.JPEG);
        param.setPreviewSize(800, 400);
        param.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
                    mCamera.takePicture(null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {

                            String filename = UUID.randomUUID().toString() + ".jpg";
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
                                Matrix matrix = new Matrix();
                                matrix.setRotate(180);
                                Bitmap bitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
                                ImageView iv = (ImageView) dialog_layout.findViewById(R.id.mAvatar);
                                fos1 = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos1);
                                fos.flush();
                                cameraTakeAvatarPath = path;
                                iv.setImageBitmap(bitmap);
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
                }
            }
        });

    }

    public Camera getCamera() {
        Camera camera;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            camera = null;
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

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setStartPreview(mCamera, mHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mCamera.stopPreview();
        setStartPreview(mCamera, mHolder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.read_id_card:
                loading(getString(R.string.read_card));
                readCard();
                break;
            case R.id.btn_capture:
                this.capture();
                break;
            case R.id.clear_register_btn:
                this.clear();
                break;
            case R.id.visitor_register_btn:
                this.createBarCodeAndSetAvatar();
                if (idCardAvatarPath != null && idCardAvatarPic != null) {
                    print();
                }else{
                    this.toast(getString(R.string.please_put_card));
                }
                break;
            case R.id.print_preview_btn:
                this.createBarCodeAndSetAvatar();
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View print_preview_view = inflater.inflate(R.layout.print_preview, null);
                ImageView print_avatar = (ImageView) print_preview_view.findViewById(R.id.print_avatar);
                ImageView print_barcode = (ImageView) print_preview_view.findViewById(R.id.bar_code);
                print_barcode.setImageBitmap(barCodePic);
                if (idCardAvatarPath != null && idCardAvatarPic != null) {
                    print_avatar.setImageBitmap(idCardAvatarPic);
                    showPrintPreviewDialog(print_preview_view);
                }else{
                    this.toast(getString(R.string.please_put_card));
                }
                break;

        }
    }

    private void readCard() {

        idCardAvatarPic=null;
        idCardAvatarPath = null;
        idcard = mSerialPort.getIDCard();
        if (idcard != null) {
            name.setText(idcard.getName());
            sex.setText(idcard.getSex());
            ethnic.setText(idcard.getNation());
            birthday.setText(idcard.getBirthday().substring(0, 4) + "-"
                    + idcard.getBirthday().substring(4, 6) + "-"
                    + idcard.getBirthday().substring(6, 8));
            address.setText(idcard.getAddress());
            id_number.setText(idcard.getIDCardNo());
            police.setText(idcard.getGrantDept());
            valid_date.setText(idcard.getUserLifeBegin() + "-" + idcard.getUserLifeEnd());
//            tv = (TextView) findViewById(R.id.textViewStatus);
//            tv.setText(getString(R.string.sdtstatus));
            if (idcard.getWlt() == null) {
                return;
            }
            try {
                File wltFile = new File(wltPath);
                FileOutputStream fos = new FileOutputStream(wltFile);
                fos.write(idcard.getWlt());
                fos.close();
                DecodeWlt dw = new DecodeWlt();
                int result = dw.Wlt2Bmp(wltPath, bmpPath);

                if (result == 1) {
                    File f = new File(bmpPath);
                    if (f.exists()) {
                        Bitmap readedBitmap = BitmapFactory.decodeFile(bmpPath);
                        avatar.setImageBitmap(readedBitmap);
                        String filename = UUID.randomUUID().toString() + ".jpg";
                        String directory = Environment.getExternalStorageDirectory() + STROE_IDCARD_AVATAR_PATH;
                        String path = directory + filename;
                        File dir = new File(directory);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        FileOutputStream fos1 = new FileOutputStream(path);
                        readedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos1);
                        fos1.flush();
                        fos1.close();
                        File file = new File(path);
                        if (file.exists()) {
                            this.idCardAvatarPath = file.getAbsolutePath();
                        }
                    } else {
                        avatar.setImageResource(R.mipmap.photo);
                    }
                } else {
                    avatar.setImageResource(R.mipmap.photo);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } else {
            clear();
        }
        hideLoading();
    }

    private SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException {
        if (mSerialPort == null) {
            mSerialPort = new SerialPort(new File("/dev/ttySAC1"), 115200, 0);
            mSerialPort.setMaxRFByte((byte) 0x50);
        }

        return mSerialPort;
    }

    private void showCapturePreviewDialog(View view, final String filepath) {

        capturePreviewDialog = new AlertDialog.Builder(getActivity())
                .setTitle("头像预览").setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "保存成功", Toast.LENGTH_LONG).show();
                        setStartPreview(mCamera,mHolder);
                    }
                }).setNegativeButton("重拍", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "重拍", Toast.LENGTH_LONG).show();
                        File file = new File(filepath);

                        if (file.exists() && file.isFile()) file.delete();
                        dialog.dismiss();
                        capture();

                    }
                }).create();
        capturePreviewDialog.show();
    }

    private void initPrinter() {
        Handler mhandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case PrinterClass.MESSAGE_READ:
                        byte[] readBuf = (byte[]) msg.obj;
                        Log.i(TAG, "readBuf:" + readBuf[0]);
                        if (readBuf[0] == 0x13) {
                            PrintService.isFUll = true;
                        } else if (readBuf[0] == 0x11) {
                            PrintService.isFUll = false;
                        } else {
                            String readMessage = new String(readBuf, 0, msg.arg1);
                            if (readMessage.contains("800"))// 80mm paper
                            {
                                PrintService.imageWidth = 72;
                                Toast.makeText(getActivity(), "80mm",
                                        Toast.LENGTH_SHORT).show();
                            } else if (readMessage.contains("580"))// 58mm paper
                            {
                                PrintService.imageWidth = 48;
                                Toast.makeText(getActivity(), "58mm",
                                        Toast.LENGTH_SHORT).show();
                            } else {

                            }
                        }
                        break;
                    case PrinterClass.MESSAGE_STATE_CHANGE:// 蓝牙连接状
                        switch (msg.arg1) {
                            case PrinterClass.STATE_CONNECTED:// 已经连接
                                break;
                            case PrinterClass.STATE_CONNECTING:// 正在连接
                                Toast.makeText(getActivity(),
                                        "STATE_CONNECTING", Toast.LENGTH_SHORT).show();
                                break;
                            case PrinterClass.STATE_LISTEN:
                            case PrinterClass.STATE_NONE:
                                break;
                            case PrinterClass.SUCCESS_CONNECT:
                                printerClass.write(new byte[]{0x1b, 0x2b});// 检测打印机型号
                                Toast.makeText(getActivity(),
                                        "SUCCESS_CONNECT", Toast.LENGTH_SHORT).show();
                                break;
                            case PrinterClass.FAILED_CONNECT:
                                Toast.makeText(getActivity(),
                                        "FAILED_CONNECT", Toast.LENGTH_SHORT).show();

                                break;
                            case PrinterClass.LOSE_CONNECT:
                                Toast.makeText(getActivity(), "LOSE_CONNECT",
                                        Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case PrinterClass.MESSAGE_WRITE:

                        break;
                }
                super.handleMessage(msg);
            }
        };

        printerClass = new PrinterClassSerialPort(mhandler);
        printerClass.open(getActivity());

    }


    private Handler hanler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    print_preview.setEnabled(true);
                    visitor_register.setEnabled(true);
                    break;

                default:
                    break;
            }
        }
    };

    private void print() {

        initPrinter();
        if (printerClass != null) {
            toast(getPrintText());
            if (idCardAvatarPic != null) {
                printerClass.printImage(idCardAvatarPic);
                Message msgMessage = new Message();
                msgMessage.what = 0;
                hanler.sendMessage(msgMessage);
            } else {
                toast("未找到需要打印的访客身份证头像");
            }

            new Thread() {
                public void run() {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    printerClass.printText(getPrintText());
                }
            }.start();
            if (barCodePic != null) {
                new Thread() {
                    public void run() {
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        printerClass.printImage(barCodePic);
                        Message msgMessage = new Message();
                        msgMessage.what = 0;
                        hanler.sendMessage(msgMessage);

                        printerClass.printText("\r\n\r\n");
                    }
                }.start();
            } else {
                toast("生成条码失败，请进入打印预览进行打印");
            }
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
                        Toast.makeText(getActivity(), "打印成功", Toast.LENGTH_LONG).show();
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
        String visitorName = "张宏伟";
        String visitorCount = "1";
        String visitTime = "2015-10-23 10:00:00";
        String visitedName = "刘启明";
        String visitedDept = "人力资源部";
        String visitReason = "送货";
        String space = "   ";
        String nextLine = "\r\n";

        String text = space + "宾客姓名：" + this.name.getText() + nextLine
                + space + "来访人数：" + visitorCount + nextLine
                + space + "被访部门：" + visitedDept + nextLine
                + space + "被访人员：" + visitedName + nextLine
                + space + "进入时间：" + visitTime + nextLine
                + space + "来访事由：" + visitReason + nextLine
                + space + "被访签名：" + nextLine
                + space + "保安签名：" + nextLine + nextLine;
        return text;
    }

    private void createBarCodeAndSetAvatar(){
        barCodeString = String.valueOf(new Date().getTime());
        barCodePic = BarcodeCreater.creatBarcode(getActivity(),
                barCodeString, 386, 84, true, 1);// 最后一位参数是条码格式
        if (idCardAvatarPath != null) {
            idCardAvatarPic = BitmapFactory.decodeFile(idCardAvatarPath);
            VisitorLeaveFragment.avatarBitmap = idCardAvatarPic;
        } else {
            this.toast(getString(R.string.please_put_card));
            readCard();
        }
    }

    private void clear(){
        idCardAvatarPath = null;
        idCardAvatarPic = null;
        barCodePic = null;
        barCodeString = null;

        visitedPhone.setText("");
        visitedName.setText("");
        visitorCount.setText("1");
        visitorPhone.setText("");
        visitedPos.setText("");

        name.setText("");
        sex.setText("");
        ethnic.setText("");
        birthday.setText("");
        address.setText("");
        id_number.setText("");
        police.setText("");
        valid_date.setText("");
        avatar.setImageResource(R.mipmap.photo);

    }

    public void loading(String text){
        if(progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
        }
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
this.toast("in loading");
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

}

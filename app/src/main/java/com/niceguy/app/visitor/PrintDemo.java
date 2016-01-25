package com.niceguy.app.visitor;

import java.io.UnsupportedEncodingException;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.zkc.helper.printer.BarcodeCreater;
import com.zkc.helper.printer.PrintService;
import com.zkc.helper.printer.PrinterClass;
import com.zkc.pc700.helper.PrinterClassSerialPort;

public class PrintDemo extends Activity {
    PrinterClassSerialPort printerClass = null;
    private static final int REQUEST_EX = 1;
    protected static final String TAG = "PrintDemo";

    private Thread autoprint_Thread;
    boolean isPrint = true;
    int times = 500;// Automatic print time interval

    private Button btnUnicode;// Print by Unicode

    private EditText et_input = null;

    private CheckBox checkBoxAuto = null;
    private Button btnPrint = null;

    private Button btnOpenPic = null;

    private Button btnPrintPic = null;

    private ImageView iv = null;

    private String picPath = "";// 打开图片保存的路径
    private Bitmap btMap = null;// 缓存图片

    private Button btnQrCode = null;
    private Button btnBarCode = null;

    private Button btnWordToPic = null;

    private Spinner spinnerBaudrate = null;

    String thread = "readThread";
    String text = "打印测试打印测试打印测试打印测试打印测试\r\nabcdefghijklmnopqrstuvw\r\n";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.printdemo);


        et_input = (EditText) findViewById(R.id.editText1);
        btnUnicode = (Button) findViewById(R.id.btnUnicode);
        btnPrint = (Button) findViewById(R.id.btnPrint);

        et_input.setText(text);

        btnOpenPic = (Button) findViewById(R.id.btnOpenPic);
        btnPrintPic = (Button) findViewById(R.id.btnPrintPic);

        checkBoxAuto = (CheckBox) findViewById(R.id.checkBoxTimer);
        iv = (ImageView) findViewById(R.id.iv_test);

        btnQrCode = (Button) findViewById(R.id.btnQrCode);
        btnBarCode = (Button) findViewById(R.id.btnBarCode);

        btnWordToPic = (Button) findViewById(R.id.btnWordToPic);

        spinnerBaudrate = (Spinner) findViewById(R.id.spinner1);
        String[] baudrates = getResources().getStringArray(
                R.array.baudrates_name);
        ArrayAdapter<String> _Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, baudrates);
        spinnerBaudrate.setAdapter(_Adapter);

        spinnerBaudrate.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Object str = parent.getItemAtPosition(position);
                if (printerClass.setSerialPortBaudrate(Integer.parseInt((String) str))) {
                    Toast.makeText(PrintDemo.this, "set " + str + " success", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }
        });


        if (btnQrCode != null) {
            btnQrCode.setOnClickListener(new OnClickListener() {

                public void onClick(View v) {
                    String message = "http://www.sznewbest.com";
                    if (message.length() > 0) {
                        try {
                            message = new String(message.getBytes("utf8"));
                        } catch (UnsupportedEncodingException e) {
                            Log.e(TAG, e.getMessage());
                        }
                        btMap = BarcodeCreater.encode2dAsBitmap(message, 384, 384, 2);
                        //BarcodeCreater.saveBitmap2file(btMap, "mypic1.JPEG");
                        iv.setImageBitmap(btMap);

                    }
                }
            });
        }
        if (btnWordToPic != null) {
            btnWordToPic.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    String str = et_input.getText().toString();
                    btMap = Bitmap.createBitmap(384,
                            et_input.getLineCount() * 25, Config.ARGB_8888);
                    Canvas canvas = new Canvas(btMap);
                    canvas.drawColor(Color.WHITE);
                    TextPaint textPaint = new TextPaint();
                    textPaint.setStyle(Paint.Style.FILL);
                    textPaint.setColor(Color.BLACK);
                    textPaint.setTextSize(25.0F);
                    StaticLayout layout = new StaticLayout(str, textPaint,
                            btMap.getWidth(), Alignment.ALIGN_NORMAL,
                            (float) 1.0, (float) 0.0, true);

                    layout.draw(canvas);

                    iv.setImageBitmap(btMap);

                }
            });
        }

        if (btnBarCode != null) {
            btnBarCode.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    String message = "123456789";

                    if (message.getBytes().length > message.length()) {
                        Toast.makeText(
                                PrintDemo.this,
                                PrintDemo.this.getResources().getString(
                                        R.string.str_cannotcreatebar), Toast.LENGTH_LONG)
                                .show();
                        return;
                    }
                    if (message.length() > 0) {

                        btMap = BarcodeCreater.creatBarcode(PrintDemo.this,
                                message, 384, 80, true, 1);// 最后一位参数是条码格式
                        iv.setImageBitmap(btMap);

                    }

                }
            });
            if (btnPrint != null) {
                btnPrint.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        String str = et_input.getText().toString();
                        try {
                            printerClass.printText(str);
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }

                    }
                });
            }
            if (btnUnicode != null) {
                btnUnicode.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String str = et_input.getText().toString();
                        try {
                            printerClass.printUnicode(str);
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                });
            }
            if (btnOpenPic != null) {
                btnOpenPic.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent(
                                Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, REQUEST_EX);
                    }
                });
            }
            if (btnPrintPic != null) {
                btnPrintPic.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        new Thread() {
                            public void run() {
                                if (btMap != null) {
                                    printerClass.printImage(btMap);
                                    Message msgMessage = new Message();
                                    msgMessage.what = 0;
                                    hanler.sendMessage(msgMessage);
                                }
                            }
                        }.start();

                        return;

                    }
                });
            }
        }
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
                                Toast.makeText(getApplicationContext(), "80mm",
                                        Toast.LENGTH_SHORT).show();
                            } else if (readMessage.contains("580"))// 58mm paper
                            {
                                PrintService.imageWidth = 48;
                                Toast.makeText(getApplicationContext(), "58mm",
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
                                Toast.makeText(getApplicationContext(),
                                        "STATE_CONNECTING", Toast.LENGTH_SHORT).show();
                                break;
                            case PrinterClass.STATE_LISTEN:
                            case PrinterClass.STATE_NONE:
                                break;
                            case PrinterClass.SUCCESS_CONNECT:
                                printerClass.write(new byte[]{0x1b, 0x2b});// 检测打印机型号
                                Toast.makeText(getApplicationContext(),
                                        "SUCCESS_CONNECT", Toast.LENGTH_SHORT).show();
                                break;
                            case PrinterClass.FAILED_CONNECT:
                                Toast.makeText(getApplicationContext(),
                                        "FAILED_CONNECT", Toast.LENGTH_SHORT).show();

                                break;
                            case PrinterClass.LOSE_CONNECT:
                                Toast.makeText(getApplicationContext(), "LOSE_CONNECT",
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
        printerClass.open(this);

        // Auto Print
        autoprint_Thread = new Thread() {
            public void run() {
                while (isPrint) {
                    if (checkBoxAuto.isChecked()) {
                        String message = et_input.getText().toString();
                        printerClass.printText(message);
                        try {
                            Thread.sleep(times);
                        } catch (InterruptedException e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                }
            }
        };
        autoprint_Thread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        printerClass.close(this);
    }

    private Handler hanler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    btnPrintPic.setEnabled(true);
                    btnOpenPic.setEnabled(true);
                    btnBarCode.setEnabled(true);
                    btnWordToPic.setEnabled(true);
                    btnQrCode.setEnabled(true);
                    btnPrint.setEnabled(true);
                    break;

                default:
                    break;
            }
        }
    };

    public static Bitmap resizeImage(Bitmap bitmap, int w, int h) {
        Bitmap BitmapOrg = bitmap;
        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        int newWidth = w;
        int newHeight = h;

        if (width >= newWidth) {
            float scaleWidth = ((float) newWidth) / width;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleWidth);
            Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
                    height, matrix, true);
            return resizedBitmap;
        } else {

            Bitmap bitmap2 = Bitmap.createBitmap(newWidth, newHeight,
                    bitmap.getConfig());
            Canvas canvas = new Canvas(bitmap2);
            canvas.drawColor(Color.WHITE);

            canvas.drawBitmap(BitmapOrg, (newWidth - width) / 2, 0, null);

            return bitmap2;

        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EX && resultCode == RESULT_OK
                && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            picPath = picturePath;
            iv.setImageURI(selectedImage);
            btMap = BitmapFactory.decodeFile(picPath);
            if (btMap.getHeight() > 384) {
                btMap = BitmapFactory.decodeFile(picPath);
                iv.setImageBitmap(resizeImage(btMap, 384, 384));

            }
            cursor.close();
        }

    }

    static byte[] string2Unicode(String s) {
        try {
            byte[] bytes = s.getBytes("unicode");
            byte[] bt = new byte[bytes.length - 2];
            for (int i = 2, j = 0; i < bytes.length - 1; i += 2, j += 2) {
                bt[j] = (byte) (bytes[i + 1] & 0xff);
                bt[j + 1] = (byte) (bytes[i] & 0xff);
            }
            return bt;
        } catch (Exception e) {
            try {
                byte[] bt = s.getBytes("GBK");
                return bt;
            } catch (UnsupportedEncodingException e1) {
                Log.e(TAG, e.getMessage());
                return null;
            }
        }
    }

}

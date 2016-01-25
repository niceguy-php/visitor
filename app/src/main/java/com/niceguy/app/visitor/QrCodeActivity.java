package com.niceguy.app.visitor;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class QrCodeActivity extends AppCompatActivity {
	private final static int SCANNIN_GREQUEST_CODE = 1;

	public static Bitmap bitmapRec=null;
	/**
	 * 显示扫描结果
	 */
	private TextView mTextView;
	/**
	 * 显示扫描拍的图片
	 */
	private ImageView mImageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan);

//		mTextView = (TextView) findViewById(R.id.result);
		mImageView = (ImageView) findViewById(R.id.qrcode_bitmap);

		// 点击按钮跳转到二维码扫描界面，这里用的是startActivityForResult跳转
		// 扫描完了之后调到该界面
		Button mButton = (Button) findViewById(R.id.btnclean);
		mButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(QrCodeActivity.this, CaptureActivity.class);
				startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case SCANNIN_GREQUEST_CODE:
				if (resultCode == RESULT_OK) {
					Bundle bundle = data.getExtras();
					// 显示扫描到的内容
					mTextView.setText(bundle.getString("result"));
					// 显示
					if(bitmapRec!=null)
					{
						mImageView.setImageBitmap(bitmapRec);
					}
				}
				break;
		}
	}


}

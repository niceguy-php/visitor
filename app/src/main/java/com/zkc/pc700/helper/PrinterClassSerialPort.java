package com.zkc.pc700.helper;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import com.zkc.helper.printer.Device;
import com.zkc.helper.printer.PrintService;
import com.zkc.helper.printer.PrinterClass;

public class PrinterClassSerialPort extends SerialPortHelper implements
		PrinterClass {
	PrintService printservice = new PrintService();
	private Handler mHandler;
	String choosed_serial = "/dev/ttySAC3";
	int baudrate = 38400;
	
	public PrinterClassSerialPort(Handler _mHandler){
		mHandler=_mHandler;
	}
	
	public boolean setSerialPortBaudrate(int _baudrate)
	{
		if(CloseSerialPort())
		{
			return OpenSerialPort(choosed_serial, _baudrate);
		}
		return false;
	}

	@Override
	protected void onDataReceived(byte[] buffer, int size) {
		if (buffer[0] == 0x13) {
			PrintService.isFUll = true;
			Log.i(TAG, "0x13:");
		} else if (buffer[0] == 0x11) {
			PrintService.isFUll = false;
			Log.i(TAG, "0x11:");
		} else {
			mHandler.obtainMessage(PrinterClass.MESSAGE_READ,
					size, -1, buffer).sendToTarget();
		}
	}

	@Override
	public boolean open(Context context) {
		return OpenSerialPort(choosed_serial, baudrate);
	}

	@Override
	public boolean close(Context context) {
		return CloseSerialPort();
	}

	@Override
	public void scan() {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Device> getDeviceList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void stopScan() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean connect(String device) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean disconnect() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getState() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setState(int state) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean IsOpen() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean write(byte[] bt) {
		return Write(bt);
	}

	@Override
	public boolean printText(String textStr) {
		byte[] buffer = printservice.getText(textStr);

		if (buffer.length <= 100) {
			write(buffer);
			return true;
		}
		int sendSize = 100;
		int issendfull = 0;
		for (int j = 0; j < buffer.length; j += sendSize) {

			if (PrintService.isFUll) {
				Log.i("BUFFER", "BUFFER FULL");
				int index = 0;
				while (index++ < 800) {
					if (!PrintService.isFUll) {
						issendfull = 0;
						Log.i("BUFFER", "BUFFER NULL" + index);
						break;
					}
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						Log.e(TAG, e.getMessage());
					}
				}
			}

			byte[] btPackage = new byte[sendSize];
			if (buffer.length - j < sendSize) {
				btPackage = new byte[buffer.length - j];
			}
			System.arraycopy(buffer, j, btPackage, 0, btPackage.length);
			write(btPackage);
		}

		return true;
	}

	@Override
	public boolean printImage(Bitmap bitmap) {
		write(printservice.getImage(bitmap));
		return write(new byte[] { 0x0a });
	}

	@Override
	public boolean printUnicode(String textStr) {
		return write(printservice.getTextUnicode(textStr));
	}
}

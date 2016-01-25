package com.zkc.pc700.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import android.util.Log;

public abstract class SerialPortHelper {

	protected static final String TAG = "SerialPortHelper";
	protected SerialPort mSerialPort;
	protected OutputStream mOutputStream;
	private InputStream mInputStream;
	private ReadThread mReadThread;

	private class ReadThread extends Thread {

		@Override
		public void run() {
			super.run();
			while (!isInterrupted()) {
				int size;
				try {
					byte[] buffer = new byte[64];
					if (mInputStream == null)
						return;
					size = mInputStream.read(buffer);
					if (size > 0) {
						onDataReceived(buffer, size);
					}
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
					return;
				}
			}
		}
	}

	protected Boolean OpenSerialPort(String device, int baudrate) {

		try {
			mSerialPort = new SerialPort(device, baudrate, 0);
			mOutputStream = mSerialPort.getOutputStream();
			mInputStream = mSerialPort.getInputStream();

			mReadThread = new ReadThread();
			mReadThread.start();
		} catch (SecurityException e) {
			Log.e(TAG, e.getMessage());
			return false;
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
			return false;
		} catch (InvalidParameterException e) {
			Log.e(TAG, e.getMessage());
			return false;
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return false;
		}
		return true;
	}	
	
	public Boolean Write(String str)
	{
		byte[] buffer=str.getBytes();
		return Write(buffer);
	}

	protected Boolean Write(byte[] buffer) {
		try {
			Log.d(TAG+" Write", byte2HexStr(buffer,buffer.length));
			mOutputStream.write(buffer);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
			return false;
		}
		return true;
	}

	protected Boolean CloseSerialPort() {
		try {
			if (mReadThread != null)
				mReadThread.interrupt();
			mReadThread=null;
			mSerialPort.close();
			mSerialPort = null;
			mOutputStream.close();
			mInputStream.close();
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return false;
		}
		return true;
	}



	public static String byte2HexStr(byte[] b, int lenth) {
		String stmp = "";
		StringBuilder sb = new StringBuilder("");
		for (int n = 0; n < lenth; n++) {
			stmp = Integer.toHexString(b[n] & 0xFF);
			sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
			sb.append(" ");
		}
		return sb.toString().toUpperCase().trim();
	}
	
	protected abstract void onDataReceived(final byte[] buffer, final int size);

}

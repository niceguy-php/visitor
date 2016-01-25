package com.zkc.pc700.helper;

import java.io.FileDescriptor;

public class PC700Helper {
		static {
			System.loadLibrary("PC700");
		}
		public native static int OPENPOWER();
		public native static int IOCTL(int fd,int controlcode,int ledID);
	    public native static int CLOSEPOWER();
}

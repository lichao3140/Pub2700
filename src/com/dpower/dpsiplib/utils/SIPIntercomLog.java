package com.dpower.dpsiplib.utils;

import com.dpower.dpsiplib.service.MyApp;

import android.util.Log;

public class SIPIntercomLog {
	private static final String TAG = "SIPIntercom";
	
	public static final int VERBOSE = 0;
	public static final int DEBUG = 1;
	public static final int INFO = 2;
	public static final int WARN = 3;
	public static final int ERROR = 4;
	private static boolean mIsShowLog = true;
	
	public static void setLogPrint(boolean isShowLog) {
		mIsShowLog = isShowLog;
		if(MyApp.endpoint!= null){
			if(isShowLog)
				MyApp.endpoint.setLogPrint(1);
			else
				MyApp.endpoint.setLogPrint(0);
		}
	}
	
	public static boolean getLogPrint() {
		return mIsShowLog;
	}

	public static void print(String msg) {
		if(mIsShowLog)
			Log.i(TAG, msg);
	}

	public static void print(int flag, String content) {
		if(!mIsShowLog)
			return;
		switch (flag) {
			case VERBOSE:
				Log.v(TAG, "" + content);
				break;
			case DEBUG:
				Log.d(TAG, "" + content);
				break;
			case INFO:
				Log.i(TAG, "" + content);
				break;
			case WARN:
				Log.w(TAG, "" + content);
				break;
			case ERROR:
				Log.e(TAG, "" + content);
			default:
				break;
		}
	}
	
	public static void print(String tag, String msg) {
		if(mIsShowLog)
			Log.i(tag, msg);
	}

	public static void print(int flag, String tag, String content) {
		if(!mIsShowLog)
			return;
		switch (flag) {
			case VERBOSE:
				Log.v(tag, "" + content);
				break;
			case DEBUG:
				Log.d(tag, "" + content);
				break;
			case INFO:
				Log.i(tag, "" + content);
				break;
			case WARN:
				Log.w(tag, "" + content);
				break;
			case ERROR:
				Log.e(tag, "" + content);
			default:
				break;
		}
	}
}

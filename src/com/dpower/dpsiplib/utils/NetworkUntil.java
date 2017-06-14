package com.dpower.dpsiplib.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;

public class NetworkUntil {
	
	public static final int CONNECTED_NO = 0;
	public static final int CONNECTED_MOBILE = 1;
	public static final int CONNECTED_WIFI = 2;
	public static final int CONNECTED_ETHERNET = 3;
	public static final String NETWORK_ERROR = "network error";
	
	public static int getNetState(Context context) {
		ConnectivityManager manager = (ConnectivityManager) 
				context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mobile = manager.getNetworkInfo(
				ConnectivityManager.TYPE_MOBILE);
		if (mobile != null) {
			State state = mobile.getState();
			if (state == State.CONNECTED) {
				return CONNECTED_MOBILE;
			}
		}
		NetworkInfo wifi = manager.getNetworkInfo(
				ConnectivityManager.TYPE_WIFI);
		if (wifi != null) {
			State state = wifi.getState();
			if (state == State.CONNECTED) {
				return CONNECTED_WIFI;
			}
		}
		NetworkInfo ethernet = manager.getNetworkInfo(
				ConnectivityManager.TYPE_ETHERNET);
		if (ethernet != null) {
			State state = ethernet.getState();
			if (state == State.CONNECTED) {
				return CONNECTED_ETHERNET;
			}
		}
		return CONNECTED_NO;
	}
	
	public static boolean getLanConnectState(String lanName) {
		Process process = null;
		InputStream inputStream = null;
		ByteArrayOutputStream outputStream = null;
		String cmd = "ifconfig " + lanName;
		String result = "";
		try {
			outputStream = new ByteArrayOutputStream();
			process = Runtime.getRuntime().exec(cmd);
			outputStream.write("/n".getBytes());
			inputStream = process.getInputStream();
			int len;
			while ((len = inputStream.read()) > 0) {
				outputStream.write(len);
			}
			outputStream.flush();
			byte[] data = outputStream.toByteArray();
			result = new String(data);
		} catch (IOException e) {
			e.printStackTrace();
			SIPIntercomLog.print(SIPIntercomLog.ERROR, "cmd", "error");
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (result.contains("up broadcast running multicast")) {
			return true;
		} else {
			return false;
		}
	}
}

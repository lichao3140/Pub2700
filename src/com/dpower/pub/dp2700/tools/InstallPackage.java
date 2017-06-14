package com.dpower.pub.dp2700.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class InstallPackage {
	
	public static String apkVersion = "1.1.9000";

	public static String install(String apkAbsolutePath) {
		String[] args = { "pm", "install", "-r", apkAbsolutePath };
		String result = "";
		ProcessBuilder processBuilder = new ProcessBuilder(args);
		Process process = null;
		InputStream errorStream = null;
		InputStream inputStream = null;
		ByteArrayOutputStream outputStream = null;
		try {
			outputStream = new ByteArrayOutputStream();
			int len;
			process = processBuilder.start();
			errorStream = process.getErrorStream();
			while ((len = errorStream.read()) > 0) {
				outputStream.write(len);
			}
			outputStream.write('\n');
			inputStream = process.getInputStream();
			while ((len = inputStream.read()) > 0) {
				outputStream.write(len);
			}
			byte[] data = outputStream.toByteArray();
			result = new String(data);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (errorStream != null) {
					errorStream.close();
				}
				if (inputStream != null) {
					inputStream.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (process != null) {
				process.destroy();
			}
		}
		return result;
	}

	public static String getPackageName(Context context, String packageName) {
		final PackageManager packageManager = context.getPackageManager();
		List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序
		List<String> pName = new ArrayList<String>();// 用于存储所有已安装程序的包名
		if (pinfo != null) {
			for (int i = 0; i < pinfo.size(); i++) {
				String pn = pinfo.get(i).packageName;
				pName.add(pn);
			}
		}
		boolean result = pName.contains(packageName);
		if (result) {
			return packageName;
		} else {
			return null;
		}
	}
}

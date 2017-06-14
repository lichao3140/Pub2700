package com.dpower.pub.dp2700.application;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import com.dpower.pub.dp2700.activity.ExceptionActivity;
import com.dpower.util.ConstConf;
import com.dpower.util.MyLog;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;

/**
 * @author ZhengZhiying
 * @Funtion 异常处理类
 */
public class CrashHandler implements UncaughtExceptionHandler {
	private static final String TAG = "CrashHandler";
	
	private UncaughtExceptionHandler mDefaultHandler;
	private static CrashHandler mInstance;
	private Context mContext;

	public static CrashHandler getInstance() {
		if (mInstance == null) {
			mInstance = new CrashHandler();
		}
		return mInstance;
	}
	
	public void init(Context context) {
		mContext = context;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		savaInfoToSD(mContext, ex);
		if (ex == null && mDefaultHandler != null) {
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			MyLog.print(MyLog.ERROR, TAG, "重启应用程序中.......");
			Intent intent = new Intent(mContext, ExceptionActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(intent);
			android.os.Process.killProcess(android.os.Process.myPid());// ZhengZhiying add
			System.exit(1);// ZhengZhiying add
		}
	}

	/**
	 * 保存获取的软件信息，设备信息和出错信息保存在SDcard中
	 * 
	 * @param context
	 * @param ex
	 * @return
	 */
	private String savaInfoToSD(Context context, Throwable ex) {
		String fileName = null;
		StringBuffer sb = new StringBuffer();

		for (Map.Entry<String, String> entry : obtainSimpleInfo(context)
				.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			sb.append(key).append(" = ").append(value).append("\n");
		}

		sb.append(obtainExceptionInfo(ex));

		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			String dirPath = ConstConf.SD_DIR;
			File dir = new File(dirPath + "crashLog" + File.separator);
			if (!dir.exists()) {
				dir.mkdir();
			}
			File[] files = dir.listFiles();
			if (files.length > 20) {
				// delete when record over 20
				for (int i = 0; i < files.length; i++) {
					files[i].delete();
				}
			}
			FileOutputStream outputStream = null;
			try {
				fileName = dir.toString() + File.separator
						+ paserTime(System.currentTimeMillis()) + ".log";
				outputStream = new FileOutputStream(fileName);
				outputStream.write(sb.toString().getBytes());
				outputStream.flush();
				outputStream.getFD().sync();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (outputStream != null) {
					try {
						outputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return fileName;
	}

	/**
	 * 获取一些简单的信息,软件版本，手机版本，型号等信息存放在HashMap中
	 * 
	 * @param context
	 * @return
	 */
	private HashMap<String, String> obtainSimpleInfo(Context context) {
		HashMap<String, String> map = new HashMap<String, String>();
		PackageManager packageManager = context.getPackageManager();
		PackageInfo packageInfo = null;
		try {
			packageInfo = packageManager.getPackageInfo(
					context.getPackageName(), PackageManager.GET_ACTIVITIES);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		map.put("versionName", packageInfo.versionName);
		map.put("versionCode", "" + packageInfo.versionCode);
		map.put("MODEL", "" + Build.MODEL);
		map.put("SDK_INT", "" + Build.VERSION.SDK_INT);
		map.put("PRODUCT", "" + Build.PRODUCT);
		return map;
	}

	/**
	 * 将毫秒数转换成yyyy-MM-dd-HH-mm-ss的格式
	 * 
	 * @param milliseconds
	 * @return
	 */
	private String paserTime(long milliseconds) {
		System.setProperty("user.timezone", "Asia/Shanghai");
		TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
		TimeZone.setDefault(tz);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		String times = format.format(new Date(milliseconds));
		return times;
	}

	/**
	 * 获取系统未捕捉的错误信息
	 * 
	 * @param throwable
	 * @return
	 */
	private String obtainExceptionInfo(Throwable throwable) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		throwable.printStackTrace(printWriter);
		printWriter.close();
		return stringWriter.toString();
	}
}
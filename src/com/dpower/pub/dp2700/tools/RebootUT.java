package com.dpower.pub.dp2700.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.dpower.pub.dp2700.broadcastreceiver.RebootAlarmReceiver;
import com.dpower.util.MyLog;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * @author ZhengZhiying 2015-4-7 14:04:33
 * @Funtion 重启工具类
 */
public class RebootUT {
	private static final String TAG = "RebootUT";
	
	public static final int REBOOT_TIME = 3;
	private static RebootUT mRebootUT;
	private Context mContext;
	private AlarmManager mAlarmManager;

	private RebootUT(Context context) {
		mContext = context;
	}

	public static RebootUT getInstance(Context context) {
		if (mRebootUT == null) {
			mRebootUT = new RebootUT(context);
		}
		return mRebootUT;
	}

	public void rebootAtTime(int hour) {
		mAlarmManager = (AlarmManager) mContext.getSystemService(
				Context.ALARM_SERVICE);
		Intent intent = new Intent(mContext, RebootAlarmReceiver.class);
		intent.putExtra("reboot", "to reboot");
		PendingIntent pi;
		pi = PendingIntent.getBroadcast(mContext, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		long time = getTime(hour);
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		MyLog.print(TAG, "重启闹钟设置：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
				Locale.getDefault()).format(cal.getTime()));
		mAlarmManager.cancel(pi);
		// mAlarmManager.set(AlarmManager.RTC_WAKEUP, time, pi); // 设置闹钟，当前时间就唤醒
		mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time,
				AlarmManager.INTERVAL_DAY, pi); // 设置闹钟，当前时间就唤醒
//		mAlarmManager.setRepeating(AlarmManager.RTC, time, 
//				AlarmManager.INTERVAL_DAY, pi);
	}

	private long getTime(int hour) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		if (c.get(Calendar.HOUR_OF_DAY) >= hour) {
			c.add(Calendar.DAY_OF_MONTH, 1);
		}
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		return c.getTimeInMillis();
	}
	
	/**  关机重启   */
	public static void rebootSU() {  
        Runtime runtime = Runtime.getRuntime();  
        Process proc = null;  
        OutputStreamWriter osw = null;  
        StringBuilder sbstdOut = new StringBuilder();  
        StringBuilder sbstdErr = new StringBuilder();  
  
        String command="/system/bin/reboot";  
  
        try { // Run Script  
            proc = runtime.exec("su");  
            osw = new OutputStreamWriter(proc.getOutputStream());  
            osw.write(command);  
            osw.flush();  
            osw.close();  
  
        } catch (IOException ex) {  
            ex.printStackTrace();  
        } finally {  
            if (osw != null) {  
                try {  
                    osw.close();  
                } catch (IOException e) {  
                    e.printStackTrace();                      
                }  
            }  
        }  
        try {  
            if (proc != null)  
                proc.waitFor();  
        } catch (InterruptedException e) {  
            e.printStackTrace();  
        }  
  
        sbstdOut.append(new BufferedReader(new InputStreamReader(proc  
                .getInputStream())));  
        sbstdErr.append(new BufferedReader(new InputStreamReader(proc  
                .getErrorStream())));  
        if (proc.exitValue() != 0) {  
        }  
    } 

	// 测试程序
	// private long getTimeTest(int hour){
	// Calendar c=Calendar.getInstance();//获取日期对象
	// int second = c.get(Calendar.SECOND) + 10;
	// c.add(Calendar.SECOND, second);
	// return c.getTimeInMillis();
	// }
}

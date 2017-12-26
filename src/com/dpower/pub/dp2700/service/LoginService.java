package com.dpower.pub.dp2700.service;

import com.dpower.cloudintercom.CloudIntercom;
import com.dpower.pub.dp2700.broadcastreceiver.LoginBroadcastReveicer;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

/**
 * ��̨��ʱ��¼SIP�˺�
 * @author LiChao
 *
 */
public class LoginService extends Service{

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				CloudIntercom.startLogin();
				Log.e("lichao", "��ʱ3���Ӻ��Զ���¼");
			}
		}).start();
		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		int mTime = 1000 * 60 * 3; // ����120s
		long triggerAtTime = SystemClock.elapsedRealtime() + mTime;
		Intent mIntent = new Intent(this, LoginBroadcastReveicer.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, mIntent, 0);
		alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pendingIntent);
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

}

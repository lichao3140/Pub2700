package com.dpower.pub.dp2700.service;

import java.util.ArrayList;

import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.util.MyLog;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

/**
 * @author ZhengZhiying
 * @Funtion	ÆÁÄ»ÐÝÃß¼à¿Ø·þÎñ
 */
@SuppressWarnings("deprecation")
public class ScreenSaverService extends Service {
	private static final String TAG = "ScreenSaverService";
	
	public static final int BLACK = 0;
	public static final int CALENDAR = 1;
	public static final int PICTURE = 2;
	private static ArrayList<ScreenOffCallback> mCallbacks = new ArrayList<ScreenOffCallback>();
	private KeyguardManager mKeyguardManager = null;  
	private KeyguardLock mKeyguardLock = null;   
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);   
	    mKeyguardLock = mKeyguardManager.newKeyguardLock("screen");   
	    mKeyguardLock.disableKeyguard();

		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		registerReceiver(mScreenSaverReciever, filter);
		MyLog.print(TAG, "×¢²á¼àÌý ÆÁÄ»ÐÝÃß");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		MyLog.print(TAG, "onDestroy ½â³ý×¢²á¼àÌý ÆÁÄ»ÐÝÃß");
		unregisterReceiver(mScreenSaverReciever);
	};

	BroadcastReceiver mScreenSaverReciever = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			MyLog.print(TAG, "¼àÌýÆÁÄ»ÐÝÃß ");
			if (!mCallbacks.isEmpty()) {
				for (ScreenOffCallback screenOffCallback : mCallbacks) {
					screenOffCallback.onScreenOff(
							SPreferences.getInstance().getScreenSaverMode());
					MyLog.print(TAG, "onScreenOff");
				}
			}
		}
	};
	
	public interface ScreenOffCallback {
		public void onScreenOff(int flag);
	}
	
	public static void registerScreenOffCallback(ScreenOffCallback callback) {
		MyLog.print(TAG, "registerScreenOffCallback");
		mCallbacks.add(callback);
	}

	public static void unRegisterScreenOffCallback(ScreenOffCallback callback) {
		MyLog.print(TAG, "unRegisterScreenOffCallback");
		if (mCallbacks.contains(callback)) {
			mCallbacks.remove(callback);
		}
	}
}

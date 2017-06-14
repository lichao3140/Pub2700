package com.dpower.pub.dp2700.tools;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;

/**
 * ÆÁÄ»ÐÝÃß¹¤¾ß
 */
public class ScreenUT {
	
	public static boolean isAlwaysScreenOn = false;
	private static ScreenUT mScreenUT;
	private PowerManager mPowerManager;
	private WakeLock mWakeLock;
	private Context mContext;

	private ScreenUT() {
		
	}
	
	@SuppressWarnings("deprecation")
	public void init(Context context){
		mContext = context;
		mPowerManager = (PowerManager) mContext
				.getSystemService(Context.POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
				| PowerManager.ON_AFTER_RELEASE 
				| PowerManager.SCREEN_DIM_WAKE_LOCK, "ScreenUT");
	}

	public static ScreenUT getInstance() {
		if (mScreenUT == null) {
			mScreenUT = new ScreenUT();
		}
		return mScreenUT;
	}

	/**
	 * »½ÐÑÆÁÄ»,±£³ÖÆÁÄ»ÐÝÃß
	 */
	public void acquireWakeLock() {
		wakeUpScreen();
		mWakeLock.acquire();
	}

	/**
	 * ÊÍ·Å,ÔÊÐíÆÁÄ»ÐÝÃß
	 */
	public void releaseWakeLock() {
		if(isAlwaysScreenOn){
			return;
		}
		if (mWakeLock.isHeld()) {
			mWakeLock.release();
		}
	}

	/**
	 * »½ÐÑÆÁÄ»
	 */
	public void wakeUpScreen() {
		if(!mPowerManager.isScreenOn()) {
			mWakeLock.acquire();
			mWakeLock.release();
		}
	}
	
	/**
	 * ¹Ø±ÕÆÁÄ»
	 */
	public void goToSleep() {
		mPowerManager.goToSleep(SystemClock.uptimeMillis());
	}
}

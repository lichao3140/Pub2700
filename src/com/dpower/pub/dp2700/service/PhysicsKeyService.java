package com.dpower.pub.dp2700.service;

import java.util.ArrayList;
import org.dpower.GenernalGpioSet.JniClassGpioSet;
import org.dpower.GenernalGpioSet.UtilConst;
import com.dpower.util.MyLog;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * 物理按键监控服务
 */
public class PhysicsKeyService extends Service {
	private static final String TAG = "PhysicsKeyService";
	
	public static final int MESSAGE = 8;
	public static final int HANGUP = 9;
	public static final int UNLOCK = 10;
	public static final int MONITOR = 11;
	public static final int VOLUME = 12;
	private static boolean mThreadFlag;
	private static ArrayList<KeyCallback> mKeyCallbacks = new ArrayList<KeyCallback>();
	/** 小区信息，呼叫管理中心，监视，开锁，接听或挂断*/
	private static boolean[] mKeySwitch;
	private static boolean[] mTempKeySwitch = new boolean[] { false, false, false, false, false };
	private JniClassGpioSet mGpioSet;
	private int mMessage_H8;
	private int mHangUp_H9;
	private int mUnlock_H10;
	private int mMonitor_H11;
	private int mVolume_H12;
	private PhysicKeyThread mKeyThread;
	private int state;
	private int mOldState = 0;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		MyLog.print(TAG, "onCreate");
		mGpioSet = new JniClassGpioSet();
		mMessage_H8 = mGpioSet.InitGpio(UtilConst.GPIO_H8, 0);
		mHangUp_H9 = mGpioSet.InitGpio(UtilConst.GPIO_H9, 0);
		mUnlock_H10 = mGpioSet.InitGpio(UtilConst.GPIO_H10, 0);
		mMonitor_H11 = mGpioSet.InitGpio(UtilConst.GPIO_H11, 0);
		mVolume_H12 = mGpioSet.InitGpio(UtilConst.GPIO_H12, 0);
		int total = mGpioSet.GetGpioValue(mMessage_H8) + mGpioSet.GetGpioValue(mHangUp_H9)
		 	+ mGpioSet.GetGpioValue(mUnlock_H10) + mGpioSet.GetGpioValue(mMonitor_H11)
		 	+ mGpioSet.GetGpioValue(mVolume_H12);
		if (total > 2) {
			mThreadFlag = true;
			mKeyThread = new PhysicKeyThread();
			mKeyThread.start();
			mKeySwitch = mTempKeySwitch;
			MyLog.print(TAG, "7寸不带电源按键");
		} else {
			MyLog.print(TAG, "7寸带电源按键");
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mThreadFlag = false;
		if (mGpioSet != null) {
			mGpioSet.UninitGpio(mMessage_H8);
			mGpioSet.UninitGpio(mHangUp_H9);
			mGpioSet.UninitGpio(mUnlock_H10);
			mGpioSet.UninitGpio(mMonitor_H11);
			mGpioSet.UninitGpio(mVolume_H12);
			mGpioSet = null;
		}
		MyLog.print(TAG, "onDestroy");
	}
	
	public static void setKeyListener(boolean threadFlag) {
		MyLog.print(TAG, "setKeyListener " + threadFlag);
		PhysicsKeyService.mThreadFlag = threadFlag;
	}
	
	/**
	 * @param keySwitch 小区信息，呼叫管理中心，监视，开锁，接听或挂断
	 */
	public static void setKeySwitch(boolean[] keySwitch) {
		mTempKeySwitch = keySwitch;
		mKeySwitch = mTempKeySwitch;
		for (int i = 0; i < mKeySwitch.length; i++) {
			MyLog.print(TAG, "mKeySwitch " + mKeySwitch[i]);
		}
	}
	
	/**
	 * @return 小区信息，呼叫管理中心，监视，开锁，接听或挂断
	 */
	public static boolean[] getKeySwitch() {
		return mKeySwitch;
	}
	
	public interface KeyCallback {
		public void onKey(int keyIO);
	}
	
	public static void registerKeyCallback(KeyCallback callback) {
		MyLog.print(TAG, "registerKeyCallback");
		mKeyCallbacks.add(callback);
	}

	public static void unRegisterKeyCallback(KeyCallback callback) {
		MyLog.print(TAG, "unRegisterKeyCallback");
		if (mKeyCallbacks.contains(callback)) {
			mKeyCallbacks.remove(callback);
		}
	}
	
	class PhysicKeyThread extends Thread {

		@Override
		public void run() {
			MyLog.print(TAG, "PhysicKeyThread run");
			MyLog.print(TAG, "mThreadFlag " + mThreadFlag);
			for (int i = 0; i < mKeySwitch.length; i++) {
				MyLog.print(TAG, "mKeySwitch " + mKeySwitch[i]);
			}
			
			while (mThreadFlag) {
				try {
					PhysicKeyThread.sleep(100);
					for (int i = 0; i < mKeySwitch.length; i++) {
						if (mKeySwitch[i]) {
							handleKey(i);
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		private void handleKey(int key) {
			switch (key) {
				case 0:
					state = mGpioSet.GetGpioValue(mMessage_H8);
					if (state != mOldState) {
						mOldState = state;
						if (state == 0 && !mKeyCallbacks.isEmpty()) {
							for (KeyCallback keyCallBack : mKeyCallbacks) {
								keyCallBack.onKey(MESSAGE);
								MyLog.print(TAG, "onMessageKey");
							}
							for (int i = 0; i < mTempKeySwitch.length; i++) {
								MyLog.print(TAG, "mTempKeySwitch " + mTempKeySwitch[i]);
							}
							mKeySwitch = new boolean[] { true, false, false, false, false }; //屏蔽其他按键，防止多个按键被同时按下
						} else if (state == 1) {
							MyLog.print(TAG, "onMessageKey 恢复按键开关状态");
							mKeySwitch = mTempKeySwitch;
						}
					}
					break;
				case 1:
					state = mGpioSet.GetGpioValue(mVolume_H12);
					if (state != mOldState) {
						mOldState = state;
						if (state == 0 && !mKeyCallbacks.isEmpty()) {
							for (KeyCallback keyCallBack : mKeyCallbacks) {
								keyCallBack.onKey(VOLUME);
								MyLog.print(TAG, "onVolumeKey");
							}
							for (int i = 0; i < mTempKeySwitch.length; i++) {
								MyLog.print(TAG, "mTempKeySwitch " + mTempKeySwitch[i]);
							}
							mKeySwitch = new boolean[] { false, true, false, false, false };
						} else if (state == 1) {
							MyLog.print(TAG, "onVolumeKey 恢复按键开关状态");
							mKeySwitch = mTempKeySwitch;
						}
					}
					break;
				case 2:
					state = mGpioSet.GetGpioValue(mMonitor_H11);
					if (state != mOldState) {
						mOldState = state;
						if (state == 0 && !mKeyCallbacks.isEmpty()) {
							for (KeyCallback keyCallBack : mKeyCallbacks) {
								keyCallBack.onKey(MONITOR);
								MyLog.print(TAG, "onMonitorKey");
							}
							for (int i = 0; i < mTempKeySwitch.length; i++) {
								MyLog.print(TAG, "mTempKeySwitch " + mTempKeySwitch[i]);
							}
							mKeySwitch = new boolean[] { false, false, true, false, false };
						} else if (state == 1) {
							MyLog.print(TAG, "onMonitorKey 恢复按键开关状态");
							mKeySwitch = mTempKeySwitch;
						}
					}
					break;
				case 3:
					state = mGpioSet.GetGpioValue(mUnlock_H10);
					if (state != mOldState) {
						mOldState = state;
						if (state == 0 && !mKeyCallbacks.isEmpty()) {
							for (KeyCallback keyCallBack : mKeyCallbacks) {
								keyCallBack.onKey(UNLOCK);
								MyLog.print(TAG, "onUnlockKey");
							}
							for (int i = 0; i < mTempKeySwitch.length; i++) {
								MyLog.print(TAG, "mTempKeySwitch " + mTempKeySwitch[i]);
							}
							mKeySwitch = new boolean[] { false, false, false, true, false };
						} else if (state == 1) {
							MyLog.print(TAG, "onUnlockKey 恢复按键开关状态");
							mKeySwitch = mTempKeySwitch;
						}
					}
					break;
				case 4:
					state = mGpioSet.GetGpioValue(mHangUp_H9);
					if (state != mOldState) {
						mOldState = state;
						if (state == 0 && !mKeyCallbacks.isEmpty()) {
							for (KeyCallback keyCallBack : mKeyCallbacks) {
								keyCallBack.onKey(HANGUP);
								MyLog.print(TAG, "onHangUpKey");
							}
							for (int i = 0; i < mTempKeySwitch.length; i++) {
								MyLog.print(TAG, "mTempKeySwitch " + mTempKeySwitch[i]);
							}
							mKeySwitch = new boolean[] { false, false, false, false, true };
						} else if (state == 1) {
							MyLog.print(TAG, "onHangUpKey 恢复按键开关状态");
							mKeySwitch = mTempKeySwitch;
						}
					}
					break;
				default:
					break;
			}
		}
	}
}

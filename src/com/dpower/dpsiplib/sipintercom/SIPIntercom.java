package com.dpower.dpsiplib.sipintercom;

import java.util.List;

import org.pjsip.pjsua2.VideoWindow;

import com.dpower.dpsiplib.callback.SIPCallback;
import com.dpower.dpsiplib.service.DPSIPService;
import com.dpower.dpsiplib.service.MyCall;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SIPIntercom {
	private static final String LICHAO = "lichao";
	private static SIPCallback mSIPCallback = null;
	private static Context mContext = null;
	
	public static void init(Context context, SIPCallback callback) {
		mContext = context;
		mSIPCallback = callback;
		mContext.startService(new Intent(mContext, DPSIPService.class));
	}
	
	public static void deinit() {
		mContext.stopService(new Intent(mContext, DPSIPService.class));
		mSIPCallback = null;
		mContext = null;
	}
	
	public static void setSIPCallback(SIPCallback callback) {
		mSIPCallback = callback;
	}
	
	public static SIPCallback getSIPCallback() {
		return mSIPCallback;
	}
	
	/**
	 * ��¼
	 * @param username �û���
	 * @param url ��������ַ
	 * @return
	 */
	public static boolean login(String username, String password, String url) {
		if (DPSIPService.getInstance() != null) {
			return DPSIPService.getInstance().login(username, password, url);
		} else {
			if (mSIPCallback != null) {
				mSIPCallback.sipServiceStop();
			}
			return false;
		}
	}
	
	/** �˳���¼ */
	public static void logout() {
	   if (DPSIPService.getInstance() != null) {
			DPSIPService.getInstance().logout();
		} else {
			if (mSIPCallback != null) {
				mSIPCallback.sipServiceStop();
			}
		}
	}
   
	/** ������˷�, 0-�ر�, 1-�� */
	public static void setMic(int level) {
		if (DPSIPService.getInstance() != null) {
			DPSIPService.getInstance().setMic(level);
		} else {
			if (mSIPCallback != null) {
				mSIPCallback.sipServiceStop();
			}
		}
	}
	
	/** ������Ƶ, 0-����, 1-������ */
	public static void setVolume(int level) {
		if (DPSIPService.getInstance() != null) {
			DPSIPService.getInstance().setVolume(level);
		} else {
			if (mSIPCallback != null) {
				mSIPCallback.sipServiceStop();
			}
		}
	}
	
	/** �˺��Ƿ����� */
	public static boolean isOnline() {
		if (DPSIPService.getInstance() != null) {
			return DPSIPService.getInstance().isOnline();
		} else {
			if (mSIPCallback != null) {
				mSIPCallback.sipServiceStop();
			}
			return false;
		}
	}
	
	/** ��ȡͨ����Ƶ */
	public static VideoWindow getVideoWindow() {
		if (DPSIPService.getInstance() != null) {
			return DPSIPService.getInstance().getVideoWindow();
		} else {
			if (mSIPCallback != null) {
				mSIPCallback.sipServiceStop();
			}
			return null;
		}
	}
	
	/** ͨ���б� */
	public static List<MyCall> getCallList() {
		if (DPSIPService.getInstance() != null) {
			return DPSIPService.getInstance().getCallList();
		} else {
			if (mSIPCallback != null) {
				mSIPCallback.sipServiceStop();
			}
			return null;
		}
	}
	
	/**
	 * ����
	 * @param user �Է����û���
	 * @return
	 */
	public static MyCall callOut(String user) {
		if (DPSIPService.getInstance() != null) {
			return DPSIPService.getInstance().callOut(user);
		} else {
			Log.i(LICHAO, "SIPIntercom callout user null");
			if (mSIPCallback != null) {
				mSIPCallback.sipServiceStop();
			}
			return null;
		}
	}
	
	/** ���� */
	public static void accept() {
		if (DPSIPService.getInstance() != null) {
			DPSIPService.getInstance().accept();
		} else {
			if (mSIPCallback != null) {
				mSIPCallback.sipServiceStop();
			}
		}
	}
	
	/** �Ҷϵ�ǰͨ�� */
	public static void hangupForBusy() {
		if (DPSIPService.getInstance() != null) {
			DPSIPService.getInstance().hangupForBusy();
		} else {
			if (mSIPCallback != null) {
				mSIPCallback.sipServiceStop();
			}
		}
	}
	
	/** �Ҷ�ȫ��ͨ�� */
	public static void hangup() {
		if (DPSIPService.getInstance() != null) {
			DPSIPService.getInstance().hangup();
		} else {
			if (mSIPCallback != null) {
				mSIPCallback.sipServiceStop();
			}
		}
	}
	
	/** �Ҷ�ָ��ͨ�� */
	public static void hangup(MyCall call) {
		if (DPSIPService.getInstance() != null) {
			DPSIPService.getInstance().hangup(call);
		} else {
			if (mSIPCallback != null) {
				mSIPCallback.sipServiceStop();
			}
		}
	}
}

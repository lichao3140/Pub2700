package com.dpower.dpsiplib.sipintercom;

import java.util.List;
import com.dpower.dpsiplib.callback.SIPCallback;
import com.dpower.dpsiplib.service.DPSIPService;
import com.dpower.dpsiplib.service.MyCall;
import android.content.Context;
import android.content.Intent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;

public class SIPIntercom {
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
	
	/** ��Ƶ�ص� */
	public static Callback getCallback() {
		if (DPSIPService.getInstance() != null) {
			return DPSIPService.getInstance().getCallback();
		} else {
			if (mSIPCallback != null) {
				mSIPCallback.sipServiceStop();
			}
			return null;
		}
	}
	
	public static void setSurfaceHolder(SurfaceHolder holder) {
		if (DPSIPService.getInstance() != null) {
			DPSIPService.getInstance().setSurfaceHolder(holder);
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
	
	/**
	 * ����JSON����
	 * @param json JSON���ݵ��ַ���
	 * @param remoteUser �Է��˺�
	 * @param isEncryption true-����,false-������
	 * @return 0-�ɹ�����0-ʧ��
	 */
	public static int sendMessage(String json, String remoteUser, boolean isEncryption) {
		if (DPSIPService.getInstance() != null) {
			return DPSIPService.getInstance().sendMessage(json, remoteUser, isEncryption);
		} else {
			if (mSIPCallback != null) {
				mSIPCallback.sipServiceStop();
			}
			return -3;
		}
	}
}

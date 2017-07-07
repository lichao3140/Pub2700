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
	 * 登录
	 * @param username 用户名
	 * @param url 服务器地址
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
	
	/** 退出登录 */
	public static void logout() {
	   if (DPSIPService.getInstance() != null) {
			DPSIPService.getInstance().logout();
		} else {
			if (mSIPCallback != null) {
				mSIPCallback.sipServiceStop();
			}
		}
	}
   
	/** 设置麦克风, 0-关闭, 1-打开 */
	public static void setMic(int level) {
		if (DPSIPService.getInstance() != null) {
			DPSIPService.getInstance().setMic(level);
		} else {
			if (mSIPCallback != null) {
				mSIPCallback.sipServiceStop();
			}
		}
	}
	
	/** 视频回调 */
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
	
	/** 账号是否在线 */
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
	
	/** 通话列表 */
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
	 * 呼叫
	 * @param user 对方的用户名
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
	
	/** 接听 */
	public static void accept() {
		if (DPSIPService.getInstance() != null) {
			DPSIPService.getInstance().accept();
		} else {
			if (mSIPCallback != null) {
				mSIPCallback.sipServiceStop();
			}
		}
	}
	
	/** 挂断当前通话 */
	public static void hangupForBusy() {
		if (DPSIPService.getInstance() != null) {
			DPSIPService.getInstance().hangupForBusy();
		} else {
			if (mSIPCallback != null) {
				mSIPCallback.sipServiceStop();
			}
		}
	}
	
	/** 挂断全部通话 */
	public static void hangup() {
		if (DPSIPService.getInstance() != null) {
			DPSIPService.getInstance().hangup();
		} else {
			if (mSIPCallback != null) {
				mSIPCallback.sipServiceStop();
			}
		}
	}
	
	/** 挂断指定通话 */
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
	 * 发送JSON数据
	 * @param json JSON数据的字符串
	 * @param remoteUser 对方账号
	 * @param isEncryption true-加密,false-不加密
	 * @return 0-成功，非0-失败
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

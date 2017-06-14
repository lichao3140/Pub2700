package com.dpower.cloudmessage;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

public class CloudMessage {
	private static final String TAG = "CloudMessage";
	
	private static final int MSG_LOGIN  = 100; //登陆
	private static final int MSG_NORMAL_LINK =1000; //消息处理
	private static Object lock = new Object();
	private static CloudMessage mCloudMessage = null;
	private static MessageCallback mCallback = null;
	
	private CloudMessage() {
		
	}
	
	public static CloudMessage getInstance(MessageCallback callback) {
		if (mCloudMessage == null) {
			mCloudMessage = new CloudMessage();
		}
		mCallback = callback;
		return mCloudMessage;
	}
	
	public static void deinit() {
		mCloudMessage = null;
		mCallback = null;
	}

	/**
	 * 登录外网服务器
	 * @param username 账号
	 * @param serverIp 消息服务器ip
	 * @param portServer 消息服务器端口
	 */
	public native void DPLogin(String username, String serverIp, int portServer);

	/**
	 * 退出登录
	 */
	public native void DPUnLogin();

	/**
	 * 发送消息
	 * @param strMsg
	 *            整个消息体
	 * @param strEx
	 *            附加消息（传额外参数）
	 * @return
	 */
	public native int DPSendMessage(String strMsg, String strEx, int exSize);

	/**
	 * 发送普通消息到手机
	 * 
	 * @param localUser
	 * @param remoteUser
	 * @param content
	 *            协议里"b"的内容
	 */
	public void DPSendMessage(String localUser, String remoteUser,
			String content) {
		synchronized (lock) {
			Log.i(TAG, "DPSendMessage");
			JSONObject jsonmsg = new JSONObject();
			try {
				jsonmsg.put("cmd", "msg");
				jsonmsg.put("fu", localUser);
				jsonmsg.put("tu", remoteUser);
				jsonmsg.put("b", content);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (jsonmsg != null) {
				String strtmp = jsonmsg.toString();
				DPSendMessage(strtmp, null, 0);
			}
		}
	}
	
	/**
	 * 发送普通消息到手机
	 * 
	 * @param localUser
	 * @param remoteUser
	 * @param content
	 *            协议里"b"的内容
	 * @param data
	 *            附加数据
	 */
	public void DPSendMessage(String localUser, String remoteUser,
			String content, String data) {
		synchronized (lock) {
			Log.i(TAG, "DPSendMessage 附加数据");
			JSONObject jsonmsg = new JSONObject();
			try {
				jsonmsg.put("cmd", "msg");
				jsonmsg.put("fu", localUser);
				jsonmsg.put("tu", remoteUser);
				jsonmsg.put("b", content);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (jsonmsg != null) {
				String strtmp = jsonmsg.toString();
				DPSendMessage(strtmp, data, 0);
			}
		}
	}

	/**
	 * 发送消息到全部手机
	 * 
	 * @param mode
	 */
	public void DPSendMessageToAll(String localUser, String msg, List<String> accounts) {
		if(accounts == null || accounts.size() == 0){
			Log.e(TAG, "accounts is empty");
			return;
		}
		synchronized (lock) {
			Log.i(TAG, "DPSendMessageToAll");
			for (String acc : accounts) {
				JSONObject jsonmsg = new JSONObject();
				try {
					jsonmsg.put("cmd", "msg");
					jsonmsg.put("fu", localUser);
					jsonmsg.put("tu", acc);
					jsonmsg.put("b", msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (jsonmsg != null) {
					String strtmp = jsonmsg.toString();
					DPSendMessage(strtmp, null, 0);
				}
			}
		}
	}

	/**
	 * 消息回调
	 * 
	 * @param type
	 * @param wpara
	 * @param lpara
	 * @param zpara
	 * @param content
	 */
	public void onRecvMessage(int type, int wpara, int lpara, int zpara,
			String content) {
		Log.e(TAG, "callback type :" + type + " ,wpara:" + wpara);
		switch (type) {
			case MSG_LOGIN: // 登录结果
				if (wpara == 0) {
					Log.i(TAG, "Message登录成功！");
				} else {
					Log.i(TAG, "Message登录失败 ：" + content);
				}
				if (mCallback != null) {
					mCallback.loginResult(wpara, content);
				}
				break;
			case MSG_NORMAL_LINK: // 收到消息
				if (TextUtils.isEmpty(content))
					return;
				if (mCallback != null) {
					mCallback.receivedMessage(content);
				}
				break;
		}
	}
	
	static {
		try {
			Log.e(TAG, " load YunClient");
			System.loadLibrary("YunClient");
		} catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
		}
	}
}

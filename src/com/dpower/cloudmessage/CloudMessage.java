package com.dpower.cloudmessage;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

public class CloudMessage {
	private static final String TAG = "CloudMessage";
	
	private static final int MSG_LOGIN  = 100; //��½
	private static final int MSG_NORMAL_LINK =1000; //��Ϣ����
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
	 * ��¼����������
	 * @param username �˺�
	 * @param serverIp ��Ϣ������ip
	 * @param portServer ��Ϣ�������˿�
	 */
	public native void DPLogin(String username, String serverIp, int portServer);

	/**
	 * �˳���¼
	 */
	public native void DPUnLogin();

	/**
	 * ������Ϣ
	 * @param strMsg
	 *            ������Ϣ��
	 * @param strEx
	 *            ������Ϣ�������������
	 * @return
	 */
	public native int DPSendMessage(String strMsg, String strEx, int exSize);

	/**
	 * ������ͨ��Ϣ���ֻ�
	 * 
	 * @param localUser
	 * @param remoteUser
	 * @param content
	 *            Э����"b"������
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
	 * ������ͨ��Ϣ���ֻ�
	 * 
	 * @param localUser
	 * @param remoteUser
	 * @param content
	 *            Э����"b"������
	 * @param data
	 *            ��������
	 */
	public void DPSendMessage(String localUser, String remoteUser,
			String content, String data) {
		synchronized (lock) {
			Log.i(TAG, "DPSendMessage ��������");
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
	 * ������Ϣ��ȫ���ֻ�
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
	 * ��Ϣ�ص�
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
			case MSG_LOGIN: // ��¼���
				if (wpara == 0) {
					Log.i(TAG, "Message��¼�ɹ���");
				} else {
					Log.i(TAG, "Message��¼ʧ�� ��" + content);
				}
				if (mCallback != null) {
					mCallback.loginResult(wpara, content);
				}
				break;
			case MSG_NORMAL_LINK: // �յ���Ϣ
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

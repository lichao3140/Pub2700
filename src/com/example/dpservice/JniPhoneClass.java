package com.example.dpservice;

import android.content.Context;
import android.content.Intent;

import com.dpower.function.DPFunction;
import com.dpower.util.MyLog;

/**
 * @������JniPhoneClass
 * @������DP�Խ��� ���෽������JNIʵ��,����Ϊ�����ಢ�Ҹ����������̶�Ϊcom.example.dpservice
 */
public class JniPhoneClass {
	
	private static JniPhoneClass mInstance = null;
	private static Context mContext = null;

	private JniPhoneClass() {
		   mContext= DPFunction.getContext();
	}

	public static JniPhoneClass getInstance() {
		if (mInstance == null) {
			mInstance = new JniPhoneClass();
		}
		return mInstance;
	}

	/**
	 * @��������Init
	 * @���ܣ������Խ�����
	 * @����1��String localCode - ������ǰ����
	 * @����2��String localIpAddr - ������ǰIP��ַ
	 * @����ֵ��boolean ture-�ɹ� false-ʧ��
	 * @��ע����UnInit()����ʹ��
	 */
	public native boolean Init(String localCode, String localIpAddr);

	/**
	 * @��������UnInit
	 * @���ܣ��Y���Խ�����
	 */
	public native void UnInit();

	/**
	 * @��������CallOut
	 * @���ܣ�����ָ������
	 * @����1��String RemoteCode - �Է�����("1010101010101")
	 * @����2��String RemoteipAddr - �Է�IP��ַ("192.168.1.21")
	 * @����ֵ��0-ʧ��,��0-CallSessionID
	 */
	public native int CallOut(String RemoteCode, String RemoteipAddr);

	/**
	 * @��������Monitor
	 * @���ܣ����ָ���ſڻ�
	 * @����1��String RemoteCode - �Է�����("3010101010101")
	 * @����2��String RemoteipAddr - �Է�IP��ַ("192.168.1.20")
	 * @����ֵ��0-ʧ��,��0-CallSessionID
	 */
	public native int Monitor(String RemoteCode, String RemoteipAddr);

	/**
	 * @��������Accept
	 * @���ܣ�����ָ��ID�ĻỰ
	 * @������int CallSessionID - ID
	 * @����ֵ��boolean false-ʧ��,true-�ɹ�
	 */
	public native boolean Accept(int CallSessionID);

	/**
	 * @��������HangUp
	 * @���ܣ��Ҷ�ָ��ID�ĻỰ
	 * @������int CallSessionID - ID
	 * @����ֵ��boolean false-ʧ��,true-�ɹ�
	 */
	public native boolean HangUp(int CallSessionID);

	/**
	 * @��������SetVideoDisplayArea
	 * @���ܣ����öԷ���Ƶ�ڱ�����Ļ����ʾ����
	 * @����1��int CallSessionID - ID
	 * @����2��int index - ������ʼ��Ϊ0
	 * @����3��int x - ��ʾ�������ϽǺ�����
	 * @����4��int y - ��ʾ�������Ͻ�������
	 * @����5��int w - ��ʾ����Ŀ�
	 * @����6��int h - ��ʾ����ĸ�
	 * @����ֵ��boolean false-ʧ��,true-�ɹ�
	 */
	public native boolean SetVideoDisplayArea(int CallSessionID, int index,
			int x, int y, int w, int h);

	/**
	 * @��������CaptureVideoImage
	 * @���ܣ���ָ��ID�Ự�н�����Ƶץͼһ�ţ�����ΪJPG��ʽ����
	 * @����1��int CallSessionID - ID
	 * @����2��int index - ������ʼ��Ϊ0
	 * @����3��byte[] ImageBuffer - ���������ݣ�������ImageBuffer�Ĵ�СӦ���ڵ��ڽ�����Ƶ(ͼ���*ͼ���/4)
	 * @����ֵ��0-ʧ��,��0-�ɹ�
	 */
	public native int CaptureVideoImage(int CallSessionID, int index,
			byte[] ImageBuffer);

	/**
	 * @��������SetAudioVolume
	 * @���ܣ�����ָ��ID�Ự��ͨ������
	 * @����1��int CallSessionID - ID
	 * @����2��int Percent - �����ٷֱ�
	 * @����ֵ��boolean false-ʧ��,true-�ɹ�
	 */
	public native boolean SetAudioVolume(int CallSessionID, int Percent);

	/**
	 * @��������SetTVoutDisplayArea
	 * @���ܣ����õ��������ģ�⸱�ֻ�����ʾ����
	 * @����1��int CallSessionID - ID
	 * @����2��int index - ������ʼ��Ϊ0
	 * @����3��int x - ��ʾ�������ϽǺ�����
	 * @����4��int y - ��ʾ�������Ͻ�������
	 * @����5��int w - ��ʾ����Ŀ�
	 * @����6��int h - ��ʾ����ĸ�
	 * @����ֵ��boolean false-ʧ��,true-�ɹ�
	 */
	public native boolean SetTVoutDisplayArea(int CallSessionID, int index,
			int x, int y, int w, int h);

	/**
	 * @��������Transfer
	 * @���ܣ�����ת�� ���º������ʱ,�������������Ҫת��ʱ,���ô˺�������ת�Ƶĺ���.Ȼ�����HangUp�������Ҷϴ˻Ự
	 * @����1��int CallSessionID - ID
	 * @����2��String TransferCode - ת�Ʒ���("3010101010101")
	 * @��ע�����жԷ�ʱ,����Է���������ת����Ϣʱ,���п���лص���ϢMSG_CALLDISTRACT֪ͨӦ�ó�������Ӧ����
	 * @����ֵ��boolean false-ʧ��,true-�ɹ�
	 */
	public native boolean Transfer(int CallSessionID, String TransferCode);

	/**
	 * @��������SetLocalVideoVisable
	 * @���ܣ����Ʊ�����Ƶ�Ƿ�Է��ɼ� ���ڵ���SetVideoDisplayAreaǰһ�е������������ó�ʼĬ��ֵ��Ȼ���û�����Ƶ���ذ�ťʱҲ����
	 * @����1��int CallSessionID - ID
	 * @����2��int index - ������ʼ��Ϊ0
	 * @����3��boolean enable - ������ʼ��Ϊ0
	 * @����ֵ��boolean false-ʧ��,true-�ɹ�
	 */
	public native boolean SetLocalVideoVisable(int CallSessionID, int index,
			boolean enable);

	/**
	 * @��������SetRelayMode
	 * @���ܣ�����ת�� ���յ���(����Ƶ)����ת��ָ��IP�Ͷ˿�
	 * @����1��int CallSessionID - ID
	 * @����2��boolean enable - ������ʼ��Ϊtrue
	 * @����3��int LocalPort - ������ǰ�������ݵĶ˿�
	 * @����4��String RemoteIP - �Է�IP
	 * @����5��int RemotePort - �Է��˿ں�
	 * @����ֵ��boolean false-ʧ��,true-�ɹ�
	 */
	public native boolean SetRelayMode(int CallSessionID, boolean enable,
			int LocalPort, String RemoteIP, int RemotePort);

	/**
	 * @��������SetFilePlayMode
	 * @���ܣ�����Ӧ������ ���ڻỰ���������
	 * @����1��int CallSessionID - ID
	 * @����2��boolean enable - ������ʼ��Ϊtrue
	 * @����3��String FileName - ���ŵ���Ƶ�ļ�����·��
	 * @����ֵ��boolean false-ʧ��,true-�ɹ�
	 */
	public native boolean SetFilePlayMode(int CallSessionID, boolean enable,
			String FileName);

	/**
	 * @��������SetFileRecordMode
	 * @���ܣ�¼�Ʒÿ����� ���ڻỰ���������
	 * @����1��int CallSessionID - ID
	 * @����2��boolean enable - ������ʼ��Ϊtrue
	 * @����3��String FileName - ¼�Ƶ���Ƶ�ļ�����·��
	 * @����ֵ��boolean false-ʧ��,true-�ɹ�
	 */
	public native boolean SetFileRecordMode(int CallSessionID, boolean enable,
			String FileName);

	/**
	 * @��������MessageCallback
	 * @������JNI�ص����� �յ���Ϣ�����ڷ������д�����Ϣ(��������,�������finish()���ղ���)
	 * @���ܣ��Խ���Ϣ֪ͨ
	 * @����1��int CallSessionID - ID
	 * @����2��int MsgType - ��Ϣ����
	 * @����3��String MsgContent - ��Ϣ����
	 * @����ֵ��0
	 */
	private int MessageCallback(int CallSessionID, int MsgType,
			String MsgContent) {
		MyLog.print("MessageCallback SessionID = " + CallSessionID
				+ ",MsgType = " + MsgType + ",MsgContent = " + MsgContent);
		Intent intent = new Intent();
		intent.setAction(CallReceiver.CALL_ACTION);
		intent.putExtra("SessionID", CallSessionID);
		intent.putExtra("MsgType", MsgType);
		intent.putExtra("MsgContent", MsgContent);
		if (mContext != null) {
			mContext.sendBroadcast(intent);
		} else {
			MyLog.print("JniPhoneClass", "mContext is null");
		}
		return 0;
	}

	/** @�ֻ��Ҷ� */
	public static final int MSG_PHONE_HANGUP = 1988;
	/** @�ֻ����� */
	public static final int MSG_PHONE_ACCEPT = 1989;
	
	/** @�������峬ʱ */
	public static final int MSG_RING_TIMEOUT = 1990;
	/** @ͨ����ʱ */
	public static final int MSG_TALK_TIMEOUT = 1991;
	/** @���ӳ�ʱ */
	public static final int MSG_MONITOR_TIMEOUT = 1992;

	/** @������� ���й��� */
	public static final int CALLACK_HOLD = 1996;
	/** @������� û��ý�� */
	public static final int CALLACK_NOMEDIA = 1997;
	/** @������� �Է�ռ�� */
	public static final int CALLACK_BUSY = 1998;
	
	/** @JNI�ص���Ϣ �Է����� */
	public static final int CALLACK_RING = 1999;
	/** @JNI�ص���Ϣ ����������� */
	public static final int MSG_CALLACK = 2000;
	/** @JNI�ص���Ϣ ����ת�� */
	public static final int MSG_CALLDISTRACT = 2001;
	/** @JNI�ص���Ϣ �º��� */
	public static final int MSG_CALLIN = 2002;
	/** @JNI�ص���Ϣ �Է��Ҷ� */
	public static final int MSG_REMOTE_HANGUP = 2003;
	/** @JNI�ص���Ϣ �Է����� */
	public static final int MSG_REMOTE_ACCEPT = 2004;
	/** @JNI�ص���Ϣ �Է����� */
	public static final int MSG_REMOTE_HOLD = 2005;
	/** @JNI�ص���Ϣ �Է����� */
	public static final int MSG_REMOTEWAKE = 2006;
	/** @JNI�ص���Ϣ ���г��� */
	public static final int MSG_ERROR = 2007;
	/** @JNI�ص���Ϣ ���� */
	public static final int MSG_MESSAGE = 2008;
	/** @JNI�ص���Ϣ ���� */
	public static final int MSG_MESSAGE_ERROR = 2009;

	/** @����������� ռ�� */
	public static final String CALL_BUSY = "busy";
	/** @����������� û��ý�� */
	public static final String CALL_NOMEDIA = "nomedia";
	/** @JNI����������� �Է����� */
	public static final String CALL_RING = "ring";
	/** @JNI����������� ���й��� */
	public static final String CALL_HOLD = "hold";

	static {
		System.loadLibrary("PhoneCore");
	}
}

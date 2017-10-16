package com.dpower.cloudintercom;

public class Constant {
	/** �ֻ����� */
	public static final String PHONE_TYPE_UNLOCK = "01";
	
	/** ���ֻ� */
	public static final String PHONE_TYPE_BIND = "03";
	
	/** ���ÿ������� */
	public static final String PHONE_TYPE_SETPWD = "04";
	
	/** ���÷ÿͿ������� */
	public static final String PHONE_TYPE_VISITORPWD = "05";
	
	/** ת��С����Ϣ */
	public static final String TURN_MSG_PHONE = "05";
	
	/** �ֻ����ùر��ƶԽ� */
	public static final String PHONE_CLOUD_CLOSE = "06";
	
	/** �ֻ�������ڻ� */
	public static final String PHONE_CLOUD_UNBIND = "07";
	
	/** Android�豸���� */
	public static final String PUSH_AND_TOKEN = "1";
	
	/** IOS�豸���� */
	public static final String PUSH_IOS_TOKEN = "2";
	
	/** ��ϢStatus״̬1 */
	public static final String MSG_STATUS_ONE = "1";
	
	/** ��ϢStatus״̬2 */
	public static final String MSG_STATUS_TWO = "2";
	
	/** �˺Ų�����  */
	public static final String OFFLINE = "Temporarily Unavailable";
	
	/** �˺�û���ҵ�,������  */
	public static final String NOT_FOUND = "Not Found";
	
	/** ����ʱ  */
	public static final String REQUEST_TIMEOUT = "Request Timeout";
	
	/** ���ӱ�����  */
	public static final String RESET_BY_PEER = "Connection reset by peer";
	
	/** �ܽ�  */
	public static final String DECLINE = "Decline";
	
	/** �����Ҷ�  */
	public static final String NORMAL_CALL_END = "Normal call clearing";
	
	/** ת��UTF-8 */
	public static final String CHARSET = "UTF-8";
	
	/** �������ڻ� */
	public static final int NEW_CALL = 101;
	
	/** ���ڻ������ֻ� */
	public static final int CALL_OUT = 102;
	
	/** ���ڻ��Ҷ� */
	public static final int HANGUP = 103;
	
	/** �ƶԽ�SIP��¼ */
	public static final int LOGIN = 104;
	
	/** �ƶԽ�SIP�˳� */
	public static final int SIP_LOGOUT = 105;
	
	/** SIP������IP */
	//public static final String SIP_SERVER_IP = "192.168.10.10";// QuHwa
	public static final String SIP_SERVER_IP = "120.25.126.228";
	
	/** SIP�������˿� */
	public static final int SIP_SERVER_PORT = 5060; // ��½�������˿�

	/** �������˿� */
	public static final int SERVER_PORT = 8080; // ��½�������˿�

	/** ������IP�Ͷ˿� */
	public static final String IP_PORT = "http://" + SIP_SERVER_IP + ":"
			+ SERVER_PORT;
	
	/** ���ڻ�SIPע�� */
	public static final String REG_SIP_URL = IP_PORT
			+ "/smarthomeservice/rest/device/login";

	/** ������Ϣ��Android */
	public static final String PUSH_AND_URL = IP_PORT
			+ "/smarthomeservice/rest/push/client/pushToAndroidClientIntercom";

	/** ������Ϣ��IOS */
	public static final String PUSH_IOS_URL = IP_PORT
			+ "/smarthomeservice/rest/push/client/pushToIosClientIntercom";
	
	/** �ϴ����ż�¼�������� */
	public static final String UPLOAD_OPEN_DOOR_RECORD = IP_PORT
			+ "/smarthomeservice/rest/unlock/add";
	
	/** �ϴ�С����Ϣ�������� */
	public static final String UPLOAD_NOTICE_INFO = IP_PORT
			+ "/smarthomeservice/rest/notice/add";
}

package com.dpower.cloudintercom;

public class Constant {
	/** 手机开锁 */
	public static final String PHONE_TYPE_UNLOCK = "01";
	
	/** 绑定手机 */
	public static final String PHONE_TYPE_BIND = "03";
	
	/** 设置开锁密码 */
	public static final String PHONE_TYPE_SETPWD = "04";
	
	/** 设置访客开锁密码 */
	public static final String PHONE_TYPE_VISITORPWD = "05";
	
	/** 转发小区消息 */
	public static final String TURN_MSG_PHONE = "05";
	
	/** 手机设置关闭云对讲 */
	public static final String PHONE_CLOUD_CLOSE = "06";
	
	/** 手机解绑室内机 */
	public static final String PHONE_CLOUD_UNBIND = "07";
	
	/** Android设备类型 */
	public static final String PUSH_AND_TOKEN = "1";
	
	/** IOS设备类型 */
	public static final String PUSH_IOS_TOKEN = "2";
	
	/** 消息Status状态1 */
	public static final String MSG_STATUS_ONE = "1";
	
	/** 消息Status状态2 */
	public static final String MSG_STATUS_TWO = "2";
	
	/** 账号不在线  */
	public static final String OFFLINE = "Temporarily Unavailable";
	
	/** 账号没有找到,不在线  */
	public static final String NOT_FOUND = "Not Found";
	
	/** 请求超时  */
	public static final String REQUEST_TIMEOUT = "Request Timeout";
	
	/** 连接被重置  */
	public static final String RESET_BY_PEER = "Connection reset by peer";
	
	/** 拒接  */
	public static final String DECLINE = "Decline";
	
	/** 正常挂断  */
	public static final String NORMAL_CALL_END = "Normal call clearing";
	
	/** 转码UTF-8 */
	public static final String CHARSET = "UTF-8";
	
	/** 监视室内机 */
	public static final int NEW_CALL = 101;
	
	/** 室内机呼叫手机 */
	public static final int CALL_OUT = 102;
	
	/** 室内机挂断 */
	public static final int HANGUP = 103;
	
	/** 云对讲SIP登录 */
	public static final int LOGIN = 104;
	
	/** 云对讲SIP退出 */
	public static final int SIP_LOGOUT = 105;
	
	/** SIP服务器IP */
	//public static final String SIP_SERVER_IP = "192.168.10.10";// QuHwa
	public static final String SIP_SERVER_IP = "120.25.126.228";
	
	/** SIP服务器端口 */
	public static final int SIP_SERVER_PORT = 5060; // 登陆服务器端口

	/** 服务器端口 */
	public static final int SERVER_PORT = 8080; // 登陆服务器端口

	/** 服务器IP和端口 */
	public static final String IP_PORT = "http://" + SIP_SERVER_IP + ":"
			+ SERVER_PORT;
	
	/** 室内机SIP注册 */
	public static final String REG_SIP_URL = IP_PORT
			+ "/smarthomeservice/rest/device/login";

	/** 推送消息给Android */
	public static final String PUSH_AND_URL = IP_PORT
			+ "/smarthomeservice/rest/push/client/pushToAndroidClientIntercom";

	/** 推送消息给IOS */
	public static final String PUSH_IOS_URL = IP_PORT
			+ "/smarthomeservice/rest/push/client/pushToIosClientIntercom";
	
	/** 上传开门记录到服务器 */
	public static final String UPLOAD_OPEN_DOOR_RECORD = IP_PORT
			+ "/smarthomeservice/rest/unlock/add";
	
	/** 上传小区消息到服务器 */
	public static final String UPLOAD_NOTICE_INFO = IP_PORT
			+ "/smarthomeservice/rest/notice/add";
}

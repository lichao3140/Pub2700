package com.dpower.cloudmessage;
/**
 * 处理与message server通信的内容
 * @author Arlene
 * 2016/10/13
 */
public interface MessageCallback {
	
	/**
	 * 登录结果 
	 * @param loginStatus 登录结果 0-成功，非0-失败
	 * @param reason 失败的原因
	 */
	abstract void loginResult(int loginStatus, String reason);
	
	/**
	 * 收到手机的消息
	 * @param msg 消息内容
	 */
	abstract void receivedMessage(String msg);
}

package com.dpower.cloudmessage;
/**
 * ������message serverͨ�ŵ�����
 * @author Arlene
 * 2016/10/13
 */
public interface MessageCallback {
	
	/**
	 * ��¼��� 
	 * @param loginStatus ��¼��� 0-�ɹ�����0-ʧ��
	 * @param reason ʧ�ܵ�ԭ��
	 */
	abstract void loginResult(int loginStatus, String reason);
	
	/**
	 * �յ��ֻ�����Ϣ
	 * @param msg ��Ϣ����
	 */
	abstract void receivedMessage(String msg);
}

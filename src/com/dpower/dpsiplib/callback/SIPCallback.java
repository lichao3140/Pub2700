package com.dpower.dpsiplib.callback;

public interface SIPCallback {
	
	/** �յ��Է��ĺ���
	 * @param remoteAccount ���з����ʺ�
	 */
	public void newCall(int sessionID, String remoteAccount);
	
	/** ͨ�������ɹ�����ʼͨ�� */
	public void callStart(int sessionID);
	
	/** ͨ���Ͽ����� 
	 * @param reason �Ҷ�ԭ��
	 */
	public void callEnd(int sessionID, String reason);
	
	/**
	 * ͨ����������ʱ��ǰ״̬
	 */
	public void callState(int sessionID, String state);
	
	/**
	 * �ͻ�����sip����״̬�ı�
	 * @param isConnected �Ƿ���sip��������������
	 * @param reason ����ԭ��
	 */
	public void sipConnectChange(boolean isConnected, String reason);
	
	/** ����������� */
	public void sipServiceStart();
	
	/** ������ֹͣ */
	public void sipServiceStop(); 
	
	/** ���������� */
	public void noNetworkConnection();
}

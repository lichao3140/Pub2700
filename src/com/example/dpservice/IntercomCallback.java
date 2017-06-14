package com.example.dpservice;

/**
 * ���ȫ���ص�
 * @author Administrator
 *
 */
public interface IntercomCallback {
	
	/** �������峬ʱ */
	public void onRingTimeOut(int CallSessionID, int MsgType, String MsgContent);
	/** ͨ����ʱ */
	public void onTalkTimeOut(int CallSessionID, int MsgType, String MsgContent);
	/** ���ӳ�ʱ */
	public void onMonitorTimeOut(int CallSessionID, int MsgType, String MsgContent);
	/** �Է����� */
	public void onAckRing(int CallSessionID, int MsgType, String MsgContent);
	/** �Է�ռ�� */
	public void onAckBusy(int CallSessionID, int MsgType, String MsgContent);
	/** û��ý�� */
	public void onAckNoMeia(int CallSessionID, int MsgType, String MsgContent);
	/** ���й��� */
	public void onAckHold(int CallSessionID, int MsgType, String MsgContent);
	/** ����������� */
	public void onCallOutAck(int CallSessionID, int MsgType, String MsgContent);
	/** �º��� */
	public void onNewCallIn(int CallSessionID, int MsgType, String MsgContent);
	/** �Է��Ҷ� */
	public void onRemoteHangUp(int CallSessionID, int MsgType, String MsgContent);
	/** �Է����� */
	public void onRemoteAccept(int CallSessionID, int MsgType, String MsgContent);
	/** �Է����� */
	public void onRemoteHold(int CallSessionID, int MsgType, String MsgContent);
	/** �Է����� */
	public void onRemoteWake(int CallSessionID, int MsgType, String MsgContent);
	/** ���г��� */
	public void onError(int CallSessionID, int MsgType, String MsgContent);
	/** ���� */
	public void onMessage(int CallSessionID, int MsgType, String MsgContent);
	/** ���� */
	public void onMessageError(int CallSessionID, int MsgType, String MsgContent);

	/** �ֻ����� */
	public void onPhoneAccept();
	/** �ֻ��Ҷ� */
	public void onPhoneHangUp();
}

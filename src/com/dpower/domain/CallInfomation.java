package com.dpower.domain;

import com.dpower.util.MyLog;

/**
 * @������CallInfomation
 * @��������¼DP¥��Ự����Ϣ
 * @��ע���ỰID���Ự�Է����š��Ự����������ʱ�䡢����ʱ�䡢�Ҷ�ʱ�䡢�Ƿ�����
 */
public class CallInfomation {
	
	/** �����ѽ��� */
	public static final int CALL_OUT_ACCEPT = 1 << 0;
	/** ����δ���� */
	public static final int CALL_OUT_UNACCEPT = 1 << 1;
	/** �����ѽ��� */
	public static final int CALL_IN_ACCEPT = 1 << 2;
	/** ����δ���� */
	public static final int CALL_IN_UNACCEPT = 1 << 3;
	/** �����ѽ��� */
	public static final int SEE_ACCEPT = 1 << 4;
	/** ����δ���� */
	public static final int SEE_UNACCEPT = 1 << 5;
	
	private int mDB_id = 0;
	private int mSessionID = 0;
	private String mRemoteCode = null;
	private long mStartTime = 0;
	private long mAcceptTime = 0;
	private long mEndTime = 0;
	private int mType = 0;
	private boolean mIsRead = false;
	private boolean mIsOpenLock = false;
	private boolean mIsHangUp = true;
	private boolean mIsSuccess = false; //�Ƿ�ɹ��ϱ���������

	public CallInfomation() {
		
	}

	public CallInfomation(int db_id, String remoteCode, int type, long startTime,
			long acceptTime, long endTime, boolean isOpenLock, boolean isRead) {
		mDB_id = db_id;
		mSessionID = 0;
		mRemoteCode = remoteCode;
		mStartTime = startTime;
		mAcceptTime = acceptTime;
		mEndTime = endTime;
		mType = type;
		mIsRead = isRead;
		mIsOpenLock = isOpenLock;
		mIsHangUp = true;
	}

	/** �ж��Ƿ�Ϊ�ſڻ� */
	public boolean isDoor() {
		if (mRemoteCode != null) {
			int type = Integer.valueOf(new String(mRemoteCode.substring(0, 1)));
			return type == 2 || type == 3 || type == 7;
		}
		MyLog.print("isDoor error, remotecode is null");
		return false;
	}
	
	/**�ж��ǲ��ǹ�������*/
	public boolean isManageCenter(){
		return (mRemoteCode.substring(0, 1).equals("8"));
	}

	public int getSessionID() {
		return mSessionID;
	}

	public void setSessionID(int sessionID) {
		mSessionID = sessionID;
	}

	public long getStartTime() {
		return mStartTime;
	}

	public void setStartTime() {
		mStartTime = System.currentTimeMillis();
	}

	public long getAcceptTime() {
		return mAcceptTime;
	}

	public void setAcceptTime() {
		mAcceptTime = System.currentTimeMillis();
	}

	public long getEndTime() {
		return mEndTime;
	}

	public void setEndTime() {
		mEndTime = System.currentTimeMillis();
	}

	public int getType() {
		return mType;
	}

	public void setType(int type) {
		mType = type;
	}

	public boolean isRead() {
		return mIsRead;
	}

	public void setIsRead(boolean isRead) {
		mIsRead = isRead;
	}

	public String getRemoteCode() {
		return mRemoteCode;
	}

	public void setRemoteCode(String remoteCode) {
		mRemoteCode = remoteCode;
	}

	public boolean isOpenLock() {
		return mIsOpenLock;
	}

	public void setIsOpenLock(boolean isOpenLock) {
		mIsOpenLock = isOpenLock;
	}

	public boolean isHangUp() {
		return mIsHangUp;
	}

	public void setIsHangUp(boolean isHangUp) {
		mIsHangUp = isHangUp;
	}

	public int getDb_id() {
		return mDB_id;
	}

	public void setDb_id(int db_id) {
		mDB_id = db_id;
	}
	
	public boolean getIsSuccess() {
		return mIsSuccess;
	}
	
	public void setIsSuccess(boolean isSuccess) {
		mIsSuccess = isSuccess;
	}

	public String toString() {
		return "CallInfo:db_id=" + getDb_id() + ",sessionID=" + getSessionID()
				+ ",code=" + getRemoteCode() + ",type=" + getType()
				+ ",startTime=" + getStartTime() + ",acceptTime="
				+ getAcceptTime() + ",endTime=" + getEndTime() + ",isRead="
				+ isRead() + ",isOpenLock=" + isOpenLock() + ",isHangUp="
				+ isHangUp();
	}
}

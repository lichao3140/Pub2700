package com.dpower.cloudintercom;

import java.util.List;

import com.dpower.domain.CallInfomation;
import com.dpower.pub.dp2700.model.IndoorSipInfo;

public interface CloudIntercomCallback {
	
	/** ��ȡ���ط��� */
	public String getRoomCode();
	
	/** ��ȡ��Ӱ���Լ�¼�б� */
	public String getVisitRecord();
	
	/** ��ȡָ����Ӱ������Ϣ */
	public String getVisitFileData(int id);
	
	/** ��ȡ������¼�б� */
	public String getAlarmLog();
	
	/** ��ȡС����Ϣ�б� */
	public String getMessageLog();
	
	/** ��ȡָ��С����Ϣ */
	public String[] getMessageFileData(int id);
	
	/** ��ȡ����ģʽ */
	public String getSafeMode();
	
	/** ��ȡ���ܼҾ�ģʽ */
	public String getSmartHomeMode();
	
	/** ��ȡ������� */
	public int getCallInSize();
	
	/** ��ȡ�������� */
	public int getCallOutSize();
	
	/**
	 * �޸İ���ģʽ
	 * @param isAuto true-���ڻ��Զ�ͬ������ģʽ, false-�ֶ��޸İ���ģʽ
	 */
	public void changeSafeMode(int model, boolean isAuto);
	
	/** �޸����ܼҾ�ģʽ */
	public void changeSmartHomeMode(int model);
	
	/** ���� */
	public boolean openLock();
	
	/** ���� */
	public void accept();
	
	/** �Ҷ� */
	public void hangUp(int sessionID);
	
	/** sip��¼״̬�ı� */
	public void sipConnectChange(boolean isConnected, String reason);
	
	/** msg server��¼״̬�ı� */
	public void loginResult(int loginStatus, String reason);
	
	/**
	 * �ֻ��󶨻��߽���豸
	 * @param isBind true-��, false-���
	 */
	public void onBindTel(String tel, boolean isBind);
	
	/** ��ȡ�˺��б� */
	public List<String> getAccountList();
	
	/** ��ȡ���ڻ�SIP�˺��б� */
	public List<String> getIndoorSipList();
	
	/** �ж�SIP�˺��Ƿ����  */
	public boolean isIndoorSipExist(String sipid);
	
	/** ����˺� */
	public int addAccount(String account, String token, String mobiletype);
	
	/** ɾ���˺� */
	public void deleteAccount(String account);
	
	/** ����˺� */
	public void clearAccount();
	
	/** �ҳ���ͬ���͵����� */
	public int getTokensCount(String tokentype);
	
	/** �ж�Token�˺��Ƿ��Ѿ����� */
	public boolean isTokenExist(String token);
	
	/** ��ѯͨ����¼���һ������ */
	public CallInfomation quaryLastCall();
	
	/** �������ڻ�SIP��Ϣ�����ݿ� */
	public void addIndoorSip(IndoorSipInfo info);
	
	/** �������ڻ�SIP��Ϣ */
	public void modifyIndoorSip(IndoorSipInfo info);
	
	/** ����phoneType��ȡToken�б� */
	public List<String> getTokenByTypeList(String type);
	
	/** ��ѯ���ڻ�SIP�˺ŵ����� */
	public int countIndoorSip();
	
	/** ��ѯ���ڻ�SIP�˺ŵĵ�һ�� */
	public IndoorSipInfo queryFistSip();
	
	/** �����˺����� */
	public void setAccountOnline(String account, boolean online);
	
	/** ����account��token����ֻ� */
	public void delAccountByToken(String account, String token);
}

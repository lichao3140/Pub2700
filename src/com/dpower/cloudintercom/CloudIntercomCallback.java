package com.dpower.cloudintercom;

import java.util.List;

import com.dpower.domain.CallInfomation;
import com.dpower.pub.dp2700.model.IndoorSipInfo;

public interface CloudIntercomCallback {
	
	/** 获取本地房号 */
	public String getRoomCode();
	
	/** 获取留影留言记录列表 */
	public String getVisitRecord();
	
	/** 获取指定留影留言信息 */
	public String getVisitFileData(int id);
	
	/** 获取报警记录列表 */
	public String getAlarmLog();
	
	/** 获取小区信息列表 */
	public String getMessageLog();
	
	/** 获取指定小区信息 */
	public String[] getMessageFileData(int id);
	
	/** 获取安防模式 */
	public String getSafeMode();
	
	/** 获取智能家居模式 */
	public String getSmartHomeMode();
	
	/** 获取呼入个数 */
	public int getCallInSize();
	
	/** 获取呼出个数 */
	public int getCallOutSize();
	
	/**
	 * 修改安防模式
	 * @param isAuto true-室内机自动同步安防模式, false-手动修改安防模式
	 */
	public void changeSafeMode(int model, boolean isAuto);
	
	/** 修改智能家居模式 */
	public void changeSmartHomeMode(int model);
	
	/** 开锁 */
	public boolean openLock();
	
	/** 接听 */
	public void accept();
	
	/** 挂断 */
	public void hangUp(int sessionID);
	
	/** sip登录状态改变 */
	public void sipConnectChange(boolean isConnected, String reason);
	
	/** msg server登录状态改变 */
	public void loginResult(int loginStatus, String reason);
	
	/**
	 * 手机绑定或者解绑设备
	 * @param isBind true-绑定, false-解绑
	 */
	public void onBindTel(String tel, boolean isBind);
	
	/** 获取账号列表 */
	public List<String> getAccountList();
	
	/** 获取室内机SIP账号列表 */
	public List<String> getIndoorSipList();
	
	/** 判断SIP账号是否存在  */
	public boolean isIndoorSipExist(String sipid);
	
	/** 添加账号 */
	public int addAccount(String account, String token, String mobiletype);
	
	/** 删除账号 */
	public void deleteAccount(String account);
	
	/** 清空账号 */
	public void clearAccount();
	
	/** 找出不同类型的总数 */
	public int getTokensCount(String tokentype);
	
	/** 判断Token账号是否已经存在 */
	public boolean isTokenExist(String token);
	
	/** 查询通话记录最后一条数据 */
	public CallInfomation quaryLastCall();
	
	/** 增加室内机SIP信息到数据库 */
	public void addIndoorSip(IndoorSipInfo info);
	
	/** 更新室内机SIP信息 */
	public void modifyIndoorSip(IndoorSipInfo info);
	
	/** 根据phoneType获取Token列表 */
	public List<String> getTokenByTypeList(String type);
	
	/** 查询室内机SIP账号的条数 */
	public int countIndoorSip();
	
	/** 查询室内机SIP账号的第一条 */
	public IndoorSipInfo queryFistSip();
	
	/** 设置账号在线 */
	public void setAccountOnline(String account, boolean online);
	
	/** 根据account和token解绑手机 */
	public void delAccountByToken(String account, String token);
}

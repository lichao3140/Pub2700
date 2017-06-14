package com.dpower.dpsiplib.callback;

import com.dpower.dpsiplib.service.MyCall;

public interface SIPCallback {
	
	/** 收到对方的呼叫
	 * @param remoteAccount 呼叫方的帐号
	 */
	public void newCall(int sessionID, String remoteAccount);
	
	/** 通话建立成功，开始通话 */
	public void callStart(int sessionID);
	
	/** 通话断开连接 
	 * @param reason 挂断原因
	 */
	public void callEnd(int sessionID, String reason);
	
	/**
	 * 通话建立连接时当前状态
	 */
	public void callState(int sessionID, String state);
	
	/**
	 * 通话媒体流
	 * @param call
	 */
	public void callMediaState(MyCall call);
	
	/**
	 * 客户端与sip连接状态改变
	 * @param isConnected 是否与sip服务器保持连接
	 * @param reason 掉线原因
	 */
	public void sipConnectChange(boolean isConnected, String reason);
	
	/** 服务启动完毕 */
	public void sipServiceStart();
	
	/** 服务已停止 */
	public void sipServiceStop(); 
	
	/** 无网络连接 */
	public void noNetworkConnection();
}

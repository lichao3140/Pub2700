package com.dpower.pub.dp2700.service;

import org.pjsip.pjsua2.OnInstantMessageParam;

import android.util.Log;

import com.dpower.dpsiplib.service.DPSIPService;

/**
 * 消息接受服务
 * @author LiChao
 *
 */
public class MessageService extends DPSIPService {
	private static final String TAG = "MessageService";
	
	public static final int MESSAGE = 8;
	public static final int HANGUP = 9;
	public static final int UNLOCK = 10;
	public static final int MONITOR = 11;
	public static final int VOLUME = 12;

	@Override
	public void notifyMessageFromPhone(OnInstantMessageParam prm) {
		super.notifyMessageFromPhone(prm);
		Log.i(TAG, "Message body:" + prm.getMsgBody());
	}
	
}

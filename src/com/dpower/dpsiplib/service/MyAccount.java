package com.dpower.dpsiplib.service;

import java.util.ArrayList;
import java.util.Date;
import org.pjsip.pjsua2.Account;
import org.pjsip.pjsua2.AccountConfig;
import org.pjsip.pjsua2.BuddyConfig;
import org.pjsip.pjsua2.OnIncomingCallParam;
import org.pjsip.pjsua2.OnInstantMessageParam;
import org.pjsip.pjsua2.OnInstantMessageStatusParam;
import org.pjsip.pjsua2.OnRegStateParam;
import com.dpower.dpsiplib.utils.SIPIntercomLog;

public class MyAccount extends Account {
	
	public ArrayList<MyBuddy> buddyList = new ArrayList<MyBuddy>();
	public AccountConfig cfg;

	public MyAccount(AccountConfig config) {
		super();
		cfg = config;
	}

	public MyBuddy addBuddy(BuddyConfig bud_cfg) {
		/* Create Buddy */
		SIPIntercomLog.print("addBuddy .......");
		MyBuddy bud = new MyBuddy(bud_cfg);
		try {
			bud.create(this, bud_cfg);
		} catch (Exception e) {
			bud.delete();
			bud = null;
		}

		if (bud != null) {
			buddyList.add(bud);
			if (bud_cfg.getSubscribe()) {
				try {
					bud.subscribePresence(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
				
		}
		return bud;
	}

	public void delBuddy(MyBuddy buddy) {
		buddyList.remove(buddy);
		buddy.delete();
	}

	public void delBuddy(int index) {
		MyBuddy bud = buddyList.get(index);
		buddyList.remove(index);
		bud.delete();
	}

	@Override
	public void onRegState(OnRegStateParam prm) {
		if (MyApp.callback != null) {
			MyApp.callback.notifyRegState(prm.getCode(), prm.getReason(),
					prm.getExpiration());
		}
	}

	@Override
	public void onIncomingCall(OnIncomingCallParam prm) {
		MyCall call = new MyCall(this, prm.getCallId());
		if (MyApp.callback != null) {
			MyApp.callback.notifyIncomingCall(call);
		}
	}

	@Override
	public void onInstantMessage(OnInstantMessageParam prm) {
		MyApp.callback.notifyMessageFromPhone(prm);
		SIPIntercomLog.print("======== onInstantMessage ======== ");
		SIPIntercomLog.print("From     : " + prm.getFromUri());
		SIPIntercomLog.print("To       : " + prm.getToUri());
		SIPIntercomLog.print("Rdata    : " + prm.getRdata());
		SIPIntercomLog.print("Contact  : " + prm.getContactUri());
		SIPIntercomLog.print("Mimetype : " + prm.getContentType());
		SIPIntercomLog.print("Body     : " + prm.getMsgBody());
		SIPIntercomLog.print(SIPIntercomLog.ERROR, "---------------Now time " + new Date().getTime());
		SIPIntercomLog.print(SIPIntercomLog.ERROR, "onInstantMessage body len=" + prm.getMsgBody().length());
	}
	
	@Override
	public void onInstantMessageStatus(OnInstantMessageStatusParam prm) {
		super.onInstantMessageStatus(prm);
		MyApp.callback.notifyMessageFromPhoneStatus(prm);
		SIPIntercomLog.print("======== onInstantMessageStatus ======== ");
		SIPIntercomLog.print("To       : " + prm.getToUri());
		SIPIntercomLog.print("Reason   : " + prm.getReason());
		SIPIntercomLog.print("Code     : " + prm.getCode());
		SIPIntercomLog.print("Body     : " + prm.getMsgBody());
		SIPIntercomLog.print("Rdata    : " + prm.getRdata());
		SIPIntercomLog.print("UserData : " + prm.getUserData());
	}
}

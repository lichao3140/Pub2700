package com.dpower.dpsiplib.callback;

import org.pjsip.pjsua2.OnInstantMessageParam;
import org.pjsip.pjsua2.OnInstantMessageStatusParam;
import org.pjsip.pjsua2.pjsip_status_code;

import com.dpower.dpsiplib.service.MyBuddy;
import com.dpower.dpsiplib.service.MyCall;

public interface MyAppCallback {
	
	abstract void notifyRegState(pjsip_status_code code, String reason,
			int expiration);

	abstract void notifyIncomingCall(MyCall call);

	abstract void notifyCallState(MyCall call);

	abstract void notifyCallMediaState(MyCall call);

	abstract void notifyBuddyState(MyBuddy buddy);
	
	abstract void notifyMessageFromPhone(OnInstantMessageParam prm);
	
	abstract void notifyMessageFromPhoneStatus(OnInstantMessageStatusParam prm);
}

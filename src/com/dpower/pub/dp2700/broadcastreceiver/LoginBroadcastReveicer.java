package com.dpower.pub.dp2700.broadcastreceiver;

import com.dpower.pub.dp2700.service.LoginService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LoginBroadcastReveicer extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent login = new Intent(context, LoginService.class);
		context.startService(login);
	}

}

package com.dpower.pub.dp2700.broadcastreceiver;

import com.dpower.function.DPFunction;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmFinishCallBroadcast extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (DPFunction.ACTION_ALARMING.equals(intent.getAction())) {
			int i = intent.getIntExtra("alarm", 0);
			if (i == -1) {
				
			} else {
				((Activity) context).finish();
			}
		}
	}
}

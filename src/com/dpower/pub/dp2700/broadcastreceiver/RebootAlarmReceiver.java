package com.dpower.pub.dp2700.broadcastreceiver;

import java.util.Calendar;

import com.dpower.pub.dp2700.activity.RebootActivity;
import com.dpower.pub.dp2700.tools.RebootUT;
import com.dpower.util.MyLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author ZhengZhiying 2015-4-7 14:14:28
 * @Funtion ���յ��㲥������
 */
public class RebootAlarmReceiver extends BroadcastReceiver {
	private static final String TAG = "RebootAlarmReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		MyLog.print(TAG, "���յ��㲥");
		String msg = intent.getExtras().getString("reboot");
		if (msg != null) {
			Calendar cal = Calendar.getInstance();
			if (cal.get(Calendar.HOUR_OF_DAY) == RebootUT.REBOOT_TIME) {
				if (cal.get(Calendar.MINUTE) == 0 ) {
					Log.i(TAG, "���յ������Ķ�ʱ�㲥");
					Intent i = new Intent();
					i.setClass(context, RebootActivity.class);
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(i);
				}
			}
		} else {
			MyLog.print(TAG, "���յ��Ĺ㲥��������");
		}
	}
}

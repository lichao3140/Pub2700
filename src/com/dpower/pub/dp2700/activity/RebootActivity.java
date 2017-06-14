package com.dpower.pub.dp2700.activity;

import java.io.IOException;
import java.util.Calendar;

import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.tools.RebootUT;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

/**
 * @author ZhengZhiying
 * @Funtion 显示正在清理，并且做重启操作
 */
public class RebootActivity extends BaseActivity {

	private TextView mTime;
	private boolean mIsCancel = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reboot);
		mTime = (TextView) findViewById(R.id.tv_time);
		Calendar cal = Calendar.getInstance();
		mTime.setText("" + cal.get(Calendar.YEAR));
		mTime.append("-");
		mTime.append("" + cal.get(Calendar.MONTH));
		mTime.append("-");
		mTime.append("" + cal.get(Calendar.DAY_OF_MONTH));
		mTime.append(" " + cal.get(Calendar.HOUR_OF_DAY));
		mTime.append(":" + cal.get(Calendar.MINUTE));
		mTime.append(":" + cal.get(Calendar.SECOND));
		mTime.append(" " + getString(R.string.reboot_cancal));
		mTime.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mIsCancel = true;
				finish();
			}
		});
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				if (mIsCancel) {
					// no thing to do 
				} else if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) 
						== RebootUT.REBOOT_TIME) {
					reboot();
				} else {
					finish();
				}
			}
		}, 5000);
	}

	private void reboot() {
		// try{
		// Log.v("RebootAlarmReceiver", "root Runtime->shutdown");
		// //Process proc =Runtime.getRuntime().exec(new
		// String[]{"su","-c","shutdown"}); //关机
		// Process proc =Runtime.getRuntime().exec(new
		// String[]{"su","-c","reboot -p"}); //关机
		// proc.waitFor();
		// }catch(Exception e){
		// e.printStackTrace();
		// }

		String cmd = "su -c reboot";
		try {
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

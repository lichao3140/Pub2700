package com.dpower.pub.dp2700.activity.dialog;

import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.activity.BaseActivity;
import com.dpower.util.ConstConf;
import com.dpower.util.MyLog;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * 模式倒计时弹窗
 */
public class HomeSecurityModeDelayDialog extends BaseActivity {
	private static final String TAG = "HomeSecurityModeDelayDialog";
	
	private int mDelayTime;
	private Handler mTimeCountHandler;
	private Runnable mTimeCountRunnable;
	private MediaPlayer mMediaPlayer;
	private int mClickedMode;
	private Button mCancel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mode_delay);
		mDelayTime = DPFunction.getProtectionDelayTime();
		mDelayTime += 1;
		mClickedMode = getIntent().getIntExtra("modeflag",
				ConstConf.UNSAFE_MODE);
		mCancel = (Button) findViewById(R.id.btn_cancel);
		String string = null;
		switch (mClickedMode) {
			case ConstConf.HOME_MODE:
				string = getString(R.string.mode_at_home);
				break;
			case ConstConf.NIGHT_MODE:
				string = getString(R.string.mode_in_hight);
				break;
			case ConstConf.LEAVE_HOME_MODE:
				string = getString(R.string.mode_leave_home);
				break;
			case ConstConf.UNSAFE_MODE:
				string = getString(R.string.mode_out_of_security);
				mDelayTime = 1;
				break;
			default:
				break;
		}
		((TextView) findViewById(R.id.tv_mode_type)).setText(string);
		mCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mTimeCountHandler != null) {
					mTimeCountHandler.removeCallbacks(mTimeCountRunnable);
				}
				finish();
			}
		});
		delayCount();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mCancel != null) {
			mCancel.performClick();
		}
	}

	@Override
	protected void onDestroy() {
		if (mMediaPlayer != null) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
			}
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		super.onDestroy();
	}

	/**
	 * 倒计时函数,并根据倒计时播放不同的响铃
	 */
	public void delayCount() {
		mTimeCountHandler = new Handler();
		mTimeCountRunnable = new Runnable() {
			
			@Override
			public void run() {
				mTimeCountHandler.postDelayed(this, 1000);
				if (mDelayTime > 1) { // 延迟 最后1秒用于显示提示
					mDelayTime--;
					mCancel.setVisibility(View.VISIBLE);
					findViewById(R.id.btn_confirm).setVisibility(View.GONE);
					((TextView) findViewById(R.id.tv_delay_time))
							.setText(String.valueOf(mDelayTime));
					if (mMediaPlayer == null) {
						mMediaPlayer = MediaPlayer.create(
								HomeSecurityModeDelayDialog.this,
								R.raw.timeover15);
					}
					mMediaPlayer.setLooping(false);
					mMediaPlayer.start();

				} else if (mDelayTime > 0) { // 不延迟
					((TextView) findViewById(R.id.tv_mode_type))
							.setText(getString(R.string.tips_mode_has_started));
					((TextView) findViewById(R.id.tv_delay_time))
							.setVisibility(View.INVISIBLE);
					mCancel.setVisibility(View.GONE);
					findViewById(R.id.btn_confirm).setVisibility(View.VISIBLE);
					mDelayTime--;
				} else {
					switch (mClickedMode) {
						case ConstConf.HOME_MODE:
							DPFunction.changeSafeMode(ConstConf.HOME_MODE, false);
							break;
						case ConstConf.NIGHT_MODE:
							DPFunction.changeSafeMode(ConstConf.NIGHT_MODE, false);
							break;
						case ConstConf.LEAVE_HOME_MODE:
							DPFunction.changeSafeMode(ConstConf.LEAVE_HOME_MODE, false);
							break;
						case ConstConf.UNSAFE_MODE:
							if (DPFunction.getAlarming()) {
								DPFunction.disAlarm(false);
							} else {
								DPFunction.changeSafeMode(ConstConf.UNSAFE_MODE, false);
							}
							startActivity(new Intent(
									HomeSecurityModeDelayDialog.this,
									HomeSecurityCancelTipsDialog.class));
							break;
					}
					MyLog.print(TAG, "修改模式");
					// 更新布防模式开关
					mTimeCountHandler.removeCallbacks(mTimeCountRunnable);
					Intent intent = new Intent();
					intent.setAction(DPFunction.ACTION_SAFE_MODE);
					sendBroadcast(intent);
					finish();
				}
			}
		};
		mTimeCountHandler.post(mTimeCountRunnable);
	}
}

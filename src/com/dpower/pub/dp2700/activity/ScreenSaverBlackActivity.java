package com.dpower.pub.dp2700.activity;

import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.service.PhysicsKeyService;
import com.dpower.pub.dp2700.service.PhysicsKeyService.KeyCallback;
import com.dpower.pub.dp2700.tools.ScreenUT;
import com.dpower.util.MyLog;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

/**
 * ºÚÆÁÆÁ±£
 */
public class ScreenSaverBlackActivity extends BaseActivity {
	private static final String TAG = "ScreenSaverBlackActivity";
	
	private LinearLayout mLayout;
	private boolean[] mKeySwitch;
	
	private KeyCallback mKeyCallback = new KeyCallback() {

		@Override
		public void onKey(int keyIO) {
			switch (keyIO) {
				case PhysicsKeyService.MESSAGE:
				case PhysicsKeyService.VOLUME:
				case PhysicsKeyService.MONITOR:
				case PhysicsKeyService.UNLOCK:
				case PhysicsKeyService.HANGUP:
					ScreenUT.getInstance().wakeUpScreen();
					break;
				default:
					break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_screen_saver_black);
		MyLog.print(TAG, "onCreate");
		mLayout = (LinearLayout) findViewById(R.id.screen_saver_black);
		mLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		PhysicsKeyService.registerKeyCallback(mKeyCallback);
	}
	
	@Override
	protected void onStart() {
		MyLog.print(TAG, "onStart");
		super.onStart();
	}
	
	@Override
	protected void onResume() {
		MyLog.print(TAG, "onResume");
		mKeySwitch = PhysicsKeyService.getKeySwitch();
		PhysicsKeyService.setKeySwitch(new boolean[] {
				true, true, true, true, true });
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		MyLog.print(TAG, "onPause");
		PhysicsKeyService.setKeySwitch(mKeySwitch);
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		MyLog.print(TAG, "onStop");
		super.onStop();
	}
	
	@Override
	protected void onRestart() {
		MyLog.print(TAG, "onRestart");
		finish();
		super.onRestart();
	}
	
	@Override
	protected void onDestroy() {
		MyLog.print(TAG, "onDestroy");
		PhysicsKeyService.unRegisterKeyCallback(mKeyCallback);
		MainActivity.isScreenOff = false;
		super.onDestroy();
	}
}

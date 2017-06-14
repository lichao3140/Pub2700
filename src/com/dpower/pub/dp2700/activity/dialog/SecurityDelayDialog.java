package com.dpower.pub.dp2700.activity.dialog;

import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.activity.BaseActivity;
import com.dpower.pub.dp2700.view.wheel.NumericWheelAdapter;
import com.dpower.pub.dp2700.view.wheel.WheelView;
import com.dpower.util.MyLog;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;

/**
 * ²¼·ÀÑÓ³Ù´°¿Ú
 */
public class SecurityDelayDialog extends BaseActivity implements OnClickListener {
	private static final String TAG = "SecurityDelayDialog";
	
	private LinearLayout mInfoWindow;
	private WheelView mWheelView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_security_delay);
		init();
	}
	
	private void init() {
		mWheelView = (WheelView) findViewById(R.id.wheel_view);
		mWheelView.setAdapter(new NumericWheelAdapter(1, 30));
		mWheelView.setCyclic(true);
		mWheelView.setVisibleItems(5);
		mWheelView.setCurrentItem(DPFunction.getProtectionDelayTime() - 1);
		findViewById(R.id.screen_window).setOnClickListener(this);
		mInfoWindow = (LinearLayout) findViewById(R.id.info_window);
		mInfoWindow.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				MyLog.print(TAG, "mInfoWindow onTouch");
				return true;
			}
		});
		findViewById(R.id.btn_confirm).setOnClickListener(this);
		findViewById(R.id.btn_cancel).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_confirm:
	            Intent intent = new Intent();
	            intent.putExtra("delayTime", mWheelView.getCurrentItem() + 1);
				setResult(101, intent);
				finish();
				break;
			case R.id.screen_window:
			case R.id.btn_cancel:
				finish();
				break;
			default:
				break;
		}
	}
}

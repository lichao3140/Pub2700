package com.dpower.pub.dp2700.activity.dialog;

import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.activity.BaseActivity;
import com.dpower.util.MyLog;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;

/**
 * ≥∑∑¿Ã· æ¥∞ø⁄
 */
public class HomeSecurityCancelTipsDialog  extends BaseActivity{
	private static final String TAG = "HomeSecurityCancelTipsDialog";
	
	private RelativeLayout mInfoWindow;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cancel_defence);
		findViewById(R.id.screen_window).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mInfoWindow = (RelativeLayout) findViewById(R.id.info_window);
		mInfoWindow.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				MyLog.print(TAG, "mInfoWindow onTouch");
				return true;
			}
		});
	}

}

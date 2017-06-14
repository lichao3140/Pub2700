package com.dpower.pub.dp2700.activity.dialog;

import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.activity.BaseActivity;
import com.dpower.pub.dp2700.tools.MyToast;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.util.MyLog;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * ∆¡±£…Ë÷√¥∞ø⁄
 */
public class SetScreenSaverDialog extends BaseActivity implements OnClickListener {
	private static final String TAG = "SetScreenSaverDialog";
	
	private LinearLayout mInfoWindow;
	private int mFlag;
	private Button[] mButtonArray;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_screen_saver_set);
		init();
	}

	private void init() {
		findViewById(R.id.screen_window).setOnClickListener(this);
		mInfoWindow = (LinearLayout) findViewById(R.id.info_window);
		mInfoWindow.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				MyLog.print(TAG, "mInfoWindow onTouch");
				return true;
			}
		});
		findViewById(R.id.btn_save).setOnClickListener(this);
		Button black = (Button) findViewById(R.id.btn_black);
		Button calendar = (Button) findViewById(R.id.btn_calendar);
		Button picture = (Button) findViewById(R.id.btn_picture);
		mButtonArray = new Button[] { black, calendar, picture };
		for (int i = 0; i < mButtonArray.length; i++) {
			mButtonArray[i].setOnClickListener(this);
		}
		mFlag = SPreferences.getInstance().getScreenSaverMode();
		setBackground();
	}

	private void setBackground() {
		for (int i = 0; i < 3; i++) {
			if (i == mFlag) {
				mButtonArray[i].setBackgroundResource(R.drawable.bg_button_blue_all);
				mButtonArray[i].setTextColor(getResources().getColor(R.color.White));
			} else {
				mButtonArray[i].setBackgroundResource(R.drawable.bg_button_blue_frame_white_inside);
				mButtonArray[i].setTextColor(getResources().getColor(R.color.DarkBlue));
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.screen_window:
				finish();
				break;
			case R.id.btn_black:
				mFlag = 0;
				setBackground();
				break;
			case R.id.btn_calendar:
				mFlag = 1;
				setBackground();
				break;
			case R.id.btn_picture:
				mFlag = 2;
				setBackground();
				break;
			case R.id.btn_save:
				SPreferences.getInstance().setScreenSaverMode(mFlag);
				MyToast.show(R.string.save_success);
				finish();
				break;
			default:
				break;
		}
	}
}

package com.dpower.pub.dp2700.activity.dialog;

import java.util.Locale;

import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.activity.BaseActivity;
import com.dpower.pub.dp2700.tools.Language;
import com.dpower.util.MyLog;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * ”Ô—‘…Ë÷√¥∞ø⁄
 */
public class SetLanguageDialog extends BaseActivity implements OnClickListener {
	private static final String TAG = "SetLanguageDialog";
	
	private LinearLayout mInfoWindow;
	private int mFlag;
	private Button mChinese;
	private Button mEnglish;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_language_setting);
		init();
	}

	private void init() {
		mChinese = (Button) findViewById(R.id.btn_chinese);
		mEnglish = (Button) findViewById(R.id.btn_english);
		mChinese.setOnClickListener(this);
		mEnglish.setOnClickListener(this);
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
		if(Locale.getDefault().getLanguage().equals("en")) {
			mFlag = 2;
			setBackground();
		} else {
			mFlag = 1;
			setBackground();
		}
	}

	private void setBackground() {
		switch (mFlag) {
			case 1:
				mChinese.setBackgroundResource(R.drawable.bg_button_blue_all);
				mChinese.setTextColor(getResources().getColor(R.color.White));
				mEnglish.setBackgroundResource(R.drawable.bg_button_blue_frame_white_inside);
				mEnglish.setTextColor(getResources().getColor(R.color.DarkBlue));
				break;
			case 2:
				mChinese.setBackgroundResource(R.drawable.bg_button_blue_frame_white_inside);
				mChinese.setTextColor(getResources().getColor(R.color.DarkBlue));
				mEnglish.setBackgroundResource(R.drawable.bg_button_blue_all);
				mEnglish.setTextColor(getResources().getColor(R.color.White));
				break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_chinese:
				mFlag = 1;
				setBackground();
				break;
			case R.id.btn_english:
				mFlag = 2;
				setBackground();
				break;
			case R.id.btn_save:
//				Language.updateLaguage(UserSetLanguageWindow.this,mFlag);
				Language.updateLaguage(mFlag);
				finish();
				break;
			case R.id.screen_window:
				finish();
				break;
			default:
				break;
		}
	}
}

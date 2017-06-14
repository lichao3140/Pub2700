package com.dpower.pub.dp2700.activity;

import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.tools.SPreferences;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CallForwardingActivity extends BaseFragmentActivity 
		implements OnClickListener {
	
	private LinearLayout mCallForwarding;
	private LinearLayout mCallNotForwarding;
	private TextView mTextCallForwarding;
	private TextView mTextCallForwardingTips;
	private TextView mTextCallNotForwarding;
	private TextView mTextCallNotForwardingTips;
	private boolean mFlag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_call_forwarding);
		init();
	}
	
	private void init() {
		mCallForwarding = (LinearLayout) findViewById(
				R.id.layout_call_forwarding);
		mCallNotForwarding = (LinearLayout) findViewById(
				R.id.layout_call_not_forwarding);
		mTextCallForwarding = (TextView) findViewById(
				R.id.tv_call_forwarding);
		mTextCallForwardingTips = (TextView) findViewById(
				R.id.tv_call_forwarding_tips);
		mTextCallNotForwarding = (TextView) findViewById(
				R.id.tv_call_not_forwarding);
		mTextCallNotForwardingTips = (TextView) findViewById(
				R.id.tv_call_not_forwarding_tips);
		mCallForwarding.setOnClickListener(this);
		mCallNotForwarding.setOnClickListener(this);
		findViewById(R.id.btn_back).setOnClickListener(this);
		findViewById(R.id.btn_confirm).setOnClickListener(this);
		mFlag = DPFunction.getCallToPhone();
		setBackground();
	}
	
	private void setBackground() {
		if (mFlag) {
			mCallForwarding.setBackgroundResource(
					R.drawable.bg_button_blue_all);
			mTextCallForwarding.setTextColor(
					getResources().getColor(R.color.White));
			mTextCallForwardingTips.setTextColor(
					getResources().getColor(R.color.White));
			mCallNotForwarding.setBackgroundResource(
					R.drawable.bg_button_blue_frame_white_inside);
			mTextCallNotForwarding.setTextColor(
					getResources().getColor(R.color.DarkBlue));
			mTextCallNotForwardingTips.setTextColor(
					getResources().getColor(R.color.DarkBlue));
		} else {
			mCallForwarding.setBackgroundResource(
					R.drawable.bg_button_blue_frame_white_inside);
			mTextCallForwarding.setTextColor(
					getResources().getColor(R.color.DarkBlue));
			mTextCallForwardingTips.setTextColor(
					getResources().getColor(R.color.DarkBlue));
			mCallNotForwarding.setBackgroundResource(
					R.drawable.bg_button_blue_all);
			mTextCallNotForwarding.setTextColor(
					getResources().getColor(R.color.White));
			mTextCallNotForwardingTips.setTextColor(
					getResources().getColor(R.color.White));
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		findViewById(R.id.root_view).setBackground(
				SPreferences.getInstance().getWallpaper());
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_back:
				finish();
				break;
			case R.id.btn_confirm:
				DPFunction.setCallToPhone(mFlag);
				finish();
				break;
			case R.id.layout_call_forwarding:
				mFlag = true;
				setBackground();
				break;
			case R.id.layout_call_not_forwarding:
				mFlag = false;
				setBackground();
				break;
			default:
				break;
		}
	}

}

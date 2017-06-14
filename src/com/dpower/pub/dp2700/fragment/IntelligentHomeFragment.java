package com.dpower.pub.dp2700.fragment;

import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.application.App;
import com.dpower.pub.dp2700.service.SmartHomeService;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.util.ProjectConfigure;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * ÖÇÄÜ¼Ò¾Ó
 */
public class IntelligentHomeFragment extends Fragment 
		implements OnClickListener {
	
	private View mRootView;
	private Button mButtonSmart;
	private SmartHomeModeChangeReceiver mSmartHomeModeReceiver;
	private Button mAtHome;
	private Button mInBed;
	private Button mDinner;
	private Button mMedia;
	private Button mEntertainment;
	private Button mDisableAll;
	private Button[] mButtonArray;
	private int mFlag;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (ProjectConfigure.project == 1) {
			mRootView = inflater.inflate(R.layout.fragment_intelligent_home1, 
					container, false);
		} else if (ProjectConfigure.project == 2) {
			mRootView = inflater.inflate(R.layout.fragment_intelligent_home2, 
					container, false);
		} else {
			mRootView = inflater.inflate(R.layout.fragment_intelligent_home, 
					container, false);
		}
		init();
		return mRootView;
	}

	private void init() {
		mAtHome = (Button) mRootView.findViewById(R.id.btn_at_home);
		mInBed = (Button) mRootView.findViewById(R.id.btn_in_bed);
		mDinner = (Button) mRootView.findViewById(R.id.btn_dinner);
		mMedia = (Button) mRootView.findViewById(R.id.btn_media);
		mEntertainment = (Button) mRootView.findViewById(R.id.btn_entertainment);
		mDisableAll = (Button) mRootView.findViewById(R.id.btn_disable_all);
		mButtonArray = new Button[] {mAtHome, mInBed, mDinner, 
				mMedia, mEntertainment, mDisableAll};
		for (int i = 0; i < mButtonArray.length; i++) {
			mButtonArray[i].setOnClickListener(this);
		}
		mButtonSmart = (Button) mRootView.findViewById(
				R.id.btn_smart_home_server_on);
		if (SPreferences.getInstance().getSmartServerState()) {
			mButtonSmart.setText(R.string.smart_home_server_off);
		} else {
			mButtonSmart.setText(R.string.smart_home_server_on);
		}
		mButtonSmart.setVisibility(View.GONE);
		mButtonSmart.setOnClickListener(this);
		mFlag = DPFunction.getSmartHomeMode() - 1;
		mSmartHomeModeReceiver = new SmartHomeModeChangeReceiver();
		App.getInstance().getContext().registerReceiver(mSmartHomeModeReceiver,
				new IntentFilter(DPFunction.ACTION_SMART_HOME_MODE));
	}

	@Override
	public void onResume() {
		super.onResume();
		if (SPreferences.getInstance().getSmartServerState()) {
			mButtonSmart.setText(R.string.smart_home_server_off);
		} else {
			mButtonSmart.setText(R.string.smart_home_server_on);
		}
		setButtonBackground();
	}
	
	private void setButtonBackground() {
		if (mFlag < 0) {
			return;
		}
		for (int i = 0; i < mButtonArray.length; i++) {
			if (i == mFlag) {
				if (ProjectConfigure.project == 1) {
					mButtonArray[i].setBackgroundResource(
							R.drawable.bg_home_orange_selector);
				} else {
					mButtonArray[i].setBackgroundResource(
							R.color.color_button_transparency_purple);
				}
			} else {
				if (ProjectConfigure.project == 1) {
					mButtonArray[i].setBackgroundResource(
							R.drawable.bg_home_blue_selector);
				} else {
					mButtonArray[i].setBackgroundResource(
							R.color.color_button_blue_purple);
				}
			}
		}
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (mSmartHomeModeReceiver != null) {
			App.getInstance().getContext().unregisterReceiver(mSmartHomeModeReceiver);
			mSmartHomeModeReceiver = null;
		}
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(SmartHomeService.ACTION_SMART_HOME);
		switch (v.getId()) {
			case R.id.btn_at_home:
				intent.putExtra("mode", 
						SmartHomeService.ACTION_IN_HOME);
				mFlag = 0;
				break;
			case R.id.btn_in_bed:
				intent.putExtra("mode", 
						SmartHomeService.ACTION_IN_BED);
				mFlag = 1;
				break;
			case R.id.btn_dinner:
				intent.putExtra("mode",
						SmartHomeService.ACTION_IN_DINNER);
				mFlag = 2;
				break;
			case R.id.btn_media:
				intent.putExtra("mode", 
						SmartHomeService.ACTION_IN_VIDEO);
				mFlag = 3;
				break;
			case R.id.btn_entertainment:
				intent.putExtra("mode",
						SmartHomeService.ACTION_IN_ENTERTAINMENT);
				mFlag = 4;
				break;
			case R.id.btn_disable_all:
				intent.putExtra("mode", 
						SmartHomeService.ACTION_ALL_OFF);
				mFlag = 5;
				break;
			case R.id.btn_smart_home_server_on:
				if (SPreferences.getInstance().getSmartServerState()) {
					intent.putExtra("mode",
							SmartHomeService.ACTION_SERVER_OFF);
				} else {
					intent.putExtra("mode",
							SmartHomeService.ACTION_SERVER_START);
				}
				break;
			default:
				break;
		}
		getActivity().sendBroadcast(intent);
		DPFunction.setSmartHomeMode(mFlag + 1);
		setButtonBackground();
	}

	private class SmartHomeModeChangeReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (SPreferences.getInstance().getSmartServerState()) {
				mButtonSmart.setText(R.string.smart_home_server_off);
			} else {
				mButtonSmart.setText(R.string.smart_home_server_on);
			}
			mFlag = DPFunction.getSmartHomeMode() - 1;
			setButtonBackground();
		}
	}
}

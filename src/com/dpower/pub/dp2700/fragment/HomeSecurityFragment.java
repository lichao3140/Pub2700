package com.dpower.pub.dp2700.fragment;

import java.util.ArrayList;

import com.dpower.domain.AlarmLog;
import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.activity.AlarmVideoActivity;
import com.dpower.pub.dp2700.activity.ModeSetActivity;
import com.dpower.pub.dp2700.activity.AlarmRecordActivity;
import com.dpower.pub.dp2700.activity.DefenceRecordActivity;
import com.dpower.pub.dp2700.activity.dialog.HomeSecurityModeClickedDialog;
import com.dpower.pub.dp2700.application.App;
import com.dpower.pub.dp2700.tools.MyToast;
import com.dpower.util.ConstConf;
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
 * 家庭安防
 */
public class HomeSecurityFragment extends Fragment implements OnClickListener {

	private View mRootView;
	private Button mAtHome, mInNight, mOutHome, mOutSecurity;
	private Button mRecordAlarm;
	private SafeModeChangeReceiver mSafeModeReceiver;
	private int mMode;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (ProjectConfigure.project == 1) {
			mRootView = inflater.inflate(R.layout.fragment_home_security1, container,
					false);
		} else if (ProjectConfigure.project == 2) {
			mRootView = inflater.inflate(R.layout.fragment_home_security2, container,
					false);
		} else {
			mRootView = inflater.inflate(R.layout.fragment_home_security, container,
					false);
		}
		init();
		return mRootView;
	}

	private void init() {
		mAtHome = (Button) mRootView.findViewById(R.id.btn_at_home);
		mInNight = (Button) mRootView.findViewById(R.id.btn_in_night);
		mOutHome = (Button) mRootView.findViewById(R.id.btn_out_home);
		mOutSecurity = (Button) mRootView.findViewById(R.id.btn_out_security);
		mAtHome.setOnClickListener(this);
		mInNight.setOnClickListener(this);
		mOutHome.setOnClickListener(this);
		mOutSecurity.setOnClickListener(this);
		mRootView.findViewById(R.id.btn_record_security).setOnClickListener(this);
		mRootView.findViewById(R.id.btn_record_alarm).setOnClickListener(this);
		mRootView.findViewById(R.id.btn_alarm_video).setOnClickListener(this);
		mRootView.findViewById(R.id.btn_mode_set).setOnClickListener(this);
		mRecordAlarm = (Button) mRootView.findViewById(R.id.record_alarm_hint);
		mMode = DPFunction.getSafeMode();
		setButtonBackground();
		mSafeModeReceiver = new SafeModeChangeReceiver();
		App.getInstance().getContext().registerReceiver(mSafeModeReceiver, 
				new IntentFilter(DPFunction.ACTION_SAFE_MODE));
	}
	
	private void setButtonBackground() {
		switch (mMode) {
			case ConstConf.HOME_MODE:
				if (ProjectConfigure.project == 1) {
					mAtHome.setBackgroundResource(R.drawable.bg_home_orange_selector);
					mInNight.setBackgroundResource(R.drawable.bg_home_blue_selector);
					mOutHome.setBackgroundResource(R.drawable.bg_home_blue_selector);
					mOutSecurity.setBackgroundResource(R.drawable.bg_home_blue_selector);
				} else {
					mAtHome.setBackgroundResource(R.color.color_button_transparency_purple);
					mInNight.setBackgroundResource(R.color.color_button_blue_purple);
					mOutHome.setBackgroundResource(R.color.color_button_blue_purple);
					mOutSecurity.setBackgroundResource(R.color.color_button_blue_purple);
				}
				break;
			case ConstConf.LEAVE_HOME_MODE:
				if (ProjectConfigure.project == 1) {
					mAtHome.setBackgroundResource(R.drawable.bg_home_blue_selector);
					mInNight.setBackgroundResource(R.drawable.bg_home_blue_selector);
					mOutHome.setBackgroundResource(R.drawable.bg_home_orange_selector);
					mOutSecurity.setBackgroundResource(R.drawable.bg_home_blue_selector);
				} else {
					mAtHome.setBackgroundResource(R.color.color_button_blue_purple);
					mInNight.setBackgroundResource(R.color.color_button_blue_purple);
					mOutHome.setBackgroundResource(R.color.color_button_transparency_purple);
					mOutSecurity.setBackgroundResource(R.color.color_button_blue_purple);
				}
				break;
			case ConstConf.NIGHT_MODE:
				if (ProjectConfigure.project == 1) {
					mAtHome.setBackgroundResource(R.drawable.bg_home_blue_selector);
					mInNight.setBackgroundResource(R.drawable.bg_home_orange_selector);
					mOutHome.setBackgroundResource(R.drawable.bg_home_blue_selector);
					mOutSecurity.setBackgroundResource(R.drawable.bg_home_blue_selector);
				} else {
					mAtHome.setBackgroundResource(R.color.color_button_blue_purple);
					mInNight.setBackgroundResource(R.color.color_button_transparency_purple);
					mOutHome.setBackgroundResource(R.color.color_button_blue_purple);
					mOutSecurity.setBackgroundResource(R.color.color_button_blue_purple);
				}
				break;
			case ConstConf.UNSAFE_MODE:
				if (ProjectConfigure.project == 1) {
					mAtHome.setBackgroundResource(R.drawable.bg_home_blue_selector);
					mInNight.setBackgroundResource(R.drawable.bg_home_blue_selector);
					mOutHome.setBackgroundResource(R.drawable.bg_home_blue_selector);
					mOutSecurity.setBackgroundResource(R.drawable.bg_home_orange_selector);
				} else {
					mAtHome.setBackgroundResource(R.color.color_button_blue_purple);
					mInNight.setBackgroundResource(R.color.color_button_blue_purple);
					mOutHome.setBackgroundResource(R.color.color_button_blue_purple);
					mOutSecurity.setBackgroundResource(R.color.color_button_transparency_purple);
				}
				break;
			default:
				break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_at_home:
				if (DPFunction.getAlarming()) {
					MyToast.show(R.string.alarm_alarming_to_unsafe);
					break;
				}
				if (ConstConf.HOME_MODE != DPFunction.getSafeMode()) {
					startActivity(ConstConf.HOME_MODE);
				}
				break;
			case R.id.btn_in_night:
				if (DPFunction.getAlarming()) {
					MyToast.show(R.string.alarm_alarming_to_unsafe);
					break;
				}
				if (ConstConf.NIGHT_MODE != DPFunction.getSafeMode()) {
					startActivity(ConstConf.NIGHT_MODE);
				}
				break;
			case R.id.btn_out_home:
				if (DPFunction.getAlarming()) {
					MyToast.show(R.string.alarm_alarming_to_unsafe);
					break;
				}
				if (ConstConf.LEAVE_HOME_MODE != DPFunction.getSafeMode()) {
					startActivity(ConstConf.LEAVE_HOME_MODE);
				}
				break;
			case R.id.btn_out_security:
				if (ConstConf.UNSAFE_MODE != DPFunction.getSafeMode() 
					|| DPFunction.getAlarming()) {
					startActivity(ConstConf.UNSAFE_MODE);
				}
				break;
			case R.id.btn_record_security:
				// 安防日志
				startActivity(new Intent(getActivity(), DefenceRecordActivity.class));
				break;
			case R.id.btn_record_alarm:
				// 报警记录
				startActivity(new Intent(getActivity(), AlarmRecordActivity.class));
				break;
			case R.id.btn_alarm_video:
				// 安防录像
				startActivity(new Intent(getActivity(), AlarmVideoActivity.class));
				break;
			case R.id.btn_mode_set:
				// 模式设置
				startActivity(new Intent(getActivity(), ModeSetActivity.class));
				break;
			default:
				break;
			}
	}

	private class SafeModeChangeReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			mMode = DPFunction.getSafeMode();
			setButtonBackground();
		}
	}

	private void startActivity(int mode) {
		Intent intent = new Intent();
		intent.setClass(getActivity(), HomeSecurityModeClickedDialog.class);
		intent.putExtra("modeflag", mode);
		startActivity(intent);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		updateRecordAlarmView();
	}

	private void updateRecordAlarmView() {
		ArrayList<AlarmLog> alarmLogs = DPFunction.getAlarmLogList();
		if (alarmLogs == null) {
			mRecordAlarm.setVisibility(View.GONE);
			return;
		}
		int count = 0;
		for (int i = 0; i < alarmLogs.size(); i++) {
			if (!alarmLogs.get(i).getIsRead()) {
				count++;
			}
		}
		if (count > 0) {
			mRecordAlarm.setVisibility(View.VISIBLE);
			if (count <= 99) {
				mRecordAlarm.setText("" + count);
			} else {
				mRecordAlarm.setText("99+");
			}
		} else {
			mRecordAlarm.setVisibility(View.GONE);
		}
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (mSafeModeReceiver != null) {
			App.getInstance().getContext().unregisterReceiver(mSafeModeReceiver);
			mSafeModeReceiver = null;
		}
	}
}

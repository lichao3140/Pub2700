package com.dpower.pub.dp2700.fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.dpower.domain.AddrInfo;
import com.dpower.domain.CallInfomation;
import com.dpower.domain.MessageInfo;
import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.activity.CallGuardActivity;
import com.dpower.pub.dp2700.activity.CallLogActivity;
import com.dpower.pub.dp2700.activity.CallManagementActivity;
import com.dpower.pub.dp2700.activity.MonitorActivity;
import com.dpower.pub.dp2700.activity.SMSActivity;
import com.dpower.pub.dp2700.activity.UserDialActivity;
import com.dpower.pub.dp2700.activity.dialog.HomeSecurityModeClickedDialog;
import com.dpower.pub.dp2700.application.App;
import com.dpower.pub.dp2700.tools.MyToast;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.util.ConstConf;
import com.dpower.util.ProjectConfigure;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * 主页
 */
public class MainFragment extends Fragment implements OnClickListener {

	private final int UPDATE_DATE = 100;
	private View mRootView;
	private TextView mTextTime;
	private TextView mTextDate;
	private TextView mTextWeek;
	private Button mButtonDefence;
	private Button mButtonCallElevator;
	private Button mButtonRing;
	private AudioManager mAudioManager;// 设置系统音频开关
	private Button mNewMessage;// 管理中心的未读消息数目
	private Button mCallLog;
	private RingModeChangeReceiver mRingModeReceiver;
	private SMSBroadcastReceiver mSMSReceiver;
	private SafeModeChangeReceiver mSafeModeReceiver;
	private SimpleDateFormat mTimeTextFormat;
	private SimpleDateFormat mWeekTextFormat;
	private SimpleDateFormat mDateTextFormat;
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case UPDATE_DATE:
					Date localDate = new Date();
					mTextDate.setText(mTimeTextFormat.format(localDate));
					mTextWeek.setText(mWeekTextFormat.format(localDate));
					mTextTime.setText(mDateTextFormat.format(localDate));
					mHandler.sendEmptyMessageDelayed(UPDATE_DATE, 30 * 1000);
					break;
				default:
					break;
			}
		};
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (ProjectConfigure.project == 1) {
			mRootView = inflater.inflate(R.layout.fragment_main1, container,
					false);
		} else if (ProjectConfigure.project == 2) {
			mRootView = inflater.inflate(R.layout.fragment_main2, container,
					false);
		} else {
			mRootView = inflater.inflate(R.layout.fragment_main, container,
					false);
		}
		init();
		return mRootView;
	}

	private void init() {
		mTextTime = (TextView) mRootView.findViewById(R.id.tv_time);
		mTextDate = (TextView) mRootView.findViewById(R.id.tv_date);
		mTextWeek = (TextView) mRootView.findViewById(R.id.tv_week);
		mRootView.findViewById(R.id.btn_msg).setOnClickListener(this);
		mRootView.findViewById(R.id.btn_call_center).setOnClickListener(this);
		mRootView.findViewById(R.id.btn_talk_in_user).setOnClickListener(this);
		mRootView.findViewById(R.id.btn_monitor).setOnClickListener(this);
		mButtonDefence = (Button) mRootView.findViewById(R.id.btn_out_security);
		mButtonCallElevator = (Button) mRootView.findViewById(R.id.btn_call_elevator);
		mNewMessage = (Button) mRootView.findViewById(R.id.sms_hint);
		mCallLog = (Button) mRootView.findViewById(R.id.call_log_hint);
		mButtonRing = (Button) mRootView.findViewById(R.id.btn_ring_enable);
		mButtonRing.setOnClickListener(this);
		mButtonDefence.setOnClickListener(this);
		mButtonCallElevator.setOnClickListener(this);
		mRootView.findViewById(R.id.btn_call_log).setOnClickListener(this);
		mAudioManager = (AudioManager) getActivity().getSystemService(
				Context.AUDIO_SERVICE);
		if (DPFunction.getSafeMode() == ConstConf.UNSAFE_MODE) {
			mButtonDefence.setText(R.string.text_one_key_set_defence);
		} else {
			mButtonDefence.setText(R.string.text_one_key_cancel_defence);
		}
		mTimeTextFormat = new SimpleDateFormat(getActivity().getString(R.string.format_date));
		mWeekTextFormat = new SimpleDateFormat("EEEE");
		mDateTextFormat = new SimpleDateFormat("HH:mm");
		mHandler.sendEmptyMessage(UPDATE_DATE);
		registerReceiver();
	}
	
	private void registerReceiver() {
		IntentFilter filter = new IntentFilter(ConstConf.VOLUME_CHANGED_ACTION);
		mRingModeReceiver = new RingModeChangeReceiver();
		App.getInstance().getContext().registerReceiver(mRingModeReceiver, filter);
		filter = new IntentFilter(DPFunction.ACTION_SAFE_MODE);
		mSafeModeReceiver = new SafeModeChangeReceiver();
		App.getInstance().getContext().registerReceiver(mSafeModeReceiver, filter);
		filter = new IntentFilter(MessageInfo.ACTION_MESSAGE);
		mSMSReceiver = new SMSBroadcastReceiver();
		App.getInstance().getContext().registerReceiver(mSMSReceiver, filter);
	}
	
	private void updateVolumeView() {
		int vol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		SPreferences.getInstance().setSystemSilent(vol == 0 ? true : false);
		if (vol == 0) {
			mButtonRing.setText(R.string.ring_disable);
			mButtonRing.setTextColor(Color.RED);
		} else {
			mButtonRing.setText(R.string.ring_enable);
			mButtonRing.setTextColor(Color.WHITE);
			mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		}
	}
	
	private void updateMessageView() {
		int count = DPFunction.getUnReadMessageLogNum(
				MessageInfo.PERSONAL | MessageInfo.PUBLIC);
		if (count > 0) {
			mNewMessage.setVisibility(View.VISIBLE);
			if (count <= 99) {
				mNewMessage.setText("" + count);
			} else {
				mNewMessage.setText("99+");
			}
		} else {
			mNewMessage.setVisibility(View.GONE);
		}
	}
	
	private void updateCallLogView() {
		ArrayList<CallInfomation> callLogList = DPFunction
				.getCallLogList(CallInfomation.CALL_IN_UNACCEPT);
		if (callLogList == null) {
			mCallLog.setVisibility(View.GONE);
			return;
		}
		int count = 0;
		for (int i = 0; i < callLogList.size(); i++) {
			if (!callLogList.get(i).isRead()) {
				count++;
			}
		}
		if (count > 0) {
			mCallLog.setVisibility(View.VISIBLE);
			if (count <= 99) {
				mCallLog.setText("" + count);
			} else {
				mCallLog.setText("99+");
			}
		} else {
			mCallLog.setVisibility(View.GONE);
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		updateVolumeView();
		updateMessageView();
		updateCallLogView();
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_msg:
				startActivity(new Intent(getActivity(), SMSActivity.class));
				break;
			case R.id.btn_call_center:
				if (ProjectConfigure.project == 1) {
					startActivity(new Intent(getActivity(), CallGuardActivity.class));
				} else {
					startActivity(new Intent(getActivity(), CallManagementActivity.class));
				}
				break;
			case R.id.btn_talk_in_user:
				startActivity(new Intent(getActivity(), UserDialActivity.class));
				break;
			case R.id.btn_monitor:
				startActivity(new Intent(getActivity(), MonitorActivity.class));
				break;
			case R.id.btn_ring_enable:
				int beforeVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
				if (beforeVol == 0 || (mAudioManager.getRingerMode() 
						== AudioManager.RINGER_MODE_SILENT)) {
					mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
					int afterVol = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
					if (afterVol == 0) {
						afterVol = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
						mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 
								afterVol, AudioManager.FLAG_PLAY_SOUND);
					}
					beforeVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
					SPreferences.getInstance().setSystemSilent(false);
					SPreferences.getInstance().setRingVol(beforeVol);
					updateVolumeView();
				} else {
					mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
					SPreferences.getInstance().setSystemSilent(true);
					SPreferences.getInstance().setRingVol(beforeVol);
					updateVolumeView();
				}
				break;
			case R.id.btn_out_security:
				Intent intent = new Intent();
				intent.setClass(getActivity(), HomeSecurityModeClickedDialog.class);
				if (DPFunction.getSafeMode() == ConstConf.UNSAFE_MODE) {
					if (DPFunction.getAlarming()) {
						MyToast.show(R.string.alarm_alarming_to_unsafe);
						break;
					}
					intent.putExtra("modeflag", ConstConf.LEAVE_HOME_MODE);
					startActivity(intent);
				} else {
					intent.putExtra("modeflag", ConstConf.UNSAFE_MODE);
					startActivity(intent);
				}
				break;
			case R.id.btn_call_elevator:
				AddrInfo addrInfo;
				ArrayList<AddrInfo> arrayList = DPFunction.getCellSeeList();
				if (arrayList != null) {
					addrInfo = arrayList.get(0);
					int result = DPFunction.toDoorCallEvt(addrInfo.getIp());
					if (result == 0) {
						MyToast.show(R.string.tips_call_elevator_success);
					} else {
						MyToast.show(R.string.tips_call_elevator_fail);
					}
				} else {
					MyToast.show(R.string.tips_communicate_entrance_machine_fail);
				}
				break;
			case R.id.btn_call_log:
				startActivity(new Intent(getActivity(), CallLogActivity.class));
				break;
			default:
				break;
		}
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		unregisterReceiver();
		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
        }
	}
	
	private void unregisterReceiver() {
		if (mRingModeReceiver != null) {
			App.getInstance().getContext().unregisterReceiver(mRingModeReceiver);
			mRingModeReceiver = null;
		}
		if (mSMSReceiver != null) {
			App.getInstance().getContext().unregisterReceiver(mSMSReceiver);
			mSMSReceiver = null;
		}
		if (mSafeModeReceiver != null) {
			App.getInstance().getContext().unregisterReceiver(mSafeModeReceiver);
			mSafeModeReceiver = null;
		}
	}

	private class RingModeChangeReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			updateVolumeView();
		}
	}
	
	private class SMSBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			updateMessageView();
		}
	}
	
	private class SafeModeChangeReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (DPFunction.getSafeMode() == ConstConf.UNSAFE_MODE) {
				mButtonDefence.setText(R.string.text_one_key_set_defence);
			} else {
				mButtonDefence.setText(R.string.text_one_key_cancel_defence);
			}
		}
	}
}

package com.dpower.pub.dp2700.fragment;

import java.util.ArrayList;

import com.dpower.domain.CallInfomation;
import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.activity.CallGuardActivity;
import com.dpower.pub.dp2700.activity.CallLogActivity;
import com.dpower.pub.dp2700.activity.CallManagementActivity;
import com.dpower.pub.dp2700.activity.LeaveMessageActivity;
import com.dpower.pub.dp2700.activity.MonitorActivity;
import com.dpower.pub.dp2700.activity.UserDialActivity;
import com.dpower.util.ProjectConfigure;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * ø… ”∂‘Ω≤
 */
public class VisualIntercomFragment extends Fragment implements OnClickListener {

	private View mRootView;
	private Button mMessage;
	private Button mCallLog;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (ProjectConfigure.project == 1) {
			mRootView = inflater.inflate(R.layout.fragment_visual_intercom1, container,
					false);
		} else if (ProjectConfigure.project == 2) {
			mRootView = inflater.inflate(R.layout.fragment_visual_intercom2, container,
					false);
		} else {
			mRootView = inflater.inflate(R.layout.fragment_visual_intercom, container,
					false);
		}
		init();
		return mRootView;
	}

	private void init() {
		mRootView.findViewById(R.id.btn_building_monitor).setOnClickListener(this);
		mRootView.findViewById(R.id.btn_call_management_center).setOnClickListener(this);
		mRootView.findViewById(R.id.btn_user_call).setOnClickListener(this);
		mRootView.findViewById(R.id.btn_call_record).setOnClickListener(this);
		mRootView.findViewById(R.id.btn_leave_message_or_video).setOnClickListener(this);
		mMessage = (Button) mRootView.findViewById(R.id.message_hint);
		mCallLog = (Button) mRootView.findViewById(R.id.call_log_hint);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_building_monitor:
				startActivity(new Intent(getActivity(), MonitorActivity.class));
				break;
			case R.id.btn_call_management_center:
				if (ProjectConfigure.project == 1) {
					startActivity(new Intent(getActivity(), CallGuardActivity.class));
				} else {
					startActivity(new Intent(getActivity(), CallManagementActivity.class));
				}
				break;
			case R.id.btn_user_call:
				startActivity(new Intent(getActivity(), UserDialActivity.class));
				break;
			case R.id.btn_call_record:
				startActivity(new Intent(getActivity(), CallLogActivity.class));
				break;
			case R.id.btn_leave_message_or_video:
				startActivity(new Intent(getActivity(), LeaveMessageActivity.class));
				break;
			default:
				break;
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		updateView();
	}
	
	private void updateView() {
		ArrayList<CallInfomation> callLogList = DPFunction
				.getCallLogList(CallInfomation.CALL_IN_UNACCEPT);
		if (callLogList == null) {
			mCallLog.setVisibility(View.GONE);
			mMessage.setVisibility(View.GONE);
			return;
		}
		int callLogCount = 0;
		int messageCount = 0;
		for (int i = 0; i < callLogList.size(); i++) {
			if (!callLogList.get(i).isRead()) {
				callLogCount++;
			}
			if (callLogList.get(i).isDoor() && 
					!callLogList.get(i).isRead()) {
				messageCount++;
			}
		}
		if (callLogCount > 0) {
			mCallLog.setVisibility(View.VISIBLE);
			if (callLogCount <= 99) {
				mCallLog.setText("" + callLogCount);
			} else {
				mCallLog.setText("99+");
			}
		} else {
			mCallLog.setVisibility(View.GONE);
		}
		if (messageCount > 0) {
			mMessage.setVisibility(View.VISIBLE);
			if (messageCount <= 99) {
				mMessage.setText("" + messageCount);
			} else {
				mMessage.setText("99+");
			}
		} else {
			mMessage.setVisibility(View.GONE);
		}
	}
}

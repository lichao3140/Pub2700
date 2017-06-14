package com.dpower.pub.dp2700.activity;

import java.util.List;

import com.dpower.domain.AlarmInfo;
import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.activity.dialog.SecurityDelayDialog;
import com.dpower.pub.dp2700.adapter.ModeSetAdapter;
import com.dpower.pub.dp2700.tools.MyToast;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.util.ConstConf;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

/**
 * @author ZhengZhiying
 * @Funtion 家庭安防/模式设置
 */
public class ModeSetActivity extends BaseFragmentActivity 
		implements OnClickListener {

	public ListView mListView;
	public ModeSetAdapter mAdapter;
	private Button mInHome, mLeaveHome, mInNight, mSecurityPassword;
	private Button mButtonDelay;
	public List<AlarmInfo> mAlarmInfos;
	public int curpos = 0;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mode_setting);
		init();
	}

	private void init() {
		mContext = this;
		mListView = (ListView) findViewById(R.id.list_view_mode_set);
		mAdapter = new ModeSetAdapter(mContext);
		mAlarmInfos = DPFunction.getAlarmInfoList(mAdapter.getMode());
        if (mAlarmInfos == null) {
			finish();
			return;
		}
		mAdapter.setAlarmInfos(mAlarmInfos);
		mListView.setAdapter(mAdapter);
		findViewById(R.id.btn_back).setOnClickListener(this);
		findViewById(R.id.btn_delay).setOnClickListener(this);
		findViewById(R.id.btn_confirm).setOnClickListener(this);
		mButtonDelay = (Button) findViewById(R.id.btn_delay);
		mInHome = (Button) findViewById(R.id.btn_at_home);
		mInNight = (Button) findViewById(R.id.btn_in_night);
		mLeaveHome = (Button) findViewById(R.id.btn_leave_home);
		mSecurityPassword = (Button) findViewById(R.id.btn_security_password);
		mButtonDelay.setOnClickListener(this);
		mInHome.setOnClickListener(this);
		mInNight.setOnClickListener(this);
		mLeaveHome.setOnClickListener(this);
		mSecurityPassword.setOnClickListener(this);
		updateButtonBackground(mAdapter.getMode());
		mButtonDelay.setText(getString(R.string.enable_delay) + " : "
				+ DPFunction.getProtectionDelayTime());
	}
	
	private void updateButtonBackground(int mode) {
		switch (mode) {
			case ConstConf.HOME_MODE:
				mInHome.setBackgroundColor(getResources().getColor(
						R.color.DialogTransparency));
				mLeaveHome.setBackgroundColor(getResources().getColor(
						R.color.Transparency));
				mInNight.setBackgroundColor(getResources().getColor(
						R.color.Transparency));
				mSecurityPassword.setBackgroundColor(getResources().getColor(
						R.color.Transparency));
				break;
			case ConstConf.LEAVE_HOME_MODE:
				mInHome.setBackgroundColor(getResources().getColor(
						R.color.Transparency));
				mLeaveHome.setBackgroundColor(getResources().getColor(
						R.color.DialogTransparency));
				mInNight.setBackgroundColor(getResources().getColor(
						R.color.Transparency));
				mSecurityPassword.setBackgroundColor(getResources().getColor(
						R.color.Transparency));
				break;
			case ConstConf.NIGHT_MODE:
				mInHome.setBackgroundColor(getResources().getColor(
						R.color.Transparency));
				mLeaveHome.setBackgroundColor(getResources().getColor(
						R.color.Transparency));
				mInNight.setBackgroundColor(getResources().getColor(
						R.color.DialogTransparency));
				mSecurityPassword.setBackgroundColor(getResources().getColor(
						R.color.Transparency));
				break;
			case ConstConf.UNSAFE_MODE:
				mInHome.setBackgroundColor(getResources().getColor(
						R.color.Transparency));
				mLeaveHome.setBackgroundColor(getResources().getColor(
						R.color.Transparency));
				mInNight.setBackgroundColor(getResources().getColor(
						R.color.Transparency));
				mSecurityPassword.setBackgroundColor(getResources().getColor(
						R.color.DialogTransparency));
			default:
				break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		findViewById(R.id.root_view).setBackground(
				SPreferences.getInstance().getWallpaper());
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 100 && resultCode == 101) {
			int modeDelayTime = data.getIntExtra("delayTime", 30);
			mButtonDelay.setText(getString(R.string.enable_delay) + ":"
					+ modeDelayTime);
			DPFunction.setProtectionDelayTime(modeDelayTime);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;
		case R.id.btn_delay:
			startActivityForResult(new Intent(mContext, SecurityDelayDialog.class), 100);
			break;
		case R.id.btn_confirm:
			mAdapter.save();
			MyToast.show(R.string.save_success);
			finish();
			break;
		case R.id.btn_at_home:
			mListView.setVisibility(View.VISIBLE);
			mAdapter.setMode(ConstConf.HOME_MODE);
			update();
			break;
		case R.id.btn_in_night:
			mListView.setVisibility(View.VISIBLE);
			mAdapter.setMode(ConstConf.NIGHT_MODE);
			update();
			break;
		case R.id.btn_leave_home:
			mListView.setVisibility(View.VISIBLE);
			mAdapter.setMode(ConstConf.LEAVE_HOME_MODE);
			update();
			break;
		case R.id.btn_security_password:
			mListView.setVisibility(View.GONE);
			mAdapter.setMode(ConstConf.UNSAFE_MODE);
			update();
			break;
		default:
			break;
		}
	}
	
	private void update() {
		updateButtonBackground(mAdapter.getMode());
		mAlarmInfos = DPFunction.getAlarmInfoList(mAdapter.getMode());
        if (mAlarmInfos == null) {
			finish();
			return;
		}
		mAdapter.setAlarmInfos(mAlarmInfos);
		mAdapter.notifyDataSetChanged();
	}
}

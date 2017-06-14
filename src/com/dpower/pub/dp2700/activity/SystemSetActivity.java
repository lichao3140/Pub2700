package com.dpower.pub.dp2700.activity;

import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.activity.dialog.SmartServerIPSetDialog;
import com.dpower.pub.dp2700.activity.dialog.SystemResetDialog;
import com.dpower.pub.dp2700.activity.dialog.WebCameraSetDialog;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.util.ProjectConfigure;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * œµÕ≥…Ë÷√
 */
public class SystemSetActivity extends BaseActivity implements OnClickListener {
	
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (ProjectConfigure.project == 1) {
			setContentView(R.layout.activity_system_setting1);
		} else if (ProjectConfigure.project == 2) {
			setContentView(R.layout.activity_system_setting2);
		} else {
			setContentView(R.layout.activity_system_setting);
		}
		mContext = this;
		findViewById(R.id.btn_room_num_set).setOnClickListener(this);
		findViewById(R.id.btn_back).setOnClickListener(this);
		findViewById(R.id.btn_defence_area_set).setOnClickListener(this);
		findViewById(R.id.btn_system_reset).setOnClickListener(this);
		findViewById(R.id.btn_project_password_set).setOnClickListener(this);
		findViewById(R.id.btn_unlock_set).setOnClickListener(this);
		findViewById(R.id.btn_unlock_psd).setOnClickListener(this);
		findViewById(R.id.btn_villa_extension_set).setOnClickListener(this);
		findViewById(R.id.btn_project_webcamera).setOnClickListener(this);
		findViewById(R.id.btn_project_smart_serv).setOnClickListener(this);
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
		case R.id.btn_room_num_set:
			startActivity(new Intent(mContext, RoomNumSetActivity.class));
			break;
		case R.id.btn_defence_area_set:
			startActivity(new Intent(mContext, DefenceAreaSetActivity.class));
			break;
		case R.id.btn_system_reset:
			startActivity(new Intent(mContext, SystemResetDialog.class));
			break;
		case R.id.btn_unlock_set:
			startActivity(new Intent(mContext, UnlockSetActivity.class));
			break;
		case R.id.btn_unlock_psd:
			startActivity(new Intent(mContext, UnlockPasswordActivity.class));
			break;
		case R.id.btn_villa_extension_set:
			Intent villaSet = new Intent(mContext, RoomNumSetActivity.class);
			villaSet.setAction("villaSet");
			startActivity(villaSet);
			break;
		case R.id.btn_project_password_set:
			startActivity(new Intent(mContext, ChangeProjectPasswordActivity.class));
			break;
		case R.id.btn_project_webcamera:
			startActivity(new Intent(mContext, WebCameraSetDialog.class));
			break;
		case R.id.btn_project_smart_serv:
			startActivity(new Intent(mContext, SmartServerIPSetDialog.class));
			break;
		default:
			break;
		}
	}
}

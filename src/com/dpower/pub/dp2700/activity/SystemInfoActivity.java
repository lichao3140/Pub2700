package com.dpower.pub.dp2700.activity;

import com.dpower.domain.AddrInfo;
import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.util.ProjectConfigure;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * 系统信息界面
 */
public class SystemInfoActivity extends BaseActivity {
	
	private TextView mTextRoomNum;
	private TextView mTextIp;
	private TextView mTextNetCfgVer;
	private TextView mTextAPKVersion;
	private TextView mTextDeviceType;
	private TextView mTextSystemType;
	private TextView mTextFirmwareVersion;
	private String mDeviceType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_system_info);
		if (ProjectConfigure.project == 1) {
			if (ProjectConfigure.size == 10) {
				mDeviceType = "90AC10";
			} else {
				mDeviceType = "90AC1";
			}
		} else if (ProjectConfigure.project == 2) {
			if (ProjectConfigure.size == 10) {
				mDeviceType = "CMT-1601";
			} else {
				mDeviceType = "CMT-1602";
			}
		} else {
			mDeviceType = getVersionName();
		}
		mTextRoomNum = (TextView) findViewById(R.id.room_num);
		mTextIp = (TextView) findViewById(R.id.ip);
		mTextNetCfgVer = (TextView) findViewById(R.id.net_cfg_ver);
		mTextAPKVersion = (TextView) findViewById(R.id.apk_version);
		mTextDeviceType = (TextView) findViewById(R.id.device_type);
		mTextSystemType =(TextView) findViewById(R.id.system_type);
		mTextFirmwareVersion =(TextView) findViewById(R.id.firmware_version);
		String roomNum = DPFunction.getRoomCode();
		String areaStr = roomNum.substring(1, 3);
		String mewsStr = roomNum.substring(3, 5);
		String unitStr = roomNum.substring(5, 7);
		String roomStr = roomNum.substring(7, 11);
		String extensionStr = roomNum.substring(11, 13);
		String roomName = getString(R.string.system_info_room) 
							+ areaStr + getString(R.string.text_area) 
							+ mewsStr + getString(R.string.text_building) 
							+ unitStr + getString(R.string.text_unit)
							+ roomStr + getString(R.string.text_room) 
							+ extensionStr + getString(R.string.text_extension);
		mTextRoomNum.setText(roomName);
		AddrInfo info = DPFunction.getAddrInfo(DPFunction.getRoomCode());
		if (info == null) {
			finish();
			return;
		}
		mTextIp.setText(getString(R.string.system_info_ip_address) 
				+ info.getIp() + ", " + info.getMask() + ", " + info.getGw());
		mTextNetCfgVer.setText(getString(R.string.system_info_net_cfg) 
				+ DPFunction.getNetCfgVer());

		try {
			PackageInfo apkInfo = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			mTextAPKVersion.setText(getString(R.string.system_info_version) 
					+ apkInfo.versionCode);
		} catch (NameNotFoundException e) {
			mTextAPKVersion.setText(getString(R.string.system_info_version) + "test");
			e.printStackTrace();
		}
		mTextDeviceType.setText(getString(R.string.system_info_type) + mDeviceType);
		mTextSystemType.setText(getString(R.string.system_info) 
				+ "Android " + Build.VERSION.RELEASE);
		mTextFirmwareVersion.setText(getString(R.string.firmware_version) + Build.DISPLAY);
	}
	
	private String getVersionName() {
		PackageManager manager = getPackageManager();
		PackageInfo info;
		try {
			info = manager.getPackageInfo(getPackageName(), 0);
			return info.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return "";
	}

	@Override
	protected void onResume() {
		super.onResume();
		findViewById(R.id.root_view).setBackground(
				SPreferences.getInstance().getWallpaper());
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		finish();
		return super.onTouchEvent(event);
	}
}

package com.dpower.pub.dp2700.fragment;

import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.activity.FunMusicActivity;
import com.dpower.pub.dp2700.activity.FunPhotoActivity;
import com.dpower.pub.dp2700.activity.SystemInfoActivity;
import com.dpower.pub.dp2700.activity.SetTimeActivity;
import com.dpower.pub.dp2700.activity.dialog.CheckPasswordDialog;
import com.dpower.pub.dp2700.activity.dialog.SetScreenSaverDialog;
import com.dpower.pub.dp2700.activity.dialog.SetLeaveAudioDialog;
import com.dpower.pub.dp2700.activity.dialog.SystemLightSetDialog;
import com.dpower.pub.dp2700.activity.dialog.SetLanguageDialog;
import com.dpower.util.ProjectConfigure;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

/**
 * 设置
 */
public class UserSettingFragment extends Fragment implements OnClickListener {

	private View mRootView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (ProjectConfigure.project == 1) {
			mRootView = inflater.inflate(R.layout.fragment_setting1, container,
					false);
		} else if (ProjectConfigure.project == 2) {
			mRootView = inflater.inflate(R.layout.fragment_setting2, container,
					false);
		} else {
			mRootView = inflater.inflate(R.layout.fragment_setting, container,
					false);
		}
		mRootView.findViewById(R.id.btn_screen_saver_set).setOnClickListener(this);
		mRootView.findViewById(R.id.btn_system_info).setOnClickListener(this);
		mRootView.findViewById(R.id.btn_system_setting).setOnClickListener(this);
		mRootView.findViewById(R.id.btn_wallpaper_set).setOnClickListener(this);
		mRootView.findViewById(R.id.btn_light).setOnClickListener(this);
		mRootView.findViewById(R.id.btn_ringset).setOnClickListener(this);
		mRootView.findViewById(R.id.btn_time_setting).setOnClickListener(this);
		mRootView.findViewById(R.id.btn_leave_audio).setOnClickListener(this);
		mRootView.findViewById(R.id.btn_language_set).setOnClickListener(this);

		return mRootView;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_screen_saver_set:
				// 屏保设置
				startActivity(new Intent(getActivity(), SetScreenSaverDialog.class));
				break;
			case R.id.btn_system_info:
				// 系统信息
				startActivity(new Intent(getActivity(), SystemInfoActivity.class));
				break;
			case R.id.btn_system_setting:
				// 系统设置
				Intent systemSet = new Intent(getActivity(), CheckPasswordDialog.class);
				systemSet.setAction("systemSet");
				getActivity().startActivity(systemSet);
				break;
			case R.id.btn_wallpaper_set:
				// 壁纸设置
				Intent intent = new Intent();
				intent.putExtra("wallpaper", "wallpaper");
				intent.setClass(getActivity(), FunPhotoActivity.class);
				getActivity().startActivity(intent);
				break;
			case R.id.btn_light:
				// 屏幕设置
				startActivity(new Intent(getActivity(), SystemLightSetDialog.class));
				break;
			case R.id.btn_ringset:
				// 铃声设置 
				Intent intentRingset = new Intent();
				intentRingset.putExtra("FunMusicActivity", "FunMusicActivity");
				intentRingset.setClass(getActivity(), FunMusicActivity.class);
				getActivity().startActivity(intentRingset);
				break;
			case R.id.btn_time_setting:
				// 时间设置
				startActivity(new Intent(getActivity(), SetTimeActivity.class));
				break;
			case R.id.btn_leave_audio:
				// 留言设置
				startActivity(new Intent(getActivity(), SetLeaveAudioDialog.class));
				break;
			case R.id.btn_language_set:
				// 语言设置
				startActivity(new Intent(getActivity(), SetLanguageDialog.class));
				break;
			default:
				break;
		}
	}
}

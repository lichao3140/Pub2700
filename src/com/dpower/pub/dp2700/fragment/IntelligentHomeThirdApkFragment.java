package com.dpower.pub.dp2700.fragment;

import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.tools.MyToast;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class IntelligentHomeThirdApkFragment extends Fragment 
		implements OnClickListener {
	
	private View mRootView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRootView = inflater.inflate(
				R.layout.fragment_intelligent_home_third_app, container, false);
		mRootView.findViewById(R.id.bt_start_app).setOnClickListener(this);
		return mRootView;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.bt_start_app:
				Intent intent = new Intent(); // 测试启动第三方程序，2016.03.21
				// 修改了对应的xml，存在部分BUG
				intent.setComponent(new ComponentName(
						"com.job.netsearch",
						"com.netsearch.activity.StartUp"));
				try {
					startActivity(intent);
				} catch (ActivityNotFoundException e) {
					MyToast.show("智能家居应用未安装，请安装后重试");
				}
				break;
			default:
				break;
		}
	}
}

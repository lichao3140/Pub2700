package com.dpower.pub.dp2700.adapter;

import java.util.ArrayList;
import java.util.List;

import com.dpower.pub.dp2700.fragment.CloudIntercomFragment;
import com.dpower.pub.dp2700.fragment.EntertainmentFragment;
import com.dpower.pub.dp2700.fragment.HomeSecurityFragment;
import com.dpower.pub.dp2700.fragment.IntelligentHomeFragment;
import com.dpower.pub.dp2700.fragment.IntelligentHomeThirdApkFragment;
import com.dpower.pub.dp2700.fragment.MainFragment;
import com.dpower.pub.dp2700.fragment.UserSettingFragment;
import com.dpower.pub.dp2700.fragment.VisualIntercomFragment;
import com.dpower.util.ProjectConfigure;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MainPageAdapter extends FragmentPagerAdapter {
	
	private static int mHomePage;
	private List<Fragment> mFragments;

	public MainPageAdapter(FragmentManager fm) {
		super(fm);
		mFragments = new ArrayList<Fragment>();
		if (ProjectConfigure.project == 2) {
			mFragments.add(new MainFragment());
			mFragments.add(new HomeSecurityFragment());
			mFragments.add(new VisualIntercomFragment());
			mFragments.add(new UserSettingFragment());
		} else {
			mFragments.add(new CloudIntercomFragment());
			mFragments.add(new HomeSecurityFragment());
			mFragments.add(new VisualIntercomFragment());
			mFragments.add(new MainFragment());
			if (ProjectConfigure.smartHomeApk) {
				mFragments.add(new IntelligentHomeThirdApkFragment());
			} else {
				mFragments.add(new IntelligentHomeFragment());
			}
			mFragments.add(new UserSettingFragment());
			mFragments.add(new EntertainmentFragment());
		}
	}

	@Override
	public Fragment getItem(int arg0) {
		return mFragments.get(arg0);
	}

	@Override
	public int getCount() {
		return mFragments.size();
	}
	
	public static int getHomePage() {
		if (ProjectConfigure.project == 2) {
			mHomePage = 0;
		} else {
			mHomePage = 3;
		}
		return mHomePage;
	}
}

package com.dpower.pub.dp2700.fragment;

import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.activity.FunMovieActivity;
import com.dpower.pub.dp2700.activity.FunMusicActivity;
import com.dpower.pub.dp2700.activity.FunPhotoActivity;
import com.dpower.util.ProjectConfigure;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

/**
 * ”È¿÷ÃÏµÿ
 */
public class EntertainmentFragment extends Fragment implements OnClickListener {
	
	private View mRootView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (ProjectConfigure.project == 1) {
			mRootView = inflater.inflate(R.layout.fragment_entertainment1, container,
					false);
		} else if (ProjectConfigure.project == 2) {
			mRootView = inflater.inflate(R.layout.fragment_entertainment2, container,
					false);
		} else {
			mRootView = inflater.inflate(R.layout.fragment_entertainment, container,
					false);
		}
		mRootView.findViewById(R.id.btn_photo).setOnClickListener(this);
		mRootView.findViewById(R.id.btn_music).setOnClickListener(this);
		mRootView.findViewById(R.id.btn_movie).setOnClickListener(this);
		return mRootView;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_photo:
			startActivity(new Intent(getActivity(), FunPhotoActivity.class));
			break;
		case R.id.btn_music:
			startActivity(new Intent(getActivity(), FunMusicActivity.class));
			break;
		case R.id.btn_movie:
			startActivity(new Intent(getActivity(), FunMovieActivity.class));
			break;
		default:
			break;
		}
	}
}

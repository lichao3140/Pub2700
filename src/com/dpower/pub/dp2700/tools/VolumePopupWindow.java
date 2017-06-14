package com.dpower.pub.dp2700.tools;

import com.dpower.pub.dp2700.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

public class VolumePopupWindow {
	
	public boolean isToHide = false;
	private  PopupWindow mPopupWindow;
	private View mPopupView;
	private View mParentView;
	private TextView mTextVolume;
	private Context mContext;
	private int[] mVolumeID = { R.id.image_volume_1,
			R.id.image_volume_2, R.id.image_volume_3,
			R.id.image_volume_4, R.id.image_volume_5,
	};
	private Handler mHandler = new Handler();

	public VolumePopupWindow(Context context, View parentView) {
		super();
		mContext = context;
		mParentView = parentView;
		init();
	}

	private void init() {
		mPopupView = LayoutInflater.from(mContext).inflate(
				R.layout.popup_volume_control, null);
		mPopupWindow = new PopupWindow(mPopupView, 
				(int) mContext.getResources().getDimension(
						R.dimen.volume_layout_width), 
				(int) mContext.getResources().getDimension(
						R.dimen.volume_layout_height), false);
		mPopupWindow.setTouchable(true);
		mPopupWindow.setOutsideTouchable(false);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable(mContext
				.getResources(), (Bitmap) null));
		mTextVolume = (TextView) mPopupView.findViewById(R.id.tv_volume);
	}

	public void show(int vol) {
		for (int i = 0; i < 5; i++) {
			if (i < vol) {
				mPopupView.findViewById(mVolumeID[i]).setBackgroundColor(
						mContext.getResources().getColor(R.color.DarkDeepPurple));
			} else {
				mPopupView.findViewById(mVolumeID[i]).setBackgroundColor(
						mContext.getResources().getColor(R.color.DarkPink));
			}
		}
		mTextVolume.setText(String.format("%02d", vol));
		mHandler.removeCallbacks(runnable);
		mHandler.postDelayed(runnable, 3000);
		mPopupWindow.showAtLocation(mParentView, Gravity.CENTER, 0, 0); // 在屏幕中央显示音量窗口
	}

	Runnable runnable = new Runnable() {

		@Override
		public void run() {
			isToHide = true;
			mPopupWindow.dismiss();
		}
	};
	
	public void cancelPopupWindow(){
		mHandler.removeCallbacks(runnable);
	}
}

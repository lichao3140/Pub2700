package com.dpower.pub.dp2700.dialog;

import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;

public class BaseDialogFragment extends DialogFragment {

	private int mWidth;
	private int mHeight;

	public BaseDialogFragment() {
		mWidth = LayoutParams.WRAP_CONTENT;
		mHeight = LayoutParams.WRAP_CONTENT;
	}

	public void removeTitleBar() {
		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	/**
	 * 设置对话框在屏幕的比例（0-1.0）
	 * @param w
	 * @param h
	 */
	public void setLayout(float w, float h) {
		DisplayMetrics metrics;
		metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay()
				.getMetrics(metrics);
		if (h != 0) {
			mHeight = (int) ((metrics.heightPixels + 48) * h);
		}
		if (w != 0) {
			mWidth = (int) (metrics.widthPixels * w);
		}
		if(h > 1) {
			mHeight = (int) h;
		}
		if(w > 1) {
			mWidth = (int) w;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		getDialog().getWindow().setLayout(mWidth, mHeight);
	}
}

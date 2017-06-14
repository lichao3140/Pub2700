package com.dpower.pub.dp2700.tools;

import com.dpower.pub.dp2700.application.App;

import android.widget.Toast;

public class MyToast {

	private static Toast mToast;

	public static void show(int resId) {
		if (mToast != null) {
			mToast.cancel();
		}
		mToast = Toast.makeText(App.getInstance().getContext(), resId,
				Toast.LENGTH_SHORT);
		mToast.show();
	}

	public static void show(String msg) {
		if (mToast != null) {
			mToast.cancel();
		}
		mToast = Toast.makeText(App.getInstance().getContext(), msg, 
				Toast.LENGTH_SHORT);
		mToast.show();
	}

	public static void show(CharSequence text) {
		show(text.toString());
	}
	
	public static void shortToast(int resId) {
		mToast = Toast.makeText(App.getInstance().getContext(), resId, 
				Toast.LENGTH_SHORT);
		mToast.show();
	}
	
	public static void shortToast(String msg) {
		mToast = Toast.makeText(App.getInstance().getContext(), msg, 
				Toast.LENGTH_SHORT);
		mToast.show();
	}
	
	public static void shortToast(CharSequence text) {
		mToast = Toast.makeText(App.getInstance().getContext(), text, 
				Toast.LENGTH_SHORT);
		mToast.show();
	}
}

package com.dpower.pub.dp2700.fragment;

import com.dpower.pub.dp2700.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class KeyboardNumber6x2Fragment extends BaseFragment implements OnClickListener{

	private View mView;
	private OnNumberKeyboardListener mKeyListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_keyboard_number6x2, container, false);
		setClickListener(R.id.key_1,"1");
		setClickListener(R.id.key_2,"2");
		setClickListener(R.id.key_3,"3");
		setClickListener(R.id.key_4,"4");
		setClickListener(R.id.key_5,"5");
		setClickListener(R.id.key_6,"6");
		setClickListener(R.id.key_7,"7");
		setClickListener(R.id.key_8,"8");
		setClickListener(R.id.key_9,"9");
		setClickListener(R.id.key_0,"0");
		setClickListener(R.id.key_dot,".");
		setClickListener(R.id.key_del,"-1");
		return mView;
	}

	private void setClickListener(int resId, String tag) {
		View button;
		button = mView.findViewById(resId);
		button.setTag(tag);
		button.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (mKeyListener != null) {
			mKeyListener.onKeyboardClick(v.getTag().toString());
		}
	}
	
	public interface OnNumberKeyboardListener {
		public void onKeyboardClick(String key);
	}
	
	public void setOnKeyboardListener(OnNumberKeyboardListener keyListener) {
		mKeyListener = keyListener;
	}
}

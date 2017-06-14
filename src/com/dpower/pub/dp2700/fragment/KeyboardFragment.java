package com.dpower.pub.dp2700.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.dpower.pub.dp2700.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * @author ZhengZhiying
 * @Funtion º¸≈Ã
 */
public class KeyboardFragment extends BaseFragment implements OnClickListener {

	private View mView;
	private Boolean mIsUpper = false; //  «¥Û–¥
	private List<Button> mButtons = new ArrayList<Button>();
	private OnKeyboardListener mKeyListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_keyboard, container, false);
		//  ˝◊÷º¸
		setClickListener(R.id.bt_keyboard_number_1, "1");
		setClickListener(R.id.bt_keyboard_number_2, "2");
		setClickListener(R.id.bt_keyboard_number_3, "3");
		setClickListener(R.id.bt_keyboard_number_4, "4");
		setClickListener(R.id.bt_keyboard_number_5, "5");
		setClickListener(R.id.bt_keyboard_number_6, "6");
		setClickListener(R.id.bt_keyboard_number_7, "7");
		setClickListener(R.id.bt_keyboard_number_8, "8");
		setClickListener(R.id.bt_keyboard_number_9, "9");
		setClickListener(R.id.bt_keyboard_number_0, "0");
		setClickListener(R.id.bt_keyboard_dot, ".");
		// ◊÷ƒ∏º¸
		setClickListener(R.id.bt_keyboard_lowercase_a, "a");
		setClickListener(R.id.bt_keyboard_lowercase_b, "b");
		setClickListener(R.id.bt_keyboard_lowercase_c, "c");
		setClickListener(R.id.bt_keyboard_lowercase_d, "d");
		setClickListener(R.id.bt_keyboard_lowercase_e, "e");
		setClickListener(R.id.bt_keyboard_lowercase_f, "f");
		setClickListener(R.id.bt_keyboard_lowercase_g, "g");
		setClickListener(R.id.bt_keyboard_lowercase_h, "h");
		setClickListener(R.id.bt_keyboard_lowercase_i, "i");
		setClickListener(R.id.bt_keyboard_lowercase_j, "j");
		// 11-20
		setClickListener(R.id.bt_keyboard_lowercase_k, "k");
		setClickListener(R.id.bt_keyboard_lowercase_l, "l");
		setClickListener(R.id.bt_keyboard_lowercase_m, "m");
		setClickListener(R.id.bt_keyboard_lowercase_n, "n");
		setClickListener(R.id.bt_keyboard_lowercase_o, "o");
		setClickListener(R.id.bt_keyboard_lowercase_p, "p");
		setClickListener(R.id.bt_keyboard_lowercase_q, "q");
		setClickListener(R.id.bt_keyboard_lowercase_r, "r");
		setClickListener(R.id.bt_keyboard_lowercase_s, "s");
		setClickListener(R.id.bt_keyboard_lowercase_t, "t");
		// 21-26
		setClickListener(R.id.bt_keyboard_lowercase_u, "u");
		setClickListener(R.id.bt_keyboard_lowercase_v, "v");
		setClickListener(R.id.bt_keyboard_lowercase_w, "w");
		setClickListener(R.id.bt_keyboard_lowercase_x, "x");
		setClickListener(R.id.bt_keyboard_lowercase_y, "y");
		setClickListener(R.id.bt_keyboard_lowercase_z, "z");
		

		// …æ≥˝º¸
		setClickListener(R.id.bt_del, "-1");
		setClickListener(R.id.bt_switch, "");
		return mView;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.bt_switch) {
			mIsUpper = !mIsUpper;
			changeKey();
		} else if (mKeyListener != null) {
			mKeyListener.onKeyboardClick(v.getTag().toString());
		}
	}

	private void changeKey() {
		if(mIsUpper) {
			for(Button button : mButtons) {
				button.setText(button.getTag().toString().toUpperCase(Locale.US));
				button.setTag(button.getText().toString());
			}
		} else {
			for(Button button : mButtons) {
				button.setText(button.getTag().toString().toLowerCase(Locale.US));
				button.setTag(button.getText().toString());
			}
		}
	}

	private void setClickListener(int resId, String tag) {
		View button;
		button = mView.findViewById(resId);
		button.setTag(tag);
		button.setOnClickListener(this);
		if(tag.length() == 1 && isWord(tag)) {
			mButtons.add((Button)button);
		}
	}

    private boolean isWord(String str) {
    	String wordstr = "abcdefghijklmnopqrstuvwxyz";
    	if (wordstr.indexOf(str.toLowerCase(Locale.US)) > -1) {
			return true;
		}
    	return false;
    }
    
    public interface OnKeyboardListener {
		public void onKeyboardClick(String key);
	}
    
    public void setOnKeyboardListener(OnKeyboardListener keyListener) {
		mKeyListener = keyListener;
	}
}

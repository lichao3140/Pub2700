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
 * @Funtion 键盘
 */
public class KeyboardFragment extends BaseFragment implements OnClickListener {

	private View mView;
	private Boolean mIsUpper = false; // 是大写
	private Boolean mIsSymbol = false;// 是特殊符号
	private List<Button> mButtons = new ArrayList<Button>();
	private OnKeyboardListener mKeyListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_keyboard, container, false);
		// 数字键
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
		// 字母键
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
		
		// 删除键
		setClickListener(R.id.bt_del, "-1");
		// 切换键
		setClickListener(R.id.bt_switch, "");
		setClickListener(R.id.bt_special_symbol, "");
		return mView;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.bt_switch) {
			mIsUpper = !mIsUpper;
			changeKey();
		} else if (v.getId() == R.id.bt_special_symbol) {
			mIsSymbol = !mIsSymbol;
			changeSymbol();
		} else if (mKeyListener != null) {
			mKeyListener.onKeyboardClick(v.getTag().toString());
		} 
		
	}

	private void changeSymbol() {
		if(mIsSymbol) {//特殊符号
			changeSymbol(R.id.bt_keyboard_lowercase_q, "=");
			changeSymbol(R.id.bt_keyboard_lowercase_w, "/");
			changeSymbol(R.id.bt_keyboard_lowercase_e, "+");
			changeSymbol(R.id.bt_keyboard_lowercase_r, "?");
			changeSymbol(R.id.bt_keyboard_lowercase_t, ",");
			changeSymbol(R.id.bt_keyboard_lowercase_y, "-");
			changeSymbol(R.id.bt_keyboard_lowercase_u, "'");
			changeSymbol(R.id.bt_keyboard_lowercase_i, "{");
			changeSymbol(R.id.bt_keyboard_lowercase_o, "}");
			changeSymbol(R.id.bt_keyboard_lowercase_p, "*");
			
			changeSymbol(R.id.bt_keyboard_lowercase_a, "@");
			changeSymbol(R.id.bt_keyboard_lowercase_s, "¥");
			changeSymbol(R.id.bt_keyboard_lowercase_d, "&");
			changeSymbol(R.id.bt_keyboard_lowercase_f, "_");
			changeSymbol(R.id.bt_keyboard_lowercase_g, "(");
			changeSymbol(R.id.bt_keyboard_lowercase_h, ")");
			changeSymbol(R.id.bt_keyboard_lowercase_j, ":");
			changeSymbol(R.id.bt_keyboard_lowercase_k, ";");
			changeSymbol(R.id.bt_keyboard_lowercase_l, "\"");
			
			changeSymbol(R.id.bt_keyboard_lowercase_z, "!");
			changeSymbol(R.id.bt_keyboard_lowercase_x, "[");
			changeSymbol(R.id.bt_keyboard_lowercase_c, "]");
			changeSymbol(R.id.bt_keyboard_lowercase_v, "<");
			changeSymbol(R.id.bt_keyboard_lowercase_b, ">");
			changeSymbol(R.id.bt_keyboard_lowercase_n, "~");
			changeSymbol(R.id.bt_keyboard_lowercase_m, "^");
			
		} else {
			changeSymbol(R.id.bt_keyboard_lowercase_q, "q");
			changeSymbol(R.id.bt_keyboard_lowercase_w, "w");
			changeSymbol(R.id.bt_keyboard_lowercase_e, "e");
			changeSymbol(R.id.bt_keyboard_lowercase_r, "r");
			changeSymbol(R.id.bt_keyboard_lowercase_t, "t");
			changeSymbol(R.id.bt_keyboard_lowercase_y, "y");
			changeSymbol(R.id.bt_keyboard_lowercase_u, "u");
			changeSymbol(R.id.bt_keyboard_lowercase_i, "i");
			changeSymbol(R.id.bt_keyboard_lowercase_o, "o");
			changeSymbol(R.id.bt_keyboard_lowercase_p, "p");
			
			changeSymbol(R.id.bt_keyboard_lowercase_a, "a");
			changeSymbol(R.id.bt_keyboard_lowercase_s, "s");
			changeSymbol(R.id.bt_keyboard_lowercase_d, "d");
			changeSymbol(R.id.bt_keyboard_lowercase_f, "f");
			changeSymbol(R.id.bt_keyboard_lowercase_g, "g");
			changeSymbol(R.id.bt_keyboard_lowercase_h, "h");
			changeSymbol(R.id.bt_keyboard_lowercase_j, "j");
			changeSymbol(R.id.bt_keyboard_lowercase_k, "k");
			changeSymbol(R.id.bt_keyboard_lowercase_l, "l");
			
			changeSymbol(R.id.bt_keyboard_lowercase_z, "z");
			changeSymbol(R.id.bt_keyboard_lowercase_x, "x");
			changeSymbol(R.id.bt_keyboard_lowercase_c, "c");
			changeSymbol(R.id.bt_keyboard_lowercase_v, "v");
			changeSymbol(R.id.bt_keyboard_lowercase_b, "b");
			changeSymbol(R.id.bt_keyboard_lowercase_n, "n");
			changeSymbol(R.id.bt_keyboard_lowercase_m, "m");
		}
		
	}

	private void changeKey() {
		if(mIsUpper) {
			for(Button button : mButtons) {//大写
				button.setText(button.getTag().toString().toUpperCase(Locale.US));
				button.setTag(button.getText().toString());
			}
		} else {
			for(Button button : mButtons) {//小写
				button.setText(button.getTag().toString().toLowerCase(Locale.US));
				button.setTag(button.getText().toString());
			}
		}
	}
	
	private void changeSymbol(int resId, String tag) {
		Button button;
		button = (Button) mView.findViewById(resId);
		button.setTag(tag);
		button.setText(tag);
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

package com.dpower.pub.dp2700.activity;

import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.tools.EditTextTool;
import com.dpower.pub.dp2700.tools.MyToast;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.pub.dp2700.view.MyEditText;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

/**
 * 开锁设置
 */
public class UnlockSetActivity extends BaseActivity implements OnClickListener {
	private static final String TAG = "UnlockSetActivity";
	
	private EditTextTool mEditTool;
	private MyEditText mEditExtension;
	private MyEditText mEditUnlockDelay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_unlock_set);
		init();
	}

	private void init() {
		mEditTool = EditTextTool.getInstance();
		mEditExtension = (MyEditText) findViewById(R.id.et_villa_extension_num);
		mEditUnlockDelay = (MyEditText) findViewById(R.id.et_unlock_delay);
		findViewById(R.id.btn_back).setOnClickListener(this);
		findViewById(R.id.btn_confirm).setOnClickListener(this);
		setKeyboardClickListener(R.id.bt_num_1, "1");
		setKeyboardClickListener(R.id.bt_num_2, "2");
		setKeyboardClickListener(R.id.bt_num_3, "3");
		setKeyboardClickListener(R.id.bt_num_4, "4");
		setKeyboardClickListener(R.id.bt_num_5, "5");
		setKeyboardClickListener(R.id.bt_num_6, "6");
		setKeyboardClickListener(R.id.bt_num_7, "7");
		setKeyboardClickListener(R.id.bt_num_8, "8");
		setKeyboardClickListener(R.id.bt_num_9, "9");
		setKeyboardClickListener(R.id.bt_num_0, "0");
		setKeyboardClickListener(R.id.btn_delete, "-1");
	}
	
	private void setKeyboardClickListener(int resId, String tag) {
		View button;
		button = findViewById(resId);
		button.setTag(tag);
		button.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		findViewById(R.id.root_view).setBackground(
				SPreferences.getInstance().getWallpaper());
	}

	@Override
	public void onClick(View v) {
		if (v.getTag() != null) {
			onKeyboardClick(v.getTag().toString());
		}
		switch (v.getId()) {
			case R.id.btn_back:
				finish();
				break;
			case R.id.btn_confirm:
				checkInput();
				break;
			default:
				break;
		}
	}
	
	private void onKeyboardClick(String key) {
		if (mEditExtension.hasFocus()) {
			if (key.equals("-1") || mEditExtension.getText().toString().length() < 2) {
				editText(mEditExtension, key);
			}
		} else if (mEditUnlockDelay.hasFocus()) {
			if (key.equals("-1") || mEditUnlockDelay.getText().toString().length() < 2) {
				editText(mEditUnlockDelay, key);
			}
		}
	}
	
	private void editText(EditText editText, String key){
		mEditTool.setEditText(editText);
		if (key.equals("-1")) {
			mEditTool.deleteText();
		} else {
			mEditTool.appendTextTo(key);
		}
	}

	private void checkInput() {
		int result = 0;
		// 判断别墅机号输入正确
		result = checkEmptyOrZero(mEditExtension);
		if (result == -1) {
			MyToast.show(R.string.input_extension);
			return;
		}
		if (result == 0) {
			MyToast.show(R.string.no_input_extension);
			return;
		}
		int extensionNum = result;
		// 判断开锁延时
		result = checkEmptyOrZero(mEditUnlockDelay);
		if (result == -1) {
			MyToast.show(R.string.input_unlock_time);
			return;
		}
		if (result == 0) {
			MyToast.show(R.string.input_correct_unlock_time);
			return;
		}
		int unlockDelay = result;
		result = DPFunction.toDoorSetLockParam(extensionNum, unlockDelay, -1, -1);
		Log.i(TAG, "result = " + result);
		if (result == 0) {
			MyToast.show(R.string.set_success);
			finish();
		} else if (result == -1) {
			MyToast.show(R.string.no_this_villa_gate_number);
		} else {
			MyToast.show(R.string.set_fail);
		}
	}

	private int checkEmptyOrZero(EditText editText) {
		String strNum = editText.getText().toString();
		if (TextUtils.isEmpty(strNum)) {
			return -1;
		}
		if (Integer.parseInt(strNum) <= 0) {
			return 0;
		}
		return Integer.parseInt(strNum);
	}
}

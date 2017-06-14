package com.dpower.pub.dp2700.fragment;

import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.tools.EditTextTool;
import com.dpower.pub.dp2700.tools.MyToast;
import com.dpower.pub.dp2700.view.MyEditText;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class SecurityPasswordFragment extends BaseFragment 
		implements OnClickListener {

	private View mRootView;
	private Button mDisAlarm;
	private Button mHold;
	private MyEditText mOldPassword, mNewPassword, mConfirmPassword;
	private boolean mIsHolding = false;
	private EditTextTool mEditTool;
	private String mCurrentOldPassword;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_security_password, 
				container, false);
		init();
		return mRootView;
	}

	private void init() {
		mEditTool = EditTextTool.getInstance();
		mDisAlarm = (Button) mRootView.findViewById(R.id.btn_disalarm_password);
		mHold = (Button) mRootView.findViewById(R.id.btn_hold_password);
		mDisAlarm.setOnClickListener(this);
		mHold.setOnClickListener(this);
		mOldPassword = (MyEditText) mRootView.findViewById(R.id.et_old_password);
		mNewPassword = (MyEditText) mRootView.findViewById(R.id.et_new_password);
		mConfirmPassword = (MyEditText) mRootView.findViewById(R.id.et_again_new_password);
		mRootView.findViewById(R.id.btn_ok).setOnClickListener(this);
		setKeyboardClickListener(R.id.btn_num_1, "1");
		setKeyboardClickListener(R.id.btn_num_2, "2");
		setKeyboardClickListener(R.id.btn_num_3, "3");
		setKeyboardClickListener(R.id.btn_num_4, "4");
		setKeyboardClickListener(R.id.btn_num_5, "5");
		setKeyboardClickListener(R.id.btn_num_6, "6");
		setKeyboardClickListener(R.id.btn_num_7, "7");
		setKeyboardClickListener(R.id.btn_num_8, "8");
		setKeyboardClickListener(R.id.btn_num_9, "9");
		setKeyboardClickListener(R.id.btn_num_0, "0");
		setKeyboardClickListener(R.id.btn_delete, "-1");
		mDisAlarm.performClick();
	}
	
	private void setKeyboardClickListener(int resId, String tag) {
		View button;
		button = mRootView.findViewById(resId);
		button.setTag(tag);
		button.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getTag() != null) {
			onKeyboardClick(v.getTag().toString());
		}
		switch (v.getId()) {
			case R.id.btn_disalarm_password:
				mIsHolding = false;
				mCurrentOldPassword = DPFunction.getSafePassword(false);
				mDisAlarm.setBackgroundResource(R.color.Purple);
				mHold.setBackgroundResource(R.color.DarkBlue);
				mOldPassword.setText("");
				mNewPassword.setText("");
				mConfirmPassword.setText("");
				break;
			case R.id.btn_hold_password:
				mIsHolding = true;
				mCurrentOldPassword = DPFunction.getSafePassword(true);
				mHold.setBackgroundResource(R.color.Purple);
				mDisAlarm.setBackgroundResource(R.color.DarkBlue);
				mOldPassword.setText("");
				mNewPassword.setText("");
				mConfirmPassword.setText("");
				break;
			case R.id.btn_ok:
				changePassword();
				break;
			default:
				break;
		}
	}
	
	private void onKeyboardClick(String key) {
		if (mOldPassword.hasFocus()) {
			if (key.equals("-1") || mOldPassword.getText().toString().length() < 6) {
				editText(mOldPassword, key);
			}
		} else if (mNewPassword.hasFocus()) {
			if (key.equals("-1") || mNewPassword.getText().toString().length() < 6) {
				editText(mNewPassword, key);
			}
		} else if (mConfirmPassword.hasFocus()) {
			if (key.equals("-1") || mConfirmPassword.getText().toString().length() < 6) {
				editText(mConfirmPassword, key);
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

	/**
	 * 修改密码
	 */
	private void changePassword() {
		if (checkInput()) {
			if (mIsHolding) { // 修改挟持密码
				// 如果当前撤防密码和将要修改挟持密码相同则修改失败
				if (DPFunction.getSafePassword(false).equals(
						mNewPassword.getText().toString())) {
					MyToast.show(R.string.seized_cannot_same_as_cancel);
				} else {
					if (DPFunction.setSafePassword(true, mNewPassword.getText()
							.toString())) {
						MyToast.show(R.string.change_succeeded);
					} else {
						MyToast.show(R.string.change_failed);
					}
				}
			} else { // 修改撤防密码
				if (DPFunction.getSafePassword(true).equals(
						mNewPassword.getText().toString())) {
					MyToast.show(R.string.seized_cannot_same_as_cancel);
				} else {
					if (DPFunction.setSafePassword(false, mNewPassword.getText()
							.toString())) {
						MyToast.show(R.string.change_succeeded);
					} else {
						MyToast.show(R.string.change_failed);
					}
				}
			}
		}
	}
	
	/**
	 * 验证输入是否有误
	 */
	private boolean checkInput() {
		if (mOldPassword.getText().toString().length() != 6
				|| mNewPassword.getText().toString().length() != 6
				|| mConfirmPassword.getText().toString().length() != 6) {
			MyToast.show(R.string.input_six_password);
			return false;
		}
		if (!mNewPassword.getText().toString()
				.equals(mConfirmPassword.getText().toString())) {
			MyToast.show(R.string.password_not_same);
			return false;
		}
		if (!mOldPassword.getText().toString().equals(mCurrentOldPassword)) {
			MyToast.show(R.string.local_password_error);
			return false;
		}
		return true;
	}
}

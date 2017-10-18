package com.dpower.pub.dp2700.activity;

import java.util.ArrayList;
import com.dpower.domain.AddrInfo;
import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.tools.EditTextTool;
import com.dpower.pub.dp2700.tools.MyToast;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.pub.dp2700.view.MyEditText;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

/**
 * 用户设置---开锁密码
 */
public class UnlockPasswordActivity extends BaseFragmentActivity 
		implements OnClickListener {

	private EditTextTool mEditTool;
	private MyEditText mEditPassword;
	private MyEditText mEditPasswordAgain;
	private RadioGroup mRadioGroup;
	
	private ArrayList<AddrInfo> monitorLists;
	private String doorIpAddr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_unlock_password);
		init();
	}

	private void init() {
		monitorLists = DPFunction.getCellSeeList();
		doorIpAddr = monitorLists.get(0).getIp();
		mEditTool = EditTextTool.getInstance();
		mEditPassword = (MyEditText) findViewById(R.id.et_password);
		mEditPasswordAgain = (MyEditText) findViewById(R.id.et_password_again);
		findViewById(R.id.btn_back).setOnClickListener(this);
		findViewById(R.id.btn_confirm).setOnClickListener(this);
		mRadioGroup = (RadioGroup) findViewById(R.id.rd_door_no);
		mRadioGroup.setOnCheckedChangeListener(listener);
		
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
		if (mEditPassword.hasFocus()) {
			if (key.equals("-1") || mEditPassword.getText().toString().length() < 6) {
				editText(mEditPassword, key);
			}
		} else if (mEditPasswordAgain.hasFocus()) {
			if (key.equals("-1") || mEditPasswordAgain.getText().toString().length() < 6) {
				editText(mEditPasswordAgain, key);
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
		if (mEditPassword.getText().toString().equals("")) {
			MyToast.show(R.string.wifi_password_input);
			return;
		}
		if (mEditPassword.getText().toString().length() != 6) {
			MyToast.show(R.string.input_six_password);
			return;
		}
		if (!mEditPassword.getText().toString().equals(
				mEditPasswordAgain.getText().toString())) {
			MyToast.show(R.string.password_not_same);
			return;
		}
		int result = DPFunction.toDoorModifyPassWord(doorIpAddr,
				mEditPassword.getText().toString());
		if (result == 0) {
			MyToast.show(R.string.change_succeeded);
			finish();
		} else {
			MyToast.show(R.string.change_failed);
		}
	}
	
	private OnCheckedChangeListener listener = new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			switch (group.getCheckedRadioButtonId()) {
				case R.id.rdb_door_1:
					if (monitorLists.size() >= 1) {
						doorIpAddr = monitorLists.get(0).getIp();
					} else {
						MyToast.shortToast(R.string.change_entrance_machine_not_exsit);
					}
					break;
				case R.id.rdb_door_2:
					if (monitorLists.size() >= 2) {
						doorIpAddr = monitorLists.get(1).getIp();
					} else {
						MyToast.shortToast(R.string.change_entrance_machine_not_exsit);
					}
					break;
				case R.id.rdb_door_3:
					if (monitorLists.size() >= 3) {
						doorIpAddr = monitorLists.get(2).getIp();
					} else {
						MyToast.shortToast(R.string.change_entrance_machine_not_exsit);
					}
					break;
			}
		}
	};
}

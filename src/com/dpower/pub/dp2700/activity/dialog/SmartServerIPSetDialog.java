package com.dpower.pub.dp2700.activity.dialog;

import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.activity.BaseFragmentActivity;
import com.dpower.pub.dp2700.fragment.KeyboardNumberFragment;
import com.dpower.pub.dp2700.fragment.KeyboardNumberFragment.OnNumberKeyboardListener;
import com.dpower.pub.dp2700.tools.EditTextTool;
import com.dpower.pub.dp2700.tools.MyToast;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.util.CommonUT;
import com.dpower.util.MyLog;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.LinearLayout;

/**
 * 第三方服务器设置窗口
 */
public class SmartServerIPSetDialog extends BaseFragmentActivity 
		implements OnClickListener {
	private static final String TAG = "SmartServerIPSetDialog";
	
	private LinearLayout mInfoWindow;
	private KeyboardNumberFragment mKeyboard;
	private EditTextTool mEditTool;
	private EditText mEditIpAddress;
	private LinearLayout mLinearUsername;
	private LinearLayout mLinearPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_camera_set);
		init();
	}
	
	private void init() {
		findViewById(R.id.screen_window).setOnClickListener(this);
		mInfoWindow = (LinearLayout) findViewById(R.id.info_window);
		mInfoWindow.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				MyLog.print(TAG, "mInfoWindow onTouch");
				return true;
			}
		});
		findViewById(R.id.btn_confirm).setOnClickListener(this);
		findViewById(R.id.btn_cancel).setOnClickListener(this);
		mEditIpAddress = (EditText) findViewById(R.id.et_ip_address);
		mLinearUsername = (LinearLayout) findViewById(R.id.linear_username);
		mLinearPassword = (LinearLayout) findViewById(R.id.linear_password);
		SPreferences sp = SPreferences.getInstance();
		mEditIpAddress.setText(sp.getSmartServerIP());
		mEditIpAddress.setSelection(mEditIpAddress.getText().length());
		mLinearUsername.setVisibility(View.GONE);
		mLinearPassword.setVisibility(View.GONE);
		mEditTool = EditTextTool.getInstance();
        mKeyboard = new KeyboardNumberFragment();
        getSupportFragmentManager().beginTransaction().replace(
        		R.id.frame_keyboard, mKeyboard).commitAllowingStateLoss();
        setKeyboardListener();
	}

	private void setKeyboardListener() {
		mKeyboard.setOnKeyboardListener(new OnNumberKeyboardListener() {
			
			@Override
			public void onKeyboardClick(String key) {
				if (mEditIpAddress.hasFocus()) {
					editText(mEditIpAddress, key);
				}
			}
		});
	}

	private void editText(EditText editText, String key){
		mEditTool.setEditText(editText);
		if (key.equals("-1")) {
			mEditTool.deleteText();
		} else {
			mEditTool.appendTextTo(key);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_confirm:
				String ip = mEditIpAddress.getText().toString().trim();
				if (TextUtils.isEmpty(ip)) {
					MyToast.show(R.string.wifi_ip_settings_empty_ip_address);
					return;
				}
				if (!CommonUT.isIp(ip)) {
					MyToast.show(R.string.ip_illegal);
					return;
				}
				SPreferences sp = SPreferences.getInstance();
				sp.saveSmartServerIP(ip);
				MyToast.show(R.string.set_success);
				finish();
				break;
			case R.id.screen_window:
			case R.id.btn_cancel:
				finish();
				break;
			default:
				break;
		}
	}
}

package com.dpower.pub.dp2700.activity.dialog;

import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.activity.BaseFragmentActivity;
import com.dpower.pub.dp2700.fragment.KeyboardFragment;
import com.dpower.pub.dp2700.fragment.KeyboardFragment.OnKeyboardListener;
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
 * ÍøÂçÉãÏñÍ·ÉèÖÃ´°¿Ú
 */
public class WebCameraSetDialog extends BaseFragmentActivity 
		implements OnClickListener {
	private static final String TAG = "WebCameraSetDialog";
	
	private LinearLayout mInfoWindow;
	private KeyboardFragment mKeyboard;
	private EditTextTool mEditTool;
	private EditText mEditIpAddress;
	private EditText mEditUsername;
	private EditText mEditPassword;
	
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
		mEditUsername = (EditText) findViewById(R.id.et_username);
		mEditPassword = (EditText) findViewById(R.id.et_password);
		SPreferences sp = SPreferences.getInstance();
		mEditIpAddress.setText(sp.getWebCameraIP());
		mEditUsername.setText(sp.getWebCameraUserName());
		mEditPassword.setText(sp.getWebCameraPassword());
		mEditIpAddress.setSelection(mEditIpAddress.getText().length());
		mEditUsername.setSelection(mEditUsername.getText().length());
		mEditPassword.setSelection(mEditPassword.getText().length());
		mEditTool = EditTextTool.getInstance();
        mKeyboard = new KeyboardFragment();
        getSupportFragmentManager().beginTransaction().replace(
        		R.id.frame_keyboard, mKeyboard).commitAllowingStateLoss();
        setKeyboardListener();
	}

	private void setKeyboardListener() {
		mKeyboard.setOnKeyboardListener(new OnKeyboardListener() {
			
			@Override
			public void onKeyboardClick(String key) {
				if (mEditUsername.hasFocus()) {
					editText(mEditUsername, key);
				} else if (mEditPassword.hasFocus()) {
					editText(mEditPassword, key);
				} else if (mEditIpAddress.hasFocus()) {
					if (key.equals("-1") || key.equals(".") || isNumber(key)) {
						editText(mEditIpAddress, key);
					}
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
	
	private boolean isNumber(String key) {
		try {
			if (Integer.parseInt(key) >= 0 && Integer.parseInt(key) < 10) return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_confirm:
				String ip = mEditIpAddress.getText().toString().trim();
				String username = mEditUsername.getText().toString().trim();
				String password = mEditPassword.getText().toString().trim();
				if (TextUtils.isEmpty(ip)) {
					MyToast.show(R.string.wifi_ip_settings_empty_ip_address);
					return;
				}
				if (TextUtils.isEmpty(username)) {
					MyToast.show(R.string.user_name_shouldnot_null);
					return;
				}
				if (TextUtils.isEmpty(password)) {
					MyToast.show(R.string.password_shouldnot_null);
					return;
				}
				if (!CommonUT.isIp(ip)) {
					MyToast.show(R.string.ip_illegal);
					return;
				}
				SPreferences sp = SPreferences.getInstance();
				sp.saveWebCameraIP(ip);
				sp.saveWebCameraUserName(username);
				sp.saveWebCameraPassword(password);
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

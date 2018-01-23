package com.dpower.pub.dp2700.activity.dialog;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.activity.BaseFragmentActivity;
import com.dpower.pub.dp2700.activity.SystemSetActivity;
import com.dpower.pub.dp2700.application.App;
import com.dpower.pub.dp2700.fragment.KeyboardNumberFragment;
import com.dpower.pub.dp2700.fragment.KeyboardNumberFragment.OnNumberKeyboardListener;
import com.dpower.pub.dp2700.tools.EditTextTool;
import com.dpower.pub.dp2700.tools.MyToast;
import com.dpower.util.ConstConf;
import com.dpower.util.MyLog;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.RelativeLayout;

/**
 * ¼ì²éÃÜÂë´°¿Ú
 */
public class CheckPasswordDialog extends BaseFragmentActivity 
		implements OnClickListener {
	private static final String TAG = "CheckPasswordDialog";
	
	private RelativeLayout mInfoWindow;
	private EditText mEditPassword;
	private KeyboardNumberFragment mKeyboard;
	private EditTextTool mEditTool;
	private String mAction;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_check_password);
		mContext = this;
		init();
	}

	private void init() {
		mAction = getIntent().getAction();
		if (mAction == null) {
			mAction = "default";
		}
		mEditPassword = (EditText) findViewById(R.id.et_password);
		findViewById(R.id.screen_window).setOnClickListener(this);
		mInfoWindow = (RelativeLayout) findViewById(R.id.info_window);
		mInfoWindow.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				MyLog.print(TAG, "mInfoWindow onTouch");
				return true;
			}
		});
		findViewById(R.id.btn_confirm).setOnClickListener(this);
		findViewById(R.id.btn_cancel).setOnClickListener(this);
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
				if (mEditPassword.hasFocus()) {
					editText(mEditPassword, key);
				}
			}
		});
	}
	
	private void editText(EditText editText, String key){
		mEditTool.setEditText(editText);
		if (key.equals("-1")) {
			mEditTool.deleteText();
		} else {
			if (editText.getText().toString().length() < 6) {
				mEditTool.appendTextTo(key);
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_confirm:
				if (TextUtils.isEmpty(mEditPassword.getText().toString().trim())) {
					MyToast.show(R.string.wifi_password_input);
					return;
				}
				
				String password = null;
				if (mAction.equals("disalarm")) {
					password = DPFunction.getSafePassword(false);
				} else if (mAction.equals("systemSet")) {
					password = DPFunction.getPsdProjectSetting();
				} else if (mAction.equals("existApp")) {
					App.exit();
					//App.amForceAppProcess("com.dpower.pub.dp2700");
					//App.killProcess(getProcessID(mContext, "com.dpower.pub.dp2700"));
					return;
				}
				
				if (mEditPassword.getText().toString().trim().equals(password)) {
					if (mAction.equals("disalarm")) {
						Intent intent = new Intent(mContext, HomeSecurityModeDelayDialog.class);
						intent.putExtra("modeflag", ConstConf.UNSAFE_MODE);
						startActivity(intent);
					} else {
						startActivity(new Intent(mContext, SystemSetActivity.class));
					}
					finish();
				} else {
					MyToast.show(R.string.password_error);
				}
				break;
			case R.id.screen_window:
			case R.id.btn_cancel:
				finish();
				break;
			default:
				break;
		}
	}
	
	public int getProcessID(Context ctx, String packageName) {
		ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> infoList = am.getRunningAppProcesses();
		int pid = -1;
		if (infoList == null) {
			return pid;
		}
		
		for (RunningAppProcessInfo info : infoList) {
			if (info.processName.equals(packageName)) {
				pid = info.pid;
			}
		}
		return pid;
    }
}

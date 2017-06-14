package com.dpower.pub.dp2700.activity.dialog;

import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.activity.BaseActivity;
import com.dpower.util.ConstConf;
import com.dpower.util.MyLog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 启动安防模式确定窗口
 */
public class HomeSecurityModeClickedDialog extends BaseActivity implements OnClickListener {
	private static final String TAG = "HomeSecurityModeClickedDialog";
	
	private RelativeLayout mInfoWindow;
	private TextView mMode;
	private int mModeflag;
	private Context mContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mode_clicked);
		mContext = this;
		init();
	}

	private void init() {
		mMode = (TextView) findViewById(R.id.tv_mode);
	    mModeflag = getIntent().getIntExtra("modeflag", ConstConf.UNSAFE_MODE);
		String  string = null;
		switch (mModeflag) {
			case ConstConf.HOME_MODE:
				 string = getString(R.string.mode_at_home);
				break;
			case ConstConf.NIGHT_MODE:
				 string = getString(R.string.mode_in_hight);
				break;
			case ConstConf.LEAVE_HOME_MODE:
				 string = getString(R.string.mode_leave_home);
				break;
			case ConstConf.UNSAFE_MODE:
				 string = getString(R.string.mode_out_of_security);
				break;
			default:
				break;
		}
		mMode.setText(string);
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
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_confirm:
				if (mModeflag == ConstConf.UNSAFE_MODE) {	
					Intent disalarm = new Intent(mContext, CheckPasswordDialog.class);
					disalarm.setAction("disalarm");
		   			startActivity(disalarm);
				} else {
					Intent intent = new Intent(mContext, HomeSecurityModeDelayDialog.class);	
		   			intent.putExtra("modeflag", mModeflag);
		   			startActivity(intent);
				}
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

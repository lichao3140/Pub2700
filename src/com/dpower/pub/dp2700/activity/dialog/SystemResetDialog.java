package com.dpower.pub.dp2700.activity.dialog;

import java.io.File;
import java.io.IOException;

import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.activity.BaseActivity;
import com.dpower.pub.dp2700.tools.FileOperate;
import com.dpower.util.ConstConf;
import com.dpower.util.MyLog;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * ϵͳ��λ����
 */
public class SystemResetDialog extends BaseActivity implements OnClickListener {
	private static final String TAG = "SystemResetDialog";
	
	private RelativeLayout mInfoWindow;
	private TextView mTitle;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_system_reset);
		init();
	}

	private void init() {
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
		mTitle = (TextView) findViewById(R.id.tv_title);
		mTitle.setText(R.string.tips_is_reset_all_data);
	}

	private void reset() {
		// ɾ�����ݿ�
		String cmd = "rm -rf /data/data/com.dpower.pub.dp2700/files";
		try {
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			MyLog.print("cmd", "error" + e);
		}
		// ɾ��SharedPreferences�ļ�
		cmd = "rm -rf /data/data/com.dpower.pub.dp2700/shared_prefs";
		try {
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			MyLog.print("cmd", "error" + e);
		}
		// ɾ���绰����
		File root = new File(ConstConf.RING_PATH);
		if (root.exists()) {
			FileOperate.recursionDeleteFile(root);
		}
		// ɾ���������ñ�
		root = new File(ConstConf.NET_CFG_PATH);
		if (root.exists()) {
			root.delete();
		}
		// ɾ�����������ļ�
		root = new File(ConstConf.SAFE_ALARM_PATH);
		if (root.exists()) {
			FileOperate.recursionDeleteFile(root);
		}
		// ɾ��ҵ������
		root = new File("/mnt/sdcard/leavemessage");
		if (root.exists()) {
			FileOperate.recursionDeleteFile(root);
		}
		cmd = "rm -rf /system/media/backup/userleave.wav";
		try {
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			MyLog.print("cmd", "error" + e);
		}
		// ɾ��С����Ϣ
		root = new File(ConstConf.MESSAGE_PATH);
		if (root.exists()) {
			FileOperate.recursionDeleteFile(root);
		}
		// ɾ����Ӱ����
		root = new File(ConstConf.VISIT_PATH);
		if (root.exists()) {
			FileOperate.recursionDeleteFile(root);
		}
		//ɾ������¼��
		root = new File(ConstConf.ALARM_VIDEO_PATH);
		if (!root.exists()) {
			FileOperate.recursionDeleteFile(root);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_confirm:
				reset();
				String cmd = "su -c reboot";
				try {
					Runtime.getRuntime().exec(cmd);
				} catch (IOException e) {
					MyLog.print("cmd", "error" + e);
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

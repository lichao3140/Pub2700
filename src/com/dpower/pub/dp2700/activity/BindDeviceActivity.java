package com.dpower.pub.dp2700.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.dpower.cloudintercom.CloudIntercom;
import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.application.App;
import com.dpower.pub.dp2700.dialog.TipsDialog;
import com.dpower.pub.dp2700.dialog.TipsDialog.OnDialogClickListener;
import com.dpower.pub.dp2700.tools.JniBase64Code;
import com.dpower.pub.dp2700.tools.MyToast;
import com.dpower.pub.dp2700.tools.RebootUT;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.util.CommonUT;
import com.dpower.util.ProjectConfigure;
import com.google.zxing.WriterException;

/**
 * ���豸
 */
public class BindDeviceActivity extends BaseFragmentActivity implements
		OnClickListener {

	private static final String CHARSET = "UTF-8";
	private final int CODE_COLOR = Color.parseColor("#000000");
	private ImageView mImageQRCode;
	private TextView mTextQRCode;
	private CloudLoginChangeReceiver mCloudLoginReceiver;
	private SharedPreferences sharedPreferences;
	private Editor editor;
	
	@SuppressLint("HandlerLeak") 
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				String QRString = (String) msg.obj;
				String encode = null;
				try {
					JniBase64Code base = new JniBase64Code();
					byte[] b = base.enBase(QRString.getBytes(CHARSET));
					encode = new String(b, CHARSET);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (QRString == null) {
					QRString = "DISABLED";
				}
				showQRCode(encode);
				saveQRCodeInfo(QRString, encode);
				break;
			case 1:
				String error = (String) msg.obj;
				showQRCode(error);
				MyToast.show(R.string.cloud_status_tip);
			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cloud_intercom);
		mImageQRCode = (ImageView) findViewById(R.id.image_qr_code);
		mTextQRCode = (TextView) findViewById(R.id.tv_qrcode);
		findViewById(R.id.btn_back).setOnClickListener(this);
		findViewById(R.id.btn_login).setOnClickListener(this);
		findViewById(R.id.btn_reboot).setOnClickListener(this);
		if(!readQRCodeInfo("encode").equals("") && readCodeInfo()) {
			showQRCode(readQRCodeInfo("encode"));
		} else {
			DPFunction.getQRString(handler);
		}
		updateLoginStatus();
		IntentFilter filter = new IntentFilter(DPFunction.ACTION_CLOUD_LOGIN_CHANGED);
		mCloudLoginReceiver = new CloudLoginChangeReceiver();
		App.getInstance().getContext().registerReceiver(mCloudLoginReceiver, filter);
	}

	private void updateLoginStatus() {
		String msg;
		String account = DPFunction.getAccount();
		if (account == null) {
			account = "DISABLED";
		}
		if (!ProjectConfigure.isDebug) {
			msg = getString(R.string.account) + account + "\n"
					+ getString(R.string.account_is_online)
					+ (DPFunction.isOnline() ? "YES" : "NO");
		} else {// CloudMessage���ã�MSG��ʾSIP״̬
			msg = getString(R.string.account) + account + "\n" + getString(R.string.cloud_login_status)
					+ (DPFunction.sipIsOnline() ? getString(R.string.cloud_login_success) : getString(R.string.cloud_login_failed));
		}
		mTextQRCode.setText(msg);
	}

	@Override
	protected void onResume() {
		super.onResume();
		findViewById(R.id.root_view).setBackground(
				SPreferences.getInstance().getWallpaper());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mCloudLoginReceiver != null) {
			App.getInstance().getContext()
					.unregisterReceiver(mCloudLoginReceiver);
			mCloudLoginReceiver = null;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;
		case R.id.btn_login:
			CloudIntercom.startLogin();
			MyToast.show(R.string.cloud_login_restar);
			break;
		case R.id.btn_reboot:
			TipsDialog dialog = new TipsDialog(this);
			dialog.setContent(getString(R.string.restar_or_not) + "?");
			dialog.setOnClickListener(new OnDialogClickListener() {
				
				@Override
				public void onClick() {
					RebootUT.rebootSU();
				}
			});
			dialog.show();
			break;
		default:
			break;
		}
	}

	/** ��ʾ��ά�� */
	private void showQRCode(String QRString) {
		try {
			Bitmap bm = CommonUT.createQRCode(QRString, 300, CODE_COLOR);
			mImageQRCode.setImageBitmap(bm);
		} catch (WriterException e) {
			e.printStackTrace();
		}
	}
	
	/** �洢��ά�������Ϣ */
	private void saveQRCodeInfo(String code, String encode) {
		sharedPreferences = getSharedPreferences("QRinfo", Activity.MODE_PRIVATE);
		editor = sharedPreferences.edit();
		editor.putString("code", code);
		editor.putString("encode", encode);
		editor.apply();
	}
	
	/** �жϱ���Ķ�ά����Ϣ�Ƿ�Ҫ����  */
	private boolean readCodeInfo(){
		boolean flag = true;
		if(!readQRCodeInfo("code").equals("")) {
			String newcode = readQRCodeInfo("code").substring(6, 32).replaceAll("_", "");
			String account = CloudIntercom.getAccount();
			if(!newcode.equals(account)){
				flag = false;
			}
		}
		return flag;
	}
	
	/** ��ȡ��ά�������Ϣ 
	 * @return */
	private String readQRCodeInfo(String keystr){
		sharedPreferences = getSharedPreferences("QRinfo", Activity.MODE_PRIVATE);
		String encode = sharedPreferences.getString(keystr, "");
		return encode;
	}

	/** SIP��¼״̬������ʾ */
	private class CloudLoginChangeReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			updateLoginStatus();
		}
	} 

}

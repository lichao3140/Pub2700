package com.dpower.pub.dp2700.activity;

import java.util.ArrayList;
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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.dpower.cloudintercom.CloudIntercom;
import com.dpower.cloudintercom.Constant;
import com.dpower.domain.AddrInfo;
import com.dpower.dpsiplib.service.DPSIPService;
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
import com.dpower.util.DPDBHelper;
import com.dpower.util.ProjectConfigure;
import com.dpower.util.ReceiverAction;
import com.google.zxing.WriterException;

/**
 * 绑定设备
 */
public class BindDeviceActivity extends BaseFragmentActivity implements
		OnClickListener {
	private static final String TAG = "BindDeviceActivity";
	
	private final int CODE_COLOR = Color.parseColor("#000000");
	private ImageView mImageQRCode;
	private TextView mTextQRCode;
	private Button btnRegister;
	private CloudLoginChangeReceiver mCloudLoginReceiver;
	private SharedPreferences sharedPreferences;
	private ArrayList<AddrInfo> mMonitorLists;
	private Editor editor;
	private IntentFilter filterlogin;
	private IntentFilter filtermax;
	private IntentFilter filterexist;
	private int sipcount;
	
	@SuppressLint("HandlerLeak") 
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				String QRString = (String) msg.obj;
				mMonitorLists = DPFunction.getCellSeeList();
				for(int i=0; i< mMonitorLists.size(); i++) {
					QRString = QRString + "_" + mMonitorLists.get(i).getCode();
				}
				String new_QRString = QRString + "_" + mMonitorLists.size();
				Log.i(TAG, "new QRString = " + new_QRString);
				String encode = null;
				try {
					JniBase64Code base = new JniBase64Code();
					byte[] b = base.enBase(new_QRString.getBytes(Constant.CHARSET));
					encode = new String(b, Constant.CHARSET);
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
		sipcount = DPDBHelper.countIndoorSip();
		
		mImageQRCode = (ImageView) findViewById(R.id.image_qr_code);
		mTextQRCode = (TextView) findViewById(R.id.tv_qrcode);
		btnRegister = (Button)findViewById(R.id.btn_register);
		findViewById(R.id.btn_back).setOnClickListener(this);
		findViewById(R.id.btn_login).setOnClickListener(this);
		findViewById(R.id.btn_reboot).setOnClickListener(this);
		btnRegister.setOnClickListener(this);
		if(!DPFunction.isOnline() || (sipcount != 0)) {
			btnRegister.setVisibility(View.GONE);
			btnRegister.setClickable(false);
		} else {
			btnRegister.setClickable(true);
		}
		
		if(!readQRCodeInfo("encode").equals("") && readCodeInfo()) {
			showQRCode(readQRCodeInfo("encode"));
		} else {
			DPFunction.getQRString(handler);
		}
		updateLoginStatus();
		filterlogin = new IntentFilter(DPFunction.ACTION_CLOUD_LOGIN_CHANGED);
		filterexist = new IntentFilter(ReceiverAction.ACTION_ACCOUNT_IS_EXIST);
		filtermax = new IntentFilter(ReceiverAction.ACTION_ACCOUNT_IS_MAX);
		mCloudLoginReceiver = new CloudLoginChangeReceiver();
		App.getInstance().getContext().registerReceiver(mCloudLoginReceiver, filterlogin);
		App.getInstance().getContext().registerReceiver(mCloudLoginReceiver, filterexist);
		App.getInstance().getContext().registerReceiver(mCloudLoginReceiver, filtermax);
		
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
		} else {// CloudMessage不用，MSG显示SIP状态
			msg = getString(R.string.account) + account + "\n" + getString(R.string.cloud_login_status)
					+ (DPFunction.isOnline() ? getString(R.string.cloud_login_success) : getString(R.string.cloud_login_failed));
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
		case R.id.btn_register:			
			if(sipcount >= 1) {
				MyToast.show(R.string.cloud_is_register);
			} else {
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						if(DPSIPService.ping()) {
							CloudIntercom.registerIndoor();
						} else {
							MyToast.show(R.string.cloud_network_faile);
						}
					}
				}).start();
			}
			break;
		case R.id.btn_login:
			if(!DPFunction.isOnline()) {//不在线
				CloudIntercom.startLogin();
				MyToast.show(R.string.cloud_login_restar);
			} else {
				MyToast.show(R.string.cloud_is_logined);
			}
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

	/** 显示二维码 */
	private void showQRCode(String QRString) {
		try {
			Bitmap bm = CommonUT.createQRCode(QRString, 300, CODE_COLOR);
			mImageQRCode.setImageBitmap(bm);
		} catch (WriterException e) {
			e.printStackTrace();
		}
	}
	
	/** 存储二维码加密信息 */
	private void saveQRCodeInfo(String code, String encode) {
		sharedPreferences = getSharedPreferences("QRinfo", Activity.MODE_PRIVATE);
		editor = sharedPreferences.edit();
		editor.putString("code", code);
		editor.putString("encode", encode);
		editor.apply();
	}
	
	/** 判断保存的二维码信息是否要更新  */
	private boolean readCodeInfo(){
		boolean flag = true;
		String savecode = readQRCodeInfo("code");//保存的二维码信息
		String door = "";
		if(!savecode.equals("")) {
			String newcode = savecode.substring(6, 19);//保存的室内机信息
			String doorcode = savecode.substring(31, savecode.length());//保存的门口机信息
			String account = CloudIntercom.getRoomId();
			mMonitorLists = DPFunction.getCellSeeList();
			for(int i=0; i< mMonitorLists.size(); i++) {
				door = door + "_" + mMonitorLists.get(i).getCode();
			}
			
			if(!newcode.equals(account) || !doorcode.equals(door)){
				flag = false;
			}
		}
		return flag;
	}
	
	/** 读取二维码加密信息  */
	private String readQRCodeInfo(String keystr){
		sharedPreferences = getSharedPreferences("RoomCode", Activity.MODE_PRIVATE);
		String encode = sharedPreferences.getString(keystr, "");
		return encode;
	}

	/** SIP登录状态更新显示 */
	private class CloudLoginChangeReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(ReceiverAction.ACTION_ACCOUNT_IS_EXIST)) {
				MyToast.show(R.string.bind_device_is_exist);
			} else if(action.equals(ReceiverAction.ACTION_ACCOUNT_IS_MAX)) {
				MyToast.show(R.string.bind_device_is_max + DPDBHelper.ACCOUNT_MACNUM);
			}
			updateLoginStatus();
		}
	} 

}

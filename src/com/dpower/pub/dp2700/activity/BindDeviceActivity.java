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
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.dpower.cloudintercom.CloudIntercom;
import com.dpower.cloudintercom.Constant;
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
	
	private final int CODE_COLOR_BACK = Color.parseColor("#000000");
	private ImageView mImageQRCode;
	private TextView mTextQRCode;
	private Button btnRegister;
	private CloudLoginChangeReceiver mCloudLoginReceiver;
	private SharedPreferences sharedPreferences;
	private Editor editor;
	private IntentFilter filterlogin;
	private IntentFilter filtermax;
	private IntentFilter filterexist;
	
	@SuppressLint("HandlerLeak") 
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				String QRString = (String) msg.obj;
				Log.i(TAG, "QRString:" + QRString);
				String encode = null;
				try {
					JniBase64Code base = new JniBase64Code();
					byte[] b = base.enBase(QRString.getBytes(Constant.CHARSET));
					encode = new String(b, Constant.CHARSET);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (QRString == null) {
					QRString = "DISABLED";
				}
				Log.i(TAG, "Encode QRString:" + encode);
				showQRCode(encode);
				saveQRCodeInfo(QRString, encode);
				break;
			case 1:
				String error = (String) msg.obj;
				showQRCode(error);
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
		btnRegister = (Button)findViewById(R.id.btn_register);
		findViewById(R.id.btn_back).setOnClickListener(this);
		findViewById(R.id.btn_login).setOnClickListener(this);
		findViewById(R.id.btn_reboot).setOnClickListener(this);
		btnRegister.setOnClickListener(this);
		if(!DPFunction.isOnline() && (DPDBHelper.countIndoorSip() == 0)) {
			btnRegister.setClickable(true);
		} else {
			btnRegister.setVisibility(View.GONE);
			btnRegister.setClickable(false);
		}
		if((readQRCodeInfo("encode").length() > 0) && CodeIsChange()) {
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
			if(DPDBHelper.countIndoorSip() >= 1) {
				MyToast.show(R.string.cloud_is_register);
			} else {
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						if(DPSIPService.ping()) {
							CloudIntercom.registerIndoor();
						} else {
							Log.e(TAG, "NO INTERNET");
							//Looper解决Toast在子线程使用异常
							Looper.prepare();  
							MyToast.show(R.string.cloud_network_faile);
							Looper.loop();
						}
					}
				}).start();
			}
			break;
		case R.id.btn_login:
			if(DPDBHelper.countIndoorSip() == 0 && DPFunction.getAccount().isEmpty()) {
				MyToast.show(R.string.cloud_status_tip);
			} else if(DPDBHelper.countIndoorSip() == 0 && !DPFunction.getAccount().isEmpty()) {
				MyToast.show(R.string.cloud_status_tip_regist);
			} else if(!DPFunction.isOnline()) {//不在线
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
			if(DPDBHelper.countIndoorSip() == 0 && DPFunction.getAccount().isEmpty()) {
				Bitmap bm = CommonUT.createDestroyImage(getString(R.string.cloud_status_tip), 300, 30);
				mImageQRCode.setImageBitmap(bm);
			} else if(!DPFunction.getAccount().equals(DPFunction.getAccount()) || 
					QRString.equals("DISABLED")) {
				Bitmap bm = CommonUT.createDestroyImage(getString(R.string.cloud_status_tip_regist), 300, 30);
				mImageQRCode.setImageBitmap(bm);
			} else {
//				Bitmap qrBitmap  = CommonUT.createQRCode(QRString, 300, CODE_COLOR_BACK);
//				Bitmap logoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
//				Bitmap bitmap = CommonUT.addLogo(qrBitmap, logoBitmap);
//				mImageQRCode.setImageBitmap(qrBitmap);
				
				Bitmap logoBitmap = modifyLogo(
						BitmapFactory.decodeResource(getResources(), R.drawable.qrcode_white_bg), 
						BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
				Bitmap codeBitmap = CommonUT.createCode(QRString, logoBitmap);
				mImageQRCode.setImageBitmap(codeBitmap);
			}
		} catch (WriterException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 存储二维码加密信息
	 * @param code    未加密二维码信息
	 * @param encode  已加密二维码信息
	 */
	private void saveQRCodeInfo(String code, String encode) {
		sharedPreferences = getSharedPreferences("QRinfo", Activity.MODE_PRIVATE);
		editor = sharedPreferences.edit();
		editor.putString("code", code);
		editor.putString("encode", encode);
		editor.commit();
	}
	
	/**
	 * 判断保存的二维码信息是否改变
	 * True    未改变
	 * False   已改变
	 */
	private boolean CodeIsChange(){
		boolean flag = true;
		String savecode = readQRCodeInfo("code");//保存的二维码信息
		Log.i(TAG, "savecode=" + savecode);
		if(!savecode.equals("")) {
			String SP_deviceNo = savecode.substring(6, 22);//保存的室内机deviceNo
			String DB_deviceNo = DPDBHelper.queryFistSip().getDeviceNo().toString();//数据库deviceNo
			
			if(!SP_deviceNo.equals(DB_deviceNo)){
				flag = false;
			}
		}
		return flag;
	}
	
	/**
	 * 读取二维码加密信息
	 * @param keystr
	 * @return
	 */
	private String readQRCodeInfo(String keystr){
		sharedPreferences = getSharedPreferences("QRinfo", Activity.MODE_PRIVATE);
		String readcode = sharedPreferences.getString(keystr, "");
		Log.i(TAG, "readencode=" + readcode);
		return readcode;
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
	
	/**
	 * @return 返回带有白色背景框logo
	 */
	public Bitmap modifyLogo(Bitmap bgBitmap, Bitmap logoBitmap) {
		//读取背景图片，并构建绘图对象
		int bgWidth = bgBitmap.getWidth();
		int bgHeigh = bgBitmap.getHeight();
		//通过ThumbnailUtils压缩原图片，并指定宽高为背景图的3/4
		logoBitmap = ThumbnailUtils.extractThumbnail(logoBitmap, bgWidth*3/4, bgHeigh*3/4, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		Bitmap cvBitmap = Bitmap.createBitmap(bgWidth, bgHeigh, Config.ARGB_8888);
		Canvas canvas = new Canvas(cvBitmap);
		// 开始绘制图片
		canvas.drawBitmap(bgBitmap, 0, 0, null);
		canvas.drawBitmap(logoBitmap,(bgWidth - logoBitmap.getWidth()) /2,(bgHeigh - logoBitmap.getHeight()) / 2, null);
		canvas.save(Canvas.ALL_SAVE_FLAG);// 保存
		canvas.restore();
		if(cvBitmap.isRecycled()){
			cvBitmap.recycle();
		}
		return cvBitmap;
	}
}

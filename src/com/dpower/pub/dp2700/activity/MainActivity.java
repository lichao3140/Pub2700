package com.dpower.pub.dp2700.activity;

import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.adapter.MainPageAdapter;
import com.dpower.pub.dp2700.application.App;
import com.dpower.pub.dp2700.service.PhysicsKeyService;
import com.dpower.pub.dp2700.service.PhysicsKeyService.KeyCallback;
import com.dpower.pub.dp2700.service.ScreenSaverService;
import com.dpower.pub.dp2700.service.ScreenSaverService.ScreenOffCallback;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.util.CommonUT;
import com.dpower.util.ConstConf;
import com.dpower.util.MyLog;
import com.dpower.util.ProjectConfigure;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ethernet.EthernetManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends BaseFragmentActivity 
		implements OnClickListener {
	private static final String TAG = "MainActivity";
	
	public static boolean isScreenOff = false; // 针对屏幕关闭时按键的处理
	public static boolean isStartScreenSaver = true; // 针对日历屏保、图片屏保进行定时退出并关闭屏幕的处理
	public ViewPager mViewPaper;
	private MainPageAdapter mAdapter;
	public Button mHome, mVisualIntercom, mCloudIntercom, mHomeSercurity,
			mIntelligentHome, mSetting, mEntertainment;
	private Button[] mButtonArray;
	private ImageView mNetworkEthernet, mNetworkWireless, mDefence;
	private int mFlag;
	private SafeModeChangeReceiver mSafeModeReceiver;
	private NetworkBroadcastReceiver mNetworkReceiver;
	private Context mContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (ProjectConfigure.project == 2) {
			setContentView(R.layout.activity_main2);
		} else {
			setContentView(R.layout.activity_main);
		}
		init();
	}
	
	private void init() {
		mContext = this;
		mHome = (Button) findViewById(R.id.btn_home);
		mVisualIntercom = (Button) findViewById(R.id.btn_visual_intercom);
		mHomeSercurity = (Button) findViewById(R.id.btn_house_sercurity);
		mCloudIntercom = (Button) findViewById(R.id.btn_cloud_intercom);
		mIntelligentHome = (Button) findViewById(R.id.btn_intelligent_home);
		mSetting = (Button) findViewById(R.id.btn_setting);
		mEntertainment = (Button) findViewById(R.id.btn_entertainment);
		mNetworkEthernet = (ImageView) findViewById(R.id.image_network_ethernet);
		mNetworkWireless = (ImageView) findViewById(R.id.image_network_wireless);
		mDefence = (ImageView) findViewById(R.id.image_defence);

		if (ProjectConfigure.project == 2) {
			mButtonArray = new Button[] {mHome, mHomeSercurity, mVisualIntercom, mSetting};
		} else {
			mButtonArray = new Button[] {mCloudIntercom, mHomeSercurity, mVisualIntercom, 
					mHome, mIntelligentHome, mSetting, mEntertainment};
		}
		for (int i = 0; i < mButtonArray.length; i++) {
			mButtonArray[i].setOnClickListener(this);
		}
		mFlag = MainPageAdapter.getHomePage();
		setButtonBackground();
		mAdapter = new MainPageAdapter(getSupportFragmentManager());
		mViewPaper = (ViewPager) findViewById(R.id.view_pager);
		mViewPaper.setAdapter(mAdapter);
		mViewPaper.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				mFlag = arg0;
				setButtonBackground();
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				
			}
		});
		mViewPaper.setCurrentItem(mFlag);
		startService(new Intent(mContext, ScreenSaverService.class));
		ScreenSaverService.registerScreenOffCallback(mScreenOffCallback);
		if (ProjectConfigure.project == 1 && ProjectConfigure.size == 7) {
			startService(new Intent(mContext, PhysicsKeyService.class));
			PhysicsKeyService.registerKeyCallback(mKeyCallback);
		}
		DPFunction.setDoorCallInActivity(CallInFromDoorActivity.class);
		DPFunction.setRoomCallInActivity(CallInActivity.class);
		DPFunction.setAlarmActivity(AlarmActivity.class);
		DPFunction.loadAlarmFile();
		mSafeModeReceiver = new SafeModeChangeReceiver();
		registerReceiver(mSafeModeReceiver, new IntentFilter(
				DPFunction.ACTION_SAFE_MODE));
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		filter.addAction(EthernetManager.NETWORK_STATE_CHANGED_ACTION);
		mNetworkReceiver = new NetworkBroadcastReceiver();
		registerReceiver(mNetworkReceiver, filter);
		registerReceiver(mHomeKeyReceiver, 
				new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		MyLog.print(TAG, "onCreate");
	}
	
	private void setButtonBackground() {
		if (mFlag < 0) {
			return;
		}
		for (int i = 0; i < mButtonArray.length; i++) {
			if (i == mFlag) {
				if (ProjectConfigure.project == 1) {
					mButtonArray[i].setBackgroundColor(getResources()
							.getColor(R.color.DarkGreen));
				} else {
					mButtonArray[i].setBackgroundColor(getResources()
							.getColor(R.color.TransparencyBlue));
				}
			} else {
				mButtonArray[i].setBackgroundColor(getResources()
						.getColor(R.color.Transparency));
			}
		}
	}

	private class SafeModeChangeReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			updateDefenceView();
		}
	}
	
	private void updateDefenceView() {
		switch (DPFunction.getSafeMode()) {
			case ConstConf.HOME_MODE:
				mDefence.setImageResource(
						R.drawable.ic_in_home_mode_status_bar);
				break;
			case ConstConf.LEAVE_HOME_MODE:
				mDefence.setImageResource(
						R.drawable.ic_leave_home_mode_status_bar);
				break;
			case ConstConf.NIGHT_MODE:
				mDefence.setImageResource(
						R.drawable.ic_in_night_mode_status_bar);
				break;
			case ConstConf.UNSAFE_MODE:
				mDefence.setImageResource(
						R.drawable.ic_cancel_defence_status_bar);
				break;
			default:
				break;
		}
	}

	private class NetworkBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					EthernetManager.NETWORK_STATE_CHANGED_ACTION)) {
				if (CommonUT.getLanConnectState(ConstConf.LAN_NETWORK_CARD)) {
					mNetworkEthernet.setVisibility(View.VISIBLE);
				} else {
					mNetworkEthernet.setVisibility(View.GONE);
				}
				return;
			}
			if (CommonUT.getLanConnectState(ConstConf.WAN_NETWORK_CARD)) {
				mNetworkWireless.setVisibility(View.VISIBLE);
			} else {
				mNetworkWireless.setVisibility(View.GONE);
			}
		}
	}
	
	private BroadcastReceiver mHomeKeyReceiver = new BroadcastReceiver() {  
		
        String SYSTEM_REASON = "reason";
        String SYSTEM_HOME_KEY = "homekey";
  
        @Override  
        public void onReceive(Context context, Intent intent) {  
            String action = intent.getAction();  
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_REASON);  
                if (reason.equals(SYSTEM_HOME_KEY)) {
					App.getInstance().onHomeKey();
				}
            }   
        }  
    };

	private ScreenOffCallback mScreenOffCallback = new ScreenOffCallback() {
		
		@Override
		public void onScreenOff(int flag) {
			MyLog.print(TAG, "isStartScreenSaver = " + isStartScreenSaver);
			if (!isStartScreenSaver) {
				isStartScreenSaver = true;
				return;
			}
			switch (flag) {
				case ScreenSaverService.BLACK:
					isScreenOff = true;
					MyLog.print(TAG, "ScreenSaverBlackActivity");
					startActivity(new Intent(mContext, ScreenSaverBlackActivity.class));
					break;
				case ScreenSaverService.CALENDAR:
					isScreenOff = true;
					MyLog.print(TAG, "ScreenSaverCalendarActivity");
					startActivity(new Intent(mContext, ScreenSaverCalendarActivity.class));
					break;
				case ScreenSaverService.PICTURE:
					isScreenOff = true;
					MyLog.print(TAG, "ScreenSaverPictureActivity");
					startActivity(new Intent(mContext, ScreenSaverPictureActivity.class));
					break;
				default:
					break;
			}
		}
	};
	
	private KeyCallback mKeyCallback = new KeyCallback() {

		@Override
		public void onKey(int keyIO) {
			if (isScreenOff) {
				isScreenOff = false;
				return;
			}
			switch (keyIO) {
				case PhysicsKeyService.MESSAGE:
					startActivity(new Intent(mContext, SMSActivity.class));
					break;
				case PhysicsKeyService.VOLUME:
					if (ProjectConfigure.project == 1) {
						startActivity(new Intent(mContext, CallGuardActivity.class));
					} else {
						startActivity(new Intent(mContext, CallManagementActivity.class));
					}
					break;
				case PhysicsKeyService.MONITOR:
					startActivity(new Intent(mContext, MonitorActivity.class));
					break;
				case PhysicsKeyService.UNLOCK:
					break;
				case PhysicsKeyService.HANGUP:
					break;
				default:
					break;
			}
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		findViewById(R.id.root_view).setBackground(
				SPreferences.getInstance().getWallpaper());
		updateDefenceView();
		PhysicsKeyService.setKeyListener(true);
		PhysicsKeyService.setKeySwitch(new boolean[] {
				true, true, true, true, true });
		updateNetworkView();
	}
	
	private void updateNetworkView() {
		if (CommonUT.getLanConnectState(ConstConf.LAN_NETWORK_CARD)) {
			mNetworkEthernet.setVisibility(View.VISIBLE);
		} else {
			mNetworkEthernet.setVisibility(View.GONE);
		}
		if (CommonUT.getLanConnectState(ConstConf.WAN_NETWORK_CARD)) {
			mNetworkWireless.setVisibility(View.VISIBLE);
		} else {
			mNetworkWireless.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopService(new Intent(mContext, ScreenSaverService.class));
		ScreenSaverService.unRegisterScreenOffCallback(mScreenOffCallback);
		if (ProjectConfigure.project == 1 && ProjectConfigure.size == 7) {
			stopService(new Intent(mContext, PhysicsKeyService.class));
			PhysicsKeyService.unRegisterKeyCallback(mKeyCallback);
			PhysicsKeyService.setKeyListener(false);
		}
		if (mNetworkReceiver != null) {
			unregisterReceiver(mNetworkReceiver);
			mNetworkReceiver = null;
		}
		if (mSafeModeReceiver != null) {
			unregisterReceiver(mSafeModeReceiver);
			mSafeModeReceiver = null;
		}
		if (mHomeKeyReceiver != null) {
			unregisterReceiver(mHomeKeyReceiver);
			mHomeKeyReceiver = null;
		}
		MyLog.print(TAG, "onDestroy");
		mButtonArray = null;
	}

	@Override
	public void onClick(View v) {
		if (ProjectConfigure.project == 2) {
			switch (v.getId()) {
				case R.id.btn_home:
					mFlag = MainPageAdapter.getHomePage();
					mViewPaper.setCurrentItem(mFlag);
					break;
				case R.id.btn_house_sercurity:
					mFlag = MainPageAdapter.getHomePage() + 1;
					mViewPaper.setCurrentItem(mFlag);
					break;
				case R.id.btn_visual_intercom:
					mFlag = MainPageAdapter.getHomePage() + 2;
					mViewPaper.setCurrentItem(mFlag);
					break;
				case R.id.btn_setting:
					mFlag = MainPageAdapter.getHomePage() + 3;
					mViewPaper.setCurrentItem(mFlag);
					break;
				default:
					break;
			}
		} else {
			switch (v.getId()) {
				case R.id.btn_cloud_intercom:
					mFlag = MainPageAdapter.getHomePage() -3;
					mViewPaper.setCurrentItem(mFlag);
					break;
				case R.id.btn_house_sercurity:
					mFlag = MainPageAdapter.getHomePage() - 2;
					mViewPaper.setCurrentItem(mFlag);
					break;
				case R.id.btn_visual_intercom:
					mFlag = MainPageAdapter.getHomePage() - 1;
					mViewPaper.setCurrentItem(mFlag);
					break;
				case R.id.btn_home:
					mFlag = MainPageAdapter.getHomePage();
					mViewPaper.setCurrentItem(mFlag);
					break;
				case R.id.btn_intelligent_home:
					mFlag = MainPageAdapter.getHomePage() + 1;
					mViewPaper.setCurrentItem(mFlag);
					break;
				case R.id.btn_setting:
					mFlag = MainPageAdapter.getHomePage() + 2;
					mViewPaper.setCurrentItem(mFlag);
					break;
				case R.id.btn_entertainment:
					mFlag = MainPageAdapter.getHomePage() + 3;
					mViewPaper.setCurrentItem(mFlag);
					break;
				default:
					break;
			}
		}
		setButtonBackground();
	}
}

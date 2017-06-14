package com.dpower.pub.dp2700.activity;

import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.adapter.WifiAdapter;
import com.dpower.pub.dp2700.dialog.WifiConnectDialog;
import com.dpower.pub.dp2700.dialog.WifiConnectDialog.OnConnectDialogClickListener;
import com.dpower.pub.dp2700.dialog.WifiControlDialog;
import com.dpower.pub.dp2700.dialog.WifiControlDialog.OnControlDialogClickListener;
import com.dpower.pub.dp2700.dialog.WifiInfoDialog;
import com.dpower.pub.dp2700.dialog.WifiInfoDialog.OnInfoDialogClickListener;
import com.dpower.pub.dp2700.dialog.WifiModifyDialog;
import com.dpower.pub.dp2700.dialog.WifiModifyDialog.OnModifyDialogClickListener;
import com.dpower.pub.dp2700.tools.MyToast;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.util.CommonUT;
import com.dpower.util.MyLog;
import com.dpower.wifi.WifiCallback;
import com.dpower.wifi.WifiScanResult;
import com.dpower.wifi.WifiSettings;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

/**
 * WiFi设置
 */
public class WifiActivity extends BaseFragmentActivity 
		implements View.OnClickListener {
	private static final String TAG = "WifiActivity";
	
	private final int SET_STATIC_IP_FAILED = 101;
	private final int SET_DHCP_FAILED = 102;
	private final int WIFI_ENABLING = 103;
	private final int WIFI_ENABLED = 104;
	private final int WIFI_DISABLING = 105;
	private final int WIFI_DISABLED = 106;
	private final int WIFI_ERROR = 107;
	private final int WIFI_AUTHENTICATING = 108;
	private final int WIFI_OBTAININGIPADDR = 109;
	private final int WIFI_CONNECTED = 110;
	private final int WIFI_CONNECTFAILED = 111;
	private final int WIFI_DISCONNECTED = 112;
	private ListView mWifiList;
	private WifiSettings mWifiSettings;
	private WifiAdapter mAdapter;
	private WifiInfo mWifiInfo;
	private Button mSwitch;
	private TextView mEmptyView;
	private WifiConnectDialog mConnectDialog;
	private WifiControlDialog mControlDialog;
	private WifiInfoDialog mInfoDialog;
	private WifiModifyDialog mModifyDialog;
	private boolean mIsWifiError;
	private Context mContext;
	
	private WifiCallback mCallback = new WifiCallback() {
		
		@Override
		public void updateWifiList() {
			mAdapter.wifiList = mWifiSettings.getWifiList();
	    	mAdapter.notifyDataSetChanged();
		}

		@Override
		public void wifiScanFail() {
			mEmptyView.setText(R.string.wifi_fail_to_scan);
		}

		@Override
		public void wifiEnabling() {
			mHandler.sendEmptyMessage(WIFI_ENABLING);
		}

		@Override
		public void wifiEnabled() {
			mHandler.sendEmptyMessage(WIFI_ENABLED);
		}

		@Override
		public void wifiDisabling() {
			mHandler.sendEmptyMessage(WIFI_DISABLING);
		}

		@Override
		public void wifiDisabled() {
			mHandler.sendEmptyMessage(WIFI_DISABLED);
		}
		
		@Override
		public void wifiError() {
			mHandler.sendEmptyMessage(WIFI_ERROR);
		}

		@Override
		public void wifiAuthenticating() {
			mHandler.sendEmptyMessage(WIFI_AUTHENTICATING);
		}

		@Override
		public void wifiObtainingIpaddr() {
			mHandler.sendEmptyMessage(WIFI_OBTAININGIPADDR);
		}

		@Override
		public void wifiConnected() {
			mHandler.sendEmptyMessage(WIFI_CONNECTED);
		}

		@Override
		public void wifiConnectFailed() {
			mHandler.sendEmptyMessage(WIFI_CONNECTFAILED);
		}

		@Override
		public void wifiDisconnected() {
			mHandler.sendEmptyMessage(WIFI_DISCONNECTED);
		}
	};
	
	private Handler mHandler = new Handler() {
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case SET_STATIC_IP_FAILED:
					MyToast.show(R.string.wifi_static_ip_set_failure);
					break;
				case SET_DHCP_FAILED:
					MyToast.show(R.string.wifi_dynamic_ip_set_failure);
					break;
				case WIFI_ENABLING:
					mEmptyView.setText(R.string.wifi_starting);
					mSwitch.setEnabled(false);
					if (mIsWifiError) {
						mEmptyView.setText(R.string.wifi_error);
					}
					break;
				case WIFI_ENABLED:
					mSwitch.setEnabled(true);
					mSwitch.setText(R.string.wifi_enabled);
					if (mAdapter.wifiList.size() == 0) {
						mEmptyView.setText(R.string.wifi_empty_list_wifi_on);
					}
					break;
				case WIFI_DISABLING:
					mSwitch.setEnabled(false);
					mAdapter.wifiList.clear();
					mAdapter.notifyDataSetChanged();
					mEmptyView.setText(R.string.wifi_stopping);
					if (mIsWifiError) {
						mEmptyView.setText(R.string.wifi_error);
					}
					break;
				case WIFI_DISABLED:
					mSwitch.setEnabled(true);
					mSwitch.setText(R.string.wifi_disabled);
					mEmptyView.setText(R.string.wifi_empty_list_wifi_off);
					break;
				case WIFI_ERROR:
					MyToast.show(R.string.wifi_error);
					mSwitch.setEnabled(true);
					mSwitch.setText(R.string.wifi_disabled);
					mEmptyView.setText(R.string.wifi_error);
					break;
				case WIFI_AUTHENTICATING:
					updateWifiListShow(WifiScanResult.AUTHENTICATING);
					break;
				case WIFI_OBTAININGIPADDR:
					updateWifiListShow(WifiScanResult.OBTAINING_IPADDR);
					break;
				case WIFI_CONNECTED:
					updateWifiListShow(WifiScanResult.CURRENT);
					break;
				case WIFI_CONNECTFAILED:
					MyToast.show(R.string.wifi_failed_connect);
					updateWifiListShow(WifiScanResult.CONNECT_FAILURE);
					break;
				case WIFI_DISCONNECTED:
					updateWifiListShow(WifiScanResult.DISCONNECTED);
					break;
				default:
					break;
			}
		};
	};
	
	private void updateWifiListShow(int state) {
		if (mAdapter.wifiList.size() > 0) {
			mAdapter.wifiList.get(0).status = state;
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wifi);
		mContext = this;
		mWifiSettings = new WifiSettings(mContext, mCallback);
		mConnectDialog = new WifiConnectDialog();
		mControlDialog = new WifiControlDialog();
		mInfoDialog = new WifiInfoDialog();
		mModifyDialog = new WifiModifyDialog(mWifiSettings);
        initView();
        initListener();
        initData();
	    
	}

	private void initView() {
		mSwitch = (Button) findViewById(R.id.btn_switch);
		mEmptyView = (TextView) findViewById(R.id.empty);
		mWifiList = (ListView) findViewById(R.id.list_wifi);
	}

	private void initListener() {
		findViewById(R.id.btn_back).setOnClickListener(this);
		findViewById(R.id.btn_refresh).setOnClickListener(this);
		mSwitch.setOnClickListener(this);
		setListViewListener();
		setConnectDialogListener();
		setControlDialogListener();
		setInfoDialogListener();
		setModifyDialogListener();
	}

	private void setListViewListener() {
		mWifiList.setOnItemClickListener(new ListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (mAdapter.wifiList.get(position).status 
						== WifiScanResult.ENABLED || 
            			mAdapter.wifiList.get(position).status 
            			== WifiScanResult.DISABLED || 
            			mAdapter.wifiList.get(position).status 
            			== WifiScanResult.CONNECT_FAILURE) {
            		String ssid = mAdapter.wifiList.get(position).SSID;
    				int security = mAdapter.wifiList.get(position).security;
    				int netId = mAdapter.wifiList.get(position).networkId;
    				int ipType = mAdapter.wifiList.get(position).ipType;
    				wifiControl(ssid, security, netId, ipType);
        		} else if (mAdapter.wifiList.get(position).status 
        				== WifiScanResult.CURRENT) {
        			int security = mAdapter.wifiList.get(position).security;
        			int ipType = mAdapter.wifiList.get(position).ipType;
        			showWifiInfo(security, ipType);
        		} else if (mAdapter.wifiList.get(position).status 
        				== WifiScanResult.AUTHENTICATING || 
        				mAdapter.wifiList.get(position).status 
        				== WifiScanResult.OBTAINING_IPADDR) {
        			MyLog.print(TAG, "正在连接WiFi");
        		} else {
        			String ssid = mAdapter.wifiList.get(position).SSID;
    				int security = mAdapter.wifiList.get(position).security;
    				connectWifi(ssid, security);
				}
			}
		});
	}
	
	private void wifiControl(String ssid, int security, int netId, int ipType) {
		mControlDialog = new WifiControlDialog();
		setControlDialogListener();
		mControlDialog.ssid = ssid;
		mControlDialog.security = security;
		mControlDialog.netId = netId;
		mControlDialog.ipType = ipType;
		mControlDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.style_dialog);
		if (!mControlDialog.isAdded()) {
			mControlDialog.show(getSupportFragmentManager(), "wifi control");
			MyLog.print(TAG, "ControlDialog show");
		}
	}
	
	private void showWifiInfo(int security, int ipType) {
		mInfoDialog = new WifiInfoDialog();
		setInfoDialogListener();
		mWifiInfo = mWifiSettings.getConnectionInfo();
		mInfoDialog.ssid = mWifiInfo.getSSID().replace("\"", "");
		mInfoDialog.status = getString(R.string.wifi_connected);
		mInfoDialog.speed = mWifiInfo.getLinkSpeed() + "Mbps";
		mInfoDialog.ip = CommonUT.intToIp(mWifiInfo.getIpAddress());
		mInfoDialog.security = security;
		mInfoDialog.ipType = ipType;
		DhcpInfo mDhcpInfo = mWifiSettings.getDhcpInfo();
		if (mDhcpInfo != null) {
			String subnetMask = CommonUT.intToIp(mDhcpInfo.netmask);
			if (subnetMask.equals(getString(R.string.wifi_ip_zero))) {
                subnetMask = getString(R.string.wifi_subnet_mask_hint);
            }
			mInfoDialog.subnetMask = subnetMask;
			mInfoDialog.gateway = CommonUT.intToIp(mDhcpInfo.gateway);
			mInfoDialog.dns = CommonUT.intToIp(mDhcpInfo.dns1);
		} else {
			mInfoDialog.subnetMask = getString(R.string.wifi_ip_zero);
			mInfoDialog.gateway = getString(R.string.wifi_ip_zero);
			mInfoDialog.dns = getString(R.string.wifi_ip_zero);
		}
		mInfoDialog.netId = mWifiInfo.getNetworkId();
		mInfoDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.style_dialog);
		if (!mInfoDialog.isAdded()) {
			mInfoDialog.show(getSupportFragmentManager(), "wifi info");
			MyLog.print(TAG, "InfoDialog show");
		}
	}
	
	private void connectWifi(String ssid, int security) {
		if (security == WifiScanResult.SECURITY_NONE) {
			mWifiSettings.connectWifi(ssid, "", security);
		} else {
			mConnectDialog = new WifiConnectDialog();
			setConnectDialogListener();
			mConnectDialog.ssid = ssid;
			mConnectDialog.security = security;
			mConnectDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.style_dialog);
			if (!mConnectDialog.isAdded()) {
				mConnectDialog.show(getSupportFragmentManager(), "wifi connect");
				MyLog.print(TAG, "ConnectDialog show");
			}
		}
	}
	
	private void setControlDialogListener() {
		mControlDialog.setControlDialogClickListener(
				new OnControlDialogClickListener() {
			
			@Override
			public void onRemoveNetwork(int netId) {
				mWifiSettings.removeNetwork(netId);
			}
			
			@Override
			public void onModifyNetwork(
					String ssid, int security, int netId, int ipType) {
				modifyNetwork(ssid, security, netId, ipType);
			}
			
			@Override
			public void onConnectNetwork(String ssid) {
				mWifiSettings.connectWifi(ssid, null, 0);
			}
		});
	}
	
	private void setInfoDialogListener() {
		mInfoDialog.setInfoDialogClickListener(
				new OnInfoDialogClickListener() {
			
			@Override
			public void onRemoveNetwork(int netId) {
				mWifiSettings.removeNetwork(netId);
			}
			
			@Override
			public void onModifyNetwork(
					String ssid, int security, int netId, int ipType) {
				modifyNetwork(ssid, security, netId, ipType);
			}
			
			@Override
			public void onDisconnectNetwork() {
				mWifiSettings.disconnectWifi();
			}
		});
	}
	
	private void modifyNetwork(String ssid, int security, int netId, int ipType) {
		mModifyDialog = new WifiModifyDialog(mWifiSettings);
		setModifyDialogListener();
		mModifyDialog.ssid = ssid;
		mModifyDialog.security = security;
		mModifyDialog.netId = netId;
		mModifyDialog.ipType = ipType;
		mModifyDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.style_dialog);
		if (!mModifyDialog.isAdded()) {
			mModifyDialog.show(getSupportFragmentManager(), "wifi modify");
			MyLog.print(TAG, "ModifyDialog show");
		}
	}

	private void setConnectDialogListener() {
		mConnectDialog.setOnConnectDialogClickListener(new OnConnectDialogClickListener() {
			
			@Override
			public void onStaticIpConnect(final String ssid, final String password, 
					final int security, final String ipAddress, final String dnsAddress,
					final String gateway) {
				Thread thread = new Thread() {
					
					@Override
					public void run() {
						int result = mWifiSettings.staticIpConnect(ssid, password, 
								security, ipAddress, dnsAddress, gateway);
						if (result == WifiSettings.FAILURE) {
							mHandler.sendEmptyMessage(SET_STATIC_IP_FAILED);
						}
					}
				};
				thread.start();
			}
			
			@Override
			public void onConnectWifi(String ssid, String password, int security) {
				mWifiSettings.connectWifi(ssid, password, security);
			}
		});
	}
	
	private void setModifyDialogListener() {
		mModifyDialog.setOnModifyDialogClickListener(new OnModifyDialogClickListener() {
			
			@Override
			public void onSetStaticIp(final int netId, final String ipAddress, 
					final String dnsAddress, final String gateway) {
				Thread thread = new Thread() {
					
					@Override
					public void run() {
						int result = mWifiSettings.setStaticIp(netId, 
								ipAddress, dnsAddress, gateway);
						if (result == WifiSettings.FAILURE) {
							mHandler.sendEmptyMessage(SET_STATIC_IP_FAILED);
						}
					}
				};
				thread.start();
			}
			
			@Override
			public void onSetDHCP(final int netId) {
				Thread thread = new Thread() {
					
					@Override
					public void run() {
						int result = mWifiSettings.setDHCP(netId);
						if (result == WifiSettings.FAILURE) {
							mHandler.sendEmptyMessage(SET_STATIC_IP_FAILED);
						}
					}
				};
				thread.start();
			}
			
			@Override
			public void onConnectWifi(String ssid, String password, int security) {
				mWifiSettings.connectWifi(ssid, password, security);
			}
		});
	}

	private void initData() {
		mAdapter = new WifiAdapter(this);
		if (mWifiSettings.isWifiEnabled()) {
			mSwitch.setText(R.string.wifi_enabled);
			mAdapter.wifiList = mWifiSettings.getWifiList();
			mAdapter.notifyDataSetChanged();
			if (mAdapter.wifiList.size() == 0) {
				mEmptyView.setText(R.string.wifi_empty_list_wifi_on);
			}
		} else {
			mSwitch.setText(R.string.wifi_disabled);
			mEmptyView.setText(R.string.wifi_empty_list_wifi_off);
		}
		mWifiList.setAdapter(mAdapter);
		mWifiList.setEmptyView(mEmptyView);
		findViewById(R.id.root_view).setBackground(SPreferences
				.getInstance().getWallpaper());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_back:
				finish();
				break;
			case R.id.btn_switch:
				boolean isChecked;
				if (!mWifiSettings.isWifiEnabled()) {
					isChecked = true;
				} else {
					isChecked = false;
				}
				mSwitch.setEnabled(false);
	            if (!mWifiSettings.setWifiEnabled(isChecked)) {
	                // Error
	            	mSwitch.setEnabled(true);
	            	mIsWifiError = true;
	            	MyToast.show(R.string.wifi_error);
	            } else {
	            	mIsWifiError = false;
				}
				break;
			case R.id.btn_refresh:
				if (mWifiSettings.isWifiEnabled()) {
					mWifiSettings.resumeWifiScan();
				}
				break;
			default:
				break;
		}
	}
	
	@Override
	protected void onDestroy() {
		if (mWifiSettings != null) {
			mWifiSettings.unInit();
			mWifiSettings = null;
		}
		super.onDestroy();
	}
}

package com.dpower.pub.dp2700.adapter;

import java.util.ArrayList;

import com.dpower.pub.dp2700.R;
import com.dpower.wifi.WifiScanResult;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author misakayaho
 */
public class WifiAdapter extends BaseAdapter {

	public ArrayList<WifiScanResult> wifiList;
	private Context mContext;

	public WifiAdapter(Context context) {
		super();
		mContext = context;
        wifiList = new ArrayList<WifiScanResult>();
	}

	@Override
	public int getCount() {
		return wifiList == null ? 0 : wifiList.size();
	}

	@Override
	public Object getItem(int position) {
		return wifiList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.adapter_wifi_list, parent, false);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		WifiScanResult wifiScanResult = wifiList.get(position);
		holder.mTextSSID.setText(wifiScanResult.SSID);
		
		if (wifiScanResult.security == WifiScanResult.SECURITY_NONE) {
        	if (wifiScanResult.level >= -69) {
        		holder.mImageLevel.setImageResource(
        				R.drawable.ic_wifi_signal_4_light);
			} else if (wifiScanResult.level >= -80) {
				holder.mImageLevel.setImageResource(
						R.drawable.ic_wifi_signal_3_light);
			} else if (wifiScanResult.level >= -88) {
				holder.mImageLevel.setImageResource(
						R.drawable.ic_wifi_signal_2_light);
			} else if (wifiScanResult.level >= -92) {
				holder.mImageLevel.setImageResource(
						R.drawable.ic_wifi_signal_1_light);
			} else {
				holder.mImageLevel.setImageResource(
						R.drawable.ic_wifi_signal_0_light);
			}
			
		} else {
			if (wifiScanResult.level >= -69) {
        		holder.mImageLevel.setImageResource(
        				R.drawable.ic_wifi_lock_signal_4_light);
			} else if (wifiScanResult.level >= -80) {
				holder.mImageLevel.setImageResource(
						R.drawable.ic_wifi_lock_signal_3_light);
			} else if (wifiScanResult.level >= -88) {
				holder.mImageLevel.setImageResource(
						R.drawable.ic_wifi_lock_signal_2_light);
			} else if (wifiScanResult.level >= -92) {
				holder.mImageLevel.setImageResource(
						R.drawable.ic_wifi_lock_signal_1_light);
			} else {
				holder.mImageLevel.setImageResource(
						R.drawable.ic_wifi_lock_signal_0_light);
			}
		}
		
		if (wifiScanResult.status == WifiScanResult.ENABLED) {
        	holder.mTextHint.setText(R.string.wifi_remembered);
		} else if (wifiScanResult.status == WifiScanResult.DISABLED) {
			switch (wifiScanResult.disableReason) {
	            case WifiScanResult.DISABLED_AUTH_FAILURE:
	                holder.mTextHint.setText(
	                		R.string.wifi_disabled_password_failure);
	                break;
	            case WifiScanResult.DISABLED_DHCP_FAILURE:
	            case WifiScanResult.DISABLED_DNS_FAILURE:
	                holder.mTextHint.setText(
	                		R.string.wifi_disabled_network_failure);
	                break;
	            case WifiScanResult.DISABLED_UNKNOWN_REASON:
	                holder.mTextHint.setText(R.string.wifi_disabled_generic);
			}
		} else if (wifiScanResult.status == WifiScanResult.CURRENT) {
        	holder.mTextHint.setText(R.string.wifi_connected);
		} else if (wifiScanResult.status == WifiScanResult.AUTHENTICATING) {
        	holder.mTextHint.setText(R.string.wifi_password_authenticating);
		} else if (wifiScanResult.status == WifiScanResult.OBTAINING_IPADDR) {
        	holder.mTextHint.setText(R.string.wifi_obtaining_ipaddr);
		} else if (wifiScanResult.status == WifiScanResult.CONNECT_FAILURE) {
        	holder.mTextHint.setText(R.string.wifi_failed_connect);
		} else {
			holder.mTextHint.setText(getEncryptionName(wifiScanResult.capabilities));
		}
		return convertView;
	}

	private class ViewHolder {
		public TextView mTextSSID;
		public TextView mTextHint;
		public ImageView mImageLevel;
		
		public ViewHolder(View view) {
			super();
			mTextSSID = (TextView) view.findViewById(R.id.tv_ssid);
			mTextHint = (TextView) view.findViewById(R.id.tv_hint);
			mImageLevel = (ImageView) view.findViewById(R.id.image_level);
		}
	}
	
	private String getEncryptionName(String capabilities) {
		StringBuffer result = new StringBuffer();
		String encryptionName;
		if (capabilities.contains("wep") || capabilities.contains("WEP")) {
			result.append("WEP");
        }
		if (capabilities.contains("wpa") || capabilities.contains("WPA")) {
			if (!result.toString().equals("")) {
				result.append("/");
			}
			result.append("WPA");
		}
		if (capabilities.contains("wpa2") || capabilities.contains("WPA2")) {
			if (!result.toString().equals("")) {
				result.append("/");
			}
			result.append("WPA2");
		}
		if (capabilities.contains("wps") || capabilities.contains("WPS")) {
			if (!result.toString().equals("")) {
				result.append("/");
			}
			result.append("WPS");
		}
		encryptionName = result.toString();
		if (encryptionName.contains("WEP") && encryptionName.contains("WPS")) {
			encryptionName = mContext.getString(R.string.wifi_security_wep) 
					+ mContext.getString(R.string.wifi_could_use_wps);
        } else if (encryptionName.contains("WEP")) {
        	encryptionName = mContext.getString(R.string.wifi_security_wep);
		} else if (encryptionName.contains("WPA") && encryptionName.contains("WPA2") 
				&& encryptionName.contains("WPS")) {
			encryptionName = mContext.getString(R.string.wifi_security_wpa_or_wpa2) 
					+ mContext.getString(R.string.wifi_could_use_wps);
		} else if (encryptionName.contains("WPA") && encryptionName.contains("WPA2")) {
			encryptionName = mContext.getString(R.string.wifi_security_wpa_or_wpa2);
		} else if (encryptionName.contains("WPA")) {
			encryptionName = mContext.getString(R.string.wifi_security_wpa);
		} else if (encryptionName.contains("WPA2")) {
			encryptionName = mContext.getString(R.string.wifi_security_wpa2);
		} else {
			encryptionName = mContext.getString(R.string.wifi_no_security);
		} 
		return encryptionName;
	}
}

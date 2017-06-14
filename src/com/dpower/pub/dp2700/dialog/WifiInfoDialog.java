package com.dpower.pub.dp2700.dialog;

import com.dpower.pub.dp2700.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 已连接的热点的信息
 * @author misakayaho
 */
public class WifiInfoDialog extends BaseDialogFragment 
		implements View.OnClickListener {
	
	public String ssid;
	public String status;
	public String speed;
	public String ip;
	public String subnetMask;
	public String gateway;
	public String dns;
	public int netId;
	public int security;
	public int ipType;
	private View mView;
	private TextView mTextSSID;
	private TextView mTextStatus;
	private TextView mTextSpeed;
	private TextView mTextIp;
	private TextView mTextSubnetMask;
	private TextView mTextGateway;
	private TextView mTextDns;
	private Button mButtonForget;
	private Button mButtonModify;
	private Button mButtonDisconnect;
	private OnInfoDialogClickListener mListener;
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.dialog_wifi, container, false);
		removeTitleBar();
		setLayout(0.7f, 0.0f);
		mTextSSID = (TextView) mView.findViewById(R.id.tv_ssid);
		mTextStatus = (TextView) mView.findViewById(R.id.tv_status);
		mTextSpeed = (TextView) mView.findViewById(R.id.tv_speed);
		mTextIp = (TextView) mView.findViewById(R.id.tv_ip);
		mTextSubnetMask = (TextView) mView.findViewById(R.id.tv_subnet_mask);
		mTextGateway = (TextView) mView.findViewById(R.id.tv_gateway);
		mTextDns = (TextView) mView.findViewById(R.id.tv_dns);
		mButtonForget = (Button) mView.findViewById(R.id.btn_forget);
		mButtonModify = (Button) mView.findViewById(R.id.btn_modify);
		mButtonModify.setVisibility(View.VISIBLE);
		mButtonDisconnect = (Button) mView.findViewById(R.id.btn_disconnect);
		LinearLayout showInfo =(LinearLayout) mView.findViewById(R.id.show_info);
		showInfo.setVisibility(View.VISIBLE);
		
		mButtonForget.setOnClickListener(this);
		mButtonModify.setOnClickListener(this);
		mButtonDisconnect.setOnClickListener(this);
		return mView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (ssid != null) {
			mTextSSID.setText(ssid);
			mTextStatus.setText(status);
			mTextSpeed.setText(speed);
			mTextIp.setText(ip);
			mTextSubnetMask.setText(subnetMask);
			mTextGateway.setText(gateway);
			mTextDns.setText(dns);
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_forget:
				if (mListener != null) {
					mListener.onRemoveNetwork(netId);
					dismiss();
				}
				break;
			case R.id.btn_modify:
				if (mListener != null) {
					mListener.onModifyNetwork(ssid, security, netId, ipType);
					dismiss();
				}		
				break;
			case R.id.btn_disconnect:
				if (mListener != null) {
					mListener.onDisconnectNetwork();
					dismiss();
				}
				break;
			default:
				break;
		}
	}
	
	public interface OnInfoDialogClickListener {
		
		public void onRemoveNetwork(int netId);
		
		public void onModifyNetwork(String ssid, int security, int netId, int ipType);

		public void onDisconnectNetwork();
	}
	
	public void setInfoDialogClickListener(
			OnInfoDialogClickListener listener) {
		mListener = listener;
	}
}

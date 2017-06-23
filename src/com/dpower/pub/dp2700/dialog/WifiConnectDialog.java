package com.dpower.pub.dp2700.dialog;

import java.util.ArrayList;
import java.util.List;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.fragment.KeyboardFragment;
import com.dpower.pub.dp2700.fragment.KeyboardFragment.OnKeyboardListener;
import com.dpower.pub.dp2700.tools.EditTextTool;
import com.dpower.pub.dp2700.tools.MyToast;
import com.dpower.wifi.WifiScanResult;
import com.dpower.wifi.WifiSettings;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * 连接热点
 * @author misakayaho
 */
public class WifiConnectDialog extends 
		BaseDialogFragment implements View.OnClickListener {
	
	public String ssid;
	public int security;
	private View mView;
	private KeyboardFragment mKeyboard;
	private EditTextTool mEditTool;
	private List<String> mIpSetList;
	private ArrayAdapter<String> mAdapter;
	private TextView mTextSSID;
	private ScrollView mScrollView;
	private EditText mEditPassword;
	private EditText mEditIp;
	private EditText mEditGateway;
	private EditText mEditSubnetMask;
	private EditText mEditDns;
	private CheckBox mShowPassword;
	private CheckBox mShowAdvanced;
	private LinearLayout mIpSet;
	private LinearLayout mStaticIp;
	private Spinner mSpinner;
	private Button mButtonForget;
	private Button mButtonDisconnect;
	private FrameLayout mKeyboardView;
	private OnConnectDialogClickListener mListener;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.dialog_wifi, container, false);
		removeTitleBar();
		setLayout(0.7f, 0.9f);
		initView();
        initListener();
        initData();
		return mView;
	}
	
	private void initView() {
		mTextSSID = (TextView) mView.findViewById(R.id.tv_ssid);
		mScrollView = (ScrollView) mView.findViewById(R.id.scroll_view);
        mEditPassword = (EditText) mView.findViewById(R.id.et_password);
        mShowPassword = (CheckBox) mView.findViewById(R.id.show_password);
        mShowAdvanced = (CheckBox) mView.findViewById(R.id.advanced);
        mIpSet = (LinearLayout) mView.findViewById(R.id.ip_set);
        mStaticIp = (LinearLayout) mView.findViewById(R.id.static_ip);
        mSpinner = (Spinner) mView.findViewById(R.id.spinner);
        mEditIp = (EditText) mView.findViewById(R.id.et_ip);
        mEditGateway = (EditText) mView.findViewById(R.id.et_gateway);
        mEditSubnetMask = (EditText) mView.findViewById(R.id.et_subnet_mask);
        mEditDns = (EditText) mView.findViewById(R.id.et_dns);
        mButtonForget = (Button) mView.findViewById(R.id.btn_forget);
        mButtonDisconnect = (Button) mView.findViewById(R.id.btn_disconnect);
        mKeyboardView = (FrameLayout) mView.findViewById(R.id.frame_keyboard);
        mKeyboard = new KeyboardFragment();
        getChildFragmentManager().beginTransaction().replace(
        		R.id.frame_keyboard, mKeyboard).commitAllowingStateLoss();
       
	}

	private void initListener() {
		mButtonForget.setOnClickListener(this);
		mButtonDisconnect.setOnClickListener(this);
		setKeyboardListener();
		
		mShowPassword.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					mEditPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
				} else {
					mEditPassword.setInputType(InputType.TYPE_CLASS_TEXT 
							| InputType.TYPE_TEXT_VARIATION_PASSWORD);
				}
			}
		});
        
        mShowAdvanced.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					mIpSet.setVisibility(View.VISIBLE);
				} else {
					mIpSet.setVisibility(View.GONE);
				}
			}
		});
       
        mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position,
					long id) {
				if (position == WifiScanResult.STATIC_IP) {
					mStaticIp.setVisibility(View.VISIBLE);
				} else {
					mStaticIp.setVisibility(View.GONE);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});
	}
	
	private void setKeyboardListener() {
		mKeyboard.setOnKeyboardListener(new OnKeyboardListener() {

			@Override
			public void onKeyboardClick(String key) {
				if (mEditPassword.hasFocus()) {
					editText(mEditPassword, key);
				} else if (mEditIp.hasFocus()) {
					if (key.equals("-1") || key.equals(".") || isNumber(key)) {
						editText(mEditIp, key);
					}
				} else if (mEditGateway.hasFocus()) {
					if (key.equals("-1") || key.equals(".") || isNumber(key)) {
						editText(mEditGateway, key);
					}
				} else if (mEditSubnetMask.hasFocus()) {
					if (key.equals("-1") || key.equals(".") || isNumber(key)) {
						editText(mEditSubnetMask, key);
					}
				} else if (mEditDns.hasFocus()) {
					if (key.equals("-1") || key.equals(".") || isNumber(key)) {
						editText(mEditDns, key);
					}
				}
			}
		});
	}
	
	private void editText(EditText editText, String key){
		mEditTool.setEditText(editText);
		if (key.equals("-1")) {
			mEditTool.deleteText();
		} else {
			mEditTool.appendTextTo(key);
		}
	}
	
	private boolean isNumber(String key) {
		try {
			if (Integer.parseInt(key) >= 0 && Integer.parseInt(key) < 10) return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private void initData() {
		mEditTool = EditTextTool.getInstance();
		mIpSetList = new ArrayList<String>();
		mIpSetList.add(getActivity().getString(R.string.wifi_ip_dynamic));    
        mIpSetList.add(getActivity().getString(R.string.wifi_ip_static)); 
        mAdapter = new ArrayAdapter<String>(getActivity(), 
        		android.R.layout.simple_spinner_item, mIpSetList);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mScrollView.setVisibility(View.VISIBLE);
        mShowAdvanced.setVisibility(View.VISIBLE);
        mSpinner.setAdapter(mAdapter);
        mButtonForget.setText(R.string.cancel);
        mButtonDisconnect.setText(R.string.wifi_connect);
        mKeyboardView.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (ssid != null) {
			mTextSSID.setText(ssid);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_forget:
			dismiss();
			break;
		case R.id.btn_disconnect:
			connect();
			break;
		default:
			break;
		}
	}

	private void connect() {
		String password = mEditPassword.getText().toString().trim();
    	
		if (password.length() >= 8) {
			if (mSpinner.getSelectedItemPosition() == WifiScanResult.STATIC_IP) {
				String ipAddress = mEditIp.getText().toString().trim();
				String gateway = mEditGateway.getText().toString().trim();
				String dnsAddress = mEditDns.getText().toString().trim();
				String subnetMask = mEditSubnetMask.getText().toString().trim();
				
				if (TextUtils.isEmpty(ipAddress)) {
					MyToast.show(R.string.wifi_ip_settings_empty_ip_address);
                    return;
                }
                if (TextUtils.isEmpty(gateway)) {
                    gateway = ipAddress.substring(0, ipAddress.lastIndexOf(".") + 1) + "1";
                }
                if (TextUtils.isEmpty(dnsAddress)) {
                    dnsAddress = getString(R.string.wifi_dns_hint);
                }
                if (TextUtils.isEmpty(subnetMask)) {
                    subnetMask = getString(R.string.wifi_subnet_mask_hint);
                }
                if (!WifiSettings.isIpAddress(ipAddress)) {
                	MyToast.show(R.string.wifi_ip_settings_invalid_ip_address);
                    return;
                }
                if (!WifiSettings.isIpAddress(gateway)) {
                	MyToast.show(R.string.wifi_ip_settings_invalid_gateway);
                    return;
                }
                if (!WifiSettings.isIpAddress(dnsAddress)) {
                	MyToast.show(R.string.wifi_ip_settings_invalid_dns);
                    return;
                }
                if (!WifiSettings.isIpAddress(subnetMask)) {
                	MyToast.show(R.string.wifi_ip_settings_invalid_subnet_mask);
                    return;
                }
				if (mListener != null) {
					mListener.onStaticIpConnect(ssid, password, security, 
							ipAddress, dnsAddress, gateway);
				}
			} else {
				if (mListener != null) {
					mListener.onConnectWifi(ssid, password, security);
				}
			}
    		dismiss();
		} else if (password.length() > 0) {
			MyToast.show(R.string.wifi_password_length_error);
		} else {
			MyToast.show(R.string.wifi_password_input);
		}
	}
	
	public interface OnConnectDialogClickListener {
		
		public void onStaticIpConnect(String ssid, String password, int security, 
				String ipAddress, String dnsAddress, String gateway);
		
		public void onConnectWifi(String ssid, String password, int security);
	}
	
	public void setOnConnectDialogClickListener(
			OnConnectDialogClickListener listener) {
		mListener = listener;
	}
}

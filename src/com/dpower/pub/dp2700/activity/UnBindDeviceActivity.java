package com.dpower.pub.dp2700.activity;

import java.util.ArrayList;
import java.util.List;
import com.dpower.cloudintercom.CloudIntercom;
import com.dpower.cloudintercom.Constant;
import com.dpower.domain.BindAccountInfo;
import com.dpower.dpsiplib.model.PhoneMessageMod;
import com.dpower.dpsiplib.service.DPSIPService;
import com.dpower.dpsiplib.utils.MSG_TYPE;
import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.adapter.DevicesListAdapter;
import com.dpower.pub.dp2700.application.App;
import com.dpower.pub.dp2700.dialog.TipsDialog;
import com.dpower.pub.dp2700.dialog.TipsDialog.OnDialogClickListener;
import com.dpower.pub.dp2700.tools.MyToast;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.util.ReceiverAction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

/**
 * 设备管理
 */
public class UnBindDeviceActivity extends BaseActivity implements OnClickListener {
	
	private static final String DEVICE_TYPE_PHONE = "1";//移动设备
	private static final String DEVICE_TYPE_CARDS = "2";//门磁卡片
	private static final String DEVICE_TYPE_OTHER = "0";//其他设备
	private ListView mListView;
	private DevicesListAdapter mAdapter;
	private UnBindPhoneReceiver unBindPhoneReceiver;
	private IntentFilter filter_one_success;
	private IntentFilter filter_one_faile;
	private IntentFilter filter_all_success;
	private IntentFilter filter_all_faile;
	private IntentFilter filter_and_success;
	private IntentFilter filter_and_faile;
	private IntentFilter filter_ios_success;
	private IntentFilter filter_ios_faile;
	/** 移动设备 */
	private Button mButtonPhoneDevices;
	/** 门磁设备 */
	private Button mButtonCardDevices;
	/** 其他设备 */
	private Button mButtonOtherDevices;

	/** 当前正在显示的按键 */
	private Button mButtonCurrent;
	private List<BindAccountInfo> mBindAccountList;
	private Handler mHandler;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_unbind_device);
		init();
		initReceiver();
	}
	
	/**  广播接收初始化 */
	private void initReceiver(){
		filter_one_success = new IntentFilter(ReceiverAction.ACTION_UNBIND_PHONE_ONE_SUCCESS);
		filter_one_faile = new IntentFilter(ReceiverAction.ACTION_UNBIND_PHONE_ONE_FAILED);
		filter_all_success = new IntentFilter(ReceiverAction.ACTION_UNBIND_PHONE_ALL_SUCCESS);
		filter_all_faile = new IntentFilter(ReceiverAction.ACTION_UNBIND_PHONE_ALL_FAILED);
		filter_and_success = new IntentFilter(ReceiverAction.ACTION_UNBIND_PHONE_AND_SUCCESS);
		filter_and_faile = new IntentFilter(ReceiverAction.ACTION_UNBIND_PHONE_AND_FAILED);
		filter_ios_success = new IntentFilter(ReceiverAction.ACTION_UNBIND_PHONE_IOS_SUCCESS);
		filter_ios_faile = new IntentFilter(ReceiverAction.ACTION_UNBIND_PHONE_IOS_FAILED);
		unBindPhoneReceiver = new UnBindPhoneReceiver();
		App.getInstance().getContext().registerReceiver(unBindPhoneReceiver, filter_one_success);
		App.getInstance().getContext().registerReceiver(unBindPhoneReceiver, filter_one_faile);
		App.getInstance().getContext().registerReceiver(unBindPhoneReceiver, filter_all_success);
		App.getInstance().getContext().registerReceiver(unBindPhoneReceiver, filter_all_faile);
		App.getInstance().getContext().registerReceiver(unBindPhoneReceiver, filter_and_success);
		App.getInstance().getContext().registerReceiver(unBindPhoneReceiver, filter_and_faile);
		App.getInstance().getContext().registerReceiver(unBindPhoneReceiver, filter_ios_success);
		App.getInstance().getContext().registerReceiver(unBindPhoneReceiver, filter_ios_faile);
	}
	
	private void init() {
		mContext = this;
		mButtonPhoneDevices = (Button) findViewById(R.id.btn_all_devices);
		mButtonPhoneDevices.setOnClickListener(this);
		mButtonCardDevices = (Button) findViewById(R.id.btn_devices_and);
		mButtonCardDevices.setOnClickListener(this);
		mButtonOtherDevices = (Button) findViewById(R.id.btn_devices_ios);
		mButtonOtherDevices.setOnClickListener(this);
		findViewById(R.id.btn_back).setOnClickListener(this);
		findViewById(R.id.btn_delete).setOnClickListener(this);
		findViewById(R.id.btn_delete_all).setOnClickListener(this);
		mListView = (ListView) findViewById(R.id.list_view_call_log);
		mBindAccountList = new ArrayList<BindAccountInfo>();
		mAdapter = new DevicesListAdapter(mContext);
		mListView.setAdapter(mAdapter);
		mButtonCurrent = mButtonPhoneDevices;
		mHandler = new Handler();
		updateCurrentList(mButtonPhoneDevices);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mAdapter.checkID = position;
				mAdapter.notifyDataSetChanged();
			}
		});
	}

	private void updateCurrentList(Button button) {
		// 恢复上一次值
		mButtonCurrent.setBackgroundColor(getResources().getColor(R.color.Transparency));
		mButtonCurrent.setClickable(true);
		// 设置当前的值
		button.setBackgroundColor(getResources().getColor(R.color.DialogTransparency));
		mButtonCurrent = button;
		mButtonCurrent.setClickable(false);
		mBindAccountList.clear();
		mAdapter.checkID = -1;
		mAdapter.setDevicesList(mBindAccountList);
		mAdapter.notifyDataSetChanged();
		new Thread(new Runnable() {

			@Override
			public void run() {
				updateData();
			}
		}).start();
	}
	
	private synchronized void updateData() {
		ArrayList<BindAccountInfo> infos = null;
		if (mButtonCurrent == mButtonCardDevices) {
			infos = DPFunction.getAccountByPhonetpye("1");
		} else if (mButtonCurrent == mButtonOtherDevices) {
			infos = DPFunction.getAccountByPhonetpye("2");
		} else if (mButtonCurrent == mButtonPhoneDevices) {
			infos = DPFunction.getAccountInfoList();
		}
		
		ArrayList<BindAccountInfo> mods = new ArrayList<BindAccountInfo>();
		for (int i = 0; i < infos.size(); i++) {
			mods.add(new BindAccountInfo());
			mods.get(i).mDB_id = infos.get(i).getmDB_id();
			mods.get(i).accountname = infos.get(i).getAccountname();
			mods.get(i).isonline = infos.get(i).getIsonline();
			mods.get(i).phonetype = infos.get(i).getPhonetype();
		}
		mBindAccountList = mods;
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				mAdapter.setDevicesList(mBindAccountList);
				mAdapter.notifyDataSetChanged();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		findViewById(R.id.root_view).setBackground(
				SPreferences.getInstance().getWallpaper());
		updateCurrentList(mButtonCurrent);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (unBindPhoneReceiver != null) {
			App.getInstance().getContext().unregisterReceiver(unBindPhoneReceiver);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_all_devices://移动设备
				updateCurrentList(mButtonPhoneDevices);
				break;
			case R.id.btn_devices_and://所有安卓设备
				updateCurrentList(mButtonCardDevices);
				break;
			case R.id.btn_devices_ios://所有苹果设备
				updateCurrentList(mButtonOtherDevices);
				break;
			case R.id.btn_back://返回
				finish();
				break;
			case R.id.btn_delete://解绑选中设备
				if (mAdapter.checkID >= 0) {
					TipsDialog dialog = new TipsDialog(mContext);
					dialog.setContent(getString(R.string.unbind_or_not) + "?");
					dialog.setOnClickListener(new OnDialogClickListener() {
						
						@Override
						public void onClick() {
							String sip_phone = mAdapter.getDevicesList().get(mAdapter.checkID).accountname;
							String msg_body = CloudIntercom.getRoomInfo();
							DPSIPService.currentMsgType = MSG_TYPE.MSG_BACK_UNBIND_PHONE_ONE;
							DPSIPService.sendInstantMessage(sip_phone, DPSIPService.getMsgCommand(new PhoneMessageMod(Constant.PHONE_CLOUD_UNBIND, msg_body, "0")));
						}
					});
					dialog.show();
				} else if (mAdapter.getDevicesList().size() > 0) {
					MyToast.show(R.string.no_item_check);
				} else {
					MyToast.show(R.string.no_item_to_del);
				}
				break;
			case R.id.btn_delete_all://解绑所有设备
				if (mAdapter.getDevicesList().size() > 0) {
					TipsDialog dialog = new TipsDialog(mContext);
					if (mButtonCurrent == mButtonCardDevices) {
						dialog.setContent(getString(R.string.delete_and_devices) + "?");
					} else if (mButtonCurrent == mButtonOtherDevices) {
						dialog.setContent(getString(R.string.delete_ios_devices) + "?");
					} else {
						dialog.setContent(getString(R.string.delete_all_devices) + "?");
					}
					dialog.setOnClickListener(new OnDialogClickListener() {
						
						@Override
						public void onClick() {
							if (mButtonCurrent == mButtonCardDevices) {
								DPSIPService.currentMsgType = MSG_TYPE.MSG_BACK_UNBIND_PHONE_AND;
								unBindPhone(DEVICE_TYPE_PHONE);
							} else if (mButtonCurrent == mButtonOtherDevices) {
								DPSIPService.currentMsgType = MSG_TYPE.MSG_BACK_UNBIND_PHONE_IOS;
								unBindPhone(DEVICE_TYPE_CARDS);
							} else {
								DPSIPService.currentMsgType = MSG_TYPE.MSG_BACK_UNBIND_PHONE_ALL;
								unBindPhone(DEVICE_TYPE_OTHER);
							}
						}
					});
					dialog.show();
				} else {
					MyToast.show(R.string.no_item_to_del);
				}
				break;
			default:
				break;
		}
	}
	
	/**
	 * 按手机类型解绑手机
	 * @param type
	 */
	public void unBindPhone(String type){
		List<String> accounts = DPFunction.getAccountByPhoneType(type);
		for (String account : accounts) {
			String msg_body = CloudIntercom.getRoomInfo();
			DPSIPService.sendInstantMessage(account, DPSIPService.getMsgCommand(new PhoneMessageMod(Constant.PHONE_CLOUD_UNBIND, msg_body, "0")));
		}
	}
	
	/**  强制解绑手机    */
	public void forceUnbindPhone(){
		if (mAdapter.checkID >= 0) {
			TipsDialog dialog = new TipsDialog(mContext);
			dialog.setContent(getString(R.string.force_unbind_or_not) + "?");
			dialog.setOnClickListener(new OnDialogClickListener() {
				
				@Override
				public void onClick() {
					DPFunction.deleteAccount(mAdapter.getDevicesList().get(mAdapter.checkID).mDB_id);
					MyToast.show(R.string.force_unbind_phone_device_success);
					updateCurrentList(mButtonCurrent);
				}
			});
			dialog.show();
		} else if (mAdapter.getDevicesList().size() > 0) {
			MyToast.show(R.string.no_item_check);
		} else {
			MyToast.show(R.string.no_item_to_del);
		}
	}
	
	/**  解绑手机广播    */
	private class UnBindPhoneReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ReceiverAction.ACTION_UNBIND_PHONE_ONE_SUCCESS)) {
				DPFunction.deleteAccount(mAdapter.getDevicesList().get(mAdapter.checkID).mDB_id);
				MyToast.show(R.string.unbind_phone_device_success);
				updateCurrentList(mButtonCurrent);
				
			} else if (action.equals(ReceiverAction.ACTION_UNBIND_PHONE_ONE_FAILED)) {
				MyToast.show(R.string.unbind_phone_device_faile);
				forceUnbindPhone();
				
			} else if (action.equals(ReceiverAction.ACTION_UNBIND_PHONE_AND_SUCCESS)){
				DPFunction.clearAccount(DEVICE_TYPE_PHONE);
				MyToast.show(R.string.unbind_phone_device_success);
				updateCurrentList(mButtonCurrent);
				
			} else if (action.equals(ReceiverAction.ACTION_UNBIND_PHONE_AND_FAILED)){
				MyToast.show(R.string.unbind_phone_device_faile);
				
			} else if (action.equals(ReceiverAction.ACTION_UNBIND_PHONE_IOS_SUCCESS)){
				DPFunction.clearAccount(DEVICE_TYPE_CARDS);
				MyToast.show(R.string.unbind_phone_device_success);
				updateCurrentList(mButtonCurrent);
				
			} else if (action.equals(ReceiverAction.ACTION_UNBIND_PHONE_IOS_FAILED)){
				MyToast.show(R.string.unbind_phone_device_faile);
				
			} else if (action.equals(ReceiverAction.ACTION_UNBIND_PHONE_ALL_SUCCESS)){
				DPFunction.clearAccount(DEVICE_TYPE_OTHER);
				MyToast.show(R.string.unbind_phone_device_success);
				updateCurrentList(mButtonCurrent);
				
			} else if (action.equals(ReceiverAction.ACTION_UNBIND_PHONE_ALL_FAILED)){
				MyToast.show(R.string.unbind_phone_device_faile);
				
			}
		}
		
	}
}

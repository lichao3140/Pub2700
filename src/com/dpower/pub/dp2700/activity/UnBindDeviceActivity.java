package com.dpower.pub.dp2700.activity;

import java.util.ArrayList;
import java.util.List;
import com.dpower.domain.BindAccountInfo;
import com.dpower.dpsiplib.model.PhoneMessageMod;
import com.dpower.dpsiplib.service.DPSIPService;
import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.adapter.DevicesListAdapter;
import com.dpower.pub.dp2700.dialog.TipsDialog;
import com.dpower.pub.dp2700.dialog.TipsDialog.OnDialogClickListener;
import com.dpower.pub.dp2700.tools.MyToast;
import com.dpower.pub.dp2700.tools.SPreferences;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

/**
 * 手机解绑
 */
public class UnBindDeviceActivity extends BaseActivity implements OnClickListener {
	private static final String UNBIND_MSG_PHONE = "07";
	private ListView mListView;
	private DevicesListAdapter mAdapter;
	/** 全部设备 */
	private Button mButtonAllDevices;
	/** Android设备 */
	private Button mButtonDevicesAnd;
	/** iPhone设备 */
	private Button mButtonDevicesIos;

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
	}
	
	private void init() {
		mContext = this;
		mButtonAllDevices = (Button) findViewById(R.id.btn_all_devices);
		mButtonAllDevices.setOnClickListener(this);
		mButtonDevicesAnd = (Button) findViewById(R.id.btn_devices_and);
		mButtonDevicesAnd.setOnClickListener(this);
		mButtonDevicesIos = (Button) findViewById(R.id.btn_devices_ios);
		mButtonDevicesIos.setOnClickListener(this);
		findViewById(R.id.btn_back).setOnClickListener(this);
		findViewById(R.id.btn_delete).setOnClickListener(this);
		findViewById(R.id.btn_delete_all).setOnClickListener(this);
		mListView = (ListView) findViewById(R.id.list_view_call_log);
		mBindAccountList = new ArrayList<BindAccountInfo>();
		mAdapter = new DevicesListAdapter(mContext);
		mListView.setAdapter(mAdapter);
		mButtonCurrent = mButtonAllDevices;
		mHandler = new Handler();
		updateCurrentList(mButtonAllDevices);
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
		ArrayList<BindAccountInfo> infos;
		if (mButtonCurrent == mButtonDevicesAnd) {
			infos = DPFunction.getAccountByPhonetpye("1");
		} else if (mButtonCurrent == mButtonDevicesIos) {
			infos = DPFunction.getAccountByPhonetpye("2");
		} else {
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
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_all_devices:
				updateCurrentList(mButtonAllDevices);
				break;
			case R.id.btn_devices_and:
				updateCurrentList(mButtonDevicesAnd);
				break;
			case R.id.btn_devices_ios:
				updateCurrentList(mButtonDevicesIos);
				break;
			case R.id.btn_back:
				finish();
				break;
			case R.id.btn_delete:
				if (mAdapter.checkID >= 0) {
					TipsDialog dialog = new TipsDialog(mContext);
					dialog.setContent(getString(R.string.delete_or_not) + "?");
					dialog.setOnClickListener(new OnDialogClickListener() {
						
						@Override
						public void onClick() {
							String sip_phone = mAdapter.getDevicesList().get(mAdapter.checkID).accountname;
							String msg_body = DPFunction.getRoomCode();
							DPSIPService.sendInstantMessage(sip_phone, DPSIPService.getMsgCommand(new PhoneMessageMod(UNBIND_MSG_PHONE, msg_body, "0")));
							DPFunction.deleteAccount(mAdapter.getDevicesList()
									.get(mAdapter.checkID).mDB_id);
							MyToast.show(R.string.delete_success);
							updateCurrentList(mButtonCurrent);
						}
					});
					dialog.show();
				} else if (mAdapter.getDevicesList().size() > 0) {
					MyToast.show(R.string.no_item_check);
				} else {
					MyToast.show(R.string.no_item_to_del);
				}
				break;
			case R.id.btn_delete_all:
				if (mAdapter.getDevicesList().size() > 0) {
					TipsDialog dialog = new TipsDialog(mContext);
					if (mButtonCurrent == mButtonDevicesAnd) {
						dialog.setContent(getString(R.string.delete_and_devices) + "?");
					} else if (mButtonCurrent == mButtonDevicesIos) {
						dialog.setContent(getString(R.string.delete_ios_devices) + "?");
					} else {
						dialog.setContent(getString(R.string.delete_all_devices) + "?");
					}
					dialog.setOnClickListener(new OnDialogClickListener() {
						
						@Override
						public void onClick() {
							int type = 0;
							if (mButtonCurrent == mButtonDevicesAnd) {
								type = 1;
								unBindPhone(1);
							} else if (mButtonCurrent == mButtonDevicesIos) {
								type = 2;
								unBindPhone(2);
							} else {
								type = 0;
								unBindPhone(0);
							}
							DPFunction.clearAccount(type);
							MyToast.show(R.string.delete_success);
							updateCurrentList(mButtonCurrent);
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
	public void unBindPhone(int type){
		List<String> accounts = DPFunction.getAccountByPhoneType(type);
		for (String account : accounts) {
			String msg_body = DPFunction.getRoomCode();
			DPSIPService.sendInstantMessage(account, DPSIPService.getMsgCommand(new PhoneMessageMod(UNBIND_MSG_PHONE, msg_body, "0")));
		}
	}
}

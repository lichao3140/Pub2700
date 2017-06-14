package com.dpower.pub.dp2700.activity;

import java.io.File;
import java.util.ArrayList;

import com.dpower.domain.MessageInfo;
import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.adapter.SMSAdapter;
import com.dpower.pub.dp2700.dialog.TipsDialog;
import com.dpower.pub.dp2700.dialog.TipsDialog.OnDialogClickListener;
import com.dpower.pub.dp2700.service.PhysicsKeyService;
import com.dpower.pub.dp2700.tools.FileOperate;
import com.dpower.pub.dp2700.tools.MyToast;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.util.ConstConf;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

/**
 * 小区消息
 */
public class SMSActivity extends BaseActivity implements OnClickListener {
	
	private Button mButtonAll;// 全部信息
	private Button mButtonPerson;// 个人信息
	private Button mButtonCommunity;// 小区信息
	private Button mButtonCurrent;// 当前选中的按键
	private ListView mListView;// 消息列表ListView
	private SMSAdapter mAdapter;// 列表适配器
	private BroadcastReceiver mMessageReceiver;
	private Context mContext;
	private boolean[] mKeySwitch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sms);
		init();
	}

	private void init() {
		mContext = this;
		mButtonAll = (Button) findViewById(R.id.btn_sms_all);
		mButtonAll.setOnClickListener(this);
		mButtonPerson = (Button) findViewById(R.id.btn_sms_personal);
		mButtonPerson.setOnClickListener(this);
		mButtonCommunity = (Button) findViewById(R.id.btn_sms_community);
		mButtonCommunity.setOnClickListener(this);
		findViewById(R.id.btn_delete).setOnClickListener(this);
		findViewById(R.id.btn_delete_all).setOnClickListener(this);
		findViewById(R.id.btn_check).setOnClickListener(this);
		findViewById(R.id.btn_back).setOnClickListener(this);
		mListView = (ListView) findViewById(R.id.list_view_msg);
		mAdapter = new SMSAdapter(mContext);
		mListView.setAdapter(mAdapter);
		mButtonCurrent = mButtonAll;
		updateCurrentList(mButtonAll);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mAdapter.checkPos = position;
				mAdapter.notifyDataSetChanged();
			}
		});
		IntentFilter filterSMS = new IntentFilter(MessageInfo.ACTION_MESSAGE);
		mMessageReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				updateCurrentList(mButtonCurrent);
			}

		};
		registerReceiver(mMessageReceiver, filterSMS);
	}

	private void updateCurrentList(Button button) {
		// 恢复上一次值
		mButtonCurrent.setBackgroundColor(getResources().getColor(
				R.color.Transparency));
		mButtonCurrent.setClickable(true);
		// 设置当前的值
		button.setBackgroundColor(getResources().getColor(
				R.color.DialogTransparency));
		mButtonCurrent = button;
		mButtonCurrent.setClickable(false);
		ArrayList<MessageInfo> datas;
		if (mButtonCurrent == mButtonCommunity) {
			datas = DPFunction.getMessageLogList(MessageInfo.PUBLIC);
		} else if (mButtonCurrent == mButtonPerson) {
			datas = DPFunction.getMessageLogList(MessageInfo.PERSONAL);
		} else {
			datas = DPFunction.getMessageLogList(MessageInfo.PERSONAL
					| MessageInfo.PUBLIC);
		}
		mAdapter.checkPos = -1;
		mAdapter.messageList = datas;
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onPause() {
		super.onPause();
		PhysicsKeyService.setKeySwitch(mKeySwitch);
	}

	@Override
	public void onResume() {
		super.onResume();
		findViewById(R.id.root_view).setBackground(
				SPreferences.getInstance().getWallpaper());
		mKeySwitch = PhysicsKeyService.getKeySwitch();
		PhysicsKeyService.setKeySwitch(new boolean[] {
				false, true, true, false, false });
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mMessageReceiver != null) {
			unregisterReceiver(mMessageReceiver);
			mMessageReceiver = null;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_sms_all:
				// 全部消息
				updateCurrentList(mButtonAll);
				break;
			case R.id.btn_sms_personal:
				// 个人信息
				updateCurrentList(mButtonPerson);
				break;
			case R.id.btn_sms_community:
				// 小区信息
				updateCurrentList(mButtonCommunity);
				break;
			case R.id.btn_delete:
				if (mAdapter.checkPos >= 0) {
					TipsDialog dialog = new TipsDialog(mContext);
					dialog.setContent(getString(R.string.delete_or_not) + "?");
					dialog.setOnClickListener(new OnDialogClickListener() {
						
						@Override
						public void onClick() {
							DPFunction.deleteMessageLog(mAdapter.messageList
									.get(mAdapter.checkPos)
									.getDb_id());
							String resName = mAdapter.messageList.get(mAdapter.checkPos)
									.getResName();
							if (resName != null) {
								File file = new File(ConstConf.MESSAGE_PATH 
										+ File.separator + resName);
								if (file.exists()) {
									file.delete();
								}
							}
							MyToast.show(R.string.delete_success);
							updateCurrentList(mButtonCurrent);
						}
					});
					dialog.show();
				} else if (mAdapter.messageList.size() > 0) {
					MyToast.show(R.string.no_item_check);
				} else {
					MyToast.show(R.string.no_item_to_del);
				}
				break;
			case R.id.btn_delete_all:
				if (mAdapter.messageList.size() > 0) {
					TipsDialog dialog = new TipsDialog(mContext);
					dialog.setContent(getString(R.string.text_delete_all) + "?");
					dialog.setOnClickListener(new OnDialogClickListener() {
						
						@Override
						public void onClick() {
							if (mButtonCurrent == mButtonCommunity) {
								DPFunction.deleteAllMessageLog(MessageInfo.PUBLIC);
								deleteFile();
							} else if (mButtonCurrent == mButtonPerson) {
								DPFunction.deleteAllMessageLog(MessageInfo.PERSONAL);
								deleteFile();
							} else {
								DPFunction.deleteAllMessageLog(MessageInfo.PERSONAL
												| MessageInfo.PUBLIC);
								File file = new File(ConstConf.MESSAGE_PATH);
								if (file.exists()) {
									FileOperate.recursionDeleteFile(file);
								}
							}
							MyToast.show(R.string.delete_success);
							updateCurrentList(mButtonCurrent);
						}
					});
					dialog.show();
				} else {
					MyToast.show(R.string.no_item_to_del);
				}
				break;
			case R.id.btn_check:
				if (mAdapter.checkPos >= 0) {
					mAdapter.messageList.get(mAdapter.checkPos).setRead(true);
					DPFunction.modifyMessageLog(mAdapter.messageList
							.get(mAdapter.checkPos));
					mAdapter.notifyDataSetChanged();
					String resName = mAdapter.messageList.get(mAdapter.checkPos)
							.getResName();
					if (resName == null) {
						MyToast.show(R.string.no_message_body);
						return;
					}
					Intent intent = new Intent();
					intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
					intent.setClass(mContext, SMSCheckActivity.class);
					intent.putExtra("resName", resName);
					startActivity(intent);
				} else if (mAdapter.messageList.size() > 0) {
					MyToast.show(R.string.no_item_check);
				} else {
					MyToast.show(R.string.no_item_to_del);
				}
				break;
			case R.id.btn_back:
				finish();
				break;
			default:
				break;
		}
	}
	
	private void deleteFile() {
		if (mAdapter.messageList != null 
				&& mAdapter.messageList.size() > 0) {
			for (int i = 0; i < mAdapter.messageList.size(); i++) {
				String resName = mAdapter.messageList.get(i)
						.getResName();
				if (resName != null) {
					File file = new File(ConstConf.MESSAGE_PATH 
							+ File.separator + resName);
					if (file.exists()) {
						file.delete();
					}
				}
			}
		}
	}
}

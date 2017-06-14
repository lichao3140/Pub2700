package com.dpower.pub.dp2700.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.dpower.domain.CallInfomation;
import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.adapter.CallRecordAdapter;
import com.dpower.pub.dp2700.dialog.TipsDialog;
import com.dpower.pub.dp2700.dialog.TipsDialog.OnDialogClickListener;
import com.dpower.pub.dp2700.model.CallRecordMod;
import com.dpower.pub.dp2700.tools.FileOperate;
import com.dpower.pub.dp2700.tools.MyToast;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.util.CommonUT;
import com.dpower.util.ConstConf;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

/**
 * 通话记录
 */
public class CallLogActivity extends BaseActivity implements OnClickListener {
	
	private ListView mListView;
	private CallRecordAdapter mAdapter;
	/** 未接通话 */
	private Button mButtonNotAccept;
	/** 已接通话 */
	private Button mButtonAccepted;
	/** 已拨通话 */
	private Button mButtonCalled;

	/** 当前正在显示的按键 */
	private Button mButtonCurrent;
	private List<CallRecordMod> mCallRecordList;
	private Handler mHandler;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_call_log);
		init();
	}
	
	private void init() {
		mContext = this;
		mButtonNotAccept = (Button) findViewById(R.id.btn_missed);
		mButtonNotAccept.setOnClickListener(this);
		mButtonAccepted = (Button) findViewById(R.id.btn_answered);
		mButtonAccepted.setOnClickListener(this);
		mButtonCalled = (Button) findViewById(R.id.btn_called_out);
		mButtonCalled.setOnClickListener(this);
		findViewById(R.id.btn_back).setOnClickListener(this);
		findViewById(R.id.btn_delete).setOnClickListener(this);
		findViewById(R.id.btn_delete_all).setOnClickListener(this);
		findViewById(R.id.btn_call).setOnClickListener(this);
		mListView = (ListView) findViewById(R.id.list_view_call_log);
		mCallRecordList = new ArrayList<CallRecordMod>();
		mAdapter = new CallRecordAdapter(mContext);
		mListView.setAdapter(mAdapter);
		mButtonCurrent = mButtonNotAccept;
		mHandler = new Handler();
		updateCurrentList(mButtonNotAccept);
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
		mButtonCurrent.setBackgroundColor(getResources().getColor(
				R.color.Transparency));
		mButtonCurrent.setClickable(true);
		// 设置当前的值
		button.setBackgroundColor(getResources().getColor(
				R.color.DialogTransparency));
		mButtonCurrent = button;
		mButtonCurrent.setClickable(false);
		mCallRecordList.clear();
		mAdapter.checkID = -1;
		mAdapter.setCallRecordList(mCallRecordList);
		mAdapter.notifyDataSetChanged();
		new Thread(new Runnable() {

			@Override
			public void run() {
				updateData();
			}
		}).start();
	}
	
	private synchronized void updateData() {
		ArrayList<CallInfomation> infos;
		if (mButtonCurrent == mButtonAccepted) {
			infos = DPFunction.getCallLogList(CallInfomation.CALL_IN_ACCEPT);
		} else if (mButtonCurrent == mButtonCalled) {
			infos = DPFunction.getCallLogList(CallInfomation.CALL_OUT_ACCEPT
							| CallInfomation.CALL_OUT_UNACCEPT);
		} else {
			infos = DPFunction.getCallLogList(CallInfomation.CALL_IN_UNACCEPT);
			for (int i = 0; i < infos.size(); i++) {
				infos.get(i).setIsRead(true);
				DPFunction.modifyCallLog(infos.get(i));
			}
		}
		
		ArrayList<CallRecordMod> mods = new ArrayList<CallRecordMod>();
		for (int i = 0; i < infos.size(); i++) {
			mods.add(new CallRecordMod());
			mods.get(i).id = infos.get(i).getDb_id();
			mods.get(i).callType = infos.get(i).getType();
			mods.get(i).isDoor = infos.get(i).isDoor();
			mods.get(i).beginTime = CommonUT.formatTime(
					infos.get(i).getStartTime());
			mods.get(i).talkTime = CommonUT.formatTime(
					infos.get(i).getEndTime() - infos.get(i).getStartTime());
			mods.get(i).roomCode = infos.get(i).getRemoteCode();
		}
		mCallRecordList = mods;
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				mAdapter.setCallRecordList(mCallRecordList);
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
			case R.id.btn_missed:
				updateCurrentList(mButtonNotAccept);
				break;
			case R.id.btn_answered:
				updateCurrentList(mButtonAccepted);
				break;
			case R.id.btn_called_out:
				updateCurrentList(mButtonCalled);
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
							DPFunction.deleteCallLog(mAdapter.getCallRecordList()
									.get(mAdapter.checkID).id);
							if (mAdapter.getCallRecordList().get(mAdapter.checkID)
									.callType == CallInfomation.CALL_IN_UNACCEPT && mAdapter
									.getCallRecordList().get(mAdapter.checkID).isDoor) {
								deleteFile();
							}
							MyToast.show(R.string.delete_success);
							updateCurrentList(mButtonCurrent);
						}
					});
					dialog.show();
				} else if (mAdapter.getCallRecordList().size() > 0) {
					MyToast.show(R.string.no_item_check);
				} else {
					MyToast.show(R.string.no_item_to_del);
				}
				break;
			case R.id.btn_delete_all:
				if (mAdapter.getCallRecordList().size() > 0) {
					TipsDialog dialog = new TipsDialog(mContext);
					if (mButtonCurrent == mButtonAccepted) {
						dialog.setContent(getString(R.string.delete_accept_record) + "?");
					} else if (mButtonCurrent == mButtonCalled) {
						dialog.setContent(getString(R.string.delete_callout_record) + "?");
					} else {
						dialog.setContent(getString(R.string.delete_no_accept_record) + "?");
					}
					dialog.setOnClickListener(new OnDialogClickListener() {
						
						@Override
						public void onClick() {
							int type = 0;
							if (mButtonCurrent == mButtonAccepted) {
								type = CallInfomation.CALL_IN_ACCEPT
										| CallInfomation.CALL_OUT_ACCEPT;
							} else if (mButtonCurrent == mButtonCalled) {
								type = CallInfomation.CALL_OUT_ACCEPT
										| CallInfomation.CALL_OUT_UNACCEPT;
							} else {
								type = CallInfomation.CALL_IN_UNACCEPT;
								File file = new File(ConstConf.VISIT_PATH);
								if (file.exists()) {
									FileOperate.recursionDeleteFile(file);
								}
							}
							DPFunction.deleteAllCallLog(type);
							MyToast.show(R.string.delete_success);
							updateCurrentList(mButtonCurrent);
						}
					});
					dialog.show();
				} else {
					MyToast.show(R.string.no_item_to_del);
				}
				break;
			case R.id.btn_call:
				if (mAdapter.checkID >= 0) {
					callOut(mAdapter.getCallRecordList()
							.get(mAdapter.checkID).roomCode);
				} else if (mAdapter.getCallRecordList().size() > 0) {
					MyToast.show(R.string.no_item_check);
				} else {
					MyToast.show(R.string.no_item_to_del);
				}
				break;
			default:
				break;
		}
	}
	
	private void deleteFile() {
		String picturePath = new String(ConstConf.VISIT_PATH
				+ File.separator + mAdapter.getCallRecordList()
				.get(mAdapter.checkID).beginTime.replaceAll(":", "-") + ".jpg");
		String audioPath = new String(ConstConf.VISIT_PATH
				+ File.separator + mAdapter.getCallRecordList()
				.get(mAdapter.checkID).beginTime.replaceAll(":", "-") + ".wav");
		File pictureFile = new File(picturePath);
		File audioFile = new File(audioPath);
		if (pictureFile.exists()) {
			pictureFile.delete();
		}
		if (audioFile.exists()) {
			audioFile.delete();
		}
	}
	
	private void callOut(String roomCode) {
		Intent intent;
		if (roomCode != null && roomCode.length() > 0) {
			if (roomCode.startsWith("2")) {
				MyToast.show(R.string.cannot_callout_type);
			} else {
				intent = new Intent();
				intent.setClass(mContext, CallOutActivity.class);
				intent.putExtra(CallOutActivity.INTENT_EXTRA, roomCode);
				intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
			}
		}
	}
}

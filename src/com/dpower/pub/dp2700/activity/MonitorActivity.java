package com.dpower.pub.dp2700.activity;

import java.util.ArrayList;

import com.dpower.domain.AddrInfo;
import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.activity.MonitorActivity.MonitorAdapter.ViewHolder;
import com.dpower.pub.dp2700.service.PhysicsKeyService;
import com.dpower.pub.dp2700.tools.MyToast;
import com.dpower.pub.dp2700.tools.ScreenUT;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.util.MyLog;
import com.dpower.util.ProjectConfigure;
import com.example.dpservice.CallReceiver;
import com.example.dpservice.IntercomCallback;
import com.example.live555.DPRtspClient;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 楼栋监控
 */
public class MonitorActivity extends BaseActivity 
		implements IntercomCallback {
	private static final String TAG = "MonitorActivity";
	
	public ArrayList<AddrInfo> mMonitorLists;
	private int mMonitorSessionID = 0;
	private CallReceiver mCallReceiver;
	private TextView mMonitorInfo;
	private ListView mMonitorListView;
	private ImageView mVideoView;
	private MonitorAdapter mAdapter;
	private DPRtspClient mRtspClient;
	private String mWebCameraUrl;
	private boolean mIsFirst = true;
	private boolean[] mKeySwitch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monitor);
		init();
	}
	
	private void init() {
		SPreferences sp = SPreferences.getInstance();
		mWebCameraUrl = "rtsp://" + sp.getWebCameraUserName() + ":"
				+ sp.getWebCameraPassword() + "@" + sp.getWebCameraIP()
				+ ":554/h264/ch1/main/av_stream";
		mMonitorInfo = (TextView) findViewById(R.id.tv_monitor_info);
		mMonitorListView = (ListView) findViewById(R.id.list_view_monitor);
		mVideoView = (ImageView) findViewById(R.id.image_video);
		findViewById(R.id.btn_back).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				hangUp();
				finish();
			}
		});
		/** 获取单元门口机列表 */
		mMonitorLists = DPFunction.getCellSeeList();
		if (mMonitorLists != null) {
			/** 获取别墅门口机列表 */
			ArrayList<AddrInfo> secDoorlist = DPFunction.getSecSeeList();
			if (secDoorlist != null) {
				mMonitorLists.addAll(secDoorlist);
				secDoorlist.clear();
				secDoorlist = null;
			}
		} else {
			mMonitorLists = new ArrayList<AddrInfo>();
		}
		if (ProjectConfigure.webCamera) {
			AddrInfo addInfo = new AddrInfo();
			addInfo.setName(getString(R.string.web_camera));
			mMonitorLists.add(addInfo);
		}
		mCallReceiver = new CallReceiver(this, CallReceiver.SEE_ACTION);
		registerReceiver(mCallReceiver, mCallReceiver.getFilter());

		mAdapter = new MonitorAdapter(this, mMonitorLists.size());
		mMonitorListView.setAdapter(mAdapter);
		mMonitorListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (mAdapter.selecteItem == position) {
					return;
				}
				mVideoView.setBackgroundColor(Color.TRANSPARENT);
				ViewHolder holder = (ViewHolder) view.getTag();
				String string = holder.textMonitor.getText().toString();
				mAdapter.selecteItem = position;
				mAdapter.notifyDataSetChanged();
				if (mMonitorSessionID != 0) {
					DPFunction.seeHangUp();
					mMonitorSessionID = 0;
				}
				if (mMonitorLists.get(position).getName()
						.equals(getString(R.string.web_camera))) {
					mMonitorInfo.setText(getString(R.string.is_monitoring) + string);
					int result = mRtspClient.OpenUrl(mWebCameraUrl);
					if (result == 0) {
						mVideoView.setBackgroundColor(Color.BLACK);
					} else {
						mMonitorInfo.setText(R.string.monitor_device_fail);
					}
					MyLog.print(TAG, "打开网络摄像头result = " + result);
				} else {
					mRtspClient.CloseUrl();
					mMonitorSessionID = DPFunction.seeDoor(
							mMonitorLists.get(position));
					mMonitorInfo.setText(getString(R.string.is_monitoring) + string);
				}
			}
		});
		if (mMonitorLists.size() == 0) {
			mMonitorInfo.setText(R.string.monitor_device_none);
		}
		mRtspClient = DPRtspClient.getInstance();
		mRtspClient.SetDecParam(
				getResources().getInteger(R.integer.monitor_width),
				getResources().getInteger(R.integer.monitor_height), 0,
				getResources().getInteger(R.integer.title_bar_height),
				getResources().getInteger(R.integer.monitor_width),
				getResources().getInteger(R.integer.monitor_height));
	}
	
	private void hangUp() {
		if (mMonitorSessionID != 0) {
			DPFunction.seeHangUp();
			mMonitorSessionID = 0;
		}
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus && mIsFirst) {
			mIsFirst = false;
			View view = mMonitorListView.getChildAt(0);
			if (view != null) {
				mMonitorListView.performItemClick(view, 0, view.getId());
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		MyLog.print(TAG, "onResume");
		ScreenUT.getInstance().acquireWakeLock();
		mKeySwitch = PhysicsKeyService.getKeySwitch();
		PhysicsKeyService.setKeySwitch(new boolean[] {
				true, true, false, false, false });
	}

	@Override
	protected void onPause() {
		super.onPause();
		MyLog.print(TAG, "onPause");
		ScreenUT.getInstance().releaseWakeLock();
		PhysicsKeyService.setKeySwitch(mKeySwitch);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		hangUp();
		if (mCallReceiver != null) {
			unregisterReceiver(mCallReceiver);
			mCallReceiver = null;
		}
		mRtspClient.CloseUrl();
		MyLog.print(TAG, "onDestroy");
	}

	@Override
	public void onAckBusy(int arg0, int arg1, String arg2) {
	}

	@Override
	public void onAckHold(int arg0, int arg1, String arg2) {

	}

	@Override
	public void onAckNoMeia(int arg0, int arg1, String arg2) {

	}

	@Override
	public void onAckRing(int arg0, int arg1, String arg2) {

	}

	@Override
	public void onCallOutAck(int arg0, int arg1, String arg2) {

	}

	@Override
	public void onError(int CallSessionID, int MsgType, String MsgContent) {
		if (CallSessionID == mMonitorSessionID) {
			DPFunction.seeHangUp();
			mMonitorSessionID = 0;
			mMonitorInfo.setText(R.string.monitor_device_fail);
		}
	}

	@Override
	public void onMessage(int arg0, int arg1, String arg2) {

	}

	@Override
	public void onMessageError(int arg0, int arg1, String arg2) {

	}

	@Override
	public void onMonitorTimeOut(int arg0, int arg1, String arg2) {
		hangUp();
		finish();
	}

	@Override
	public void onNewCallIn(int arg0, int arg1, String arg2) {
		hangUp();
		finish();
	}

	@Override
	public void onPhoneAccept() {

	}

	@Override
	public void onPhoneHangUp() {

	}

	@Override
	public void onRemoteAccept(int CallSessionID, int MsgType, String MsgContent) {
		if (CallSessionID == mMonitorSessionID) {
			mVideoView.setBackgroundColor(Color.BLACK);
			mMonitorInfo.setText("");
			final int temp = CallSessionID;
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					DPFunction.setLocalVideoVisable(temp, true);
					DPFunction.setVideoDisplayArea(
									temp, 0,
									getResources().getInteger(
											R.integer.title_bar_height),
									getResources().getInteger(
											R.integer.monitor_width),
									getResources().getInteger(
											R.integer.monitor_height));
				}
			}, 500);

		} else {
			MyToast.show(R.string.device_link_fail);
			MyLog.print("not see this CallSessionID");
		}
	}

	@Override
	public void onRemoteHangUp(int arg0, int arg1, String arg2) {
		DPFunction.seeHangUp();
		mMonitorSessionID = 0;
	}

	@Override
	public void onRemoteHold(int arg0, int arg1, String arg2) {

	}

	@Override
	public void onRemoteWake(int arg0, int arg1, String arg2) {

	}

	@Override
	public void onRingTimeOut(int arg0, int arg1, String arg2) {

	}

	@Override
	public void onTalkTimeOut(int arg0, int arg1, String arg2) {

	}

	class MonitorAdapter extends BaseAdapter {

		public int selecteItem = -1;
		private int mListSize;
		private Context mContext;

		public MonitorAdapter(Context context, int size) {
			mListSize = size;
			mContext = context;
		}

		@Override
		public int getCount() {
			return mListSize;
		}

		@Override
		public Object getItem(int position) {
			return mMonitorLists.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.adapter_monitor_list, null);
				holder = new ViewHolder(convertView);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			AddrInfo addrInfo = mMonitorLists.get(position);
			MyLog.print(TAG, addrInfo.toString());
			String unitRoomCode = addrInfo.getCode();
			String string;
			if (unitRoomCode != null) {
				String num = unitRoomCode.substring(unitRoomCode.length() - 2,
						unitRoomCode.length());
				string = "" + num + getString(R.string.number_hao);
				if (unitRoomCode.startsWith("2")) {
					// 单元门口机
					string += getString(R.string.gate_phone);
				} else if (unitRoomCode.startsWith("3")) {
					// 别墅门口机
					string += getString(R.string.villa_gate);
				} else {
					// 未知类型
					string += getString(R.string.unknow_type);
				}
			} else {
				string = getString(R.string.web_camera);
			}
			holder.textMonitor.setText(string);
			if (selecteItem == position) {
				holder.textMonitor.setTextColor(getResources().getColor(R.color.Teal));
			} else {
				holder.textMonitor.setTextColor(getResources().getColor(R.color.DarkBlue));
			}
			return convertView;
		}
		
		public class ViewHolder {
			public TextView textMonitor;
			
			public ViewHolder(View view) {
				textMonitor = (TextView) view.findViewById(R.id.tv_monitor);
			}
		}
	}
}

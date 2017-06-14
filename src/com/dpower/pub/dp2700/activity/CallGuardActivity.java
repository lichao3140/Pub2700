package com.dpower.pub.dp2700.activity;

import java.io.File;

import com.dpower.domain.CallInfomation;
import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.broadcastreceiver.AlarmFinishCallBroadcast;
import com.dpower.pub.dp2700.service.PhysicsKeyService;
import com.dpower.pub.dp2700.service.PhysicsKeyService.KeyCallback;
import com.dpower.pub.dp2700.tools.MediaPlayerTools;
import com.dpower.pub.dp2700.tools.MyToast;
import com.dpower.pub.dp2700.tools.RoomInfoUT;
import com.dpower.pub.dp2700.tools.ScreenUT;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.pub.dp2700.tools.VolumePopupWindow;
import com.dpower.util.ConstConf;
import com.dpower.util.MyLog;
import com.example.dpservice.CallReceiver;
import com.example.dpservice.IntercomCallback;

import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 呼叫保安分机
 */
public class CallGuardActivity extends BaseActivity implements
		OnClickListener, IntercomCallback {
	private static final String TAG = "CallGuardActivity";
	
	private final int KEY_HANG_UP = 100;
	private ImageView mVideoView;
	private TextView mCallInfo;
	private Button mVideoSwitch;
	private Button mButtonHangUp;
	private CallReceiver mCallReceiver;
	private AlarmFinishCallBroadcast mAlarmBroadcast;
	private int mCallSessionID = 0;
	private boolean mVideoShow = false;
	private ImageButton mVolumeUp, mVolumeDown;
	private int mCurrentVolume;
	private VolumePopupWindow mVolumePopupWindow;
	private MediaPlayerTools mPlayerTools;
	private boolean mIsHangUp = false;
	private boolean mIsCallCenter = false;
	private boolean[] mKeySwitch;
	private Context mContext;

	private Handler mHandler = new Handler(){
		
        @Override
        public void handleMessage(Message msg) {
        	super.handleMessage(msg);
	        switch (msg.what){
                case KEY_HANG_UP:
                	if (mButtonHangUp != null) {
    					mButtonHangUp.performClick();
    				}
                    break;
                default:
                    break;
	        }
        }
	};
	
	private KeyCallback mKeyCallback = new KeyCallback() {

		@Override
		public void onKey(int keyIO) {
			switch (keyIO) {
				case PhysicsKeyService.MESSAGE:
					break;
				case PhysicsKeyService.VOLUME:
					break;
				case PhysicsKeyService.MONITOR:
					break;
				case PhysicsKeyService.UNLOCK:
					break;
				case PhysicsKeyService.HANGUP:
					mHandler.sendEmptyMessage(KEY_HANG_UP);
					break;
				default:
					break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_call);
		init();
	}

	private void init() {
		mContext = this;
		mPlayerTools = new MediaPlayerTools(true);
		mVideoView = (ImageView) findViewById(R.id.image_video);
		mCallInfo = (TextView) findViewById(R.id.call_info);
		mVolumePopupWindow = new VolumePopupWindow(mContext, LayoutInflater.from(mContext)
				.inflate(R.layout.activity_call, null));
		mVideoSwitch = (Button) findViewById(R.id.btn_open_video);
		mVideoSwitch.setOnClickListener(this);
		mVideoSwitch.setClickable(false);
		mButtonHangUp = (Button) findViewById(R.id.btn_hang_up);
		mButtonHangUp.setOnClickListener(this);
		mVolumeUp = (ImageButton) findViewById(R.id.btn_volume_up);
		mVolumeUp.setOnClickListener(this);
		mVolumeUp.setClickable(false);
		mVolumeDown = (ImageButton) findViewById(R.id.btn_volume_down);
		mVolumeDown.setOnClickListener(this);
		mVolumeDown.setClickable(false);
		mCurrentVolume = SPreferences.getInstance().getTalkingVolume();
		mCallReceiver = new CallReceiver(this, CallReceiver.CALL_OUT_ACTION);
		registerReceiver(mCallReceiver, mCallReceiver.getFilter());
		mAlarmBroadcast = new AlarmFinishCallBroadcast();
		IntentFilter filter = new IntentFilter(DPFunction.ACTION_ALARMING);
		PhysicsKeyService.registerKeyCallback(mKeyCallback);
		registerReceiver(mAlarmBroadcast, filter);
		if (DPFunction.callSecurity() != 0) {
			MyToast.show(R.string.call_error);
			hangUp();
			finish();
			return;
		}
		int count = DPFunction.getCallOutSize();
		if (count > 0) {
			mCallInfo.setText(R.string.calling_guard);
		} else {
			hangUp();
			finish();
			return;
		}
		String path = SPreferences.getInstance().getRingAbsolutePath();
		if (TextUtils.isEmpty(path)) {
			mPlayerTools.initDefaultRingFile(getApplicationContext());
			path = SPreferences.getInstance().getRingAbsolutePath();
		}
		// 当铃声设置为SD卡的音频时，拔出SD卡后要恢复默认
		if (!new File(path).exists()) {
			SPreferences.getInstance().setRingAbsolutePath(
					ConstConf.RING_PATH + ConstConf.RING_MP3);
			path = SPreferences.getInstance().getRingAbsolutePath();
		}
		mPlayerTools.playMusic(path);
	}
	
	private CharSequence getRoomName(String roomCode) {
		StringBuffer result = new StringBuffer();
		result.append(getString(R.string.call_from_info));
		RoomInfoUT infoUT = new RoomInfoUT(roomCode);
		result.append(infoUT.getRoomName(mContext));
		return result.toString();
	}
	
	private void hangUp() {
		if (mPlayerTools != null) {
			mPlayerTools.release();
			mPlayerTools = null;
		}
		if (mCallSessionID != 0) {
			DPFunction.callHangUp(mCallSessionID);
			mCallSessionID = 0;
		}
		DPFunction.callOutHangUp();
		if (mCallReceiver != null) {
			unregisterReceiver(mCallReceiver);
			mCallReceiver = null;
		}
		if (mAlarmBroadcast != null) {
			unregisterReceiver(mAlarmBroadcast);
			mAlarmBroadcast = null;
		}
		mIsHangUp = true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		MyLog.print(TAG, "onResume");
		ScreenUT.getInstance().acquireWakeLock();
		mKeySwitch = PhysicsKeyService.getKeySwitch();
		PhysicsKeyService.setKeySwitch(new boolean[] {
				false, false, false, false, true });
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		MyLog.print(TAG, "onPause");
		ScreenUT.getInstance().releaseWakeLock();
		PhysicsKeyService.setKeySwitch(mKeySwitch);
	}

	@Override
	protected void onStop() {
		super.onStop();
		mVolumePopupWindow.cancelPopupWindow();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		PhysicsKeyService.unRegisterKeyCallback(mKeyCallback);
		if (!mIsHangUp) {
			hangUp();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_open_video:
			if (mCallSessionID != 0) {
				mVideoShow = !mVideoShow;
				if (mVideoShow) {
					mVideoSwitch.setText(R.string.text_close_video);
				} else {
					mVideoSwitch.setText(R.string.text_open_video);
				}
				DPFunction.setLocalVideoVisable(mCallSessionID, mVideoShow);
			}
			break;
		case R.id.btn_volume_up:
			if (mCurrentVolume < 5) {
				mCurrentVolume++;
				SPreferences.getInstance().setTalkingVolume(mCurrentVolume);
				DPFunction.setAudioVolume(mCallSessionID, mCurrentVolume * 20);
			}
			mVolumePopupWindow.show(mCurrentVolume);
			break;
		case R.id.btn_volume_down:
			if (mCurrentVolume > 0) {
				mCurrentVolume--;
				SPreferences.getInstance().setTalkingVolume(mCurrentVolume);
				DPFunction.setAudioVolume(mCallSessionID, mCurrentVolume * 20);
			}
			mVolumePopupWindow.show(mCurrentVolume);
			break;
		case R.id.btn_hang_up:
			MyToast.show(R.string.call_end);
			hangUp();
			finish();
			break;
		default:
			break;
		}
	}

	@Override
	public void onRingTimeOut(int CallSessionID, int MsgType, String MsgContent) {
		MyLog.print(TAG, "onRingTimeOut");
		if (DPFunction.getCallOutSize() == 0 && !mIsCallCenter) {
			MyToast.show(R.string.ring_time_out);
			int temp = DPFunction.callManager();
			if (temp == 0) {
				mCallInfo.setText(R.string.calling_management_center);
				mIsCallCenter = true;
			} else {
				MyToast.show(R.string.call_error);
				hangUp();
				finish();
			}
		} else if (DPFunction.getCallOutSize() == 0) {
			mIsCallCenter = false;
			MyToast.show(R.string.ring_time_out);
			hangUp();
			finish();
		}
	}

	@Override
	public void onTalkTimeOut(int CallSessionID, int MsgType, String MsgContent) {
		MyLog.print(TAG, "onTalkTimeOut");
		hangUp();
		finish();
	}

	@Override
	public void onMonitorTimeOut(int CallSessionID, int MsgType,
			String MsgContent) {
	}

	@Override
	public void onAckRing(int CallSessionID, int MsgType, String MsgContent) {
		MyLog.print("onAckRing CallSessionID = " + CallSessionID);
	}

	@Override
	public void onAckBusy(int CallSessionID, int MsgType, String MsgContent) {
		MyLog.print("onAckBusy CallSessionID = " + CallSessionID);
		RoomInfoUT infoUT = new RoomInfoUT(MsgContent);
		MyToast.show(infoUT.getRoomName(mContext) + getString(R.string.busy_line));
		if (DPFunction.getCallOutSize() == 0 && !mIsCallCenter) {
			int temp = DPFunction.callManager();
			if (temp == 0) {
				mCallInfo.setText(R.string.calling_management_center);
				mIsCallCenter = true;
			} else {
				MyToast.show(R.string.call_error);
				hangUp();
				finish();
			}
		} else if (DPFunction.getCallOutSize() == 0) {
			mIsCallCenter = false;
			hangUp();
			finish();
		}
		MyLog.print(TAG, "onAckBusy");
	}

	@Override
	public void onAckNoMeia(int CallSessionID, int MsgType, String MsgContent) {
	}

	@Override
	public void onAckHold(int CallSessionID, int MsgType, String MsgContent) {
	}

	@Override
	public void onCallOutAck(int CallSessionID, int MsgType, String MsgContent) {
	}

	@Override
	public void onNewCallIn(int CallSessionID, int MsgType, String MsgContent) {
	}

	@Override
	public void onRemoteHangUp(int CallSessionID, int MsgType, String MsgContent) {
		MyLog.print(TAG, "onRemoteHangUp");
		MyToast.show(R.string.be_cutoff);
		hangUp();
		finish();
	}

	@Override
	public void onRemoteAccept(int CallSessionID, int MsgType, String MsgContent) {
		if (mPlayerTools != null) {
			mPlayerTools.release();
			mPlayerTools = null;
		}
		mCallSessionID = CallSessionID;
		CallInfomation info = DPFunction.findCallOut(mCallSessionID);
		mCallInfo.setText(getRoomName(info.getRemoteCode()));
		DPFunction.setAudioVolume(mCallSessionID, mCurrentVolume * 20);
		DPFunction.setLocalVideoVisable(mCallSessionID, mVideoShow);
		DPFunction.setVideoDisplayArea(mCallSessionID, 0,
				getResources().getInteger(R.integer.title_bar_height),
				getResources().getInteger(R.integer.call_management_width),
				getResources().getInteger(R.integer.call_management_height));
		mVolumeDown.setClickable(true);
		mVolumeUp.setClickable(true);
		mVideoSwitch.setClickable(true);
		mVideoView.setBackgroundColor(Color.BLACK);
	}

	@Override
	public void onRemoteHold(int CallSessionID, int MsgType, String MsgContent) {
	}

	@Override
	public void onRemoteWake(int CallSessionID, int MsgType, String MsgContent) {
	}

	@Override
	public void onError(int CallSessionID, int MsgType, String MsgContent) {
		RoomInfoUT infoUT = new RoomInfoUT(MsgContent);
		MyToast.show(infoUT.getRoomName(mContext) + getString(R.string.no_online));
		if (DPFunction.getCallOutSize() == 0 && !mIsCallCenter) {
			int temp = DPFunction.callManager();
			if (temp == 0) {
				mCallInfo.setText(R.string.calling_management_center);
				mIsCallCenter = true;
			} else {
				MyToast.show(R.string.call_error);
				hangUp();
				finish();
			}
		} else if (DPFunction.getCallOutSize() == 0) {
			mIsCallCenter = false;
			hangUp();
			finish();
		}
	}

	@Override
	public void onMessage(int CallSessionID, int MsgType, String MsgContent) {
	}

	@Override
	public void onMessageError(int CallSessionID, int MsgType, String MsgContent) {
		if (DPFunction.getCallOutSize() == 0) {
			if (mCallInfo != null) {
				mCallInfo = null;
			}
			hangUp();
			finish();
			MyLog.print(MyLog.ERROR, TAG, "onMessageError");
		}
	}

	@Override
	public void onPhoneAccept() {
	}

	@Override
	public void onPhoneHangUp() {
	}
}

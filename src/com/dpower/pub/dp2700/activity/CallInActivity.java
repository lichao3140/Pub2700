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

import android.content.Intent;
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
 * 用户呼入界面
 */
public class CallInActivity extends BaseActivity implements 
		OnClickListener, IntercomCallback {
	private static final String TAG = "UserCallInActivity";
	
	private final int KEY_HANG_UP = 100;
	private ImageView mVideoView;
	private TextView mCallInfo;
	private Button mVideoSwitch;
	private Button mButtonAccept;
	private Button mButtonHangUp;
	private int mAcceptSessionID = 0;
	private String mRoomCode;
	private CallReceiver mCallReceiver;
	private AlarmFinishCallBroadcast mAlarmBroadcast;
	private boolean mVideoShow = false;
	private ImageButton mVolumeUp, mVolumeDown;
	private int mCurrentVolume;
	private VolumePopupWindow mVolumePopupWindow;
	private MediaPlayerTools mPlayerTools;
	private boolean mIsHangUp = false;
	private boolean[] mKeySwitch;
	private boolean mPhoneAccept = false;
	
	private Handler mHandler = new Handler(){
		
        @Override
        public void handleMessage(Message msg) {
        	super.handleMessage(msg);
	        switch (msg.what) {
				case KEY_HANG_UP:
					if (mButtonAccept != null && mButtonAccept.getVisibility() == View.VISIBLE) {
						mButtonAccept.performClick();
					} else if (mButtonHangUp != null) {
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
		mVideoView = (ImageView) findViewById(R.id.image_video);
		mCallInfo = (TextView) findViewById(R.id.call_info);
		mVideoSwitch = (Button) findViewById(R.id.btn_open_video);
		mVideoSwitch.setOnClickListener(this);
		mVideoSwitch.setClickable(false);
		mButtonAccept = (Button) findViewById(R.id.btn_answer);
		mButtonAccept.setVisibility(View.VISIBLE);
		mButtonAccept.setOnClickListener(this);
		mButtonHangUp = (Button) findViewById(R.id.btn_hang_up);
		mButtonHangUp.setOnClickListener(this);
		mVolumeUp = (ImageButton) findViewById(R.id.btn_volume_up);
		mVolumeUp.setOnClickListener(this);
		mVolumeUp.setClickable(false);
		mVolumeDown = (ImageButton) findViewById(R.id.btn_volume_down);
		mVolumeDown.setOnClickListener(this);
		mVolumeDown.setClickable(false);
		mCurrentVolume = SPreferences.getInstance().getTalkingVolume();
		mVolumePopupWindow = new VolumePopupWindow(this, LayoutInflater.from(this)
				.inflate(R.layout.activity_call, null));
		mPlayerTools = new MediaPlayerTools(true);
		Intent intent = getIntent();
		mAcceptSessionID = intent.getIntExtra("SessionID", 0);
		mRoomCode = intent.getStringExtra("code");
		MyLog.print("mCallSessionID = " + mAcceptSessionID);
		MyLog.print("mRoomCode = " + mRoomCode);
		mCallReceiver = new CallReceiver(this, CallReceiver.CALL_IN_ACTION);
		registerReceiver(mCallReceiver, mCallReceiver.getFilter());
		mAlarmBroadcast = new AlarmFinishCallBroadcast();
		IntentFilter filter = new IntentFilter(DPFunction.ACTION_ALARMING);
		registerReceiver(mAlarmBroadcast, filter);
		if (mCallInfo != null && mRoomCode != null) {
			mCallInfo.setText(getRoomName(mRoomCode));
			CallInfomation info = DPFunction.findCallIn(mAcceptSessionID);
			if (info != null) {

			} else {
				mAcceptSessionID = 0;
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
		PhysicsKeyService.registerKeyCallback(mKeyCallback);
	}
	
	private CharSequence getRoomName(String roomCode) {
		StringBuffer result = new StringBuffer();
		result.append(getString(R.string.call_from_info));
		RoomInfoUT infoUT = new RoomInfoUT(roomCode);
		result.append(infoUT.getRoomName(this));
		return result.toString();
	}
	
	private void hangUp() {
		if (mPlayerTools != null) {
			mPlayerTools.release();
			mPlayerTools = null;
		}
		if (!mPhoneAccept && mAcceptSessionID != 0) {
			DPFunction.setLocalVideoVisable(mAcceptSessionID, false);
			DPFunction.callHangUp(mAcceptSessionID);
			mAcceptSessionID = 0;
		}
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
		MyLog.print(TAG, "onPause");
		ScreenUT.getInstance().releaseWakeLock();
		PhysicsKeyService.setKeySwitch(mKeySwitch);
		super.onPause();
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
			case R.id.btn_answer:
				if (mPlayerTools != null) {
					mPlayerTools.release();
					mPlayerTools = null;
				}
				DPFunction.setLocalVideoVisable(mAcceptSessionID, mVideoShow);
				DPFunction.setVideoDisplayArea(mAcceptSessionID, 0,
						getResources().getInteger(R.integer.title_bar_height),
						getResources().getInteger(R.integer.user_call_in_width),
						getResources().getInteger(R.integer.user_call_in_height));
				DPFunction.accept(mAcceptSessionID, 0);
				DPFunction.setAudioVolume(mAcceptSessionID, mCurrentVolume * 20);
				mVolumeDown.setClickable(true);
				mVolumeUp.setClickable(true);
				mButtonAccept.setVisibility(View.GONE);
				mVideoSwitch.setClickable(true);
				mVideoView.setBackgroundColor(Color.BLACK);
				break;
			case R.id.btn_hang_up:
				MyToast.show(R.string.call_end);
				hangUp();
				finish();
				break;
			case R.id.btn_volume_up:
				if (mCurrentVolume < 5) {
					mCurrentVolume++;
					SPreferences.getInstance().setTalkingVolume(mCurrentVolume);
					DPFunction.setAudioVolume(mAcceptSessionID, mCurrentVolume * 20);
				}
				mVolumePopupWindow.show(mCurrentVolume);
				break;
			case R.id.btn_volume_down:
				if (mCurrentVolume > 0) {
					mCurrentVolume--;
					SPreferences.getInstance().setTalkingVolume(mCurrentVolume);
					DPFunction.setAudioVolume(mAcceptSessionID, mCurrentVolume * 20);
				}
				mVolumePopupWindow.show(mCurrentVolume);
				break;
			case R.id.btn_open_video:
				if (mAcceptSessionID != 0) {
					mVideoShow = !mVideoShow;
					if (mVideoShow) {
						mVideoSwitch.setText(R.string.text_close_video);
					} else {
						mVideoSwitch.setText(R.string.text_open_video);
					}
					DPFunction.setLocalVideoVisable(mAcceptSessionID, mVideoShow);
				}
				break;
			default:
				break;
		}
	}

	@Override
	public void onMonitorTimeOut(int CallSessionID, int MsgType,
			String MsgContent) {
		/** 监视超时, 不应该在此界面出现此消息 */
	}

	@Override
	public void onTalkTimeOut(int CallSessionID, int MsgType, String MsgContent) {
		MyToast.show(R.string.call_timeout);
		hangUp();
		finish();
	}

	@Override
	public void onRingTimeOut(int CallSessionID, int MsgType, String MsgContent) {
		MyToast.show(R.string.ring_time_out);
	}

	@Override
	public void onAckRing(int CallSessionID, int MsgType, String MsgContent) {
		/** 呼出结果返回，不应该在此界面出现此消息 */
	}

	@Override
	public void onAckBusy(int CallSessionID, int MsgType, String MsgContent) {
		/** 呼出结果返回，不应该在此界面出现此消息 */
	}

	@Override
	public void onAckNoMeia(int CallSessionID, int MsgType, String MsgContent) {
		/** 呼出结果返回，不应该在此界面出现此消息 */
	}

	@Override
	public void onAckHold(int CallSessionID, int MsgType, String MsgContent) {
		/** 呼出结果返回，不应该在此界面出现此消息 */
	}

	@Override
	public void onCallOutAck(int CallSessionID, int MsgType, String MsgContent) {
		/** 呼出结果返回，不应该在此界面出现此消息 */
	}

	@Override
	public void onNewCallIn(int CallSessionID, int MsgType, String MsgContent) {
		/** 不应该在呼入界面再次收到此消息 */
	}

	@Override
	public void onRemoteHangUp(int CallSessionID, int MsgType, String MsgContent) {
		MyToast.show(R.string.be_cutoff);
		mAcceptSessionID = 0;
		hangUp();
		finish();
	}

	@Override
	public void onRemoteAccept(int CallSessionID, int MsgType, String MsgContent) {
		/** 不应该在呼入界面再次收到此消息 */
	}

	@Override
	public void onRemoteHold(int CallSessionID, int MsgType, String MsgContent) {
		/** 暂不支持 */
	}

	@Override
	public void onRemoteWake(int CallSessionID, int MsgType, String MsgContent) {
		/** 暂不支持 */
	}

	@Override
	public void onError(int CallSessionID, int MsgType, String MsgContent) {
		hangUp();
		finish();
	}

	@Override
	public void onMessage(int CallSessionID, int MsgType, String MsgContent) {
		/** 保留 */
	}

	@Override
	public void onMessageError(int CallSessionID, int MsgType, String MsgContent) {
		/** 保留 */
	}

	@Override
	public void onPhoneAccept() {
		mPhoneAccept = true;
		hangUp();
		finish();
	}

	@Override
	public void onPhoneHangUp() {
		hangUp();
		finish();
	}
}

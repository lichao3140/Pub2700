package com.example.dpservice;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import com.dpower.domain.CallInfomation;
import com.dpower.function.DPFunction;
import com.dpower.util.MyLog;

/**
 * 对讲服务
 */
public class DPIntercomService extends Service {
	private static final String TAG = "DPIntercomService";
	
	/** 模拟小门口机正在通话 */
	public static boolean isAnalogTalking = false;
	private  ScheduledThreadPoolExecutor mIntercomScheduled;
	private CallReceiver mCallReceiver = null;
	private Context mContext;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;
		MyLog.print(TAG, "onCreate");
		/**
		 * 注册广播，接收JNIPhoneClass广播过来的呼叫消息
		 * 接收后，再区分成呼入、呼出、监视3类广播出去，所以，呼入、呼出、监视界面都要注册各自对应的广播
		 */
		mCallReceiver = new CallReceiver(mPhoneCallback);
		registerReceiver(mCallReceiver, mCallReceiver.getFilter());
		
		/**
		 * 开启定时器，1秒1次检测呼入、呼出、监视会话是否超时
		 */
		mIntercomScheduled = new ScheduledThreadPoolExecutor(5);
		mIntercomScheduled.scheduleWithFixedDelay(mCallRunnable, 1, 1, TimeUnit.SECONDS);
	}
	
	private Runnable mCallRunnable = new Runnable() {

		@Override
		public void run() {
			Message msg = new Message();
			msg.what = 1;
			mHandler.sendMessage(msg);
		}
	};
	
	private static Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				DPFunction.checkCallTime();
			}
		};
	};

	@Override
	public void onDestroy() {
		MyLog.print(TAG, "onDestroy");
		if (mCallReceiver != null) {
			unregisterReceiver(mCallReceiver);
			mCallReceiver = null;
		}
		if (mIntercomScheduled != null) {
			mIntercomScheduled.shutdown();
			mIntercomScheduled=null;
		}
		super.onDestroy();
	}

	/**
	 * 接收广播后再分呼入、呼出、监视3类广播出去
	 * */
	private IntercomCallback mPhoneCallback = new IntercomCallback() {
		
		@Override
		public void onRingTimeOut(int CallSessionID, int MsgType,
				String MsgContent) {
			MyLog.print("Should not be the case onRingTimeOut");
		}

		@Override
		public void onTalkTimeOut(int CallSessionID, int MsgType,
				String MsgContent) {
			MyLog.print("Should not be the case onTalkTimeOut");
		}

		@Override
		public void onMonitorTimeOut(int CallSessionID, int MsgType,
				String MsgContent) {
			MyLog.print("Should not be the case onMonitorTimeOut");
		}

		@Override
		public void onAckRing(int CallSessionID, int MsgType, String MsgContent) {
			MyLog.print("Should not be the case onAckRing");
		}

		@Override
		public void onAckBusy(int CallSessionID, int MsgType, String MsgContent) {
			MyLog.print("Should not be the case onAckBusy");
		}

		@Override
		public void onAckNoMeia(int CallSessionID, int MsgType,
				String MsgContent) {
			MyLog.print("Should not be the case onAckNoMeia");
		}

		@Override
		public void onAckHold(int CallSessionID, int MsgType, String MsgContent) {
			MyLog.print("Should not be the case onAckHold");
		}

		/**
		 * 收到呼叫结果 可能为 busy 忙 ring 振铃 hold 挂起 nomedia 无媒体
		 */
		@Override
		public void onCallOutAck(int CallSessionID, int MsgType,
				String MsgContent) {
			MyLog.print(TAG, "onCallOutAck " + MsgContent);
			String code = null;
			CallInfomation info = null;
			Intent intent = new Intent();
			if (MsgContent.equals(JniPhoneClass.CALL_BUSY)
					|| MsgContent.equals(JniPhoneClass.CALL_NOMEDIA)) {
				if (MsgContent.equals(JniPhoneClass.CALL_BUSY)) {
					intent.putExtra("MsgType", JniPhoneClass.CALLACK_BUSY);
				} else {
					intent.putExtra("MsgType", JniPhoneClass.CALLACK_NOMEDIA);
				}
				/** 监视失败 */
				if (CallSessionID == DPFunction.getSeeInfo().getSessionID()) {
					DPFunction.seeHangUp();
					intent.putExtra("code", DPFunction.getSeeInfo()
							.getRemoteCode());
					intent.setAction(CallReceiver.SEE_ACTION);
					mContext.sendBroadcast(intent);
				} else {
					/** 呼叫占线 */
					code = DPFunction.callHangUp(CallSessionID);
					intent.putExtra("code", code);
					intent.putExtra("SessionID", CallSessionID);
					intent.putExtra("MsgContent", MsgContent);
					intent.setAction(CallReceiver.CALL_OUT_ACTION);
					mContext.sendBroadcast(intent);
				}
			} else if (MsgContent.equals(JniPhoneClass.CALL_RING)) {
				intent.putExtra("MsgType", JniPhoneClass.CALLACK_RING);
				if (CallSessionID == DPFunction.getSeeInfo().getSessionID()) {
					intent.putExtra("code", DPFunction.getSeeInfo()
							.getRemoteCode());
					intent.setAction(CallReceiver.SEE_ACTION);
					mContext.sendBroadcast(intent);
				} else {
					info = DPFunction.findCallOut(CallSessionID);
					if (info != null) {
						intent.putExtra("code", info.getRemoteCode());
						intent.setAction(CallReceiver.CALL_OUT_ACTION);
						mContext.sendBroadcast(intent);
					} else {
						MyLog.print("Not find ring SessionID");
					}
				}
			} else if (MsgContent.equals(JniPhoneClass.CALL_HOLD)) {
				MyLog.print("Temporary does not support");
			} else {
				MyLog.print("The CallOutAck of the unknown");
			}
		}

		/**
		 * 新呼入
		 */
		@Override
		public void onNewCallIn(final int CallSessionID, final int MsgType,
				String MsgContent) {
			MyLog.print(TAG, "onNewCallIn DPFunction.getAlarming() = "
					+ DPFunction.getAlarming());
			DPFunction.toPhoneHangUp();
			DPFunction.isCallAccept = false;
			if (DPFunction.getAlarming() || isAnalogTalking) {
				DPFunction.callHangUp();
				MyLog.print(TAG, "onNewCallIn isAnalogTalking = "
						+ isAnalogTalking);
				return;
			}
			CallInfomation callInfo = DPFunction.isCanCallIn(CallSessionID,
					MsgContent);
			if (callInfo != null) {
				Intent callIn = new Intent();
				if (callInfo.isDoor()) {
					/** 门口机呼入 */
					new Handler().postDelayed(new Runnable() {

						@Override
						public void run() {
							Intent intent = new Intent();
							intent.putExtra("MsgType", MsgType);
							intent.putExtra("SessionID", CallSessionID);
							intent.putExtra("code", DPFunction.getSeeInfo().getRemoteCode());
							intent.setAction(CallReceiver.SEE_ACTION);
							mContext.sendBroadcast(intent);
						}
					}, 800);
					if (mContext == null || DPFunction.callInFromDoorActivity == null) {
						DPFunction.callHangUp();
						return;
					} else {
						if (DPFunction.getCallToPhone()) {
							DPFunction.callPhone();
						}
						callIn.setClass(mContext, DPFunction.callInFromDoorActivity);
					}
				} else {
					/** 管理中心或室内机呼入 */
					callIn.setClass(mContext, DPFunction.callInActivity);
				}
				callIn.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_SINGLE_TOP);
				callIn.putExtra("SessionID", CallSessionID);
				callIn.putExtra("code", MsgContent);
				mContext.startActivity(callIn);
			} else {
				MyLog.print("The line is busy.");
				DPFunction.callHangUp(CallSessionID);
			}
		}

		/**
		 * 对方挂断
		 */
		@Override
		public void onRemoteHangUp(int CallSessionID, int MsgType,
				String MsgContent) {
			MyLog.print("接收到挂断信息");
			DPFunction.isCallAccept = false;
			Intent intent = new Intent();
			intent.putExtra("MsgType", MsgType);
			intent.putExtra("SessionID", CallSessionID);
			CallInfomation info = DPFunction.findCallOut(CallSessionID);
			if (info == null) {
				info = DPFunction.findCallIn(CallSessionID);
				if (info == null) {
					if (CallSessionID == DPFunction.getSeeInfo().getSessionID()) {
						intent.putExtra("code", DPFunction.getSeeInfo()
								.getRemoteCode());
						intent.setAction(CallReceiver.SEE_ACTION);
						mContext.sendBroadcast(intent);
					} else {
						MyLog.print("CallSessionID id is not find.");
					}
				} else {
					DPFunction.toPhoneHangUp();
					intent.putExtra("code", info.getRemoteCode());
					intent.setAction(CallReceiver.CALL_IN_ACTION);
					mContext.sendBroadcast(intent);
				}
			} else {
				intent.putExtra("code", info.getRemoteCode());
				intent.setAction(CallReceiver.CALL_OUT_ACTION);
				mContext.sendBroadcast(intent);
			}
			MyLog.print("RemoteHangUp");
			DPFunction.callHangUp(CallSessionID);
		}

		/**
		 * 对方接听
		 */
		@Override
		public void onRemoteAccept(int CallSessionID, int MsgType,
				String MsgContent) {
			MyLog.print("onRemoteAccept");
			Intent intent = new Intent();
			intent.putExtra("MsgType", MsgType);
			intent.putExtra("SessionID", CallSessionID);
			CallInfomation info = DPFunction.findCallOut(CallSessionID);
			if (info == null) {
				info = DPFunction.findCallIn(CallSessionID);
				if (info == null) {
					if (CallSessionID == DPFunction.getSeeInfo().getSessionID()) {
						DPFunction.getSeeInfo().setAcceptTime();
						DPFunction.getSeeInfo().setType(CallInfomation.SEE_ACCEPT);
						intent.putExtra("code", DPFunction.getSeeInfo()
								.getRemoteCode());
						intent.setAction(CallReceiver.SEE_ACTION);
						mContext.sendBroadcast(intent);
					} else {
						MyLog.print("CallSessionID id is not find.");
					}
				} else {
					intent.putExtra("code", info.getRemoteCode());
					intent.setAction(CallReceiver.CALL_IN_ACTION);
					mContext.sendBroadcast(intent);
				}
			} else {
				intent.putExtra("code", info.getRemoteCode());
				intent.setAction(CallReceiver.CALL_OUT_ACTION);
				mContext.sendBroadcast(intent);
			}
			MyLog.print("RemoteAccept");
			DPFunction.callOutOtherHangUp(CallSessionID);
		}

		/** 对方挂起 */
		@Override
		public void onRemoteHold(int CallSessionID, int MsgType,
				String MsgContent) {
			MyLog.print("onRemoteHold");

		}

		/** 对方唤醒 */
		@Override
		public void onRemoteWake(int CallSessionID, int MsgType,
				String MsgContent) {
			MyLog.print("onRemoteWake");

		}

		/** 会话错误 可能为 sendfail 发送消息失败 */
		@Override
		public void onError(int CallSessionID, int MsgType, String MsgContent) {
			MyLog.print(TAG, "onError CallSessionID = " + CallSessionID);
			/** 停止铃声 */
			Intent intent = new Intent();
			intent.putExtra("MsgType", MsgType);
			intent.putExtra("SessionID", CallSessionID);
			CallInfomation info = DPFunction.findCallOut(CallSessionID);
			if (info == null) {
				info = DPFunction.findCallIn(CallSessionID);
				if (info == null) {
					if (CallSessionID == DPFunction.getSeeInfo().getSessionID()) {
						intent.putExtra("code", DPFunction.getSeeInfo()
								.getRemoteCode());
						intent.setAction(CallReceiver.SEE_ACTION);
						mContext.sendBroadcast(intent);
						DPFunction.seeHangUp();
					} else {
						MyLog.print("CallSessionID id is not find.");
					}
				} else {
					intent.putExtra("code", info.getRemoteCode());
					intent.setAction(CallReceiver.CALL_IN_ACTION);
					mContext.sendBroadcast(intent);
					DPFunction.callHangUp(CallSessionID);
				}
			} else {
				intent.putExtra("code", info.getRemoteCode());
				intent.setAction(CallReceiver.CALL_OUT_ACTION);
				mContext.sendBroadcast(intent);
				DPFunction.callHangUp(CallSessionID);
			}
			MyLog.print("onError");
		}

		@Override
		public void onMessage(int CallSessionID, int MsgType, String MsgContent) {
			MyLog.print("onMessage");
		}

		@Override
		public void onMessageError(int CallSessionID, int MsgType,
				String MsgContent) {
			MyLog.print("onMessageError");
		}
		

		@Override
		public void onPhoneAccept() {
			MyLog.print("onPhoneAccept");
		}

		@Override
		public void onPhoneHangUp() {
			MyLog.print("onPhoneHangUp");
		}
	};
}

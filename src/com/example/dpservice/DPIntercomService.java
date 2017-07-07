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
 * �Խ�����
 */
public class DPIntercomService extends Service {
	private static final String TAG = "DPIntercomService";
	
	/** ģ��С�ſڻ�����ͨ�� */
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
		 * ע��㲥������JNIPhoneClass�㲥�����ĺ�����Ϣ
		 * ���պ������ֳɺ��롢����������3��㲥��ȥ�����ԣ����롢���������ӽ��涼Ҫע����Զ�Ӧ�Ĺ㲥
		 */
		mCallReceiver = new CallReceiver(mPhoneCallback);
		registerReceiver(mCallReceiver, mCallReceiver.getFilter());
		
		/**
		 * ������ʱ����1��1�μ����롢���������ӻỰ�Ƿ�ʱ
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
	 * ���չ㲥���ٷֺ��롢����������3��㲥��ȥ
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
		 * �յ����н�� ����Ϊ busy æ ring ���� hold ���� nomedia ��ý��
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
				/** ����ʧ�� */
				if (CallSessionID == DPFunction.getSeeInfo().getSessionID()) {
					DPFunction.seeHangUp();
					intent.putExtra("code", DPFunction.getSeeInfo()
							.getRemoteCode());
					intent.setAction(CallReceiver.SEE_ACTION);
					mContext.sendBroadcast(intent);
				} else {
					/** ����ռ�� */
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
		 * �º���
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
					/** �ſڻ����� */
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
					/** �������Ļ����ڻ����� */
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
		 * �Է��Ҷ�
		 */
		@Override
		public void onRemoteHangUp(int CallSessionID, int MsgType,
				String MsgContent) {
			MyLog.print("���յ��Ҷ���Ϣ");
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
		 * �Է�����
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

		/** �Է����� */
		@Override
		public void onRemoteHold(int CallSessionID, int MsgType,
				String MsgContent) {
			MyLog.print("onRemoteHold");

		}

		/** �Է����� */
		@Override
		public void onRemoteWake(int CallSessionID, int MsgType,
				String MsgContent) {
			MyLog.print("onRemoteWake");

		}

		/** �Ự���� ����Ϊ sendfail ������Ϣʧ�� */
		@Override
		public void onError(int CallSessionID, int MsgType, String MsgContent) {
			MyLog.print(TAG, "onError CallSessionID = " + CallSessionID);
			/** ֹͣ���� */
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

package com.dpower.dpsiplib.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.pjsip.pjsua2.AccountConfig;
import org.pjsip.pjsua2.AuthCredInfo;
import org.pjsip.pjsua2.AuthCredInfoVector;
import org.pjsip.pjsua2.BuddyConfig;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.OnInstantMessageParam;
import org.pjsip.pjsua2.OnInstantMessageStatusParam;
import org.pjsip.pjsua2.SendInstantMessageParam;
import org.pjsip.pjsua2.VideoWindow;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_role_e;
import org.pjsip.pjsua2.pjsip_status_code;
import org.pjsip.pjsua2.pjsua_stun_use;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.TextUtils;
import android.util.Log;
import com.dpower.cloudintercom.CloudIntercom;
import com.dpower.domain.MessageInfo;
import com.dpower.dpsiplib.callback.MyAppCallback;
import com.dpower.dpsiplib.model.PhoneMessageMod;
import com.dpower.dpsiplib.sipintercom.SIPIntercom;
import com.dpower.dpsiplib.utils.MSG_TYPE;
import com.dpower.dpsiplib.utils.SIPIntercomLog;
import com.dpower.dpsiplib.utils.NetworkUntil;
import com.dpower.pub.dp2700.tools.JniBase64Code;
import com.dpower.util.DPDBHelper;
import com.dpower.util.ReceiverAction;
import com.google.gson.GsonBuilder;

/**
 * SIP������
 * @author LiChao
 *
 */
public class DPSIPService extends Service implements MyAppCallback {
	private static final String TAG = "DPSIPService";
	private static final String LICHAO = "lichao";
	private static final String CHARSET = "UTF-8";
	/**ת��С����Ϣ*/
	private static final String TURN_MSG_PHONE = "05";
	/**PJSIP������Ϣ����*/
	public static int currentMsgType = 0;
	private final String ACCOUNT_CHECK = "";
	private static DPSIPService mInstance = null;
	private WakeLock mWakeLock = null;
	private MyApp mApp = null;
	private static MyAccount mAccount = null;
	private MyCall mOldCall = null;
	private PendingIntent mPendingIntent;
	private AlarmManager mAlarmManager;
	private ScreenChangeReceiver mReceiver;
	private TurnPhoneMsgBroadcastReceiver mTurnPhoneMsgBroadcastReceiver;
	private boolean mIsOnline = false; 
	private boolean mIsFirstLogin = true;
	private boolean mIsAllowLogin = true;
	//private static String mServerUrl = "120.25.126.228";
	private static String mServerUrl = "192.168.10.10";
	
	/** ͨ���б� */
	private List<MyCall> mCalls = null;
	private Handler mHandler;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	public static DPSIPService getInstance() {
		return mInstance;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		SIPIntercomLog.print(TAG, "onCreate");
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_USER_PRESENT);
		mReceiver = new ScreenChangeReceiver();
		registerReceiver(mReceiver, filter);
		IntentFilter phoneSMS = new IntentFilter(MessageInfo.ACTION_MESSAGE);
		mTurnPhoneMsgBroadcastReceiver = new TurnPhoneMsgBroadcastReceiver();
		registerReceiver(mTurnPhoneMsgBroadcastReceiver, phoneSMS);
		startTicker();
		mHandler = new Handler();
		mCalls = new ArrayList<MyCall>();
		mApp = new MyApp();
		mApp.init(DPSIPService.this, getFilesDir().getAbsolutePath(), null);
		mInstance = this;
	}
	
	private void startTicker() {
		if (mPendingIntent == null) {
			mPendingIntent = PendingIntent.getService(this, 0, new Intent(
					"com.action.ticker"), 0);
		}
		if (mAlarmManager == null) {
			mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		}
		mAlarmManager.cancel(mPendingIntent);
		mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis(), 2 * 60000, mPendingIntent);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (SIPIntercom.getSIPCallback() != null) {
			SIPIntercom.getSIPCallback().sipServiceStart();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		SIPIntercomLog.print(TAG, "onDestroy");
		mInstance = null;
		if(mReceiver != null) {
			unregisterReceiver(mReceiver);
			mReceiver = null;
		}
		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
			mHandler = null;
		}
		stopTicker();
		mApp.deinit();
		mApp = null;
		if (SIPIntercom.getSIPCallback() != null) {
			SIPIntercom.getSIPCallback().sipServiceStop();
		}
		super.onDestroy();
	}
	
	private void stopTicker() {
		if (mPendingIntent == null) {
			mPendingIntent = PendingIntent.getService(this, 0, new Intent(
					"com.action.ticker"), 0);
		}
		if (mAlarmManager == null) {
			mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		}
		mAlarmManager.cancel(mPendingIntent);
	}
	
	/**
	 * ��¼
	 * @param username �û���
	 * @param url ��������ַ
	 * @return
	 */
	public boolean login(final String username, final String password, final String url) {
		if (!mIsAllowLogin) {
			return false;
		} else {
			mIsAllowLogin = false;
		}
		if(TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(url)) return false;
		Log.i(LICHAO, "DPSIPService username=" + username);
		Log.i(LICHAO, "DPSIPService password=" + password);
		Log.i(LICHAO, "DPSIPService url=" + url);
		SIPIntercomLog.print("username " + username);
		boolean result = true;
		int netState = NetworkUntil.getNetState(this);
		if (netState == NetworkUntil.CONNECTED_NO) {
			SIPIntercomLog.print("������");
			if (SIPIntercom.getSIPCallback() != null) {
				SIPIntercom.getSIPCallback().noNetworkConnection();
			}
			mIsOnline = false;
			mIsAllowLogin = true;
			return false;
		} else {
			new Thread(new Runnable() {

				@Override
				public void run() {
					if(!ping()){
						SIPIntercomLog.print("���粻ͨ");
						mIsOnline = false;
						mIsAllowLogin = true;
						if(SIPIntercom.getSIPCallback() != null){
							SIPIntercom.getSIPCallback().sipConnectChange(
									false, NetworkUntil.NETWORK_ERROR);
						}
						return;
					}
					if (mHandler != null) {
						mHandler.post(new Runnable() {
							
							@Override
							public void run() {
								mServerUrl = url;
								AccountConfig accCfg = new AccountConfig();
								accCfg.setIdUri("sip:" + ACCOUNT_CHECK + username + "@" + url);
								accCfg.getRegConfig().setRegistrarUri("sip:" + url);
								accCfg.getRegConfig().setTimeoutSec(100);
								accCfg.getRegConfig().setRetryIntervalSec(30);
//							    AccountSipConfig sipCfg = accCfg.getSipConfig();
//							    StringVector proxy = sipCfg.getProxies();
//							    proxy.add("sip:" + ServerIP + ";transport=tcp");
								accCfg.getNatConfig().setIceEnabled(true);
								accCfg.getVideoConfig().setAutoTransmitOutgoing(true);
								accCfg.getVideoConfig().setAutoShowIncoming(true);
								accCfg.getNatConfig().setSipStunUse(pjsua_stun_use.PJSUA_STUN_USE_DEFAULT);
								accCfg.getVideoConfig().setRateControlBandwidth(368000);
								Log.e(LICHAO, "login sip:" + ACCOUNT_CHECK + username + "@" + url);
								if(mAccount != null) {
									mAccount.delacc();
								}
								mAccount = mApp.addAccount(accCfg);
								AuthCredInfoVector creds = accCfg.getSipConfig().getAuthCreds();
								creds.clear();
								creds.add(new AuthCredInfo("Digest", "*", ACCOUNT_CHECK 
										+ username, 0, ACCOUNT_CHECK + password));
								/* Enable ICE */
								accCfg.getNatConfig().setIceEnabled(true);
								/* Finally */
								try {
									SIPIntercomLog.print("connecting to sip server");
									mAccount.modify(accCfg);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						});
					}
				}
			}).start();
		}
		return result;
	}	
	
	/**
    * @category �ж��Ƿ����������ӣ���ͨ���������ж������������Ƿ����ӣ����������Ͼ�������
    * @return
    */ 
   private final boolean ping() {
       boolean result = false; 
       InputStream input = null; 
       BufferedReader reader = null; 
       Process p = null; 
       try { 
           String ip = "8.8.8.8";// ping �ĵ�ַ�����Ի����κ�һ�ֿɿ������� 
           p = Runtime.getRuntime().exec("su -c ping -c 2 -w 100 " + ip);
           // ��ȡping�����ݣ����Բ��� 
           input = p.getInputStream();
           reader = new BufferedReader(new InputStreamReader(input)); 
           StringBuffer stringBuffer = new StringBuffer(); 
           String content = ""; 
           while ((content = reader.readLine()) != null) { 
                   stringBuffer.append(content); 
           } 
           SIPIntercomLog.print("content: " + stringBuffer.toString()); 
           // ping��״̬ 
           int status = p.waitFor(); 
           if (status == 0) { 
               result = true;
           } 
       } catch (IOException e) {
    	   e.printStackTrace();
       } catch (InterruptedException e) { 
    	   e.printStackTrace();
       } finally { 
    	   SIPIntercomLog.print("ping result = " + result); 
    	   try {
    		   if (input != null) {
        		   input.close();
    		   }
    		   if (reader != null) {
    			   reader.close();
    		   }
			} catch (IOException e) {
				e.printStackTrace();
			}
       } 
       return result;
   }
   
   /** �˳���¼ */
   public void logout() {
	   hangup();
	   mIsOnline = false;
	   mIsAllowLogin = true;
	   if (mAccount != null) {
		try {
			mAccount.setRegistration(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	}
	
	/** ������˷�, 0-�ر�, 1-�� */
	public void setMic(int level) {
		try {
			if (mOldCall != null) {
				if (mOldCall.audioMedia != null) {
					mOldCall.audioMedia.adjustRxLevel(level);
					SIPIntercomLog.print(SIPIntercomLog.ERROR, "������˷磺 " + level);
				} else {
					mOldCall.micLevel = level;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** ������Ƶ, 0-����, 1-������ */
	public void setVolume(int level) {
		try {
			if (mOldCall != null) {
				if (mOldCall.audioMedia != null) {
					mOldCall.audioMedia.adjustTxLevel(level);
					SIPIntercomLog.print(SIPIntercomLog.ERROR, "������Ƶ�� " + level);
				} else {
					mOldCall.volumeLevel = level;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** �˺��Ƿ����� */
	public boolean isOnline() {
		return mIsOnline;
	}
	
	/** ��ȡͨ����Ƶ */
	public VideoWindow getVideoWindow() {
		if (mOldCall != null) {
			return mOldCall.videoWindow;
		} else {
			return null;
		}
	}
	
	public List<MyCall> getCallList() {
		return mCalls;
	}

	/**
	 * ����   ��ת�����ֻ��ƶԽ���
	 * @param user �Է����û���
	 * @return
	 */
	public MyCall callOut(String user) {
		MyCall call = new MyCall(mAccount, -1);
		CallOpParam prm = new CallOpParam(true);
		try {
			call.makeCall("sip:" + ACCOUNT_CHECK + user + "@" + mServerUrl, prm);
			Log.i(LICHAO, "DPSIPService callout user=" + user);
		} catch (Exception e) {
			call.delete();
			return null;
		}
		mOldCall = call;
		mCalls.add(call);
		return call;
	}
	
	/** ���� */
	public void accept() {
		if(mOldCall == null)
			return;
		CallOpParam prm = new CallOpParam();
		prm.setStatusCode(pjsip_status_code.PJSIP_SC_OK);
		try {
			mOldCall.answer(prm);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** �Ҷϵ�ǰͨ�� */
	public void hangupForBusy() {
		if (mOldCall != null) {
			for (int i = 0; i < mCalls.size(); i++) {
				MyCall myCall = mCalls.get(i);
				if (myCall.getId() == mOldCall.getId()) {
					mCalls.remove(i);
					i = mCalls.size();
				}
			}
			CallOpParam prm = new CallOpParam();
			prm.setStatusCode(pjsip_status_code.PJSIP_SC_BUSY_HERE);
			try {
				SIPIntercomLog.print(SIPIntercomLog.ERROR, "hangup for busy");
				mOldCall.hangup(prm);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/** �Ҷ�ȫ��ͨ�� */
	public void hangup() {
		if (mCalls.size() > 0) {
			mCalls.clear();
			CallOpParam prm = new CallOpParam();
			prm.setStatusCode(pjsip_status_code.PJSIP_SC_DECLINE);
			try {
				SIPIntercomLog.print(SIPIntercomLog.ERROR, "hangup all");
				MyApp.endpoint.hangupAllCalls();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/** �Ҷ�ָ��ͨ�� */
	public void hangup(MyCall call) {
		if (call != null) {
			for (int i = 0; i < mCalls.size(); i++) {
				MyCall myCall = mCalls.get(i);
				if (myCall.getId() == call.getId()) {
					mCalls.remove(i);
					i = mCalls.size();
				}
			}
			CallOpParam prm = new CallOpParam();
			prm.setStatusCode(pjsip_status_code.PJSIP_SC_DECLINE);
			try {
				SIPIntercomLog.print(SIPIntercomLog.ERROR, "hangup");
				call.hangup(prm);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	// ��¼���ؽ��
	@Override
	public void notifyRegState(pjsip_status_code code, String reason, int expiration) {
		mIsAllowLogin = true;
		if(expiration == 0){
			return;
		}
		try {
			if (code.swigValue() / 100 == 2) {
				SIPIntercomLog.print("SIP ��¼�ɹ�");
				if(mIsFirstLogin)
					mIsFirstLogin = false;
				mIsOnline = true;
				if(SIPIntercom.getSIPCallback() != null){
					SIPIntercom.getSIPCallback().sipConnectChange(true, null);
				}
			} else if ((code.swigValue() == 401 
					|| code.swigValue() == 407) && mIsFirstLogin){
				SIPIntercomLog.print("waiting next code " + code.swigValue());
			} else {
				if(mIsFirstLogin)
					mIsFirstLogin = false;
				SIPIntercomLog.print(SIPIntercomLog.ERROR, "SIP ��¼ʧ��");
				mIsOnline = false;
				hangup();
				if(SIPIntercom.getSIPCallback() != null){
					SIPIntercom.getSIPCallback().sipConnectChange(false, reason);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** �������ڻ�  */
	@Override
	public void notifyIncomingCall(MyCall call) {
		SIPIntercomLog.print("oldCall " + mOldCall + ", newCall=" + call);
		if (mOldCall != null) {
			SIPIntercomLog.print("ͨ���� hangup newCall " + call.getId());
			CallOpParam prm = new CallOpParam();
			prm.setStatusCode(pjsip_status_code.PJSIP_SC_BUSY_HERE);
			 try{
				 call.hangup(prm); 
			 } catch (Exception e) {
				 e.printStackTrace();
			 }
			 call.delete();
			return;
		}
		mOldCall = call;
		mCalls.add(call);
		/* Answer with ringing */
		CallOpParam prm = new CallOpParam();
		prm.setStatusCode(pjsip_status_code.PJSIP_SC_RINGING);
		try {
			call.answer(prm);
		} catch (Exception e) {
			e.printStackTrace();
		}
		CallInfo callInfo;
		try {
			callInfo = call.getInfo();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		int indexStart = callInfo.getRemoteUri().indexOf(":");
		int indexEnd = callInfo.getRemoteUri().indexOf("@");
		Log.i(LICHAO, "notifyIncoming callInfo��" + callInfo.getRemoteUri());
		String user = null;
		if (indexStart > 0 && indexEnd > 0) {
			user = callInfo.getRemoteUri().substring(indexStart + 1, indexEnd);
		}
		//if(user != null && user.length() > 6) {
		if(user != null) {
			//SIPIntercomLog.print("�����˺ţ�" + user.substring(6));
			Log.i(LICHAO, "�����˺ţ�" + user.substring(0));
			if(SIPIntercom.getSIPCallback() != null){
				SIPIntercom.getSIPCallback().newCall(call.getId(), user.substring(0));
			}
		}
	}

	/** ����ͨ����  */
	@Override
	public void notifyCallState(MyCall call) {
		SIPIntercomLog.print("service call state change notifyCallState");
		CallInfo callInfo;
		try {
			callInfo = call.getInfo();
		} catch (Exception e) {
			callInfo = null;
		}
		if (callInfo == null) {
			return;
		}
		//���н������ӳɹ�
		if (callInfo.getState().swigValue() < 
				pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED.swigValue()) {
			if (callInfo.getRole() == pjsip_role_e.PJSIP_ROLE_UAS) {
				SIPIntercomLog.print("new callIn!! " + callInfo.getRemoteContact() + ", "
						+ callInfo.getRemoteUri());
				/* Default button texts are already 'Accept' & 'Reject' */
			} else {
				SIPIntercomLog.print("call state " + callInfo.getStateText());
				Log.e(LICHAO, "DPSIPService call state " + callInfo.getStateText());
				if(SIPIntercom.getSIPCallback() != null){
					SIPIntercom.getSIPCallback().callState(
							call.getId(), callInfo.getStateText());
				}
			}
			
		} else if (callInfo.getState().swigValue() >= 
				pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED.swigValue()) {
			if (callInfo.getState() == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {// �������ӳɹ�
				SIPIntercomLog.print("-------------------------");
				for (int i = 0; i < mCalls.size(); i++) {
					MyCall myCall = mCalls.get(i);
					if (myCall.getId() != call.getId()) {
						hangup(myCall);
						i--;
					}
				}
				if(mOldCall != null && call.getId() != mOldCall.getId())
					mOldCall = call;
				if(SIPIntercom.getSIPCallback() != null){
					Log.e(LICHAO, "calling state " + callInfo.getRemoteUri());
					SIPIntercom.getSIPCallback().callStart(call.getId());
				}
			} else if (callInfo.getState() == 
					pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {// �Է��ѹҶϣ����ߺ������Ӵ���
				SIPIntercomLog.print("state : " + callInfo.getStateText() + ", reason:"
						+ callInfo.getLastReason() + ", " + call.getId());
				for (int i = 0; i < mCalls.size(); i++) {
					MyCall myCall = mCalls.get(i);
					if (myCall.getId() == call.getId()) {
						mCalls.remove(i);
						i = mCalls.size();
						if(SIPIntercom.getSIPCallback() != null){
							SIPIntercom.getSIPCallback().callEnd(
									call.getId(), callInfo.getLastReason());
						}
					}
				}
				if(mOldCall != null && call.getId() == mOldCall.getId())
					mOldCall = null;
			}
		}
	}

	@Override
	public void notifyCallMediaState(MyCall call) {
		SIPIntercomLog.print("media state change notifyCallMediaState");
		if(mOldCall != null) {
			if(mOldCall.getId() != call.getId()) {
				SIPIntercomLog.print(SIPIntercomLog.ERROR, "notifyCallMediaState ERROR");
				return;
			}
		}
		if(SIPIntercom.getSIPCallback() != null){
			SIPIntercom.getSIPCallback().callMediaState(call);
		}
	}
	
	@Override
	public void notifyBuddyState(MyBuddy buddy) {
		
	}
	
	/** ��Ļ�仯�����㲥 */
	private class ScreenChangeReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
				acquireWakeLock();
			} else if(Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
				releaseWakeLock();
			}
		}
	}
	
	/** ��ȡ��Դ�������ָ÷�������ĻϨ��ʱ��Ȼ��ȡCPUʱ����������  */
	private void acquireWakeLock() {
		if (null == mWakeLock) {
			PowerManager pm = (PowerManager) this
					.getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
					| PowerManager.ON_AFTER_RELEASE, "DPSipService");
			if (null != mWakeLock) {
				mWakeLock.acquire();
			}
		}
	}

	// �ͷ��豸��Դ��
	private void releaseWakeLock() {
		if (null != mWakeLock) {
			mWakeLock.release();
			mWakeLock = null;
		}
	}
	
	/**
	 * ������Ϣ
	 * @param number �Է�sip�˺�
	 * @param msgBody ��Ϣ�ı�
	 */
	public static void sendInstantMessage(String number, String msgBody) {
	    String buddy_uri = "<sip:" + number + "@" + mServerUrl + ">";
	    String encode = null;
	    try {
	    	JniBase64Code base = new JniBase64Code();
			byte[] b = base.enBase(msgBody.getBytes(CHARSET));
			encode = new String(b, CHARSET);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
	    
	    BuddyConfig bCfg = new BuddyConfig();
	    bCfg.setUri(buddy_uri);
	    bCfg.setSubscribe(false);

	    MyBuddy myBuddy = new MyBuddy(bCfg);
	    SendInstantMessageParam prm = new SendInstantMessageParam();
	    prm.setContent(encode);

	    try {
	        myBuddy.create(mAccount, bCfg);
	        myBuddy.sendInstantMessage(prm);
	        myBuddy.delete();
	    } catch (Exception e) {
	        e.printStackTrace();
	        return;
	    }
	}
	
	/**
	 * ��ȡ��Ϣ����
	 * @param msg ��װ��Ϣ�ı��Ķ���
	 * @return JSON�ַ���
	 */
	public static String getMsgCommand(PhoneMessageMod msg) {
		String json = new GsonBuilder().disableHtmlEscaping().create().toJson(msg);
		Log.i(LICHAO, "send json:" + json);
		return json;
	}

	/** �����ֻ����͹������ı���Ϣ */
	@Override
	public void notifyMessageFromPhone(OnInstantMessageParam prm) {
		CloudIntercom.MessageToTraitement(prm.getFromUri(), prm.getMsgBody());
	}
	
	/** �����ֻ����͹������ı���Ϣ״̬ */
	@Override
	public void notifyMessageFromPhoneStatus(OnInstantMessageStatusParam prm) {
		Intent intent = new Intent();
		System.out.println("currentMsgType:"+currentMsgType);
		switch (currentMsgType) {
		case MSG_TYPE.MSG_BACK_UNBIND_PHONE_ONE:
			if (prm.getCode().equals(pjsip_status_code.PJSIP_SC_OK)) {
				intent.setAction(ReceiverAction.ACTION_UNBIND_PHONE_ONE_SUCCESS);
			} else {
				intent.setAction(ReceiverAction.ACTION_UNBIND_PHONE_ONE_FAILED);
			}
			sendBroadcast(intent);
			break;
		case MSG_TYPE.MSG_BACK_UNBIND_PHONE_AND:
			if (prm.getCode().equals(pjsip_status_code.PJSIP_SC_OK)) {
				intent.setAction(ReceiverAction.ACTION_UNBIND_PHONE_AND_SUCCESS);
			} else {
				intent.setAction(ReceiverAction.ACTION_UNBIND_PHONE_AND_FAILED);
			}
			sendBroadcast(intent);
			break;
		case MSG_TYPE.MSG_BACK_UNBIND_PHONE_IOS:
			if (prm.getCode().equals(pjsip_status_code.PJSIP_SC_OK)) {
				intent.setAction(ReceiverAction.ACTION_UNBIND_PHONE_IOS_SUCCESS);
			} else {
				intent.setAction(ReceiverAction.ACTION_UNBIND_PHONE_IOS_FAILED);
			}
			sendBroadcast(intent);
			break;
		case MSG_TYPE.MSG_BACK_UNBIND_PHONE_ALL:
			if (prm.getCode().equals(pjsip_status_code.PJSIP_SC_OK)) {
				intent.setAction(ReceiverAction.ACTION_UNBIND_PHONE_ALL_SUCCESS);
			} else {
				intent.setAction(ReceiverAction.ACTION_UNBIND_PHONE_ALL_FAILED);
			}
			sendBroadcast(intent);
			break;
		default:
			break;
		}
	}
	
	/** �յ�С����Ϣ�㲥  */
	private static class TurnPhoneMsgBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			TurnPhoneMsg();
		}		
	}
	
	/**
	 * ����С����Ϣ���ֻ�
	 */
	private static void TurnPhoneMsg() {
		MessageInfo msgInfo = DPDBHelper.queryLasgMessage();
		String title = msgInfo.getTitle().toString();
		String body = msgInfo.getBody().toString();
		String time = msgInfo.getTime().toString();
		boolean personal = msgInfo.isPersonal();
		String msgbody = "title=" + title + "," + "body=" + body + ",time=" + time + ",personal=" + personal;
		List<String> accounts = DPDBHelper.getAccountList();
		for (String account : accounts) {
			Log.i(LICHAO, "sip account:" + account);
			sendInstantMessage(account, getMsgCommand(new PhoneMessageMod(TURN_MSG_PHONE, msgbody,"")));
		}
	}
	
}

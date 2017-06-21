package com.dpower.cloudintercom;

import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.dpower.cloudmessage.CloudMessage;
import com.dpower.cloudmessage.MessageCallback;
import com.dpower.domain.AddrInfo;
import com.dpower.domain.CallInfomation;
import com.dpower.dpsiplib.callback.SIPCallback;
import com.dpower.dpsiplib.model.PhoneMessageMod;
import com.dpower.dpsiplib.service.MyCall;
import com.dpower.dpsiplib.sipintercom.SIPIntercom;
import com.dpower.dpsiplib.utils.JsonParser;
import com.dpower.dpsiplib.utils.NetworkUntil;
import com.dpower.dpsiplib.utils.SIPIntercomLog;
import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.model.IndoorInfoMod;
import com.dpower.pub.dp2700.model.IndoorSipInfo;
import com.dpower.pub.dp2700.tools.JniBase64Code;
import com.dpower.pub.dp2700.tools.MyToast;
import com.google.gson.Gson;
import com.okhttplib.HttpInfo;
import com.okhttplib.OkHttpUtil;
import com.okhttplib.callback.Callback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

/**
 * 云对讲
 */
public class CloudIntercom {
	private static final String TAG = "CloudIntercom";
	private static final String LICHAO = "lichao";

	/** 消息服务器IP */
	private static final String REG_SERVER_IP = "121.43.185.244";

	/** 消息服务器端口 */
	private static final int REG_SERVER_PORT = 15001; // 登陆服务器端口

	/** SIP服务器IP */
	private static final String SIP_SERVER_IP = "192.168.10.10";// QuHwa
	//private static final String SIP_SERVER_IP = "120.25.126.228";
	/** SIP服务器端口 */
	private static final int SIP_SERVER_PORT = 5060; // 登陆服务器端口

	/** 服务器端口 */
	private static final int SERVER_PORT = 8080; // 登陆服务器端口

	/** 服务器IP和端口 */
	public static final String IP_PORT = "http://" + SIP_SERVER_IP + ":"
			+ SERVER_PORT;

	/** 室内机SIP注册 */
	private static final String REG_SIP_URL = IP_PORT
			+ "/smarthomeservice/rest/device/login";

	/** 推送消息给Android */
	private static final String PUSH_AND_URL = IP_PORT
			+ "/smarthomeservice/rest/push/client/pushToAndroidClient";

	/** 推送消息给IOS */
	private static final String PUSH_IOS_URL = IP_PORT
			+ "/smarthomeservice/rest/push/client/pushToIosClient";
	
	/** 上传开门记录到服务器 */
	private static final String UPLOAD_OPEN_DOOR_RECORD = IP_PORT
			+ "/smarthomeservice/rest/unlock/add";
	
	/** 手机开锁 */
	private static final String PHONE_TYPE_UNLOCK = "01";
	/** 绑定手机 */
	private static final String PHONE_TYPE_BIND = "03";
	/** 设置开锁密码 */
	private static final String PHONE_TYPE_SETPWD = "04";
	/** 设置访客开锁密码 */
	private static final String PHONE_TYPE_VISITORPWD = "05";
	/** 手机设置关闭云对讲 */
	private static final String PHONE_CLOUD_CLOSE = "06";
	/** 手机解绑室内机 */
	private static final String PHONE_CLOUD_UNBIND = "07";
	/** Android设备类型 */
	private static final String PUSH_AND_TOKEN = "1";
	/** IOS设备类型 */
	private static final String PUSH_IOS_TOKEN = "2";
	private static final String LAN_NETWORK_CARD_ETH0 = "eth0";
	private static final String LAN_NETWORK_CARD_ETH1 = "eth1";
	private static final String WLAN_NETWORK_CARD = "wlan0";
	private static final String CHARSET = "UTF-8";
	private static final int NEW_CALL = 101;
	private static final int CALL_OUT = 102;
	private static final int HANGUP = 103;
	private static final int LOGIN = 104;
	private static final int SIP_LOGOUT = 105;
	private static SIPHandler mHandler = null;
	private static NetworkBroadcastReceiver mNetworkReceiver = null;
	private static Object mutex = new Object();
	private static boolean mIsEthernet = false;
	private static boolean mCloudMessageOnline = false;
	private static CloudMessage mCloudMessage = null;
	private static IndoorSipInfo sipinfo = null;
	private static CloudIntercomCallback mCallback = null;
	private static String networkCard;
	private static Context mContext = null;
	private static JniBase64Code jniCode;
	private static String mRoomCode;
	private static int count_sip;
	private static String account;
	private static String sipPwd;

	public static void init(Context context, CloudIntercomCallback callback) {
		mCloudMessage = CloudMessage.getInstance(mMessageCallback);
		if (mCloudMessage == null) {
			SIPIntercomLog.print(TAG, "get mCloudMessage instance failed");
			return;
		}
		mContext = context;
		mCallback = callback;
		SIPIntercom.init(mContext, mSipCallback);
		mHandler = new SIPHandler();
		if (mIsEthernet) {
			networkCard = LAN_NETWORK_CARD_ETH1;
		} else {
			networkCard = WLAN_NETWORK_CARD;
		}
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		mNetworkReceiver = new NetworkBroadcastReceiver();
		mContext.registerReceiver(mNetworkReceiver, filter);		
	}

	public static void deinit() {
		CloudMessage.deinit();
		SIPIntercom.deinit();
		mIsEthernet = false;
		mCloudMessageOnline = false;
		mCloudMessage = null;
		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
			mHandler = null;
		}
		mCallback = null;
		if (mNetworkReceiver != null) {
			mContext.unregisterReceiver(mNetworkReceiver);
			mNetworkReceiver = null;
		}
		mContext = null;
	}

	private static class SIPHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case NEW_CALL:
				if (mCallback.getCallInSize() == 0
						&& mCallback.getCallOutSize() == 0) {
					SIPIntercom.setMic(0);
					SIPIntercom.accept();
				} else {
					SIPIntercom.hangupForBusy();
				}
				break;
			case CALL_OUT:
				if (!(mCallback.getTokensCount(PUSH_AND_TOKEN) == 0)) {
					poushToAnd("访客来电");
				}
				if (!(mCallback.getTokensCount(PUSH_IOS_TOKEN) == 0)) {
					poushToIos("访客来电");
				}
				List<String> accounts = mCallback.getAccountList();
				for (String account : accounts) {
					MyCall call = SIPIntercom.callOut(account);
					Log.i(LICHAO, "CloudIntercom callout accounts:" + account
							+ ", callID " + call.getId());
					SIPIntercomLog.print("call phone " + account + ", callID "
							+ call.getId());
				}
				break;
			case HANGUP:
				SIPIntercom.hangup();
				break;
			case LOGIN:
				if (!SIPIntercom.isOnline()) {
					startLogin();
				}
				break;
			case SIP_LOGOUT:
				if (SIPIntercom.isOnline()) {
					SIPIntercom.logout();
				}
				break;
			}
		}
	}

	private static SIPCallback mSipCallback = new SIPCallback() {

		@Override
		public void newCall(int sessionID, String remoteAccount) {
			SIPIntercomLog.print(TAG, "新来电");
			SIPIntercomLog.print(TAG, "sessionID " + sessionID);
			Log.i(LICHAO, "CloudIntercom newCall" + "sessionID:" + sessionID);
			mHandler.sendEmptyMessage(NEW_CALL);
		}

		@Override
		public void callStart(int sessionID) {
			SIPIntercomLog.print(TAG, "通话开始 sessionID = " + sessionID);
			Log.i(LICHAO, "CloudIntercom callStart" + "sessionID:" + sessionID);
			mCallback.accept();
		}

		@Override
		public void callEnd(int sessionID, String reason) {
			SIPIntercomLog.print(TAG, "通话结束 sessionID = " + sessionID);
			Log.i(LICHAO, "CloudIntercom callEnd" + "sessionID:" + sessionID);
			if (reason != null) {
				SIPIntercomLog.print(SIPIntercomLog.ERROR, TAG, reason);
				Log.e(LICHAO, "CloudIntercom callEnd reason " + reason);
			}
			mCallback.hangUp(sessionID);
		}

		@Override
		public void callState(int sessionID, String state) {
			SIPIntercomLog.print(TAG, "callState sessionID " + sessionID
					+ " state " + state);
		}

		@Override
		public void callMediaState(MyCall call) {

		}

		@Override
		public void sipConnectChange(boolean isConnected, String reason) {
			SIPIntercomLog.print(TAG, "SIP登录状态改变");
			if (reason != null) {
				SIPIntercomLog.print(SIPIntercomLog.ERROR, TAG, reason);
			}
			if (reason != null && reason.equals(NetworkUntil.NETWORK_ERROR)) {
				mHandler.sendEmptyMessageDelayed(LOGIN, 10000);
			} else {
				mHandler.sendEmptyMessage(LOGIN);
			}
			mCallback.sipConnectChange(isConnected, reason);
		}

		@Override
		public void sipServiceStart() {
			SIPIntercomLog.print(SIPIntercomLog.ERROR, "服务开启");
			mHandler.sendEmptyMessage(LOGIN);
		}

		@Override
		public void sipServiceStop() {
			SIPIntercomLog.print(SIPIntercomLog.ERROR, TAG, "sipServiceStop");
		}

		@Override
		public void noNetworkConnection() {

		}
	};

	/** 手机类型过滤 */
	private static String getTokens(String type) {
		String tokens = "";
		List<String> dbtoken = mCallback.getTokenByTypeList(type);
		for (String token : dbtoken) {
			if (token != null) {
				tokens += token + ",";
			}
		}
		return tokens;
	}

	/** 推送给IOS */
	public static void poushToIos(String tokenmsg) {
		final HashMap<String, String> maps = new HashMap<String, String>();
		String tokenStr = getTokens(PUSH_IOS_TOKEN);
		String token = null;
		if (!tokenStr.equals("")) {
			token = tokenStr.substring(0, tokenStr.length() - 1);
		}
		Log.e(LICHAO, "IosTokens=" + token);
		maps.put("content", tokenmsg);
		maps.put("tokens", token);
		OkHttpUtil.getDefault()
				.doPostAsync(
						HttpInfo.Builder().setUrl(PUSH_IOS_URL).addParams(maps)
								.build(), new Callback() {

							@Override
							public void onSuccess(HttpInfo info)
									throws IOException {
								String result = info.getRetDetail();
								Log.i(LICHAO, "IOS请求成功：" + result);
							}

							@Override
							public void onFailure(HttpInfo info)
									throws IOException {
								String result = info.getRetDetail();
								Log.e(LICHAO, "IOStuis失败：" + result);
							}
						});
	}

	/** 推送给Android */
	public static void poushToAnd(String tokenmsg) {
		final HashMap<String, String> maps = new HashMap<String, String>();
		String tokenStr = getTokens(PUSH_AND_TOKEN);
		String token = null;
		if (!tokenStr.equals("")) {
			token = tokenStr.substring(0, tokenStr.length() - 1);
		}
		Log.e(LICHAO, "AndTokens=" + token);
		maps.put("content", tokenmsg);
		maps.put("tokens", token);
		OkHttpUtil.getDefault()
				.doPostAsync(
						HttpInfo.Builder().setUrl(PUSH_AND_URL).addParams(maps)
								.build(), new Callback() {

							@Override
							public void onSuccess(HttpInfo info)
									throws IOException {
								String result = info.getRetDetail();
								Log.i(LICHAO, "Android请求成功：" + result);
							}

							@Override
							public void onFailure(HttpInfo info)
									throws IOException {
								String result = info.getRetDetail();
								Log.e(LICHAO, "Android请求失败：" + result);
							}
						});
	}

	/** 注册室内机SIP账号 */
	private static void registerIndoor() {
		final HashMap<String, String> maps = new HashMap<String, String>();
		String roomNum = mCallback.getRoomCode();
		String mac = getMacAddress();
		StringBuilder  sb = new StringBuilder (mac);  
		sb.insert(10, ":");  
		sb.insert(8, ":");
		sb.insert(6, ":");                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
		sb.insert(4, ":");  
		sb.insert(2, ":");  
		String macStrNew = sb.toString();  
		AddrInfo info = DPFunction.getAddrInfo(DPFunction.getRoomCode());
		maps.put("deviceName", roomNum);
		maps.put("deviceType", "01");
		maps.put("device_status", "1");
		maps.put("mac", macStrNew);
		maps.put("regionNo", roomNum.substring(1, 3));
		maps.put("areaNo", "1001");
		maps.put("ip", info.getIp());
		maps.put("buildingNo", roomNum.substring(3, 5));
		maps.put("unitNo", roomNum.substring(5, 7));
		maps.put("houseNo", roomNum.substring(7, 11));
		OkHttpUtil.getDefault().doPostAsync(
				HttpInfo.Builder().setUrl(REG_SIP_URL).addParams(maps).build(),
				new Callback() {

					@Override
					public void onSuccess(HttpInfo info) throws IOException {
						String result = info.getRetDetail();
						SIPIntercomLog.print("regiter success:" + result);
						IndoorInfoMod sipInfoMod = new Gson().fromJson(result, IndoorInfoMod.class);
						sipinfo = sipInfoMod.getData();
						count_sip = mCallback.countIndoorSip();
						if (count_sip == 0) {
							mCallback.addIndoorSip(sipinfo); 
						}
						if (!mCallback.isIndoorSipExist(sipinfo.getSipId())) {
							mCallback.modifyIndoorSip(sipinfo);
						}
					}

					@Override
					public void onFailure(HttpInfo info) throws IOException {
						String result = info.getRetDetail();
						String responseStr = info.getUrl().toString();
						SIPIntercomLog.print("regiter failure:" + result + "URL:" + responseStr);
					}
				});
	}

	/**
	 * 上传开门记录到服务器
	 * @param sip
	 */
	public static void uploadOpenDoorRecord(String sip){
		final HashMap<String, String> maps = new HashMap<String, String>();
		String deviceNo = mCallback.queryFistSip().getDeviceNo();
		String roomNo = mCallback.queryFistSip().getRegionNo();
		maps.put("deviceNo", deviceNo);
		maps.put("roomNo", roomNo);
		maps.put("type", "1");
		maps.put("fromSipid", sip);
		OkHttpUtil.getDefault().doPostAsync(
				HttpInfo.Builder().setUrl(UPLOAD_OPEN_DOOR_RECORD).addParams(maps).build(),
				new Callback() {
					
					@Override
					public void onSuccess(HttpInfo info) throws IOException {
						String result = info.getRetDetail();
						SIPIntercomLog.print("Door Open Record Success:" + result);
					}
					
					@Override
					public void onFailure(HttpInfo info) throws IOException {
						String result = info.getRetDetail();
						SIPIntercomLog.print("Door Open Record Failuer:" + result);
					}
				});
	}

	/** 登录到服务器 */
	private static boolean startLogin() {
		synchronized (mutex) {
			if (getMacAddress() != null
					&& NetworkUntil.getLanConnectState(networkCard)) {
				if (!SIPIntercom.isOnline() || !mCloudMessageOnline) {
					SIPIntercomLog.print(SIPIntercomLog.ERROR, "SIP = "
							+ SIPIntercom.isOnline() + ", CloudMessage = "
							+ mCloudMessageOnline);
					boolean ret = false;
					count_sip = mCallback.countIndoorSip();
					if (count_sip == 0) {
						registerIndoor();
						account = getSipAcount();
						sipPwd = getSipPwd();
					} else if (count_sip == 1) {
						account = mCallback.queryFistSip().getSipId().toString();
						sipPwd = mCallback.queryFistSip().getSipPwd().toString();
					}
					if (TextUtils.isEmpty(account))
						return false;
					SIPIntercomLog.print("start login =" + account);
					if (!SIPIntercom.isOnline()) {// 登录SIP
						ret = SIPIntercom.login(account, sipPwd, SIP_SERVER_IP
								+ ":" + SIP_SERVER_PORT);
					}
					if (mCloudMessageOnline) {// 登录CloudMessage-------!mCloudMessageOnline
						new Thread(new Runnable() {

							@Override
							public void run() {
								// 不用CloudMessage发送文本消息
								mCloudMessage.DPLogin(account, REG_SERVER_IP,
										REG_SERVER_PORT);
							}
						}).start();
					}
					return ret;
				}
			}
			return false;
		}
	}

	/**
	 * 获取"eth0"的MAC地址，如果开机时绑定网卡失败则会获取null
	 */
	private static String getMacAddress() {
		String strMacAddr = "";
		byte[] b;
		try {
			NetworkInterface NIC = NetworkInterface
					.getByName(LAN_NETWORK_CARD_ETH0);
			b = NIC.getHardwareAddress();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < b.length; i++) {
				String str = Integer.toHexString(b[i] & 0xFF);
				buffer.append(str.length() == 1 ? 0 + str : str);
			}
			strMacAddr = buffer.toString().toUpperCase();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return strMacAddr;
	}

	private static MessageCallback mMessageCallback = new MessageCallback() {

		@Override
		public void loginResult(int loginStatus, String reason) {
			SIPIntercomLog.print(TAG, "CloudMessage login " + loginStatus);
			mCloudMessageOnline = (loginStatus == 0) ? true : false;
			if (reason != null) {
				SIPIntercomLog.print(SIPIntercomLog.ERROR, TAG, reason);
			}
			if (mCloudMessageOnline) {
				String msg = "{\"cmd\":\"getbind\"}";
				sendMessage(msg, null);
			}
			mCallback.loginResult(loginStatus, reason);
		}

		@Override
		public void receivedMessage(String msg) {
			SIPIntercomLog.print("receivedMessage " + msg);
			JSONObject json = null;
			try {
				json = new JSONObject(msg);
			} catch (JSONException e) {
				e.printStackTrace();
				return;
			}
			String cmd = null, tel = null;
			try {
				cmd = json.getString("cmd");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (!TextUtils.isEmpty(cmd)) {
				if (cmd.equals("bindTel")) {
					try {
						tel = json.getString("tel");
					} catch (JSONException e) {
						e.printStackTrace();
					}
					int result = mCallback.addAccount(tel, null, null);
					if (result == 0) {
						sendMessage(
								"{\"cmd\":\"bindTelAck\", \"r\":\"ok\", \"tel\":\""
										+ tel + "\"}", null);
						mCallback.onBindTel(tel, true);
					} else if (result == -1) {
						sendMessage(
								"{\"cmd\":\"bindTelAck\", \"r\":\"fail\", \"reason\":\"account max\", \"tel\":\""
										+ tel + "\"}", null);
					} else {
						sendMessage(
								"{\"cmd\":\"bindTelAck\", \"r\":\"fail\", \"reason\":\"account exist\", \"tel\":\""
										+ tel + "\"}", null);
					}
					SIPIntercomLog.print("绑定手机号： " + tel);
				} else if (cmd.equals("disbindTel")) {
					try {
						tel = json.getString("tel");
					} catch (JSONException e) {
						e.printStackTrace();
					}
					mCallback.deleteAccount(tel);
					sendMessage(
							"{\"cmd\":\"disbindTelAck\", \"r\":\"ok\", \"tel\":\""
									+ tel + "\"}", null);
					mCallback.onBindTel(tel, false);
				} else if (cmd.equals("getbindAck")) {
					try {
						tel = json.getString("tel");
					} catch (JSONException e1) {
						e1.printStackTrace();
					}
					if (tel != null) {
						try {
							JSONArray jsonArray = new JSONArray(tel);
							if (jsonArray != null) {
								mCallback.clearAccount();
								Log.e(LICHAO, "===CloudIntercome clearAccount===");
								for (int i = 0; i < jsonArray.length(); i++) {
									String telItem = jsonArray.get(i)
											.toString();
									if (telItem != null)
										mCallback.addAccount(telItem, null,
												null);
								}
								return;
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				} else if (cmd.equals("msg")) {
					try {
						String strFu = json.getString("fu");
						String content = json.getString("b");
						JSONObject jsonContent = new JSONObject(content);
						if (jsonContent != null) {
							String ct = jsonContent.getString("ct");
							if (ct != null) {
								if (ct.equals("lock")) { // 开锁
									openLock(strFu);
								} else if (ct.equals("gmm")) { // 获取智能家居模式
									getSmartHomeMode(strFu);
								} else if (ct.equals("smm")) { // 设置智能家居模式
									setSmartHomeMode(strFu,
											jsonContent.getString("mode"));
								} else if (ct.equals("gsm")) { // 获取当前安防状态
									getSafeMode(strFu);
								} else if (ct.equals("ssm")) { // 设置安防状态
									setSafeMode(strFu,
											jsonContent.getString("mode"));
								} else if (ct.equals("gsl")) { // 获取安防日志
									getAlarmLog(strFu);
								} else if (ct.equals("gmsgl")) { // 获取小区信息列表
									getMessageLog(strFu);
								} else if (ct.equals("gmsg")) { // 获取指定小区信息
									getMessageFileData(strFu,
											jsonContent.getInt("id"));
								} else if (ct.equals("gvl")) { // 获取留影留言列表
									getVisitRecordList(strFu);
								} else if (ct.equals("gv")) { // 获取指定留影留言信息
									getVisitFileData(strFu,
											jsonContent.getInt("id"));
								}
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		}
	};

	private static void sendMessage(final String json, final String data) {
		if (mCloudMessage != null && mCloudMessageOnline) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					mCloudMessage.DPSendMessage(json, data, 0);
				}
			}).start();
		}
	}

	/** 开锁 */
	private static void openLock(String remoteUser) {
		boolean result = mCallback.openLock();
		JSONObject json = new JSONObject();
		try {
			json.put("ct", "locka");
			if (result) {
				json.put("st", "true");
			} else {
				json.put("st", "false");
			}
			Log.i(LICHAO, "CloudIntercom openLock remoteUser:" + remoteUser
					+ ":" + json.toString());
			sendMessage(getAccount(), remoteUser, json.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private static void sendMessage(final String localUser,
			final String remoteUser, final String content) {
		if (mCloudMessage != null && mCloudMessageOnline) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					Log.e(LICHAO, "CloudIntercom sendMessage:" + content);
					mCloudMessage.DPSendMessage(localUser, remoteUser, content);
				}
			}).start();
		}
	}

	/** 获取智能家居模式 */
	private static void getSmartHomeMode(String remoteUser) {
		JSONObject json = new JSONObject();
		try {
			json.put("ct", "gmma");
			json.put("mode", mCallback.getSmartHomeMode());
			sendMessage(getAccount(), remoteUser, json.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/** 设置智能家居模式 */
	private static void setSmartHomeMode(String remoteUser, String mode) {
		int model = 1;
		if (mode.equals("Home")) {
			model = 1;
		} else if (mode.equals("Night")) {
			model = 2;
		} else if (mode.equals("Dining")) {
			model = 3;
		} else if (mode.equals("Video")) {
			model = 4;
		} else if (mode.equals("Disco")) {
			model = 5;
		} else if (mode.equals("AllClose")) {
			model = 6;
		}
		mCallback.changeSmartHomeMode(model);
	}

	/** 获取安防模式 */
	private static void getSafeMode(String remoteUser) {
		JSONObject json = new JSONObject();
		try {
			json.put("ct", "gsma");
			json.put("mode", mCallback.getSafeMode());
			sendMessage(getAccount(), remoteUser, json.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/** 设置安防模式 */
	private static void setSafeMode(String remoteUser, String mode) {
		int model = 1;
		if (mode.equals("UnSafe")) {
			model = 1;
		} else if (mode.equals("Night")) {
			model = 2;
		} else if (mode.equals("Home")) {
			model = 3;
		} else if (mode.equals("Leave")) {
			model = 4;
		}
		mCallback.changeSafeMode(model, false);
	}

	/** 获取报警记录 */
	private static void getAlarmLog(String remoteUser) {
		String alarmLog = mCallback.getAlarmLog();
		if (alarmLog != null) {
			sendMessage(getAccount(), remoteUser, alarmLog);
		}
	}

	/** 获取小区信息列表 */
	private static void getMessageLog(String remoteUser) {
		String messageLog = mCallback.getMessageLog();
		if (messageLog != null) {
			sendMessage(getAccount(), remoteUser, messageLog);
		}
	}

	/** 获取指定小区信息 */
	private static void getMessageFileData(String remoteUser, int id) {
		String[] messageFile = mCallback.getMessageFileData(id);
		if (messageFile != null) {
			sendMessage(getAccount(), remoteUser, messageFile[0],
					messageFile[1]);
		}
	}

	private static void sendMessage(final String localUser,
			final String remoteUser, final String content, final String data) {
		if (mCloudMessage != null && mCloudMessageOnline) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					mCloudMessage.DPSendMessage(localUser, remoteUser, content,
							data);
				}
			}).start();
		}
	}

	/** 获取留影留言列表 */
	private static void getVisitRecordList(String remoteUser) {
		String visitRecord = mCallback.getVisitRecord();
		if (visitRecord != null) {
			sendMessage(getAccount(), remoteUser, visitRecord);
		}
	}

	/** 获取指定留影留言信息 */
	private static void getVisitFileData(String remoteUser, int id) {
		String visitFile = mCallback.getVisitFileData(id);
		if (visitFile != null) {
			JSONObject json = new JSONObject();
			try {
				json.put("ct", "gva");
				json.put("id", id);
				sendMessage(getAccount(), remoteUser, json.toString(),
						visitFile);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 通知已登录的手机,有新的报警
	 * 
	 * @param alarmArea
	 *            报警防区
	 * @param alarmType
	 *            报警类型
	 */
	public static void toPhoneAlarm(int alarmArea, int alarmType, String time) {
		if (mCallback == null || !mCloudMessageOnline) {
			return;
		}
		SIPIntercomLog.print("toPhoneAlarm " + alarmType);
		JSONObject jsonPlace = new JSONObject();
		try {
			jsonPlace.put("area", alarmArea);
			jsonPlace.put("type", alarmType);
			jsonPlace.put("time", time);
			JSONObject json = new JSONObject();
			json.put("ct", "alarm");
			json.put("place", jsonPlace.toString());
			sendMessageToAll(getAccount(), json.toString(),
					mCallback.getAccountList());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private static void sendMessageToAll(final String localUser,
			final String content, final List<String> remoteUserList) {
		if (mCloudMessage != null && mCloudMessageOnline) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					mCloudMessage.DPSendMessageToAll(localUser, content,
							remoteUserList);
				}
			}).start();
		}
	}

	/**
	 * 同步安防模式到手机
	 * 
	 * @param safeMode
	 *            UnSafe, Night, Home, Leave
	 */
	public static void synchPhoneSafeMode(String safeMode) {
		if (mCallback == null || !mCloudMessageOnline) {
			return;
		}
		JSONObject jsonMode = new JSONObject();
		try {
			jsonMode.put("ct", "synsm");
			jsonMode.put("mode", safeMode);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		sendMessageToAll(getAccount(), jsonMode.toString(),
				mCallback.getAccountList());
	}

	/**
	 * 同步智能家居模式到手机
	 * 
	 * @param smartHomeMode
	 *            Home, Night, Dining, Video, Disco, AllClose
	 */
	public static void synchPhoneSmartHomeMode(String smartHomeMode) {
		if (mCallback == null || !mCloudMessageOnline) {
			return;
		}
		JSONObject jsonMode = new JSONObject();
		try {
			jsonMode.put("ct", "synmm");
			jsonMode.put("mode", smartHomeMode);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		sendMessageToAll(getAccount(), jsonMode.toString(),
				mCallback.getAccountList());
	}

	/** 挂断手机 */
	public static void toPhoneHangUp() {
		if (mCallback == null) {
			return;
		}
		mHandler.sendEmptyMessage(HANGUP);
	}

	/** 呼叫所有手机 */
	public static boolean callPhone() {
		if (mCallback == null || !SIPIntercom.isOnline()) {
			return false;
		}
		mHandler.sendEmptyMessage(CALL_OUT);
		return true;
	}

	/**
	 * @功能：通知已登录的手机，收到了新信息
	 * @参数1：boolean isPersonal: true-个人信息 false-公共信息
	 * @参数2：收到新信息的条数
	 */
	public static void toPhoneNewMsg(boolean isPersonal, int count) {
		if (mCallback == null || !mCloudMessageOnline) {
			return;
		}
		JSONObject json = new JSONObject();
		try {
			json.put("ct", "newMsg");
			json.put("count", count);
			json.put("area", isPersonal ? 1 : 0);
			sendMessageToAll(getAccount(), json.toString(),
					mCallback.getAccountList());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}

	/** 登录到服务器 */
	public static void toStartLogin() {
		if (mCallback == null) {
			return;
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				if (mCloudMessageOnline) {
					mCloudMessageOnline = false;
					mCloudMessage.DPUnLogin();
				}
				if (SIPIntercom.isOnline()) {
					mHandler.sendEmptyMessage(SIP_LOGOUT);
				}
				SIPIntercomLog.print(SIPIntercomLog.ERROR, "修改房号，重新登录");
				mHandler.sendEmptyMessage(LOGIN);
			}
		}).start();
	}

	/** 室内机是否在线 */
	public static boolean isOnline() {
		if (mCallback == null) {
			return false;
		}
		SIPIntercomLog.print(SIPIntercomLog.ERROR,
				"SIP = " + SIPIntercom.isOnline() + ",CloudMessage = "
						+ mCloudMessageOnline);
		return SIPIntercom.isOnline() && mCloudMessageOnline;
	}

	/** SIP是否在线 */
	public static boolean sipIsOnline() {
		if (mCallback == null) {
			return false;
		}
		return SIPIntercom.isOnline();
	}

	/** MSG是否在线 */
	public static boolean msgIsOnline() {
		if (mCallback == null) {
			return false;
		}
		return mCloudMessageOnline;
	}

	/**
	 * 用来显示到二维码上
	 */
	public static void getQRString(final Handler handler) {
		
		new Thread(){
			public void run() {
				String sipId = null;
				if(mCallback.countIndoorSip() == 0) {
					sipId = "DISABLED";
				} else {
					sipId = mCallback.queryFistSip().getSipId().toString();
				}
				String mac = getMacAddress();
				String qr = mac + "_" + mCallback.getRoomCode();
				String myqr = "QUHWA_" + qr + "_" + sipId;
				SIPIntercomLog.print("getQRAccount: " + myqr);
				if (!myqr.equals("") && myqr.length() > 0 && !sipId.equals("DISABLED")) {
					Message msg = handler.obtainMessage(0);
					msg.obj = myqr;
					handler.sendMessage(msg);
				} else if (sipId.equals("DISABLED")) {
					Message msg = handler.obtainMessage(1);
					msg.obj = "DISABLED";
					handler.sendMessage(msg);
				}
			};
		}.start();
		
	}
	
	/**
	 * 获取二维码
	 * @return
	 */
	public static String getQRString(){
		if (mCallback == null) {
			return null;
		}
		String sipId = mCallback.queryFistSip().getSipId().toString();
		String mac = getMacAddress();
		String qr = mac + "_" + mCallback.getRoomCode();
		String myqr = "QUHWA_" + qr + "_" + sipId;
		return myqr;
	}

	/**
	 * 获取室内机的帐号
	 */
	public static String getAccount() {
		if (mCallback == null) {
			return null;
		}
		String mac = getMacAddress();
		String account = mac + mCallback.getRoomCode();
		SIPIntercomLog.print("getAccount " + account);
		return account;
	}

	/**
	 * 获取室内机SIP账号
	 * 
	 * @return
	 */
	public static String getSipAcount() {
		if (mCallback == null) {
			return null;
		}
		if (sipinfo != null) {
			String sipaccount = sipinfo.getSipId().toString();
			return sipaccount;
		} 
		return null;
	}

	/**
	 * 获取室内机SIP密码
	 * 
	 * @return
	 */
	public static String getSipPwd() {
		if (mCallback == null) {
			return null;
		}
		if (sipinfo != null) {
			String sipPwd = sipinfo.getSipPwd().toString();
			return sipPwd;
		}
		return null;
	}

	/**
	 * 设置网卡（根据设置的网卡的网络状态来进行登录和退出登录，默认是无线网卡）
	 * 
	 * @param isEthernet
	 *            true-以太网"eth1", false-无线
	 */
	public static void setEthernet(boolean isEthernet) {
		mIsEthernet = isEthernet;
		if (mIsEthernet) {
			networkCard = LAN_NETWORK_CARD_ETH1;
		} else {
			networkCard = WLAN_NETWORK_CARD;
		}
	}

	private static class NetworkBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			SIPIntercomLog.print(SIPIntercomLog.ERROR, "网络改变");
			if (!NetworkUntil.getLanConnectState(networkCard)) {
				SIPIntercomLog.print(SIPIntercomLog.ERROR, networkCard + " 网络关闭");
				new Thread(new Runnable() {

					@Override
					public void run() {
						if (mCloudMessageOnline) {
							mCloudMessageOnline = false;
							mCloudMessage.DPUnLogin();
						}
						if (SIPIntercom.isOnline()) {
							mHandler.sendEmptyMessage(SIP_LOGOUT);
						}
					}
				}).start();
			} else {
				SIPIntercomLog.print(SIPIntercomLog.ERROR, networkCard + " 网络开启");
				mHandler.sendEmptyMessage(LOGIN);
			}
		}
	}

	/**
	 * 处理接收到的手机信息
	 * 
	 * @param fromuri
	 *            手机的sip账号
	 * @param msgbody
	 *            接收到手机发来的信息
	 */
	public static void MessageToTraitement(final String fromuri,
			final String msgbody) {
		Log.i(LICHAO, "fromuri:" + fromuri + " msgbody:" + msgbody);
		try {
			String sip_no = fromuri.substring(5, fromuri.length() - 1);
			String sip[] = sip_no.split("@");
			String isdecode_str = null;
			jniCode = new JniBase64Code();
			byte[] b_decode = jniCode.deBase(msgbody.getBytes(CHARSET));
			isdecode_str = new String(b_decode, CHARSET);
			Log.i(LICHAO, "isdecode_str:" + isdecode_str);
			JsonParser gson = new JsonParser();
			PhoneMessageMod phoneMsg = gson.getObject(isdecode_str, PhoneMessageMod.class);
			if (phoneMsg.getType().equals(PHONE_TYPE_UNLOCK)) {// 手机开锁
				CallInfomation info = mCallback.quaryLastCall();
				mRoomCode = info.getRemoteCode().toString();
				OpenLockMessage(mRoomCode, sip[0]);
				
			} else if (phoneMsg.getType().equals(PHONE_TYPE_BIND)) {// 绑定手机
				Map<String, String> map = new HashMap<String, String>();
				map = gson.getMapFromString(phoneMsg.getMsg().toString());
				BindPhoneMessage(map.get("sipId"), map.get("token"), map.get("mobileType"));
				
			} else if (phoneMsg.getType().equals(PHONE_TYPE_SETPWD)) {// 设置开锁密码
				SetDoorPassword(phoneMsg.getMsg().toString(), sip[0]);
				
			} else if (phoneMsg.getType().equals(PHONE_TYPE_VISITORPWD)) {// 设置访客密码
				SetVisitorPassword(phoneMsg.getMsg().toString(), sip[0]);
				
			} else if (phoneMsg.getType().equals(PHONE_CLOUD_CLOSE)) {//手机屏蔽云对讲
				SetCloudClose(phoneMsg.getMsg().toString(), phoneMsg.getStatus().toString());
				
			} else if (phoneMsg.getType().equals(PHONE_CLOUD_UNBIND)) {//手机解绑室内机
				Map<String, String> map = new HashMap<String, String>();
				map = gson.getMapFromString(phoneMsg.getMsg().toString());
				UnBindDevice(map.get("sipId"), map.get("token"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 呼叫开门消息处理
	 * 
	 * @param roomCode
	 *            门口机号
	 * @param phoneSip
	 *            手机sip账号
	 * @param status
	 *            开锁类型
	 */
	private static void OpenLockMessage(String roomCode, String phoneSip) {
		DPFunction.phoneopenlock(mRoomCode);
		uploadOpenDoorRecord(phoneSip);
	}

	/**
	 * 绑定手机
	 * 
	 * @param phoneSip
	 *            SIP账号
	 * @param token
	 *            推送token
	 * @param mobiletype
	 *            手机类型 1为Android 2为IOS
	 */
	private static void BindPhoneMessage(String phoneSip, String token, String mobiletype) {
		int add_result = mCallback.addAccount(phoneSip, token, mobiletype);
		if (add_result == 0) {
			Log.i(LICHAO, "account success:" + phoneSip);
		} else if (add_result == -2) {
			Log.i(LICHAO, "account isexist:" + phoneSip);
		} else if (add_result == -1) {
			Log.i(LICHAO, "account is max num,cann't add");
		}
	}

	/**
	 * 设置开门密码
	 * 
	 * @param newpassword
	 * @param sipaccount
	 */
	private static void SetDoorPassword(String newpassword, String sipaccount) {
		ArrayList<AddrInfo> monitorLists = DPFunction.getCellSeeList();
		String doorIpAddr = monitorLists.get(0).getIp();
		Log.e(LICHAO, "doorIpAddr=" + doorIpAddr);
		int result = DPFunction.toDoorModifyPassWord(doorIpAddr, newpassword);
		if (result == 0) {
			if (!(mCallback.getTokensCount(PUSH_AND_TOKEN) == 0)) {
				CloudIntercom.poushToAnd("您的开锁密码已修改");
			}
			if (!(mCallback.getTokensCount(PUSH_IOS_TOKEN) == 0)) {
				CloudIntercom.poushToIos("您的开锁密码已修改");
			}
			Log.i(LICHAO, "DoorPassword success:" + newpassword);
			MyToast.show(sipaccount + R.string.change_succeeded);
		} else {
			Log.i(LICHAO, "DoorPassword fail");
			MyToast.show(R.string.change_failed);
		}
	}

	/**
	 * 设置访客密码
	 * 
	 * @param newpassword
	 * @param sipaccount
	 */
	private static void SetVisitorPassword(String newpassword, String sipaccount) {
		ArrayList<AddrInfo> monitorLists = DPFunction.getCellSeeList();
		String doorIpAddr = monitorLists.get(0).getIp();
		Log.e(LICHAO, "doorIpAddr=" + doorIpAddr);
		int result = DPFunction.toDoorModifyPassWord(doorIpAddr, newpassword + ",600");
		if (result == 0) {
			Log.i(LICHAO, "VisitorPassword success:" + newpassword);
		} else {
			Log.i(LICHAO, "VisitorPassword fail");
		}
	}
	
	/**
	 * 手机屏蔽云对讲
	 * @param msg
	 * @param status
	 */
	private static void SetCloudClose(String msg, String status) {
		boolean isOnline = true;//默认为1,不屏蔽
		if(status.equals("1")){//取消屏蔽		
			mCallback.setAccountOnline(msg, isOnline);
		} else if (status.equals("0")) {//屏蔽
			isOnline = false;
			mCallback.setAccountOnline(msg, isOnline);
		}
	}
	
	/**
	 * 手机解绑室内机
	 * @param account
	 * @param token
	 */
	private static void UnBindDevice(String account, String token){
		mCallback.delAccountByToken(account, token);
	}
}

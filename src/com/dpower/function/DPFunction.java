package com.dpower.function;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.android.api.UhomeApi;
import org.android.netcfgsdk.NetCfgManager;
import org.android.talkserversdk.LockParam;
import org.android.talkserversdk.TalkManager;
import org.android.talkserversdk.TalkerCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.net.ethernet.EthernetDevInfo;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import com.dpower.cloudintercom.CloudIntercom;
import com.dpower.cloudintercom.CloudIntercomCallback;
import com.dpower.domain.AddrInfo;
import com.dpower.domain.AlarmInfo;
import com.dpower.domain.AlarmLog;
import com.dpower.domain.AlarmNameInfo;
import com.dpower.domain.AlarmTypeInfo;
import com.dpower.domain.AlarmVideo;
import com.dpower.domain.BindAccountInfo;
import com.dpower.domain.CallInfomation;
import com.dpower.domain.MessageInfo;
import com.dpower.domain.RoomNumInfo;
import com.dpower.domain.SafeModeInfo;
import com.dpower.pub.dp2700.model.IndoorSipInfo;
import com.dpower.util.CommonUT;
import com.dpower.util.ConstConf;
import com.dpower.util.DPDBHelper;
import com.dpower.util.MyLog;
import com.dpower.util.ProjectConfigure;
import com.example.dpservice.CallReceiver;
import com.example.dpservice.DPIntercomService;
import com.example.dpservice.DPSafeService;
import com.example.dpservice.JniPhoneClass;

/**
 * sdk��Կ����ߵĽӿڼ�������Ϣ������
 */
public class DPFunction {
	private static final String TAG = "DPFunction";
	private static final String LICHAO = "lichao";
	
	private static Object mutex = new Object(); // ���򿪷��ߵĽӿڼ���

	/** �������� ,������Ϣ"alarm" */
	public static final String ACTION_ALARMING = "action.intent.ALARMING";

	/** ������� */
	public static final String ACTION_DISALARMING = "action.intent.DISALARMING";

	/** ����ģʽ�ı� */
	public static final String ACTION_SAFE_MODE = "action.intent.SAFE_MODE";
	
	/** ���ܼҾ�ģʽ�ı� */
	public static final String ACTION_SMART_HOME_MODE = "action.intent.SMART_HOME_MODE";

	/** �����������ñ� */
	public static final String ACTION_UPDATE_NETCFG = "manager.update.netcfg";

	/** ������������apk */
	public static final String ACTION_UPDATE_APK = "manager.update.apk";

	/** ��������ͬ��ʱ�� */
	public static final String ACTION_UPDATE_TIME = "manager.update.time";

	/** ��������������� */
	public static final String ACTION_UPDATE_ADVERTISEMENT = "manager.update.advertisement";

	/** �������ĸ������� */
	public static final String ACTION_UPDATE_WEATHER = "manager.update.weather";

	/** */
	public static final String ACTION_UPDATE_GUARD = "manager.update.guard";
	
	/** �ƶԽ���¼״̬�ı� */
	public static final String ACTION_CLOUD_LOGIN_CHANGED = "action.intent.CLOUD_LOGIN_CHANGED";
	
	/** �ֻ��󶨻��߽���豸 */
	public static final String ACTION_CLOUD_BIND_CHANGED = "action.intent.CLOUD_BIND_CHANGED";
	
	public static int[] videoAreaCallInUnit = { 0, 0, 600, 480 }; // Ĭ����Ƶ��ʾ����
	public static boolean phoneAccept = false;
	public static boolean isCallAccept = false;
	public static Class<?> alarmActivity = null;
	public static Class<?> callInFromDoorActivity = null;
	public static Class<?> callInActivity = null;
	
	private static final String DPSAFESERVICE_NULL = "DPSafeService is not runing";

	/** �������豸ͨ�� */
	private static TalkManager mTalkManager = null;

	/** ���������ñ���Ϣ���� */
	private static NetCfgManager mNetCfgManager = null;

	/** �������ñ�汾�� */
	private static int mNetCfgVer = 0;

	/** �������豸ͨ�� */
	private static JniPhoneClass mJniPhoneClass = null;
	private static UhomeApi mUhomeApi = null;
	private static CallInfomation mSeeInfo = null;
	private static List<CallInfomation> mCallOutSessionIDList = null;
	private static List<CallInfomation> mCallInSessionIDList = null;
	private static boolean mIsAlarming = false; // �Ƿ����ڱ���
	private static boolean mAlamActivityState = false; // ���������Ƿ��Ѿ�����
	private static SoundPool mSoundPool = null;
	private static int mStreamID = 0;
	private static File mFromFile;
	private static File mToFile;
	private static Context mContext = null;

	public static Context getContext() {
		return mContext;
	}

	/**
	 * ��ʼ����̨�����Լ���Ӧ������,���ٵ�ʱ����Ҫִ��deinit() �˷������ʱ10s���ң�����10s֮���ٽ�����������
	 * 
	 * @param context
	 *            ����������Ҫһֱ���ڣ����ɱ�destroy��
	 * @return int ����0�������������쳣
	 */
	public static int init(Context context) {
		synchronized (mutex) {
			MyLog.setLogPrint(true);
			MyLog.print(TAG, "init��ʼ");
			mContext = context;
			Time time = new Time();
			time.setToNow();
			if (time.year < 2017) {
				setTime("2017-01-01 00:00:00");
			}
			
			mUhomeApi = new UhomeApi(mContext);
			mSeeInfo = new CallInfomation();
			mCallOutSessionIDList = Collections
					.synchronizedList(new ArrayList<CallInfomation>());
			mCallInSessionIDList = Collections
					.synchronizedList(new ArrayList<CallInfomation>());
			mContext.startService(new Intent(mContext, DPSafeService.class)); // ������������
			mContext.startService(new Intent(mContext, DPIntercomService.class)); // �����Խ�����
			mTalkManager = new TalkManager(mTalkerCallback);
			if (mTalkManager == null) {
				return -2;
			}
			mNetCfgManager = NetCfgManager.getInstance();
			if (mNetCfgManager == null) {
				MyLog.print("NetCfgManager init() fail");
				return -3;
			}
			mNetCfgVer = mNetCfgManager.InitNetcfgFile(ConstConf.NET_CFG_PATH);
			if (mNetCfgVer == 0) {
				MyLog.print("Init NetcfgFile fail");
				return -4;
			}
			mJniPhoneClass = JniPhoneClass.getInstance();
			if (mJniPhoneClass == null) {
				return -5;
			}
			String saveCode = DPDBHelper.getRoomCode();
			MyLog.print("saveCode - > " + saveCode);
			int result = changeRoomCode(saveCode);
			if (result != 0) {
				String defaultTerm = mNetCfgManager.GetDefaultTerm(1);
				ArrayList<AddrInfo> list = AddrInfo
						.parsingAddrInfo(defaultTerm);
				if (list == null || list.size() != 1) {
					MyLog.print("GetDefaultTerm error");
					return -6;
				}
				AddrInfo info = list.get(0);
				MyLog.print("info - > " + info.getCode());
				result = changeRoomCode(info.getCode());
			}
			if (result == 0) {
				MyLog.print("start TalkManager service");
				TalkManager.StartManageClient();
				TalkManager.StartPCServer();
				TalkManager.StartRoomServer();
				if (ProjectConfigure.needCloudIntercom) {
					MyLog.print(TAG, "�����ƶԽ�");
					CloudIntercom.init(mContext, mCloudIntercomCallback);
				}
				return 0;
			}
			return -7;
		}
	}

	/**
	 * ���������������
	 */
	public static void deinit() {
		synchronized (mutex) {
			mContext.stopService(new Intent(mContext, DPSafeService.class)); // ������������
			mContext.stopService(new Intent(mContext, DPIntercomService.class)); // �����Խ�����
			if (ProjectConfigure.needCloudIntercom) {
				CloudIntercom.deinit();
			}
			mCallOutSessionIDList.clear();
			mCallInSessionIDList.clear();
			if (mNetCfgManager != null) {
				mNetCfgManager.ReleaseNetcfg();
				mNetCfgManager = null;
			}
			isCallAccept = false;
			mIsAlarming = false;
			mAlamActivityState = false;
		}
	}
	
	// TODO-----------------------------ͨ�����-----------------------------

	private static TalkerCallback mTalkerCallback = new TalkerCallback() {

		@Override
		public void SynchSuccessed() {
			MyLog.print("TalkerCallback SynchSuccessed");
			// ͬ��������¼
			ArrayList<AlarmLog> alarmLogs = DPDBHelper.getAlarmLogList();
			if (alarmLogs == null) {
				return;
			}
			for (int i = 0; i < alarmLogs.size(); i++) {
				if (!alarmLogs.get(i).getIsSuccess()) {
					int delayTime[] = DPDBHelper.getAlarmDelayTime(DPDBHelper.getSafeMode());
					String alarmRoom; //���������źͷ���������
					int areaNameValue = getAlarmAreaNameList().get(
							alarmLogs.get(i).getAreaName()).value;
					if (areaNameValue < 10) {
						alarmRoom = alarmLogs.get(i).getAreaNum() + "0" + areaNameValue;
					} else {
						alarmRoom = Integer.toString(alarmLogs.get(i).getAreaNum()) + areaNameValue;
					}
					int result = TalkManager.toManageAlarm(
							CommonUT.formatTime(alarmLogs.get(i).getTime()), 
							Integer.parseInt(alarmRoom),
							getAlarmTypeNameList().get(alarmLogs.get(i).getAreaType()).value,
							delayTime[alarmLogs.get(i).getAreaNum() - 1]);
					if (result == 0) {
						alarmLogs.get(i).setIsSuccess(true);
						DPDBHelper.modifyAlarmLog(alarmLogs.get(i));
						MyLog.print("toManageAlarm Success");
					}
				}
			}
			if (ProjectConfigure.project == 2) {
				// ͬ��������¼
				ArrayList<SafeModeInfo> list = DPDBHelper.getSafeModeInfoList();
				if (list == null) {
					return;
				}
				for (int i = 0; i < list.size(); i++) {
					if (!list.get(i).getIsSuccess()) {
						int result = TalkManager.toManageAlarm(
								CommonUT.formatTime(list.get(i).getTime()), 
								list.get(i).getMode() + 90, 98, 0);
						if (result == 0) {
							list.get(i).setIsSuccess(true);
							DPDBHelper.modifySafeModeLog(list.get(i));
							MyLog.print("toManageSafeMode Success");
						}
					}
				}
				// ͬ���ſڻ����м�¼
				ArrayList<CallInfomation> infos = DPFunction.getCallLogList(
						CallInfomation.CALL_IN_ACCEPT | CallInfomation.CALL_IN_UNACCEPT);
				if (infos == null) {
					return;
				}
				for (int i = 0; i < infos.size(); i++) {
					if (!infos.get(i).getIsSuccess()) {
						boolean isDoor = false;
						int type = 0;
						if (infos.get(i).getType() == CallInfomation.CALL_IN_UNACCEPT
								&& infos.get(i).isDoor()) {
							isDoor = true;
							type = 1;
						} else if (infos.get(i).isDoor()) {
							isDoor = true;
							type = 0;
						}
						if (isDoor) {
							int result = TalkManager.toManageDoorCall(infos.get(i).getRemoteCode(), 
									CommonUT.formatTime(infos.get(i).getStartTime()), type);
							if (result == 0) {
								infos.get(i).setIsSuccess(true);
								DPDBHelper.modifyCallLog(infos.get(i));
								MyLog.print("toManageDoorCall Success");
							}
						}
					}
				}
			}
		}
		
		@Override
		public void SetTime(String dateTime) {
			setTime(dateTime);
		}

		/**
		 * ����С����Ϣ
		 */
		@Override
		public void UpdateInfoMsg(String infos) {
			if (TextUtils.isEmpty(infos)) {
				return;
			}
			String saveTime = null;
			boolean personal = true;
			//<type>��ǩ,personal�ֶ�
			String type = getXmlVal("type", infos);
			if (type == null) {
				personal = false;
			}
			int count = 0;
			while (true) {
				int start = infos.indexOf("<info>");
				int end = infos.indexOf("</info>");
				if (start == -1 || end == -1) {
					break;
				}
				String infoItem = new String(infos.substring(start + 6, end));
				MessageInfo msgInfo = new MessageInfo();
				msgInfo.setPersonal(personal);
				//<time>��ǩ
				String val = getXmlVal("time", infoItem);
				if (val != null) {
					msgInfo.setTime(val);
					// ������󹫹���Ϣʱ��,�ж�����Ϣʱ����1��Ϊ���ʱ��
					if (!personal && saveTime == null) {
						saveTime = val;
						DPDBHelper.setUpdateMsgTime(saveTime);
					}
				}
				//<title>��ǩ
				val = getXmlVal("title", infoItem);
				if (val != null) {
					msgInfo.setTitle(val);
				} else {
					return;
				}
				//<body>��ǩ
				val = getXmlVal("body", infoItem);
				if (val != null) {
					msgInfo.setBody(val);
				}
				//<URL>��ǩ �����ӵ�ַ��ʱ���浽body�ֶ�����
				val = getXmlVal("url", infoItem);
				if (val != null) {
					msgInfo.setBody(val);
				}
				if (val != null) {
					val = val.replace("\\", File.separator);
					if (val.contains("htm")) {
						File destDir = new File(ConstConf.MESSAGE_PATH);
						if (!destDir.exists()) {
							destDir.mkdirs();
						}
						msgInfo.setIsJpg(false);
						String resName = msgInfo.getTime().replaceAll(":", "-") + ".htm";
						MyLog.print(TAG, "resName = " + resName);
						boolean result = CommonUT.httpDownload(val, ConstConf.MESSAGE_PATH 
								+ File.separator + resName);
						if (result) {
							msgInfo.setResName(resName);
						}
					}
				}
				//<JPG>��ǩ
				val = getXmlVal("jpg", infoItem);
				if (val != null) {
					val = val.replace("\\", File.separator);
					if (val.contains("jpg")) {
						File destDir = new File(ConstConf.MESSAGE_PATH);
						if (!destDir.exists()) {
							destDir.mkdirs();
						}
						msgInfo.setIsJpg(true);
						String resName = msgInfo.getTime().replaceAll(":", "-") + ".jpg";
						MyLog.print(TAG, "resName = " + resName);
						boolean result = CommonUT.httpDownload(val, ConstConf.MESSAGE_PATH 
								+ File.separator + resName);
						if (result) {
							msgInfo.setResName(resName);
						}
					}
				}
				++count;
				DPDBHelper.addMessageLog(msgInfo);
				Log.i(LICHAO, "UpdateInfoMsg " + msgInfo.toString());
				infos = new String(infos.substring(end + 7));
			}

			mContext.sendBroadcast(new Intent(MessageInfo.ACTION_MESSAGE));
			// ������ʾ��
			playNewMsgRing();
			toPhoneNewMsg(personal, count);		
		}

		@Override
		public void UpdateWeather(String info) {
			// ��Ӹ�����������
			// ����ʱ��
			String time = getXmlVal("time", info);
			if (time != null) {
				DPDBHelper.setUpdateWeatherTime(time);
			}
			mContext.sendBroadcast(new Intent(ACTION_UPDATE_WEATHER));
		}

		@Override
		public void UpdateNetcfg(String infos) {
			if (TextUtils.isEmpty(infos)) {
				return;
			}
			String tempUrl = getXmlVal("netcfgurl", infos);
			if (tempUrl == null) {
				return;
			}
			String url = tempUrl.replace("\\", File.separator);
			if (url == null) {
				return;
			}
			String fileMD5 = getXmlVal("md5", infos);
			if (fileMD5 == null) {
				return;
			}
			File destDir = new File(ConstConf.TEMP_PATH);
			if (!destDir.exists()) {
				destDir.mkdirs();
			}
			if (!CommonUT.httpDownload(url, ConstConf.NEW_NET_CFG_PATH)) {
				MyLog.print(MyLog.ERROR, TAG, "�����������ñ�ʧ��");
				return;
			}
			// �ȽϹ������ĸ�MD5ֵ�ͱ������ص��ļ���MD5ֵ������ͬ�����ص��ļ�������
			if (!fileMD5.equals(getFileMD5(ConstConf.NEW_NET_CFG_PATH))) {
				MyLog.print(TAG, "�����������ñ�ʧ��");
				MyLog.print(MyLog.ERROR, TAG, "MD5����ͬ");
				MyLog.print(MyLog.ERROR, TAG, fileMD5);
				MyLog.print(MyLog.ERROR, TAG, getFileMD5(ConstConf.NEW_NET_CFG_PATH));
			} else {
				from(ConstConf.NEW_NET_CFG_PATH);
				if (to(ConstConf.NET_CFG_PATH)) {
					deinit();
					init(mContext);
					mContext.sendBroadcast(new Intent(ACTION_UPDATE_NETCFG));
					MyLog.print(TAG, "�����������ñ�ɹ�");
				} else {
					MyLog.print(TAG, "�����������ñ�ʧ��");
				}
			}
			File file = new File(ConstConf.NEW_NET_CFG_PATH);
			if (file.exists()) {
				file.delete();
			}
		}

		/**
		 * ������������APK
		 */
		@Override
		public void UpdateVersion(String infos) {
			if (TextUtils.isEmpty(infos)) {
				return;
			}
			String tempUrl = getXmlVal("url", infos);
			if (tempUrl == null) {
				return;
			}
			String url = tempUrl.replace("\\", File.separator);
			if (url == null) {
				return;
			}
			String fileMD5 = getXmlVal("md5", infos);
			if (fileMD5 == null) {
				return;
			}
			if (!CommonUT.httpDownload(url, ConstConf.UPDATE_PATH)) {
				MyLog.print(MyLog.ERROR, TAG, "����APKʧ��");
				return;
			}
			// �ȽϹ������ĸ�MD5ֵ�ͱ������ص��ļ���MD5ֵ������ͬ�����ص��ļ�������
			if (!fileMD5.equals(getFileMD5(ConstConf.UPDATE_PATH))) {
				MyLog.print(MyLog.ERROR, TAG, "����APKʧ��");
				MyLog.print(MyLog.ERROR, TAG, "MD5����ͬ");
				MyLog.print(MyLog.ERROR, TAG, fileMD5);
				MyLog.print(MyLog.ERROR, TAG, getFileMD5(ConstConf.UPDATE_PATH));
			} else {
				int result = TalkManager.Unpack(ConstConf.UPDATE_PATH, ConstConf.SD_DIR);
				if (result == 0) {
					MyLog.print(MyLog.ERROR, TAG, "��ѹ�ɹ�");
					// ���͹㲥��������װ
					mContext.sendBroadcast(new Intent(ACTION_UPDATE_APK));
				} else {
					MyLog.print(MyLog.ERROR, TAG, "����APKʧ��");
				}
			}
			File file = new File(ConstConf.UPDATE_PATH);
			if (file.exists()) {
				file.delete();
			}
		}

		@Override
		public void UpdateAdvs(String info) {
			Intent intent = new Intent(ACTION_UPDATE_ADVERTISEMENT);
			intent.putExtra(ACTION_UPDATE_ADVERTISEMENT, info);
			mContext.sendBroadcast(intent);
		}

		@Override
		public void UpdateGuard(String info) {
			Intent intent = new Intent(ACTION_UPDATE_GUARD);
			intent.putExtra(ACTION_UPDATE_GUARD, info);
			mContext.sendBroadcast(intent);
		}

		@Override
		public void ChangeSafeMode(String info) {
			int model = 1;
			if (info.equals("UnSafe")) {
				model = 1;
			} else if (info.equals("Night")) {
				model = 2;
			} else if (info.equals("Home")) {
				model = 3;
			} else if (info.equals("Leave")) {
				model = 4;
			}
			changeSafeMode(model, true);
		}

		@Override
		public String GetSafeMode() {
			return getStringSafeMode();
		}

		@Override
		public boolean ChangeRoomNum(String newCode) {
			return changeRoomCode(newCode) == 0;
		}

		@Override
		public String GetLastInfoTime() {
			return DPDBHelper.getUpdateMsgTime();
		}

		@Override
		public String GetWeatherLastTime() {
			return DPDBHelper.getUpdateWeatherTime();
		}

		@Override
		public String GetManageIP() {
			MyLog.print("GetManageIP");
			if (mNetCfgManager == null) {
				return null;
			}
			String managerInfo = mNetCfgManager.ManagerGet(null);
			if (managerInfo != null) {
				ArrayList<AddrInfo> list = AddrInfo
						.parsingAddrInfo(managerInfo);
				if (list == null || list.isEmpty()) {
					MyLog.print("This netcfg is not manager");
				} else {
					MyLog.print("GetManageIP - > " + list.get(0).getIp());
					return list.get(0).getIp();
				}
			}
			MyLog.print("GetManageIP - > " + null);
			return null;
		}

		@Override
		public String GetDevType() {
			PackageManager manager = mContext.getPackageManager();
			PackageInfo info;
			try {
				info = manager.getPackageInfo(mContext.getPackageName(), 0);
				return info.versionName;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public String GetLoaclCode() {
			return getRoomCode();
		}

		@Override
		public String GetVersion() {
			PackageManager manager = mContext.getPackageManager();
			PackageInfo info;
			try {
				info = manager.getPackageInfo(mContext.getPackageName(), 0);
				return new String(info.versionCode + "");
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public String GetSysVersion() {
			return Build.DISPLAY;
		}

		@Override
		public String GetNetcfgMD5() {
			return getFileMD5(ConstConf.NET_CFG_PATH);
		}

		@Override
		public void Restart() {
			try {
				Runtime.getRuntime().exec("su -c reboot");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void Reset() {
		}

		@Override
		public void OpenFtp() {
		}
	};
	
	/** ������Ϣ��¼�����ݿ��� */
	public static void addMessageLog(MessageInfo info) {
		DPDBHelper.addMessageLog(info);
	}

	/**
	 * @���ܣ��޸����ݿ��е���Ϣ��¼
	 */
	public static void modifyMessageLog(MessageInfo info) {
		DPDBHelper.modifyMessageLog(info);
	}

	/**
	 * @���ܣ�ɾ�����ݿ���ָ��ID����Ϣ��¼
	 * @������int db_id - ���ݿ����Զ����ɵ�_id
	 */
	public static void deleteMessageLog(int db_id) {
		DPDBHelper.deleteMessageLog(db_id);
	}

	/**
	 * @���ܣ�ɾ����Ӧ���͵�������Ϣ��¼
	 * @������int type - 1-������Ϣ��2-������Ϣ��3-������Ϣ
	 */
	public static void deleteAllMessageLog(int type) {
		DPDBHelper.deleteAllMessageLog(type);
	}

	/**
	 * @���ܣ������ݿ��л�ȡָ�����͵ĵ�δ����Ϣ����
	 * @������int type PERSONAL��PUBLIC��PERSONAL|PUBLIC
	 */
	public static int getUnReadMessageLogNum(int type) {
		return DPDBHelper.getUnReadMessageLogNum(type);
	}

	/**
	 * @���ܣ������ݿ��л�ȡָ�����͵���Ϣ��¼
	 * @������int type PERSONAL��PUBLIC��PERSONAL|PUBLIC
	 */
	public static ArrayList<MessageInfo> getMessageLogList(int type) {
		return DPDBHelper.getMessageLogList(type);
	}
	
	// TODO-----------------------------�������-----------------------------
	
	/** ���ñ������� */
	public static void setAlarmActivity(Class<?> cl) {
		alarmActivity = cl;
	}
	
	/** �����ſڻ��º����������Activity���� ��ʱ�ſڻ������ڻ�������ͷ�����С��һ����������ʾ�Ľ��治һ������ֿ����� */
	public static void setDoorCallInActivity(Class<?> cl) {
		callInFromDoorActivity = cl;
	}

	/** �������ڻ��º����������Activity���� */
	public static void setRoomCallInActivity(Class<?> cl) {
		callInActivity = cl;
	}

	/** ��ȡ�������Ƿ��Ѿ����� */
	public static boolean getAlamActivityState() {
		return mAlamActivityState;
	}

	/** ���þ������Ƿ��Ѿ����У������ڱ���ҳ�����ǰ��ԭΪfalse */
	public static void setAlamActivityState(boolean AlamActivityState) {
		mAlamActivityState = AlamActivityState;
	}
	
	private static void playNewMsgRing() {
		if (mSoundPool != null) {
			if (mStreamID != 0) {
				mSoundPool.stop(mStreamID);
				mStreamID = 0;
			}
			mSoundPool.release();
			mSoundPool = null;
		}
		mSoundPool = new SoundPool(1, AudioManager.STREAM_RING, 5);
		mSoundPool.load(ConstConf.MSG_RING_PATH, 1);
		mSoundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId,
					int status) {
				int streamID = soundPool.play(1, 1, 1, 0, 0, 1);
				if (streamID == 0) {
					MyLog.print(MyLog.ERROR, "paly MsgRing error");
				}
			}
		});
	}
	
	/**
	 * @���ܣ�����Դ�ַ����е����ַ������������ַ�����XML������ֵ��
	 * @����1��String key - ���ַ���
	 * @����2��String src - Դ�ַ���
	 * @����ֵ��String - key�����ڷ���null,���򷵻�key�������ַ���
	 */
	private static String getXmlVal(String key, String src) {
		String strstart = "<" + key + ">";
		String strend = "</" + key + ">";
		int start = src.indexOf(strstart);
		if (start == -1) {
			MyLog.print("getXmlVal -> start key does not exist");
			return null;
		}
		int end = src.indexOf(strend);
		if (end == -1) {
			MyLog.print("getXmlVal -> end key does not exist");
			return null;
		}
		String strval = new String(
				src.substring(start + strstart.length(), end));
		return strval;
	}

	/**
	 * @���ܣ���ȡָ��·���ļ���MD5ֵ
	 * @������String filePath - �ļ�����·��
	 * @����ֵ��String - �ļ��������򷵻�null,���򷵻ظ��ļ�MD5ֵ
	 */
	private static String getFileMD5(String filePath) {
		File file = new File(filePath);
		if (!file.isFile()) {
			MyLog.print("getFileMD5 -> File does not exist");
			return null;
		}
		MessageDigest digest = null;
		FileInputStream in = null;
		byte[] buffer = new byte[1024];
		int len;
		try {
			digest = MessageDigest.getInstance("MD5");
			in = new FileInputStream(file);
			while ((len = in.read(buffer)) > 0) {
				digest.update(buffer, 0, len);
			}
			in.close();
		} catch (Exception e) {
			MyLog.print("getFileMD5 -> " + e.toString());
			if (in != null) {
				try {
					in.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			return null;
		}
		return bytesToHexString(digest.digest());
	}

	/**
	 * ��byte[]ת����16�����ַ���
	 */
	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}
	
	/**
	 * ��16�����ַ���ת����byte[]
	 */
	public static byte[] hexStringToBytes(String hexString) {
	    if (hexString == null || hexString.equals("")) {
	        return null;
	    }
	    hexString = hexString.toUpperCase();
	    int length = hexString.length() / 2;
	    char[] hexChars = hexString.toCharArray(); 
	    byte[] data = new byte[length];  
	    for (int i = 0; i < length; i++) {
	        int pos = i * 2;
	        data[i] = (byte) (charToByte(hexChars[pos]) << 4 
	        		| charToByte(hexChars[pos + 1])); 
	    }
	    return data;
	}
	
	private static byte charToByte(char c) {   
		return (byte) "0123456789ABCDEF".indexOf(c);   
	} 
	
	/**
	 * ��intת��Ϊռ�ĸ��ֽڵ�byte[]
	 */
	public static byte[] intToBytes( int value ) {
	    byte[] src = new byte[4];
	    src[3] =  (byte) ((value >> 24) & 0xFF);
	    src[2] =  (byte) ((value >> 16) & 0xFF);
	    src[1] =  (byte) ((value >> 8) & 0xFF);
	    src[0] =  (byte) (value & 0xFF);
	    return src;
	}
	
	/**
	 * ��byte[]ת��ΪINT
	 */
	public static int bytesToInt(byte[] src, int offset) {
	    int value;
	    value = (int) ((src[offset] & 0xFF)
	            | ((src[offset + 1] & 0xFF) << 8) 
	            | ((src[offset + 2] & 0xFF) << 16) 
	            | ((src[offset + 3] & 0xFF) << 24));
	    return value;
	} 
	
	/**
	* ���ļ�ת��Ϊ�ֽ�����
	*/
	public static byte[] fileToBytes(File file) {
		if (file == null || (int)file.length() == 0) {
            return null;
        }
	    byte[] data = null;
	    FileInputStream inputStream = null;
	    ByteArrayOutputStream outputStream = null;
	    try{
	        inputStream = new FileInputStream(file);
	        outputStream = new ByteArrayOutputStream((int)file.length());
	        byte[] buffer = new byte[1024];
	        int len;
	        while ((len = inputStream.read(buffer)) > 0) {
	            outputStream.write(buffer, 0, len);
	        }
	        outputStream.flush();
	        data = outputStream.toByteArray();
	    } catch(Exception e) {
	        e.printStackTrace();
	    } finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	    return data;
	}
	
	/**
	* ���ַ�������ѹ��----"ISO-8859-1"
	*/
	public static String compress(String data) {
		if (data == null) {
			return null;
		}
	    String newData = null;
	    ByteArrayOutputStream outputStream = null;
	    GZIPOutputStream gzip = null;
	    MyLog.print("���ַ�������ѹ��before:" + data.length());
	    try{
	        outputStream = new ByteArrayOutputStream();
	        gzip = new GZIPOutputStream(outputStream);
	        gzip.write(data.getBytes("ISO-8859-1"));
	        gzip.finish();
	        gzip.flush();
	        outputStream.flush();
	        newData = outputStream.toString("ISO-8859-1");
	        MyLog.print("���ַ�������ѹ��after:" + newData.length());
	    } catch(Exception e) {
	        e.printStackTrace();
	    } finally {
			try {
				if (gzip != null) {
					gzip.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	    return newData;
	}
	
	/** 
     * ��byte[]����ѹ��
     */  
	public static byte[] compress(byte[] data) {  
		if (data == null || data.length == 0) {
            return null;
        }
		byte[] newData = null;
		ByteArrayOutputStream outputStream = null;
	    GZIPOutputStream gzip = null;
		MyLog.print("��byte[]����ѹ��before:" + data.length);
        try {
        	outputStream = new ByteArrayOutputStream();
        	gzip = new GZIPOutputStream(outputStream);
            gzip.write(data);
            gzip.finish();
            gzip.flush();
            outputStream.flush();
            newData = outputStream.toByteArray();
            MyLog.print("��byte[]����ѹ��after:" + newData.length);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
			try {
				if (gzip != null) {
					gzip.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
        return newData;
    }  
	
	/** ��byte[]���н�ѹ�� */
	public static byte[] uncompress(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        byte[] newData = null;
        MyLog.print("��byte[]���н�ѹ��before:" + data.length);
        ByteArrayOutputStream outputStream = null;
        ByteArrayInputStream inputStream = null;
        GZIPInputStream gzip = null;
        try {
        	outputStream = new ByteArrayOutputStream();
        	inputStream = new ByteArrayInputStream(data);
            gzip = new GZIPInputStream(inputStream);
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = gzip.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
            newData = outputStream.toByteArray();
            MyLog.print("��byte[]���н�ѹ��after:" + newData.length);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
				if (gzip != null) {
					gzip.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
        return newData;
    }
	
	/** ��ͼƬ����ѹ�� */
	private static byte[] compressBitmap(Bitmap bitmap) {
		if (bitmap == null) {
			return null;
		}
		byte[] newData = null;
	    ByteArrayOutputStream stream = null;
	    try {
	    	stream = new ByteArrayOutputStream();
	    	bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		    MyLog.print("��ͼƬ����ѹ��before:" + stream.toByteArray().length); 
		    int options = 90;
		    while (stream.toByteArray().length / 1024 > 25) {
		        stream.reset();
		        bitmap.compress(Bitmap.CompressFormat.JPEG, options, stream);
		        options -= 5;
		    }
		    stream.flush();
		    newData = stream.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	    MyLog.print("��ͼƬ����ѹ��after:" + newData.length);
	    return newData;
	}

	/** ���÷��� */
	public static boolean setRoomCode(String roomCode) {
		return DPDBHelper.setRoomCode(roomCode);
	}
	
	/**
	 * @���ܣ���ȡ��������
	 * @����ֵ��null�򱾻�����
	 * */
	public static String getRoomCode() {
		if (mNetCfgManager == null) {
			return null;
		}
		return mNetCfgManager.GetLocalCode();
	}

	/**
	 * @���ܣ��޸ķ��źͶ�Ӧ��IP��ַ
	 * @������String newCode - �µķ��ţ�13λ���ַ�����
	 * @����ֵ��int 0-�ɹ�����0ʧ��
	 */
	public static int changeRoomCode(String newCode) {
		synchronized (mutex) {
			MyLog.print("changeRoomCode " + newCode);
			if (newCode == null) {
				MyLog.print(MyLog.ERROR, "changeRoomCode is null");
				return -1;
			}
			if (mNetCfgManager == null) {
				return -1;
			}
			// �жϷ��Ų�����
			if (mNetCfgManager.TermGet(newCode) == null) {
				MyLog.print(MyLog.ERROR, "newCode " + newCode + " is not exist");
				return -1;
			}
			String newInfo = mNetCfgManager.InitTerm(newCode);
			String oldCode = DPDBHelper.getRoomCode();
			if (newInfo == null) {
				MyLog.print(MyLog.ERROR, "InitTerm " + newCode + " error");
				mNetCfgManager.InitTerm(oldCode);
				return -2;
			}
			ArrayList<AddrInfo> list = AddrInfo.parsingAddrInfo(newInfo);
			if (list == null || list.size() != 1) {
				MyLog.print(MyLog.ERROR, "Should not be the case!");
				mNetCfgManager.InitTerm(oldCode);
				return -3;
			}
			AddrInfo curInfo = list.get(0);
			if (!setIp(ConstConf.LAN_NETWORK_CARD, curInfo.getIp(),
					curInfo.getMask(), curInfo.getGw(), null)) {
				MyLog.print(MyLog.ERROR, "setIp - >" + curInfo.toString());
				mNetCfgManager.InitTerm(oldCode);
				return -4;
			}
			if (mJniPhoneClass != null) {
				mJniPhoneClass.UnInit();
			}
			if (mJniPhoneClass.Init(curInfo.getCode(), curInfo.getIp())) {
				// ֻ�����ֻ���������������
				DPDBHelper.setRoomCode(mNetCfgManager.GetLocalCode());
				//DPDBHelper.clearAccount();
				toStartLogin();
				return 0;
			}
			MyLog.print(MyLog.ERROR, "JniPhoneClass Init error");
			String oldInfo = mNetCfgManager.InitTerm(oldCode);
			list = AddrInfo.parsingAddrInfo(oldInfo);
			if (list != null && list.size() == 1) {
				curInfo = list.get(0);
				setIp(ConstConf.LAN_NETWORK_CARD, curInfo.getIp(),
						curInfo.getMask(), curInfo.getGw(), null);
			}
			return -5;
		}
	}
	
	public static void setDefauleWanIP() {
		MyLog.print("setDefauleWanIP - > " + DPDBHelper.isWanDHCP());
		if (DPDBHelper.isWanDHCP()) {
			setIp(ConstConf.WAN_NETWORK_CARD);
		} else {
			setIp(ConstConf.WAN_NETWORK_CARD, DPDBHelper.getWanIP(),
					DPDBHelper.getWanMask(), DPDBHelper.getWanGw(),
					DPDBHelper.getWanDNS());
		}
	}

	/**
	 * @��������setIp
	 * @���ܣ�����ָ�������Ķ�̬IP��ַ
	 * @����1��String netCardName - ��������
	 */
	private static boolean setIp(String netCardName) {
		if (TextUtils.isEmpty(netCardName)) {
			return false;
		}
		EthernetDevInfo devInfo = new EthernetDevInfo();
		devInfo.setIfName(netCardName);
		devInfo.setConnectMode(EthernetDevInfo.ETHERNET_CONN_MODE_DHCP);
		mUhomeApi.setEthernetInfo(devInfo);
		if (netCardName.equals(ConstConf.WAN_NETWORK_CARD)) {
			DPDBHelper.setWanDHCP(true);
		}
		return true;
	}
	
	/**
	 * @���ܣ�����ָ��������IP��ַ
	 * @����1��String netCardName - ��������
	 * @����2��String ip - ip��ַ
	 * @����3��String mask - ��������
	 * @����4��String gw - ����
	 */
	private static boolean setIp(String netCardName, String ip, String mask,
			String gw, String dns) {
		if (TextUtils.isEmpty(ip)) {
			return false;
		}
		EthernetDevInfo devInfo = new EthernetDevInfo();
		if (!TextUtils.isEmpty(gw)) {
			devInfo.setGateWay(gw);
		}
		if (!TextUtils.isEmpty(mask)) {
			devInfo.setNetMask(mask);
		}
		if (!TextUtils.isEmpty(dns)) {
			devInfo.setDnsAddr(dns);
		} else {
			devInfo.setDnsAddr(gw);
		}

		devInfo.setIfName(netCardName);
		devInfo.setIpAddress(ip);
		devInfo.setConnectMode(EthernetDevInfo.ETHERNET_CONN_MODE_MANUAL);
		mUhomeApi.setEthernetInfo(devInfo);
		if (netCardName.equals(ConstConf.WAN_NETWORK_CARD)) {
			DPDBHelper.setWanDHCP(false);
			DPDBHelper.setWanIP(ip);
			DPDBHelper.setWanMask(mask);
			DPDBHelper.setWanGw(gw);
			if (!TextUtils.isEmpty(dns)) {
				DPDBHelper.setWanDNS(dns);
			}
		}
		MyLog.print("netCardName:" + netCardName + ",ip:" + ip + ",mask:"
				+ mask + ",gw:" + gw);
		return true;
	}

	/**
	 * �����ſڻ��û���������
	 * 
	 * @param doorIpAddr
	 *            �ſڻ�ip
	 * @param pwd
	 *            ������
	 * @return
	 */
	public static int toDoorModifyPassWord(String doorIpAddr, String pwd) {
		long roomID = CodetoID(getRoomCode());
		int ret = TalkManager.toDoorModifyPassWord(doorIpAddr, roomID, pwd,
				true);
		return ret;
	}
	
	/**
	 * ���÷ÿ�����
	 * @param doorIpAddr
	 * @param pwd
	 * @return
	 */
	public static int setVisitorPassWord(String doorIpAddr, String pwd) {
		long roomID = CodetoID(getRoomCode());
		int ret = TalkManager.toDoorModifyPassWord(doorIpAddr, roomID, pwd,
				true);
		return ret;
	}
	
	/** ���ݷ��Ż�ȡ��Ӧid */
	private static long CodetoID(String code) {
		if (mNetCfgManager == null) {
			return 0;
		}
		return mNetCfgManager.Code2ID(code);
	}

	/**
	 * ��̬��������getNetCfgVer
	 * 
	 * @���ܣ���ȡ�������ñ�汾��
	 * @����ֵ��0���������ñ�汾��
	 * */
	public static int getNetCfgVer() {
		return mNetCfgVer;
	}
	
	/**
	 * @���ܣ����ݷ��Ż�ȡ�������ñ��ж�Ӧ��IP��ַ��Ϣ
	 * @������String code - ����
	 * @����ֵ��AddrInfo IP��ַ��Ϣ
	 */
	public static AddrInfo getAddrInfo(String code) {
		if (mNetCfgManager == null) {
			return null;
		}
		if (code == null) {
			return null;
		}
		if (getRoomCode() == null) {
			return null;
		}
		if (code.length() == getRoomCode().length()) {
			String info = mNetCfgManager.TermGet(code);
			if (info == null) {
				return null;
			}
			info = getXmlVal("netcfg", info);
			if (info == null) {
				return null;
			}
			String strnum = getXmlVal("num", info);
			if (strnum == null) {
				return null;
			}
			int num = Integer.valueOf(strnum).intValue();
			if (num == 1) {
				ArrayList<AddrInfo> list = AddrInfo
						.parsingAddrInfo(info);
				if (list != null && !list.isEmpty()) {
					return list.get(0);
				}
			}
		}
		return null;
	}

	/**
	 * @���ܣ����ݷ��Ż�ȡ�������ñ��ж�Ӧ����������
	 * @����1��String code -����
	 * @����2��boolean isAbsolute - true-�������� false-��Ա������ŵ�����
	 * @����ֵ��String �������ñ��ж�Ӧ����������
	 * @eg��code2Name("1010101010101",ture) - 1��1��1��Ԫ0101��1�����ڷֻ�
	 * @��ע���������ñ���ֻ���������ƣ���Ҫ��ȡ��Ӣ������ֻ���Լ����ݷ�����д����
	 */
	public static String code2Name(String code, boolean isAbsolute) {
		if (mNetCfgManager == null) {
			return null;
		}
		if (code != null) {
			if (getRoomCode() != null) {
				if (code.length() == getRoomCode().length()) {
					long id = mNetCfgManager.Code2ID(code);
					if (id != 0) {
						return mNetCfgManager.ID2Name(id, isAbsolute);
					}
				}
			}
		}
		return null;
	}

	/**
	 * @���ܣ����ݷ��Ż�ȡ�豸����
	 * @������String code -����
	 * @����ֵ��int 1-���ڻ���2-��Ԫ�ſڻ���3-�����ſڻ���С�ſڻ�����4-��������6-����Ա����7-Χǽ����8-��������
	 */
	public static int code2Type(String code) {
		if (mNetCfgManager == null) {
			MyLog.print("mNetCfgManager is not init<>");
			return 0;
		}
		if (code == null) {
			MyLog.print("param is null");
			return 0;
		}
		return mNetCfgManager.Code2Type(code);
	}

	/**
	 * ��ȡ�������ñ��е����е�����������Ԫ�����š��ֻ��š�
	 * 
	 * @return ʧ�ܷ���null;
	 */
	public static ArrayList<RoomNumInfo> getRoomNumInfos() {
		String string = mNetCfgManager.GetAllTerm();
		return RoomNumInfo.parsingRoomNumInfo(string);
	}

	/**
	 * ����
	 * @param doorIp
	 *            �ſڻ�ip
	 * @return 0-�ɹ�,��0-ʧ��
	 */
	public static int toDoorCallEvt(String doorIp) {
		return TalkManager.toDoorCallEvt(doorIp,
				mNetCfgManager.Code2ID(getRoomCode()));
	}

	/**
	 * �����ſڻ��������ã��Ŵ���ʱ��������ƽ��������ʱ��
	 * 
	 * @param iNum
	 *            ��������
	 * @param LockDelay
	 *            ������ʱ(Ĭ��Ϊ-1)
	 * @param LockLevel
	 *            ������ƽ(Ĭ��Ϊ-1)
	 * @param MagicDelay
	 *            �Ŵ���ʱ(Ĭ��Ϊ-1)
	 * @return 0-�ɹ�,��0-ʧ��
	 */
	public static int toDoorSetLockParam(int iNum, int LockDelay,
			int LockLevel, int MagicDelay) {
		LockParam lockParam = new LockParam();
		String localCode = getRoomCode();
		String doorInfo = mNetCfgManager.SecDoorGet(localCode);
		ArrayList<AddrInfo> list = AddrInfo.parsingAddrInfo(doorInfo);
		if (list == null || iNum > list.size()) {
			return -1;
		}
		// ��ȡС�ſڻ�IP
		String doorIp = list.get(iNum - 1).getIp();
		// ��ȡ���ڻ���ǰCODE��Ϣ
		long roomID = mNetCfgManager.Code2ID(getRoomCode());
		MyLog.print("toDoorSetLockParam  doorIp " + doorIp + ", roomID "
				+ roomID);
		MyLog.print("toDoorSetLockParam  LockDelay " + LockDelay
				+ ", LockLevel " + LockLevel + ", MagicDelay " + MagicDelay);
		if (LockDelay != -1)
			lockParam.setLockDelay(LockDelay);// ������ʱ
		if (LockLevel != -1)
			lockParam.setLockLevel(LockLevel);// ������ƽ
		if (MagicDelay != -1)
			lockParam.setMagicDelay(MagicDelay);// �Ŵ���ʱ
		int result = TalkManager.toDoorSetLockParam(doorIp, roomID, lockParam);
		if (result == 0) {
			return 0;
		} else {
			return -2;
		}
	}

	/**
	 * �޸��ſڻ�����
	 * 
	 * @param doorCode
	 *            �ſڻ�����
	 * @return 0-�ɹ�,��0-ʧ��
	 */
	public static int toDoorSetNum(String doorCode) {
		String info = mNetCfgManager.TermGet(doorCode);
		ArrayList<AddrInfo> list = AddrInfo.parsingAddrInfo(info);
		if (list == null || list.size() != 1) {
			MyLog.print("room_code_not_exist");
			return -1;
		}
		// ��ȡС�ſڻ�Ĭ��CODE��Ϣ
		String defaultInfo = mNetCfgManager.GetDefaultTerm(3);
		MyLog.print("info:" + defaultInfo);
		list = AddrInfo.parsingAddrInfo(defaultInfo);
		if (list == null || list.size() != 1) {
			MyLog.print("GetDefaultTerm 3 error!");
			return -2;
		}
		String doorIp = list.get(0).getIp();
		MyLog.print("doorIp:" + doorIp);
		// ��ȡ���ڻ���ǰCODE��Ϣ
		long roomID = mNetCfgManager.Code2ID(getRoomCode());
		MyLog.print("roomID:" + roomID);
		// ��ȡ�µ�С�ſڻ�CODE��ת����ID
		if (!doorCode.startsWith("3") && doorCode.length() != 13) {
			MyLog.print("error 1");
			return -3;
		}
		MyLog.print("doorCode:" + doorCode);
		long doorID = mNetCfgManager.Code2ID(doorCode);
		MyLog.print("doorID:" + doorID);
		if (doorIp == null || roomID == 0 || doorID == 0) {
			MyLog.print("door code error");
			return -4;
		}
		int result = TalkManager.toDoorSetNum(doorIp, roomID, doorID);
		if (result == 0) {
			return 0;
		} else {
			return -5;
		}
	}
	
	/**
	 * @��̬��������setTime
	 * @���ܣ�����ʱ��
	 * @������String dateTime - "2013-10-23 11:28:02"
	 */
	private static void setTime(String dateTime) {
		boolean result = CommonUT.setSystemTime(dateTime);
		if (result) {
			mContext.sendBroadcast(new Intent(ACTION_UPDATE_TIME));
		}
	}

	/**
	 * @���ܣ���������ģʽ
	 * @���� int mode 0-�����ԣ�1-Ĭ�����ԣ�2-ҵ������
	 */
	public static boolean setMessageMode(int mode) {
		return DPDBHelper.setMessageMode(mode);
	}

	/**
	 * @���ܣ���ȡ����ģʽ
	 * @return 0-�����ԣ�1-Ĭ�����ԣ�2-ҵ������
	 */
	public static int getMessageMode() {
		return DPDBHelper.getMessageMode();
	}
	
	/**
	 * @���ܣ��������ܼҾ�ģʽ
	 * @���� int mode 1=�ڼң�2=���ޣ�3=�۲ͣ�4=Ӱ�ӣ�5=���֣�6=ȫ��
	 */
	public static void setSmartHomeMode(int mode) {
		DPDBHelper.setSmartHomeMode(mode);
		mContext.sendBroadcast(new Intent(ACTION_SMART_HOME_MODE));
		synchPhoneSmartHomeMode();
	}

	/**
	 * ��ȡ���ܼҾ�ģʽ
	 * @return 1=�ڼң�2=���ޣ�3=�۲ͣ�4=Ӱ�ӣ�5=���֣�6=ȫ��
	 */
	public static int getSmartHomeMode() {
		return DPDBHelper.getSmartHomeMode();
	}
	
	/**
	 * ��ȡ���ܼҾ�ģʽ
	 * @return Home, Night, Dining, Video, Disco, AllClose
	 */
	public static String getStringSmartHomeMode() {
		String smartHomeMode = null;
		switch (DPDBHelper.getSmartHomeMode()) {
			case 1:
				smartHomeMode = "Home";
				break;
			case 2:
				smartHomeMode = "Night";
				break;
			case 3:
				smartHomeMode = "Dining";
				break;
			case 4:
				smartHomeMode = "Video";
				break;
			case 5:
				smartHomeMode = "Disco";
				break;
			case 6:
				smartHomeMode = "AllClose";
				break;
			default:
				smartHomeMode = "Home";
				break;
		}
		return smartHomeMode;
	}

	/** �������ڻ������������� **/
	public static boolean setPsdProjectSetting(String projectPsd) {
		return DPDBHelper.setPsdProjectSetting(projectPsd);
	}

	/** ��ȡ���ڻ������������� */
	public static String getPsdProjectSetting() {
		return DPDBHelper.getPsdProjectSetting();
	}

	/** �������� */
	public static boolean setBrightness(int value) {
		return DPDBHelper.setBrightness(value);
	}
	
	/** ��ȡ���� */
	public static int getBrightness() {
		return DPDBHelper.getBrightness();
	}

	/** ����Աȶ� */
	public static boolean setContrast(int value) {
		return DPDBHelper.setContrast(value);
	}
	
	/** ��ȡ�Աȶ� */
	public static int getContrast() {
		return DPDBHelper.getContrast();
	}

	/** ���汥�Ͷ� */
	public static boolean setSaturability(int value) {
		return DPDBHelper.setSaturability(value);
	}
	
	/** ��ȡ���Ͷ� */
	public static int getSaturability() {
		return DPDBHelper.getSaturability();
	}

	/** ����ɫ�� */
	public static boolean setHue(int value) {
		return DPDBHelper.setHue(value);
	}
	
	/** ��ȡɫ�� */
	public static int getHue() {
		return DPDBHelper.getHue();
	}
	
	/** ��ȡ�Ƿ����ת�Ƶ��ֻ� */
	public static boolean getCallToPhone() {
		return DPDBHelper.getCallToPhone();
	}
	
	/** �����Ƿ����ת�Ƶ��ֻ� */
	public static void setCallToPhone(boolean isToPhone) {
		DPDBHelper.setCallToPhone(isToPhone);
	}
	
	private static void from(String path) {
		mFromFile = new File(path);
	}

	private static boolean to(String filePath) {
		boolean result = false;
		if (mFromFile == null || !mFromFile.exists()) {
			if (mFromFile != null) {
				MyLog.print(MyLog.ERROR, TAG, "�ļ�����ʧ�ܣ�mFromFile������ " 
						+ mFromFile.getAbsolutePath());
			} else {
				MyLog.print(MyLog.ERROR, TAG, "�ļ�����ʧ�ܣ�mFromFile null ");
			}
			return result;
		}
		InputStream inputStream = null;
		FileOutputStream outputStream = null;
		try {
			mToFile = new File(filePath);
			inputStream = new FileInputStream(mFromFile);
			outputStream = new FileOutputStream(mToFile);
			byte[] buffer = new byte[1024];
			int len;
			while ((len = inputStream.read(buffer)) > 0) {
				outputStream.write(buffer, 0, len);
			}
			outputStream.flush();
			outputStream.getFD().sync();
			MyLog.print(TAG, "�ļ������ɹ���" + filePath);
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			MyLog.print(MyLog.ERROR, TAG, "�ļ�����ʧ�ܣ�" + filePath);
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	// TODO-----------------------------�������-----------------------------
	
	public static int getTanTouNum() {
		return DPSafeService.tanTouNum;
	}
	
	/**
	 * ���밲�������ļ�(�ı�����ʱ��Ҫ���µ���)
	 */
	public static void loadAlarmFile() {
		if (DPSafeService.getInstance() != null) {
			DPSafeService.getInstance().loadAlarmFile();
		}
	}
	
	/**
	 * ����Ĭ�Ϸ���λ��(������Ч������1.�״�����APP��2.��ʼ��DPFunction֮ǰ)
	 * @param area
	 */
	public static void setDefaultAlarmArea(int[] area) {
		DPSafeService.setDefaultAlarmArea(area);
	}
	
	/**
	 * ����Ĭ�Ϸ�������(������Ч������1.�״�����APP��2.��ʼ��DPFunction֮ǰ)
	 * @param type
	 */
	public static void setDefaultAlarmType(int[] type) {
		DPSafeService.setDefaultAlarmType(type);
	}
	
	/**
	 * ��ȡ��ǰ����ģʽ
	 * @return UnSafe, Night, Home, Leave
	 */
	public static String getStringSafeMode() {
		String safeMode = null;
		switch (DPDBHelper.getSafeMode()) {
			case ConstConf.UNSAFE_MODE:
				safeMode = "UnSafe";
				break;
			case ConstConf.NIGHT_MODE:
				safeMode = "Night";
				break;
			case ConstConf.HOME_MODE:
				safeMode = "Home";
				break;
			case ConstConf.LEAVE_HOME_MODE:
				safeMode = "Leave";
				break;
			default:
				safeMode = "UnSafe";
				break;
		}
		return safeMode;
	}
	
	/**
	 * @���ܣ���ȡ����ģʽ
	 * 0-���ԣ�1-������2-ҹ�䣬3-�ڼң�4-���
	 * @��ע��Ĭ��Ϊ1-����ģʽ
	 */
	public static int getSafeMode() {
		return DPDBHelper.getSafeMode();
	}

	/**
	 * @���ܣ����ð���ģʽ
	 * @���� int mode 0-���ԣ�1-������2-ҹ�䣬3-�ڼң�4-���
	 */
	public static boolean setSafeMode(int mode) {
		return DPDBHelper.setSafeMode(mode);
	}

	/** ��ȡ��ǰ����λ�� */
	public static int[] getAlarmArea() {
		if (DPSafeService.getInstance() != null) {
			return DPSafeService.getInstance().alarmArea;
		} else {
			MyLog.print(TAG, DPSAFESERVICE_NULL);
			return null;
		}
	}

	/** ��ȡ��ǰ�������� */
	public static int[] getAlarmType() {
		if (DPSafeService.getInstance() != null) {
			return DPSafeService.getInstance().alarmType;
		} else {
			MyLog.print(TAG, DPSAFESERVICE_NULL);
			return null;
		}
	}

	/** ��ȡ���������ļ��еķ������� */
	public static List<AlarmNameInfo> getAlarmAreaNameList() {
		if (DPSafeService.getInstance() != null) {
			return DPSafeService.getInstance().alarmAreaNameList;
		} else {
			MyLog.print(TAG, DPSAFESERVICE_NULL);
			return null;
		}
	}

	/** ��ȡ���������ļ��еķ������� */
	public static List<AlarmTypeInfo> getAlarmTypeNameList() {
		if (DPSafeService.getInstance() != null) {
			return DPSafeService.getInstance().alarmTypeNameList;
		} else {
			MyLog.print(TAG, DPSAFESERVICE_NULL);
			return null;
		}
	}

	/** ��ȡ��ǰ̽ͷ״̬ */
	public static long getSafeState() {
		if (DPSafeService.getInstance() != null) {
			return DPSafeService.getInstance().oldState;
		} else {
			MyLog.print(TAG, DPSAFESERVICE_NULL);
			return 0;
		}
	}

	/** ��ȡָ������ģʽ������״̬ */
	public static List<AlarmInfo> getAlarmInfoList(int model) {
		if (DPSafeService.getInstance() != null) {
			return DPSafeService.getInstance().getAlarmInfoList(model);
		} else {
			MyLog.print(TAG, DPSAFESERVICE_NULL);
			return null;
		}
	}

	/**
	 * @���ܣ��ж��Ƿ����������ʱ������
	 * @��ע��priority 0-���塢1-������2-�̸�ú����3-�Ŵź���
	 */
	public static boolean isEmergency(int i) {
		if (DPSafeService.getInstance() != null) {
			return DPSafeService.getInstance().isEmergency(i);
		} else {
			MyLog.print(TAG, DPSAFESERVICE_NULL);
			return false;
		}
	}

	/**
	 * ����ȫ����������
	 * @param alarmArea ���������б�
	 */
	public static boolean setAlarmArea(int[] alarmArea) {
		synchronized (mutex) {
			if (DPSafeService.getInstance() != null) {
				return DPSafeService.getInstance().setAlarmArea(alarmArea);
			} else {
				MyLog.print(TAG, DPSAFESERVICE_NULL);
				return false;
			}
		}
	}

	/** �����ض����� */
	public static boolean setAlarmArea(int i, int alarmArea) {
		synchronized (mutex) {
			if (DPSafeService.getInstance() != null) {
				return DPSafeService.getInstance().setAlarmArea(i, alarmArea);
			} else {
				MyLog.print(TAG, DPSAFESERVICE_NULL);
				return false;
			}
		}
	}
	
	/**
	 * @���ܣ����ø���̽ͷ��������
	 * @param area
	 *            ���ֶ�Ӧ�����ƴ����ñ��ж�ȡ
	 * */
	public static boolean setSafeArea(int[] area) {
		return DPDBHelper.setSafeArea(area);
	}

	/** @���ܣ���ȡ����̽ͷ�������� */
	public static int[] getSafeArea() {
		return DPDBHelper.getSafeArea();
	}

	/**
	 * @���ܣ��޸����еķ�������
	 * @��ע���޸ĺ��ΪĬ��״̬����ʱ�����ͽ��ô˷���
	 * @�������������̸С�ú�������ͱ������ò�����ʱ����ʱ��
	 */
	public static boolean setAlarmType(int[] alarmType) {
		synchronized (mutex) {
			if (DPSafeService.getInstance() != null) {
				return DPSafeService.getInstance().setAlarmType(alarmType);
			} else {
				MyLog.print(TAG, DPSAFESERVICE_NULL);
				return false;
			}
		}
	}

	/**
	 * @���ܣ��޸ĵ�����������
	 * @����1��int i -�ڼ���̽ͷ ��0��ʼ
	 * @����2��int alarmType ��������
	 * @��ע���޸ĺ��ΪĬ��״̬����ʱ�����ͽ��ô˷���
	 * @�������������̸С�ú�������ͱ�������������ʱ����ʱ��
	 */
	public static boolean setAlarmType(int i, int alarmType) {
		synchronized (mutex) {
			if (DPSafeService.getInstance() != null) {
				return DPSafeService.getInstance().setAlarmType(i, alarmType);
			} else {
				MyLog.print(TAG, DPSAFESERVICE_NULL);
				return false;
			}
		}
	}
	
	/** ���ø���̽ͷ���� */
	public static boolean setSafeType(int[] type) {
		return DPDBHelper.setSafeType(type);
	}

	/** ��ȡ����̽ͷ���� */
	public static int[] getSafeType() {
		return DPDBHelper.getSafeType();
	}

	/**
	 * �޸����ڻ��İ���ģʽ��ͬʱ��д�����ݿ��У�
	 * @param isAuto true-���ڻ��Զ�ͬ������ģʽ, false-�ֶ��޸İ���ģʽ
	 */
	public static void changeSafeMode(int model, boolean isAuto) {
		synchronized (mutex) {
			if (DPSafeService.getInstance() != null) {
				DPSafeService.getInstance().changeSafeMode(model, isAuto);
			} else {
				MyLog.print(TAG, DPSAFESERVICE_NULL);
			}
		}
	}
	
	/**
	 * @���ܣ����ø�������ģʽ�µ�̽ͷ���������
	 * @����1��mode - ����ģʽ
	 * @����2��enable - ���û���� 2����λΪ1����
	 * @����ֵ��boolean false-ʧ�ܣ�true-�ɹ�
	 */
	public static boolean setSafeModeEnable(int mode, long enable) {
		return DPDBHelper.setSafeModeEnable(mode, enable);
	}

	/**
	 * @���ܣ���ȡ��������ģʽ�µ�̽ͷ���������
	 * @������mode - ����ģʽ
	 * @����ֵ��long - ���û���� 2����λΪ1����
	 */
	public static long getSafeModeEnable(int mode) {
		return DPDBHelper.getSafeModeEnable(mode);
	}

	/**
	 * �޸��û�ģʽ�µ� ����ģʽ��Ӧ�Ŀ���״̬
	 * 
	 * @param model
	 *            ģʽ ���� �ڼ� ���ģʽ
	 * @param enable
	 *            8�����صĿ���״̬
	 */
	public static void changeSafeEnable(int model, long enable) {
		synchronized (mutex) {
			if (DPSafeService.getInstance() != null) {
				DPSafeService.getInstance().changeSafeEnable(model, enable);
			} else {
				MyLog.print(TAG, DPSAFESERVICE_NULL);
			}
		}
	}

	/**
	 * ���÷����Ľӷ�(�����򳣱�)
	 * @param connection
	 */
	public static void changeSafeConnection(long connection) {
		synchronized (mutex) {
			if (DPSafeService.getInstance() != null) {
				DPSafeService.getInstance().changeSafeConnection(connection);
			} else {
				MyLog.print(TAG, DPSAFESERVICE_NULL);
			}
		}
	}
	
	/**
	 * @���ܣ����ð�������̽ͷ�ӷ�-�ߵ͵�ƽ
	 * @������long connection
	 * @���磺long connection = 0x1F ��2���Ƶ�1111 ��ô̽ͷ1-5�������պ�ʱ������̽ͷ6-8���գ���ʱ����
	 * @������̽ͷ�ӷ����ߵ͵�ƽ����0-���գ�1-����
	 */
	public static boolean setSefeConnection(long connection) {
		return DPDBHelper.setSefeConnection(connection);
	}

	/**
	 * @���ܣ���ȡ��������̽ͷ�ӷ�-�ߵ͵�ƽ
	 * @����ֵ��long - ����̽ͷ�ӷ�
	 * @��ע������ֵΪ 0x1F ��2���Ƶ�1111 ��ô̽ͷ1-5�������պ�ʱ������̽ͷ6-8���գ���ʱ����
	 * @������̽ͷ�ӷ����ߵ͵�ƽ����0-���գ�1-����
	 */
	public static long getSefeConnection() {
		return DPDBHelper.getSefeConnection();
	}

	/**
	 * ���ö�Ӧ����ģʽ�ĸ���������ʱʱ��
	 * 
	 * @param model
	 *            ����ģʽ
	 * @param time
	 *            �ϱ���ʱʱ��
	 */
	public static void changeAlarmDelayTime(int model, int[] time) {
		synchronized (mutex) {
			if (DPSafeService.getInstance() != null) {
				DPSafeService.getInstance().changeAlarmDelayTime(model, time);
			} else {
				MyLog.print(TAG, DPSAFESERVICE_NULL);
			}
		}
	}
	
	/**
	 * @���ܣ����ø�������ģʽ�µ�ÿ��̽ͷ�ı�����ʱʱ��
	 * @����1��mode - ����ģʽ
	 * @����2��int[] - ÿ��̽ͷ�ı�����ʱʱ��
	 * @����ֵ��boolean false-ʧ�ܣ�true-�ɹ�
	 */
	public static boolean setAlarmDelayTime(int mode, int[] time) {
		return DPDBHelper.setAlarmDelayTime(mode, time);
	}

	/**
	 * @���ܣ���ȡ��������ģʽ�µ�ÿ��̽ͷ�ı�����ʱʱ��
	 * @������mode - ����ģʽ
	 * @����ֵ��int[] - ÿ��̽ͷ�ı�����ʱʱ��
	 */
	public static int[] getAlarmDelayTime(int mode) {
		return DPDBHelper.getAlarmDelayTime(mode);
	}

	/**
	 * @return �Ƿ����ڱ���
	 */
	public static boolean getAlarming() {
		return mIsAlarming;
	}
	
	/**
	 * �������ڱ���
	 */
	public static void setAlarming() {
		mIsAlarming = true;
	}

	/**
	 * �������
	 * @param isHolding
	 *            �ǲ���Ю�ֱ���
	 */
	public static void disAlarm(boolean isHolding) {
		mIsAlarming = false;
		DPSafeService.getInstance().releaseAlarmRing();
		DPSafeService.getInstance().changeSafeMode(ConstConf.UNSAFE_MODE, false);
		if (isHolding) {
			TalkManager.toManageAlarm(
					CommonUT.formatTime(System.currentTimeMillis()), 99, 19, 0);
		} else {
			TalkManager.toManageDisAlarm();
		}
	}

	/** ͬ�����ڷֻ�����ģʽ */
	public static void synchRoomSafeMode() {
		synchronized (mutex) {
			if (mNetCfgManager == null) {
				return;
			}
			String roomCode = getRoomCode()
					.substring(1, getRoomCode().length() - 2);
			String roomInfo = mNetCfgManager.RoomGet(roomCode);
			ArrayList<AddrInfo> list = AddrInfo.parsingAddrInfo(roomInfo);
			if (list != null && !list.isEmpty()) {
				for (int i = 0; i < list.size(); i++) {
					if (getRoomCode().equals(list.get(i).getCode())) {
						continue;
					}
					int ret = TalkManager.synchRoomSafeMode(list.get(i).getIp());
					MyLog.print("synchRoomSafeMode = " + ret);
				}
			}
		}
	}
	
	/**
	 * @���ܣ����ò���ʱ��
	 * @���� int time 0-300��
	 */
	public static boolean setProtectionDelayTime(int time) {
		return DPDBHelper.setProtectionDelayTime(time);
	}

	/** @���ܣ���ȡ����ʱ�� */
	public static int getProtectionDelayTime() {
		return DPDBHelper.getProtectionDelayTime();
	}

	/**
	 * ���氲������
	 * 
	 * @param holding
	 *            �Ƿ���Ю������
	 * @param pwd
	 *            ������
	 **/
	public static boolean setSafePassword(boolean holding, String pwd) {
		return DPDBHelper.setSafePassword(holding, pwd);
	}
	
	/**
	 * ��ȡ��������
	 * 
	 * @param holding
	 *            �Ƿ���Ю������
	 **/
	public static String getSafePassword(boolean holding) {
		return DPDBHelper.getSafePassword(holding);
	}
	
	/**
	 * @���ܣ����Ӱ�����¼�����ݿ���
	 */
	public static void addSafeModeLog(SafeModeInfo info) {
		DPDBHelper.addSafeModeLog(info);
	}

	/**
	 * @���ܣ�ɾ�����ݿ���ָ��ID�İ�����¼
	 * @������int db_id - ���ݿ����Զ����ɵ�_id
	 */
	public static void deleteSafeModeLog(int db_id) {
		DPDBHelper.deleteSafeModeLog(db_id);
	}

	/**
	 * @���ܣ�ɾ�����в�����¼
	 */
	public static void deleteAllSafeModeLog() {
		DPDBHelper.deleteAllSafeModeLog();
	}

	/**
	 * ��ȡ������¼
	 */
	public static ArrayList<SafeModeInfo> getSafeModeInfoList() {
		return DPDBHelper.getSafeModeInfoList();
	}

	/**
	 * @���ܣ����ӱ�����¼�����ݿ���
	 */
	public static void addAlarmLog(AlarmLog info) {
		DPDBHelper.addAlarmLog(info);
	}
	
	/**
	 * @���ܣ��޸ı�����¼�����ݿ���
	 */
	public static void modifyAlarmLog(AlarmLog info) {
		DPDBHelper.modifyAlarmLog(info);
	}

	/**
	 * @���ܣ�ɾ�����ݿ���ָ��ID�ı�����¼
	 * @������int db_id - ���ݿ����Զ����ɵ�_id
	 */
	public static void deleteAlarmLog(int db_id) {
		DPDBHelper.deleteAlarmLog(db_id);
	}

	/**
	 * @���ܣ�ɾ�����б�����¼
	 */
	public static void deleteAllAlarmLog() {
		DPDBHelper.deleteAllAlarmLog();
	}

	/** ��ȡ������¼ */
	public static ArrayList<AlarmLog> getAlarmLogList() {
		return DPDBHelper.getAlarmLogList();
	}
	
	/**
	 * @���ܣ����ӱ���¼�����ݿ���
	 */
	public static void addAlarmVideo(AlarmVideo info) {
		DPDBHelper.addAlarmVideo(info);
	}
	
	/**
	 * @���ܣ�ɾ�����ݿ���ָ��ID�ı���¼��
	 * @������int db_id - ���ݿ����Զ����ɵ�_id
	 */
	public static void deleteAlarmVideo(int db_id) {
		DPDBHelper.deleteAlarmVideo(db_id);
	}

	/**
	 * @���ܣ�ɾ�����б���¼��
	 */
	public static void deleteAlarmVideo() {
		DPDBHelper.deleteAllAlarmVideo();
	}
	
	/** ��ȡ����¼�� */
	public static ArrayList<AlarmVideo> getAlarmVideoList() {
		return DPDBHelper.getAlarmVideoList();
	}
	
	// TODO-----------------------------�Խ����-----------------------------
	
	public static JniPhoneClass getJniPhoneClass() {
		return mJniPhoneClass;
	}
	
	public static ArrayList<AddrInfo> getMonitorList() {
		if (mNetCfgManager == null) {
			return null;
		}
		String info = mNetCfgManager.MonitorGet(null);
		if (info == null) {
			MyLog.print("DPGetMonitorList error.");
			return null;
		}
		int num = 0;
		info = getXmlVal("netcfg", info);
		if (info == null) {
			MyLog.print("DPGetMonitorList error.Do not find <netcfg>");
			return null;
		}
		String strnum = getXmlVal("num", info);
		if (strnum == null) {
			MyLog.print("DPGetMonitorList error.Do not find <num>");
			return null;
		}
		num = Integer.valueOf(strnum).intValue();
		if (num == 0) {
			MyLog.print("DPGetMonitorList error.num is 0");
			return null;
		}
		return AddrInfo.parsingAddrInfo(info);
	}

	/**
	 * @���ܣ���ȡ��Ա������ŵ����е�Ԫ�ſڻ���IP��Ϣ�б�
	 */
	public static ArrayList<AddrInfo> getCellSeeList() {
		if (mNetCfgManager == null) {
			return null;
		}
		String info = mNetCfgManager.CellDoorGet(null);
		if (info == null) {
			return null;
		}
		int num = 0;
		info = getXmlVal("netcfg", info);
		if (info == null) {
			MyLog.print("DPGetCellSeeList error.Do not find <netcfg>");
			return null;
		}
		String strnum = getXmlVal("num", info);
		if (strnum == null) {
			MyLog.print("DPGetCellSeeList error.Do not find <num>");
			return null;
		}
		num = Integer.valueOf(strnum).intValue();
		if (num == 0) {
			MyLog.print("DPGetCellSeeList error.num is 0");
			return null;
		}
		return AddrInfo.parsingAddrInfo(info);
	}

	/**
	 * @���ܣ���ȡ��Ա������ŵ����Єe���ſڻ���IP��Ϣ�б�
	 */
	public static ArrayList<AddrInfo> getSecSeeList() {
		String info = mNetCfgManager.SecDoorGet(null);
		if (info == null) {
			return null;
		}
		int num = 0;
		info = getXmlVal("netcfg", info);
		if (info == null) {
			MyLog.print("DPGetSecSeeList error.Do not find <netcfg>");
			return null;
		}
		String strnum = getXmlVal("num", info);
		if (strnum == null) {
			MyLog.print("DPGetSecSeeList error.Do not find <num>");
			return null;
		}
		num = Integer.valueOf(strnum).intValue();
		if (num == 0) {
			MyLog.print("DPGetSecSeeList error.num is 0");
			return null;
		}
		return AddrInfo.parsingAddrInfo(info);
	}

	/**
	 * @���ܣ���ȡ��Ա������ŵ����д�ڻ���IP��Ϣ�б�
	 */
	public static ArrayList<AddrInfo> getAreaDoorSeeList() {
		String info = mNetCfgManager.AreadoorGet(null);
		if (info == null) {
			return null;
		}
		int num = 0;
		info = getXmlVal("netcfg", info);
		if (info == null) {
			MyLog.print("DPGetSecSeeList error.Do not find <netcfg>");
			return null;
		}
		String strnum = getXmlVal("num", info);
		if (strnum == null) {
			MyLog.print("DPGetSecSeeList error.Do not find <num>");
			return null;
		}
		num = Integer.valueOf(strnum).intValue();
		if (num == 0) {
			MyLog.print("DPGetSecSeeList error.num is 0");
			return null;
		}
		return AddrInfo.parsingAddrInfo(info);
	}
	
	/**
	 * @��������isCanCallIn
	 * @�������յ��º�������
	 * @���ܣ��ж��Ƿ���Ժ���
	 * @����1��int CallSessionID - ID
	 * @����2��String code - ����ķ���
	 * @����ֵ��null�������Ϣ
	 */
	public static CallInfomation isCanCallIn(int CallSessionID, String code) {
		// change by ZhengZhiying
		CallInfomation callInfo = new CallInfomation();
		callInfo.setSessionID(CallSessionID);
		callInfo.setStartTime();
		callInfo.setIsHangUp(false);
		callInfo.setRemoteCode(code);
		callInfo.setType(CallInfomation.CALL_IN_UNACCEPT);
		if (mCallOutSessionIDList.size() == 0 && mCallInSessionIDList.size() == 0
				&& CallSessionID > 0) {
			mCallInSessionIDList.add(callInfo);
			return callInfo;
		}
		mCallInSessionIDList.add(callInfo);
		return null;
	}

	/**
	 * @���ܣ����ж�Ӧ����
	 * @������String - RoomNum ����
	 * @����ֵ��=0-�ɹ���>0-�к�������δ�Ҷϲ��ܺ��С�<0-ʧ��
	 * @��ע������ RoomNum�ĳ��ȿ��Բ���
	 * @���磺1��01-����1�����ڷֻ���0101��101-���б���Ԫ0101�����зֻ�,020101��20101-���б���2��Ԫ0101�ҵ����зֻ�
	 */
	public static int callOut(String RoomNum) {
		synchronized (mutex) {
			if (mCallOutSessionIDList.size() > 0) {
				return 1;
			}
			if (mCallInSessionIDList.size() > 0) {
				return 2;
			}
			if (RoomNum != null) {
				int len = RoomNum.length();
				if (len != 0) {
					String localCode = getRoomCode();
					MyLog.print("localCode = " + localCode);
					if (localCode == null) {
						return -1;
					}
					if (mNetCfgManager == null) {
						return -1;
					}
					if (len < 3) {
						String code2 = new String(localCode.substring(0,
								localCode.length() - RoomNum.length()));
						RoomNum = code2.concat(RoomNum);
					}
					len = RoomNum.length();
					int num = 0;
					ArrayList<AddrInfo> list = null;
					String roomInfo = null;
					boolean bFull = false;
					if (len == localCode.length()) {
						roomInfo = mNetCfgManager.TermGet(RoomNum);
						bFull = true;
					} else {
						roomInfo = mNetCfgManager.RoomGet(RoomNum);
					}
					if (roomInfo == null) {
						MyLog.print("do not find this num");
						return -2;
					}
					roomInfo = getXmlVal("netcfg", roomInfo);
					if (roomInfo == null) {
						return -3;
					}
					String strnum = getXmlVal("num", roomInfo);
					if (strnum == null) {
						return -4;
					}
					num = Integer.valueOf(strnum).intValue();
					if (num == 0) {
						MyLog.print("do not find this num");
						return -5;
					}
					list = AddrInfo.parsingAddrInfo(roomInfo);
					if (!bFull && list != null && !list.isEmpty()) {
						RoomNum = new String(list.get(0).getCode()
								.substring(0, localCode.length() - 2));
					}
					if (localCode.startsWith(RoomNum)) {
						MyLog.print("can not call self");
						return -6;
					}
					if (!callOut(list)) {
						MyLog.print("call error");
						return -7;
					}
				}
			}
			return 0;
		}
	}
	
	/**
	 * @��������callOut
	 * @���ܣ����ж���豸
	 * @������ArrayList<AddrInfo> list - ����豸��IP��Ϣ�б�
	 * @����ֵ��boolean true-�ɹ�,false-ʧ��
	 */
	private static boolean callOut(ArrayList<AddrInfo> list) {
		synchronized (mutex) {
			int num = 0;
			if (list != null && !list.isEmpty()) {
				int sessionID = 0;
				Iterator<AddrInfo> iterator = list.iterator();
				while (iterator.hasNext()) {
					AddrInfo addrInfo = iterator.next();
					sessionID = mJniPhoneClass.CallOut(addrInfo.getCode(),
							addrInfo.getIp());
					MyLog.print("DPCallOut sessionid = " + sessionID
							+ addrInfo.toString());
					if (sessionID > 0) {
						num++;
						CallInfomation callInfo = new CallInfomation();
						callInfo.setSessionID(sessionID);
						callInfo.setStartTime();
						callInfo.setIsHangUp(false);
						callInfo.setRemoteCode(addrInfo.getCode());
						callInfo.setType(CallInfomation.CALL_OUT_UNACCEPT);
						mCallOutSessionIDList.add(callInfo);
						MyLog.print("calloutSessionIdList size="
								+ mCallOutSessionIDList.size());
					}
				}
			}
			return num > 0;
		}
	}

	/**
	 * @���ܣ��������еĹ�������
	 * @����ֵ��=0-�ɹ���>0-�к�������δ�Ҷϲ��ܺ��С�<0-ʧ��
	 * @��ע�����ֻ�赥������1�Ź���������ʹ�� int callOut(String RoomNum)
	 */
	public static int callManager() {
		synchronized (mutex) {
			if (mCallOutSessionIDList.size() > 0) {
				return 1;
			}
			if (mCallInSessionIDList.size() > 0) {
				return 2;
			}
			if (mNetCfgManager == null) {
				return -1;
			}
			String managerinfo = mNetCfgManager.ManagerGet(null);
			if (managerinfo == null) {
				return -1;
			}
			int num = 0;
			ArrayList<AddrInfo> list = null;
			managerinfo = getXmlVal("netcfg", managerinfo);
			if (managerinfo == null) {
				return -2;
			}
			String strnum = getXmlVal("num", managerinfo);
			if (strnum == null) {
				return -3;
			}
			num = Integer.valueOf(strnum).intValue();
			if (num == 0) {
				MyLog.print("do not find this num");
				return -4;
			}
			list = AddrInfo.parsingAddrInfo(managerinfo);
			if (list == null || list.isEmpty()) {
				return -5;
			}
			if (callOut(list)) {
				return 0;
			}
			return -6;
		}
	}
	
	/**
	 * @���ܣ��������еı����ֻ�
	 * @����ֵ��=0-�ɹ���>0-�к�������δ�Ҷϲ��ܺ��С�<0-ʧ��
	 * @��ע�����ֻ�赥��������ʹ�� int callOut(String RoomNum)
	 */
	public static int callSecurity() {
		synchronized (mutex) {
			if (mCallOutSessionIDList.size() > 0) {
				return 1;
			}
			if (mCallInSessionIDList.size() > 0) {
				return 2;
			}
			if (mNetCfgManager == null) {
				return -1;
			}
			String info = mNetCfgManager.GuardGet(null);
			if (info == null) {
				MyLog.print("info is null");
				return -1;
			}
			int num = 0;
			ArrayList<AddrInfo> list = null;
			info = getXmlVal("netcfg", info);
			if (info == null) {
				return -2;
			}
			String strnum = getXmlVal("num", info);
			if (strnum == null) {
				return -3;
			}
			num = Integer.valueOf(strnum).intValue();
			if (num == 0) {
				MyLog.print("do not find this num");
				return -4;
			}
			list = AddrInfo.parsingAddrInfo(info);
			if (list == null || list.isEmpty()) {
				return -5;
			}
			if (callOut(list)) {
				return 0;
			}
			return -6;
		}
	}

	/**
	 * @��������seeDoor
	 * @���ܣ������ſڻ�
	 * @������AddrInfo info -�ſڻ� ��Ϣ
	 * @����ֵ��boolean true-�ɹ�,false-ʧ��
	 */
	public static int seeDoor(AddrInfo info) {
		synchronized (mutex) {
			if (mSeeInfo == null) {
				return 0;
			}
			if (mSeeInfo.isHangUp()) {
				int sessionID = mJniPhoneClass.Monitor(info.getCode(),
						info.getIp());
				if (sessionID > 0) {
					mSeeInfo = new CallInfomation();
					mSeeInfo.setIsHangUp(false);
					mSeeInfo.setRemoteCode(info.getCode());
					mSeeInfo.setType(CallInfomation.SEE_UNACCEPT);
					mSeeInfo.setStartTime();
					mSeeInfo.setSessionID(sessionID);
				}
			} else {
				MyLog.print("have other see.seeSessionID = "
						+ mSeeInfo.getSessionID());
			}
			return mSeeInfo.getSessionID();
		}
	}

	/**
	 * @��������seeHangUp
	 * @���ܣ��Ҷϵ�ǰ���ӵ��ſڻ�
	 */
	public static void seeHangUp() {
		synchronized (mutex) {
			if (!mSeeInfo.isHangUp()) {
				mSeeInfo.setEndTime();
				mSeeInfo.setIsHangUp(true);
				DPDBHelper.addCallLog(mSeeInfo);
				mJniPhoneClass.HangUp(mSeeInfo.getSessionID());
			}
		}
	}

	/**
	 * @��������openLock
	 * @���ܣ�֪ͨ�ſڻ�����
	 * @������String doorCode-�ſڻ�����
	 * @����ֵ��boolean true-�ɹ�,false-ʧ��
	 */
	public static boolean openLock(String doorCode) {
		synchronized (mutex) {
			// �ж��Ƿ��ſڻ�����
			if (doorCode == null) {
				return false;
			} 
			Iterator<CallInfomation> iterator = mCallInSessionIDList.iterator();
			while (iterator.hasNext()) {
				CallInfomation callinfo = iterator.next();
				if (callinfo.getRemoteCode() != null
						&& doorCode.equals(callinfo.getRemoteCode())) {
					if (callinfo.isDoor()) {
						if (mNetCfgManager == null) {
							return false;
						}
						String info = mNetCfgManager.TermGet(doorCode);
						if (info != null) {
							ArrayList<AddrInfo> list = AddrInfo
									.parsingAddrInfo(info);
							if (list != null && list.size() == 1) {
								if (TalkManager.toDoorOpenLock(list.get(0)
										.getIp(), getRoomCode()) == 0) {
									callinfo.setIsOpenLock(true);
									return true;
								}
							}
						}
					}
				}
			}
			return false;
		}
	}
	
	/**
	 * �ֻ��Լ�����
	 * @param doorCode
	 * @return
	 */
	public static boolean phoneopenlock (String doorCode) {
		CallInfomation callinfo = new CallInfomation();
		String info = mNetCfgManager.TermGet(doorCode);
		if (info != null) {
			ArrayList<AddrInfo> list = AddrInfo
					.parsingAddrInfo(info);
			if (list != null && list.size() == 1) {
				if (TalkManager.toDoorOpenLock(list.get(0)
						.getIp(), getRoomCode()) == 0) {
					callinfo.setIsOpenLock(true);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @���ܣ����Ự��ʱ
	 */
	public static void checkCallTime() {
		synchronized (mutex) {
			if (mCallInSessionIDList.size() > 0) {
				Iterator<CallInfomation> iterator_in = mCallInSessionIDList.iterator();
				while (iterator_in.hasNext()) {
					CallInfomation callInfo = iterator_in.next();
					if (callInfo.getType() == CallInfomation.CALL_IN_UNACCEPT) {
						// ���ڻ��������ڻ�������ʱ����
						// ���������ڻ�������ʱ����,���ǶԷ����к�������ᵼ�����ڻ�һֱ���������
						if (DPFunction.code2Type(callInfo.getRemoteCode()) == 1) {
							if (Math.abs(System.currentTimeMillis()
									- callInfo.getStartTime()) > ConstConf.RING_TIMEOUT + 2000) {
								Intent intent = new Intent();
								intent.putExtra("SessionID", callInfo.getSessionID());
								intent.putExtra("MsgType", JniPhoneClass.MSG_RING_TIMEOUT);
								intent.putExtra("code", callInfo.getRemoteCode());
								intent.setAction(CallReceiver.CALL_IN_ACTION);
								mContext.sendBroadcast(intent);
							}
						}
						if (Math.abs(System.currentTimeMillis()
								- callInfo.getStartTime()) > ConstConf.RING_TIMEOUT) {
							/** �������峬ʱӦ���ɶԷ���⣬�����������������ģʽ�����Զ��������������� */
							if (callInfo.isDoor()
									&& callInfo.getAcceptTime() == 0) {
								Intent intent = new Intent();
								intent.putExtra("SessionID", callInfo.getSessionID());
								intent.putExtra("MsgType", JniPhoneClass.MSG_RING_TIMEOUT);
								intent.putExtra("code", callInfo.getRemoteCode());
								intent.setAction(CallReceiver.CALL_IN_ACTION);
								mContext.sendBroadcast(intent);
							}
						}
					} else if (callInfo.getType() == CallInfomation.CALL_IN_ACCEPT) {
						if (Math.abs(System.currentTimeMillis()
								- callInfo.getAcceptTime()) > ConstConf.TALK_TIMEOUT) {
							Intent intent = new Intent();
							intent.putExtra("SessionID", callInfo.getSessionID());
							intent.putExtra("MsgType", JniPhoneClass.MSG_TALK_TIMEOUT);
							intent.putExtra("code", callInfo.getRemoteCode());
							intent.setAction(CallReceiver.CALL_IN_ACTION);
							mContext.sendBroadcast(intent);
							callHangUp(callInfo.getSessionID());
							iterator_in = mCallInSessionIDList.iterator();
						}
					}
				}
			} else if (mCallOutSessionIDList.size() > 0) {
				Iterator<CallInfomation> iterator_out = mCallOutSessionIDList
						.iterator();
				while (iterator_out.hasNext()) {
					CallInfomation callInfo = iterator_out.next();
					if (callInfo.getType() == CallInfomation.CALL_OUT_UNACCEPT) {
						if (Math.abs(System.currentTimeMillis()
								- callInfo.getStartTime()) > ConstConf.RING_TIMEOUT) {
							Intent intent = new Intent();
							intent.putExtra("SessionID", callInfo.getSessionID());
							intent.putExtra("MsgType", JniPhoneClass.MSG_RING_TIMEOUT);
							intent.putExtra("code", callInfo.getRemoteCode());
							intent.setAction(CallReceiver.CALL_OUT_ACTION);
							mContext.sendBroadcast(intent);
							callHangUp(callInfo.getSessionID());
							iterator_out = mCallOutSessionIDList.iterator();
						}
					} else if (callInfo.getType() == CallInfomation.CALL_OUT_ACCEPT) {
						if (Math.abs(System.currentTimeMillis()
								- callInfo.getAcceptTime()) > ConstConf.TALK_TIMEOUT) {
							Intent intent = new Intent();
							intent.putExtra("SessionID", callInfo.getSessionID());
							intent.putExtra("MsgType", JniPhoneClass.MSG_TALK_TIMEOUT);
							intent.putExtra("code", callInfo.getRemoteCode());
							intent.setAction(CallReceiver.CALL_OUT_ACTION);
							mContext.sendBroadcast(intent);
							callHangUp(callInfo.getSessionID());
							iterator_out = mCallOutSessionIDList.iterator();
						}
					}
				}
			} else if (!getSeeInfo().isHangUp()
					&& getSeeInfo().getType() == CallInfomation.SEE_ACCEPT) { // ���ӳ�ʱ
				long time = Math.abs(System.currentTimeMillis()
						- getSeeInfo().getAcceptTime());
				if (time > ConstConf.MONITOR_TIMEOUT) {
					Intent intent = new Intent();
					intent.putExtra("SessionID", getSeeInfo().getSessionID());
					intent.putExtra("MsgType", JniPhoneClass.MSG_MONITOR_TIMEOUT);
					intent.putExtra("code", getSeeInfo().getRemoteCode());
					intent.setAction(CallReceiver.SEE_ACTION);
					mContext.sendBroadcast(intent);
					DPFunction.seeHangUp();
				}
			}
		}
	}
	
	/**
	 * @���ܣ���ȡ������ǰ������Ϣ
	 */
	public static CallInfomation getSeeInfo() {
		return mSeeInfo;
	}
	
	/**
	 * @���ܣ���ȡ������ǰ�����Ự�б�
	 */
	public static List<CallInfomation> getCallOutList() {
		if (mCallOutSessionIDList == null) {
			return null;
		} else {
			return mCallOutSessionIDList;
		}
	}

	/**
	 * @���ܣ���ȡ������ǰ�����Ự�ĸ���
	 */
	public static int getCallOutSize() {
		if (mCallOutSessionIDList == null) {
			return 0;
		} else {
			return mCallOutSessionIDList.size();
		}
	}
	
	/**
	 * @���ܣ���ȡ������ǰ����Ự�б�
	 */
	public static List<CallInfomation> getCallInList() {
		if (mCallInSessionIDList == null) {
			return null;
		} else {
			return mCallInSessionIDList;
		}
	}

	/**
	 * @���ܣ���ȡ������ǰ����Ự�ĸ���
	 */
	public static int getCallInSize() {
		if (mCallInSessionIDList == null) {
			return 0;
		} else {
			return mCallInSessionIDList.size();
		}
	}
	
	/**
	 * @���ܣ����öԷ�ͼ���ڱ�����Ļ����ʾ����
	 * @����1��int CallSessionID - ID
	 * @����2-5��int xywh - ��ʼλ�úͿ��
	 * @����ֵ��boolean true-�ɹ���false-ʧ��
	 */
	public static boolean setVideoDisplayArea(int CallSessionID, int x, int y,
			int w, int h) {
		synchronized (mutex) {
			if (mJniPhoneClass != null && CallSessionID > 0) {
				return mJniPhoneClass.SetVideoDisplayArea(CallSessionID, 0, x,
						y, w, h);
			}
			return false;
		}
	}

	/**
	 * @���ܣ����öԷ�ͼ����TV��Ļ����ʾ����
	 * @����1��int CallSessionID - ID
	 * @����2-5��int xywh - ��ʼλ�úͿ��
	 * @����ֵ��boolean true-�ɹ���false-ʧ��
	 */
	public static boolean setTVDisplayArea(int CallSessionID, int x, int y,
			int w, int h) {
		synchronized (mutex) {
			if (mJniPhoneClass != null && CallSessionID > 0) {
				return mJniPhoneClass.SetTVoutDisplayArea(CallSessionID, 0, x,
						y, w, h);
			}
			return false;
		}
	}

	/**
	 * @���ܣ�����ͨ������
	 * @����1��int CallSessionID - ID
	 * @����2��int Percent - �����ٷֱ�
	 * @����ֵ��boolean true-�ɹ���false-ʧ��
	 */
	public static boolean setAudioVolume(int CallSessionID, int Percent) {
		synchronized (mutex) {
			if (mJniPhoneClass != null && CallSessionID > 0) {
				return mJniPhoneClass.SetAudioVolume(CallSessionID, Percent);
			}
			return false;
		}
	}

	/**
	 * @���ܣ����Ʊ�����Ƶ�Ƿ�Է��ɼ� ���ڵ���SetVideoDisplayAreaǰһ�е������������ó�ʼĬ��ֵ��Ȼ���û�����Ƶ���ذ�ťʱҲ����
	 * @����1��int CallSessionID - ID
	 * @����2��boolean enable true-�ɼ�,false-���ɼ�
	 * @����ֵ��boolean false-ʧ��,true-�ɹ�
	 */
	public static boolean setLocalVideoVisable(int CallSessionID, boolean enable) {
		synchronized (mutex) {
			if (mJniPhoneClass != null && CallSessionID > 0) {
				return mJniPhoneClass.SetLocalVideoVisable(CallSessionID, 0,
						enable);
			}
			return false;
		}
	}

	/**
	 * @��������accept
	 * @���ܣ�����ָ���Ự
	 * @����1��int CallSessionID - ID
	 * @����2��int acceptType 0 - ���ֶ�������1 - ��ʱ�Զ�����,2 - �ֻ�����
	 * @����ֵ��boolean true-�����ɹ���false-����ʧ��
	 * @��ע���Զ������������Ӧ����Ƶ�ļ�·���������ļ�·��
	 */
	public static boolean accept(int CallSessionID, int acceptType) {
		synchronized (mutex) {
			MyLog.print("accept acceptType = " + acceptType);
			if (CallSessionID > 0) {
				Iterator<CallInfomation> iterator = mCallInSessionIDList.iterator();
				while (iterator.hasNext()) {
					CallInfomation callInfo = iterator.next();
					if (CallSessionID == callInfo.getSessionID()
							&& callInfo.getAcceptTime() == 0) {
						if (mJniPhoneClass.Accept(CallSessionID)) {
							callInfo.setAcceptTime();
							if (!callInfo.isDoor()) {
								// ��������ʾ��������ͷͼ����Է�
								setLocalVideoVisable(CallSessionID, false);
								setVideoDisplayArea(CallSessionID,
										videoAreaCallInUnit[0],
										videoAreaCallInUnit[1],
										videoAreaCallInUnit[2],
										videoAreaCallInUnit[3]);
							}
							if (acceptType == 1) {
								callInfo.setType(CallInfomation.CALL_IN_UNACCEPT);
								boolean ret = setMessageModeFilePath(callInfo);
								MyLog.print("atuo accept ret=" + ret);
								toPhoneHangUp();
								isCallAccept = true;
							} else if (acceptType == 0) {
								callInfo.setType(CallInfomation.CALL_IN_ACCEPT);
								toPhoneHangUp();
								isCallAccept = true;
							} else {
								callInfo.setType(CallInfomation.CALL_IN_ACCEPT);
							}
							return true;
						}
					}
				}
			}
			return false;
		}
	}
	
	/**
	 * @��������setMessageModeFilePath
	 * @���ܣ������ſڻ����б�����ʱ��Ӧ����Ƶ�ļ�·���������ļ�·��
	 * @������CallInfo info - �Ự��Ϣ
	 * @����ֵ��boolean false-ʧ��,true-�ɹ�
	 */
	private static boolean setMessageModeFilePath(CallInfomation info) {
		if (info != null) {
			String palyFile = null;
			int mode = DPDBHelper.getMessageMode();
			if (mode == 1) {
				/** Ĭ������ */
				palyFile = new String(ConstConf.DEFAULT_LEAVE_PATH);
			} else if (mode == 2) {
				/** ҵ������ */
				palyFile = new String(ConstConf.USER_LEAVE_PATH);
			} else {
				return false;
			}
			File play = new File(palyFile);
			if (play.exists()) {
				/**
				 * ���ݻỰ��ʼʱ�������������¼���ļ��� ���ļ������в����У�����Ҫ�滻��-
				 */
				File destDir = new File(ConstConf.VISIT_PATH);
				if (!destDir.exists()) {
					destDir.mkdirs();
				}
				String audioName = ConstConf.VISIT_PATH
						+ File.separator
						+ CommonUT.formatTime(info.getStartTime())
								.replaceAll(":", "-") + ".wav";
				return mJniPhoneClass.SetFilePlayMode(info.getSessionID(),
						true, palyFile)
						&& mJniPhoneClass.SetFileRecordMode(
								info.getSessionID(), true, audioName);
			} else {
				MyLog.print("leave message file miss");
			}
		}
		return false;
	}
	
	/**
	 * @���ܣ�����ͼ��
	 * @������CallInfo callInfo - �Ự��Ϣ
	 */
	private static void saveImage(CallInfomation callInfo) {
		synchronized (mutex) {
			if (callInfo.getType() == CallInfomation.CALL_IN_UNACCEPT
					&& callInfo.isDoor()) {
				byte[] buffer = new byte[1280 * 720 / 4];
				int temp = mJniPhoneClass.CaptureVideoImage(
						callInfo.getSessionID(), 0, buffer);
				if (temp != 0) {
					FileOutputStream outputStream = null;
					try {
						File destDir = new File(ConstConf.VISIT_PATH);
						if (!destDir.exists()) {
							destDir.mkdirs();
						}
						String imageName = new String(destDir.getAbsolutePath()
								+ File.separator
								+ CommonUT.formatTime(callInfo.getStartTime())
										.replaceAll(":", "-") + ".jpg");
						MyLog.print("saveImage name = " + imageName);
						outputStream = new FileOutputStream(imageName);
						outputStream.write(buffer);
						outputStream.flush();
						outputStream.getFD().sync();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						if (outputStream != null) {
							try {
								outputStream.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				} else {
					MyLog.print("saveImage error:" + temp);
				}
			}
		}
	}

	/**
	 * @�������յ��Է����������
	 * @���ܣ��Ҷϳ�ȥָ���Ự��ĻỰ
	 * @����1��int CallSessionID - ID
	 * @����2��boolean isAutoAccept - �Ƿ�ʱ�Զ�������true-��ʱ�Զ�����
	 * @����ֵ��boolean true-�����ɹ���false-����ʧ��
	 */
	public static void callOutOtherHangUp(int callSessionID) {
		synchronized (mutex) {
			MyLog.print("callOutOtherHangUp CallSessionID = " + callSessionID);
			if (callSessionID > 0 && mCallOutSessionIDList.size() > 0) {
				int sessionID[] = new int[mCallOutSessionIDList.size()];
				Arrays.fill(sessionID, 0);
				for (int i = 0; i < mCallOutSessionIDList.size(); i++) {
					CallInfomation callInfo = mCallOutSessionIDList.get(i);
					if (callSessionID == callInfo.getSessionID()) {
						callInfo.setAcceptTime();
						callInfo.setType(CallInfomation.CALL_OUT_ACCEPT);
					} else {
						sessionID[i] = callInfo.getSessionID();
					}
				}
				for (int i = 0; i < sessionID.length; i++) {
					if (sessionID[i] != 0) {
						callHangUp(sessionID[i]);
					}
				}
			}
		}
	}
	
	/**
	 * @��������callHangUp
	 * @���ܣ��Ҷ�ָ���Ự
	 * @����1��int CallSessionID - ID
	 * @����ֵ��String ָ���Ự�ĶԷ�����
	 * @��ע���ſڻ��������û�н���������»ᱣ��һ֡ͼ���Ҷ�
	 */
	public static String callHangUp(int callSessionID) {
		synchronized (mutex) {
			MyLog.print("callHangUp callSessionID = " + callSessionID);
			phoneAccept = false;
			isCallAccept = false;
			String ret = null;
			if (callSessionID > 0) {
				Iterator<CallInfomation> iterator_in = mCallInSessionIDList.iterator();
				while (iterator_in.hasNext()) {
					CallInfomation callInfo = iterator_in.next();
					if (callSessionID == callInfo.getSessionID()) {
						callInfo.setEndTime();
						callInfo.setIsHangUp(true);
						if (callInfo.getRemoteCode() != null) {
							ret = new String(callInfo.getRemoteCode());
						}
						if (ProjectConfigure.project == 2) {
							boolean isDoor = false;
							int type = 0;
							if (callInfo.getType() == CallInfomation.CALL_IN_UNACCEPT
									&& callInfo.isDoor()) {
								saveImage(callInfo);
								// ��֪ͨ���µ����ü�¼
								isDoor = true;
								type = 1;
							} else if (callInfo.isDoor()) {
								isDoor = true;
								type = 0;
							}
							if (isDoor) {
								int result = TalkManager.toManageDoorCall(callInfo.getRemoteCode(), 
										CommonUT.formatTime(callInfo.getStartTime()), type);
								if (result == 0) {
									callInfo.setIsSuccess(true);
									MyLog.print(TAG, "toManageDoorCall Success");
								}
							}
						} else {
							if (callInfo.getType() == CallInfomation.CALL_IN_UNACCEPT
									&& callInfo.isDoor()) {
								saveImage(callInfo);
								// ��֪ͨ���µ����ü�¼
							}
						}
						DPDBHelper.addCallLog(callInfo);
						mJniPhoneClass.HangUp(callSessionID);
						toPhoneHangUp();
						callSessionID = 0;
						iterator_in.remove();
						iterator_in = mCallInSessionIDList.iterator();
						break;
					}
				}
				MyLog.print("mCallInSessionIDList size = "
						+ mCallInSessionIDList.size());
				Iterator<CallInfomation> iterator_out = mCallOutSessionIDList.iterator();
				while (iterator_out.hasNext()) {
					CallInfomation callInfo = iterator_out.next();
					if (callSessionID == callInfo.getSessionID()) {
						callInfo.setEndTime();
						callInfo.setIsHangUp(true);
						DPDBHelper.addCallLog(callInfo);
						if (callInfo.getRemoteCode() != null) {
							ret = new String(callInfo.getRemoteCode());
						}
						mJniPhoneClass.HangUp(callSessionID);
						callSessionID = 0;
						iterator_out.remove();
						iterator_out = mCallOutSessionIDList.iterator();
						break;
					}
				}
				MyLog.print("mCallOutSessionIDList size = "
						+ mCallOutSessionIDList.size());
				Log.i(LICHAO, "mCallOutSessionIDList size = "
						+ mCallOutSessionIDList.size());
			}
			return ret;
		}
	}

	/**
	 * @��������callOutHangUp
	 * @���ܣ��Ҷ����к����Ự
	 */
	public static void callOutHangUp() {
		synchronized (mutex) {
			Iterator<CallInfomation> iterator_out = mCallOutSessionIDList.iterator();
			while (iterator_out.hasNext()) {
				CallInfomation calloutinfo = iterator_out.next();
				calloutinfo.setEndTime();
				calloutinfo.setIsHangUp(true);
				DPDBHelper.addCallLog(calloutinfo);
				mJniPhoneClass.HangUp(calloutinfo.getSessionID());
				iterator_out.remove();
				iterator_out = mCallOutSessionIDList.iterator();
			}
			MyLog.print("callOutHangUp mCallOutSessionIDList size = "
					+ mCallOutSessionIDList.size());
		}
	}

	/**
	 * �Ҷ�����ͨ���������ֻ��������豸
	 * 
	 */
	public static String callHangUp() {
		synchronized (mutex) {
			toPhoneHangUp();
			phoneAccept = false;
			isCallAccept = false;
			String ret = null;
			Iterator<CallInfomation> iterator_in = mCallInSessionIDList.iterator();
			while (iterator_in.hasNext()) {
				CallInfomation callInfo = iterator_in.next();
				callInfo.setEndTime();
				callInfo.setIsHangUp(true);
				if (callInfo.getRemoteCode() != null) {
					ret = new String(callInfo.getRemoteCode());
				}
				if (ProjectConfigure.project == 2) {
					boolean isDoor = false;
					int type = 0;
					if (callInfo.getType() == CallInfomation.CALL_IN_UNACCEPT
							&& callInfo.isDoor()) {
						saveImage(callInfo);
						// ��֪ͨ���µ����ü�¼
						isDoor = true;
						type = 1;
					} else if (callInfo.isDoor()) {
						isDoor = true;
						type = 0;
					}
					if (isDoor) {
						int result = TalkManager.toManageDoorCall(callInfo.getRemoteCode(), 
								CommonUT.formatTime(callInfo.getStartTime()), type);
						if (result == 0) {
							callInfo.setIsSuccess(true);
							MyLog.print(TAG, "toManageDoorCall Success");
						}
					}
				} else {
					if (callInfo.getType() == CallInfomation.CALL_IN_UNACCEPT
							&& callInfo.isDoor()) {
						saveImage(callInfo);
						// ��֪ͨ���µ����ü�¼
					}
				}
				DPDBHelper.addCallLog(callInfo);
				mJniPhoneClass.HangUp(callInfo.getSessionID());
				iterator_in.remove();
				iterator_in = mCallInSessionIDList.iterator();
			}
			MyLog.print("mCallInSessionIDList size = "
					+ mCallInSessionIDList.size());
			Iterator<CallInfomation> iterator_out = mCallOutSessionIDList.iterator();
			while (iterator_out.hasNext()) {
				CallInfomation callInfo = iterator_out.next();
				callInfo.setEndTime();
				callInfo.setIsHangUp(true);
				DPDBHelper.addCallLog(callInfo);
				if (callInfo.getRemoteCode() != null) {
					ret = new String(callInfo.getRemoteCode());
				}
				
				mJniPhoneClass.HangUp(callInfo.getSessionID());
				iterator_out.remove();
				iterator_out = mCallOutSessionIDList.iterator();
			}
			MyLog.print("mCallOutSessionIDList size = "
					+ mCallOutSessionIDList.size());
			return ret;
		}
	}
	
	/**
	 * @���ܣ����Һ����б���ָ���ỰID�ĺ�����Ϣ
	 * @������int CallSessionID - ID
	 * @����ֵ��ָ���ỰID�ĺ�����Ϣ
	 */
	public static CallInfomation findCallOut(int callSessionID) {
		synchronized (mutex) {
			if (callSessionID > 0) {
				Iterator<CallInfomation> iterator_out = mCallOutSessionIDList.iterator();
				while (iterator_out.hasNext()) {
					CallInfomation callinfo = iterator_out.next();
					if (callSessionID == callinfo.getSessionID()) {
						return callinfo;
					}
				}
			}
			return null;
		}
	}

	/**
	 * @���ܣ����Һ����б���ָ�������ŵĺ�����Ϣ
	 * @������int index ������
	 * @����ֵ��ָ�������ŵĺ�����Ϣ
	 */
	public static CallInfomation findCallOutIndex(int index) {
		synchronized (mutex) {
			if (index >= 0) {
				int i = 0;
				Iterator<CallInfomation> iterator_out = mCallOutSessionIDList
						.iterator();
				while (iterator_out.hasNext()) {
					CallInfomation callinfo = iterator_out.next();
					if (i++ == index) {
						return callinfo;
					}
				}
			}
			return null;
		}
	}

	/**
	 * @��������findCallin
	 * @���ܣ����Һ����б���ָ���ỰID�ĺ�����Ϣ
	 * @������int CallSessionID - ID
	 * @����ֵ��ָ���ỰID�ĺ�����Ϣ
	 */
	public static CallInfomation findCallIn(int callSessionID) {
		synchronized (mutex) {
			if (callSessionID > 0) {
				Iterator<CallInfomation> iterator_out = mCallInSessionIDList
						.iterator();
				while (iterator_out.hasNext()) {
					CallInfomation callinfo = iterator_out.next();
					if (callSessionID == callinfo.getSessionID()) {
						return callinfo;
					}
				}
			}
			return null;
		}
	}
	
	/**
	 * @���ܣ����Ӻ��м�¼�����ݿ��� ���п�ʼ������ʱ��С��1.5sʱ�������¼
	 */
	public static void addCallLog(CallInfomation info) {
		DPDBHelper.addCallLog(info);
	}

	/**
	 * @���ܣ��޸����ݿ��еĺ��м�¼
	 */
	public static void modifyCallLog(CallInfomation info) {
		DPDBHelper.modifyCallLog(info);
	}

	/**
	 * @���ܣ�ɾ�����ݿ���ָ��ID�ĺ��м�¼
	 * @������int db_id - ���ݿ����Զ����ɵ�_id
	 */
	public static void deleteCallLog(int db_id) {
		DPDBHelper.deleteCallLog(db_id);
	}

	/**
	 * @���ܣ�ɾ����Ӧ���͵����к��м�¼
	 * @������int type ��CallInfo���е�CALLOUT_ACCEPT��ֵ
	 * @��ע�������������ʹ�ã���CALLOUT_ACCEPT | CALLOUT_UNACCEPT
	 */
	public static void deleteAllCallLog(int type) {
		DPDBHelper.deleteAllCallLog(type);
	}

	/**
	 * @���ܣ������ݿ��л�ȡ���м�¼
	 * @������int type ��CallInfo���е�CALLOUT_ACCEPT��ֵ
	 * @��ע�������������ʹ�ã���CALLOUT_ACCEPT | CALLOUT_UNACCEPT
	 */
	public static ArrayList<CallInfomation> getCallLogList(int type) {
		return DPDBHelper.getCallLogList(type);
	}
	
	/**
	 * ��ȡ���豸
	 * @return
	 */
	public static ArrayList<BindAccountInfo> getAccountInfoList(){
		return DPDBHelper.getAccountInfoList();
	}
	
	/**
	 * ���һ���ֻ�
	 * @param db_id
	 */
	public static void deleteAccount(int db_id){
		DPDBHelper.deleteAccount(db_id);
	}
	
	/**
	 * ���ֻ����Ͳ����û�
	 * @param type
	 * @return
	 */
	public static List<String> getAccountByPhoneType(int type){
		return DPDBHelper.getAccountByPhoneType(type);
	}
	/**
	 * ��������豸
	 */
	public static void clearAccount(int type){
		DPDBHelper.clearAccount(type);
	}
	
	/**
	 * �����ֻ����Ͳ��Ұ��û�
	 * @param type
	 * @return
	 */
	public static ArrayList<BindAccountInfo> getAccountByPhonetpye(String type){
		return DPDBHelper.getAccountByPhonetpye(type);
	}
	
	// TODO-----------------------------�ƶԽ����-----------------------------
	
	private static CloudIntercomCallback mCloudIntercomCallback = 
			new CloudIntercomCallback() {

		@Override
		public String getRoomCode() {
			return DPFunction.getRoomCode();
		}

		@Override
		public String getVisitRecord() {
			ArrayList<CallInfomation> list = DPDBHelper
					.getCallLogList(CallInfomation.CALL_IN_UNACCEPT);
			JSONObject json = new JSONObject();
			JSONObject temp;
			JSONArray jsonArray = new JSONArray();
			Iterator<CallInfomation> iterator = list.iterator();
			try {
				while (iterator.hasNext()) {
					CallInfomation callInfo = iterator.next();
					temp = new JSONObject();
					if (callInfo.isDoor()) {
						temp.put("id", callInfo.getDb_id());
						temp.put("from", callInfo.getRemoteCode());
						temp.put("time", CommonUT.formatTime(callInfo.getStartTime()));
						temp.put("accept", "false");
						temp.put("look", callInfo.isRead() + "");
						temp.put("md5", (callInfo.getStartTime()) + "");
						jsonArray.put(temp);
					}
				}
				json.put("ct", "gvla");
				json.put("list", jsonArray.toString());
				return json.toString();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		public String getVisitFileData(int id) {
			ArrayList<CallInfomation> callInfos = DPDBHelper.getCallLogList(
					 CallInfomation.CALL_IN_UNACCEPT);
			if (callInfos == null) {
				return null;
			}
			Iterator<CallInfomation> iterator = callInfos.iterator();
			String visitFile = null;
			byte[] data = null;
			byte[] picture = null;
			byte[] pictureLength = null;
			byte[] audio = null;
			byte[] audioLength = null;
			while (iterator.hasNext()) {
				CallInfomation callInfo = iterator.next();
				if (callInfo.isDoor() && callInfo.getDb_id() == id) {
					File pictureFile = new File(ConstConf.VISIT_PATH + File.separator 
							+ CommonUT.formatTime(callInfo.getStartTime())
							.replaceAll(":", "-") + ".jpg");
//					File audioFile = new File(ConstConf.VISIT_PATH + File.separator 
//							+ CommonUT.formatTime(callInfo.getStartTime())
//							.replaceAll(":", "-") + ".wav");
					File audioFile = new File(ConstConf.VISIT_PATH + File.separator 
							+ "test.wav");
					if ((int) pictureFile.length() > 0) {
						Bitmap bitmap = BitmapFactory.decodeFile(ConstConf.VISIT_PATH + File.separator 
								+ CommonUT.formatTime(callInfo.getStartTime())
								.replaceAll(":", "-") + ".jpg");
						picture = compressBitmap(bitmap);
						pictureLength = intToBytes(picture.length);
					}
					if ((int) audioFile.length() > 0) {
						audio = fileToBytes(audioFile);
						audioLength = intToBytes(audio.length);
					}
				}
			}
			if (picture != null && audio != null) {
				data = new byte[8 + picture.length + audio.length];
			} else if (picture != null) {
				data = new byte[4 + picture.length];
			} else {
				return null;
			}
			if (picture != null) {
				System.arraycopy(pictureLength, 0, data, 0, 4);
				System.arraycopy(picture, 0, data, 4, picture.length);
				MyLog.print(TAG, "picture.length = " + picture.length);
				MyLog.print(TAG, "data.length = " + data.length);
			}
			if (audio != null) {
				System.arraycopy(audioLength, 0, data, picture.length + 4, 4);
				System.arraycopy(audio, 0, data, picture.length + 8, audio.length);
				MyLog.print(TAG, "audio.length = " + audio.length);
				MyLog.print(TAG, "data.length = " + data.length);
			}
			if (data != null) {
				try {
//					data = compress(data);
					visitFile = new String(data, "ISO-8859-1");
					MyLog.print(TAG, "visitFile = " + visitFile.length());
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			return visitFile;
		}

		@Override
		public String getAlarmLog() {
			if (DPSafeService.getInstance() != null) {
				List<AlarmNameInfo> nameList = getAlarmAreaNameList();
				List<AlarmTypeInfo> typeList = getAlarmTypeNameList();
				ArrayList<AlarmLog> list = getAlarmLogList();
				if (nameList == null || typeList == null || list == null) {
					return null;
				}

				JSONObject json = new JSONObject();
				JSONObject temp;
				JSONArray jsonArray = new JSONArray();
				try {
					for (int i = 0; i < list.size(); i++) {
						temp = new JSONObject();
						temp.put("area", nameList.get(
								list.get(i).getAreaName()).value);
						temp.put("type", typeList.get(
								list.get(i).getAreaType()).value);
						temp.put("time", CommonUT.formatTime(list.get(i).getTime()));
						jsonArray.put(temp);
					}
					json.put("ct", "gsla");
					json.put("list", jsonArray.toString());
					return json.toString();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			return null;
		}
		
		@Override
		public String getMessageLog() {
			if (DPSafeService.getInstance() != null) {
				ArrayList<MessageInfo> messageList = getMessageLogList(
						MessageInfo.PERSONAL | MessageInfo.PUBLIC);
				if (messageList == null) {
					return null;
				}

				JSONObject json = new JSONObject();
				JSONObject temp;
				JSONArray jsonArray = new JSONArray();
				try {
					for (int i = 0; i < messageList.size(); i++) {
						if (!messageList.get(i).isJpg()) {
							continue;
						}
						temp = new JSONObject();
						temp.put("id", messageList.get(i).getDb_id());
						String title = URLEncoder.encode(
								messageList.get(i).getTitle(), "UTF-8");
						temp.put("head", title);
						temp.put("time", messageList.get(i).getTime());
						temp.put("look", messageList.get(i).isRead() + "");
						temp.put("md5", messageList.get(i).getTime());
						jsonArray.put(temp);
					}
					json.put("ct", "gmsgla");
					json.put("list", jsonArray.toString());
					return json.toString();
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			return null;
		}
		
		@Override
		public String[] getMessageFileData(int id) {
			ArrayList<MessageInfo> messageList = getMessageLogList(
					MessageInfo.PERSONAL | MessageInfo.PUBLIC);
			String[] message = new String[2];
			JSONObject json = new JSONObject();
			JSONObject temp = new JSONObject();
			JSONArray jsonArray = new JSONArray();
			try {
				if (messageList == null) {
					temp.put("id", id);
					temp.put("picname", "");
					jsonArray.put(temp);
					json.put("ct", "gmsga");
					json.put("msg", jsonArray.toString());
					message[0] = json.toString();
					return message;
				}
				Iterator<MessageInfo> iterator = messageList.iterator();
				String messageFile = null;
				byte[] data = null;
				while (iterator.hasNext()) {
					MessageInfo messageInfo = iterator.next();
					if (messageInfo.getDb_id() == id) {
						String resName = messageInfo.getResName();
						if (resName == null) {
							temp.put("id", id);
							temp.put("picname", "");
							jsonArray.put(temp);
							json.put("ct", "gmsga");
							json.put("msg", jsonArray.toString());
							message[0] = json.toString();
							return message;
						}
						File file = new File(ConstConf.MESSAGE_PATH 
								+ File.separator + resName);
						if ((int) file.length() > 0) {
							Bitmap bitmap = BitmapFactory.decodeFile(
									ConstConf.MESSAGE_PATH + File.separator + resName);
							data = compressBitmap(bitmap);
							temp.put("id", id);
							temp.put("picname", resName);
							jsonArray.put(temp);
							MyLog.print(TAG, "data.length = " + data.length);
						}
					}
				}
				if (data != null) {
					try {
						messageFile = new String(data, "ISO-8859-1");
						MyLog.print(TAG, "messageFile = " + messageFile.length());
						json.put("ct", "gmsga");
						json.put("msg", jsonArray.toString());
						message[0] = json.toString();
						message[1] = messageFile;
						return message;
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public String getSafeMode() {
			return getStringSafeMode();
		}
		
		@Override
		public String getSmartHomeMode() {
			return getStringSmartHomeMode();
		}
		
		@Override
		public int getCallInSize() {
			return DPFunction.getCallInSize();
		}

		@Override
		public int getCallOutSize() {
			return DPFunction.getCallOutSize();
		}

		@Override
		public void changeSafeMode(int model, boolean isAuto) {
			if (model == DPDBHelper.getSafeMode() 
					&& model != ConstConf.UNSAFE_MODE) {
				// ����յ���ģʽ�뵱ǰ��ͬ(�ǳ���)��ͬ���ֻ��˰���ģʽ
				synchPhoneSafeMode();
			} else {
				if (model == ConstConf.UNSAFE_MODE) {
					disAlarm(false);
					Intent intent = new Intent();
					intent.setAction(DPFunction.ACTION_ALARMING);
					intent.putExtra("alarm", -1);
					mContext.sendBroadcast(intent);
				} else {
					DPFunction.changeSafeMode(model, false);
				}
			}
		}
		
		@Override
		public void changeSmartHomeMode(int model) {
			if (model == DPDBHelper.getSmartHomeMode()) {
				// ����յ���ģʽ�뵱ǰ��ͬ��ͬ���ֻ������ܼҾ�ģʽ
				synchPhoneSmartHomeMode();
			} else {
				DPFunction.setSmartHomeMode(model);
			}
		}

		@Override
		public boolean openLock() {
			int callInCount = getCallInSize();
			if (callInCount == 1) {
				CallInfomation callInfo = getCallInList().get(0);
				if (callInfo != null && callInfo.isDoor()) {
					return DPFunction.openLock(callInfo.getRemoteCode());
				}
			}
			return false;
		}

		@Override
		public void accept() {
			int callInCount = getCallInSize();
			if (callInCount == 1) {
				CallInfomation callInfo =  getCallInList().get(0);
				if (callInfo != null && callInfo.getAcceptTime() == 0) {
					if (DPFunction.accept(callInfo.getSessionID(), 2)) {
						setLocalVideoVisable(callInfo.getSessionID(), false);
//						 ���յ���(����Ƶ)����ת��ָ��IP�Ͷ˿�
						boolean result = getJniPhoneClass().SetRelayMode(
								callInfo.getSessionID(), true, 6135,
								"127.0.0.1", 6136);
						if (result) {
							MyLog.print("���յ���(����Ƶ)����ת��ָ��IP�Ͷ˿ڳɹ�");
						} else {
							MyLog.print("���յ���(����Ƶ)����ת��ָ��IP�Ͷ˿�ʧ��");
						}
						phoneAccept = true;
						Intent intent = new Intent();
						intent.putExtra("MsgType", JniPhoneClass.MSG_PHONE_ACCEPT);
						intent.setAction(CallReceiver.CALL_IN_ACTION);
						mContext.sendBroadcast(intent);
					}
				}
			}
		}

		@Override
		public void hangUp(int sessionID) {
			if (getCallInList() != null) {
				int callInCount = getCallInSize();
				if (callInCount == 1) {
					if (!DPFunction.isCallAccept) {
						CallInfomation callInfo = getCallInList().get(0);
						MyLog.print("callInSession "
								+ callInfo.getSessionID());
						if (phoneAccept) {
							callHangUp();
						}
						Intent intent = new Intent();
						intent.putExtra("MsgType",
								JniPhoneClass.MSG_PHONE_HANGUP);
						intent.putExtra("SessionID", sessionID);
						intent .setAction(CallReceiver.CALL_IN_ACTION);
						mContext.sendBroadcast(intent);
					}
				}
			}
		}

		@Override
		public void sipConnectChange(boolean isConnected, String reason) {
			mContext.sendBroadcast(new Intent(ACTION_CLOUD_LOGIN_CHANGED));
		}
		
		@Override
		public void loginResult(int loginStatus, String reason) {
			mContext.sendBroadcast(new Intent(ACTION_CLOUD_LOGIN_CHANGED));
		}

		@Override
		public void onBindTel(String tel, boolean isBind) {
			mContext.sendBroadcast(new Intent(ACTION_CLOUD_BIND_CHANGED));
		}
		
		@Override
		public List<String> getAccountList() {
			return DPDBHelper.getAccountList();
		}

		@Override
		public int addAccount(String account, String token, String mobiletype) {
			return DPDBHelper.addAccount(account, token, mobiletype);
		}

		@Override
		public void deleteAccount(String account) {
			DPDBHelper.delAccount(account);
		}

		@Override
		public void clearAccount() {
			DPDBHelper.clearAccount();
		}

		@Override
		public List<String> getIndoorSipList() {
			return DPDBHelper.getIndoorSipList();
		}

		@Override
		public boolean isIndoorSipExist(String sipid) {
			return DPDBHelper.isIndoorSipExist(sipid);
		}

		@Override
		public int getTokensCount(String tokentype) {
			return DPDBHelper.getTokensCount(tokentype);	
		}

		@Override
		public boolean isTokenExist(String token) {
			return DPDBHelper.isTokenExist(token);
		}

		@Override
		public CallInfomation quaryLastCall() {
			return DPDBHelper.quaryLastCall();
		}

		@Override
		public void addIndoorSip(IndoorSipInfo info) {
			DPDBHelper.addIndoorSip(info);
		}

		@Override
		public void modifyIndoorSip(IndoorSipInfo info) {
			DPDBHelper.modifyIndoorSip(info);
		}

		@Override
		public List<String> getTokenByTypeList(String type) {
			return DPDBHelper.getTokenByTypeList(type);
		}

		@Override
		public int countIndoorSip() {
			return DPDBHelper.countIndoorSip();
		}

		@Override
		public IndoorSipInfo queryFistSip() {
			return DPDBHelper.queryFistSip();
		}

		@Override
		public void setAccountOnline(String account, boolean online) {
			DPDBHelper.setAccountOnline(account, online);
		}

		@Override
		public void delAccountByToken(String account, String token) {
			DPDBHelper.delAccountByToken(account, token);
		}
		
	};
	
	/**
	 * ֪ͨ�ѵ�¼���ֻ�,���µı���
	 * @param alarmArea
	 *            ��������
	 * @param alarmType
	 *            ��������
	 */
	public static void toPhoneAlarm(int alarmArea, int alarmType, String time) {
		CloudIntercom.toPhoneAlarm(alarmArea, alarmType, time);
	}
	
	/**
	 * ͬ������ģʽ���ֻ�
	 */
	public static void synchPhoneSafeMode() {
		CloudIntercom.synchPhoneSafeMode(getStringSafeMode());
	}
	
	/**
	 * ͬ�����ܼҾ�ģʽ���ֻ�
	 */
	public static void synchPhoneSmartHomeMode() {
		CloudIntercom.synchPhoneSmartHomeMode(getStringSmartHomeMode());
	}
	
	/** �Ҷ��ֻ� */
	public static void toPhoneHangUp() {
		CloudIntercom.toPhoneHangUp();
	}
	
	/** ���������ֻ� */
	public static void callPhone() {
		CloudIntercom.callPhone();
	}
	
	/**
	 * @���ܣ�֪ͨ�ѵ�¼���ֻ��������յ�������Ϣ
	 * @����1��boolean isPersonal: true-������Ϣ false-������Ϣ
	 * @����2���յ�����Ϣ������
	 */
	public static void toPhoneNewMsg(boolean isPersonal, int count) {
		CloudIntercom.toPhoneNewMsg(isPersonal, count);
	}

	/** ��¼�������� */
	public static void toStartLogin() {
		CloudIntercom.toStartLogin();
	}
	
	/** �ʺ��Ƿ����� */
	public static boolean isOnline() {
		return CloudIntercom.isOnline();
	}
	
	/** MSG�Ƿ����� */
	public static boolean msgIsOnline() {
		return CloudIntercom.msgIsOnline();
	}
	
	/**
	 * ������ʾ����ά����
	 */
	public static void getQRString(Handler handler) {
		 CloudIntercom.getQRString(handler);
	}
	
	/**
	 * ��ȡ���ڻ����ʺ�
	 */
	public static String getAccount() {
		return CloudIntercom.getAccount();
	}
	
	/**
	 * �����������������õ�����������״̬�����е�¼���˳���¼��Ĭ��������������
	 * @param isEthernet true-��̫��"eth1", false-���� 
	 */
	public static void setEthernet(boolean isEthernet) {
		CloudIntercom.setEthernet(isEthernet);
	}
	
	/**
	 * ��ȡ�����ڻ����ֻ��ʺ��б�
	 */
	public static List<String> getPhoneList() {
		return DPDBHelper.getAccountList();
	}
}

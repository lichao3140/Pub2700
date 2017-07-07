package com.dpower.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import com.dpower.domain.AlarmLog;
import com.dpower.domain.AlarmVideo;
import com.dpower.domain.BindAccountInfo;
import com.dpower.domain.CallInfomation;
import com.dpower.domain.MessageInfo;
import com.dpower.domain.SafeModeInfo;
import com.dpower.pub.dp2700.model.IndoorSipInfo;
import com.example.dpservice.DPSafeService;

/**
 * �Զ����࣬�����ݿ�Ĳ����������ֻ����б�,ͨ����¼,������¼,������¼,С����Ϣ��
 */
public class DPDBHelper {
	
	private static final int MAX_COUNT = 50; // �洢��¼���������
	private static final int ACCOUNT_MACNUM = 50; // ���ڻ����ֻ����������
	private static final String DB_NAME = "lib2700_db.db";

	/** ��ֵ�� */
	private static final String TBL_APP_STATUS = "localstatus";

	/** �󶨵��ֻ����ʺ� */
	private static final String TBL_ACCOUNT_LIST = "accountlist";

	/** ͨ����¼, ��Ӱ�����ļ���Ϊ��ʼʱ������ */
	private static final String TBL_CALL_INFO = "callinfo";

	/** ������¼ */
	private static final String TBL_SAFE_MODE_INFO = "safemodeinfo";

	/** ������¼ */
	private static final String TBL_AlARM_INFO = "alarminfo";
	
	/** ����¼�� */
	private static final String TBL_ALARM_VIDEO = "alarmvideo";

	/** С����Ϣ */
	private static final String TBL_MESSAGE_INFO = "messageinfo";
	
	/** ���ڻ�SIP��Ϣ */
	private static final String TBL_INDOORSIP_INFO = "indoorsipinfo";
	
	private static SQLiteDatabase mDatabase = null;
	private static Object mLock = new Object();
	private static Context mContext = null;

	/**
	 * ��ʼ�����ݿ�,ÿ������APPʱִ��
	 */
	public static void DBInstance(Context context) {
		mContext = context;
		try {
			MyLog.print("path: "
					+ Environment.getExternalStorageDirectory().getPath()
					+ ", " + Environment.getDataDirectory().getPath()
					+ File.separator + DB_NAME + ", " + context.getFilesDir());

			mDatabase = SQLiteDatabase.openOrCreateDatabase(context.getFilesDir()
					+ File.separator + DB_NAME, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// ��ֵ��
		if (!tabIsExist(TBL_APP_STATUS)) {
			// ���ƣ� ��Ӧ��ֵ
			mDatabase.execSQL("create table if not exists "
					+ TBL_APP_STATUS
					+ "("
					+ "statusname varchar(256) not null,statusvalue varchar(256) not null default 0)");
			MyLog.print("create " + TBL_APP_STATUS + " table success");
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('init_app', 1)"); // 0-false, 1-true
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('roomcode', '1999999999901')"); // ����
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('message_mode', 0)"); // ����ģʽ
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('safe_protectiondelaytime', 30)"); // ������ʱʱ��
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('wan_dhcp', 0)"); // 0-false, 1-true
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('wan_ip', '0.0.0.0')"); // ip
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('wan_mask', '0.0.0.0')"); // 0-false, 1-true
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('wan_gw', '0.0.0.0')"); // 0-false, 1-true
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('wan_dns', '0.0.0.0')"); // 0-false, 1-true
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('brightness', 50)"); // ����
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('contrast', 50)"); // �Աȶ�
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('saturability', 50)"); // ���Ͷ�
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('hue', 50)"); // ɫ��
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('istophone', 1)"); // �Ƿ����ת�Ƶ��ֻ�,0-��ת��, 1-ת�� 
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('smart_home', 1)"); // ���ܼҾ�ģʽ
			
			int defalutSafeMode = 8;
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('safe_connection', "
					+ (0xFFFFFFFF & ((1 << defalutSafeMode) - 1)) + ")"); // ����̽ͷ�Ľӷ����ߵ͵�ƽ
			if (ProjectConfigure.project == 1) {
				mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('projectPassword', '666666')"); // ��������
			} else {
				mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('projectPassword', '123456')"); // ��������
			}
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('holding_password', '654321')"); // Ю������
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('safe_password', '123456')"); // ��������
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('safe_mode', '1')"); // ��ǰ��������
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('safe_enable0', 0)"); // ���ԣ�����5��ģʽĬ�϶�Ϊ����
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('safe_enable1', 0)"); // ����
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('safe_enable2', 0)"); // ҹ��
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('safe_enable3', 0)"); // �ڼ�
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('safe_enable4', 0)"); // ���
			JSONArray jsonArray = new JSONArray();
			for (int i = 0; i < ConstConf.DEFAULT_SAFE_TANTOU; i++) {
				jsonArray.put(0);
			}
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('safe_alarmdelaytime0', '" + jsonArray.toString() + "')"); // ������������ʱʱ��
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('safe_alarmdelaytime1', '" + jsonArray.toString() + "')"); // ��ǰ��������
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('safe_alarmdelaytime2', '" + jsonArray.toString() + "')"); // ��ǰ��������
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('safe_alarmdelaytime3', '" + jsonArray.toString() + "')"); // ��ǰ��������
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('safe_alarmdelaytime4', '" + jsonArray.toString() + "')"); // ��ǰ��������
		}

		// �󶨵��ֻ����ʺ�
		if (!tabIsExist(TBL_ACCOUNT_LIST)) {
			// �ʺţ� �Ƿ����� :1-���ߣ�0-������
			mDatabase.execSQL("create table if not exists "
					+ TBL_ACCOUNT_LIST
					+ "("
					+ "_id integer primary key autoincrement,accountname varchar(256) not null" 
					+ ",accountpwd varchar(256) not null,isonline int not null,token varchar(256) not null,phonetype varchar(50) not null)");
			MyLog.print("create table " + TBL_ACCOUNT_LIST + " success");
		}

		// ͨ����¼
		if (!tabIsExist(TBL_CALL_INFO)) {
			// �Է��ʺ�, ��������, ��ʼʱ��(��ʼʱ��Ϊ��Ӱ����ͼƬ���ļ���), ����ʱ��, �Ҷ�ʱ��, �Ƿ���, �Ƿ��Ѷ�
			mDatabase.execSQL("create table if not exists "
					+ TBL_CALL_INFO
					+ "(_id integer primary key autoincrement,remotecode text,calltype tinyint"
					+ ",starttime int,accepttime int,endtime int,isopenlock bit,isread bit)");
			MyLog.print("create table " + TBL_CALL_INFO + " success");
		}

		// ������¼
		if (!tabIsExist(TBL_SAFE_MODE_INFO)) {
			// ����ģʽ, ����ʱ��, �Ƿ��ϱ��ɹ�
			mDatabase.execSQL(" create table "
					+ TBL_SAFE_MODE_INFO
					+ "(_id integer primary key autoincrement,safemode int,time int,issuccess bit)");
			MyLog.print("create table " + TBL_SAFE_MODE_INFO + " success");
		}

		// ������¼
		if (!tabIsExist(TBL_AlARM_INFO)) {
			// ����ģʽ, ������, ������, ����, ʱ��, �Ƿ��ϱ��ɹ�, �Ƿ��Ķ�
			mDatabase.execSQL(" create table "
					+ TBL_AlARM_INFO
					+ "(_id integer primary key autoincrement,safemode int,areanum int,areaname int,areatype int,time int,issuccess bit,isread bit)");
			MyLog.print("create table " + TBL_AlARM_INFO + " success");
		}
		
		// ����¼��
		if (!tabIsExist(TBL_ALARM_VIDEO)) {
			// ʱ��, ·��, ����, ����
			mDatabase.execSQL(" create table "
					+ TBL_ALARM_VIDEO
					+ "(_id integer primary key autoincrement,time text,path text,area int,type int)");
			MyLog.print("create table " + TBL_ALARM_VIDEO + " success");
		}

		// С����Ϣ
		if (!tabIsExist(TBL_MESSAGE_INFO)) {
			// �Ƿ������Ϣ, ʱ��, ����, ����, url, ͼƬ, �Ƿ��Ѷ�
			mDatabase.execSQL(" create table "
					+ TBL_MESSAGE_INFO
					+ "(_id integer primary key autoincrement,personal bit,time text"
					+ ",title text,body text,resname text,isjpg bit,read bit)");
			MyLog.print("create table " + TBL_MESSAGE_INFO + " success");
		}
		
		// ���ڻ�SIP��Ϣ
		if (!tabIsExist(TBL_INDOORSIP_INFO)) {
			// �豸���  С�����  ����  ¥��  ��Ԫ  ����  MAC IP �豸����  �豸����  �豸״̬
			mDatabase.execSQL(" create table "
					+ TBL_INDOORSIP_INFO
					+ "(_id integer primary key autoincrement,deviceno text" 
					+ ",areano text,regionno text,buildingno text,unitno text" 
					+ ",houseno text,mac text,ip text,devicename text,devicetype text" 
					+ ",devicepassword text,position text,version text,ipstate text" 
					+ ",housestate text,sipid text,sippwd text)");
			MyLog.print("create table " + TBL_INDOORSIP_INFO + " success");
		}

		if (mDatabase != null && mDatabase.isOpen()) {
			mDatabase.close();
			mDatabase = null;
		}
	}

	// ���ݿ���Ƿ����
	private static boolean tabIsExist(String tabName) {
		boolean result = false;
		if (tabName == null) {
			return false;
		}
		Cursor cursor = null;
		try {
			String sql = "select count(*) as c from sqlite_master where type ='table' and name ='"
					+ tabName.trim() + "' ";
			MyLog.print(sql);
			cursor = mDatabase.rawQuery(sql, null);
			if (cursor.moveToNext()) {
				int count = cursor.getInt(0);
				MyLog.print("cursor: " + cursor.getInt(0));
				if (count > 0) {
					result = true;
				}
			}
		} catch (Exception e) {
			MyLog.print(e.toString());
		}
		cursor.close();
		return result;
	}

	private static void DBOpen() {
		if (mDatabase == null)
			mDatabase = SQLiteDatabase.openOrCreateDatabase(mContext.getFilesDir()
					+ File.separator + DB_NAME, null);
	}

//	private static void DBClose() {
//		if (mDatabase != null && mDatabase.isOpen()) {
//			mDatabase.close();
//			mDatabase = null;
//		}
//	}

	// ------�Լ�ֵ�ԵĲ���---------------------------------------------------------------------------------------

	public static boolean isInitApp() {
		int initApp = getIntStatus("init_app");
		if(initApp == 1)
			return true;
		else
			return false;
	}

	public static void setInitApp(boolean init) {
		setStatusValue("init_app", init ? 1 : 0);
	}

	public static boolean isWanDHCP() {
		int dhcp = getIntStatus("wan_dhcp");
		if(dhcp == 1)
			return true;
		else
			return false;
	}

	public static void setWanDHCP(boolean bDhcp) {
		setStatusValue("wan_dhcp", bDhcp ? 1 : 0);
	}

	public static String getWanIP() {
		return getStringStatus("wan_ip");
	}

	public static boolean setWanIP(String ip) {
		setStatusValue("wan_ip", ip);
		return true;
	}

	public static String getWanMask() {
		return getStringStatus("wan_mask");
	}

	public static boolean setWanMask(String mask) {
		setStatusValue("wan_mask", mask);
		return true;
	}

	public static String getWanGw() {
		return getStringStatus("wan_gw");
	}

	public static boolean setWanGw(String gw) {
		setStatusValue("wan_gw", gw);
		return true;
	}

	public static String getWanDNS() {
		return getStringStatus("wan_dns");
	}

	public static boolean setWanDNS(String dns) {
		setStatusValue("wan_dns", dns);
		return true;
	}

	/** ��ȡͬ����������������Ϣ��ʱ�� */
	public static String getUpdateMsgTime() {
		return getStringStatus("update_msg_time");
	}

	/** ����ͬ����������������Ϣ��ʱ�� */
	public static boolean setUpdateMsgTime(String time) {
		setStatusValue("update_msg_time", time);
		return true;
	}

	/** ��ȡͬ��������������������ʱ�� */
	public static String getUpdateWeatherTime() {
		return getStringStatus("update_weather_time");
	}

	/** ����ͬ��������������������ʱ�� */
	public static boolean setUpdateWeatherTime(String time) {
		setStatusValue("update_weather_time", time);
		return true;
	}

	/** ��ȡ���� */
	public static int getBrightness() {
		return getIntStatus("brightness");
	}
	
	/** �������� */
	public static boolean setBrightness(int value) {
		setStatusValue("brightness", value);
		return true;
	}

	/** ��ȡ�Աȶ� */
	public static int getContrast() {
		return getIntStatus("contrast");
	}
	
	/** ����Աȶ� */
	public static boolean setContrast(int value) {
		setStatusValue("contrast", value);
		return true;
	}

	/** ��ȡ���Ͷ� */
	public static int getSaturability() {
		return getIntStatus("saturability");
	}
	
	/** ���汥�Ͷ� */
	public static boolean setSaturability(int value) {
		setStatusValue("saturability", value);
		return true;
	}

	/** ��ȡɫ�� */
	public static int getHue() {
		return getIntStatus("hue");
	}

	/** ����ɫ�� */
	public static boolean setHue(int value) {
		setStatusValue("hue", value);
		return true;
	}
	
	/** ��ȡ�Ƿ����ת�Ƶ��ֻ� */
	public static boolean getCallToPhone() {
		if(getIntStatus("istophone") == 1)
			return true;
		else
			return false;
	}
	
	/** �����Ƿ����ת�Ƶ��ֻ� */
	public static void setCallToPhone(boolean isToPhone) {
		setStatusValue("istophone", isToPhone ? 1 : 0);
	}
	
	/** �������ܼҾ�ģʽ  1=�ڼң�2=���ޣ�3=�۲ͣ�4=Ӱ�ӣ�5=���֣�6=ȫ�� */
	public static void setSmartHomeMode(int mode) {
		setStatusValue("smart_home", mode);
	}
	
	/** ��ȡ���ܼҾ�ģʽ  1=�ڼң�2=���ޣ�3=�۲ͣ�4=Ӱ�ӣ�5=���֣�6=ȫ��*/
	public static int getSmartHomeMode() {
		return getIntStatus("smart_home");
	}

	/** ��ȡ���� */
	public static String getRoomCode() {
		return getStringStatus("roomcode");
	}

	/** ���淿�� */
	public static boolean setRoomCode(String roomCode) {
		setStatusValue("roomcode", roomCode);
		return true;
	}

	/**
	 * @���ܣ���������ģʽ
	 * @���� int mode 0-�����ԣ�1-Ĭ�����ԣ�2-ҵ������
	 */
	public static boolean setMessageMode(int mode) {
		setStatusValue("message_mode", mode);
		return true;
	}

	/**
	 * @���ܣ���ȡ����ģʽ
	 * @���� int mode 0-�����ԣ�1-Ĭ�����ԣ�2-ҵ������
	 */
	public static int getMessageMode() {
		return getIntStatus("message_mode");
	}

	/** �������ڻ����������á����� **/
	public static boolean setPsdProjectSetting(String projectPsd) {
		setStatusValue("projectPassword", projectPsd);
		return true;
	}

	/** ��ȡ���ڻ����������á����� */
	public static String getPsdProjectSetting() {
		return getStringStatus("projectPassword");
	}

	/**
	 * @���ܣ����ò���ʱ��
	 * @���� int time 0-300��
	 */
	public static boolean setProtectionDelayTime(int time) {
		setStatusValue("safe_protectiondelaytime", time);
		return true;
	}

	/** @���ܣ���ȡ����ʱ�� */
	public static int getProtectionDelayTime() {
		return getIntStatus("safe_protectiondelaytime");
	}

	/** ��ȡ�������� 
	 * @param holding �Ƿ���Ю������
	 **/
	public static String getSafePassword(boolean holding) {
		if (holding) {
			return getStringStatus("holding_password");
		}
		return getStringStatus("safe_password");
	}

	/** ���氲������ 
	 * @param holding �Ƿ���Ю������
	 * @param pwd ������
	 **/
	public static boolean setSafePassword(boolean holding, String pwd) {
		if (holding) {
			setStatusValue("holding_password", pwd);
		} else {
			setStatusValue("safe_password", pwd);
		}
		return true;
	}

	/**
	 * @���ܣ���ȡ����ģʽ
	 * @������int mode 0-���ԣ�1-������2-ҹ�䣬3-�ڼң�4-���
	 * @��ע��Ĭ��Ϊ1-����ģʽ
	 */
	public static int getSafeMode() {
		return getIntStatus("safe_mode");
	}

	/**
	 * @���ܣ����ð���ģʽ
	 * @���� int mode 0-���ԣ�1-������2-ҹ�䣬3-�ڼң�4-���
	 */
	public static boolean setSafeMode(int mode) {
		setStatusValue("safe_mode", mode);
		return true;
	}
	
	/**
	 * @���ܣ����ø���̽ͷ��������
	 * @param area
	 *            ���ֶ�Ӧ�����ƴ����ñ��ж�ȡ
	 * */
	public static boolean setSafeArea(int[] area) {
		JSONArray jsonArray = new JSONArray();
		for (int i = 0; i < area.length; i++) {
			jsonArray.put(area[i]);
		}
		setStatusValue("safe_area", jsonArray.toString());
		return true;
	}

	/** @���ܣ���ȡ����̽ͷ�������� */
	public static int[] getSafeArea() {
		int area[] = new int[DPSafeService.tanTouNum];
		Arrays.fill(area, 0);
		try {
			String json = getStringStatus("safe_area");
			if (json == null) {
				return null;
			}
			JSONArray jsonArray = new JSONArray(json);
			if (jsonArray.length() > 0) {
				area = new int[jsonArray.length()];
				for (int i = 0; i < jsonArray.length(); i++) {
					area[i] = jsonArray.getInt(i);
				}
			} else {
				return null;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return area;
	}

	/** ���ø���̽ͷ���� */
	public static boolean setSafeType(int[] type) {
		synchronized (mLock) {
			JSONArray jsonArray = new JSONArray();
			for (int i = 0; i < type.length; i++) {
				jsonArray.put(type[i]);
			}
			setStatusValue("safe_type", jsonArray.toString());
			return true;
		}
	}

	/** ��ȡ����̽ͷ���� */
	public static int[] getSafeType() {
		int type[] = new int[DPSafeService.tanTouNum];
		Arrays.fill(type, 0);
		try {
			String json = getStringStatus("safe_type");
			if (json == null) {
				return null;
			}
			JSONArray jsonArray = new JSONArray(json);
			if (jsonArray.length() > 0) {
				type = new int[jsonArray.length()];
				for (int i = 0; i < jsonArray.length(); i++) {
					type[i] = jsonArray.getInt(i);
				}
			} else {
				return null;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return type;
	}

	/**
	 * @���ܣ����ð�������̽ͷ�ӷ�-�ߵ͵�ƽ
	 * @������long connection
	 * @���磺long connection = 0x1F ��2���Ƶ�1111 ��ô̽ͷ1-5�������պ�ʱ������̽ͷ6-8���գ���ʱ����
	 * @������̽ͷ�ӷ����ߵ͵�ƽ����0-���գ�1-����
	 */
	public static boolean setSefeConnection(long connection) {
		setStatusValue("safe_connection", connection);
		return true;
	}

	/**
	 * @���ܣ���ȡ��������̽ͷ�ӷ�-�ߵ͵�ƽ
	 * @����ֵ��long - ����̽ͷ�ӷ�
	 * @��ע������ֵΪ 0x1F ��2���Ƶ�1111 ��ô̽ͷ1-5�������պ�ʱ������̽ͷ6-8���գ���ʱ����
	 * @������̽ͷ�ӷ����ߵ͵�ƽ����0-���գ�1-����
	 */
	public static long getSefeConnection() {
		return getLongStatus("safe_connection");
	}

	/**
	 * @���ܣ����ø�������ģʽ�µ�̽ͷ���������
	 * @����1��mode - ����ģʽ
	 * @����2��enable - ���û���� 2����λΪ1����
	 * @����ֵ��boolean false-ʧ�ܣ�true-�ɹ�
	 */
	public static boolean setSafeModeEnable(int mode, long enable) {
		setStatusValue("safe_enable" + mode, enable);
		return true;
	}

	/**
	 * @���ܣ���ȡ��������ģʽ�µ�̽ͷ���������
	 * @������mode - ����ģʽ
	 * @����ֵ��long - ���û���� 2����λΪ1����
	 */
	public static long getSafeModeEnable(int mode) {
		return getLongStatus("safe_enable" + mode);
	}

	/**
	 * @���ܣ����ø�������ģʽ�µ�ÿ��̽ͷ�ı�����ʱʱ��
	 * @����1��mode - ����ģʽ
	 * @����2��int[] - ÿ��̽ͷ�ı�����ʱʱ��
	 * @����ֵ��boolean false-ʧ�ܣ�true-�ɹ�
	 */
	public static boolean setAlarmDelayTime(int mode, int[] time) {
		JSONArray jsonArray = new JSONArray();
		for (int i = 0; i < time.length; i++) {
			jsonArray.put(time[i]);
		}
		setStatusValue("safe_alarmdelaytime" + mode, jsonArray.toString());
		return true;
	}

	/**
	 * @���ܣ���ȡ��������ģʽ�µ�ÿ��̽ͷ�ı�����ʱʱ��
	 * @������mode - ����ģʽ
	 * @����ֵ��int[] - ÿ��̽ͷ�ı�����ʱʱ��
	 */
	public static int[] getAlarmDelayTime(int mode) {
		int time[] = new int[DPSafeService.tanTouNum];
		Arrays.fill(time, 0);
		try {
			String timeList = getStringStatus("safe_alarmdelaytime" + mode);
			MyLog.print("getAlarmDelayTime mode " + mode + ", mTantounum " + time.length + ", " + timeList);
			JSONArray jsonArray = new JSONArray(timeList);
			if (jsonArray.length() > 0) {
				time = new int[jsonArray.length()];
				for (int i = 0; i < jsonArray.length(); i++) {
					time[i] = jsonArray.getInt(i);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return time;
	}

	/**
	 * ��ȡ��Ӧ�ؼ��ֵ�����
	 * 
	 * @return �ַ�������
	 */
	public static String getStringStatus(String key) {
		DBOpen();
		String ret = null;
		String sql = "select statusvalue from " + TBL_APP_STATUS
				+ " where statusname='" + key + "'";
		MyLog.print(sql);
		Cursor cursor = mDatabase.rawQuery(sql, null);
		if (cursor.moveToFirst()) {
			ret = cursor.getString(0).trim();
		}
		cursor.close();
		return ret;
	}

	/**
	 * ��ȡ��Ӧ�ؼ��ֵ�����
	 * 
	 * @return ��ֵ��
	 */
	public static int getIntStatus(String key) {
		DBOpen();
		int ret = -1;
		String sql = "select statusvalue from " + TBL_APP_STATUS
				+ " where statusname='" + key + "'";
		MyLog.print(sql);
		Cursor cursor = mDatabase.rawQuery(sql, null);
		if (cursor.moveToFirst()) {
			ret = cursor.getInt(0);
		}
		cursor.close();
		return ret;
	}

	/**
	 * ��ȡ��Ӧ�ؼ��ֵ�����
	 * 
	 * @return ��ֵ��
	 */
	public static long getLongStatus(String key) {
		DBOpen();
		long ret = -1;
		String sql = "select statusvalue from " + TBL_APP_STATUS
				+ " where statusname='" + key + "'";
		MyLog.print(sql);
		Cursor cursor = mDatabase.rawQuery(sql, null);
		if (cursor.moveToFirst()) {
			ret = cursor.getLong(0);
		}
		cursor.close();
		return ret;
	}

	/**
	 * ���ö�Ӧ�ؼ��ֵ�ֵ
	 */
	public static void setStatusValue(String key, String value) {
		synchronized (mLock) {
			DBOpen();
			if (value != null) {
				String sql = "select statusvalue from " + TBL_APP_STATUS
						+ " where statusname='" + key + "'";
				Cursor cursor = mDatabase.rawQuery(sql, null);
				if (!cursor.moveToFirst()) {
					sql = "insert into " + TBL_APP_STATUS + " values('" + key
							+ "','" + value + "')";
					MyLog.print(sql);
					mDatabase.execSQL(sql);
				} else {
					sql = "update " + TBL_APP_STATUS + " set statusvalue='"
							+ value + "' where statusname='" + key + "'";
					MyLog.print(sql);
					mDatabase.execSQL(sql);
				}
			}
		}
	}

	/**
	 * ���ö�Ӧ�ؼ��ֵ�ֵ
	 */
	public static void setStatusValue(String key, int value) {
		synchronized (mLock) {
			DBOpen();
			String sql = "select statusvalue from " + TBL_APP_STATUS
					+ " where statusname='" + key + "'";
			Cursor cursor = mDatabase.rawQuery(sql, null);
			if (!cursor.moveToFirst()) {
				sql = "insert into " + TBL_APP_STATUS + " values('" + key + "',"
						+ value + ")";
				MyLog.print(sql);
				mDatabase.execSQL(sql);
			} else {
				sql = "update " + TBL_APP_STATUS + " set statusvalue=" + value
						+ " where statusname='" + key + "'";
				mDatabase.execSQL(sql);
				MyLog.print("setStatusValue sql=" + sql);
			}
		}
	}

	/**
	 * ���ö�Ӧ�ؼ��ֵ�ֵ
	 */
	public static void setStatusValue(String key, long value) {
		synchronized (mLock) {
			DBOpen();
			String sql = "select statusvalue from " + TBL_APP_STATUS
					+ " where statusname='" + key + "'";
			Cursor cursor = mDatabase.rawQuery(sql, null);
			if (!cursor.moveToFirst()) {
				sql = "insert into " + TBL_APP_STATUS + " values('" + key + "',"
						+ value + ")";
				MyLog.print(sql);
				mDatabase.execSQL(sql);
			} else {
				sql = "update " + TBL_APP_STATUS + " set statusvalue=" + value
						+ " where statusname='" + key + "'";
				mDatabase.execSQL(sql);
				MyLog.print(sql);
			}
		}
	}

	// ------�Ѱ󶨵��ֻ��б�---------------------------------------------------------------------------------------

	/**
	 *  ��ȡ���ڻ�������ֻ��ʺ�����
	 */
	private static int getAccountCount() {
		int ret = 0;
		DBOpen();
		String sql = "select count(*) from " + TBL_ACCOUNT_LIST;
		Cursor cursor = mDatabase.rawQuery(sql, null);
		if (cursor.moveToFirst()) {
			ret = cursor.getInt(0);
		}
		cursor.close();
		MyLog.print("getAccountCount sql:" + sql + "  , ���˺�����= " + ret);
		return ret;
	}
	
	/**
	 * �ҳ���ͬ���͵�����
	 * @param tokentype
	 * @return
	 */
	public static int getTokensCount(String tokentype) {
		int ret = 0;
		DBOpen();
		String sql = "select count(*) from " + TBL_ACCOUNT_LIST + " where phonetype=" + tokentype + " and isonline=1";
		Cursor cursor = mDatabase.rawQuery(sql, null);
		if (cursor.moveToFirst()) {
			ret = cursor.getInt(0);
		}
		cursor.close();
		MyLog.print(sql + "  , ret= " + ret);
		return ret;
	}

	/**
	 * �ж�SIP�˺��Ƿ��Ѿ�����
	 * @param account
	 * @return
	 */
	public static boolean isAccountExist(String account) {
		boolean ret = false;
		DBOpen();
		String sql = "select accountname from " + TBL_ACCOUNT_LIST
				+ " where accountname='" + account + "'";
		MyLog.print(sql);
		Cursor cursor = mDatabase.rawQuery(sql, null);
		if (cursor.moveToFirst()) {
			ret = true;
		}
		cursor.close();
		return ret;
	}
	
	/**
	 * �ж�Token�˺��Ƿ��Ѿ�����
	 * @param Token
	 * @return
	 */
	public static boolean isTokenExist(String token) {
		boolean ret = false;
		DBOpen();
		String sql = "select token from " + TBL_ACCOUNT_LIST
				+ " where token='" + token + "'";
		MyLog.print(sql);
		Cursor cursor = mDatabase.rawQuery(sql, null);
		if (cursor.moveToFirst()) {
			ret = true;
		}
		cursor.close();
		return ret;
	}
	
	/**
	 * ��ѯ���豸��Ϣ�����ڻ����ݿ��Ƿ����
	 * @param accountname
	 * @param token
	 * @param phonetype
	 * @return
	 */
	public static boolean isExistData(String accountname, String token, String phonetype) {
		boolean ret = false;
		DBOpen();
		String sql = "select * from " + TBL_ACCOUNT_LIST
				+ " where accountname='" + accountname + "'"
				+ " and token='" + token + "'"
				+ " and phonetype='" + phonetype + "'";
		MyLog.print(sql);
		Cursor cursor = mDatabase.rawQuery(sql, null);
		if (cursor.moveToFirst()) {
			ret = true;
		}
		cursor.close();
		return ret;
	}

	/**
	 * ����ʺ�
	 * @param account SIP�˺�
	 * @param token ����token
	 * @param mobiletype  �ֻ�����  1ΪAndroid  2ΪIOS
	 * @return 0-��ӳɹ�, -1 �ѵ��ʺŵ��������, -2 �ʺ��Ѵ���
	 */
	public static int addAccount(String account, String token, String mobiletype) {
		synchronized (mLock) {
			if (isExistData(account, token, mobiletype))
				return -2;
			if (getAccountCount() >= ACCOUNT_MACNUM)
				return -1;
			DBOpen();
			String sql = "insert into " + TBL_ACCOUNT_LIST + " values(null, '"
					+ account + "', '" + account + "', 1, '" + token + "', '" + mobiletype + "')";
			MyLog.print("addAccount sql=" + sql);
			mDatabase.execSQL(sql);
		}
		return 0;
	}

	/**
	 * ɾ�����а��豸
	 */
	public static void clearAccount() {
		synchronized (mLock) {
			DBOpen();
			String sql = "delete from " + TBL_ACCOUNT_LIST;
			MyLog.print(sql);
			mDatabase.execSQL(sql);
		}
	}
	
	/**
	 * ɾ��ĳ���͵������豸
	 * @param type
	 */
	public static void clearAccount(int type) {
		synchronized (mLock) {
			String sql;
			DBOpen();
			if(type == 0) {
				sql = "delete from " + TBL_ACCOUNT_LIST;
			} else {
				sql = "delete from " + TBL_ACCOUNT_LIST + " where phonetype=" + type;
			}
			MyLog.print(sql);
			mDatabase.execSQL(sql);
		}
	}

	/**
	 * ɾ���û�
	 * @param account
	 */
	public static void delAccount(String account) {
		synchronized (mLock) {
			if (!isAccountExist(account))
				return;
			DBOpen();
			String sql = "delete from " + TBL_ACCOUNT_LIST
					+ " where accountname='" + account + "'";
			MyLog.print(sql);
			mDatabase.execSQL(sql);
		}
	}
	
	/**
	 * ɾ���˺�
	 * @param account SIP�˺�
	 * @param token
	 */
	public static void delAccountByToken(String account, String token) {
		synchronized (mLock) {
			if (!isTokenExist(token))
				return;
			if (!isAccountExist(account))
				return;
			DBOpen();
			String sql = "delete from " + TBL_ACCOUNT_LIST
					+ " where accountname='" + account + "'" + " and token='" + token + "'";
			MyLog.print(sql);
			mDatabase.execSQL(sql);
		}
	}

	/**
	 * �����˺�����
	 * @param account
	 * @param online
	 */
	public static void setAccountOnline(String account, boolean online) {
		synchronized (mLock) {
			if (!isAccountExist(account))
				return;
			DBOpen();
			int isonline = online ? 1 : 0;
			String sql = "update " + TBL_ACCOUNT_LIST + " set isonline="
					+ isonline + " where accountname='" + account + "'";
			MyLog.print(sql);
			mDatabase.execSQL(sql);
		}
	}

	/**
	 * ��ȡAccount�б�
	 * @return
	 */
	public static List<String> getAccountList() {
		List<String> accounts = new ArrayList<String>();
		DBOpen();
		String sql = "select * from " + TBL_ACCOUNT_LIST + " where isonline=1";
		Cursor cursor = mDatabase.rawQuery(sql, null);
		if (cursor.moveToFirst()) {
			String acc = cursor.getString(1);
			accounts.add(acc);
		}
		while (cursor.moveToNext()) {
			String acc = cursor.getString(1);
			accounts.add(acc);
		}
		cursor.close();
		MyLog.print("accounts size= " + accounts.size());
		return accounts;
	}
	
	/**
	 * ���ֻ����Ͳ����û�
	 * @param type
	 * @return
	 */
	public static List<String> getAccountByPhoneType(int type){
		List<String> accounts = new ArrayList<String>();
		DBOpen();
		String sql = null;
		if (type == 1) {
			sql = "select * from " + TBL_ACCOUNT_LIST + " where phonetype=1";
		} else if (type == 2) {
			sql = "select * from " + TBL_ACCOUNT_LIST + " where phonetype=2";
		} else if (type == 0) {
			sql = "select * from " + TBL_ACCOUNT_LIST;
		}
		Cursor cursor = mDatabase.rawQuery(sql, null);
		if (cursor.moveToFirst()) {
			String acc = cursor.getString(1);
			accounts.add(acc);
		}
		while (cursor.moveToNext()) {
			String acc = cursor.getString(1);
			accounts.add(acc);
		}
		cursor.close();
		MyLog.print("accounts size= " + accounts.size());
		return accounts;
	}
	
	/**
	 * ��ȡToken�б�
	 * @return
	 */
	public static List<String> getTokenList() {
		List<String> tokens = new ArrayList<String>();
		DBOpen();
		String sql = "select * from " + TBL_ACCOUNT_LIST;
		Cursor cursor = mDatabase.rawQuery(sql, null);
		if (cursor.moveToFirst()) {
			String acc = cursor.getString(4);
			tokens.add(acc);
		}
		while (cursor.moveToNext()) {
			String acc = cursor.getString(4);
			tokens.add(acc);
		}
		cursor.close();
		MyLog.print("tokens size= " + tokens.size());
		return tokens;
	}
	
	/**
	 * ����phoneType��ȡToken�б�
	 * @return
	 */
	public static List<String> getTokenByTypeList(String type) {
		List<String> tokens = new ArrayList<String>();
		DBOpen();
		String sql = "select * from " + TBL_ACCOUNT_LIST + " where phonetype=" + type + " and isonline=1";
		Cursor cursor = mDatabase.rawQuery(sql, null);
		if (cursor.moveToFirst()) {
			String acc = cursor.getString(4);
			tokens.add(acc);
		}
		while (cursor.moveToNext()) {
			String acc = cursor.getString(4);
			tokens.add(acc);
		}
		cursor.close();
		MyLog.print("tokens size= " + tokens.size());
		return tokens;
	}
	
	/**
	 * �����ֻ����Ͳ��Ұ��û�
	 * @param type
	 * @return
	 */
	public static ArrayList<BindAccountInfo> getAccountByPhonetpye(String type) {
		ArrayList<BindAccountInfo> list = new ArrayList<BindAccountInfo>();
		DBOpen();
		//���ֶ�PhoneType�е�һ���ַ��Ĳ�ѯ
		String sql = "select * from " + TBL_ACCOUNT_LIST + " where substr(phonetype, 1, 1) = " + "'" + type + "'";
		Cursor cursor = mDatabase.rawQuery(sql, null);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				int db_id = cursor.getInt(cursor.getColumnIndex("_id"));
				String accountname = cursor.getString(cursor.getColumnIndex("accountname"));
				String accountpwd = cursor.getString(cursor.getColumnIndex("accountpwd"));
				int isonline = cursor.getInt(cursor.getColumnIndex("isonline"));
				String token = cursor.getString(cursor.getColumnIndex("token"));
				String phonetype = cursor.getString(cursor.getColumnIndex("phonetype"));
				BindAccountInfo bindaccount = new BindAccountInfo(db_id, accountname, accountpwd, isonline, token, phonetype);
				list.add(bindaccount);
			}
		}
		MyLog.print("sql=" + sql + " listSize=" + list.size());
		return list;
	}
	
	/**
	 * ��ȡ�˺���Ϣ��¼
	 */
	public static ArrayList<BindAccountInfo> getAccountInfoList() {
		ArrayList<BindAccountInfo> list = new ArrayList<BindAccountInfo>();
		DBOpen();
		Cursor cursor = mDatabase.query(TBL_ACCOUNT_LIST, null, null, null, null,
				null, null);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				int db_id = cursor.getInt(cursor.getColumnIndex("_id"));
				String accountname = cursor.getString(cursor.getColumnIndex("accountname"));
				String accountpwd = cursor.getString(cursor.getColumnIndex("accountpwd"));
				int isonline = cursor.getInt(cursor.getColumnIndex("isonline"));
				String token = cursor.getString(cursor.getColumnIndex("token"));
				String phonetype = cursor.getString(cursor.getColumnIndex("phonetype"));
				BindAccountInfo bindaccount = new BindAccountInfo(db_id, accountname, accountpwd, isonline, token, phonetype);
				list.add(bindaccount);
			}
		}
		return list;
	}
	
	/**
	 * ɾ��һ�����豸
	 * @param db_id
	 */
	public static void deleteAccount(int db_id) {
		synchronized (mLock) {
			DBOpen();
			String args[] = { String.valueOf(db_id) };
			mDatabase.delete(TBL_ACCOUNT_LIST, "_id=?", args);
		}
	}

	// ------ͨ����¼---------------------------------------------------------------------------------------

	/**
	 * @���ܣ����Ӻ��м�¼�����ݿ��� ���п�ʼ������ʱ��С��1.5sʱ�������¼
	 */
	public static void addCallLog(CallInfomation info) {
		synchronized (mLock) {
			DBOpen();
			String[] types = new String[] { String.valueOf(info.getType()) };
			Cursor cursor = mDatabase.query(TBL_CALL_INFO, null, "calltype=?", types,
					null, null, "starttime desc");
			if (cursor != null) {
				if (cursor.getCount() >= MAX_COUNT) {
					if (cursor.moveToLast()) {
						int db_id = cursor.getInt(cursor.getColumnIndex("_id"));
						String args[] = { String.valueOf(db_id) };
						mDatabase.delete(TBL_CALL_INFO, "_id=?", args);
					}
				}
			}
			ContentValues values = new ContentValues();
			values.put("remotecode", info.getRemoteCode());
			values.put("calltype", info.getType());
			values.put("starttime", info.getStartTime());
			values.put("accepttime", info.getAcceptTime());
			values.put("endtime", info.getEndTime());
			values.put("isopenlock", info.isOpenLock());
			values.put("isread", info.isRead());
			if (info.getEndTime() - info.getStartTime() > 1500 && mDatabase.isOpen()) {
				MyLog.print("addCallLog��ʱ������1.5�룬�����¼ " + values.toString());
				mDatabase.insert(TBL_CALL_INFO, null, values);
			}
		}
	}

	/**
	 * @���ܣ��޸����ݿ��еĺ��м�¼
	 */
	public static void modifyCallLog(CallInfomation info) {
		int id = info.getDb_id();
		synchronized (mLock) {
			DBOpen();
			if (id == 0) {
				MyLog.print("modifyCallLog id cannot be zero");
				return;
			}
			ContentValues values = new ContentValues();
			values.put("remotecode", info.getRemoteCode());
			values.put("calltype", info.getType());
			values.put("starttime", info.getStartTime());
			values.put("accepttime", info.getAcceptTime());
			values.put("endtime", info.getEndTime());
			values.put("isopenlock", info.isOpenLock());
			values.put("isread", info.isRead());
			String args[] = { String.valueOf(id) };
			mDatabase.update(TBL_CALL_INFO, values, "_id=?", args);
		}
	}

	/**
	 * @���ܣ�ɾ�����ݿ���ָ��ID�ĺ��м�¼
	 * @������int db_id - ���ݿ����Զ����ɵ�_id
	 */
	public static void deleteCallLog(int db_id) {
		synchronized (mLock) {
			DBOpen();
			String args[] = { String.valueOf(db_id) };
			mDatabase.delete(TBL_CALL_INFO, "_id=?", args);
		}
	}

	/**
	 * @���ܣ�ɾ����Ӧ���͵����к��м�¼
	 * @������int type ��CallInfo���е�CALLOUT_ACCEPT��ֵ
	 * @��ע�������������ʹ�ã���CALLOUT_ACCEPT | CALLOUT_UNACCEPT
	 */
	public static void deleteAllCallLog(int type) {
		synchronized (mLock) {
			DBOpen();
			List<String> list = new ArrayList<String>();
			if ((type & 0x3F) != 0) {
				if ((type & CallInfomation.CALL_IN_ACCEPT) == CallInfomation.CALL_IN_ACCEPT) {
					list.add(String.valueOf(CallInfomation.CALL_IN_ACCEPT));
				}
				if ((type & CallInfomation.CALL_IN_UNACCEPT) == CallInfomation.CALL_IN_UNACCEPT) {
					list.add(String.valueOf(CallInfomation.CALL_IN_UNACCEPT));
				}
				if ((type & CallInfomation.CALL_OUT_ACCEPT) == CallInfomation.CALL_OUT_ACCEPT) {
					list.add(String.valueOf(CallInfomation.CALL_OUT_ACCEPT));
				}
				if ((type & CallInfomation.CALL_OUT_UNACCEPT) == CallInfomation.CALL_OUT_UNACCEPT) {
					list.add(String.valueOf(CallInfomation.CALL_OUT_UNACCEPT));
				}
				if ((type & CallInfomation.SEE_ACCEPT) == CallInfomation.SEE_ACCEPT) {
					list.add(String.valueOf(CallInfomation.SEE_ACCEPT));
				}
				if ((type & CallInfomation.SEE_UNACCEPT) == CallInfomation.SEE_UNACCEPT) {
					list.add(String.valueOf(CallInfomation.SEE_UNACCEPT));
				}
			}
			String[] types = null;
			String where = null;
			if (list.size() > 0) {
				types = list.toArray(new String[list.size()]);
				where = "calltype=?";
				for (int i = 1; i < list.size(); i++) {
					where += new String(" or calltype=?");
				}
			}
			Cursor cursor = mDatabase.query(TBL_CALL_INFO, null, where, types, null,
					null, null);

			if (cursor != null) {
				while (cursor.moveToNext()) {
					int db_id = cursor.getInt(cursor.getColumnIndex("_id"));
					String args[] = { String.valueOf(db_id) };
					mDatabase.delete(TBL_CALL_INFO, "_id=?", args);
				}
			}
		}
	}

	/**
	 * @���ܣ������ݿ��л�ȡ���м�¼
	 * @������int type ��CallInfo���е�CALLOUT_ACCEPT��ֵ
	 * @��ע�������������ʹ�ã���CALLOUT_ACCEPT | CALLOUT_UNACCEPT
	 */
	public static ArrayList<CallInfomation> getCallLogList(int type) {
		ArrayList<CallInfomation> callLogList = new ArrayList<CallInfomation>();
		DBOpen();
		if (mDatabase != null) {
			List<String> list = new ArrayList<String>();
			// ��6�����ͼ�������111111��ʮ������0x3F
			if ((type & 0x3F) != 0) {
				if ((type & CallInfomation.CALL_IN_ACCEPT) == CallInfomation.CALL_IN_ACCEPT) {
					list.add(String.valueOf(CallInfomation.CALL_IN_ACCEPT));
				}
				if ((type & CallInfomation.CALL_IN_UNACCEPT) == CallInfomation.CALL_IN_UNACCEPT) {
					list.add(String.valueOf(CallInfomation.CALL_IN_UNACCEPT));
				}
				if ((type & CallInfomation.CALL_OUT_ACCEPT) == CallInfomation.CALL_OUT_ACCEPT) {
					list.add(String.valueOf(CallInfomation.CALL_OUT_ACCEPT));
				}
				if ((type & CallInfomation.CALL_OUT_UNACCEPT) == CallInfomation.CALL_OUT_UNACCEPT) {
					list.add(String.valueOf(CallInfomation.CALL_OUT_UNACCEPT));
				}
				if ((type & CallInfomation.SEE_ACCEPT) == CallInfomation.SEE_ACCEPT) {
					list.add(String.valueOf(CallInfomation.SEE_ACCEPT));
				}
				if ((type & CallInfomation.SEE_UNACCEPT) == CallInfomation.SEE_UNACCEPT) {
					list.add(String.valueOf(CallInfomation.SEE_UNACCEPT));
				}
			}
			String[] types = null;
			String where = null;
			if (list.size() > 0) {
				types = list.toArray(new String[list.size()]);
				where = "calltype=?";
				for (int i = 1; i < list.size(); i++) {
					where += new String(" or calltype=?");
				}
			}
			Cursor cursor = mDatabase.query(TBL_CALL_INFO, null, where, types,
					null, null, "starttime desc");

			if (cursor != null) {
				while (cursor.moveToNext()) {
					int db_id = cursor.getInt(cursor.getColumnIndex("_id"));
					String remoteCode = cursor.getString(cursor
							.getColumnIndex("remotecode"));
					long startTime = cursor.getLong(cursor
							.getColumnIndex("starttime"));
					long acceptTime = cursor.getLong(cursor
							.getColumnIndex("accepttime"));
					long endTime = cursor.getLong(cursor
							.getColumnIndex("endtime"));
					int callType = cursor.getInt(cursor
							.getColumnIndex("calltype"));
					boolean isOpenLock = cursor.getInt(cursor
							.getColumnIndex("isopenlock")) == 1;
					boolean isRead = cursor.getInt(cursor
							.getColumnIndex("isread")) == 1;
					CallInfomation info = new CallInfomation(db_id, remoteCode,
							callType, startTime, acceptTime, endTime,
							isOpenLock, isRead);
					callLogList.add(info);
				}
			}
		}
		return callLogList;
	}
	
	/**
	 * ��ȡͨ����¼������
	 * @return
	 */
	private static int countCallInfomation() {
		int count = 0;
		DBOpen();
		String sql = "select count(_id) from " + TBL_CALL_INFO;
		Cursor cursor = mDatabase.rawQuery(sql, null);
		if (cursor.moveToFirst()) {
			count = cursor.getInt(0);
		}
		cursor.close();
		MyLog.print(sql + "  , count= " + count);
		return count;
	}
	
	/**
	 * ��ѯͨ����¼���һ������
	 * @return
	 */
	public static CallInfomation quaryLastCall() {
		int num = countCallInfomation() - 1;
		String limit = String.valueOf(num) + ",1";
		Cursor cursor = mDatabase.query(TBL_CALL_INFO, null, null, null, null, null, null, limit);
		cursor.moveToLast();
		int db_id = cursor.getInt(cursor.getColumnIndex("_id"));
		String remoteCode = cursor.getString(cursor
				.getColumnIndex("remotecode"));
		long startTime = cursor.getLong(cursor
				.getColumnIndex("starttime"));
		long acceptTime = cursor.getLong(cursor
				.getColumnIndex("accepttime"));
		long endTime = cursor.getLong(cursor
				.getColumnIndex("endtime"));
		int callType = cursor.getInt(cursor
				.getColumnIndex("calltype"));
		boolean isOpenLock = cursor.getInt(cursor
				.getColumnIndex("isopenlock")) == 1;
		boolean isRead = cursor.getInt(cursor
				.getColumnIndex("isread")) == 1;
		CallInfomation info = new CallInfomation(db_id, remoteCode,
				callType, startTime, acceptTime, endTime,
				isOpenLock, isRead);
		cursor.close();
		return info;
	}

	// ------������¼---------------------------------------------------------------------------------------

	/**
	 * @���ܣ����Ӱ�����¼�����ݿ���
	 */
	public static void addSafeModeLog(SafeModeInfo info) {
		synchronized (mLock) {
			DBOpen();
			Cursor cursor = mDatabase.query(TBL_SAFE_MODE_INFO, null, null, null, null,
					null, "time desc");
			if (cursor != null) {
				if (cursor.getCount() >= MAX_COUNT) {
					if (cursor.moveToLast()) {
						int db_id = cursor.getInt(cursor.getColumnIndex("_id"));
						String args[] = { String.valueOf(db_id) };
						mDatabase.delete(TBL_SAFE_MODE_INFO, "_id=?", args);
					}
				}
			}
			ContentValues values = new ContentValues();
			values.put("safemode", info.getMode());
			values.put("time", info.getTime());
			values.put("issuccess", info.getIsSuccess());
			mDatabase.insert(TBL_SAFE_MODE_INFO, null, values);
		}
	}
	
	/**
	 * @���ܣ��޸����ݿ��еİ�����¼
	 * @param info
	 */
	public static void modifySafeModeLog(SafeModeInfo info) {
		int id = info.getDb_id();
		synchronized (mLock) {
			DBOpen();
			if (id == 0) {
				MyLog.print("modifySafeModeLog id cannot be zero");
				return;
			}
			ContentValues values = new ContentValues();
			values.put("safemode", info.getMode());
			values.put("time", info.getTime());
			values.put("issuccess", info.getIsSuccess());
			String args[] = { String.valueOf(id) };
			mDatabase.update(TBL_SAFE_MODE_INFO, values, "_id=?", args);
		}
	}

	/**
	 * @���ܣ�ɾ�����ݿ���ָ��ID�İ�����¼
	 * @������int db_id - ���ݿ����Զ����ɵ�_id
	 */
	public static void deleteSafeModeLog(int db_id) {
		synchronized (mLock) {
			DBOpen();
			String args[] = { String.valueOf(db_id) };
			mDatabase.delete(TBL_SAFE_MODE_INFO, "_id=?", args);
		}
	}

	/**
	 * @���ܣ�ɾ�����в�����¼
	 */
	public static void deleteAllSafeModeLog() {
		synchronized (mLock) {
			DBOpen();
			Cursor cursor = mDatabase.query(TBL_SAFE_MODE_INFO, null, null, null, null,
					null, null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					int db_id = cursor.getInt(cursor.getColumnIndex("_id"));
					String args[] = { String.valueOf(db_id) };
					mDatabase.delete(TBL_SAFE_MODE_INFO, "_id=?", args);
				}
			}
		}
	}

	/**
	 * ��ȡ������¼
	 */
	public static ArrayList<SafeModeInfo> getSafeModeInfoList() {
		ArrayList<SafeModeInfo> list = new ArrayList<SafeModeInfo>();
		DBOpen();
		Cursor cursor = mDatabase.query(TBL_SAFE_MODE_INFO, null, null, null, null,
				null, "time desc");
		if (cursor != null) {
			while (cursor.moveToNext()) {
				int db_id = cursor.getInt(cursor.getColumnIndex("_id"));
				int mode = cursor.getInt(cursor.getColumnIndex("safemode"));
				long time = cursor.getLong(cursor.getColumnIndex("time"));
				boolean isSuccess = cursor.getInt(cursor.getColumnIndex("issuccess")) == 1;
				SafeModeInfo info = new SafeModeInfo(db_id, mode, time);
				info.setIsSuccess(isSuccess);
				list.add(info);
			}
		}
		return list;
	}

	// ------������¼---------------------------------------------------------------------------------------

	/**
	 * @���ܣ����ӱ�����¼�����ݿ���
	 */
	public static void addAlarmLog(AlarmLog info) {
		synchronized (mLock) {
			DBOpen();
			ContentValues values = new ContentValues();
			values.put("safemode", info.getMode());
			values.put("areanum", info.getAreaNum());
			values.put("areaname", info.getAreaName());
			values.put("areatype", info.getAreaType());
			values.put("time", info.getTime());
			values.put("issuccess", info.getIsSuccess());
			values.put("isread", info.getIsRead());
			Cursor cursor = mDatabase.query(TBL_AlARM_INFO, null, null, null, null,
					null, "time desc");
			if (cursor != null) {
				if (cursor.getCount() >= MAX_COUNT) {
					if (cursor.moveToLast()) {
						int db_id = cursor.getInt(cursor.getColumnIndex("_id"));
						String args[] = { String.valueOf(db_id) };
						mDatabase.delete(TBL_AlARM_INFO, "_id=?", args);
					}
				}
			}
			mDatabase.insert(TBL_AlARM_INFO, null, values);
		}
	}

	/**
	 * @���ܣ��޸����ݿ��еı�����¼
	 * @param info
	 */
	public static void modifyAlarmLog(AlarmLog info) {
		int id = info.getDb_id();
		synchronized (mLock) {
			DBOpen();
			if (id == 0) {
				MyLog.print("modifyAlarmLog id cannot be zero");
				return;
			}
			ContentValues values = new ContentValues();
			values.put("safemode", info.getMode());
			values.put("areanum", info.getAreaNum());
			values.put("areaname", info.getAreaName());
			values.put("areatype", info.getAreaType());
			values.put("time", info.getTime());
			values.put("issuccess", info.getIsSuccess());
			values.put("isread", info.getIsRead());
			String args[] = { String.valueOf(id) };
			mDatabase.update(TBL_AlARM_INFO, values, "_id=?", args);
		}
	}

	/**
	 * @���ܣ�ɾ�����ݿ���ָ��ID�ı�����¼
	 * @������int db_id - ���ݿ����Զ����ɵ�_id
	 */
	public static void deleteAlarmLog(int db_id) {
		synchronized (mLock) {
			DBOpen();
			String args[] = { String.valueOf(db_id) };
			mDatabase.delete(TBL_AlARM_INFO, "_id=?", args);
		}
	}

	/**
	 * @���ܣ�ɾ�����б�����¼
	 */
	public static void deleteAllAlarmLog() {
		synchronized (mLock) {
			DBOpen();
			Cursor cursor = mDatabase.query(TBL_AlARM_INFO, null, null, null, null,
					null, null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					int db_id = cursor.getInt(cursor.getColumnIndex("_id"));
					String args[] = { String.valueOf(db_id) };
					mDatabase.delete(TBL_AlARM_INFO, "_id=?", args);
				}
			}
		}
	}

	/**
	 * ��ȡ������¼
	 */
	public static ArrayList<AlarmLog> getAlarmLogList() {
		ArrayList<AlarmLog> list = new ArrayList<AlarmLog>();
		DBOpen();
		Cursor cursor = mDatabase.query(TBL_AlARM_INFO, null, null, null, null,
				null, "time desc");
		if (cursor != null) {
			while (cursor.moveToNext()) {
				int db_id = cursor.getInt(cursor.getColumnIndex("_id"));
				int mode = cursor.getInt(cursor.getColumnIndex("safemode"));
				int num = cursor.getInt(cursor.getColumnIndex("areanum"));
				int name = cursor.getInt(cursor.getColumnIndex("areaname"));
				int type = cursor.getInt(cursor.getColumnIndex("areatype"));
				long time = cursor.getLong(cursor.getColumnIndex("time"));
				boolean isSuccess = cursor.getInt(cursor.getColumnIndex("issuccess")) == 1;
				boolean isRead = cursor.getInt(cursor.getColumnIndex("isread")) == 1;
				AlarmLog alarmLog = new AlarmLog(db_id, mode, num, name, type, time);
				alarmLog.setIsSuccess(isSuccess);
				alarmLog.setIsRead(isRead);
				list.add(alarmLog);
			}
		}
		return list;
	}
	
	// ------����¼��---------------------------------------------------------------------------------------

	/**
	 * @���ܣ����ӱ���¼�����ݿ���
	 */
	public static void addAlarmVideo(AlarmVideo info) {
		synchronized (mLock) {
			DBOpen();
			ContentValues values = new ContentValues();
			values.put("time", info.time);
			values.put("path", info.path);
			values.put("area", info.area);
			values.put("type", info.type);
			Cursor cursor = mDatabase.query(TBL_ALARM_VIDEO, null, null, null, null,
					null, "time desc");
			if (cursor != null) {
				if (cursor.getCount() >= MAX_COUNT) {
					if (cursor.moveToLast()) {
						int db_id = cursor.getInt(cursor.getColumnIndex("_id"));
						String args[] = { String.valueOf(db_id) };
						mDatabase.delete(TBL_ALARM_VIDEO, "_id=?", args);
					}
				}
			}
			mDatabase.insert(TBL_ALARM_VIDEO, null, values);
		}
	}
	
	/**
	 * @���ܣ�ɾ�����ݿ���ָ��ID�ı���¼��
	 * @������int db_id - ���ݿ����Զ����ɵ�_id
	 */
	public static void deleteAlarmVideo(int db_id) {
		synchronized (mLock) {
			DBOpen();
			String args[] = { String.valueOf(db_id) };
			mDatabase.delete(TBL_ALARM_VIDEO, "_id=?", args);
		}
	}

	/**
	 * @���ܣ�ɾ�����б���¼��
	 */
	public static void deleteAllAlarmVideo() {
		synchronized (mLock) {
			DBOpen();
			Cursor cursor = mDatabase.query(TBL_ALARM_VIDEO, null, null, null, null,
					null, null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					int db_id = cursor.getInt(cursor.getColumnIndex("_id"));
					String args[] = { String.valueOf(db_id) };
					mDatabase.delete(TBL_ALARM_VIDEO, "_id=?", args);
				}
			}
		}
	}
	
	/**
	 * ��ȡ����¼��
	 */
	public static ArrayList<AlarmVideo> getAlarmVideoList() {
		ArrayList<AlarmVideo> list = new ArrayList<AlarmVideo>();
		DBOpen();
		Cursor cursor = mDatabase.query(TBL_ALARM_VIDEO, null, null, null, null,
				null, "time desc");
		if (cursor != null) {
			while (cursor.moveToNext()) {
				int db_id = cursor.getInt(cursor.getColumnIndex("_id"));
				String time = cursor.getString(cursor.getColumnIndex("time"));
				String path = cursor.getString(cursor.getColumnIndex("path"));
				int area = cursor.getInt(cursor.getColumnIndex("area"));
				int type = cursor.getInt(cursor.getColumnIndex("type"));
				AlarmVideo alarmVideo = new AlarmVideo();
				alarmVideo.id = db_id;
				alarmVideo.time = time;
				alarmVideo.path = path;
				alarmVideo.area = area;
				alarmVideo.type = type;
				list.add(alarmVideo);
			}
		}
		return list;
	}
	
	// ------С����Ϣ---------------------------------------------------------------------------------------

	/**
	 * @���ܣ�������Ϣ��¼�����ݿ���
	 */
	public static void addMessageLog(MessageInfo info) {
		DBOpen();
		ContentValues values = new ContentValues();
		values.put("personal", info.isPersonal());
		values.put("time", info.getTime());
		values.put("title", info.getTitle());
		values.put("body", info.getBody());
		values.put("resname", info.getResName());
		values.put("isjpg", info.isJpg());
		values.put("read", info.isRead());
		String[] types = null;
		if (info.isPersonal()) {
			types = new String[1];
			types[0] = "1";
		} else {
			types = new String[1];
			types[0] = "0";
		}
		Cursor cursor = mDatabase.query(TBL_MESSAGE_INFO, null, "personal=?",
				types, null, null, null);
		if (cursor != null) {
			if (cursor.getCount() >= MAX_COUNT) {
				if (cursor.moveToFirst()) {
					int db_id = cursor.getInt(cursor.getColumnIndex("_id"));
					String args[] = { String.valueOf(db_id) };
					mDatabase.delete(TBL_MESSAGE_INFO, "_id=?", args);
				}
			}
		}
		mDatabase.insert(TBL_MESSAGE_INFO, null, values);
	}

	/**
	 * @���ܣ��޸����ݿ��е���Ϣ��¼
	 */
	public static void modifyMessageLog(MessageInfo info) {
		int id = info.getDb_id();
		synchronized (mLock) {
			DBOpen();
			if (id == 0) {
				MyLog.print("modifyMessageLog id cannot be zero");
				return;
			}
			ContentValues values = new ContentValues();
			values.put("personal", info.isPersonal());
			values.put("time", info.getTime());
			values.put("title", info.getTitle());
			values.put("body", info.getBody());
			values.put("resname", info.getResName());
			values.put("isjpg", info.isJpg());
			values.put("read", info.isRead());
			String args[] = { String.valueOf(id) };
			mDatabase.update(TBL_MESSAGE_INFO, values, "_id=?", args);
		}
	}

	/**
	 * @���ܣ�ɾ�����ݿ���ָ��ID����Ϣ��¼
	 * @������int db_id - ���ݿ����Զ����ɵ�_id
	 */
	public static void deleteMessageLog(int db_id) {
		synchronized (mLock) {
			DBOpen();
			String args[] = { String.valueOf(db_id) };
			mDatabase.delete(TBL_MESSAGE_INFO, "_id=?", args);
		}
	}

	/**
	 * @���ܣ�ɾ����Ӧ���͵�������Ϣ��¼
	 * @������int type - 1-������Ϣ��2-������Ϣ��3-������Ϣ
	 */
	public static void deleteAllMessageLog(int type) {
		synchronized (mLock) {
			DBOpen();
			String where = null;
			String[] types = null;
			if (type == (MessageInfo.PERSONAL | MessageInfo.PUBLIC)) {

			} else if (type == MessageInfo.PERSONAL) {
				where = "personal=?";
				types = new String[1];
				types[0] = "1";
			} else if (type == MessageInfo.PUBLIC) {
				where = "personal=?";
				types = new String[1];
				types[0] = "0";
			}
			Cursor cursor = mDatabase.query(TBL_MESSAGE_INFO, null, where, types, null,
					null, null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					int db_id = cursor.getInt(cursor.getColumnIndex("_id"));
					String args[] = { String.valueOf(db_id) };
					mDatabase.delete(TBL_MESSAGE_INFO, "_id=?", args);
				}
			}
		}
	}

	/**
	 * @���ܣ������ݿ��л�ȡָ�����͵ĵ�δ����Ϣ����
	 * @������int type PERSONAL��PUBLIC��PERSONAL|PUBLIC
	 */
	public static int getUnReadMessageLogNum(int type) {
		int count = 0;
		DBOpen();
		String where = null;
		String[] types = null;
		if (type == (MessageInfo.PERSONAL | MessageInfo.PUBLIC)) {
			where = "read=?";
			types = new String[1];
			types[0] = "0";
		} else if (type == MessageInfo.PERSONAL) {
			where = "personal=? and read=?";
			types = new String[2];
			types[0] = "1";
			types[1] = "0";
		} else if (type == MessageInfo.PUBLIC) {
			where = "personal=? and read=?";
			types = new String[2];
			types[0] = "0";
			types[1] = "0";
		}
		Cursor cursor = mDatabase.query(TBL_MESSAGE_INFO, new String[] {
				"personal", "read" }, where, types, null, null, null);
		if (cursor != null) {
			count = cursor.getCount();
		}
		return count;
	}

	/**
	 * @���ܣ������ݿ��л�ȡָ�����͵���Ϣ��¼
	 * @������int type PERSONAL��PUBLIC��PERSONAL|PUBLIC
	 */
	public static ArrayList<MessageInfo> getMessageLogList(int type) {
		ArrayList<MessageInfo> infoList = new ArrayList<MessageInfo>();
		DBOpen();
		String where = null;
		String[] types = null;
		if (type == (MessageInfo.PERSONAL | MessageInfo.PUBLIC)) {

		} else if (type == MessageInfo.PERSONAL) {
			where = "personal=?";
			types = new String[1];
			types[0] = "1";
		} else if (type == MessageInfo.PUBLIC) {
			where = "personal=?";
			types = new String[1];
			types[0] = "0";
		}
		Cursor cursor = mDatabase.query(TBL_MESSAGE_INFO, null, where, types, null,
				null, "time desc");
		if (cursor != null) {

			while (cursor.moveToNext()) {
				int db_id = cursor.getInt(cursor.getColumnIndex("_id"));
				boolean personal = cursor.getInt(cursor.getColumnIndex("personal")) == 1;
				String time = cursor.getString(cursor.getColumnIndex("time"));
				String title = cursor.getString(cursor.getColumnIndex("title"));
				String body = cursor.getString(cursor.getColumnIndex("body"));
				String resName = cursor.getString(cursor.getColumnIndex("resname"));
				boolean isJpg = cursor.getInt(cursor.getColumnIndex("isjpg")) == 1;
				boolean isread = cursor.getInt(cursor.getColumnIndex("read")) == 1;
				MessageInfo info = new MessageInfo(db_id, time, title,
						body, resName, isJpg, personal, isread);
				infoList.add(info);
			}
		}
		return infoList;
	}
	
	/**
	 * ��ѯС����Ϣ������
	 * @return
	 */
	private static int countMessageInfo() {
		int count = 0;
		DBOpen();
		String sql = "select count(_id) from " + TBL_MESSAGE_INFO;
		Cursor cursor = mDatabase.rawQuery(sql, null);
		if (cursor.moveToFirst()) {
			count = cursor.getInt(0);
		}
		cursor.close();
		MyLog.print(sql + "  , С����Ϣcount= " + count);
		return count;
	}
	/**
	 * ��ȡ��һ����Ϣ
	 * @return
	 */
	public static MessageInfo queryLasgMessage() {
		int num = countMessageInfo() - 1;
		String limit = String.valueOf(num) + ",1";
		Cursor cursor = mDatabase.query(TBL_MESSAGE_INFO, null, null, null, null, null, null, limit);
		cursor.moveToLast();
		int db_id = cursor.getInt(cursor.getColumnIndex("_id"));
		boolean personal = cursor.getInt(cursor.getColumnIndex("personal")) == 1;
		String time = cursor.getString(cursor.getColumnIndex("time"));
		String title = cursor.getString(cursor.getColumnIndex("title"));
		String body = cursor.getString(cursor.getColumnIndex("body"));
		String resName = cursor.getString(cursor.getColumnIndex("resname"));
		boolean isJpg = cursor.getInt(cursor.getColumnIndex("isjpg")) == 1;
		boolean isread = cursor.getInt(cursor.getColumnIndex("read")) == 1;
		MessageInfo info = new MessageInfo(db_id, time, title,
				body, resName, isJpg, personal, isread);
		cursor.close();
		return info;
	}
	
	/**
	 * �������ڻ�SIP��Ϣ�����ݿ�
	 * @param info
	 */
	public static void addIndoorSip(IndoorSipInfo info) {
		DBOpen();
		ContentValues values = new ContentValues();
		values.put("deviceno", info.getDeviceNo());
		values.put("areano", info.getAreaNo());
		values.put("regionno", info.getRegionNo());
		values.put("buildingno", info.getBuildingNo());
		values.put("unitno", info.getUnitNo());
		values.put("houseno", info.getHouseNo());
		values.put("mac", info.getMac());
		values.put("ip", info.getIp());
		values.put("devicename", info.getDeviceName());
		values.put("devicetype", info.getDeviceType());
		values.put("devicepassword", info.getDevicePassword());
		values.put("position", info.getPosition());
		values.put("version", info.getVersion());
		values.put("ipstate", info.getIpState());
		values.put("housestate", info.getHouseState());
		values.put("sipid", info.getSipId());
		values.put("sippwd", info.getSipPwd());
		Cursor cursor = mDatabase.query(TBL_INDOORSIP_INFO, null, null, null, null, null, null);
		if (cursor != null) {
			if (cursor.getCount() >= MAX_COUNT) {
				if (cursor.moveToFirst()) {
					int db_id = cursor.getInt(cursor.getColumnIndex("_id"));
					String args[] = { String.valueOf(db_id) };
					mDatabase.delete(TBL_INDOORSIP_INFO, "_id=?", args);
				}
			}
		}
		mDatabase.insert(TBL_INDOORSIP_INFO, null, values);		
	}
	
	/**
	 * ��ȡ���ڻ�SIP��Ϣ�б�
	 * @return
	 */
	public static List<String> getIndoorSipList() {
		List<String> indoorsips = new ArrayList<String>();
		DBOpen();
		String sql = "select * from " + TBL_SAFE_MODE_INFO;
		Cursor cursor = mDatabase.rawQuery(sql, null);
		if (cursor.moveToFirst()) {
			String acc = cursor.getString(0);
			indoorsips.add(acc);
		}
		while (cursor.moveToNext()) {
			String acc = cursor.getString(0);
			indoorsips.add(acc);
		}
		cursor.close();
		MyLog.print("IndoorSip size= " + indoorsips.size());
		return indoorsips;
	}
	
	/**
	 * �ж����ڻ�SIP�˺��Ƿ��Ѿ�����
	 * @param sipid
	 * @return
	 */
	public static boolean isIndoorSipExist(String sipid) {
		boolean ret = false;
		DBOpen();
		String sql = "select sipid from " + TBL_INDOORSIP_INFO
				+ " where sipid='" + sipid + "'";
		MyLog.print(sql);
		Cursor cursor = mDatabase.rawQuery(sql, null);
		if (cursor.moveToFirst()) {
			ret = true;
		}
		cursor.close();
		return ret;
	}
	
	/**
	 * �������ڻ�SIP��Ϣ
	 * @param info
	 */
	public static void modifyIndoorSip(IndoorSipInfo info) {
		int id = info.getDb_id();
		synchronized (mLock) {
			DBOpen();
			if (id == 0) {
				MyLog.print("modifyIndoorSip id cannot be zero");
				return;
			}
			ContentValues values = new ContentValues();
			values.put("deviceno", info.getDeviceNo());
			values.put("areano", info.getAreaNo());
			values.put("regionno", info.getRegionNo());
			values.put("buildingno", info.getBuildingNo());
			values.put("unitno", info.getUnitNo());
			values.put("houseno", info.getHouseNo());
			values.put("mac", info.getMac());
			values.put("ip", info.getIp());
			values.put("devicename", info.getDeviceName());
			values.put("devicetype", info.getDeviceType());
			values.put("devicepassword", info.getDevicePassword());
			values.put("position", info.getPosition());
			values.put("version", info.getVersion());
			values.put("ipstate", info.getIpState());
			values.put("housestate", info.getHouseState());
			values.put("sipid", info.getSipId());
			values.put("sippwd", info.getSipPwd());
			String args[] = { String.valueOf(id) };
			mDatabase.update(TBL_INDOORSIP_INFO, values, "_id=?", args);
			MyLog.print("modifyIndoorSip success");
		}
	}
	
	/**
	 * ��ѯ���ڻ�SIP�˺ŵ�����
	 * @return
	 */
	public static int countIndoorSip() {
		int count = 0;
		DBOpen();
		String sql = "select count(*) from " + TBL_INDOORSIP_INFO;
		Cursor cursor = mDatabase.rawQuery(sql, null);
		if (cursor.moveToFirst()) {
			count = cursor.getInt(0);
		}
		cursor.close();
		MyLog.print(sql + "  , countIndoorSip= " + count);
		return count;
	}
	
	/**
	 * ��ѯ���ڻ�SIP�˺ŵĵ�һ��
	 * @return
	 */
	public static IndoorSipInfo queryFistSip() {
		IndoorSipInfo indoorSipInfo = new IndoorSipInfo();
		String limit = "1";
		Cursor cursor = mDatabase.query(TBL_INDOORSIP_INFO, null, null, null, null, null, null, limit);
		if (cursor.moveToFirst()) {
			indoorSipInfo.setDeviceNo(cursor.getString(1));
			indoorSipInfo.setAreaNo(cursor.getString(2));
			indoorSipInfo.setRegionNo(cursor.getString(3));
			indoorSipInfo.setBuildingNo(cursor.getString(4));
			indoorSipInfo.setUnitNo(cursor.getString(5));
			indoorSipInfo.setHouseNo(cursor.getString(6));
			indoorSipInfo.setMac(cursor.getString(7));
			indoorSipInfo.setIp(cursor.getString(8));
			indoorSipInfo.setDeviceName(cursor.getString(9));
			indoorSipInfo.setDeviceType(cursor.getString(10));
			indoorSipInfo.setDevicePassword(cursor.getString(11));
			indoorSipInfo.setPosition(cursor.getString(12));
			indoorSipInfo.setVersion(cursor.getString(13));
			indoorSipInfo.setIpState(cursor.getString(14));
			indoorSipInfo.setHouseState(cursor.getString(15));
			indoorSipInfo.setSipId(cursor.getString(16));
			indoorSipInfo.setSipPwd(cursor.getString(17));
		}
		cursor.close();
		MyLog.print("indoorSipInfo Fist:" + indoorSipInfo.toString());
		return indoorSipInfo;
	}

}

package com.dpower.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.dpower.domain.AlarmLog;
import com.dpower.domain.AlarmVideo;
import com.dpower.domain.BindAccountInfo;
import com.dpower.domain.CallInfomation;
import com.dpower.domain.MessageInfo;
import com.dpower.domain.SafeModeInfo;
import com.dpower.pub.dp2700.model.IndoorSipInfo;
import com.example.dpservice.DPSafeService;

/**
 * 自定义类，对数据库的操作，包括手机号列表,通话记录,布防记录,报警记录,小区信息等
 */
public class DPDBHelper {
	
	private static final int MAX_COUNT = 50; // 存储记录的最大数量
	public static final int ACCOUNT_MACNUM = 10; // 室内机绑定手机的最大数量
	private static final String DB_NAME = "lib2700_db.db";

	/** 键值对 */
	private static final String TBL_APP_STATUS = "localstatus";

	/** 绑定的手机的帐号 */
	private static final String TBL_ACCOUNT_LIST = "accountlist";

	/** 通话记录, 留影留言文件名为起始时间命令 */
	private static final String TBL_CALL_INFO = "callinfo";

	/** 布防记录 */
	private static final String TBL_SAFE_MODE_INFO = "safemodeinfo";

	/** 报警记录 */
	private static final String TBL_AlARM_INFO = "alarminfo";
	
	/** 报警录像 */
	private static final String TBL_ALARM_VIDEO = "alarmvideo";

	/** 小区信息 */
	private static final String TBL_MESSAGE_INFO = "messageinfo";
	
	/** 室内机SIP信息 */
	private static final String TBL_INDOORSIP_INFO = "indoorsipinfo";
	
	private static SQLiteDatabase mDatabase = null;
	private static Object mLock = new Object();
	private static Context mContext = null;

	/**
	 * 初始化数据库,每次启动APP时执行
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

		// 键值对
		if (!tabIsExist(TBL_APP_STATUS)) {
			// 名称， 对应的值
			mDatabase.execSQL("create table if not exists "
					+ TBL_APP_STATUS
					+ "("
					+ "statusname varchar(256) not null,statusvalue varchar(256) not null default 0)");
			MyLog.print("create " + TBL_APP_STATUS + " table success");
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('init_app', 1)"); // 0-false, 1-true
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('roomcode', '1999999999901')"); // 房号
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('message_mode', 0)"); // 留言模式
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('safe_protectiondelaytime', 30)"); // 布防延时时间
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('wan_dhcp', 0)"); // 0-false, 1-true
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('wan_ip', '0.0.0.0')"); // ip
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('wan_mask', '0.0.0.0')"); // 0-false, 1-true
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('wan_gw', '0.0.0.0')"); // 0-false, 1-true
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('wan_dns', '0.0.0.0')"); // 0-false, 1-true
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('brightness', 50)"); // 亮度
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('contrast', 50)"); // 对比度
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('saturability', 50)"); // 饱和度
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('hue', 50)"); // 色相
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('istophone', 1)"); // 是否呼叫转移到手机,0-不转移, 1-转移 
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('smart_home', 1)"); // 智能家居模式
			
			int defalutSafeMode = 8;
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('safe_connection', "
					+ (0xFFFFFFFF & ((1 << defalutSafeMode) - 1)) + ")"); // 安防探头的接法，高低电平
			if (ProjectConfigure.project == 1) {
				mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('projectPassword', '666666')"); // 工程密码
			} else {
				mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('projectPassword', '123456')"); // 工程密码
			}
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('holding_password', '654321')"); // 挟持密码
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('safe_password', '123456')"); // 安防密码
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('safe_mode', '1')"); // 当前防区类型
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('safe_enable0', 0)"); // 测试，以下5个模式默认都为禁用
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('safe_enable1', 0)"); // 撤防
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('safe_enable2', 0)"); // 夜间
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('safe_enable3', 0)"); // 在家
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('safe_enable4', 0)"); // 离家
			JSONArray jsonArray = new JSONArray();
			for (int i = 0; i < ConstConf.DEFAULT_SAFE_TANTOU; i++) {
				jsonArray.put(0);
			}
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('safe_alarmdelaytime0', '" + jsonArray.toString() + "')"); // 各个防区的延时时间
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('safe_alarmdelaytime1', '" + jsonArray.toString() + "')"); // 当前防区类型
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('safe_alarmdelaytime2', '" + jsonArray.toString() + "')"); // 当前防区类型
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('safe_alarmdelaytime3', '" + jsonArray.toString() + "')"); // 当前防区类型
			mDatabase.execSQL("insert into " + TBL_APP_STATUS + " values('safe_alarmdelaytime4', '" + jsonArray.toString() + "')"); // 当前防区类型
		}

		// 绑定的手机的帐号
		if (!tabIsExist(TBL_ACCOUNT_LIST)) {
			// 帐号， 是否在线 :1-在线，0-不在线
			mDatabase.execSQL("create table if not exists "
					+ TBL_ACCOUNT_LIST
					+ "("
					+ "_id integer primary key autoincrement,accountname varchar(256) not null" 
					+ ",accountpwd varchar(256) not null,isonline int not null,token varchar(256),phonetype varchar(50))");
			MyLog.print("create table " + TBL_ACCOUNT_LIST + " success");
		}

		// 通话记录
		if (!tabIsExist(TBL_CALL_INFO)) {
			// 对方帐号, 呼叫类型, 开始时间(开始时间为留影留言图片的文件名), 接听时间, 挂断时间, 是否开锁, 是否已读
			mDatabase.execSQL("create table if not exists "
					+ TBL_CALL_INFO
					+ "(_id integer primary key autoincrement,remotecode text,calltype tinyint"
					+ ",starttime int,accepttime int,endtime int,isopenlock bit,isread bit)");
			MyLog.print("create table " + TBL_CALL_INFO + " success");
		}

		// 布防记录
		if (!tabIsExist(TBL_SAFE_MODE_INFO)) {
			// 安防模式, 布防时间, 是否上报成功
			mDatabase.execSQL(" create table "
					+ TBL_SAFE_MODE_INFO
					+ "(_id integer primary key autoincrement,safemode int,time int,issuccess bit)");
			MyLog.print("create table " + TBL_SAFE_MODE_INFO + " success");
		}

		// 报警记录
		if (!tabIsExist(TBL_AlARM_INFO)) {
			// 安防模式, 防区号, 防区名, 类型, 时间, 是否上报成功, 是否阅读
			mDatabase.execSQL(" create table "
					+ TBL_AlARM_INFO
					+ "(_id integer primary key autoincrement,safemode int,areanum int,areaname int,areatype int,time int,issuccess bit,isread bit)");
			MyLog.print("create table " + TBL_AlARM_INFO + " success");
		}
		
		// 报警录像
		if (!tabIsExist(TBL_ALARM_VIDEO)) {
			// 时间, 路径, 区域, 类型
			mDatabase.execSQL(" create table "
					+ TBL_ALARM_VIDEO
					+ "(_id integer primary key autoincrement,time text,path text,area int,type int)");
			MyLog.print("create table " + TBL_ALARM_VIDEO + " success");
		}

		// 小区信息
		if (!tabIsExist(TBL_MESSAGE_INFO)) {
			// 是否个人消息, 时间, 标题, 内容, url, 图片, 是否已读
			mDatabase.execSQL(" create table "
					+ TBL_MESSAGE_INFO
					+ "(_id integer primary key autoincrement,personal bit,time text"
					+ ",title text,body text,resname text,isjpg bit,read bit)");
			MyLog.print("create table " + TBL_MESSAGE_INFO + " success");
		}
		
		// 室内机SIP信息
		if (!tabIsExist(TBL_INDOORSIP_INFO)) {
			// 设备编号  小区编号  区域  楼栋  单元  房号  MAC IP 设备名称  设备类型  设备状态
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

	// 数据库表是否存在
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

	// ------对键值对的操作---------------------------------------------------------------------------------------

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

	/** 获取同步管理中心最新信息的时间 */
	public static String getUpdateMsgTime() {
		return getStringStatus("update_msg_time");
	}

	/** 保存同步管理中心最新信息的时间 */
	public static boolean setUpdateMsgTime(String time) {
		setStatusValue("update_msg_time", time);
		return true;
	}

	/** 获取同步管理中心最新天气的时间 */
	public static String getUpdateWeatherTime() {
		return getStringStatus("update_weather_time");
	}

	/** 保存同步管理中心最新天气的时间 */
	public static boolean setUpdateWeatherTime(String time) {
		setStatusValue("update_weather_time", time);
		return true;
	}

	/** 获取亮度 */
	public static int getBrightness() {
		return getIntStatus("brightness");
	}
	
	/** 保存亮度 */
	public static boolean setBrightness(int value) {
		setStatusValue("brightness", value);
		return true;
	}

	/** 获取对比度 */
	public static int getContrast() {
		return getIntStatus("contrast");
	}
	
	/** 保存对比度 */
	public static boolean setContrast(int value) {
		setStatusValue("contrast", value);
		return true;
	}

	/** 获取饱和度 */
	public static int getSaturability() {
		return getIntStatus("saturability");
	}
	
	/** 保存饱和度 */
	public static boolean setSaturability(int value) {
		setStatusValue("saturability", value);
		return true;
	}

	/** 获取色相 */
	public static int getHue() {
		return getIntStatus("hue");
	}

	/** 保存色相 */
	public static boolean setHue(int value) {
		setStatusValue("hue", value);
		return true;
	}
	
	/** 获取是否呼叫转移到手机 */
	public static boolean getCallToPhone() {
		if(getIntStatus("istophone") == 1)
			return true;
		else
			return false;
	}
	
	/** 设置是否呼叫转移到手机 */
	public static void setCallToPhone(boolean isToPhone) {
		setStatusValue("istophone", isToPhone ? 1 : 0);
	}
	
	/** 设置智能家居模式  1=在家，2=就寝，3=聚餐，4=影视，5=娱乐，6=全关 */
	public static void setSmartHomeMode(int mode) {
		setStatusValue("smart_home", mode);
	}
	
	/** 获取智能家居模式  1=在家，2=就寝，3=聚餐，4=影视，5=娱乐，6=全关*/
	public static int getSmartHomeMode() {
		return getIntStatus("smart_home");
	}

	/** 获取房号 */
	public static String getRoomCode() {
		return getStringStatus("roomcode");
	}

	/** 保存房号 */
	public static boolean setRoomCode(String roomCode) {
		setStatusValue("roomcode", roomCode);
		return true;
	}

	/**
	 * @功能：设置留言模式
	 * @参数 int mode 0-不留言，1-默认留言，2-业主留言
	 */
	public static boolean setMessageMode(int mode) {
		setStatusValue("message_mode", mode);
		return true;
	}

	/**
	 * @功能：获取留言模式
	 * @参数 int mode 0-不留言，1-默认留言，2-业主留言
	 */
	public static int getMessageMode() {
		return getIntStatus("message_mode");
	}

	/** 设置室内机“工程设置”密码 **/
	public static boolean setPsdProjectSetting(String projectPsd) {
		setStatusValue("projectPassword", projectPsd);
		return true;
	}

	/** 获取室内机“工程设置”密码 */
	public static String getPsdProjectSetting() {
		return getStringStatus("projectPassword");
	}

	/**
	 * @功能：设置布防时间
	 * @参数 int time 0-300秒
	 */
	public static boolean setProtectionDelayTime(int time) {
		setStatusValue("safe_protectiondelaytime", time);
		return true;
	}

	/** @功能：获取布防时间 */
	public static int getProtectionDelayTime() {
		return getIntStatus("safe_protectiondelaytime");
	}

	/** 获取安防密码 
	 * @param holding 是否是挟持密码
	 **/
	public static String getSafePassword(boolean holding) {
		if (holding) {
			return getStringStatus("holding_password");
		}
		return getStringStatus("safe_password");
	}

	/** 保存安防密码 
	 * @param holding 是否是挟持密码
	 * @param pwd 新密码
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
	 * @功能：获取安防模式
	 * @参数：int mode 0-测试，1-撤防，2-夜间，3-在家，4-离家
	 * @备注：默认为1-撤防模式
	 */
	public static int getSafeMode() {
		return getIntStatus("safe_mode");
	}

	/**
	 * @功能：设置安防模式
	 * @参数 int mode 0-测试，1-撤防，2-夜间，3-在家，4-离家
	 */
	public static boolean setSafeMode(int mode) {
		setStatusValue("safe_mode", mode);
		return true;
	}
	
	/**
	 * @功能：设置各个探头防区名称
	 * @param area
	 *            数字对应的名称从配置表中读取
	 * */
	public static boolean setSafeArea(int[] area) {
		JSONArray jsonArray = new JSONArray();
		for (int i = 0; i < area.length; i++) {
			jsonArray.put(area[i]);
		}
		setStatusValue("safe_area", jsonArray.toString());
		return true;
	}

	/** @功能：获取各个探头防区名称 */
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

	/** 设置各个探头类型 */
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

	/** 获取各个探头类型 */
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
	 * @功能：设置安防各个探头接法-高低电平
	 * @参数：long connection
	 * @例如：long connection = 0x1F 即2进制的1111 那么探头1-5常开，闭合时报警，探头6-8常闭，开时报警
	 * @描述：探头接法（高低电平）：0-常闭，1-常开
	 */
	public static boolean setSefeConnection(long connection) {
		setStatusValue("safe_connection", connection);
		return true;
	}

	/**
	 * @功能：获取安防各个探头接法-高低电平
	 * @返回值：long - 安防探头接法
	 * @备注：返回值为 0x1F 即2进制的1111 那么探头1-5常开，闭合时报警，探头6-8常闭，开时报警
	 * @描述：探头接法（高低电平）：0-常闭，1-常开
	 */
	public static long getSefeConnection() {
		return getLongStatus("safe_connection");
	}

	/**
	 * @功能：设置各个安防模式下的探头启用与禁用
	 * @参数1：mode - 安防模式
	 * @参数2：enable - 启用或禁用 2进制位为1启用
	 * @返回值：boolean false-失败，true-成功
	 */
	public static boolean setSafeModeEnable(int mode, long enable) {
		setStatusValue("safe_enable" + mode, enable);
		return true;
	}

	/**
	 * @功能：获取各个安防模式下的探头启用与禁用
	 * @参数：mode - 安防模式
	 * @返回值：long - 启用或禁用 2进制位为1启用
	 */
	public static long getSafeModeEnable(int mode) {
		return getLongStatus("safe_enable" + mode);
	}

	/**
	 * @功能：设置各个安防模式下的每个探头的报警延时时间
	 * @参数1：mode - 安防模式
	 * @参数2：int[] - 每个探头的报警延时时间
	 * @返回值：boolean false-失败，true-成功
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
	 * @功能：获取各个安防模式下的每个探头的报警延时时间
	 * @参数：mode - 安防模式
	 * @返回值：int[] - 每个探头的报警延时时间
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
	 * 获取对应关键字的内容
	 * 
	 * @return 字符串类型
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
	 * 获取对应关键字的内容
	 * 
	 * @return 数值型
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
	 * 获取对应关键字的内容
	 * 
	 * @return 数值型
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
	 * 设置对应关键字的值
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
	 * 设置对应关键字的值
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
	 * 设置对应关键字的值
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

	// ------已绑定的手机列表---------------------------------------------------------------------------------------

	/**
	 *  获取室内机保存的手机帐号总数
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
		MyLog.print("getAccountCount sql:" + sql + "  , 绑定账号总数= " + ret);
		return ret;
	}
	
	/**
	 * 找出不同类型的总数
	 * @param phonetype
	 * @return
	 */
	public static int getTokensCount(String phonetype) {
		int ret = 0;
		DBOpen();
		String sql = "select count(*) from " + TBL_ACCOUNT_LIST + " where substr(phonetype, 1, 1) = " + "'" + phonetype + "'" + " and isonline=1";
		Cursor cursor = mDatabase.rawQuery(sql, null);
		if (cursor.moveToFirst()) {
			ret = cursor.getInt(0);
		}
		cursor.close();
		MyLog.print(sql + "  , ret= " + ret);
		return ret;
	}

	/**
	 * 判断SIP账号是否已经存在
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
	 * 判断Token账号是否已经存在
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
	 * 查询绑定设备信息在室内机数据库是否存在
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
				+ " and substr(phonetype, 1, 1) = " + "'" + phonetype + "'";
		MyLog.print(sql);
		Cursor cursor = mDatabase.rawQuery(sql, null);
		if (cursor.moveToFirst()) {
			ret = true;
		}
		cursor.close();
		return ret;
	}

	/**
	 * 添加帐号
	 * @param account SIP账号
	 * @param token 推送token
	 * @param mobiletype  手机类型  1为Android  2为IOS
	 * @return 0-添加成功, -1 已到帐号的最大数量, -2 帐号已存在
	 */
	public static int addAccount(String account) {
		synchronized (mLock) {
			if (isAccountExist(account))
				return -2;
			if (getAccountCount() >= ACCOUNT_MACNUM)
				return -1;
			DBOpen();
			String sql = "insert into " + TBL_ACCOUNT_LIST + " values(null, '"
					+ account + "', '" + account + "', 0, null, 1)";
			MyLog.print("addAccount sql=" + sql);
			mDatabase.execSQL(sql);
		}
		return 0;
	}

	/**
	 * 删除所有绑定设备
	 */
	public static void clearAccount() {
		synchronized (mLock) {
			DBOpen();
			String sql = "delete from " + TBL_ACCOUNT_LIST;
			MyLog.print("clearAccount:" + sql);
			mDatabase.execSQL(sql);			
		}
	}
	
	/**
	 * 删除某类型的所有设备
	 * @param type
	 */
	public static void clearAccount(String type) {
		synchronized (mLock) {
			String sql = null;
			DBOpen();
			if(type.equals("0")) {
				sql = "delete from " + TBL_ACCOUNT_LIST;
			} else if(type.equals("1")) {
				sql = "delete from " + TBL_ACCOUNT_LIST + " where substr(phonetype, 1, 1) = " + "'" + type + "'";
			} else if(type.equals("2")){
				sql = "delete from " + TBL_ACCOUNT_LIST + " where substr(phonetype, 1, 1) = " + "'" + type + "'";
			}
			MyLog.print("clearAccount by type:" + sql);
			mDatabase.execSQL(sql);
		}
	}

	/**
	 * 删除用户
	 * @param account
	 */
	public static void delAccount(String account) {
		synchronized (mLock) {
			if (!isAccountExist(account))
				return;
			DBOpen();
			String sql = "delete from " + TBL_ACCOUNT_LIST
					+ " where accountname='" + account + "'";
			MyLog.print("delAccount:" + sql);
			mDatabase.execSQL(sql);
		}
	}
	
	/**
	 * 删除账号
	 * @param account SIP账号
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
			MyLog.print("delAccountByToken:" + sql);
			mDatabase.execSQL(sql);
		}
	}

	/**
	 * 设置账号在线
	 * @param account
	 * @param online
	 */
	public static void setAccountOnline(String account, boolean online) {
		synchronized (mLock) {
			if (!isAccountExist(account))
				return;
			DBOpen();
			int isonline = online ? 0 : 2;
			String sql = "update " + TBL_ACCOUNT_LIST + " set isonline="
					+ isonline + " where accountname='" + account + "'";
			MyLog.print("setAccountOnline sql:" + sql);
			mDatabase.execSQL(sql);
		}
	}

	/**
	 * 获取Account列表
	 * @return
	 */
	public static List<String> getAccountList() {
		List<String> accounts = new ArrayList<String>();
		DBOpen();
		String sql = "select * from " + TBL_ACCOUNT_LIST + " where isonline=0";
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
	 * 按手机类型查找用户
	 * @param type
	 * @return
	 */
	public static List<String> getAccountByPhoneType(String type){
		List<String> accounts = new ArrayList<String>();
		DBOpen();
		String sql = null;
		if (type.equals("1")) {
			sql = "select * from " + TBL_ACCOUNT_LIST + " where phonetype=1";
		} else if (type.equals("2")) {
			sql = "select * from " + TBL_ACCOUNT_LIST + " where substr(phonetype, 1, 1) ='2'";
		} else if (type.equals("0")) {
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
		MyLog.print("getAccountByPhoneType sql:" + sql);
		MyLog.print("accounts size= " + accounts.size());
		return accounts;
	}
	
	/**
	 * 获取Token列表
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
	 * 根据phoneType获取Token列表
	 * @return
	 */
	public static List<String> getTokenByTypeList(String type) {
		List<String> tokens = new ArrayList<String>();
		DBOpen();
		String sql = "select * from " + TBL_ACCOUNT_LIST + " where substr(phonetype, 1, 1) = " + "'" + type + "'" + " and isonline=1";
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
		MyLog.print("sql:" + sql +" tokens size= " + tokens.size());
		return tokens;
	}
	
	/**
	 * 根据手机类型查找绑定用户
	 * @param type
	 * @return
	 */
	public static ArrayList<BindAccountInfo> getAccountByPhonetpye(String type) {
		ArrayList<BindAccountInfo> list = new ArrayList<BindAccountInfo>();
		DBOpen();
		//表字段PhoneType中第一个字符的查询
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
	 * 获取账号信息记录
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
	 * 删除一个绑定设备
	 * @param db_id
	 */
	public static void deleteAccount(int db_id) {
		synchronized (mLock) {
			DBOpen();
			String args[] = { String.valueOf(db_id) };
			mDatabase.delete(TBL_ACCOUNT_LIST, "_id=?", args);
		}
	}

	// ------通话记录---------------------------------------------------------------------------------------

	/**
	 * @功能：增加呼叫记录到数据库中 呼叫开始到结束时间小于1.5s时不保存记录
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
				MyLog.print("addCallLog，时间差大于1.5秒，插入记录 " + values.toString());
				mDatabase.insert(TBL_CALL_INFO, null, values);
			}
		}
	}

	/**
	 * @功能：修改数据库中的呼叫记录
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
	 * @功能：删除数据库中指定ID的呼叫记录
	 * @参数：int db_id - 数据库中自动生成的_id
	 */
	public static void deleteCallLog(int db_id) {
		synchronized (mLock) {
			DBOpen();
			String args[] = { String.valueOf(db_id) };
			mDatabase.delete(TBL_CALL_INFO, "_id=?", args);
		}
	}

	/**
	 * @功能：删除对应类型的所有呼叫记录
	 * @参数：int type 见CallInfo类中的CALLOUT_ACCEPT等值
	 * @备注：参数可以组合使用，如CALLOUT_ACCEPT | CALLOUT_UNACCEPT
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
	 * @功能：从数据库中获取呼叫记录
	 * @参数：int type 见CallInfo类中的CALLOUT_ACCEPT等值
	 * @备注：参数可以组合使用，如CALLOUT_ACCEPT | CALLOUT_UNACCEPT
	 */
	public static ArrayList<CallInfomation> getCallLogList(int type) {
		ArrayList<CallInfomation> callLogList = new ArrayList<CallInfomation>();
		DBOpen();
		if (mDatabase != null) {
			List<String> list = new ArrayList<String>();
			// 有6个类型即二进制111111，十六进制0x3F
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
	 * 获取通话记录总条数
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
	 * 查询通话记录最后一条数据
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

	// ------布防记录---------------------------------------------------------------------------------------

	/**
	 * @功能：增加安防记录到数据库中
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
	 * @功能：修改数据库中的安防记录
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
	 * @功能：删除数据库中指定ID的安防记录
	 * @参数：int db_id - 数据库中自动生成的_id
	 */
	public static void deleteSafeModeLog(int db_id) {
		synchronized (mLock) {
			DBOpen();
			String args[] = { String.valueOf(db_id) };
			mDatabase.delete(TBL_SAFE_MODE_INFO, "_id=?", args);
		}
	}

	/**
	 * @功能：删除所有布防记录
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
	 * 获取布防记录
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

	// ------报警记录---------------------------------------------------------------------------------------

	/**
	 * @功能：增加报警记录到数据库中
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
	 * @功能：修改数据库中的报警记录
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
	 * @功能：删除数据库中指定ID的报警记录
	 * @参数：int db_id - 数据库中自动生成的_id
	 */
	public static void deleteAlarmLog(int db_id) {
		synchronized (mLock) {
			DBOpen();
			String args[] = { String.valueOf(db_id) };
			mDatabase.delete(TBL_AlARM_INFO, "_id=?", args);
		}
	}

	/**
	 * @功能：删除所有报警记录
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
	 * 获取报警记录
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
	
	// ------报警录像---------------------------------------------------------------------------------------

	/**
	 * @功能：增加报警录像到数据库中
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
	 * @功能：删除数据库中指定ID的报警录像
	 * @参数：int db_id - 数据库中自动生成的_id
	 */
	public static void deleteAlarmVideo(int db_id) {
		synchronized (mLock) {
			DBOpen();
			String args[] = { String.valueOf(db_id) };
			mDatabase.delete(TBL_ALARM_VIDEO, "_id=?", args);
		}
	}

	/**
	 * @功能：删除所有报警录像
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
	 * 获取报警录像
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
	
	// ------小区信息---------------------------------------------------------------------------------------

	/**
	 * @功能：增加信息记录到数据库中
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
	 * @功能：修改数据库中的信息记录
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
	 * @功能：删除数据库中指定ID的消息记录
	 * @参数：int db_id - 数据库中自动生成的_id
	 */
	public static void deleteMessageLog(int db_id) {
		synchronized (mLock) {
			DBOpen();
			String args[] = { String.valueOf(db_id) };
			mDatabase.delete(TBL_MESSAGE_INFO, "_id=?", args);
		}
	}

	/**
	 * @功能：删除对应类型的所有消息记录
	 * @参数：int type - 1-个人信息，2-公共信息，3-所有信息
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
	 * @功能：从数据库中获取指定类型的的未读消息个数
	 * @参数：int type PERSONAL、PUBLIC、PERSONAL|PUBLIC
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
	 * @功能：从数据库中获取指定类型的消息记录
	 * @参数：int type PERSONAL、PUBLIC、PERSONAL|PUBLIC
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
	 * 查询小区消息的总数
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
		MyLog.print(sql + "  , 小区消息count= " + count);
		return count;
	}
	/**
	 * 获取后一条消息
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
	 * 增加室内机SIP信息到数据库
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
		String DeviceName = info.getDeviceName();
		String regEx="[^0-9]";
		Pattern pattern = Pattern.compile(regEx);
		Matcher matcher = pattern.matcher(DeviceName);
		Log.e("lichao", matcher.replaceAll("").trim());
		values.put("devicename", matcher.replaceAll("").trim());
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
	 * 获取室内机SIP信息列表
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
	 * 判断室内机SIP账号是否已经存在
	 * @param sipid
	 * @return
	 */
	public static boolean isIndoorSipExist(String sipid) {
		boolean ret = false;
		DBOpen();
		String sql = "select sipid from " + TBL_INDOORSIP_INFO
				+ " where sipid='" + sipid + "'";
		MyLog.print("isIndoorSipExist sql:" + sql);
		Cursor cursor = mDatabase.rawQuery(sql, null);
		if (cursor.moveToFirst()) {
			ret = true;
		}
		cursor.close();
		return ret;
	}
	
	/**
	 * 更新室内机SIP信息
	 * @param info
	 */
	public static void modifyIndoorSip(IndoorSipInfo info) {
		Log.i("lichao", "modifyIndoorSip info:" + info.toString());
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
	 * 查询室内机SIP账号的条数
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
	 * 查询室内机SIP账号的第一条
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

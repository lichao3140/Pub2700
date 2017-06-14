package com.dpower.util;

import java.io.File;

import android.os.Environment;

/**
 * ��Ҫ���һЩ����
 * @author Arlene
 */
public class ConstConf {
	
	/** ϵͳ�����ļ�·�� system/etc/ */
	public static final String SYSTEM_CONF = "system" + File.separator + "etc" + File.separator;
	
	/** �����ļ����Ŀ¼ system/media/backup/ */
	public static final String ROM_DIR = "system/media/backup" + File.separator;
	
	/** /mnt/sdcard/ */
	public static final String SD_DIR = Environment.getExternalStorageDirectory().getPath() 
			+ File.separator;
	
	/** ��ʱ�ļ���·�� /mnt/sdcard/temp */
	public static final String TEMP_PATH = SD_DIR + "temp";
	
	/** ��������·�� system/media/backup/Music/ringin.mp3 */
	public static final String DOOR_RING_PATH = ROM_DIR + "Music/ringin.mp3";
	
	/** �绰����·�� /mnt/sdcard/Ringtones/ */
	public static final String RING_PATH = SD_DIR + "Ringtones" + File.separator;
	
	/** �绰�����ļ� ring_you.mp3 */
	public static final String RING_MP3 = "ring_you.mp3";
	
	/** �������ñ�·�� /mnt/sdcard/netcfg.dat */
	public static final String NET_CFG_PATH = SD_DIR + "netcfg.dat";
	
	/** �����������ñ�·�� /mnt/sdcard/temp/netcfg.dat */
	public static final String NEW_NET_CFG_PATH = SD_DIR + "temp/netcfg.dat";
	
	/** �������ñ� netcfg.dat */
	public static final String NET_CFG_DAT = "netcfg.dat";
	
	/** �ÿ����·�� /mnt/sdcard/visit */
	public static final String VISIT_PATH = SD_DIR + "visit";
	
	/** С����Ϣ·�� /mnt/sdcard/message */
	public static final String MESSAGE_PATH = SD_DIR + "message";
	
	/** Ĭ������·�� system/media/backup/defaultleave.wav */
	public static final String DEFAULT_LEAVE_PATH = ROM_DIR + "defaultleave.wav";
	
	/** ҵ������·��(�û��Լ�¼��������) system/media/backup/userleave.wav */
	public static final String USER_LEAVE_PATH = ROM_DIR + "userleave.wav";
	
	/** ������������apk·�� /mnt/sdcard/update.rar */
	public static final String UPDATE_PATH = SD_DIR + "update.rar";
	
	/** ��Ϣ����·�� system/media/backup/Music/MSGRING.WAV */
	public static final String MSG_RING_PATH = ROM_DIR + "Music/MSGRING.WAV";
	
	/** �����ӳ�����·�� system/media/backup/Music/DelayAlarm.wav */
	public static final String DELAYALARM_RING_PATH = ROM_DIR + "Music/DelayAlarm.wav";
	
	/** ��������·�� system/media/backup/Music/Alarm.wav */
	public static final String ALARM_RING_PATH = ROM_DIR + "Music/Alarm.wav";
	
	/** ����¼��·��  /mnt/sdcard/AlarmVideo/ */
	public static final String ALARM_VIDEO_PATH = SD_DIR + "AlarmVideo" + File.separator;
	
	/** ���������ļ� AECpara.inf */
	public static final String VOLUME_INF = "AECpara.inf";
	
	/** ���������ļ�·�� /mnt/sdcard/Alarms/ */
	public static final String SAFE_ALARM_PATH = SD_DIR + "Alarms" + File.separator;
	
	/** ���������ļ� alarm.txt */
    public static final String SAFE_ALARM_TXT = "alarm.txt";
    
    /** ����ģʽ */
	public final static int TEST_MODE = 0;
	/** ����ģʽ */
	public final static int UNSAFE_MODE = 1;
	/** ҹ��ģʽ */
	public final static int NIGHT_MODE = 2;
	/** �ڼ�ģʽ */
	public final static int HOME_MODE = 3;
	/** ���ģʽ */
	public final static int LEAVE_HOME_MODE = 4;
	/** ����ģʽ���� */
	public final static int MODE_NUM = 5;
	/** Ĭ��8��̽ͷ */
	public static final int DEFAULT_SAFE_TANTOU = 8;
	
	/** ���峬ʱ */
	public static final int RING_TIMEOUT = 50 * 1000;
	
	/** ͨ����ʱ */
	public static final int TALK_TIMEOUT = 300 * 1000;
	
	/** ���ӳ�ʱ */
	public static final int MONITOR_TIMEOUT = 120 * 1000;
	
	public static final String LAN_NETWORK_CARD = "eth0";
	public static final String WAN_NETWORK_CARD = "wlan0";
	public static final String VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";
}

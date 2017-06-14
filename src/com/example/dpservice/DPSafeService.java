package com.example.dpservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.android.talkserversdk.TalkManager;
import org.dpower.GenernalGpioSet.JniClassGpioSet;
import org.dpower.GenernalGpioSet.UtilConst;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.dpower.domain.AlarmInfo;
import com.dpower.domain.AlarmLog;
import com.dpower.domain.AlarmNameInfo;
import com.dpower.domain.AlarmTypeInfo;
import com.dpower.domain.SafeModeInfo;
import com.dpower.function.DPFunction;
import com.dpower.util.CommonUT;
import com.dpower.util.ConstConf;
import com.dpower.util.DPDBHelper;
import com.dpower.util.MyLog;
import com.dpower.util.ProjectConfigure;
import com.example.dpsafe.DPSafe;

/**
 * ���ذ������� Ĭ������̽ͷ�����ͽ��� 0-���ã�1-���� ̽ͷ�ӷ����ߵ͵�ƽ����0-���գ�1-����
 * */
public class DPSafeService extends Service {
	private static final String TAG = "DPSafeService";
    
    /** ����̽ͷ����,�������ļ��л�ȡ */
	public static int tanTouNum = ConstConf.DEFAULT_SAFE_TANTOU;

	/** ���������б��������ļ��л�ȡ */
	public List<AlarmNameInfo> alarmAreaNameList;

	/** �������������б��������ļ��л�ȡ */
	public List<AlarmTypeInfo> alarmTypeNameList;

	/** ���а���̽ͷ�������ڷ��������б������ */
	public int[] alarmArea = null;

	/** ���а���̽ͷ�ı������������ڱ������������б������ */
	public int[] alarmType = null;
	
	/** ��¼��һ�μ�⵽����̽ͷ��״̬, ��Ӧstate */
	public long oldState = 0;
	
	private static DPSafeService mInstance = null;
	private static int[] mDefaultAlarmArea = new int[ConstConf.DEFAULT_SAFE_TANTOU];
	private static int[] mDefaultAlarmType = new int[ConstConf.DEFAULT_SAFE_TANTOU];

	/** ��ǰ����̽ͷ��״̬0-255 */
	private int mState;

	/** ��ǰ����ģʽ mode 0-���ԣ�1-������2-ҹ�䣬3-�ڼң�4-��� */
	private int mCurrentMode = 0;

	/** ��ǰ����̽ͷ�Ľӷ�(��������,�û����õĽӷ�) */
	private long mConnection = 0;

	/** ��ǰ����̽ͷ��ʹ��״̬�����û����� */
	private long mEnable = 0;
	
	/** ��¼�����ı�İ���״̬ */
	private long mChange = 0;
	
	/** ��¼���ڱ����ķ���,��԰�������HOME�����˳������������� */
	private int[] mAlarmingArea; 
	
	private ScheduledThreadPoolExecutor mSafeScheduled;
	private MyHandler mHandler = null;
	private HandlerThread mHandlerThread;
	private JniClassGpioSet mGpioSet;
	private int mDoorbell_H17;
	private int mH17_oldState = 0;
	private SoundPool mSoundPool = null;
	private int mStreamID = 0;
	private boolean mDelaying = false;// ��������������ture-Ԥ��,false-����,����������ʼ�����ٴθı�ΪԤ������
	private int mCurrentDelayTime = 0; // ����ʱ��������Ԥ������ת��������
	private Timer mTimer;
	private static DelayTimerTask mDelayTimerTask;
	
	public static DPSafeService getInstance() {
		return mInstance;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		MyLog.print(TAG, "onCreate��ʼ");
		mInstance = this;
		loadAlarmFile();
		mAlarmingArea = new int[tanTouNum];
		Arrays.fill(mAlarmingArea, 1);
		alarmArea = DPDBHelper.getSafeArea();
		if (alarmArea == null) {
			MyLog.print("alarmArea = null");
			alarmArea = new int[tanTouNum];
			setAlarmArea(mDefaultAlarmArea);
		}
		alarmType = DPDBHelper.getSafeType();
		if (alarmType == null) {
			MyLog.print("alarmType = null");
			alarmType = new int[tanTouNum];
			setAlarmType(mDefaultAlarmType);
			// ��ʼ���������غͱ����ӳ�
			if (ProjectConfigure.project == 2) {
				changeSafeEnable(ConstConf.HOME_MODE, 128);
				changeSafeEnable(ConstConf.NIGHT_MODE, 255);
				changeSafeEnable(ConstConf.LEAVE_HOME_MODE, 127);
				int[] time = { 30, 0, 0, 0, 0, 0, 0, 0 };
				for (int i = 0; i < ConstConf.MODE_NUM; i++) {
					changeAlarmDelayTime(i, time);
				}
			}
		}
		DPSafe.initDPSafe();
		mCurrentMode = DPDBHelper.getSafeMode();
		if (mCurrentMode == ConstConf.TEST_MODE) {
			mCurrentMode = ConstConf.UNSAFE_MODE;
			DPDBHelper.setSafeMode(mCurrentMode);
		}
		mConnection = DPDBHelper.getSefeConnection();
		mEnable = DPDBHelper.getSafeModeEnable(mCurrentMode);
		oldState = mConnection; //��ʼĬ����һ�νӷ�������״̬�����ᱨ��
		/**
		 * ������ʱ����ÿ200ms��ⰲ��̽ͷһ��
		 */
		MyLog.print("onCreate oldState - >" + oldState);
		mSafeScheduled = new ScheduledThreadPoolExecutor(5);
		mSafeScheduled.scheduleWithFixedDelay(mSafeRunnable, 200, 200,
				TimeUnit.MILLISECONDS);

		mHandlerThread = new HandlerThread("safeThread");
		mHandlerThread.start();
		mHandler = new MyHandler(mHandlerThread.getLooper());
		if (ProjectConfigure.project == 2) {
			mGpioSet = new JniClassGpioSet();
			mDoorbell_H17 = mGpioSet.InitGpio(UtilConst.GPIO_H17, 0);
		}
		MyLog.print(TAG, "onCreate���");
	}
	
	/**
	 * ���밲�������ļ�(�ı�����ʱ��Ҫ���µ���)
	 */
	public void loadAlarmFile() {
		alarmAreaNameList = new ArrayList<AlarmNameInfo>();
		alarmTypeNameList = new ArrayList<AlarmTypeInfo>();
		String fileInfo = readTextFile(ConstConf.SAFE_ALARM_PATH + ConstConf.SAFE_ALARM_TXT);
		if (fileInfo.length() > 0) {
			printfStringtoJson(fileInfo);
		} else {
			MyLog.print(MyLog.ERROR, "��ȡ���������ļ�ʧ��");
		}
		MyLog.print(alarmAreaNameList.toString());
		MyLog.print(alarmTypeNameList.toString());
	}
	
	/** ��ȡJSON�ı��ļ� */
	private String readTextFile(String filePath) {
		String str = "";
		// ���ļ�����ANSI
		File file = new File(filePath);
		if (file.exists()) {
			InputStream in = null;
			try {
				in = new FileInputStream(file);
				int size = in.available();
				byte[] buffer = new byte[size];
				in.read(buffer);
				str = new String(buffer, "GBK");// ���������������
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			MyLog.print("Safe file is not find.");
		}
		return str;
	}

	/** ����JSON�ı��ļ� */
	private void printfStringtoJson(String info) {
		MyLog.print("��ȡ�������ñ�" + info);
		JSONTokener jsonParser = new JSONTokener(info);

		try {
			JSONObject person = (JSONObject) jsonParser.nextValue();
			tanTouNum = person.getInt("probenum");
			MyLog.print("tanTouNum:" + tanTouNum);
			JSONObject zh_en;
			if(Locale.getDefault().getLanguage().equals("en")) {
				zh_en = person.getJSONObject("english");
			} else {
				zh_en = person.getJSONObject("chinese");
			}
			JSONArray area = zh_en.getJSONArray("area");
			for (int i = 0; i < area.length(); i++) {
				JSONObject obj = area.getJSONObject(i);
				String name = (String) obj.get("name");
				int value = Integer.valueOf((String) obj.get("value"));
				alarmAreaNameList.add(new AlarmNameInfo(name, value));
			}
			JSONArray type = zh_en.getJSONArray("type");
			for (int i = 0; i < type.length(); i++) {
				JSONObject obj = type.getJSONObject(i);
				String name = (String) obj.get("name");
				int priority = Integer.valueOf((String) obj.get("priority"));
				int value = Integer.valueOf((String) obj.get("value"));
				alarmTypeNameList.add(new AlarmTypeInfo(name, priority, value));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ���ð�������
	 * @param alarmArea ���������б�
	 */
	public boolean setAlarmArea(int[] alarmArea) {
		if (this.alarmArea.length == alarmArea.length) {
			this.alarmArea = alarmArea;
			return DPDBHelper.setSafeArea(this.alarmArea);
		}
		return false;
	}
	
	/**
	 * @���ܣ��޸����еı�������
	 * @��ע�����ж������Ƿ�������޸���ʱʱ������ý���
	 * @�������������̸С�ú�������ͱ�������������ʱ����ʱ��
	 */
	public boolean setAlarmType(int[] alarmType) {
		if (this.alarmType.length == alarmType.length) {
			for (int i = 0; i < alarmType.length; i++) {
				setAlarmType(i, alarmType[i]);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * @���ܣ��޸ĵ�����������
	 * @����1��int i -�ڼ���̽ͷ ��0��ʼ
	 * @����2��int alarmType ̽ͷ����
	 * @��ע�����ж������Ƿ�������޸���ʱʱ������ý���
	 * @�������������̸С�ú�������ͱ�������������ʱ����ʱ��
	 */
	public boolean setAlarmType(int i, int alarmType) {
		if (i >= 0 && i < tanTouNum) {
			this.alarmType[i] = alarmType;
			long modeEnable, temp;
			int[] time;
			if (this.alarmType[i] >= alarmTypeNameList.size())
				return false;
			if (isEmergency(i)) {
				/** ������ģʽ�µı�����ʱ����Ϊ0������������ģʽ�µĸ�̽ͷ */
				for (int j = 0; j < ConstConf.MODE_NUM; j++) {
					modeEnable = DPDBHelper.getSafeModeEnable(j);
					temp = modeEnable | (1 << i);
					if (temp != modeEnable) {
						changeSafeEnable(j, temp);
					}
					time = DPDBHelper.getAlarmDelayTime(j);
					if (time[i] != 0) {
						time[i] = 0;
						changeAlarmDelayTime(j, time);
					}
				}
			} else if (alarmTypeNameList.get(this.alarmType[i]).priority == 3) {
				// ������ʱ������������ģʽ�����н���
				modeEnable = DPDBHelper.getSafeModeEnable(ConstConf.UNSAFE_MODE);
				temp = modeEnable & (~(1 << i));
				if (temp != modeEnable) {
					changeSafeEnable(ConstConf.UNSAFE_MODE, temp);
				}
//				for (int j = 1; j < ConstConf.MODE_NUM; j++) {
//					modeenable = DPDBHelper.getSafeModeEnable(j);
//					temp = modeenable & (~(1 << i));
//					if (temp != modeenable) {
//						ChangeSafeEnable(j, temp);
//					}
//					int[] modetime = DPDBHelper.getAlarmDelayTime(j);
//					modetime[i] = 0;
//					DPDBHelper.setAlarmDelayTime(j, modetime);
//				}
			}
			return DPDBHelper.setSafeType(this.alarmType);
		}
		return false;
	}
	
	/**
	 * @���ܣ��ж��Ƿ����������ʱ������
	 * @��ע��priority 0-���塢1-������2-�̸�ú����3-�Ŵź���
	 */
	public boolean isEmergency(int i) {
		if (alarmType[i] < alarmTypeNameList.size()) {
			return alarmTypeNameList.get(alarmType[i]).priority < 3;
		}
		return false;
	}
	
	/**
	 * �޸��û�ģʽ�µ� ����ģʽ��Ӧ�Ŀ���״̬
	 * @param model ģʽ ���� �ڼ� ���ģʽ
	 * @param enable 8�����صĿ���״̬
	 */
	public void changeSafeEnable(int model, long enable) {
		if (model >= ConstConf.MODE_NUM || model < 0) {
			MyLog.print("This mode crossing the line.mode = " + mCurrentMode);
			return;
		}
		DPDBHelper.setSafeModeEnable(model, enable);
		if (model == mCurrentMode) {
			mEnable = enable;
		}
	}
	
	/**
	 * �޸İ���ģʽ��Ӧ�ı����ӳ�ʱ��
	 * @param model ģʽ ���� �ڼ� ���ģʽ
	 * @param time �����ӳ�ʱ��
	 */
	public void changeAlarmDelayTime(int model, int[] time) {
		DPDBHelper.setAlarmDelayTime(model, time);
	}
	
	private Runnable mSafeRunnable = new Runnable() {

		@Override
		public void run() {
			// ��������IO
			if (ProjectConfigure.project == 2) {
				int h17_state = mGpioSet.GetGpioValue(mDoorbell_H17);
				if (h17_state != mH17_oldState) {
					mH17_oldState = h17_state;
					MyLog.print("mDoorbell_H17 = " + h17_state);
					if (h17_state == 0) {
						playDoorbell();
					}
				}
			}
			
			mState = DPSafe.getSafeVal();
			if (mState > 256 || mState < 0) {
				MyLog.print("get safe val error+" + mState);
				return;
			}
			if (mState == oldState) {
				if (mState != 255) {
					MyLog.print("same state! mState = " + mState);
				}
				return;
			}
			
			// ���ķ���˳��
			if (ProjectConfigure.project == 1 && ProjectConfigure.size == 10) {
				String binaryState = Integer.toBinaryString(mState);
				int length = binaryState.length();
				for(; length < 8; length++) {
					binaryState = "0" + binaryState;
				}
				MyLog.print(mState + " " + binaryState);
				
				binaryState = new StringBuffer(binaryState).reverse().toString();
				mState = Integer.parseInt(binaryState, 2);
			} else if (ProjectConfigure.project == 2) {
				String binaryState = Integer.toBinaryString(mState);
				int length = binaryState.length();
				for(; length < 8; length++) {
					binaryState = "0" + binaryState;
				}
				MyLog.print(mState + " " + binaryState);
				
				String left = binaryState.substring(0, length / 2);
				String right = binaryState.substring(length / 2, length);
				binaryState = new StringBuffer(left).reverse().toString() 
						+ new StringBuffer(right).reverse().toString();
				mState = Integer.parseInt(binaryState, 2);
			}
			
			MyLog.print("tantou changed state = " + mState + " =="
					+ Integer.toBinaryString(mState) + ",oldstate = " + oldState
					+ "= " + Long.toBinaryString(oldState));
			/** ��ȡ�����ı��̽ͷ */
			mChange = oldState ^ mState;
			oldState = mState;
			mHandler.obtainMessage().sendToTarget();
		}
	};
	
	/**
	 * ��������
	 */
	private void playDoorbell() {
		if (DPFunction.getAlarming()) return;
		// ����������3��
		if (mSoundPool != null) {
			if (mStreamID != 0) {
				mSoundPool.stop(mStreamID);
				mStreamID = 0;
			}
			mSoundPool.release();
			mSoundPool = null;
		}
		mSoundPool = new SoundPool(1, AudioManager.STREAM_RING, 5);
		mSoundPool.load(ConstConf.DOOR_RING_PATH, 1);
		mSoundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
				mStreamID = soundPool.play(1, 1, 1, 0, 2, 1);
				if (mStreamID == 0) {
					MyLog.print(MyLog.ERROR, "paly ring error");
				}
			}
		});
	}
	
	/***
	 * �������ı�İ���״̬,����ʱ���㲥
	 */
	private class MyHandler extends Handler {
		
		public MyHandler(Looper looper) {
			super(looper);
		}
		
		@Override
		public void handleMessage(Message msg) {
			// ����ģʽ
			if (mCurrentMode == ConstConf.TEST_MODE) {
				MyLog.print("�������� Test mode");
				return;
			}
			if (mCurrentMode >= ConstConf.MODE_NUM || mCurrentMode < 0) {
				MyLog.print("This mode crossing the line.mode = "
						+ mCurrentMode);
				return;
			}
			MyLog.print("mChange = " + mChange + ";mEnable = " + mEnable);
			long enableChange = mChange & mEnable;
			long abnormal = mState ^ mConnection;
			long alarm = enableChange & abnormal;
			if (alarm == 0) {
				return;
			}

			for (int i = 0; i < tanTouNum; i++) {
				if ((alarm & (1 << i)) > 0) {
					MyLog.print("alarm i = " + i + ",  " + mState + " "
							+ oldState);
					if (alarmTypeNameList.get(alarmType[i]).priority == 0) { // �ж�������
						playDoorbell();
					} else {
						if (ProjectConfigure.project != 2) {
							if (alarmTypeNameList.get(alarmType[i]).priority == 1) {
								// ֪ͨ�ֻ�����
								DPFunction.toPhoneAlarm(
										alarmAreaNameList.get(alarmArea[i]).value,
										alarmTypeNameList.get(alarmType[i]).value,
										CommonUT.formatTime(System.currentTimeMillis()));
								// �����������������棬ֻ֪ͨ�������ı���
								toManageAndLog(i);
								return;
							}
						}
						toManageAndLog(i);

						// ����֮ǰ�Ҷ�����ͨ��
						DPFunction.callHangUp();
						mAlarmingArea[i] = 0;
						
						if (!DPFunction.getAlamActivityState()) {
							if (DPFunction.alarmActivity != null) {
								Intent intent = new Intent();
								intent.setClass(DPSafeService.this, DPFunction.alarmActivity);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
										| Intent.FLAG_ACTIVITY_SINGLE_TOP);
								intent.putExtra("alarm", i);
								startActivity(intent);
								MyLog.print("alarm activity start");
							}
							
							// ��԰�������HOME�����˳�������������������������������ʱҪ�ָ�֮ǰû�г����ķ�������
							if (DPFunction.getAlarming()) {
								final int k = i;
								new Handler().postDelayed(new Runnable() {
									
									@Override
									public void run() {
										for(int j = 0; j < mAlarmingArea.length; j++) {
											if (j == k) continue;
											if (mAlarmingArea[j] == 0) {
												MyLog.print("alarm add j = " + j);
												Intent intent = new Intent();
												intent.setAction(DPFunction.ACTION_ALARMING);
												intent.putExtra("alarm", j);
												sendBroadcast(intent);
												MyLog.print("alarm add broadcast send");
											}
										}
									}
								}, 500);
							}
						}
						
						int delayTime[] = DPDBHelper.getAlarmDelayTime(mCurrentMode);
						if (!DPFunction.getAlarming()) {
							DPFunction.setAlarming();
							if (delayTime[i] > 0) {
								mCurrentDelayTime = delayTime[i];
								mDelaying = true;
								/** ������ʱ����1��1�μ�ⱨ����ʱ������ʱ����������������� */
								mTimer = new Timer();
								mDelayTimerTask = new DelayTimerTask();
								mTimer.schedule(mDelayTimerTask, 1000, 1000);
							} else {
								mDelaying = false;
							}
							playAlarmRing();
						} else {
							if (mDelaying) {
								int delay = delayTime[i];
								if (delay == 0) {
									mDelaying = false;
									playAlarmRing();
								} else {
									if (mTimer != null) {
										if (mCurrentDelayTime > 0 && delay > 0) {
											mCurrentDelayTime = Math.min(delay, mCurrentDelayTime);
										}
									}
								}
							}
						}
						
						Intent intent = new Intent();
						intent.putExtra("alarm", i);
						intent.setAction(DPFunction.ACTION_ALARMING);
						sendBroadcast(intent);
						MyLog.print("alarm broadcast sent " + i + "  area "
								+ alarmArea[i] + ", type " + alarmType[i]);
						DPFunction.toPhoneAlarm(
								alarmAreaNameList.get(alarmArea[i]).value,
								alarmTypeNameList.get(alarmType[i]).value,
								CommonUT.formatTime(System.currentTimeMillis()));
					}
				}
			}
		}
	}
	
	/**
	 * �ϱ��������ĺ���ӱ�����¼
	 * @param i ��������
	 */
	private void toManageAndLog(int i) {
		long time = System.currentTimeMillis();
		String alarmRoom; //���������źͷ���������
		int areaNum = i + 1;
		if (alarmAreaNameList.get(alarmArea[i]).value < 10) {
			alarmRoom = Integer.toString(areaNum) + "0" 
					+ alarmAreaNameList.get(alarmArea[i]).value;
		} else {
			alarmRoom = Integer.toString(areaNum) 
					+ alarmAreaNameList.get(alarmArea[i]).value;
		}
		int delayTime[] = DPDBHelper.getAlarmDelayTime(mCurrentMode);
		int result = TalkManager.toManageAlarm(CommonUT.formatTime(time), 
				Integer.parseInt(alarmRoom),
				alarmTypeNameList.get(alarmType[i]).value,
				delayTime[i]);
		AlarmLog alarmLog = new AlarmLog(mCurrentMode, areaNum, alarmArea[i],
				alarmType[i], time);
		if (result == 0) {
			alarmLog.setIsSuccess(true);
		}
		DPDBHelper.addAlarmLog(alarmLog);
	}
	
	private class DelayTimerTask extends TimerTask {
		
		@Override
		public void run() {
			if (mDelaying) {
				if (--mCurrentDelayTime <= 0) {
					mDelaying = false;
					playAlarmRing();
				} else {
					MyLog.print("currentDelayTime:" + mCurrentDelayTime);
				}
			}
		}
	};
	
	private void playAlarmRing() {
		if (mSoundPool != null) {
			if (mStreamID != 0) {
				mSoundPool.stop(mStreamID);
				mStreamID = 0;
			}
			mSoundPool.release();
			mSoundPool = null;
		}
		if (!mDelaying) {
			if (mTimer != null) {
				mDelayTimerTask.cancel();
				mTimer.cancel();
				mDelayTimerTask = null;
				mTimer = null;
			}
		}
		mSoundPool = new SoundPool(1, AudioManager.STREAM_RING, 5);
		mSoundPool.load(mDelaying ? ConstConf.DELAYALARM_RING_PATH
				: ConstConf.ALARM_RING_PATH, 1);
		mSoundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId,
					int status) {
				mStreamID = soundPool.play(1, 1, 1, 0, -1, 1);
				if (mStreamID == 0) {
					MyLog.print(MyLog.ERROR, "paly alarmRing error");
				}
			}
		});
	}
	
	public void releaseAlarmRing() {
		if (mSoundPool != null) {
			if (mStreamID != 0) {
				mSoundPool.stop(mStreamID);
				mStreamID = 0;
			}
			mSoundPool.release();
			mSoundPool = null;
		}
		if (mTimer != null) {
			mDelayTimerTask.cancel();
			mTimer.cancel();
			mDelayTimerTask = null;
			mTimer = null;
		}
		Arrays.fill(mAlarmingArea, 1);
	}
	
	public static void setDefaultAlarmArea(int[] area) {
		if (area.length == mDefaultAlarmArea.length) {
			System.arraycopy(area, 0, mDefaultAlarmArea, 0,
					mDefaultAlarmArea.length);
		} else {
			Arrays.fill(mDefaultAlarmArea, 0);
		}
	}

	public static void setDefaultAlarmType(int[] type) {
		if (type.length == mDefaultAlarmType.length) {
			System.arraycopy(type, 0, mDefaultAlarmType, 0,
					mDefaultAlarmType.length);
		} else {
			Arrays.fill(mDefaultAlarmType, 0);
		}
	}
	
	/** ��ȡָ������ģʽ������״̬ */
	public List<AlarmInfo> getAlarmInfoList(int model) {
		if (alarmAreaNameList == null || alarmTypeNameList == null) {
			return null;
		}
		List<AlarmInfo> list = new ArrayList<AlarmInfo>();
		long enable = DPDBHelper.getSafeModeEnable(model);
		int[] time = DPDBHelper.getAlarmDelayTime(model);
		for (int i = 0; i < tanTouNum; i++) {
			String areaName = alarmAreaNameList.get(alarmArea[i]).name;
			String typeName = alarmTypeNameList.get(alarmType[i]).name;
			boolean bEnable = (enable & (1 << i)) > 0;
			boolean bOpen = ((mConnection & (1 << i)) > 0);
			AlarmInfo info = new AlarmInfo(areaName, typeName, bEnable,
					bOpen, time[i]);
			MyLog.print(info.toString());
			list.add(info);
		}
		return list;
	}

	/** �����ض����� */
	public boolean setAlarmArea(int i, int alarmArea) {
		this.alarmArea[i] = alarmArea;
		return DPDBHelper.setSafeArea(this.alarmArea);
	}

	/**
	 * �޸����ڻ��İ���ģʽ��ͬʱ��д�����ݿ��У�
	 * @param isAuto true-���ڻ��Զ�ͬ������ģʽ, false-�ֶ��޸İ���ģʽ
	 */
	public void changeSafeMode(int model, boolean isAuto) {
		if (mCurrentMode >= ConstConf.MODE_NUM || mCurrentMode < 0) {
			MyLog.print("changeSafeMode This mode crossing the line.mode = "
					+ mCurrentMode);
			return;
		}
		if (model == mCurrentMode
				&& model != ConstConf.UNSAFE_MODE) {
			MyLog.print("cannot set the same safemode");
			return;
		}
		boolean addLog = (mCurrentMode != ConstConf.TEST_MODE 
				&& model != ConstConf.TEST_MODE);
		DPDBHelper.setSafeMode(model);
		mCurrentMode = DPDBHelper.getSafeMode();
		mEnable = DPDBHelper.getSafeModeEnable(mCurrentMode);
		// �ɲ���ģʽ�л�������ģʽ������ģʽ�л�������ģʽ������Ҫ����ģʽ��¼��ͬ�����ڷֻ�����
		if (addLog) {
			sendBroadcast(new Intent(DPFunction.ACTION_SAFE_MODE));
			SafeModeInfo info = new SafeModeInfo(mCurrentMode, System.currentTimeMillis());
			if (ProjectConfigure.project == 2) {
				int result = TalkManager.toManageAlarm(
						CommonUT.formatTime(info.getTime()), 
						mCurrentMode + 90,
						98,
						0);
				if (result == 0) {
					info.setIsSuccess(true);
				}
			}
			DPDBHelper.addSafeModeLog(info);
			if (!isAuto) {
				new Thread() {
					@Override
					public void run() {
						DPFunction.synchPhoneSafeMode();
						DPFunction.synchRoomSafeMode();
					};
				}.start();
			}
		}
		oldState = mConnection; //��ʼĬ����һ�νӷ���Ĭ��״̬
	}

	/**
	 * ���ð�������̽ͷ�ӷ�-�ߵ͵�ƽ
	 * @param connection
	 */
	public void changeSafeConnection(long connection) {
		DPDBHelper.setSefeConnection(connection);
		mConnection = DPDBHelper.getSefeConnection();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		MyLog.print(TAG, "onDestroy");
		mInstance = null;
		DPSafe.unInitDPSafe();
		if (mGpioSet != null) {
			mGpioSet.UninitGpio(mDoorbell_H17);
			mGpioSet = null;
		}
		if (mSafeScheduled != null) {
			mSafeScheduled.shutdown();
			mSafeScheduled = null;
		}

		if (mSoundPool != null) {
			if (mStreamID != 0) {
				mSoundPool.stop(mStreamID);
				mStreamID = 0;
			}
			mSoundPool.release();
			mSoundPool = null;
		}
		mHandler.getLooper().quit();
		mHandler.removeCallbacksAndMessages(null);
		mHandlerThread.quit();
	}
}

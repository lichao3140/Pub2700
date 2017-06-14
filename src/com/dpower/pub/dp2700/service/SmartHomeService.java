package com.dpower.pub.dp2700.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.util.CommonUT;
import com.dpower.util.ConstConf;
import com.dpower.util.MyLog;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;

/**
 * ���ܼҾӷ��� 
 */
public class SmartHomeService extends Service {
	private static final String TAG = "SmartHomeService";
	
	/** ����ģʽ */
	public final static String ACTION_IN_BED = "action_smart_home_in_bed";
	
	/** �ڼ�ģʽ */
	public final static String ACTION_IN_HOME = "action_smart_home_in_home";
	
	/** ȫ��ģʽ */
	public final static String ACTION_ALL_OFF = "action_smart_home_all_off";
	
	/** ����ģʽ */
	public final static String ACTION_IN_DINNER = "action_smart_home_in_dinner";
	
	/** ����ģʽģʽ */
	public final static String ACTION_IN_ENTERTAINMENT = "action_smart_home_in_entertainment";
	
	/** Ӱ��ģʽ */
	public final static String ACTION_IN_VIDEO = "action_smart_home_in_video";

	/** �������ܼҾӷ��� */
	public final static String ACTION_SERVER_START = "action_smart_home_server_start";

	/** �ر����ܼҾӷ��� */
	public final static String ACTION_SERVER_OFF = "action_smart_home_server_off";

	/** ���ܼҾӷ���㲥</br> ���Extraʹ�� */
	public final static String ACTION_SMART_HOME = "action_smart_home";

	/** ���ܼҾ�ҳ��㲥</br> ���Extraʹ�� */
	public final static String ACTION_SMART_HOME_CLIENT = "action_smart_home_client";

	// �����ڼҰ�ť
	public static final byte[] KEY_AT_HOME = { 0, 0, 0, 2, 0, 0, 0, 1, 2 };
	// ���¾��ް�ť
	public static final byte[] KEY_IN_BED = { 0, 0, 0, 2, 0, 0, 0, 1, 1 };
	// ���¾�ȫ�ذ�ť
	public static final byte[] KEY_DISBLEAll = { 0, 0, 0, 2, 0, 0, 0, 1, 3 };
	// �ͲͰ�ť
	public static final byte[] KEY_DINNER = { 0, 0, 0, 2, 0, 0, 0, 1, 4 };
	// ���ְ�ť
	public static final byte[] KEY_YL = { 0, 0, 0, 2, 0, 0, 0, 1, 5 };
	// Ӱ�Ӱ�ť
	public static final byte[] KEY_MEIDA = { 0, 0, 0, 2, 0, 0, 0, 1, 6 };

	private String mWlanIp;
	private String mIp;
	private SmartServerBroadcast mSmartServer;
	private ReceiverThread mReceiverThread;
	private SocketThread mSocketThread;
	private Socket mClientSocket;
	private InetSocketAddress mAddress;
	private boolean mFlag = true;
	private byte[] mHeartArray, mByteArray;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mByteArray = new byte[8];
		mHeartArray = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
		mIp = SPreferences.getInstance().getSmartServerIP();
		if (!CommonUT.isIp(mIp)) {
			MyLog.print(TAG, "ip is error:" + mIp);
			mIp = "127.0.0.1";
			MyLog.print(TAG, "use default ip:" + mIp);
		}
		if (CommonUT.getLanConnectState(ConstConf.WAN_NETWORK_CARD)) {
			WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			if (!wifiManager.isWifiEnabled()) {
				mWlanIp = "0.0.0.0";
			} else {
				WifiInfo wifiInfo = wifiManager.getConnectionInfo();
				int ipAddress = wifiInfo.getIpAddress();
				mWlanIp = CommonUT.intToIp(ipAddress);
			}
			MyLog.print(TAG, mWlanIp);
		}
		mSmartServer = new SmartServerBroadcast();
		registerReceiver(mSmartServer, new IntentFilter(ACTION_SMART_HOME));
		mReceiverThread = new ReceiverThread();
		mSocketThread = new SocketThread();
	}
	
	private class SmartServerBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ACTION_SMART_HOME)) {
				String mode = intent.getStringExtra("mode");
				MyLog.print(TAG, mode);
				try {
					if (mode.equals(ACTION_ALL_OFF)) {
						sendMsg(KEY_DISBLEAll);
					} else if (mode.equals(ACTION_IN_BED)) {
						sendMsg(KEY_IN_BED);
					} else if (mode.equals(ACTION_IN_HOME)) {
						sendMsg(KEY_AT_HOME);
					} else if (mode.equals(ACTION_IN_DINNER)) {
						sendMsg(KEY_DINNER);
					} else if (mode.equals(ACTION_IN_VIDEO)) {
						sendMsg(KEY_MEIDA);
					} else if (mode.equals(ACTION_IN_ENTERTAINMENT)) {
						sendMsg(KEY_YL);
					} else if (mode.equals(ACTION_SERVER_START)) {
						if (mSocketThread != null && mClientSocket != null
								&& mFlag) { // ��������̬
							SPreferences.getInstance().saveSmartServerState(true);
							sendBroadcast(new Intent(DPFunction.ACTION_SMART_HOME_MODE));
							return;
						} else if (mSocketThread != null && mClientSocket != null
								&& !mFlag) { // socketδ�رյ���ֹͣ���ջ��߲��������
							newReceiverThread();
							mReceiverThread.start();
						} else if (mSocketThread != null && mClientSocket == null) {
							if (mSocketThread.getState() != Thread.State.NEW) {
								mSocketThread.interrupt();
								mSocketThread = null;
								newSocket();
							}
							mSocketThread.start();
						} else {
							newSocket();
							mSocketThread.start();
						}
					} else if (mode.equals(ACTION_SERVER_OFF)) {
						if (mClientSocket != null) { // �����ر�socket
							mFlag = false;
							closeClientSocket();
						} else {
							SPreferences.getInstance().saveSmartServerState(false);
							sendBroadcast(new Intent(DPFunction.ACTION_SMART_HOME_MODE));
						}
						MyLog.print(TAG, ACTION_SERVER_OFF);
					}
					// ���Ͷ�Ӧָ��
				} catch (Exception e) {
					MyLog.print(TAG, "onReceive:" + e.toString());
				}
			}
		}
	}
	
	private void sendMsg(byte[] msg) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				mClientSocket.getOutputStream()));
		writer.write(new String(msg));
		writer.flush();
	}
	
	private class ReceiverThread extends Thread {

		@Override
		public void run() {
			super.run();
			String string;
			MyLog.print(TAG, "ReceiverThread run");
			while (mFlag) {
				try {
					if (mClientSocket != null) {
						if (!mClientSocket.isConnected()) {
							MyLog.print(TAG, "reconnet");
							if (mAddress != null) {
								mClientSocket.connect(mAddress);
							} else {
								break; // ��������ַΪ��
							}
							continue;
						} else {
							if (!SPreferences.getInstance().getSmartServerState()) {
								SPreferences.getInstance().saveSmartServerState(true);
								sendBroadcast(new Intent(DPFunction.ACTION_SMART_HOME_MODE));
								MyLog.print(TAG, "mClientSocket isConnected");
							}
						}
						string = receiverMsg();
						if (string.equals(new String(mHeartArray))) {
							sendMsg(mHeartArray);
							SPreferences.getInstance().saveSmartServerState(true);
							sendBroadcast(new Intent(DPFunction.ACTION_SMART_HOME_MODE));
						}
						MyLog.print(TAG, "receiver:" + string);
					} else {
						mFlag = false;
						MyLog.print(TAG, "clientSocket is null");
						break;
					}
					Thread.sleep(500);
				} catch (Exception e) {
					MyLog.print(TAG, e.toString());
					mFlag = false;
				} finally {
					MyLog.print(TAG, "ReceiverThread final");
				}
			}
		}
	}
	
	public String receiverMsg() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				mClientSocket.getInputStream()));
		String string = "";
		int i = 0;
		while (true) {
			int temp = reader.read();
			if (temp != -1) {
				mByteArray[i] = (byte) temp;
				i++;
				string = new String(mByteArray);
				MyLog.print(TAG, "read ��" + i + "  " + string);
				if (string.equals(new String(mHeartArray))) {
					break;
				}
			} else {
				break;
			}
		}
		return string;
	}
	
	/**
	 * ����socket����
	 */
	private class SocketThread extends Thread {

		@Override
		public void run() {
			super.run();
			try {
				mClientSocket = new Socket();
				mClientSocket.setReuseAddress(true);
				InetSocketAddress localAddress = new InetSocketAddress(
						InetAddress.getByName(mWlanIp), 30011);
				mAddress = new InetSocketAddress(InetAddress.getByName(mIp), 3001);
				mClientSocket.bind(localAddress);
				mClientSocket.connect(mAddress);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (mClientSocket != null) {
					if (mReceiverThread != null) {
						mReceiverThread.start();
						MyLog.print(TAG, "mReceiverThread:" + mReceiverThread.getName());
					}
					MyLog.print(TAG, "mClientSocket host adds:"
							+ mClientSocket.getLocalAddress().getHostAddress());
					MyLog.print(TAG, "mClientSocket local port:"
							+ mClientSocket.getLocalPort());
				}
			}
		}
	}
	
	private void newReceiverThread() {
		mFlag = false;
		if (mReceiverThread != null) {
			mReceiverThread.interrupt();
		}
		mFlag = true; // ����ѭ������
		mReceiverThread = new ReceiverThread();
	}
	
	/**
	 * �½�һ��clientSocket�Լ��շ��߳�
	 */
	private boolean newSocket() {
		mFlag = false;// ��֤�����߳�ֹͣѭ��
		if (mSocketThread != null) {
			if (mSocketThread.isAlive()) {
				mSocketThread.interrupt();
				mSocketThread = null;
			}
		}
		if (mReceiverThread != null) {
			mReceiverThread.interrupt();
		}
		mReceiverThread = new ReceiverThread();
		mSocketThread = new SocketThread();
		mFlag = true; // ����ѭ������
		return true;
	}
	
	private void closeClientSocket() {
		if (mClientSocket != null) {
			try {
				mClientSocket.shutdownInput();
				mClientSocket.shutdownOutput();
				InputStream in = mClientSocket.getInputStream();
				OutputStream ou = mClientSocket.getOutputStream();
				try {
					in.close();
					ou.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				mClientSocket.close();
				if (mSocketThread != null) {
					mSocketThread.interrupt();
					mSocketThread = null;
				}
				if (mReceiverThread != null) {
					mReceiverThread.interrupt();
					mReceiverThread = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				mClientSocket = null;
				MyLog.print(TAG, "close socket");
				SPreferences.getInstance().saveSmartServerState(false);
				sendBroadcast(new Intent(DPFunction.ACTION_SMART_HOME_MODE));
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mFlag = false;
		if (mSmartServer != null) {
			unregisterReceiver(mSmartServer);
		}
		closeClientSocket();
	}
}

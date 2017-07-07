/* $Id: MyApp.java 5022 2015-03-25 03:41:21Z nanang $ */
/*
 * Copyright (C) 2013 Teluu Inc. (http://www.teluu.com)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.dpower.dpsiplib.service;

import java.io.File;
import java.util.ArrayList;

import org.pjsip.pjsua2.AccountConfig;
import org.pjsip.pjsua2.ContainerNode;
import org.pjsip.pjsua2.Endpoint;
import org.pjsip.pjsua2.EpConfig;
import org.pjsip.pjsua2.JsonDocument;
import org.pjsip.pjsua2.LogConfig;
import org.pjsip.pjsua2.LogEntry;
import org.pjsip.pjsua2.LogWriter;
import org.pjsip.pjsua2.MediaConfig;
import org.pjsip.pjsua2.StringVector;
import org.pjsip.pjsua2.TransportConfig;
import org.pjsip.pjsua2.UaConfig;
import org.pjsip.pjsua2.pjsip_transport_type_e;
import com.dpower.dpsiplib.callback.MyAppCallback;
import com.dpower.dpsiplib.utils.SIPIntercomLog;

public class MyApp {
	
	public static Endpoint endpoint = null;
	public static MyAppCallback callback = null;
	private static final String CONFIG_NAME = "pjsua2.json";
	private static final int SIP_PORT = 5060;// sip¶Ë¿Ú
	private ArrayList<MyAccount> mAccounts = null;
	private ArrayList<MyAccountConfig> mAccountConfigs = null;
	private TransportConfig mTransportConfig;
	private String mAppDir;
	private MyLogWriter mLogWriter;
	private EpConfig mEpConfig;
	
	public MyApp() {
		endpoint = new Endpoint();
		mLogWriter  = new MyLogWriter();
		mTransportConfig = new TransportConfig();
		mAccounts = new ArrayList<MyAccount>();
		mAccountConfigs = new ArrayList<MyAccountConfig>();
		mEpConfig = new EpConfig();
		MediaConfig mc = mEpConfig.getMedConfig();
        mc.setEcOptions(3);
        mc.setEcTailLen(5);
//		epConfig.getLogConfig().setLevel(LOG_LEVEL);
//		epConfig.getLogConfig().setConsoleLevel(LOG_LEVEL);

		/* Set log config. */
		LogConfig log_cfg = mEpConfig.getLogConfig();
//		logWriter
		log_cfg.setWriter(mLogWriter);
//		log_cfg.setDecor(log_cfg.getDecor()
//				& ~(pj_log_decoration.PJ_LOG_HAS_CR.swigValue() | pj_log_decoration.PJ_LOG_HAS_NEWLINE
//						.swigValue()));
	}

	public void init(MyAppCallback obs, String app_dir, String localip) {
		init(obs, app_dir, localip, false);
	}

	public void init(MyAppCallback obs, String app_dir, String localip, 
			boolean own_worker_thread) {
		callback = obs;
		mAppDir = app_dir;
		/* Create endPoint */
		try {
			endpoint.libCreate();
		} catch (Exception e) {
			return;
		}

		/* Load config */
		String configPath = mAppDir + "/" + CONFIG_NAME;
		File f = new File(configPath);
		if (f.exists()) {
			loadConfig(configPath);
		} else {
			/* Set 'default' values */
//			mTransportConfig.setPort(SIP_PORT);
		}

		/* Override log level setting */
//		epConfig.getLogConfig().setLevel(LOG_LEVEL);
//		epConfig.getLogConfig().setConsoleLevel(LOG_LEVEL);
//
//		/* Set log config. */
//		LogConfig log_cfg = epConfig.getLogConfig();
////		logWriter
//		log_cfg.setWriter(logWriter);
//		log_cfg.setDecor(log_cfg.getDecor()
//				& ~(pj_log_decoration.PJ_LOG_HAS_CR.swigValue() | pj_log_decoration.PJ_LOG_HAS_NEWLINE
//						.swigValue()));
//
		/* Set ua config. */
		UaConfig ua_cfg = mEpConfig.getUaConfig();
//		ua_cfg.setUserAgent("Pjsua2 Android " + endpoint.libVersion().getFull());
		ua_cfg.setUserAgent("DpAndroidDevice");
		StringVector stun_servers = new StringVector();
//		stun_servers.add("192.168.1.1");
		ua_cfg.setStunServer(stun_servers);
		if (own_worker_thread) {
			ua_cfg.setThreadCnt(0);
			ua_cfg.setMainThreadOnly(true);
		}

		/* Init endpoint */
		try {
			endpoint.libInit(mEpConfig);
		} catch (Exception e) {
			return;
		}
		
		try {
			endpoint.codecSetPriority("SILK/8000", (short) 250);
			endpoint.codecSetPriority("PCMU/8000", (short) 240);
			endpoint.codecSetPriority("PCMA/8000", (short) 230);
			endpoint.codecSetPriority("SILK/16000", (short) 220);
        } catch (Exception e) {
            e.printStackTrace();
        }

		/* Create transports. */
		try {
			mTransportConfig.setPort(SIP_PORT);
			endpoint.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_UDP,
					mTransportConfig);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			mTransportConfig.setPort(SIP_PORT);
			endpoint.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_TCP,
					mTransportConfig);
		} catch (Exception e) {
			e.printStackTrace();
		}

		/* Start. */
		try {
			endpoint.libStart();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void loadConfig(String fileName) {
		JsonDocument json = new JsonDocument();
		try {
			/* Load file */
			json.loadFile(fileName);
			ContainerNode root = json.getRootContainer();

			/* Read endpoint config */
			mEpConfig.readObject(root);

			/* Read transport config */
			ContainerNode tp_node = root.readContainer("SipTransport");
			mTransportConfig.readObject(tp_node);

			/* Read account configs */
			mAccountConfigs.clear();
			ContainerNode accs_node = root.readArray("accounts");
			while (accs_node.hasUnread()) {
				MyAccountConfig acc_cfg = new MyAccountConfig();
				acc_cfg.readObject(accs_node);
				mAccountConfigs.add(acc_cfg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		/*
		 * Force delete json now, as I found that Java somehow destroys it after
		 * lib has been destroyed and from non-registered thread.
		 */
		json.delete();
	}
	
	public void deinit() {
		String configPath = mAppDir + "/" + CONFIG_NAME;
		saveConfig(configPath);

		/*
		 * Try force GC to avoid late destroy of PJ objects as they should be
		 * deleted before lib is destroyed.
		 */
		Runtime.getRuntime().gc();

		/*
		 * Shutdown pjsua. Note that Endpoint destructor will also invoke
		 * libDestroy(), so this will be a test of double libDestroy().
		 */
		try {
			endpoint.libDestroy();
		} catch (Exception e) {
		}

		/*
		 * Force delete Endpoint here, to avoid deletion from a non- registered
		 * thread (by GC?).
		 */
		endpoint.delete();
		endpoint = null;
		for(int i = 0; i< mAccounts.size(); i++) {
			delAccount(mAccounts.get(i));
		}
		callback = null;
	}
	
	private void saveConfig(String fileName) {
		JsonDocument json = new JsonDocument();
		try {
			/* Write endpoint config */
			json.writeObject(mEpConfig);

			/* Write transport config */
			ContainerNode tp_node = json.writeNewContainer("SipTransport");
			mTransportConfig.writeObject(tp_node);

			/* Write account configs */
			buildAccountConfigs();
			ContainerNode accs_node = json.writeNewArray("accounts");
			for (int i = 0; i < mAccountConfigs.size(); i++) {
				mAccountConfigs.get(i).writeObject(accs_node);
			}

			/* Save file */
			json.saveFile(fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		/*
		 * Force delete json now, as I found that Java somehow destroys it after
		 * lib has been destroyed and from non-registered thread.
		 */
		json.delete();
	}
	
	private void buildAccountConfigs() {
		/* Sync accCfgs from accList */
		mAccountConfigs.clear();
		for (int i = 0; i < mAccounts.size(); i++) {
			MyAccount acc = mAccounts.get(i);
			MyAccountConfig my_acc_cfg = new MyAccountConfig();
			my_acc_cfg.accCfg = acc.cfg;
			my_acc_cfg.buddyCfgs.clear();
			for (int j = 0; j < acc.buddyList.size(); j++) {
				MyBuddy bud = acc.buddyList.get(j);
				my_acc_cfg.buddyCfgs.add(bud.buddyConfig);
			}
			mAccountConfigs.add(my_acc_cfg);
		}
	}
	
	
	public MyAccountConfig getAccountConfig() {
		if(mAccountConfigs.size() >0) {
			return mAccountConfigs.get(0);
		}
		return null;
	}

	public MyAccount addAccount(AccountConfig cfg) {
		SIPIntercomLog.print("addAccount");
		MyAccount account = new MyAccount(cfg);
		try {
			account.create(cfg);
		} catch (Exception e) {
			account = null;
			return null;
		}
		mAccounts.add(account);
		SIPIntercomLog.print(SIPIntercomLog.ERROR, "accounts = " + mAccounts.size());
		return account;
	}

	public void delAccount(MyAccount account) {
		SIPIntercomLog.print(SIPIntercomLog.ERROR, "delAccount");
		mAccounts.remove(account);
		SIPIntercomLog.print(SIPIntercomLog.ERROR, "accounts = " + mAccounts.size());
	}
	
	static {
		try {
			System.loadLibrary("openh264");
			System.loadLibrary("yuv");
			System.loadLibrary("webrtc");
		} catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
		}
		System.loadLibrary("pjsua2");
		System.loadLibrary("Base64Code");
		SIPIntercomLog.print("Library loaded");
	}
}

class MyLogWriter extends LogWriter {
	
	@Override
	public void write(LogEntry entry) {
		SIPIntercomLog.print(SIPIntercomLog.DEBUG, entry.getMsg());
	}
}

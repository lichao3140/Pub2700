package com.dpower.pub.dp2700.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.dpower.pub.dp2700.R;
import com.dpower.util.ConstConf;
import com.dpower.util.ProjectConfigure;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

public class SPreferences {
	
	private static SPreferences mPreferences;
	private static Drawable mWallpaper;
	private SharedPreferences mReader;
	private SharedPreferences.Editor mWriter;
	private Context mContext;

	private SPreferences() {

	}

	public static SPreferences getInstance() {
		if (mPreferences == null) {
			mPreferences = new SPreferences();
		}
		return mPreferences;
	}

	public void setContext(Context context) {
		mReader = context.getSharedPreferences("com.dpower.xml",
				Context.MODE_PRIVATE);
		mWriter = context.getSharedPreferences("com.dpower.xml",
				Context.MODE_PRIVATE).edit();
		mContext = context;
	}

	public SharedPreferences getReader() {
		return mReader;
	}

	public SharedPreferences.Editor getWriter() {
		return mWriter;
	}

	/**
	 * 保存铃声设置
	 * @param path
	 */
	public void setRingAbsolutePath(String path) {
		mWriter.putString("ringPath", path);
		mWriter.commit();
	}

	/**
	 * 获取铃声设置
	 */
	public String getRingAbsolutePath() {
		return mReader.getString("ringPath", 
				ConstConf.RING_PATH + ConstConf.RING_MP3);
	}

	/**
	 * 保存通话音量
	 */
	public void setTalkingVolume(int talkingVolume) {
		mWriter.putInt("talking_volume", talkingVolume);
		mWriter.commit();
	}

	/**
	 * 获取通话音量
	 */
	public int getTalkingVolume() {
		return mReader.getInt("talking_volume", 3);
	}
	
	/**
	 * 保存屏保设置
	 */
	public void setScreenSaverMode(int mode) {
		mWriter.putInt("screen_saver", mode);
		mWriter.commit();
	}

	/**
	 * 获取屏保设置
	 */
	public int getScreenSaverMode() {
		return mReader.getInt("screen_saver", 0);
	}

	/**
	 * 保存是否绑定手机
	 * @param flag
	 */
	public void setBindPhone(Boolean flag) {
		mWriter.putBoolean("bindphone", flag);
		mWriter.commit();
	}

	/**
	 * 获取是否绑定手机
	 */
	public boolean getBindPhone() {
		return mReader.getBoolean("bindphone", false);
	}

	/**
	 * 保存是否呼叫转移
	 * @param flag
	 */
	public void setCallForward(Boolean flag) {
		mWriter.putBoolean("callforward", flag);
		mWriter.commit();
	}

	/**
	 * 获取是否呼叫转移
	 */
	public boolean getCallForward() {
		return mReader.getBoolean("callforward", true);
	}

	/**
	 * @return 系统是否为静音状态
	 */
	public boolean isSystemSilent() {
		return mReader.getBoolean("is_allow_belling", false);
	}

	/**
	 * @param isSilent
	 *            系统是否为静音状态
	 */
	public void setSystemSilent(boolean isSilent) {
		mWriter.putBoolean("is_allow_belling", isSilent);
		mWriter.commit();
	}

	/**
	 * @param ringVol
	 *            : 振铃音量
	 */
	public void setRingVol(int ringVol) {
		mWriter.putInt("ring_volume", ringVol);
		mWriter.commit();
	}

	/**
	 * @return 振铃音量
	 */
	public int getRingVol() {
		return mReader.getInt("ring_volume", 15);
	}

	/**
	 * 获取壁纸设置的方式 false --读取APK内置壁纸 true----读取内置SdCard/wallPaper下的壁纸
	 */
	public boolean getWallpaperFlag() {
		return mReader.getBoolean("wallpaperflag", false);
	}

	/**
	 * 设置壁纸读取的方式 false --读取APK内置壁纸 true----读取内置SdCard/wallPaper下的壁纸
	 * 
	 * @param wallpaperFlag
	 */
	public void setWallpaperFlag(boolean wallpaperFlag) {
		mWriter.putBoolean("wallpaperflag", wallpaperFlag);
		mWriter.commit();
		mWallpaper = readWallpaper();
	}

	public void saveWallpaper(String path) {
		mWriter.putString("wallpaperUrl", path);
		mWriter.putInt("wallpaperResouce", 0);
		mWriter.commit();
		mWallpaper = readWallpaper();
	}

	public void saveWallpaper(int resId) {
		mWriter.putInt("wallpaperResouce", resId);
		mWriter.putString("wallpaperUrl", null);
		mWriter.commit();
		mWallpaper = readWallpaper();
	}

	public Drawable getWallpaper() {
		if (mWallpaper == null) {
			mWallpaper = readWallpaper();
		}
		return mWallpaper;
	}
	
	private Drawable readWallpaper() {
		Drawable drawable = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		if (!TextUtils.isEmpty(mReader.getString("wallpaperUrl", null))) {
			String path = mReader.getString("wallpaperUrl", null);
			// 当壁纸设置为SD卡的图片时，拔出SD卡后要恢复默认
			if (!new File(path).exists()) {
				mWriter.putString("wallpaperUrl", null);
				mWriter.putInt("wallpaperResouce", 0);
				mWriter.commit();
				return readWallpaper();
			}
			InputStream stream = null;
			try {
				stream = new FileInputStream(path);
				options.inPreferredConfig = Bitmap.Config.RGB_565;
				Bitmap bitmap = BitmapFactory.decodeStream(stream, null, options);
				drawable = new BitmapDrawable(mContext.getResources(), bitmap);
			} catch (FileNotFoundException e) {
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
		} else {
			int resId = mReader.getInt("wallpaperResouce", 0);
			options.inPreferredConfig = Bitmap.Config.RGB_565;
			if (resId == 0) { // 没有获取到壁纸设置信息，使用默认壁纸
				if (ProjectConfigure.project == 1) {
					resId = R.drawable.bg_main_lqh;
				} else {
					resId = R.drawable.bg_main;
				}
				InputStream stream = mContext.getResources().openRawResource(resId);
		        Bitmap bm = BitmapFactory.decodeStream(stream, null, options);
		        drawable = new BitmapDrawable(mContext.getResources(), bm);
		        try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
			} else {
				if (mContext.getResources().getResourceName(resId) != null) {
					InputStream stream = mContext.getResources().openRawResource(resId);
			        Bitmap bm = BitmapFactory.decodeStream(stream, null, options);
			        drawable = new BitmapDrawable(mContext.getResources(), bm);
			        try {
	                    stream.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
				}
			}
		}
		return drawable;
	}

	public String getWanIP() {
		return mReader.getString("wanIP", "121.43.185.244");
	}

	public void saveWanIP(String wanIP) {
		mWriter.putString("wanIP", wanIP);
		mWriter.commit();
	}

	/*** 网络摄像头设置 */
	public void saveWebCameraIP(String cameraIP) {
		mWriter.putString("webCameraIP", cameraIP);
		mWriter.commit();
	}

	public String getWebCameraIP() {
		return mReader.getString("webCameraIP", "192.168.1.56");
	}

	public void saveWebCameraUserName(String userName) {
		mWriter.putString("webcameraUserName", userName);
		mWriter.commit();
	}

	public String getWebCameraUserName() {
		return mReader.getString("webcameraUserName", "admin");
	}

	public void saveWebCameraPassword(String password) {
		mWriter.putString("webcameraPassword", password);
		mWriter.commit();
	}

	public String getWebCameraPassword() {
		return mReader.getString("webcameraPassword", "admin");
	}

	/*** 第三方智能家居服務器IP */
	public void saveSmartServerIP(String serverIP) {
		mWriter.putString("smartSevrIP", serverIP);
		mWriter.commit();
	}

	public String getSmartServerIP() {
		return mReader.getString("smartSevrIP", "192.168.1.1");
	}

	/*** 第三方智能家居服务器状态 */
	public void saveSmartServerState(boolean state) {
		mWriter.putBoolean("smartSevrState", state);
		mWriter.commit();
	}

	public boolean getSmartServerState() {
		return mReader.getBoolean("smartSevrState", false);
	}
}

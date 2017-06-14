package com.dpower.pub.dp2700.tools;

import java.lang.reflect.Method;
import java.util.Locale;

import org.example.language.LanguageManager;

import com.dpower.util.MyLog;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

public class Language {
	private static final String TAG = "Language";
	
	private static LanguageManager mLanguageManager;
	
	public static void updateLaguage(Context context, int flag) {
		Resources resources = context.getResources();
		Configuration config = resources.getConfiguration();
		DisplayMetrics dm = resources.getDisplayMetrics();
		mLanguageManager = new LanguageManager(context);
		
		if (flag == 1) {
			config.locale = Locale.SIMPLIFIED_CHINESE;
			mLanguageManager.setLanguage(LanguageManager.CH_EASY);
			MyLog.print(TAG, "保存中文语言");
		} else {
			config.locale = Locale.ENGLISH;
			mLanguageManager.setLanguage(LanguageManager.English_UnitedStates);
			MyLog.print(TAG, "保存英文语言");
		}
		resources.updateConfiguration(config, dm);
	}
	
	public static void updateLaguage(int flag) {  
	    try {  
	        Object objIActMag;
	        Class<?> clzIActMag = Class.forName("android.app.IActivityManager");  
	        Class<?> clzActMagNative = Class.forName("android.app.ActivityManagerNative");  
	        Method mtdActMagNative$getDefault = clzActMagNative.getDeclaredMethod("getDefault");  
	        objIActMag = mtdActMagNative$getDefault.invoke(clzActMagNative);  
	        Method mtdIActMag$getConfiguration = clzIActMag.getDeclaredMethod("getConfiguration");  
	        Configuration config = (Configuration) mtdIActMag$getConfiguration.invoke(objIActMag);  
	    	if (flag == 1) {
				config.locale = Locale.SIMPLIFIED_CHINESE;
				MyLog.print(TAG, "保存中文语言");
			} else {
				config.locale = Locale.ENGLISH;
				MyLog.print(TAG, "保存英文语言");
			}
	        Class<?>[] clzParams = { Configuration.class };  
	        Method mtdIActMag$updateConfiguration = clzIActMag.getDeclaredMethod(  
	                "updateConfiguration", clzParams);  
	        mtdIActMag$updateConfiguration.invoke(objIActMag, config);  
	    } catch (Exception e) {  
	        e.printStackTrace();  
	    }  
	}
}

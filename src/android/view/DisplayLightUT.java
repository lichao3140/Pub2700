package android.view;

import com.dpower.util.MyLog;

import android.content.Context;

/**
 * @author ZhengZhiying
 * @Funtion	调节屏幕显示参数
 */
public class DisplayLightUT {
	private static final String TAG = "DisplayLightUT";
	
	private static DisplayLightUT mDisplayLightUT;
	private DisplayManagerAw mDisplayManagerAw;
	
	public static DisplayLightUT getInstance(Context context){
		if(mDisplayLightUT == null){
			mDisplayLightUT = new DisplayLightUT(context);
		}
		return mDisplayLightUT;
	}
	
	private DisplayLightUT(Context context){
		mDisplayManagerAw = (DisplayManagerAw) context
				.getSystemService("display_aw");
	}
	
	/**
	 * @param progress	亮度 0-100
	 */
	private void setDisplayBright(int progress){
		mDisplayManagerAw.setDisplayBright(0, progress);
	}
	
	/**
	 * @param progress 对比度 0-100
	 */
	private void setDisplayContrast(int progress){
		mDisplayManagerAw.setDisplayContrast(0, progress);
	}
	
	/**
	 * @param progress 饱和度 0-100
	 */
	private void setDisplaySaturation(int progress){
		mDisplayManagerAw.setDisplaySaturation(0, progress);
	}
	
	/**
	 * @param progress 色相 0-100
	 */
	private void setDisplayHue(int progress){
		mDisplayManagerAw.setDisplayHue(0, progress);
	}
	
	/**
	 *  设置为非视频的正常状态
	 */
	public void toNormal(){
		MyLog.print(TAG, "设置屏幕参数（50,50,50,50）");
		setDisplayBright(50);
		setDisplayContrast(50);
		setDisplaySaturation(50);
		setDisplayHue(50);
	}
	
	/**
	 *  设置为有视频的情况
	 */
	public void toVideo(){
//		MyLog.print(TAG,"设置屏幕参数（47,42,100,51）");
		// 修复成不改变
//		setDisplayBright(47);
//		setDisplayContrast(42);
//		setDisplaySaturation(100);
//		setDisplayHue(51);
	}
}

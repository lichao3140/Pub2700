package android.view;

import com.dpower.util.MyLog;

import android.content.Context;

/**
 * @author ZhengZhiying
 * @Funtion	������Ļ��ʾ����
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
	 * @param progress	���� 0-100
	 */
	private void setDisplayBright(int progress){
		mDisplayManagerAw.setDisplayBright(0, progress);
	}
	
	/**
	 * @param progress �Աȶ� 0-100
	 */
	private void setDisplayContrast(int progress){
		mDisplayManagerAw.setDisplayContrast(0, progress);
	}
	
	/**
	 * @param progress ���Ͷ� 0-100
	 */
	private void setDisplaySaturation(int progress){
		mDisplayManagerAw.setDisplaySaturation(0, progress);
	}
	
	/**
	 * @param progress ɫ�� 0-100
	 */
	private void setDisplayHue(int progress){
		mDisplayManagerAw.setDisplayHue(0, progress);
	}
	
	/**
	 *  ����Ϊ����Ƶ������״̬
	 */
	public void toNormal(){
		MyLog.print(TAG, "������Ļ������50,50,50,50��");
		setDisplayBright(50);
		setDisplayContrast(50);
		setDisplaySaturation(50);
		setDisplayHue(50);
	}
	
	/**
	 *  ����Ϊ����Ƶ�����
	 */
	public void toVideo(){
//		MyLog.print(TAG,"������Ļ������47,42,100,51��");
		// �޸��ɲ��ı�
//		setDisplayBright(47);
//		setDisplayContrast(42);
//		setDisplaySaturation(100);
//		setDisplayHue(51);
	}
}

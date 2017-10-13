package com.dpower.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import com.dpower.util.MyLog;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.SystemClock;

public class CommonUT {
	private static final String TAG = "CommonUT";
	/**
	 * �ڵ���ɫ
	 */
	private static final int BLACK = 0xFF000000;
	/**
	 * ��ɫ
	 */
	private static final int WHITE = 0xFFFFFFFF; 
	/**
	 * �����ζ�ά����
	 */
	private static final int CODE_WIDTH = 440;
	/**
	 * LOGO���ֵ,����ܴ��ڶ�ά��20%���ֵ,���ڿ��ܻᵼ�¶�ά����ϢʧЧ
	 */
	private static final int LOGO_WIDTH_MAX = CODE_WIDTH / 6;
	/**
	 *LOGO���ֵ,��С����С�ڶ�ά��10%���ֵ,С��Ӱ��Logo���ά����������
	 */
	private static final int LOGO_WIDTH_MIN = CODE_WIDTH / 10;
	
	public static boolean isIp(String strIP) {
		if (strIP == null || strIP.length() == 0)
			return false;
		if (strIP.length() < 7)
			return false;
		boolean b = false;
		String IP = strIP.trim();
		if (IP.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
			String s[] = IP.split("\\.");
			if (Integer.parseInt(s[0]) <= 255)
				if (Integer.parseInt(s[1]) <= 255)
					if (Integer.parseInt(s[2]) <= 255)
						if (Integer.parseInt(s[3]) <= 255)
							b = true;
		}
		return b;
	}
	
	public static String intToIp(int ip) {
		return Integer.toString(ip & 0xFF) + "."
			+ Integer.toString((ip >> 8) & 0xFF) + "."
			+ Integer.toString((ip >> 16) & 0xFF) + "."
			+ Integer.toString((ip >> 24) & 0xFF);
	}
	
	/**
	 * �����ļ�
	 * @param httpUrl ���ص�ַ
	 * @param saveFile ����·��
	 * @return
	 */
	public static boolean httpDownload(String httpUrl, String saveFile) {
		// ���������ļ�
		boolean result = false;
		URL url = null;
		try {
			url = new URL(httpUrl);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			return result;
		}
		InputStream inputStream = null;
		FileOutputStream outputStream = null;
		try {
			URLConnection conn = url.openConnection();
			inputStream = conn.getInputStream();
			outputStream = new FileOutputStream(saveFile);
			byte[] buffer = new byte[1024];
			int len;
			while ((len = inputStream.read(buffer)) > 0) {
				outputStream.write(buffer, 0, len);
			}
			outputStream.flush();
			outputStream.getFD().sync();
			MyLog.print(TAG, "���������ļ��ɹ�");
			result = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public static boolean getLanConnectState(String lanName) {
		Process process = null;
		InputStream inputStream = null;
		ByteArrayOutputStream outputStream = null;
		String cmd = "ifconfig " + lanName;
		String result = "";
		try {
			outputStream = new ByteArrayOutputStream();
			process = Runtime.getRuntime().exec(cmd);
			outputStream.write("/n".getBytes());
			inputStream = process.getInputStream();
			int len;
			while ((len = inputStream.read()) > 0) {
				outputStream.write(len);
			}
			outputStream.flush();
			byte[] data = outputStream.toByteArray();
			result = new String(data);
		} catch (IOException e) {
			e.printStackTrace();
			MyLog.print(MyLog.ERROR, "cmd", "error");
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (result.contains("up broadcast running multicast")) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * ������ά��λͼ
	 * @param str  ��ά������
	 * @param widthAndHeight ��ά��߳�
	 * @param codeColor ��ά������ɫ
	 * @return
	 * @throws WriterException
	 */
	public static Bitmap createQRCode(String str, int widthAndHeight, int codeColor) throws WriterException {
		Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();  
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        //���ؾ���
		BitMatrix matrix = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, widthAndHeight, widthAndHeight);
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		int[] pixels = new int[width * height];
		//���ؾ���ת��ɫ����
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (matrix.get(x, y)) {
					pixels[y * width + x] = codeColor;//��ά����ɫΪ����������ɫ
				} else {
					pixels[y * width + x] = 0xffffffff;//�׵�,͸����0x00ffffff
				}
			}
		}
		//������ɫ����  ��ɫ--RGB_565  ͸��--ARGB_8888��ARGB_4444
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}
	
	/**
	 * ��ά���м����ͼƬ
	 * @param qrBitmap  ԭʼ��ά��
	 * @param logoBitmap  Ҫ��ӵ�ͼƬ
	 * @return
	 */
	public static Bitmap addLogo(Bitmap qrBitmap, Bitmap logoBitmap) {  
	    int qrBitmapWidth = qrBitmap.getWidth();
	    int qrBitmapHeight = qrBitmap.getHeight();
	    int logoBitmapWidth = logoBitmap.getWidth();
	    int logoBitmapHeight = logoBitmap.getHeight();
	    
	    Bitmap blankBitmap = Bitmap.createBitmap(qrBitmapWidth, qrBitmapHeight, Bitmap.Config.ARGB_8888);
	    Canvas canvas = new Canvas(blankBitmap);
	    canvas.drawBitmap(qrBitmap, 0, 0, null);
	    canvas.save(Canvas.ALL_SAVE_FLAG);
	    
	    float scaleSize = 1.0f;   
	    while((logoBitmapWidth / scaleSize) > (qrBitmapWidth / 5) || (logoBitmapHeight / scaleSize) > (qrBitmapHeight / 5)) {  
	        scaleSize *= 2;  
	    }
	    
	    float sx = 1.0f / scaleSize;
	    canvas.scale(sx, sx, qrBitmapWidth / 2, qrBitmapHeight / 2);
	    canvas.drawBitmap(logoBitmap, (qrBitmapWidth - logoBitmapWidth) / 2, (qrBitmapHeight - logoBitmapHeight) / 2, null);
	    canvas.restore();
	    return blankBitmap;
	}
	
	/**
	 * ���ɴ�LOGO�Ķ�ά��
	 * @param content  ��ά������
	 * @param logoBitmap  ��ά�����ͼƬ
	 * @return
	 * @throws WriterException
	 */
	public static Bitmap createCode(String content, Bitmap logoBitmap) throws WriterException {
		int logoWidth = logoBitmap.getWidth();
		int logoHeight = logoBitmap.getHeight();
		int logoHaleWidth = logoWidth >= CODE_WIDTH ? LOGO_WIDTH_MIN : LOGO_WIDTH_MAX;
		int logoHaleHeight = logoHeight >= CODE_WIDTH ? LOGO_WIDTH_MIN : LOGO_WIDTH_MAX;
		// ��logoͼƬ��martix���õ���Ϣ����
		Matrix m = new Matrix();
		float sx = (float) 2 * logoHaleWidth / logoWidth;
		float sy = (float) 2 * logoHaleHeight / logoHeight;
		m.setScale(sx, sy);// ����������Ϣ
		Bitmap newLogoBitmap = Bitmap.createBitmap(logoBitmap, 0, 0, logoWidth,
				logoHeight, m, false);
		int newLogoWidth = newLogoBitmap.getWidth();
		int newLogoHeight = newLogoBitmap.getHeight();
		Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);//�����ݴ���,HΪ���
		hints.put(EncodeHintType.MAX_SIZE, LOGO_WIDTH_MAX);// ����ͼƬ�����ֵ
		hints.put(EncodeHintType.MIN_SIZE, LOGO_WIDTH_MIN);// ����ͼƬ����Сֵ
		hints.put(EncodeHintType.MARGIN, 2);//���ð�ɫ�߾�ֵ
		// ���ɶ�ά����,����ʱָ����С,��Ҫ������ͼƬ�Ժ��ٽ�������,������ģ������ʶ��ʧ��
		BitMatrix matrix = new MultiFormatWriter().encode(content,
				BarcodeFormat.QR_CODE, CODE_WIDTH, CODE_WIDTH, hints);
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		int halfW = width / 2;
		int halfH = height / 2;
		// ��ά����תΪһά��������,Ҳ����һֱ��������
		int[] pixels = new int[width * height];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				/*
				 * ȡֵ��Χ,���Ի�ͼ�����  
				 * halfW + newLogoWidth / 2 - (halfW - newLogoWidth / 2) = newLogoWidth
				 * halfH + newLogoHeight / 2 - (halfH - newLogoHeight) = newLogoHeight
				 */
				if (x > halfW - newLogoWidth / 2 && x < halfW + newLogoWidth / 2
						&& y > halfH - newLogoHeight / 2 && y < halfH + newLogoHeight / 2) {// ��λ�����ڴ��ͼƬ��Ϣ
					/*
					 *  ��¼ͼƬÿ��������Ϣ
					 *  halfW - newLogoWidth / 2 < x < halfW + newLogoWidth / 2 
					 *  --> 0 < x - halfW + newLogoWidth / 2 < newLogoWidth
					 *   halfH - newLogoHeight / 2  < y < halfH + newLogoHeight / 2
					 *   -->0 < y - halfH + newLogoHeight / 2 < newLogoHeight
					 *   �պ�ȡֵnewLogoBitmap��getPixel(0-newLogoWidth,0-newLogoHeight);
					 */
					pixels[y * width + x] = newLogoBitmap.getPixel(
							x - halfW + newLogoWidth / 2, y - halfH + newLogoHeight / 2);
				} else {
					pixels[y * width + x] = matrix.get(x, y) ? BLACK: WHITE;// ������Ϣ
				}
			}
		}
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		// ͨ��������������bitmap,����ο�api
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}
	
	/**
	 * ������ЧͼƬ
	 * @param str  ͼƬ��ʾ����
	 * @param widthAndHeight  ͼƬ���
	 * @param textSize  ���ִ�С
	 * @return
	 */
	public static Bitmap createDestroyImage(String str, int widthAndHeight, int textSize) {
		Bitmap bitmap = Bitmap.createBitmap(widthAndHeight, widthAndHeight, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setTextSize(textSize);
		
		//����ó����ֵĻ�����ʼX,Y����
		int posX = widthAndHeight / 2 - textSize * str.length() / 2;
		int posY = widthAndHeight / 2 + textSize / 2;
		
		canvas.drawColor(Color.parseColor("#00E5EE"));
		canvas.drawText(str, posX, posY, paint);
		return bitmap;
	}
	
	/**
	 * ��long����ʱ�� ת����String����
	 * @param time - "yyyy-MM-dd HH:mm:ss" "2016-08-31 08:31:01"
	 * @return
	 */
	public static String formatTime(long time) {
		SimpleDateFormat format = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		Date date = new Date(time);
		return format.format(date);
	}
	
	/**
	 * ����ϵͳʱ��
	 * @param time - "yyyy-MM-dd HH:mm:ss" "2016-08-31 08:31:01"
	 * @return
	 */
	public static boolean setSystemTime(String time) {
		try {
			requestPermission();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar calendar = Calendar.getInstance();
		try {
			calendar.setTime(dateFormat.parse(time));
		} catch (ParseException e) {
			e.printStackTrace();
			return false;
		}
		long when = calendar.getTimeInMillis();
		if (when / 1000 < Integer.MAX_VALUE) {
			SystemClock.setCurrentTimeMillis(when);
			MyLog.print(TAG, "setTime success");
			MyLog.print(TAG, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
					Locale.getDefault()).format(calendar.getTime()));
			return true;
		} else {
			MyLog.print(MyLog.ERROR, "setTime error");
			return false;
		}
	}
	
	/**
	 * ����ϵͳʱ��
	 * @param calendar
	 * @return
	 */
	public static boolean setSystemTime(Calendar calendar) {
		try {
			requestPermission();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		long when = calendar.getTimeInMillis();
		if (when / 1000 < Integer.MAX_VALUE) {
			SystemClock.setCurrentTimeMillis(when);
			MyLog.print(TAG, "setTime success");
			MyLog.print(TAG, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
					Locale.getDefault()).format(calendar.getTime()));
			return true;
		} else {
			MyLog.print(MyLog.ERROR, "setTime error");
			return false;
		}
	}
	
	/**
	 * ����ϵͳʱ��
	 * @param hour
	 * @param minute
	 * @return
	 */
	public static boolean setSystemTime(int hour, int minute) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		try {
			requestPermission();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		long when = calendar.getTimeInMillis();
		if (when / 1000 < Integer.MAX_VALUE) {
			SystemClock.setCurrentTimeMillis(when);
			MyLog.print(TAG, "setTime success");
			MyLog.print(TAG, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
					Locale.getDefault()).format(calendar.getTime()));
			return true;
		} else {
			MyLog.print(MyLog.ERROR, "setTime error");
			return false;
		}
	}
	
	/**
	 * ����ϵͳʱ��
	 * @param year
	 * @param month
	 * @param date
	 * @return
	 */
	public static boolean setSystemTime(int year, int month, int date) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month - 1);
		calendar.set(Calendar.DATE, date);
		try {
			requestPermission();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		long when = calendar.getTimeInMillis();
		if (when / 1000 < Integer.MAX_VALUE) {
			SystemClock.setCurrentTimeMillis(when);
			MyLog.print(TAG, "setTime success");
			MyLog.print(TAG, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
					Locale.getDefault()).format(calendar.getTime()));
			return true;
		} else {
			MyLog.print(MyLog.ERROR, "setTime error");
			return false;
		}
	}
	
	/**
	 * ����ϵͳʱ��
	 * @param year
	 * @param month
	 * @param date
	 * @param hour
	 * @param minute
	 * @param second
	 * @return
	 */
	public static boolean setSystemTime(int year, int month, int date, 
			int hour, int minute, int second) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month - 1);
		calendar.set(Calendar.DATE, date);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, second);
		try {
			requestPermission();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		long when = calendar.getTimeInMillis();
		if (when / 1000 < Integer.MAX_VALUE) {
			SystemClock.setCurrentTimeMillis(when);
			MyLog.print(TAG, "setTime success");
			MyLog.print(TAG, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
					Locale.getDefault()).format(calendar.getTime()));
			return true;
		} else {
			MyLog.print(MyLog.ERROR, "setTime error");
			return false;
		}
	}
	
	/**
	 * ��ȡ����ϵͳʱ��Ȩ��
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 */
	private static void requestPermission() throws InterruptedException, IOException {
		createSuProcess("chmod 666 /dev/alarm").waitFor();
	}
	
	private static Process createSuProcess(String cmd) throws IOException {
		DataOutputStream os = null;
		Process process = createSuProcess();
		try {
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(cmd + "\n");
			os.writeBytes("exit $?\n");
			os.flush();
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return process;
	}

	private static Process createSuProcess() throws IOException {
		File rootUser = new File("/system/xbin/ru");
		if (rootUser.exists()) {
			return Runtime.getRuntime().exec(rootUser.getAbsolutePath());
		} else {
			return Runtime.getRuntime().exec("su");
		}
	}
}

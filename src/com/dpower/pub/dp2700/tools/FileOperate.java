package com.dpower.pub.dp2700.tools;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.dpower.util.MyLog;

import android.content.Context;

/**
 * �ļ�����
 */
public class FileOperate {
	private static final String TAG = "FileOperate";
	
	private File mFromFile;
	private File mToFile;

	public FileOperate() {
		super();
	}

	public FileOperate from(String path) {
		mFromFile = new File(path);
		return this;
	}

	public boolean to(String filePath) {
		boolean result = false;
		if (mFromFile == null || !mFromFile.exists()) {
			if (mFromFile != null) {
				MyLog.print(MyLog.ERROR, TAG, "�ļ�����ʧ�ܣ�mFromFile������ " 
						+ mFromFile.getAbsolutePath());
			} else {
				MyLog.print(MyLog.ERROR, TAG, "�ļ�����ʧ�ܣ�mFromFile null ");
			}
			return result;
		}
		FileInputStream inputStream = null;
		FileOutputStream outputStream = null;
		try {
			mToFile = new File(filePath);
			inputStream = new FileInputStream(mFromFile);
			outputStream = new FileOutputStream(mToFile);
			byte[] buffer = new byte[1024];
			int len;
			while ((len = inputStream.read(buffer)) > 0) {
				outputStream.write(buffer, 0, len);
			}
			outputStream.flush();
			MyLog.print(TAG, "�ļ������ɹ���" + filePath);
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			MyLog.print(MyLog.ERROR, TAG, "�ļ�����ʧ�ܣ�" + filePath);
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
	
	/**
	 * ����assent���ļ���SD��
	 * @param context
	 * @param filePath ������Ŀ�ĵ�
	 * @param name assent�������Դ����
	 */
	public static boolean copyToSD(Context context, String filePath, String name) {
		boolean result = false;
		InputStream inputStream = null;
		FileOutputStream outputStream = null;
		try {
			inputStream = context.getAssets().open(name);
			outputStream = new FileOutputStream(filePath);
			byte[] buffer = new byte[1024];
			int len;
			while ((len = inputStream.read(buffer)) > 0) {
				outputStream.write(buffer, 0, len);
			}
			outputStream.flush();
			outputStream.getFD().sync();
			MyLog.print(TAG, "�ļ������ɹ���" + name + "/" + filePath);
			result = true;
		} catch (IOException e) {
			e.printStackTrace();
			MyLog.print(MyLog.ERROR, TAG, "�ļ�����ʧ�ܣ�" + name + "/" + filePath);
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

	public boolean toRomDir(String filePath) {
		boolean result = false;
		if (mFromFile == null || !mFromFile.exists()) {
			if (mFromFile != null) {
				MyLog.print(MyLog.ERROR, TAG, "�ļ�����ʧ�ܣ�mFromFile������ " 
						+ mFromFile.getAbsolutePath());
			} else {
				MyLog.print(MyLog.ERROR, TAG, "�ļ�����ʧ�ܣ�mFromFile null ");
			}
			return result;
		}
		String command = "cp " + mFromFile + " " + filePath;
		Process process = null;
		DataOutputStream os = null;
		try {
			process = Runtime.getRuntime().exec("su");
			// the phone must be root,it can exctue the adb command
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(command + "\n");
			os.writeBytes("exit\n");
			os.flush();
			MyLog.print(TAG, "�ļ�����romĿ¼�ɹ���" + filePath);
			result = true;
			changePermission(filePath);
		} catch (Exception e) {
			e.printStackTrace();
			MyLog.print(MyLog.ERROR, TAG, "�ļ�����romĿ¼ʧ�ܣ�" + filePath);
		} finally {
			try {
				if (process != null) {
					process.destroy();
				}
				if (os != null) {
					os.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public static boolean changePermission(String filePath) {
		boolean result = false;
		String command = "chmod 644 " + filePath;
		Process process = null;
		DataOutputStream os = null;
		try {
			// the phone must be root,it can exctue the adb command
			process = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(command + "\n");
			os.writeBytes("exit\n");
			os.flush();
			MyLog.print(TAG, "�޸Ķ�дȨ�޳ɹ���" + filePath);
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			MyLog.print(MyLog.ERROR, TAG, "�޸Ķ�дȨ��ʧ�ܣ�" + filePath);
		} finally {
			try {
				if (process != null) {
					process.destroy();
				}
				if (os != null) {
					os.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * �ݹ�ɾ���ļ����ļ���
	 * @param file
	 *            Ҫɾ���ĸ�Ŀ¼
	 */
	public static void recursionDeleteFile(File file) {
		if (file.exists()) {
			if (file.isFile()) {
				file.delete();
				return;
			}
			if (file.isDirectory()) {
				File[] childFile = file.listFiles();
				if (childFile == null || childFile.length == 0) {
					file.delete();
					return;
				}
				for (File f : childFile) {
					recursionDeleteFile(f);
				}
				file.delete();
			}
		}
	}
}

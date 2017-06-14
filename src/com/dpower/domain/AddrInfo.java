package com.dpower.domain;

import java.util.ArrayList;

import com.dpower.util.MyLog;

/**
 * @������AddrInfo
 * @�������������ñ���CODE��Ӧ��IP��Ϣ��
 * @��˽�г�Ա��mIp-IP��ַ,mMask-��������,mGW-����,mCode-����,mName-��������
 */
public class AddrInfo {
	
	private String mIp;
	private String mMask;
	private String mGW;
	private String mCode;
	private String mName;

	public String getIp() {
		return mIp;
	}

	public void setIp(String ip) {
		mIp = ip;
	}
	
	public String getCode() {
		return mCode;
	}

	public void setCode(String code) {
		mCode = code;
	}

	public String getMask() {
		return mMask;
	}

	public void setMask(String mask) {
		mMask = mask;
	}

	public String getGw() {
		return mGW;
	}

	public void setGw(String gw) {
		mGW = gw;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public String toString() {
		return " AddrInfo code:" + mCode + ", ip:" + mIp + ", mask:" + mMask
				+ ", gw:" + mGW + ", name:" + mName;
	}

	/**
	 * @��̬��������parsingAddrInfo
	 * @����������XML��ʽ�ַ���ת����AddrInfo�����б�
	 * @������String info - XML��ʽ�ַ���
	 * @����ֵ������Ϊnull�򷵻�null,���򷵻�AddrInfo�����б�
	 */
	public static ArrayList<AddrInfo> parsingAddrInfo(String info) {
		if (info == null) {
			MyLog.print("parsingAddrInfo - > Parameter is not correct");
			return null;
		}
		ArrayList<AddrInfo> list = new ArrayList<AddrInfo>();
		while (true) {
			int start = info.indexOf("<info>");
			int end = info.indexOf("</info>");
			if (start == -1 || end == -1) {
				break;
			}
			String infoitem = new String(info.substring(start + 6, end));
			AddrInfo addrinfo = new AddrInfo();
			String val = getXmlVal("ip", infoitem);
			if (val != null) {
				addrinfo.setIp(val);
			}
			val = getXmlVal("mask", infoitem);
			if (val != null) {
				addrinfo.setMask(val);
			}
			val = getXmlVal("gw", infoitem);
			if (val != null) {
				addrinfo.setGw(val);
			}
			val = getXmlVal("code", infoitem);
			if (val != null) {
				addrinfo.setCode(val);
			}
			val = getXmlVal("name", infoitem);
			if (val != null) {
				addrinfo.setName(val);
			}
			list.add(addrinfo);
			info = new String(info.substring(end + 7));
		}
		return list;
	}
	
	/**
	 * @��̬��������getXmlVal
	 * @���ܣ�����Դ�ַ����е����ַ������������ַ�����XML������ֵ��
	 * @����1��String key - ���ַ���
	 * @����2��String src - Դ�ַ���
	 * @����ֵ��String - key�����ڷ���null,���򷵻�key�������ַ���
	 */
	private static String getXmlVal(String key, String src) {
		String strStart = "<" + key + ">";
		String strEnd = "</" + key + ">";
		int start = src.indexOf(strStart);
		if (start == -1) {
			MyLog.print("getXmlVal -> start key does not exist");
			return null;
		}
		int end = src.indexOf(strEnd);
		if (end == -1) {
			MyLog.print("getXmlVal -> end key does not exist");
			return null;
		}
		String strval = new String(
				src.substring(start + strStart.length(), end));
		return strval;
	}
}

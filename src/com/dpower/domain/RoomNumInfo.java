package com.dpower.domain;

import java.util.ArrayList;

import com.dpower.util.MyLog;

public class RoomNumInfo {

	private String mArea;
	private String mBuild;
	private String mUnilt;
	private String mRoom;
	private String mExtension;

	/**
	 * ��ȡ����
	 * @return
	 */
	public String getArea() {
		return mArea;
	}

	/**
	 * ��������
	 * @param area
	 *            ����2λ��������λ����0����
	 * @return
	 */
	public boolean setArea(String area) {
		if (area.length() == 2) {
			mArea = area;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * ��ȡ����
	 * @return
	 */
	public String getBuilding() {
		return mBuild;
	}

	/**
	 * ���ö���
	 * @param building
	 *            ����2λ��������λ����0����
	 * @return
	 */
	public boolean setBuilding(String building) {
		if (building.length() == 2) {
			mBuild = building;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * ��ȡ��Ԫ��
	 * @return
	 */
	public String getUnit() {
		return mUnilt;
	}

	/**
	 * ���õ�Ԫ��
	 * @param unit
	 *            ����2λ��������λ����0����
	 * @return
	 */
	public boolean setUnit(String unit) {
		if (unit.length() == 2) {
			mUnilt = unit;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * ��ȡ����
	 * @return
	 */
	public String getRoom() {
		return mRoom;
	}

	/**
	 * ���÷���
	 * @param room
	 *            ����4λ��������λ����0����
	 * @return
	 */
	public boolean setRoom(String room) {
		if (room.length() == 4) {
			mRoom = room;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * ��ȡ�ֻ���
	 * @return
	 */
	public String getExtension() {
		return mExtension;
	}

	/**
	 * ���÷ֻ���
	 * @param extension
	 *            ����2λ����������0����
	 * @return
	 */
	public boolean setExtension(String extension) {
		if (extension.length() == 2) {
			mExtension = extension;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * ��ȡ12λ��������
	 * @return
	 */
	public String getString() {
		return mArea + mBuild + mUnilt + mRoom
				+ mExtension;
	}

	/**
	 * @��̬��������parsingRoomNumInfo
	 * @����������XML��ʽ�ַ���ת����RoomNumInfo�����б�
	 * @param String
	 *            infoXml - XML��ʽ�ַ���
	 * @return ����Ϊnull�򷵻�null,���򷵻�RoomNumInfo�����б�
	 */
	public static ArrayList<RoomNumInfo> parsingRoomNumInfo(String infoXml) {
		if (infoXml == null) {
			MyLog.print("parsingRoomNumInfo - > Parameter is not correct");
			return null;
		}
		ArrayList<RoomNumInfo> list = new ArrayList<RoomNumInfo>();
		while (true) {
			int start = infoXml.indexOf("<code>");
			int end = infoXml.indexOf("</code>");
			if (start == -1 || end == -1) {
				break;
			}
			String infoItem = new String(infoXml.substring(start + 6, end));
			if (infoItem != null) {
				if (infoItem.substring(0, 1).equals("1")) {
					RoomNumInfo addrinfo = new RoomNumInfo();
					addrinfo.setArea(infoItem.substring(1, 3));
					addrinfo.setBuilding(infoItem.substring(3, 5));
					addrinfo.setUnit(infoItem.substring(5, 7));
					addrinfo.setRoom(infoItem.substring(7, 11));
					addrinfo.setExtension(infoItem.substring(11, 13));
					list.add(addrinfo);
				}
			}
			infoXml = new String(infoXml.substring(end + 7));
		}
		return list;
	}
}

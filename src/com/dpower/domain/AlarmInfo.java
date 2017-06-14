package com.dpower.domain;

/**
 * @������AlarmInfo
 * @����������ģʽ����Ϣ��
 * @��˽�г�Ա��areaName-��������,areaType-��������,enable-�Ƿ�����,open-����(true) or ����(false),delayTime-��ʱʱ��
 */
public class AlarmInfo {

	public AlarmInfo(String areaName, String areaType,
			boolean enable, boolean open, int time) {
		this.areaName = areaName;
		this.areaType = areaType;
		this.enable = enable;
		this.open = open;
		this.delayTime = time;
	}

	/** �������� */
	public String areaName;
	
	/** �������� */
	public String areaType;
	
	/** �Ƿ����� */
	public boolean enable;
	
	/** ����(true) or ����(false) */
	public boolean open;
	
	/** ��ʱʱ�� */
	public int delayTime;

	@Override
	public String toString() {
		return "areaName:" + areaName + ",areaType:" + areaType
				+ ",enable:" + enable + ", open:" + open + ",delayTime:" + delayTime;
	}
}

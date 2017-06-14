package com.dpower.domain;

public class AlarmTypeInfo {
	
	public String name;
	/** 0-���塢1-������2-�̸�ú����3-�Ŵź��� */
	public int priority;
	public int value;

	public AlarmTypeInfo(String name, int priority, int value) {
		this.name = name;
		this.priority = priority;
		this.value = value;
	}

	@Override
	public String toString() {
		return "name:" + name + ",priority:" + priority + ",value:" + value;
	}
}

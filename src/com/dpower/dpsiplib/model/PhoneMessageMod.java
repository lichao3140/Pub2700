package com.dpower.dpsiplib.model;

public class PhoneMessageMod {

	/**��������*/
	private String type;
	/**��Ϣ����*/
	private String msg;
	/**״̬*/
	private String status;
	
	public PhoneMessageMod(String type, String msg, String status) {
		super();
		this.type = type;
		this.msg = msg;
		this.status = status;
	}
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getMsg() {
		return msg;
	}
	
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	@Override
	public String toString() {
		return "TextMsg [type=" + type + ", msg=" + msg + ", status=" + status
				+ "]";
	}

}

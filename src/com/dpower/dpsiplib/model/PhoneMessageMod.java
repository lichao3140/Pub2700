package com.dpower.dpsiplib.model;

public class PhoneMessageMod {

	/**具体类型*/
	private String type;
	/**消息内容*/
	private String msg;
	/**状态*/
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

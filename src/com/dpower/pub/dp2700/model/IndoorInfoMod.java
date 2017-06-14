package com.dpower.pub.dp2700.model;

public class IndoorInfoMod extends BaseMod {

	private static final long serialVersionUID = 1L;

	private int code;

	private String message;

	private IndoorSipInfo data;

	public void setCode(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setData(IndoorSipInfo data) {
		this.data = data;
	}

	public IndoorSipInfo getData() {
		return data;
	}

}

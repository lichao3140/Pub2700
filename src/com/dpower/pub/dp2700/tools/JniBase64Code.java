package com.dpower.pub.dp2700.tools;

public class JniBase64Code {
	/**  ����   */
	public native byte[] enBase(byte[] buf);
	/**  ����   */
    public native byte[] deBase(byte[] buf);
}

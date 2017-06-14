package com.dpower.util;

/**
 * 工程配置
 */
public class ProjectConfigure {

	/** 公版 = 0, 龙侨华 = 1, 克耐克 = 2 */
	public static int project = 0;
	
	/** 不区分尺寸 = 0, 7寸 = 7, 10寸 = 10 */
	public static int size = 10;
	
	/** 调试 = true, 非调试 = false*/
	public static boolean isDebug = true;
	
	/** 需要云对讲 = true, 不需要 = false*/
	public static boolean needCloudIntercom = true;
	
	/** 需要智能家居 = true, 不需要 = false*/
	public static boolean needSmartHome = true;
	
	/** 有网络摄像头 = true, 没有 = false*/
	public static boolean webCamera = true;
	
	/** true = 启动第三方 */
	public static boolean smartHomeApk = false;

	/** true = 使用第三方服务器 */
	public static boolean smartHomeServer = false;
}
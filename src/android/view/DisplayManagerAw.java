/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.view;

import android.os.Binder;
import android.os.IBinder;

/**
 * The interface that apps use to talk to the display manager.
 * <p>
 * Use <code>Context.getSystemService(Context.WINDOW_SERVICE)</code> to get one
 * of these.
 * 
 * @see android.content.Context#getSystemService
 * @see android.content.Context#DISPLAY_SERVICE
 */
public class DisplayManagerAw {
	public static final String TAG = "DisplayManagerAw";
	/**
	 * Use this method to get the default Display object.
	 * 
	 * @return default Display object
	 */
	public static final String EXTRA_HDMISTATUS = "hdmistatus";

	/**
	 * Extra for {@link android.content.Intent#ACTION_TVDACSTATUS_CHANGED}:
	 * integer containing the current health constant.
	 */
	public static final String EXTRA_TVSTATUS = "tvdacstatus";

	public static final int DISPLAY_DEVICE_ON = 0;
	public static final int DISPLAY_DEVICE_OFF = 1;

	public static final int DISPLAY_DEVICE_PLUGIN = 0;
	public static final int DISPLAY_DEVICE_PLUGOUT = 1;

	public static final int DISPLAY_OUTPUT_TYPE_NONE = 0;
	public static final int DISPLAY_OUTPUT_TYPE_LCD = 1;
	public static final int DISPLAY_OUTPUT_TYPE_TV = 2;
	public static final int DISPLAY_OUTPUT_TYPE_HDMI = 3;
	public static final int DISPLAY_OUTPUT_TYPE_VGA = 4;

	public static final int DISPLAY_MODE_SINGLE = 0;
	public static final int DISPLAY_MODE_DUALLCD = 1;
	public static final int DISPLAY_MODE_DUALDIFF = 2;
	public static final int DISPLAY_MODE_DUALSAME = 3;
	public static final int DISPLAY_MODE_DUALSAME_TWO_VIDEO = 4;
	public static final int DISPLAY_MODE_SINGLE_VAR = 5;
	public static final int DISPLAY_MODE_SINGLE_VAR_BE = 6;
	public static final int DISPLAY_MODE_SINGLE_FB_VAR = 7;
	public static final int DISPLAY_MODE_SINGLE_FB_GPU = 8;

	public static final int DISPLAY_TVDAC_NONE = 0;
	public static final int DISPLAY_TVDAC_YPBPR = 1;
	public static final int DISPLAY_TVDAC_CVBS = 2;
	public static final int DISPLAY_TVDAC_SVIDEO = 3;
	public static final int DISPLAY_TVDAC_ALL = 4;

	public static final int DISPLAY_TVFORMAT_480I = 0;
	public static final int DISPLAY_TVFORMAT_576I = 1;
	public static final int DISPLAY_TVFORMAT_480P = 2;
	public static final int DISPLAY_TVFORMAT_576P = 3;
	public static final int DISPLAY_TVFORMAT_720P_50HZ = 4;
	public static final int DISPLAY_TVFORMAT_720P_60HZ = 5;
	public static final int DISPLAY_TVFORMAT_1080I_50HZ = 6;
	public static final int DISPLAY_TVFORMAT_1080I_60HZ = 7;
	public static final int DISPLAY_TVFORMAT_1080P_24HZ = 8;
	public static final int DISPLAY_TVFORMAT_1080P_50HZ = 9;
	public static final int DISPLAY_TVFORMAT_1080P_60HZ = 0xa;
	public static final int DISPLAY_TVFORMAT_PAL = 0xb;
	public static final int DISPLAY_TVFORMAT_PAL_SVIDEO = 0xc;
	public static final int DISPLAY_TVFORMAT_PAL_CVBS_SVIDEO = 0xd;
	public static final int DISPLAY_TVFORMAT_NTSC = 0xe;
	public static final int DISPLAY_TVFORMAT_NTSC_SVIDEO = 0xf;
	public static final int DISPLAY_TVFORMAT_NTSC_CVBS_SVIDEO = 0x10;
	public static final int DISPLAY_TVFORMAT_PAL_M = 0x11;
	public static final int DISPLAY_TVFORMAT_PAL_M_SVIDEO = 0x12;
	public static final int DISPLAY_TVFORMAT_PAL_M_CVBS_SVIDEO = 0x13;
	public static final int DISPLAY_TVFORMAT_PAL_NC = 0x14;
	public static final int DISPLAY_TVFORMAT_PAL_NC_SVIDEO = 0x15;
	public static final int DISPLAY_TVFORMAT_PAL_NC_CVBS_SVIDEO = 0x16;

	public static final int DISPLAY_VGA_H1680_V1050 = 0x17;
	public static final int DISPLAY_VGA_H1440_V900 = 0x18;
	public static final int DISPLAY_VGA_H1360_V768 = 0x19;
	public static final int DISPLAY_VGA_H1280_V1024 = 0x1a;
	public static final int DISPLAY_VGA_H1024_V768 = 0x1b;
	public static final int DISPLAY_VGA_H800_V600 = 0x1c;
	public static final int DISPLAY_VGA_H640_V480 = 0x1d;
	public static final int DISPLAY_VGA_H1440_V900_RB = 0x1e;
	public static final int DISPLAY_VGA_H1680_V1050_RB = 0x1f;
	public static final int DISPLAY_VGA_H1920_V1080_RB = 0x20;
	public static final int DISPLAY_VGA_H1920_V1080 = 0x21;
	public static final int DISPLAY_VGA_H1280_V720 = 0x22;

	public static final int DISPLAY_TVFORMAT_1080P_25HZ = 0x23;
	public static final int DISPLAY_TVFORMAT_1080P_30HZ = 0x24;
	public static final int DISPLAY_TVFORMAT_1080P_24HZ_3D_FP = 0x25;
	public static final int DISPLAY_TVFORMAT_720P_50HZ_3D_FP = 0x26;
	public static final int DISPLAY_TVFORMAT_720P_60HZ_3D_FP = 0x27;

	public IBinder mToken = new Binder();

	public DisplayManagerAw() {

	}

	public int getDisplayCount()

	{

		return 0;
	}

	/**
	 * 亮度
	 * 
	 * @param displayno
	 * @param bright
	 * @return
	 */
	public int setDisplayBright(int displayno, int bright) {

		return 0;
	}

	public int getDisplayBright(int displayno) {

		return 0;
	}

	/**
	 * 对比度
	 * 
	 * @param displayno
	 * @param contrast
	 * @return
	 */
	public int setDisplayContrast(int displayno, int contrast) {
		return 0;
	}

	public int getDisplayContrast(int displayno) {
		return 0;
	}

	/**
	 * 色相
	 * 
	 * @param displayno
	 * @param hue
	 * @return
	 */
	public int setDisplayHue(int displayno, int hue) {
		return 0;
	}

	public int getDisplayHue(int displayno) {
		return 0;
	}

	/**
	 * 饱和度
	 * 
	 * @param displayno
	 * @param saturation
	 * @return
	 */
	public int setDisplaySaturation(int displayno, int saturation) {

		return 0;
	}

	public int getDisplaySaturation(int displayno) {

		return 0;
	}
}

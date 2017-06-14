package com.dpower.pub.dp2700.tools;

import com.dpower.pub.dp2700.R;

import android.content.Context;

/**
 * @author ZhengZhiying
 * @Funtion ������Ϣ
 */
public class RoomInfoUT {

	public final static int ADDR_UNIT_GATE = 0x11;
	public final static int ADDR_LITTLE_DOOR = 0x12;
	public final static int ADDR_ROOM = 0x13;
	public final static int ADDR_MANAGER_CENTER = 0x14;
	public final static int ADDR_SECURITY_EXTENSION = 0x15;
	public final static int ADDR_MAIN_GREAT_GATE = 0x16;
	private int mType;
	private String mArea;
	private String mBuild;
	private String mUnit;
	private String mRoom;
	private String mExt;

	public RoomInfoUT(String roomCode) {
		if(roomCode == null){ //add by arlene
			mType = ADDR_MANAGER_CENTER;
			return;
		}
		mType = Integer.valueOf(roomCode.substring(0, 1));
		switch (mType) {
		case 1:
			// ���ڻ�
			mType = ADDR_ROOM;
			break;
		case 2:
			// ��Ԫ�ſڻ�
			mType = ADDR_UNIT_GATE;
			break;
		case 3:
			// �����ſڻ�
			mType = ADDR_LITTLE_DOOR;
			break;
		case 6:
			// �����ֻ�
			mType = ADDR_SECURITY_EXTENSION;
			break;
		case 7:
			// ���ſڻ�
			mType = ADDR_MAIN_GREAT_GATE;
			break;
		case 8:
			// ��������
			mType = ADDR_MANAGER_CENTER;
			break;
		default:
			break;
		}
		mArea = roomCode.substring(1, 3);
		mBuild = roomCode.substring(3, 5);
		mUnit = roomCode.substring(5, 7);
		mRoom = roomCode.substring(7, 11);
		mExt = roomCode.substring(11, 13);
	}

	public int getType() {
		return mType;
	}

	public String getArea() {
		return mArea;
	}

	public String getBuild() {
		return mBuild;
	}

	public String getUnit() {
		return mUnit;
	}

	public String getRoom() {
		return mRoom;
	}

	public String getExt() {
		return mExt;
	}

	/**
	 * @param context
	 * @return ���ط������ƣ�<br>
	 *         01��01��01��Ԫ0101��01�ֻ�<br>
	 *         01���ſڻ�<br>
	 *         01�Ź�������<br>
	 *         01�ű����ֻ�<br>
	 *         01�Ŵ��ſڻ�<br>
	 *         01��С�ſڻ�<br>
	 */
	public String getRoomName(Context context) {
		StringBuffer result = new StringBuffer();
		switch (getType()) {
			case ADDR_MANAGER_CENTER:
				result.append(getExt());
				result.append(context.getString(R.string.number_hao));
				result.append(context.getString(R.string.manger_center));
				break;
			case ADDR_SECURITY_EXTENSION:
				result.append(getExt());
				result.append(context.getString(R.string.number_hao));
				result.append(context.getString(R.string.security_extension));
				break;
			case ADDR_UNIT_GATE:
				result.append(getExt());
				result.append(context.getString(R.string.num_unit_gate_phone));
				break;
			case ADDR_ROOM:
				result.append(getArea() + context.getString(R.string.text_area));
				result.append(getBuild() + context.getString(R.string.text_building));
				result.append(getUnit() + context.getString(R.string.text_unit));
				result.append(getRoom() + context.getString(R.string.text_room));
				result.append(getExt());
				result.append(context.getString(R.string.num_extension));
				break;
			case ADDR_MAIN_GREAT_GATE:
				result.append(getExt());
				result.append(context.getString(R.string.number_hao));
				result.append(context.getString(R.string.main_gate));
				break;
			case ADDR_LITTLE_DOOR:
				result.append(getExt());
				result.append(context.getString(R.string.number_hao));
				result.append(context.getString(R.string.villa_gate));
				break;
			default:
				break;
		}
		return result.toString();
	}
}

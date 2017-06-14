package com.dpower.pub.dp2700.adapter;

import java.util.List;

import com.dpower.domain.AlarmInfo;
import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.tools.MyToast;
import com.dpower.util.ConstConf;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * @author ZhengZhiying
 * @Funtion ƒ£ Ω…Ë÷√  ≈‰∆˜
 */
public class ModeSetAdapter extends BaseAdapter {

	private List<AlarmInfo> mAlarmInfos;
	private int mMode = ConstConf.HOME_MODE;
	private long mAtHomeSwitch = 0;
	private long mInNightSwitch = 0;
	private long mLeaveHomeSwitch = 0;
	private int[] mAtHomeDelay = { 0, 0, 0, 0, 0, 0, 0, 0 };
	private int[] mInNightDelay = { 0, 0, 0, 0, 0, 0, 0, 0 };
	private int[] mLeaveHomeDelay = { 0, 0, 0, 0, 0, 0, 0, 0 };
	private Context mContext;

	public ModeSetAdapter(Context context) {
		super();
		mContext = context;
		mAtHomeSwitch = DPFunction.getSafeModeEnable(ConstConf.HOME_MODE);
		mInNightSwitch = DPFunction.getSafeModeEnable(ConstConf.NIGHT_MODE);
		mLeaveHomeSwitch = DPFunction.getSafeModeEnable(ConstConf.LEAVE_HOME_MODE);
		mAtHomeDelay = DPFunction.getAlarmDelayTime(ConstConf.HOME_MODE);
		mInNightDelay = DPFunction.getAlarmDelayTime(ConstConf.NIGHT_MODE);
		mLeaveHomeDelay = DPFunction.getAlarmDelayTime(ConstConf.LEAVE_HOME_MODE);
	}

	@Override
	public int getCount() {
		return mAlarmInfos.size();
	}

	@Override
	public Object getItem(int position) {
		return mAlarmInfos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void setAlarmInfos(List<AlarmInfo> alarmInfos) {
		mAlarmInfos = alarmInfos;
	}
	
	public List<AlarmInfo> getAlarmInfos() {
		return mAlarmInfos;
	}
	
	public void setMode(int mode) {
		mMode = mode;
	}
	
	public int getMode() {
		return mMode;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.adapter_mode_set_list, parent, false);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.name.setText(mAlarmInfos.get(position).areaName);
		holder.type.setText(mAlarmInfos.get(position).areaType);
		holder.delay.setText(getAlarmDelayTime(position));
		holder.buttonSwitch.setText(getSafeModeEnable(position) 
				? mContext.getString(R.string.text_open)
				: mContext.getString(R.string.text_close));
		holder.buttonLeft.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				updateDelay(position, true);
			}
		});
		holder.buttonRight.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				updateDelay(position, false);
			}
		});
		holder.buttonSwitch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				updateSwitch(position);
			}
		});
		return convertView;
	}

	class ViewHolder {
		
		ImageButton buttonLeft;
		ImageButton buttonRight;
		TextView name;
		TextView type;
		TextView delay;
		Button buttonSwitch;

		public ViewHolder(View v) {
			buttonLeft = (ImageButton) v.findViewById(R.id.btn_left);
			buttonRight = (ImageButton) v.findViewById(R.id.btn_right);
			name = (TextView) v.findViewById(R.id.tv_name);
			type = (TextView) v.findViewById(R.id.tv_type);
			delay = (TextView) v.findViewById(R.id.tv_delay_time);
			buttonSwitch = (Button) v.findViewById(R.id.btn_switch);
		}
	}
	
	private String getAlarmDelayTime(int position) {
		String delayTime = "";
		switch (mMode) {
			case ConstConf.HOME_MODE:
				delayTime = "" + mAtHomeDelay[position];
				break;
			case ConstConf.LEAVE_HOME_MODE:
				delayTime = "" + mLeaveHomeDelay[position];
				break;
			case ConstConf.NIGHT_MODE:
				delayTime = "" + mInNightDelay[position];
				break;
		}
		return delayTime;
	}
	
	private boolean getSafeModeEnable(int position) {
		boolean enable = false;
		long i = 1 << position;
		switch (mMode) {
			case ConstConf.HOME_MODE:
				i &= mAtHomeSwitch;
				if (i > 0) {
					enable = true;
				} else {
					enable = false;
				}
				break;
			case ConstConf.LEAVE_HOME_MODE:
				i &= mLeaveHomeSwitch;
				if (i > 0) {
					enable = true;
				} else {
					enable = false;
				}
				break;
			case ConstConf.NIGHT_MODE:
				i &= mInNightSwitch;
				if (i > 0) {
					enable = true;
				} else {
					enable = false;
				}
				break;
		}
		return enable;
	}

	private void updateDelay(int position, boolean isLeft) {
		if (!DPFunction.isEmergency(position)) {
			int time = 0;
			switch (mMode) {
				case ConstConf.HOME_MODE:
					time = mAtHomeDelay[position];
					break;
				case ConstConf.LEAVE_HOME_MODE:
					time = mLeaveHomeDelay[position];
					break;
				case ConstConf.NIGHT_MODE:
					time = mInNightDelay[position];
					break;
				default:
					break;
			}
			if (isLeft && time > 0) {
				time--;
			} else if (!isLeft && time < 300) {
				time++;
			} else {
				MyToast.show(R.string.mode_time_outside);
			}
			switch (mMode) {
				case ConstConf.HOME_MODE:
					mAtHomeDelay[position] = time;
					break;
				case ConstConf.LEAVE_HOME_MODE:
					mLeaveHomeDelay[position] = time;
					break;
				case ConstConf.NIGHT_MODE:
					mInNightDelay[position] = time;
					break;
				default:
					break;
			}
			notifyDataSetChanged();
		} else {
			MyToast.show(R.string.tips_mode_not_delay);
		}
	}
	
	private void updateSwitch(int position) {
		if (!DPFunction.isEmergency(position)) {
			boolean enable = getSafeModeEnable(position);
			enable = !enable;
			long temp = 1 << position;
			switch (mMode) {
				case ConstConf.HOME_MODE:
					if (enable) {
						mAtHomeSwitch |= temp;
					} else {
						mAtHomeSwitch &= (~temp);
					}
					break;
				case ConstConf.LEAVE_HOME_MODE:
					if (enable) {
						mLeaveHomeSwitch |= temp;
					} else {
						mLeaveHomeSwitch &= (~temp);
					}
					break;
				case ConstConf.NIGHT_MODE:
					if (enable) {
						mInNightSwitch |= temp;
					} else {
						mInNightSwitch &= (~temp);
					}
					break;
				default:
					break;
			}
			notifyDataSetChanged();
		} else {
			MyToast.show(R.string.tips_mode_not_close);
		}
	}
	
	public void save() {
		DPFunction.changeSafeEnable(ConstConf.HOME_MODE, mAtHomeSwitch);
		DPFunction.changeSafeEnable(ConstConf.NIGHT_MODE, mInNightSwitch);
		DPFunction.changeSafeEnable(ConstConf.LEAVE_HOME_MODE, mLeaveHomeSwitch);
		DPFunction.changeAlarmDelayTime(ConstConf.HOME_MODE, mAtHomeDelay);
		DPFunction.changeAlarmDelayTime(ConstConf.NIGHT_MODE, mInNightDelay);
		DPFunction.changeAlarmDelayTime(ConstConf.LEAVE_HOME_MODE, mLeaveHomeDelay);
	}
}

package com.dpower.pub.dp2700.activity.dialog;

import java.util.List;

import com.dpower.domain.AlarmNameInfo;
import com.dpower.domain.AlarmTypeInfo;
import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.activity.BaseActivity;
import com.dpower.pub.dp2700.view.wheel.ArrayWheelAdapter;
import com.dpower.pub.dp2700.view.wheel.NumericWheelAdapter;
import com.dpower.pub.dp2700.view.wheel.OnWheelChangedListener;
import com.dpower.pub.dp2700.view.wheel.WheelView;
import com.dpower.util.MyLog;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;

/**
 * ·ÀÇøÉèÖÃ´°¿Ú
 */
public class AlarmAreaDialog extends BaseActivity implements OnClickListener {
	private static final String TAG = "AlarmAreaDialog";
	
	private LinearLayout mInfoWindow;
	private WheelView mProbeID;
	private WheelView mAlarmArea;
	private WheelView mAlarmType;
	private WheelView mConnection;
	private int mArea[] = DPFunction.getAlarmArea();
	private int mType[] = DPFunction.getAlarmType();
	private long mSefeConnection = DPFunction.getSefeConnection();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm_area_set);
		init();
	}

	private void init() {
		mProbeID = (WheelView) findViewById(R.id.probe_id);
		mProbeID.setAdapter(new NumericWheelAdapter(1, DPFunction.getTanTouNum()));
		mProbeID.setCyclic(true);
		mProbeID.setVisibleItems(5);
		mProbeID.addChangingListener(new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				setCurrentItem(newValue);
			}
		});
        
        mAlarmArea = (WheelView) findViewById(R.id.alarm_area);
        List<AlarmNameInfo> nameList = DPFunction.getAlarmAreaNameList();
		String[] names = new String[nameList.size()];
		for (int i = 0; i < nameList.size(); i++) {
			names[i] = nameList.get(i).name;
		}
		mAlarmArea.setAdapter(new ArrayWheelAdapter<String>(names));
		mAlarmArea.setCyclic(true);
		mAlarmArea.setVisibleItems(5);
		
		mAlarmType = (WheelView) findViewById(R.id.alarm_type);
		List<AlarmTypeInfo> typeInfoList = DPFunction.getAlarmTypeNameList();
		String[] types = new String[typeInfoList.size()];
		for (int i = 0; i < typeInfoList.size(); i++) {
			types[i] = typeInfoList.get(i).name;
		}
		mAlarmType.setAdapter(new ArrayWheelAdapter<String>(types));
		mAlarmType.setCyclic(true);
		mAlarmType.setVisibleItems(5);
		mConnection = (WheelView) findViewById(R.id.connection);
		String[] connections = { getString(R.string.level_low), getString(R.string.level_high) };
		mConnection.setAdapter(new ArrayWheelAdapter<String>(connections));
		mConnection.setVisibleItems(5);
		
		findViewById(R.id.screen_window).setOnClickListener(this);
		mInfoWindow = (LinearLayout) findViewById(R.id.info_window);
		mInfoWindow.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				MyLog.print(TAG, "mInfoWindow onTouch");
				return true;
			}
		});
		findViewById(R.id.btn_confirm).setOnClickListener(this);
		findViewById(R.id.btn_cancel).setOnClickListener(this);
		setCurrentItem(getIntent().getIntExtra("probe", 0));
	}

	private void setCurrentItem(int i) {
		mProbeID.setCurrentItem(i);
		mAlarmArea.setCurrentItem(mArea[i]);
		mAlarmType.setCurrentItem(mType[i]);
		mConnection.setCurrentItem((mSefeConnection & (1 << i)) > 0 ? 1 : 0);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_confirm:
	            Intent intent = new Intent();
	            Bundle bundle = new Bundle();
	            bundle.putInt("probe", mProbeID.getCurrentItem());
	            bundle.putInt("alarmArea", mAlarmArea.getCurrentItem());
	            bundle.putInt("alarmType", mAlarmType.getCurrentItem());
	            bundle.putInt("connection", mConnection.getCurrentItem());
				intent.putExtras(bundle);
				setResult(101, intent);
				finish();
				break;
			case R.id.screen_window:
			case R.id.btn_cancel:
				finish();
				break;
			default:
				break;
		}
	}
}

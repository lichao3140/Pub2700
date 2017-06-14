package com.dpower.pub.dp2700.activity;

import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.tools.EditTextTool;
import com.dpower.pub.dp2700.tools.MyToast;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.pub.dp2700.view.MyEditText;
import com.dpower.util.CommonUT;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.Time;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class SetTimeActivity extends BaseActivity implements OnClickListener {
	
	private EditTextTool mEditTool;
	private MyEditText mEditYear;
	private MyEditText mEditMonth;
	private MyEditText mEditDate;
	private MyEditText mEditHour;
	private MyEditText mEditMinute;
	private int mYear;
	private int mMonth;
	private int mDate;
	private int mHour;
	private int mMinute;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_time_set);
		init();
	}

	private void init() {
		mEditTool = EditTextTool.getInstance();
		mEditYear = (MyEditText) findViewById(R.id.et_year);
		mEditMonth = (MyEditText) findViewById(R.id.et_month);
		mEditDate = (MyEditText) findViewById(R.id.et_date);
		mEditHour = (MyEditText) findViewById(R.id.et_hour);
		mEditMinute = (MyEditText) findViewById(R.id.et_minute);
		Time time = new Time();
		time.setToNow();
		mEditYear.setText(String.valueOf(time.year));
		if (time.month < 9) {
			mEditMonth.setText("0" + String.valueOf(time.month + 1));
		} else {
			mEditMonth.setText(String.valueOf(time.month + 1));
		}
		if (time.monthDay < 10) {
			mEditDate.setText("0" + String.valueOf(time.monthDay));
		} else {
			mEditDate.setText(String.valueOf(time.monthDay));
		}
		if (time.hour < 10) {
			mEditHour.setText("0" + String.valueOf(time.hour));
		} else {
			mEditHour.setText(String.valueOf(time.hour));
		}
		if (time.minute < 10) {
			mEditMinute.setText("0" + String.valueOf(time.minute));
		} else {
			mEditMinute.setText(String.valueOf(time.minute));
		}
		mEditYear.setSelection(mEditYear.getText().toString().length());
		findViewById(R.id.btn_back).setOnClickListener(this);
		findViewById(R.id.btn_confirm).setOnClickListener(this);
		setKeyboardClickListener(R.id.bt_num_1, "1");
		setKeyboardClickListener(R.id.bt_num_2, "2");
		setKeyboardClickListener(R.id.bt_num_3, "3");
		setKeyboardClickListener(R.id.bt_num_4, "4");
		setKeyboardClickListener(R.id.bt_num_5, "5");
		setKeyboardClickListener(R.id.bt_num_6, "6");
		setKeyboardClickListener(R.id.bt_num_7, "7");
		setKeyboardClickListener(R.id.bt_num_8, "8");
		setKeyboardClickListener(R.id.bt_num_9, "9");
		setKeyboardClickListener(R.id.bt_num_0, "0");
		setKeyboardClickListener(R.id.btn_delete, "-1");
	}
	
	private void setKeyboardClickListener(int resId, String tag) {
		View button;
		button = findViewById(resId);
		button.setTag(tag);
		button.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		findViewById(R.id.root_view).setBackground(
				SPreferences.getInstance().getWallpaper());
	}

	@Override
	public void onClick(View v) {
		if (v.getTag() != null) {
			onKeyboardClick(v.getTag().toString());
		}
		switch (v.getId()) {
			case R.id.btn_back:
				finish();
				break;
			case R.id.btn_confirm:
				if (checkTimeInput()) {
					boolean result = CommonUT.setSystemTime(
							mYear, mMonth, mDate, mHour, mMinute, 0);
					if (result) {
						MyToast.show(R.string.change_system_time_success);
						sendBroadcast(new Intent(DPFunction.ACTION_UPDATE_TIME));
						finish();
					} else {
						MyToast.show(R.string.change_system_time_fail);
					}
				}
				break;
			default:
				break;
		}
	}

	private void onKeyboardClick(String key) {
		if (mEditYear.hasFocus()) {
			if (key.equals("-1") || mEditYear.getText().toString().length() < 4) {
				editText(mEditYear, key);
			}
		} else if (mEditMonth.hasFocus()) {
			if (key.equals("-1") || mEditMonth.getText().toString().length() < 2) {
				editText(mEditMonth, key);
			}
		} else if (mEditDate.hasFocus()) {
			if (key.equals("-1") || mEditDate.getText().toString().length() < 2) {
				editText(mEditDate, key);
			}
		} else if (mEditHour.hasFocus()) {
			if (key.equals("-1") || mEditHour.getText().toString().length() < 2) {
				editText(mEditHour, key);
			}
		} else if (mEditMinute.hasFocus()) {
			if (key.equals("-1") || mEditMinute.getText().toString().length() < 2) {
				editText(mEditMinute, key);
			}
		}
	}
	
	private void editText(EditText editText, String key){
		mEditTool.setEditText(editText);
		if (key.equals("-1")) {
			mEditTool.deleteText();
		} else {
			mEditTool.appendTextTo(key);
		}
	}

	private boolean checkTimeInput() {
		if (mEditYear.getText().toString().equals("")) {
			mYear = 0;
		} else {
			mYear = Integer.valueOf(mEditYear.getText().toString());
		}
		if (mEditMonth.getText().toString().equals("")) {
			mMonth = 0;
		} else {
			mMonth = Integer.valueOf(mEditMonth.getText().toString());
		}
		if (mEditDate.getText().toString().equals("")) {
			mDate = 0;
		} else {
			mDate = Integer.valueOf(mEditDate.getText().toString());
		}
		if (mEditHour.getText().toString().equals("")) {
			mHour = 0;
		} else {
			mHour = Integer.valueOf(mEditHour.getText().toString());
		}
		if (mEditMinute.getText().toString().equals("")) {
			mMinute = 0;
		} else {
			mMinute = Integer.valueOf(mEditMinute.getText().toString());
		}
		if (mYear > 2100 || mYear < 1970) {
			MyToast.show(R.string.year_input_error);
			return false;
		}
		if (mMonth > 12 || mMonth < 1) {
			MyToast.show(R.string.month_input_error);
			return false;
		}

		if (mMonth == 2) { // 如果是2月,检测闰年
			if (mYear % 4 == 0) {// 不跨世纪，能被4整除即为闰年
				if (mDate < 1 || mDate > 29) {
					MyToast.show(R.string.date_input_error_29);
					return false;
				}
			} else {
				if (mDate < 1 || mDate > 28) {
					MyToast.show(R.string.date_input_error_28);
					return false;
				}
			}
		} else if (mMonth == 4 || mMonth == 6 || mMonth == 9 || mMonth == 11) { // 如果不是大月
			if (mDate < 1 || mDate > 30) {
				MyToast.show(R.string.date_input_error_30);
				return false;
			}
		} else {
			if (mDate < 1 || mDate > 31) {
				MyToast.show(R.string.date_input_error_31);
				return false;
			}
		}
		if (mHour < 0 || mHour > 23) {
			MyToast.show(R.string.hour_input_error);
			return false;
		}
		if (mMinute < 0 || mMinute > 59) {
			MyToast.show(R.string.minute_input_error);
			return false;
		}
		return true;
	}
}

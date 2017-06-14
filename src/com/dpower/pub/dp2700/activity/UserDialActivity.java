package com.dpower.pub.dp2700.activity;

import java.util.ArrayList;
import java.util.List;

import com.dpower.domain.AddrInfo;
import com.dpower.domain.CallInfomation;
import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.tools.EditTextTool;
import com.dpower.pub.dp2700.tools.MyToast;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.pub.dp2700.view.MyEditText;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

/**
 * 户户通话
 */
public class UserDialActivity extends BaseActivity implements OnClickListener {

	private EditTextTool mEditTool;
	private MyEditText mEditArea; // 区
	private MyEditText mEditBuilding; // 栋
	private MyEditText mEditUnit; // 单元
	private MyEditText mEditRoom; // 室
	private MyEditText mEditExtension; // 分机
	private ListView mCallRecord;
	private ArrayAdapter<String> mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_dial);
		init();
	}

	private void init() {
		mEditTool = EditTextTool.getInstance();
		mEditArea = (MyEditText) findViewById(R.id.et_area);
		mEditBuilding = (MyEditText) findViewById(R.id.et_building);
		mEditUnit = (MyEditText) findViewById(R.id.et_unit);
		mEditRoom = (MyEditText) findViewById(R.id.et_room);
		mEditExtension = (MyEditText) findViewById(R.id.et_extension);
		findViewById(R.id.btn_back).setOnClickListener(this);
		findViewById(R.id.btn_call).setOnClickListener(this);
		String roomCode = DPFunction.getRoomCode();
		if (roomCode != null) {
			mEditArea.setText(roomCode.substring(1, 3));
			mEditBuilding.setText(roomCode.substring(3, 5));
			mEditUnit.setText(roomCode.substring(5, 7));
			mEditRoom.setText(roomCode.substring(7, 11));
			mEditExtension.setText(roomCode.substring(11, 13));
		} else {
			finish();
			return;
		}
		mCallRecord = (ListView) findViewById(R.id.list_view_call_record);
		mCallRecord.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				callOut(mAdapter.getItem(position));
			}
		});
		setKeyboardClickListener(R.id.btn_num_1, "1");
		setKeyboardClickListener(R.id.btn_num_2, "2");
		setKeyboardClickListener(R.id.btn_num_3, "3");
		setKeyboardClickListener(R.id.btn_num_4, "4");
		setKeyboardClickListener(R.id.btn_num_5, "5");
		setKeyboardClickListener(R.id.btn_num_6, "6");
		setKeyboardClickListener(R.id.btn_num_7, "7");
		setKeyboardClickListener(R.id.btn_num_8, "8");
		setKeyboardClickListener(R.id.btn_num_9, "9");
		setKeyboardClickListener(R.id.btn_num_0, "0");
		setKeyboardClickListener(R.id.btn_delete, "-1");
		mEditRoom.setFocusableInTouchMode(true);
		mEditRoom.setFocusable(true);
		mEditRoom.requestFocus();
	}
	
	private void callOut(String roomCode) {
		if (roomCode == null || roomCode.length() <= 0) {
			MyToast.show(R.string.please_input_correct);
			return;
		}
		if (roomCode.equals(DPFunction.getRoomCode())) {
			MyToast.show(R.string.cannot_call_self);
			return;
		}
		AddrInfo addrInfo = DPFunction.getAddrInfo(roomCode);
		if (addrInfo == null) {
			MyToast.show(R.string.room_code_not_exist);
			return;
		}
		Intent intent;
		intent = new Intent();
		intent.setClass(this, CallOutActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(CallOutActivity.INTENT_EXTRA, roomCode);
		startActivity(intent);
	}
	
	private void setKeyboardClickListener(int resId, String tag) {
		View button;
		button = findViewById(resId);
		button.setTag(tag);
		button.setOnClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		findViewById(R.id.root_view).setBackground(
				SPreferences.getInstance().getWallpaper());
		createData();
	}
	
	private void createData() {
		ArrayList<CallInfomation> callInfos = DPFunction
				.getCallLogList(CallInfomation.CALL_OUT_ACCEPT
						| CallInfomation.CALL_OUT_UNACCEPT);
		if (callInfos != null) {
			List<String> lists = new ArrayList<String>();
			for (int i = 0; i < callInfos.size(); i++) {
				if (!lists.contains(callInfos.get(i).getRemoteCode())) {
					lists.add(callInfos.get(i).getRemoteCode());
				}
			}
			String[] datas = new String[lists.size()];
			for (int i = 0; i < datas.length; i++) {
				datas[i] = lists.get(i);
			}
			mAdapter = new ArrayAdapter<String>(this,
					R.layout.adapter_call_record_list, datas);
			mCallRecord.setAdapter(mAdapter);
		}
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
			case R.id.btn_call:
				callOut(getRoomCode());
				break;
			default:
				break;
		}
	}
	
	private void onKeyboardClick(String key) {
		if (mEditArea.hasFocus()) {
			if (key.equals("-1") || mEditArea.getText().toString().length() < 2) {
				editText(mEditArea, key);
			}
		} else if (mEditBuilding.hasFocus()) {
			if (key.equals("-1") || mEditBuilding.getText().toString().length() < 2) {
				editText(mEditBuilding, key);
			}
		} else if (mEditUnit.hasFocus()) {
			if (key.equals("-1") || mEditUnit.getText().toString().length() < 2) {
				editText(mEditUnit, key);
			}
		} else if (mEditRoom.hasFocus()) {
			if (key.equals("-1") || mEditRoom.getText().toString().length() < 4) {
				editText(mEditRoom, key);
			}
		} else if (mEditExtension.hasFocus()) {
			if (key.equals("-1") || mEditExtension.getText().toString().length() < 2) {
				editText(mEditExtension, key);
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

	protected String getRoomCode() {
		if (TextUtils.isEmpty(mEditArea.getText().toString())) {
			MyToast.show(R.string.area_input);
			return null;
		} else if (mEditArea.getText().toString().length() < 2) {
			MyToast.show(R.string.area_input_error);
			return null;
		}
		if (TextUtils.isEmpty(mEditBuilding.getText().toString())) {
			MyToast.show(R.string.building_input);
			return null;
		} else if (mEditBuilding.getText().toString().length() < 2) {
			MyToast.show(R.string.building_input_error);
			return null;
		}
		if (TextUtils.isEmpty(mEditUnit.getText().toString())) {
			MyToast.show(R.string.unit_input);
			return null;
		} else if (mEditUnit.getText().toString().length() < 2) {
			MyToast.show(R.string.unit_input_error);
			return null;
		}
		if (TextUtils.isEmpty(mEditRoom.getText().toString())) {
			MyToast.show(R.string.room_input);
			return null;
		} else if (mEditRoom.getText().toString().length() < 4) {
			MyToast.show(R.string.room_input_error);
			return null;
		}
		if (TextUtils.isEmpty(mEditExtension.getText().toString())) {
			MyToast.show(R.string.extension_input);
			return null;
		} else if (mEditExtension.getText().toString().length() < 2) {
			MyToast.show(R.string.extension_input_error);
			return null;
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append("1");
		buffer.append(mEditArea.getText().toString());
		buffer.append(mEditBuilding.getText().toString());
		buffer.append(mEditUnit.getText().toString());
		buffer.append(mEditRoom.getText().toString());
		buffer.append(mEditExtension.getText().toString());
		return buffer.toString();
	}
}

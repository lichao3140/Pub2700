package com.dpower.pub.dp2700.activity;

import java.util.List;

import com.dpower.domain.AlarmInfo;
import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.activity.dialog.AlarmAreaDialog;
import com.dpower.pub.dp2700.tools.SPreferences;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 工程設置----防区设置
 */
public class DefenceAreaSetActivity extends BaseActivity 
		implements OnClickListener, OnItemClickListener {
	
	private ListView mListView;
	private List<AlarmInfo> mInfoList;
	private static AlarmInfoAdapter mAdapter;
	private Context mContext;
	private long mConnection;
	private long mOldState;
	private int[] mAreaArray = { 0, 0, 0, 0, 0, 0, 0, 0 }; // 暂时保存安防设置的防区数组
	private int[] mTypeArray = { 0, 0, 0, 0, 0, 0, 0, 0 }; // 暂时保存安防设置的类型数组

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_defence_area_set);
		init();
	}

	private void init() {
		mContext = this;
		findViewById(R.id.btn_confirm).setOnClickListener(this);
		findViewById(R.id.btn_back).setOnClickListener(this);
		mListView = (ListView) findViewById(R.id.area_list);
		mConnection = DPFunction.getSefeConnection();
		mOldState = DPFunction.getSafeState();
		mInfoList = DPFunction.getAlarmInfoList(0);
		mAreaArray = DPFunction.getAlarmArea();
		mTypeArray = DPFunction.getAlarmType();

		mAdapter = new AlarmInfoAdapter();
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		findViewById(R.id.root_view).setBackground(
				SPreferences.getInstance().getWallpaper());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_back:
				finish();
				break;
			case R.id.btn_confirm:
				DPFunction.changeSafeConnection(mConnection);
				updataDefenceList();
				finish();
				break;
			default:
				break;
		}
	}
	
	private void updataDefenceList() {
		DPFunction.setAlarmArea(mAreaArray);
		DPFunction.setAlarmType(mTypeArray);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (position < 0) {
			return;
		}
		Intent intent = new Intent(mContext, AlarmAreaDialog.class);
		intent.putExtra("probe", position);
		startActivityForResult(intent, 100);
	}

	private class AlarmInfoAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mInfoList.size();
		}

		@Override
		public Object getItem(int position) {
			return mInfoList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ItemHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.adapter_area_list, null);
				holder = new ItemHolder();
				holder.id = (TextView) convertView.findViewById(R.id.tv_id);
				holder.areaName = (TextView) convertView.findViewById(R.id.tv_name);
				holder.areaType = (TextView) convertView.findViewById(R.id.tv_type);
				holder.connection = (TextView) convertView.findViewById(R.id.tv_connection);
				holder.isAbnormal = (ImageView) convertView.findViewById(R.id.image_isabnormal);
				convertView.setTag(holder);
			} else {
				holder = (ItemHolder) convertView.getTag();
			}

			holder.id.setText("" + (position + 1));
			holder.areaName.setText(mInfoList.get(position).areaName);
			holder.areaType.setText(mInfoList.get(position).areaType);
			holder.connection.setText(mInfoList.get(position).open 
						? R.string.level_high : R.string.level_low);
			long abnormal = mOldState ^ mConnection;
			if ((abnormal & (1 << position)) > 0) {
				holder.isAbnormal.setBackgroundResource(
						R.drawable.img_defence_area_detail_check_red);
			} else {
				holder.isAbnormal.setBackgroundResource(
						R.drawable.img_defence_area_detail_check_green);
			}
			return convertView;
		}

		class ItemHolder {
			public TextView id;
			public TextView areaName;
			public TextView areaType;
			public TextView connection;
			public ImageView isAbnormal;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 100 && resultCode == 101) {
			Bundle bundle = data.getExtras();
			int id = bundle.getInt("probe");
			int area = bundle.getInt("alarmArea");
			int type = bundle.getInt("alarmType");
			boolean bopen = bundle.getInt("connection") == 1;
			long temp = 1 << id;
			if (bopen) {
				mConnection |= temp;
			} else {
				mConnection &= (~temp);
			}
			mAreaArray[id] = area;
			mTypeArray[id] = type;
			mInfoList.get(id).areaName = DPFunction.getAlarmAreaNameList()
					.get(area).name;
			mInfoList.get(id).areaType = DPFunction.getAlarmTypeNameList()
					.get(type).name;
			mInfoList.get(id).open = bopen;
			mAdapter.notifyDataSetChanged();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}

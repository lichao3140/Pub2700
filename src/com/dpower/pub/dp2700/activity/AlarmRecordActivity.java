package com.dpower.pub.dp2700.activity;

import java.util.ArrayList;
import java.util.List;

import com.dpower.domain.AlarmLog;
import com.dpower.domain.AlarmNameInfo;
import com.dpower.domain.AlarmTypeInfo;
import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.dialog.TipsDialog;
import com.dpower.pub.dp2700.dialog.TipsDialog.OnDialogClickListener;
import com.dpower.pub.dp2700.tools.MyToast;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.util.CommonUT;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @author ZhengZhiying
 * @Funtion 家庭安防/报警记录
 */
public class AlarmRecordActivity extends BaseFragmentActivity 
		implements OnClickListener {

	private ListView mListView;
	private AlarmRecordAdapter mAdapter;
	private ArrayList<AlarmLog> mAlarmLogs;
	private List<AlarmTypeInfo> mTypelist;
	private List<AlarmNameInfo> mNamelist;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm_record);
		init();
	}

	private void init() {
		mContext = this;
		mAlarmLogs = new ArrayList<AlarmLog>();
		mListView = (ListView) findViewById(R.id.list_view_alarm_record);
		findViewById(R.id.btn_back).setOnClickListener(this);
		findViewById(R.id.btn_delete).setOnClickListener(this);
		findViewById(R.id.btn_delete_all).setOnClickListener(this);
		mNamelist = DPFunction.getAlarmAreaNameList();
		mTypelist = DPFunction.getAlarmTypeNameList();
		mAdapter = new AlarmRecordAdapter();
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mAdapter.checkId = position;
				mAdapter.notifyDataSetChanged();

			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		findViewById(R.id.root_view).setBackground(
				SPreferences.getInstance().getWallpaper());
		updateListView();
	}

	private void updateListView() {
		mAlarmLogs = DPFunction.getAlarmLogList();
		for (int i = 0; i < mAlarmLogs.size(); i++) {
			mAlarmLogs.get(i).setIsRead(true);
			DPFunction.modifyAlarmLog(mAlarmLogs.get(i));
		}
		mAdapter.checkId = -1;
		mAdapter.notifyDataSetChanged();
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_back:
				finish();
				break;
			case R.id.btn_delete:
				if (mAdapter.checkId >= 0) {
					TipsDialog dialog = new TipsDialog(mContext);
					dialog.setContent(getString(R.string.delete_or_not) + "?");
					dialog.setOnClickListener(new OnDialogClickListener() {
						
						@Override
						public void onClick() {
							DPFunction.deleteAlarmLog(mAlarmLogs.get(mAdapter.checkId).getDb_id());
							MyToast.show(R.string.delete_success);
							updateListView();
						}
					});
					dialog.show();
				} else if (mAlarmLogs.size() > 0) {
					MyToast.show(R.string.no_item_check);
				} else {
					MyToast.show(R.string.no_item_to_del);
				}
				break;
			case R.id.btn_delete_all:
				if (mAlarmLogs.size() > 0) {
					TipsDialog dialog = new TipsDialog(mContext);
					dialog.setContent(getString(R.string.text_delete_all) + "?");
					dialog.setOnClickListener(new OnDialogClickListener() {
						
						@Override
						public void onClick() {
							DPFunction.deleteAllAlarmLog();
							MyToast.show(R.string.delete_success);
							updateListView();
						}
					});
					dialog.show();
				} else {
					MyToast.show(R.string.no_item_to_del);
				}
				break;
			default:
				break;
		}
	}

	private class AlarmRecordAdapter extends BaseAdapter {
		
		public int checkId = -1;

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.adapter_alarm_record_list, null);
				holder = new ViewHolder(convertView);
				holder.id = (TextView) convertView.findViewById(R.id.tv_id);
				holder.num = (TextView) convertView.findViewById(R.id.tv_num);
				holder.name = (TextView) convertView.findViewById(R.id.tv_name);
				holder.type = (TextView) convertView.findViewById(R.id.tv_type);
				holder.time = (TextView) convertView.findViewById(R.id.tv_time);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.id.setText((position + 1) + "");
			holder.num.setText(mAlarmLogs.get(position).getAreaNum() + "");
			holder.name.setText(mNamelist.get(mAlarmLogs.get(position).getAreaName()).name);
			holder.type.setText(mTypelist.get(mAlarmLogs.get(position).getAreaType()).name);
			holder.time.setText(CommonUT.formatTime(mAlarmLogs.get(position).getTime()));
			holder.setCheck(checkId == position);
			return convertView;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public Object getItem(int position) {
			return mAlarmLogs.get(position);
		}

		@Override
		public int getCount() {
			return mAlarmLogs.size();
		}
		
		class ViewHolder {
			TextView id;
			TextView num;
			TextView name;
			TextView type;
			TextView time;
			
			public ViewHolder(View view) {
				super();
				id = (TextView) view.findViewById(R.id.tv_id);
				num = (TextView) view.findViewById(R.id.tv_num);
				name = (TextView) view.findViewById(R.id.tv_name);
				type = (TextView) view.findViewById(R.id.tv_type);
				time = (TextView) view.findViewById(R.id.tv_time);
			}
			
			public void setCheck(boolean isChecked) {
				if (isChecked) {
					String code = id.getText().toString();
					id.setText("√ " + code);
					id.setTextColor(mContext.getResources().getColor(
							R.color.DarkIndigo));
					num.setTextColor(mContext.getResources().getColor(
							R.color.DarkIndigo));
					name.setTextColor(mContext.getResources().getColor(
							R.color.DarkIndigo));
					type.setTextColor(mContext.getResources().getColor(
							R.color.DarkIndigo));
					time.setTextColor(mContext.getResources().getColor(
							R.color.DarkIndigo));
				} else {
					id.setTextColor(mContext.getResources().getColor(
							R.color.DarkDeepGrey));
					num.setTextColor(mContext.getResources().getColor(
							R.color.DarkDeepGrey));
					name.setTextColor(mContext.getResources().getColor(
							R.color.DarkDeepGrey));
					type.setTextColor(mContext.getResources().getColor(
							R.color.DarkDeepGrey));
					time.setTextColor(mContext.getResources().getColor(
							R.color.DarkDeepGrey));
				}
			}
		}
	};
}

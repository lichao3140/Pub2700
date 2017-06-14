package com.dpower.pub.dp2700.activity;

import java.util.ArrayList;

import com.dpower.domain.SafeModeInfo;
import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.dialog.TipsDialog;
import com.dpower.pub.dp2700.dialog.TipsDialog.OnDialogClickListener;
import com.dpower.pub.dp2700.tools.MyToast;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.util.CommonUT;
import com.dpower.util.ConstConf;

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
 * @Funtion 家庭安防/安防日志
 */
public class DefenceRecordActivity extends BaseFragmentActivity 
		implements OnClickListener {

	private ListView mListView;
	private ArrayList<SafeModeInfo> mSafeModeInfos;
	private DefenceRecordAdapter mAdapter;
	private Context mContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record_security);
		init();
	}

	private void init() {
		mContext = this;
		mSafeModeInfos = new ArrayList<SafeModeInfo>();
		mListView = (ListView) findViewById(R.id.list_view_security_log);
		findViewById(R.id.btn_back).setOnClickListener(this);
		findViewById(R.id.btn_delete).setOnClickListener(this);
		findViewById(R.id.btn_delete_all).setOnClickListener(this);
		mAdapter = new DefenceRecordAdapter();
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
		mSafeModeInfos = DPFunction.getSafeModeInfoList();
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
							DPFunction.deleteAlarmLog(mSafeModeInfos.get(mAdapter.checkId).getDb_id());
							MyToast.show(R.string.delete_success);
							updateListView();
						}
					});
					dialog.show();
				} else if (mSafeModeInfos.size() > 0) {
					MyToast.show(R.string.no_item_check);
				} else {
					MyToast.show(R.string.no_item_to_del);
				}
				break;
			case R.id.btn_delete_all:
				if (mSafeModeInfos.size() > 0) {
					TipsDialog dialog = new TipsDialog(mContext);
					dialog.setContent(getString(R.string.text_delete_all) + "?");
					dialog.setOnClickListener(new OnDialogClickListener() {
						
						@Override
						public void onClick() {
							DPFunction.deleteAllSafeModeLog();
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

	private String getModeName(int value) {
		switch (value) {
			case ConstConf.UNSAFE_MODE:
				return getString(R.string.mode_out_of_security);
			case ConstConf.NIGHT_MODE:
				return getString(R.string.mode_in_hight);
			case ConstConf.HOME_MODE:
				return getString(R.string.mode_at_home);
			case ConstConf.LEAVE_HOME_MODE:
				return getString(R.string.mode_leave_home);
			default:
				return getString(R.string.unknow_type);
		}
	}

	private class DefenceRecordAdapter extends BaseAdapter {

		public int checkId = -1;

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(DefenceRecordActivity.this)
						.inflate(R.layout.adapter_security_log_list, null);
				holder = new ViewHolder(convertView);
				holder.id = (TextView) convertView.findViewById(R.id.tv_id);
				holder.type = (TextView) convertView.findViewById(R.id.tv_type);
				holder.time = (TextView) convertView.findViewById(R.id.tv_time);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.id.setText((position + 1) + "");
			holder.type.setText(getModeName(mSafeModeInfos.get(position).getMode()));
			holder.time.setText(CommonUT.formatTime(mSafeModeInfos.get(position).getTime()));
			holder.setCheck(checkId == position);
			return convertView;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public Object getItem(int position) {
			return mSafeModeInfos.get(position);
		}

		@Override
		public int getCount() {

			return mSafeModeInfos.size();
		}
		
		class ViewHolder {
			TextView id;
			TextView type;
			TextView time;
			
			public ViewHolder(View view) {
				super();
				id = (TextView) view.findViewById(R.id.tv_id);
				type = (TextView) view.findViewById(R.id.tv_type);
				time = (TextView) view.findViewById(R.id.tv_time);
			}
			
			public void setCheck(boolean isChecked) {
				if (isChecked) {
					String code = id.getText().toString();
					id.setText("√ " + code);
					id.setTextColor(mContext.getResources().getColor(
							R.color.DarkIndigo));
					type.setTextColor(mContext.getResources().getColor(
							R.color.DarkIndigo));
					time.setTextColor(mContext.getResources().getColor(
							R.color.DarkIndigo));
				} else {
					id.setTextColor(mContext.getResources().getColor(
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
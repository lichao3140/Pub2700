package com.dpower.pub.dp2700.adapter;

import java.util.ArrayList;
import java.util.List;
import com.dpower.domain.BindAccountInfo;
import com.dpower.pub.dp2700.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * @author ZhengZhiying
 * @Funtion ∞Û∂®…Ë±∏
 */
public class DevicesListAdapter extends BaseAdapter {

	public int checkID = -1;
	private Context mContext;
	private List<BindAccountInfo> mDevicesList;

	public DevicesListAdapter(Context context) {
		super();
		mContext = context;
		mDevicesList = new ArrayList<BindAccountInfo>();
	}

	@Override
	public int getCount() {
		return mDevicesList.size();
	}

	@Override
	public Object getItem(int position) {
		return mDevicesList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void setDevicesList(List<BindAccountInfo> datas) {
		mDevicesList = datas;
	}

	public List<BindAccountInfo> getDevicesList() {
		return mDevicesList;
	}

	@SuppressLint("InflateParams") @Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.adapter_bind_devices_list, null);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.id.setText(mDevicesList.get(position).getmDB_id() + "");
		holder.accountName.setText(mDevicesList.get(position).getAccountname());
		String deviceType = mDevicesList.get(position).getPhonetype();		
		if (deviceType.equals("1")) {
			holder.phoneType.setText(R.string.devices_type_phone);
		} else if (deviceType.equals("2")) {
			holder.phoneType.setText(R.string.devices_type_card);
		}
		int line = mDevicesList.get(position).getIsonline();
		if (line == 0){
			holder.isOnline.setText(R.string.bind_device_on);
		} else if(line == 2){
			holder.isOnline.setText(R.string.bind_device_off);
		}
		holder.setCheck(checkID == position);
		return convertView;
	}

	public class ViewHolder {

		public TextView id;
		public TextView accountName;
		public TextView phoneType;
		public TextView isOnline;

		public ViewHolder(View view) {
			super();
			id = (TextView) view.findViewById(R.id.tv_devices_id);
			accountName = (TextView) view.findViewById(R.id.tv_devices_name);
			phoneType = (TextView) view.findViewById(R.id.tv_devices_type);
			isOnline = (TextView) view.findViewById(R.id.tv_devices_isonline);
		}

		public void setCheck(boolean isChecked) {
			if (isChecked) {
				String code = id.getText().toString();
				id.setText("°Ã " + code);
				id.setTextColor(mContext.getResources().getColor(
						R.color.DarkIndigo));
				accountName.setTextColor(mContext.getResources().getColor(
						R.color.DarkIndigo));
				phoneType.setTextColor(mContext.getResources().getColor(
						R.color.DarkIndigo));
				isOnline.setTextColor(mContext.getResources().getColor(
						R.color.DarkIndigo));
			} else {
				id.setTextColor(mContext.getResources().getColor(
						R.color.DarkDeepGrey));
				accountName.setTextColor(mContext.getResources().getColor(
						R.color.DarkDeepGrey));
				phoneType.setTextColor(mContext.getResources().getColor(
						R.color.DarkDeepGrey));
				isOnline.setTextColor(mContext.getResources().getColor(
						R.color.DarkDeepGrey));
			}
		}
	}
}

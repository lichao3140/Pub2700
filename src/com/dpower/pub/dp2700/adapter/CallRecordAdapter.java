package com.dpower.pub.dp2700.adapter;

import java.util.ArrayList;
import java.util.List;

import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.model.CallRecordMod;
import com.dpower.pub.dp2700.tools.RoomInfoUT;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * @author ZhengZhiying
 * @Funtion ∫ÙΩ–º«¬º  ≈‰∆˜
 */
public class CallRecordAdapter extends BaseAdapter {

	public int checkID = -1;
	private Context mContext;
	private List<CallRecordMod> mCallRecordList;

	public CallRecordAdapter(Context context) {
		super();
		mContext = context;
		mCallRecordList = new ArrayList<CallRecordMod>();
	}

	@Override
	public int getCount() {
		return mCallRecordList.size();
	}

	@Override
	public Object getItem(int position) {
		return mCallRecordList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void setCallRecordList(List<CallRecordMod> datas) {
		mCallRecordList = datas;
	}

	public List<CallRecordMod> getCallRecordList() {
		return mCallRecordList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.adapter_call_log_list, null);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.roomCode.setText(mCallRecordList.get(position).roomCode);
		holder.roomName.setText(new RoomInfoUT(
				mCallRecordList.get(position).roomCode).getRoomName(mContext));
		holder.date.setText(mCallRecordList.get(position).beginTime);
		String time = mCallRecordList.get(position).talkTime;
		holder.callTime.setText(time.substring(time.indexOf(":") + 1,
				time.length()));// 00:00:00Ωÿ»°≥…00:00
		holder.setCheck(checkID == position);
		return convertView;
	}

	public class ViewHolder {

		public TextView roomName;
		public TextView roomCode;
		public TextView callTime;
		public TextView date;

		public ViewHolder(View view) {
			super();
			roomCode = (TextView) view.findViewById(R.id.tv_room_code);
			roomName = (TextView) view.findViewById(R.id.tv_room_name);
			callTime = (TextView) view.findViewById(R.id.tv_call_time);
			date = (TextView) view.findViewById(R.id.tv_date);
		}

		public void setCheck(boolean isChecked) {
			if (isChecked) {
				String code = roomCode.getText().toString();
				roomCode.setText("°Ã " + code);
				roomCode.setTextColor(mContext.getResources().getColor(
						R.color.DarkIndigo));
				roomName.setTextColor(mContext.getResources().getColor(
						R.color.DarkIndigo));
				callTime.setTextColor(mContext.getResources().getColor(
						R.color.DarkIndigo));
				date.setTextColor(mContext.getResources().getColor(
						R.color.DarkIndigo));
			} else {
				roomCode.setTextColor(mContext.getResources().getColor(
						R.color.DarkDeepGrey));
				roomName.setTextColor(mContext.getResources().getColor(
						R.color.DarkDeepGrey));
				callTime.setTextColor(mContext.getResources().getColor(
						R.color.DarkDeepGrey));
				date.setTextColor(mContext.getResources().getColor(
						R.color.DarkDeepGrey));
			}
		}
	}
}

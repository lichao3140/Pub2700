package com.dpower.pub.dp2700.adapter;

import java.util.ArrayList;
import java.util.List;

import com.dpower.domain.MessageInfo;
import com.dpower.pub.dp2700.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * @author ZhengZhiying
 * @Funtion 主页-》短信-》信息列表适配器
 */
public class SMSAdapter extends BaseAdapter {

	public List<MessageInfo> messageList;
	public int checkPos = -1;
	private Context mContext;

	public SMSAdapter(Context context) {
		super();
		mContext = context;
		messageList = new ArrayList<MessageInfo>();
	}

	@Override
	public int getCount() {
		return messageList.size();
	}

	@Override
	public Object getItem(int position) {
		return messageList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.adapter_sms_list, null);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.number.setText(String.valueOf(position + 1));
		holder.topic.setText(messageList.get(position).getTitle());
		holder.date.setText(messageList.get(position).getTime());
		if (messageList.get(position).isRead()) {
			holder.isRead.setText(R.string.already_read);
		} else {
			holder.isRead.setText(R.string.no_read);
		}
		holder.setCheck(checkPos == position);
		return convertView;
	}

	public class ViewHolder {
		
		public TextView number;
		public TextView topic;
		public TextView date;
		public TextView isRead;

		public ViewHolder(View view) {
			super();
			number = (TextView) view.findViewById(R.id.tv_number);
			topic = (TextView) view.findViewById(R.id.tv_topic);
			date = (TextView) view.findViewById(R.id.tv_date);
			isRead = (TextView) view.findViewById(R.id.tv_is_read);
		}

		public void setCheck(boolean isChecked) {
			if (isChecked) {
				String position = number.getText().toString();
				number.setText("√ " + position);
				number.setTextColor(mContext.getResources().getColor(
						R.color.DarkIndigo));
				topic.setTextColor(mContext.getResources().getColor(
						R.color.DarkIndigo));
				date.setTextColor(mContext.getResources().getColor(
						R.color.DarkIndigo));
				isRead.setTextColor(mContext.getResources().getColor(
						R.color.DarkIndigo));
			} else {
				number.setTextColor(mContext.getResources().getColor(
						R.color.Black));
				topic.setTextColor(mContext.getResources().getColor(
						R.color.Black));
				date.setTextColor(mContext.getResources().getColor(
						R.color.Black));
				isRead.setTextColor(mContext.getResources().getColor(
						R.color.Black));
			}
		}
	}
}

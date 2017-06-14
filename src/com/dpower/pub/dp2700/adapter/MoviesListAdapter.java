package com.dpower.pub.dp2700.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.dpower.pub.dp2700.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MoviesListAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	public List<File> files = new ArrayList<File>();

	public MoviesListAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return files == null ? 0 : files.size();
	}

	@Override
	public Object getItem(int position) {
		return files.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.adapter_movie_list,
					parent, false);
			holder = new ViewHolder();
			holder.videoName = (TextView) convertView.findViewById(R.id.tv_video_name);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.videoName.setText(files.get(position).getName());
		return convertView;
	}

	class ViewHolder {
		TextView videoName;
	}
}

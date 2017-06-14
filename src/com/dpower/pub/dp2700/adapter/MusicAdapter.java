package com.dpower.pub.dp2700.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.util.MyLog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author ZhengZhiying
 * @Funtion “Ù¿÷–¿…Õ  ≈‰∆˜
 */
public class MusicAdapter extends BaseAdapter {
	private static final String TAG = "MusicAdapter";
	
	private LayoutInflater mInflater;
	private List<RingChoiseMod> mDatas;
	public int mClickID = 0;

	public MusicAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
		mDatas = new ArrayList<RingChoiseMod>();
	}

	public void setMusicFiles(List<File> files) {
		if (files.size() < 1) {
			return;
		}
		String fileAbsolutePath = SPreferences.getInstance().getRingAbsolutePath();
		for (int i = 0; i < files.size(); i++) {
			mDatas.add(new RingChoiseMod(files.get(i), false));
			if (files.get(i).getAbsolutePath().equals(fileAbsolutePath)) {
				mClickID = i;
			}
		}
		MyLog.print(TAG, "init MusicData size = " + mDatas.size());
		mDatas.get(mClickID).isCheck = false;
	}

	@Override
	public int getCount() {
		return mDatas == null ? 0 : mDatas.size();
	}

	@Override
	public Object getItem(int position) {
		return mDatas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.adapter_music_list, null);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.musicName.setText(mDatas.get(position).file.getName());
		if (mDatas.get(position).isCheck) {
			holder.imageCheck.setVisibility(View.VISIBLE);
		} else {
			holder.imageCheck.setVisibility(View.INVISIBLE);
		}
		return convertView;
	}

	class RingChoiseMod {

		public File file;
		public boolean isCheck;

		public RingChoiseMod(File file, boolean isCheck) {
			super();
			this.file = file;
			this.isCheck = isCheck;
		}

		public RingChoiseMod() {
			super();
		}
	}
	
	public void setClickIndex(int pos) {
		mDatas.get(mClickID).isCheck = false;
		mClickID = pos;
		mDatas.get(mClickID).isCheck = true;
		notifyDataSetChanged();
	}

	class ViewHolder {
		ImageView imageCheck;
		TextView musicName;

		public ViewHolder(View view) {
			super();
			imageCheck = (ImageView) view.findViewById(R.id.image_ring_check);
			musicName = (TextView) view.findViewById(R.id.tv_ring_name);
		}
	}
}

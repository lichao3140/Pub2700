package com.dpower.pub.dp2700.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.dpower.domain.AlarmVideo;
import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.dialog.TipsDialog;
import com.dpower.pub.dp2700.dialog.TipsDialog.OnDialogClickListener;
import com.dpower.pub.dp2700.tools.FileOperate;
import com.dpower.pub.dp2700.tools.MyToast;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.util.ConstConf;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

/**
 * @author ZhengZhiying
 * @Funtion ¼ÒÍ¥°²·À/°²·ÀÂ¼Ïñ
 */
public class AlarmVideoActivity extends BaseFragmentActivity implements
		OnClickListener {

	private Context mContext;
	private ListView mListView;
	private AlarmVideoAdapter mAdapter;
	public List<AlarmVideo> mDatas;
	private VideoView mVideoView;
	public int mCheckPos = -1;
	private MediaController mMediaController;
	private ImageButton mButtonPlay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm_video);
		init();
	}

	private void init() {
		mContext = this;
		mListView = (ListView) findViewById(R.id.list_view_alarm_video);
		findViewById(R.id.btn_back).setOnClickListener(this);
		findViewById(R.id.btn_delete_all).setOnClickListener(this);
		findViewById(R.id.btn_delete).setOnClickListener(this);
		mButtonPlay = (ImageButton) findViewById(R.id.btn_video_play);
		mButtonPlay.setOnClickListener(this);
		mVideoView = (VideoView) findViewById(R.id.video_view);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (mVideoView != null && mVideoView.isPlaying()) {
					mVideoView.stopPlayback();
				}
				mCheckPos = position;
				mButtonPlay.setVisibility(View.VISIBLE);
				mAdapter.notifyDataSetChanged();
			}
		});
		mMediaController = new MediaController(mContext);
		mDatas = new ArrayList<AlarmVideo>();
		mAdapter = new AlarmVideoAdapter();
		mListView.setAdapter(mAdapter);
		mVideoView.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				mButtonPlay.setVisibility(View.GONE);
			}
		});
		mVideoView.setOnCompletionListener(new OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
				mVideoView.setVisibility(View.GONE);
				mButtonPlay.setVisibility(View.VISIBLE);
			}
		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mDatas = DPFunction.getAlarmVideoList();
		mCheckPos = -1;
		mAdapter.notifyDataSetChanged();
		if (mDatas.size() == 0) {
			mVideoView.setVisibility(View.GONE);
			mButtonPlay.setVisibility(View.GONE);
		}
		findViewById(R.id.root_view).setBackground(
				SPreferences.getInstance().getWallpaper());
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mVideoView != null && mVideoView.isPlaying()) {
			mVideoView.stopPlayback(); 
			mVideoView.setVisibility(View.GONE);
			mButtonPlay.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_back:
				finish();
				break;
			case R.id.btn_video_play:
				playVideo();
				break;
			case R.id.btn_delete:
				if (mCheckPos >= 0) {
					TipsDialog dialog = new TipsDialog(mContext);
					dialog.setContent(getString(R.string.delete_or_not) + "?");
					dialog.setOnClickListener(new OnDialogClickListener() {
						
						@Override
						public void onClick() {
							if (mVideoView != null && mVideoView.isPlaying()) {
								mVideoView.stopPlayback();
							}
							DPFunction.deleteAlarmVideo(mDatas.get(mCheckPos).id);
							File file = new File(mDatas.get(mCheckPos).path);
							if (file.exists()) {
								file.delete();
							}
							mDatas = DPFunction.getAlarmVideoList();
							mVideoView.setVisibility(View.GONE);
							if (mDatas.size() == 0) {
								mButtonPlay.setVisibility(View.GONE);
							} else {
								mButtonPlay.setVisibility(View.VISIBLE);
							}
							mCheckPos = -1;
							mAdapter.notifyDataSetChanged();
							MyToast.show(R.string.delete_success);
						}
					});
					dialog.show();
				} else if (mDatas.size() > 0) {
					MyToast.show(R.string.no_item_check);
				} else {
					MyToast.show(R.string.no_item_to_del);
				}
				break;
			case R.id.btn_delete_all:
				if (mDatas.size() > 0) {
					TipsDialog dialog = new TipsDialog(mContext);
					dialog.setContent(getString(R.string.text_delete_all) + "?");
					dialog.setOnClickListener(new OnDialogClickListener() {
						
						@Override
						public void onClick() {
							if (mVideoView != null && mVideoView.isPlaying()) {
								mVideoView.stopPlayback();
							}
							DPFunction.deleteAlarmVideo();
							File file = new File(ConstConf.ALARM_VIDEO_PATH);
							if (file.exists()) {
								FileOperate.recursionDeleteFile(file);
							}
							mDatas = DPFunction.getAlarmVideoList();
							mVideoView.setVisibility(View.GONE);
							mButtonPlay.setVisibility(View.GONE);
							mCheckPos = -1;
							mAdapter.notifyDataSetChanged();
							MyToast.show(R.string.delete_success);
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

	private void playVideo() {
		if (mCheckPos < 0) {
			MyToast.show(R.string.choice_none_paly_file);
			return;
		}
		if (mVideoView.isPlaying()) {
			mVideoView.pause();
			return;
		}
		File file = new File(mDatas.get(mCheckPos).path);
		if (!file.exists()) {
			MyToast.show(R.string.no_exist_file);
			return;
		}
		mVideoView.setVisibility(View.VISIBLE);
		mVideoView.setVideoPath(file.getAbsolutePath());
		mVideoView.setMediaController(mMediaController);
		mMediaController.setMediaPlayer(mVideoView);
		mVideoView.requestFocus();
		mVideoView.start();
	}

	private class AlarmVideoAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mDatas.size();
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
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.adapter_monitor_list, null);
				holder = new ViewHolder(convertView);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.textAlarmVideo.setText(mDatas.get(position).time);
			if (mCheckPos == position) {
				holder.textAlarmVideo.setTextColor(getResources()
						.getColor(R.color.Teal));
			} else {
				holder.textAlarmVideo.setTextColor(getResources()
						.getColor(R.color.DarkBlue));
			}
			return convertView;
		}
		
		public class ViewHolder {
			public TextView textAlarmVideo;
			
			public ViewHolder(View view) {
				textAlarmVideo = (TextView) view.findViewById(R.id.tv_monitor);
			}
		}
	}
}

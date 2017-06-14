package com.dpower.pub.dp2700.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.dpower.domain.CallInfomation;
import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.tools.FileOperate;
import com.dpower.pub.dp2700.tools.MediaPlayerTools;
import com.dpower.pub.dp2700.tools.MediaPlayerTools.OnEndListener;
import com.dpower.pub.dp2700.tools.MyToast;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.util.CommonUT;
import com.dpower.util.ConstConf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * ¡Ù”∞¡Ù—‘
 */
public class LeaveMessageActivity extends BaseActivity implements OnClickListener {
	
	private ListView mListView;
	private ImageView mImageVideo;
	private Button mButtonPlay;
	private ArrayList<CallInfomation> mCallLoglist;
	private int mCheckPos = -1;
	private LeaveAdapter mAdapter;
	private MediaPlayerTools mPlayerTools;
	private boolean mIsFirst = true;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_leave_message);
		init();
	}

	private void init() {
		mContext = this;
		mPlayerTools = new MediaPlayerTools(false);
		findViewById(R.id.btn_delete).setOnClickListener(this);
		findViewById(R.id.btn_delete_all).setOnClickListener(this);
		findViewById(R.id.btn_check).setOnClickListener(this);
		findViewById(R.id.btn_back).setOnClickListener(this);
		mButtonPlay = (Button) findViewById(R.id.btn_check);
		mImageVideo = (ImageView) findViewById(R.id.image_video);
		mListView = (ListView) findViewById(R.id.list_view_message);
		mCallLoglist = new ArrayList<CallInfomation>();
		mPlayerTools.setOnEndListener(new OnEndListener() {
			
			@Override
			public void onCompletion() {
				mButtonPlay.setText(R.string.text_leave_message_check);
			}
		});
		mAdapter = new LeaveAdapter();
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				File destDir = new File(ConstConf.VISIT_PATH);
				String path = new String(destDir.getAbsolutePath()
						+ File.separator
						+ CommonUT.formatTime(mCallLoglist.get(position)
								.getStartTime()).replaceAll(":", "-") + ".jpg");
				mCheckPos = position;
				showPicture(path);
				mPlayerTools.release();
				mButtonPlay.setText(R.string.text_leave_message_check);
				mAdapter.notifyDataSetChanged();
			}
		});
		showPicture("");
	}
	
	private void showPicture(String path) {
		if (TextUtils.isEmpty(path)) {
			mImageVideo.setImageResource(R.drawable.error_pic);
			return;
		}
		Bitmap bm = null;
		InputStream stream = null;
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			stream = new FileInputStream(path);
			options.inPreferredConfig = Bitmap.Config.RGB_565;
			bm = BitmapFactory.decodeStream(stream, null, options);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
		}
		if (bm == null) {
			mImageVideo.setImageResource(R.drawable.error_pic);
		} else {
			mImageVideo.setImageBitmap(bm);
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus && mIsFirst) {
			mIsFirst = false;
			View view = mListView.getChildAt(0);
			if (view != null) {
				mListView.performItemClick(view, 0, view.getId());
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		findViewById(R.id.root_view).setBackground(
				SPreferences.getInstance().getWallpaper());
		updateListView();
	}
	
	private void updateListView() {
		ArrayList<CallInfomation> callInfos = DPFunction.getCallLogList(
				CallInfomation.CALL_IN_UNACCEPT);
		if (callInfos != null) {
			ArrayList<CallInfomation> infos = new ArrayList<CallInfomation>();
			for (int i = 0; i < callInfos.size(); i++) {
				if (callInfos.get(i).isDoor()) {
					callInfos.get(i).setIsRead(true);
					DPFunction.modifyCallLog(callInfos.get(i));
					infos.add(callInfos.get(i));
				}
			}
			mCallLoglist = infos;
		}
		mCheckPos = -1;
		mAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mPlayerTools.release();
		mButtonPlay.setText(R.string.text_leave_message_check);
	}

	class LeaveAdapter extends BaseAdapter {

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
			holder.textLeaveMessage.setText(CommonUT.formatTime(
					mCallLoglist.get(position).getStartTime()));
			if (mCheckPos == position) {
				holder.textLeaveMessage.setTextColor(getResources().
						getColor(R.color.Teal));
			} else {
				holder.textLeaveMessage.setTextColor(getResources().
						getColor(R.color.DarkBlue));
			}
			return convertView;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public Object getItem(int position) {
			return mCallLoglist.get(position);
		}

		@Override
		public int getCount() {
			return mCallLoglist.size();
		}
		
		public class ViewHolder {
			public TextView textLeaveMessage;
			
			public ViewHolder(View view) {
				textLeaveMessage = (TextView) view.findViewById(R.id.tv_monitor);
			}
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_back:
				finish();
				break;
			case R.id.btn_check:
				playMessage();
				break;
			case R.id.btn_delete:
				if (mCheckPos >= 0) {
					DPFunction.deleteCallLog(mCallLoglist.get(mCheckPos).getDb_id());
					if (mPlayerTools.isPlaying) {
						mPlayerTools.release();
						mButtonPlay.setText(R.string.text_leave_message_check);
					}
					deleteFile();
					mCallLoglist.remove(mCheckPos);
					MyToast.show(R.string.delete_success);
					mCheckPos = -1;
					mAdapter.notifyDataSetChanged();
					showPicture("");
				} else if (mCallLoglist.size() > 0) {
					MyToast.show(R.string.no_item_check);
				} else {
					MyToast.show(R.string.no_item_to_del);
				}
				break;
			case R.id.btn_delete_all:
				if (mCallLoglist.size() > 0) {
					if (mPlayerTools.isPlaying) {
						mPlayerTools.release();
						mButtonPlay.setText(R.string.text_leave_message_check);
					}
					for (int i = 0; i < mCallLoglist.size(); i++) {
						DPFunction.deleteCallLog(mCallLoglist.get(i).getDb_id());
					}
					File file = new File(ConstConf.VISIT_PATH);
					if (file.exists()) {
						FileOperate.recursionDeleteFile(file);
					}
					mCallLoglist.clear();
					mCheckPos = -1;
					MyToast.show(R.string.delete_success);
					mAdapter.notifyDataSetChanged();
					showPicture("");
				} else {
					MyToast.show(R.string.no_item_to_del);
				}
				break;
			default:
				break;
		}
	}
	
	private void playMessage() {
		if (mCheckPos >= 0) {
			if (mPlayerTools.isPlaying) {
				mPlayerTools.release();
				mButtonPlay.setText(R.string.text_leave_message_check);
			} else {
				File destDir = new File(ConstConf.VISIT_PATH);
				String path = new String(destDir.getAbsolutePath()
						+ File.separator
						+ CommonUT.formatTime(mCallLoglist.get(mCheckPos)
								.getStartTime()).replaceAll(":", "-") + ".wav");
				File file = new File(path);
				if (!file.exists()) {
					MyToast.show(R.string.no_leave_audio_date_from_unitgate);
					return;
				}
				mPlayerTools.playMusic(path);
				mButtonPlay.setText(R.string.text_leave_message_palying);
			}
		} else if (mCallLoglist.size() > 0) {
			MyToast.show(R.string.no_item_check);
		} else {
			MyToast.show(R.string.no_item_to_del);
		}
	}
	
	private void deleteFile() {
		String picturePath = new String(ConstConf.VISIT_PATH
				+ File.separator
				+ CommonUT.formatTime(mCallLoglist.get(mCheckPos)
						.getStartTime()).replaceAll(":", "-") + ".jpg");
		String audioPath = new String(ConstConf.VISIT_PATH
				+ File.separator
				+ CommonUT.formatTime(mCallLoglist.get(mCheckPos)
						.getStartTime()).replaceAll(":", "-") + ".wav");
		File pictureFile = new File(picturePath);
		File audioFile = new File(audioPath);
		if (pictureFile.exists()) {
			pictureFile.delete();
		}
		if (audioFile.exists()) {
			audioFile.delete();
		}
	}
}

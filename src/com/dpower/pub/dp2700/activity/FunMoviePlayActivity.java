package com.dpower.pub.dp2700.activity;

import java.io.File;

import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.tools.ScreenUT;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.util.MyLog;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * @author ZhengZhiying
 * @Funtion ²¥·ÅÊÓÆµ
 */
public class FunMoviePlayActivity extends BaseFragmentActivity 
		implements OnClickListener {
	private static final String TAG = "FunMoviePlayActivity";
	
	private File mFile;
	private VideoView mVideoView;
	private int mPosition = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fun_movie_play);
		mVideoView = (VideoView) findViewById(R.id.video_view);
		findViewById(R.id.btn_back).setOnClickListener(this);
		final String path = getIntent().getExtras().getString("FilePath");
		playVideo(path);
	}
	
	private void playVideo(String path) {
		mFile = new File(path);
		if(mFile.exists()) {
			mVideoView.setVideoPath(mFile.getAbsolutePath());  
			MediaController mediaController = new MediaController(this);  
			mVideoView.setMediaController(mediaController);  
			mediaController.setMediaPlayer(mVideoView);  
			mVideoView.requestFocus();
			mVideoView.start();
        }  
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		MyLog.print(TAG, "movie onResume position " + mPosition);
		mVideoView.resume();
		if(mPosition >= 0) {
//			mVideoView.seekTo(mPosition);
			mPosition = -1;
		}
		findViewById(R.id.root_view).setBackground(
				SPreferences.getInstance().getWallpaper());
		ScreenUT.getInstance().acquireWakeLock();
	}

	@Override
	protected void onPause() {
		super.onPause();
		MyLog.print(TAG, "movie onPause position " + mPosition);
		mPosition = mVideoView.getCurrentPosition();
		mVideoView.stopPlayback();
		ScreenUT.getInstance().releaseWakeLock();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_back:
				mVideoView.pause();
				finish();
				break;
			default:
				break;
		}
	}
}

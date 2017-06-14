package com.dpower.pub.dp2700.tools;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

import com.dpower.util.ConstConf;
import com.dpower.util.MyLog;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Handler;
import android.os.SystemClock;

/**
 * @author ZhengZhiying
 */
public class MediaPlayerTools {
	private static final String TAG = "MediaPlayerTools";
	
	public boolean isPlaying = false;
	private MediaPlayer mPlayer;
	private int mDuration;
	private OnProgressListener mProgressListener;
	private OnEndListener mEndListener;
	private Handler mHandler;
	private boolean mIsLoop = false;
	private boolean mIsRunning = false;

	/**
	 * @param looping 是否循环播放
	 */
	public MediaPlayerTools(boolean looping) {
		mPlayer = new MediaPlayer();
		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mIsLoop = looping;
		mHandler = new Handler();
		setOnErrorListener();
	}

	public void setOnErrorListener() {
		mPlayer.setOnErrorListener(new OnErrorListener() {
			
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				mp.stop();
				mp.release();
				MyLog.print(TAG, "Error what:" + what + " extra:" + extra);
				return false;
			}
		});
	}

	public void playMusic(String filePath) {
		FileInputStream stream = null;
		try {
			release();
			mPlayer = new MediaPlayer();
			File file = new File(filePath);
			stream = new FileInputStream(file);
			FileDescriptor fd = stream.getFD();
			mPlayer.setDataSource(fd);
			mPlayer.prepare();
			mPlayer.setLooping(mIsLoop);
			mDuration = mPlayer.getDuration();

			mPlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					MyLog.print(TAG, "music end");
					isPlaying = false;
					if (mProgressListener != null) {
						mProgressListener.onProgress(mDuration);
					}
					if (mEndListener != null) {
						mEndListener.onCompletion();
					}
				}
			});
			mPlayer.start();
			isPlaying = true;
			startProgressListener();
			MyLog.print(TAG, "music play");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (stream != null) {
					stream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void startProgressListener() {
		if (mProgressListener != null && !mIsRunning) {
			ProcessBarRefresh run = new ProcessBarRefresh();
			new Thread(run).start();
			mIsRunning = true;
			MyLog.print(TAG, "ProcessBarRefresh start");
		}
	}

	class ProcessBarRefresh implements Runnable {

		@Override
		public void run() {
			while (isPlaying) {
				 mHandler.post(new Runnable() {
					 
					 @Override
					 public void run() {
						 if (mPlayer != null) {
							 mProgressListener.onProgress(
									 mPlayer.getCurrentPosition());
						 }
					 }
				 });
				 SystemClock.sleep(500);
			 }
			 mIsRunning = false;
			 MyLog.print(TAG, "ProcessBarRefresh end");
		}
	}
	
	public int getDuration() {
		return mDuration;
	}

	public void release() {
		if (mPlayer != null) {
			isPlaying = false;
			mPlayer.reset();
			mPlayer.release();
			mPlayer = null;
		}
	}

	/**
	 * 暂停播放
	 */
	public void pauseMusic() {
		if (mPlayer != null && mPlayer.isPlaying()) {
			isPlaying = false;
			mPlayer.pause();
		}
	}

	/**
	 * 暂停后继续播放
	 */
	public void continueMusic() {
		if (mPlayer != null) {
			mPlayer.start();
			isPlaying = true;
			startProgressListener();
		}
	}
	
	public void initDefaultRingFile(Context context) {
		if (!new File(ConstConf.RING_PATH + ConstConf.RING_MP3).exists()) {
			File file = new File(ConstConf.RING_PATH);
			if (!file.exists()) {
				file.mkdirs();
			}
			FileOperate.copyToSD(context, 
					ConstConf.RING_PATH + ConstConf.RING_MP3, ConstConf.RING_MP3);
		}
		SPreferences.getInstance().setRingAbsolutePath(
				ConstConf.RING_PATH + ConstConf.RING_MP3);
	}
	
	public interface OnProgressListener {
		public void onProgress(int currentPosition);
	}
	
	public void setOnProgressListener(OnProgressListener progressListener) {
		mProgressListener = progressListener;
	}
	
	public interface OnEndListener {
		public void onCompletion();
	}
	
	public void setOnEndListener(OnEndListener endListener) {
		mEndListener = endListener;
	}
}

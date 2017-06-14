package com.dpower.pub.dp2700.activity.dialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.activity.BaseFragmentActivity;
import com.dpower.pub.dp2700.tools.FileOperate;
import com.dpower.pub.dp2700.tools.MyToast;
import com.dpower.util.ConstConf;
import com.dpower.util.MyLog;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * @author ZhengZhiying
 * @Funtion 设置/留言设置窗口
 */
public class SetLeaveAudioDialog extends BaseFragmentActivity implements OnClickListener {
	private static final String TAG = "SetLeaveAudioDialog";
	
	// AudioName裸音频数据文件
	private static final String AUDIO_NAME = "/sdcard/leavemessage/original.raw";
	// NewAudioName可播放的音频文件
	public static final String NEW_AUDIO_NAME = "/sdcard/leavemessage/new.wav";
	private LinearLayout mInfoWindow;
	private PopupWindow mPopupWindow;
	private TextView mTextLeaveWay;
	private TextView mTextMinute;
	private TextView mTextSecond;
	private ProgressBar mProgressBar;
	private Button mButtonRecord;
	/** 留言方式 0-不留言 1-默认留言 2-业主留言 */
	private int mFlag;
	private MediaPlayer mMediaPlayer;
	private AudioManager mAudioManager;
	private AudioRecord mAudioRecord;
	private Handler mHandler;
	private boolean mIsPlaying = false;
	private boolean mIsRecord = false;
	private boolean mIsPlayRunning = false;
	private boolean mIsRecordRunning = false;
	private int mRecordTime;
	private int mCurrentVolume;
	private Context mContext;

	// 音频获取源
	private int mAudioSource = MediaRecorder.AudioSource.MIC;
	// 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
	private static int mSampleRateInHz = 8000;
	// 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
	@SuppressWarnings("deprecation")
	private static int mChannelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	// 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
	private static int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
	// 缓冲区字节大小
	private int mBufferSizeInBytes = 0;


	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_leave_audio);
		init();
	}

	private void init() {
		mContext = this;
		findViewById(R.id.screen_window).setOnClickListener(this);
		mInfoWindow = (LinearLayout) findViewById(R.id.info_window);
		mInfoWindow.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				MyLog.print(TAG, "mInfoWindow onTouch");
				return true;
			}
		});
		findViewById(R.id.btn_save).setOnClickListener(this);
		findViewById(R.id.layout_leave_message).setOnClickListener(this);
		findViewById(R.id.btn_play).setOnClickListener(this);
		findViewById(R.id.btn_stop).setOnClickListener(this);
		mButtonRecord = (Button) findViewById(R.id.btn_start);
		mButtonRecord.setOnClickListener(this);
		LayoutInflater inflater = getLayoutInflater();
		View view = inflater.inflate(R.layout.popup_leave_message_item, null);
		view.findViewById(R.id.btn_leave_message_no).setOnClickListener(this);
		view.findViewById(R.id.btn_leave_message_default).setOnClickListener(this);
		view.findViewById(R.id.btn_leave_message_user).setOnClickListener(this);
		mPopupWindow = new PopupWindow(view, LayoutParams.WRAP_CONTENT, 
				LayoutParams.WRAP_CONTENT, true);
		mTextLeaveWay = (TextView) findViewById(R.id.tv_leave_message);
		mTextMinute = (TextView) findViewById(R.id.tv_minute);
		mTextSecond = (TextView) findViewById(R.id.tv_second);
		mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
		mFlag = DPFunction.getMessageMode();
		setLeaveWayText();
		setCurrentTime(0);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume,
				AudioManager.FLAG_PLAY_SOUND);
		mHandler = new Handler();
	}
	
	private void setLeaveWayText() {
		switch (mFlag) {
			case 0:
				mTextLeaveWay.setText(getString(R.string.leave_message_no));
				break;
			case 1:
				mTextLeaveWay.setText(getString(R.string.leave_message_default));
				break;
			case 2:
				mTextLeaveWay.setText(getString(R.string.leave_message_user));
				break;
		}
	}
	
	private void startPlayTimeListener() {
		if (!mIsPlayRunning) {
			PlayTimeRefresh run = new PlayTimeRefresh();
			new Thread(run).start();
			mIsPlayRunning = true;
			MyLog.print(TAG, "PlayTimeRefresh start");
		}
	}
	
	class PlayTimeRefresh implements Runnable {

		@Override
		public void run() {
			 while (mIsPlaying) {
				 mHandler.post(new Runnable() {
					 
					 @Override
					 public void run() {
						 if (mMediaPlayer != null) {
							 mProgressBar.setProgress(mMediaPlayer.getCurrentPosition());
							 setCurrentTime(mMediaPlayer.getCurrentPosition() / 1000);
						}
					 }
				 });
				 SystemClock.sleep(500);
			 }
			 mIsPlayRunning = false;
			 MyLog.print(TAG, "PlayTimeRefresh end");
		}
	}
	
	private void startRecordTimeListener() {
		if (!mIsRecordRunning) {
			RecordTimeRefresh run = new RecordTimeRefresh();
			new Thread(run).start();
			mIsRecordRunning = true;
			MyLog.print(TAG, "RecordTimeRefresh start");
		}
	}
	
	class RecordTimeRefresh implements Runnable {

		@Override
		public void run() {
			 while (mIsRecord) {
				 mHandler.post(new Runnable() {
					 
					 @Override
					 public void run() {
						 mRecordTime = mRecordTime + 1;
						 setCurrentTime(mRecordTime / 2);
					 }
				 });
				 SystemClock.sleep(500);
			 }
			 mIsRecordRunning = false;
			 MyLog.print(TAG, "RecordTimeRefresh end");
		}
	}
	
	private void setCurrentTime(int time) {
		if (time < 60) {
			mTextMinute.setText("00");
			if (time < 10) {
				mTextSecond.setText("0" + time);
			} else {
				mTextSecond.setText(String.valueOf(time));
			}

		} else {
			int i, j;
			i = time / 60;
			j = time % 60;

			if (i < 10) {
				mTextMinute.setText("0" + i);
			} else {
				mTextMinute.setText(String.valueOf(i));
			}
			if (j < 10) {
				mTextSecond.setText("0" + j);
			} else {
				mTextSecond.setText(String.valueOf(j));
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.screen_window:
				File temp = new File(NEW_AUDIO_NAME);
				if (temp.exists()) {
					temp.delete();
				}
				File emptyAudio = new File(AUDIO_NAME);
				if (emptyAudio.exists()) {
					emptyAudio.delete();
				}
				finish();
				break;
			case R.id.layout_leave_message:
				mPopupWindow.setTouchable(true);
				mPopupWindow.setFocusable(true);
				mPopupWindow.showAsDropDown(mTextLeaveWay);
				break;
			case R.id.btn_leave_message_no:
				release();
				stopRecord();
				mFlag = 0;
				setLeaveWayText();
				mPopupWindow.dismiss();
				break;
			case R.id.btn_leave_message_default:
				release();
				stopRecord();
				mFlag = 1;
				setLeaveWayText();
				mPopupWindow.dismiss();
				break;
			case R.id.btn_leave_message_user:
				release();
				mFlag = 2;
				setLeaveWayText();
				mPopupWindow.dismiss();
				break;
			case R.id.btn_play:
				play();
				break;
			case R.id.btn_start:
				record();
				break;
			case R.id.btn_stop:
				stop();
				break;
			case R.id.btn_save:
				save();
				break;
			default:
				break;
		}	
	}
	
	public void release() {
		if (mMediaPlayer != null) {
			mIsPlaying = false;
			mMediaPlayer.reset();
			mMediaPlayer.release();
			mMediaPlayer = null;
			mProgressBar.setProgress(0);
			setCurrentTime(0);
		}
	}
	
	private void stopRecord() {
		if (mAudioRecord != null) {
			MyLog.print(TAG, "stop to record......");
			mIsRecord = false;
			mAudioRecord.stop();
			mAudioRecord.release();
			mAudioRecord = null;
			mButtonRecord.setBackgroundResource(R.drawable.bg_leave_message_start_red);
			setCurrentTime(0);
		}
	}
	
	private void play() {
		switch (mFlag) {
			case 0:
				break;
			case 1:
				if (mMediaPlayer == null) {
					mMediaPlayer = MediaPlayer.create(mContext,
							R.raw.leavering1);
					mMediaPlayer.setLooping(false);
					mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
						@Override
						public void onCompletion(MediaPlayer mp) {
							MyLog.print(TAG, "play end");
							release();
						}
					});
					mMediaPlayer.start();
					mProgressBar.setMax(mMediaPlayer.getDuration());
					mIsPlaying = true;
					startPlayTimeListener();
				}
				break;
			case 2:
				if (mIsRecord) { // 正在录音
					MyToast.show(R.string.recording);
					return;
				}
				File file = new File(NEW_AUDIO_NAME);
				String playFile;
				if (!file.exists()) {
					file = new File(ConstConf.USER_LEAVE_PATH);
					if (!file.exists()) {
						MyToast.show(R.string.tips_no_user_message_exit);
						return;
					} else {
						playFile = ConstConf.USER_LEAVE_PATH;
					}
				} else {
					playFile = NEW_AUDIO_NAME;
				}
				if (mMediaPlayer == null) {
					try {
						mMediaPlayer = new MediaPlayer();
						mMediaPlayer.setDataSource(playFile);
						mMediaPlayer.prepare();
						mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
							@Override
							public void onCompletion(MediaPlayer mp) {
								MyLog.print(TAG, "play end");
								release();
							}
						});
						mMediaPlayer.start();
						mProgressBar.setMax(mMediaPlayer.getDuration());
						mIsPlaying = true;
						startPlayTimeListener();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				break;
			default :
				break;
		}
	}
	
	private void record() {
		if (mFlag == 2) {
			if (mIsRecord) { // 正在录音则停止
				stopRecord();
			} else if (mMediaPlayer != null) { // 媒体正在播放
				MyToast.show(R.string.playing_record_audio);
			} else { // 未录音则启动
				try {
					MyLog.print(TAG, "start to record......");
					MyToast.show(R.string.recording_audio);
					File file = new File("/sdcard/leavemessage");
					if (!file.exists()) {
						file.mkdirs();
					}
					creatAudioRecord();
					startRecord();
					mRecordTime = 0;
					mHandler.postDelayed(new Runnable() {
						
						@Override
						public void run() {
							startRecordTimeListener();
						}
					}, 500);
					mButtonRecord.setBackgroundResource(R.drawable.bg_leave_message_start_green);
					Runnable maxTime = new Runnable() {

						@Override
						public void run() {
							if (mIsRecord) {
								stopRecord();
								MyToast.show(R.string.tips_leave_message_overtime);
							}
						}
					};
					mHandler.postDelayed(maxTime, 5 * 60 * 1000);
				} catch (IllegalStateException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void creatAudioRecord() {
		// 获得缓冲区字节大小
		mBufferSizeInBytes = AudioRecord.getMinBufferSize(mSampleRateInHz,
				mChannelConfig, mAudioFormat);
		// 创建AudioRecord对象
		mAudioRecord = new AudioRecord(mAudioSource, mSampleRateInHz,
				mChannelConfig, mAudioFormat, mBufferSizeInBytes);
	}
	
	private void startRecord() {
		mAudioRecord.startRecording();
		mIsRecord = true;
		// 开启音频文件写入线程
		new Thread(new AudioRecordThread()).start();
	}
	
	class AudioRecordThread implements Runnable {
		
		@Override
		public void run() {
			writeDateTOFile();// 往文件中写入裸数据
			copyWaveFile(AUDIO_NAME, NEW_AUDIO_NAME);// 给裸数据加上头文件
		}
	}
	
	/**
	 * 这里将数据写入文件，但是并不能播放，因为AudioRecord获得的音频是原始的裸音频，
	 * 如果需要播放就必须加入一些格式或者编码的头信息。但是这样的好处就是你可以对音频的 裸数据进行处理，比如你要做一个爱说话的TOM
	 * 猫在这里就进行音频的处理，然后重新封装 所以说这样得到的音频比较容易做一些音频的处理。
	 */
	private void writeDateTOFile() {
		// new一个byte数组用来存一些字节数据，大小为缓冲区大小
		byte[] audiodata = new byte[mBufferSizeInBytes];
		FileOutputStream stream = null;
		int readsize = 0;
		try {
			File file = new File(AUDIO_NAME);
			if (file.exists()) {
				file.delete();
			}
			stream = new FileOutputStream(file);
			while (mIsRecord == true) {
				readsize = mAudioRecord.read(audiodata, 0, mBufferSizeInBytes);
				if (AudioRecord.ERROR_INVALID_OPERATION != readsize) {
					stream.write(audiodata);
				}
			}
			stream.flush();
			stream.getFD().sync();
		} catch (Exception e) {
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
	}
	
	// 这里得到可播放的音频文件
	private void copyWaveFile(String inFileName, String outFileName) {
		FileInputStream in = null;
		FileOutputStream out = null;
		long totalAudioLen = 0;
		long totalDataLen = totalAudioLen + 36;
		long longSampleRate = mSampleRateInHz;
		int channels = 1;
		long byteRate = 16 * mSampleRateInHz * channels / 8;
		byte[] data = new byte[mBufferSizeInBytes];
		try {
			in = new FileInputStream(inFileName);
			out = new FileOutputStream(outFileName);
			totalAudioLen = in.getChannel().size();
			totalDataLen = totalAudioLen + 36;
			WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
					longSampleRate, channels, byteRate);
			while (in.read(data) != -1) {
				out.write(data);
			}
			out.flush();
			out.getFD().sync();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 这里提供一个头信息。插入这些信息就可以得到可以播放的文件。 为我为啥插入这44个字节，这个还真没深入研究，不过你随便打开一个wav
	 * 音频的文件，可以发现前面的头文件可以说基本一样哦。每种格式的文件都有 自己特有的头文件。
	 */
	private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
			long totalDataLen, long longSampleRate, int channels, long byteRate)
			throws IOException {
		byte[] header = new byte[44];
		header[0] = 'R'; // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f'; // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16; // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1; // format = 1
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (1 * 16 / 8); // block align
		header[33] = 0;
		header[34] = 16; // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
		out.write(header, 0, 44);
		out.flush();
		out.getFD().sync();
	}
	
	private void stop() {
		switch (mFlag) {
			case 0:
				break;
			case 1:
				release();
				break;
			case 2:
				release();
				stopRecord();
				break;
			default:
				break;
		}
	}

	private void save() {
		release();
		stopRecord();
		if (mFlag == 2) {
			File file = new File(ConstConf.USER_LEAVE_PATH);
			File temp = new File(NEW_AUDIO_NAME);
			if (!file.exists() && !temp.exists()) {
				MyToast.show(R.string.not_record);
				return;
			}
			if (temp.exists()) {
				FileOperate fileOperate = new FileOperate();
				boolean result = fileOperate.from(NEW_AUDIO_NAME)
						.toRomDir(ConstConf.USER_LEAVE_PATH);
				if (!result) {
					MyToast.show(R.string.save_fail);
					return;
				}
				temp.delete();
			}
			File emptyAudio = new File(AUDIO_NAME);
			if (emptyAudio.exists()) {
				emptyAudio.delete();
			}
			file = new File(ConstConf.USER_LEAVE_PATH);
			if (!file.exists()) {
				MyToast.show(R.string.save_fail);
				finish();
				return;
			}
		}
		DPFunction.setMessageMode(mFlag);
		MyToast.show(R.string.save_success);
		finish();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		release();
		stopRecord();
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mCurrentVolume,
				AudioManager.FLAG_PLAY_SOUND);
	}
}

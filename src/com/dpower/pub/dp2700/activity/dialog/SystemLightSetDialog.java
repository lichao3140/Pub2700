package com.dpower.pub.dp2700.activity.dialog;

import com.dpower.function.DPFunction;
import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.activity.BaseActivity;
import com.dpower.util.MyLog;

import android.os.Bundle;
import android.view.DisplayManagerAw;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * ÆÁÄ»ÉèÖÃ´°¿Ú
 */
public class SystemLightSetDialog extends BaseActivity implements OnClickListener {
	private static final String TAG = "SystemLightSetDialog";
	
	private LinearLayout mInfoWindow;
	private SeekBar mSeekBarLight;
	private SeekBar mSeekBarContrast;
	private SeekBar mSeekBarSaturation;
	private SeekBar mSeekBarHue;
	private TextView mTextLight;
	private TextView mTextLContrast;
	private TextView mTextLSaturation;
	private TextView mTextLHue;
	private DisplayManagerAw mDisplayManager;
	private OnSeekBarChangeListener mChangeListener = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if (mSeekBarLight == seekBar) {
				mTextLight.setText(progress + "%");
				if (DPFunction.setBrightness(progress)) {
					mDisplayManager.setDisplayBright(0, progress);
				}
				
			}
			if (mSeekBarContrast == seekBar) {
				mTextLContrast.setText(progress + "%");
				if (DPFunction.setContrast(progress)) {
					mDisplayManager.setDisplayContrast(0, progress);
				}
				
			}
			if (mSeekBarSaturation == seekBar) {
				mTextLSaturation.setText(progress + "%");
				if (DPFunction.setSaturability(progress)) {
					mDisplayManager.setDisplaySaturation(0, progress);
				}
				
			}
			if (mSeekBarHue == seekBar) {
				mTextLHue.setText(progress + "%");
				if (DPFunction.setHue(progress)) {
					mDisplayManager.setDisplayHue(0, progress);
				}
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_system_light_set);
		init();
	}

	private void init() {
		findViewById(R.id.screen_window).setOnClickListener(this);
		mInfoWindow = (LinearLayout) findViewById(R.id.info_window);
		mInfoWindow.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				MyLog.print(TAG, "mInfoWindow onTouch");
				return true;
			}
		});
		mSeekBarLight = (SeekBar) findViewById(R.id.seek_bar_light);
		mSeekBarContrast = (SeekBar) findViewById(R.id.seek_bar_contrast);
		mSeekBarSaturation = (SeekBar) findViewById(R.id.seek_bar_saturation);
		mSeekBarHue = (SeekBar) findViewById(R.id.seek_bar_hue);
		mTextLight = (TextView) findViewById(R.id.tv_light);
		mTextLContrast = (TextView) findViewById(R.id.tv_contrast);
		mTextLSaturation = (TextView) findViewById(R.id.tv_saturation);
		mTextLHue = (TextView) findViewById(R.id.tv_hue);
		mDisplayManager = (DisplayManagerAw) getSystemService("display_aw");
		mTextLight.setText(mDisplayManager.getDisplayBright(0) + "%");
		mTextLContrast.setText(mDisplayManager.getDisplayContrast(0) + "%");
		mTextLSaturation.setText(mDisplayManager.getDisplaySaturation(0) + "%");
		mTextLHue.setText(mDisplayManager.getDisplayHue(0) + "%");
		mSeekBarLight.setProgress(mDisplayManager.getDisplayBright(0));
		mSeekBarContrast.setProgress(mDisplayManager.getDisplayContrast(0));
		mSeekBarSaturation.setProgress(mDisplayManager.getDisplaySaturation(0));
		mSeekBarHue.setProgress(mDisplayManager.getDisplayHue(0));
		mSeekBarLight.setOnSeekBarChangeListener(mChangeListener);
		mSeekBarContrast.setOnSeekBarChangeListener(mChangeListener);
		mSeekBarSaturation.setOnSeekBarChangeListener(mChangeListener);
		mSeekBarHue.setOnSeekBarChangeListener(mChangeListener);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.screen_window:
				finish();
				break;
			default:
				break;
		}
	}
}

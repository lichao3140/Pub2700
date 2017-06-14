package com.dpower.pub.dp2700.activity;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.tools.ImageLoadToos;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * @author ZhengZhiying
 * @Funtion ”È¿÷ÃÏµÿ/œ‡≤·‰Ø¿¿
 */
public class FunPhotoActivity extends BaseFragmentActivity implements OnClickListener {

	private final String SYSTEM_PATH = "system/media/image/";
	private final String SDCARD_PATH = "mnt/sdcard/Pictures/";
	private final String EXTSD_PATH = "mnt/extsd/picture/";
	private ViewPager mPager;
	private List<URI> mDatas = new ArrayList<URI>();
	private ImageLoadToos mImageLoadToos;
	private int mCurrentPosition = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fun_photo);
		mPager = (ViewPager) findViewById(R.id.view_pager);
		mImageLoadToos = ImageLoadToos.getInstance(this);
		initData(mDatas);
		mPager.setAdapter(new ImageAdapter(this));
		if (getIntent().getExtras() == null) {
			findViewById(R.id.btn_confirm).setVisibility(View.GONE);
		} else {
			findViewById(R.id.btn_confirm).setOnClickListener(this);
			((TextView) findViewById(R.id.tv_title)).setText(R.string.text_wallpaper);
		}
		findViewById(R.id.btn_back).setOnClickListener(this);
		setViewPagerListener();
	}
	
	private void initData(List<URI> datas) {
		scanPath(SYSTEM_PATH, datas);
		scanPath(SDCARD_PATH, datas);
		scanPath(EXTSD_PATH, datas);
	}
	
	private void scanPath(String path, List<URI> datas) {
		File file = new File(path);
		if (!file.exists()) {
			return;
		}
		File[] listFiles = file.listFiles();
		if (listFiles == null) {
			return;
		}
		for (int i = 0; i < listFiles.length; i++) {
			if (!listFiles[i].isDirectory()) {
				String name = listFiles[i].getName();
				if (name.endsWith(".jpg") || name.endsWith(".png")
						|| name.endsWith(".mpeg")) {
					datas.add(listFiles[i].toURI());
				}
			}
		}
	}
	
	private void setViewPagerListener() {
		mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				mCurrentPosition = position;
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		findViewById(R.id.root_view).setBackground(
				SPreferences.getInstance().getWallpaper());
	}


	private class ImageAdapter extends PagerAdapter {

		private LayoutInflater mInflater;

		ImageAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public int getCount() {
			return mDatas == null ? 0 : mDatas.size();
		}

		@Override
		public Object instantiateItem(ViewGroup view, int position) {
			View imageLayout = mInflater.inflate(R.layout.adapter_photo_list,
					view, false);
			assert imageLayout != null;
			ImageView imageView = (ImageView) imageLayout.findViewById(R.id.image);
			final ProgressBar spinner = (ProgressBar) imageLayout
					.findViewById(R.id.progress_bar);
			String url = mDatas.get(position).toString()
					.replace("file:/", "file://");
			mImageLoadToos.displayImage(url, imageView, new SimpleImageLoadingListener() {
				
				@Override
				public void onLoadingStarted(String imageUri, View view) {
					spinner.setVisibility(View.VISIBLE);
				}

				@Override
				public void onLoadingFailed(String imageUri, View view,
						FailReason failReason) {
					mImageLoadToos.showErrorTips(failReason.getType());
					spinner.setVisibility(View.GONE);
				}

				@Override
				public void onLoadingComplete(String imageUri,
						View view, Bitmap loadedImage) {
					spinner.setVisibility(View.GONE);
				}
			});
			view.addView(imageLayout, 0);
			return imageLayout;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_back:
				finish();
				break;
			case R.id.btn_confirm:
				SPreferences.getInstance().saveWallpaper(
						new File(mDatas.get(mCurrentPosition)).getAbsolutePath());
				finish();
				break;
			default:
				break;
		}
	}
}

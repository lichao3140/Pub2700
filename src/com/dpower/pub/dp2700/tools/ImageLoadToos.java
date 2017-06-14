package com.dpower.pub.dp2700.tools;

import com.dpower.pub.dp2700.R;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason.FailType;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * @author ZhengZhiying
 * @Funtion 图片管理类
 */
public class ImageLoadToos {

	private DisplayImageOptions mOptions;
	private static ImageLoadToos mImageLoadToos;

	public static ImageLoadToos getInstance(Context context) {
		if (mImageLoadToos == null) {
			mImageLoadToos = new ImageLoadToos(context);
		}
		return mImageLoadToos;
	}

	private ImageLoadToos(Context context) {
		initImageLoader(context);
	}

	private void initImageLoader(Context context) {
		ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration
				.Builder(context);
		config.threadPriority(Thread.NORM_PRIORITY - 2);
		config.denyCacheImageMultipleSizesInMemory();
		config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
		config.diskCacheSize(50* 1024 * 1024); // 50 MiB
		config.tasksProcessingOrder(QueueProcessingType.LIFO);
		config.writeDebugLogs(); // 使用发行版时，请注释这句话
		//使用configuration 初始化 ImageLoader 对象 .
		ImageLoader.getInstance().init(config.build());
	}

	public DisplayImageOptions getOpinions() {
		if (mOptions == null) {
			mOptions = new DisplayImageOptions.Builder()
					.showImageForEmptyUri(R.drawable.ic_empty)
					.showImageOnFail(R.drawable.ic_error)
					.resetViewBeforeLoading(true).cacheOnDisk(true)
					.imageScaleType(ImageScaleType.EXACTLY)
					.bitmapConfig(Bitmap.Config.RGB_565)
					.considerExifParams(true)
					.displayer(new FadeInBitmapDisplayer(300)).build();
		}
		return mOptions;
	}

	public void displayImage(String uri, ImageView imageView,
			ImageLoadingListener listener) {
		ImageLoader.getInstance().displayImage(uri, imageView, 
				getOpinions(), listener);
	}

	public void showErrorTips(FailType failType) {
		String message = null;
		switch (failType) {
			case IO_ERROR:
				message = "Input/Output error";
				break;
			case DECODING_ERROR:
				message = "Image can't be decoded";
				break;
			case NETWORK_DENIED:
				message = "Downloads are denied";
				break;
			case OUT_OF_MEMORY:
				message = "Out Of Memory error";
				break;
			case UNKNOWN:
				message = "Unknown error";
				break;
		}
		MyToast.show(message);
	}
}

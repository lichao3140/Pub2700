package com.dpower.pub.dp2700.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.adapter.MoviesListAdapter;
import com.dpower.pub.dp2700.tools.SPreferences;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * @author ZhengZhiying
 * @Funtion ”∞ ”–¿…Õ
 */
public class FunMovieActivity extends BaseFragmentActivity implements OnClickListener {
	
	private final String EXTSD_PATH = "mnt/extsd/movie/";
	private final String STORAGE_PATH = Environment.getExternalStorageDirectory()
			.getPath() + "/Ringtones/";
	private final String SYSTEM_PATH = "system/media/backup/Video/";
	private ListView mMovieList;
	private MoviesListAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fun_movie);
		findViewById(R.id.btn_back).setOnClickListener(this);
		mMovieList = (ListView) findViewById(R.id.movie_list);
		mAdapter = new MoviesListAdapter(this);
		mAdapter.files = scanVideoFileList();
		mMovieList.setAdapter(mAdapter);
		setListViewListener();
	}
	
	private List<File> scanVideoFileList() {
		List<File> sdcardRings = scanVideoFileListIn(STORAGE_PATH);
		List<File> systemRings = scanVideoFileListIn(SYSTEM_PATH);
		List<File> extSd = scanVideoFileListIn(EXTSD_PATH);
		sdcardRings.addAll(systemRings);
		sdcardRings.addAll(extSd);
		return sdcardRings;
	}
	
	private List<File> scanVideoFileListIn(String path) {
		File file = new File(path);
		List<File> files = new ArrayList<File>();
		if (!file.exists()) {
			return files;
		}
		File[] subFile = file.listFiles();
		files.clear();
		if (subFile == null) {
			return files;
		}
		for (int i = 0; i < subFile.length; i++) {
			if (!subFile[i].isDirectory()) {
				String name = subFile[i].getName();
				if (name.trim().toLowerCase().endsWith(".mp4")
						|| name.trim().toLowerCase().endsWith(".avi")
						|| name.trim().toLowerCase().endsWith(".3gp")) {
					files.add(subFile[i]);
				}
			}
		}
		return files;
	}
	
	private void setListViewListener() {
		mMovieList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent();
				intent.putExtra("FilePath", mAdapter.files.get(position)
						.getPath());
				intent.setClass(FunMovieActivity.this, FunMoviePlayActivity.class);
				startActivity(intent);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		findViewById(R.id.root_view).setBackground(
				SPreferences.getInstance().getWallpaper());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_back:
				finish();
				break;
			default:
				break;
		}
	}
}

package com.dpower.pub.dp2700.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

/**
 * @author ZhengZhiying
 * @Funtion 异常处理类
 */
public class ExceptionActivity extends BaseActivity {
	
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		TextView text = new TextView(mContext);
		setContentView(text);
		startNewActicity();
		finish();
	}

	protected void startNewActicity() {
        Intent intent = new Intent(mContext, MainActivity.class); 
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
        		Intent.FLAG_ACTIVITY_NEW_TASK); 
        mContext.startActivity(intent);
	}
}

package com.dpower.pub.dp2700.activity;

import java.lang.reflect.Method;

import com.dpower.pub.dp2700.application.App;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class BaseFragmentActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		App.getInstance().addActivity(this);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		View rootView = findViewById(android.R.id.content);
		setShowSoftInputOnFocus(rootView);
	}
	
	private void setShowSoftInputOnFocus(View view) {
		if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i= 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                if (child instanceof EditText) {
                	Class<EditText> cls = EditText.class;
            		Method setShowSoftInputOnFocus;
            		try {
            			setShowSoftInputOnFocus = cls.getMethod(
            					"setShowSoftInputOnFocus", boolean.class);
            			setShowSoftInputOnFocus.setAccessible(true);
            			setShowSoftInputOnFocus.invoke(child, false);
            		} catch (Exception e) {
            			e.printStackTrace();
            		}
                } else if (child instanceof ViewGroup) {
                	setShowSoftInputOnFocus(child);
                }
            }
        }
	}

	@Override
	protected void onDestroy() {
		App.getInstance().removeActivity(this);
		super.onDestroy();
	}
}

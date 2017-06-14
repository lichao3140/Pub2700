package com.dpower.pub.dp2700.dialog;

import com.dpower.pub.dp2700.R;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class TipsDialog extends Dialog implements View.OnClickListener {
	
	private TextView mContent;
	private Button mButtonConfirm;
	private OnDialogClickListener mListener;
	private Context mContext;

	public TipsDialog(Context context) {
		super(context, R.style.style_dialog);
		setContentView(R.layout.dialog_tips);
		mContext = context;
		mContent = (TextView) findViewById(R.id.tv_content);
		findViewById(R.id.btn_cancel).setOnClickListener(this);
		mButtonConfirm = (Button) findViewById(R.id.btn_confirm);
		mButtonConfirm.setOnClickListener(this);
		getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
	}
	
	public void setContent(String text) {
		mContent.setText(text);
	}
	
	public void setContent(int resid) {
		mContent.setText(resid);
	}
	
	public void setTextColor(int resid) {
		mContent.setTextColor(mContext.getResources().getColor(resid));
	}
	
	public void setOnClickListener(OnDialogClickListener l) {
		mListener = l;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_confirm:
				dismiss();
				if (mListener != null) {
					mListener.onClick();
				}
				break;
			case R.id.btn_cancel:
				dismiss();
				break;
			default:
				break;
		}
	}
	
	public interface OnDialogClickListener {
		public void onClick();
	}
}

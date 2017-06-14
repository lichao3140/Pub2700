package com.dpower.pub.dp2700.tools;

import android.widget.EditText;

public class EditTextTool {
	
	private static EditTextTool mEditTextTool;
	private EditText mEditText;

	private EditTextTool() {
		
	}

	public static EditTextTool getInstance() {
		if (mEditTextTool == null) {
			mEditTextTool = new EditTextTool();
		}
		return mEditTextTool;
	}

	public void setEditText(EditText editText) {
		mEditText = editText;
	}

	public EditText getEditText() {
		return mEditText;
	}

	public void appendTextTo(String letter) {
		if (mEditText == null) {
			return;
		}
		if (!mEditText.hasFocus()) {
			return;
		}
		int index = mEditText.getSelectionEnd();
		String content = mEditText.getText().toString();
		String strHead = content.substring(0, index);
		String strTail = content.substring(index, content.length());
		mEditText.setText(strHead);
		mEditText.append(letter);
		mEditText.append(strTail);
		mEditText.setSelection(index + 1);
	}

	public void deleteText() {
		if (mEditText == null) {
			return;
		}
		if (!mEditText.hasFocus()) {
			return;
		}
		String content = mEditText.getText().toString();
		if (content.isEmpty()) {
			return;
		}
		int index = mEditText.getSelectionEnd();
		if (index == 0) {
			return;
		}
		String strHead = content.substring(0, index - 1);
		String strTail = content.substring(index, content.length());
		mEditText.setText(strHead);
		mEditText.append(strTail);
		index--;
		mEditText.setSelection(index < 0 ? 0 : index);
	}
}

package com.dpower.dpsiplib.service;

import java.io.Serializable;

import org.pjsip.pjsua2.AudioMedia;
import org.pjsip.pjsua2.Call;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallMediaInfo;
import org.pjsip.pjsua2.CallMediaInfoVector;
import org.pjsip.pjsua2.Media;
import org.pjsip.pjsua2.OnCallMediaStateParam;
import org.pjsip.pjsua2.OnCallStateParam;
import org.pjsip.pjsua2.VideoWindow;
import org.pjsip.pjsua2.pjmedia_type;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsua2;
import org.pjsip.pjsua2.pjsua_call_media_status;

import com.dpower.dpsiplib.utils.SIPIntercomLog;

public class MyCall extends Call implements Serializable {
	
	public VideoWindow videoWindow;
	public AudioMedia audioMedia;
	public int micLevel = -1;
	public int volumeLevel = -1;
	private static final long serialVersionUID = -6470574927973900913L;

	MyCall(MyAccount account, int call_id) {
		super(account, call_id);
		videoWindow = null;
	}

	@Override
	public void onCallState(OnCallStateParam prm) {
		if (MyApp.callback != null) {
			MyApp.callback.notifyCallState(this);
		}
		try {
			CallInfo callInfo = getInfo();
			if (callInfo.getState() == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
				SIPIntercomLog.print("onCallState hangup");
				dump(true, " ");
				this.delete();
			}
		} catch (Exception e) {
			return;
		}
	}

	@Override
	public void onCallMediaState(OnCallMediaStateParam prm) {
		CallInfo callInfo;
		try {
			callInfo = getInfo();
		} catch (Exception e) {
			return;
		}
		CallMediaInfoVector cmiv = callInfo.getMedia();
		for (int i = 0; i < cmiv.size(); i++) {
			CallMediaInfo cmi = cmiv.get(i);
			if (cmi.getType() == pjmedia_type.PJMEDIA_TYPE_AUDIO
					&& (cmi.getStatus() == pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE || cmi
							.getStatus() == pjsua_call_media_status.PJSUA_CALL_MEDIA_REMOTE_HOLD)) {
				// unfortunately, on Java too, the returned Media cannot be
				// downcasted to AudioMedia
				Media m = getMedia(i);
				audioMedia = AudioMedia.typecastFromMedia(m);
				if (micLevel != -1) {
					try {
						audioMedia.adjustRxLevel(micLevel);
						SIPIntercomLog.print(SIPIntercomLog.ERROR, "设置麦克风：设置麦克风：" + micLevel);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (volumeLevel != -1) {
					try {
						audioMedia.adjustTxLevel(volumeLevel);
						SIPIntercomLog.print(SIPIntercomLog.ERROR, "设置音频：设置音频：" + volumeLevel);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				// connect ports
				try {
					MyApp.endpoint.audDevManager().getCaptureDevMedia()
							.startTransmit(audioMedia);
					audioMedia.startTransmit(MyApp.endpoint.audDevManager()
							.getPlaybackDevMedia());
				} catch (Exception e) {
					continue;
				}
			} else if (cmi.getType() == pjmedia_type.PJMEDIA_TYPE_VIDEO
					&& cmi.getStatus() == pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE
					&& cmi.getVideoIncomingWindowId() != pjsua2.INVALID_ID) {
				videoWindow = new VideoWindow(cmi.getVideoIncomingWindowId());
			}
		}
		if (MyApp.callback != null) {
			MyApp.callback.notifyCallMediaState(this);
		}
	}
}

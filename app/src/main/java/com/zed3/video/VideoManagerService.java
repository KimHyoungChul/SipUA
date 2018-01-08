package com.zed3.video;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.zed3.addressbook.DataBaseService;
import com.zed3.addressbook.Member.UserType;
import com.zed3.groupcall.GroupCallUtil;
import com.zed3.log.MyLog;
import com.zed3.sipua.CallManager;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.UserAgent.PttPRMode;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.UserConfrimActivity;
import com.zed3.sipua.ui.UserConfrimActivity.OnButtonClickListener;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.toast.MyToast;

import org.zoolu.sip.call.ExtendedCall;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class VideoManagerService {
	public static final String ACTION_AUDIO_CALL = "com.zed3.action.AUDIO_CALL";
	public static final String ACTION_MY_NOT_ANY_CALL = "com.zed3.action.my.not.ANY_CALL";
	public static final String ACTION_MY_NOT_AUDIO_CALL = "com.zed3.action.my.not.AUDIO_CALL";
	public static final String ACTION_MY_NOT_VIDEO_CALL = "com.zed3.action.my.not.VIDEO_CALL";
	public static final String ACTION_OTHER_NOT_AUDIO_CALL = "com.zed3.action.other.not.AUDIO_CALL";
	public static final String ACTION_OTHER_NOT_VIDEO_CALL = "com.zed3.action.other.not.VIDEO_CALL";
	public static final String ACTION_PTT_DOWN = "com.zed3.action.PTT_DOWN";
	public static final String ACTION_PTT_UP = "com.zed3.action.PTT_UP";
	public static final String ACTION_VIDEO_CALL = "com.zed3.action.VIDEO_CALL";
	public static final String ACTION_VIDEO_DISPATCH = "com.zed3.action.VIDEO_DISPATCH";
	public static final String ACTION_VIDEO_MONITOR = "com.zed3.action.VIDEO_MONITOR";
	public static final String ACTION_VIDEO_TRANSCRIBE = "com.zed3.action.VIDEO_TRANSCRIBE";
	public static final String ACTION_VIDEO_UPLOAD = "com.zed3.action.VIDEO_UPLOAD";
	public static final boolean DEFAULT_SUSPEND_RECEIVE_VIDEO_VALUE = false;
	public static final boolean DEFAULT_SUSPEND_SEND_VIDEO_VALUE = false;
	public static final String EMPTY = "";
	public static final String EXTRA_SUSPEND_RECEIVE_VIDEO = "com.zed3.extra.SUSPEND_RECIEVE_VIDEO";
	public static final String EXTRA_SUSPEND_SEND_VIDEO = "com.zed3.extra.SUSPEND_SEND_VIDEO";
	public static final String EXTRA_VIDEO_ACTION = "com.zed3.extra.VIDEO_ACTION";
	public static final String EXTRA_VIDEO_NUMBER = "com.zed3.extra.VIDEO_NUMBER";
	public static final String EXTRA_VIDEO_TITLE = "com.zed3.extra.VIDEO_TITLE";
	private static final String LOG_TAG = VideoManagerService.class.getSimpleName();
	public static final int TYPE_SINGLE_AUDIO_CALL = 0;
	public static final int TYPE_VIDEO_CALL = 1;
	public static volatile boolean bye = false;
	private static VideoManagerService sDefault;
	private String mActionSplit = "$";
	private VideoParamter mCacheVideoParameter;
	private Context mContext;
	private String mCurrentVideoAction = "";
	private int mCurrentVideoParameter = -1;
	private Handler mHandler = new Handler();
	private List<EndVideoCallHandler> mHandlers = new ArrayList();
	private boolean mIsReceiveVideoData = true;
	private boolean mIsSendVideoData = true;
	private OnEndVideosCompleted mOnEndVideosCompleted;
	private VideoParamter mRemoteVideoParamter = null;

	class C13331 implements Runnable {
		C13331() {
		}

		public void run() {
			if (VideoManagerService.this.mOnEndVideosCompleted != null) {
				VideoManagerService.this.mOnEndVideosCompleted.onCompleted();
			}
		}
	}

	public interface EndVideoCallHandler {
		void handle();
	}

	public interface OnEndVideosCompleted {
		void onCompleted();
	}

	private static final class UserNumberRecognizer {
		public static final String CONTROL_NUMBER = "number_control";
		public static final String GQT_NUMBER = "number_GQT";
		public static final String GTS_NUMBER = "number_GTS";
		public static final String GVS_NUMBER = "number_GVS";
		private static final HashMap<String, String[]> sNumberMap = new HashMap();

		private UserNumberRecognizer() {
		}

		public static void init() {
			sNumberMap.clear();
		}

		public static String[] getSupportActions(String number, String isVideoBut) {
			String typekeyString = getNumberTypeString(number, isVideoBut);
			MyLog.e("showVideoSelectDialog", new StringBuilder(String.valueOf(typekeyString)).append("    getSupportActionsgetSupportActions    ").append(((String[]) sNumberMap.get(typekeyString))[0]).toString());
			return (String[]) sNumberMap.get(typekeyString);
		}

		private static String getNumberTypeString(String number, String isVideoBut) {
			String type = DataBaseService.getInstance().getMemberType(number);
			String audiotype = DataBaseService.getInstance().getMemberAudioType(number);
			String videotype = DataBaseService.getInstance().getMemberVideoType(number);
			MyLog.e("showVideoSelectDialog", "getNumberTypeString11111111111");
			ArrayList<String> array;
			if (TextUtils.isEmpty(type)) {
				MyLog.e("showVideoSelectDialog", "getNumberTypeString22222222222");
				if (isVideoBut == null || !isVideoBut.equals("videobut")) {
					array = new ArrayList();
					if (DeviceInfo.CONFIG_SUPPORT_AUDIO_SINGLE) {
						array.add(VideoManagerService.ACTION_AUDIO_CALL);
					}
					if (DeviceInfo.CONFIG_SUPPORT_VIDEO_SINGLE) {
						array.add(VideoManagerService.ACTION_VIDEO_CALL);
					}
					if (DeviceInfo.CONFIG_VIDEO_UPLOAD == 1) {
						array.add(VideoManagerService.ACTION_VIDEO_UPLOAD);
					}
					if (DeviceInfo.CONFIG_VIDEO_MONITOR == 1) {
						array.add(VideoManagerService.ACTION_VIDEO_MONITOR);
					}
					if (array.size() > 0) {
						sNumberMap.put(GQT_NUMBER, (String[]) array.toArray(new String[0]));
					} else {
						sNumberMap.put(GQT_NUMBER, new String[]{VideoManagerService.ACTION_MY_NOT_ANY_CALL});
					}
				} else {
					array = new ArrayList();
					if (DeviceInfo.CONFIG_SUPPORT_VIDEO_SINGLE) {
						array.add(VideoManagerService.ACTION_VIDEO_CALL);
					}
					if (DeviceInfo.CONFIG_VIDEO_UPLOAD == 1) {
						array.add(VideoManagerService.ACTION_VIDEO_UPLOAD);
					}
					if (DeviceInfo.CONFIG_VIDEO_MONITOR == 1) {
						array.add(VideoManagerService.ACTION_VIDEO_MONITOR);
					}
					if (array.size() > 0) {
						sNumberMap.put(GQT_NUMBER, (String[]) array.toArray(new String[0]));
					} else {
						sNumberMap.put(GQT_NUMBER, new String[]{VideoManagerService.ACTION_MY_NOT_VIDEO_CALL});
					}
				}
				return GQT_NUMBER;
			} else if (UserType.toUserType(type) == UserType.MOBILE_GQT) {
				MyLog.e("showVideoSelectDialog", "MOBILE_GQTMOBILE_GQT");
				if (isVideoBut == null || !isVideoBut.equals("videobut")) {
					array = new ArrayList();
					if (!TextUtils.isEmpty(audiotype) && audiotype.equals("1") && DeviceInfo.CONFIG_SUPPORT_AUDIO_SINGLE) {
						array.add(VideoManagerService.ACTION_AUDIO_CALL);
					}
					if (!TextUtils.isEmpty(videotype) && videotype.equals("1")) {
						if (DeviceInfo.CONFIG_SUPPORT_VIDEO_SINGLE) {
							array.add(VideoManagerService.ACTION_VIDEO_CALL);
						}
						if (DeviceInfo.CONFIG_VIDEO_UPLOAD == 1) {
							array.add(VideoManagerService.ACTION_VIDEO_UPLOAD);
						}
						if (DeviceInfo.CONFIG_VIDEO_MONITOR == 1) {
							array.add(VideoManagerService.ACTION_VIDEO_MONITOR);
						}
					}
					if (array.size() > 0) {
						sNumberMap.put(GQT_NUMBER, (String[]) array.toArray(new String[0]));
					} else {
						sNumberMap.put(GQT_NUMBER, new String[]{VideoManagerService.ACTION_MY_NOT_ANY_CALL});
					}
				} else {
					array = new ArrayList();
					if (!TextUtils.isEmpty(videotype) && videotype.equals("1")) {
						if (DeviceInfo.CONFIG_SUPPORT_VIDEO_SINGLE) {
							array.add(VideoManagerService.ACTION_VIDEO_CALL);
						}
						if (DeviceInfo.CONFIG_VIDEO_UPLOAD == 1) {
							array.add(VideoManagerService.ACTION_VIDEO_UPLOAD);
						}
						if (DeviceInfo.CONFIG_VIDEO_MONITOR == 1) {
							array.add(VideoManagerService.ACTION_VIDEO_MONITOR);
						}
					}
					if (array.size() > 0) {
						sNumberMap.put(GQT_NUMBER, (String[]) array.toArray(new String[0]));
					} else {
						sNumberMap.put(GQT_NUMBER, new String[]{VideoManagerService.ACTION_MY_NOT_VIDEO_CALL});
					}
				}
				return GQT_NUMBER;
			} else if (UserType.toUserType(type) == UserType.VIDEO_MONITOR_GVS || UserType.toUserType(type) == UserType.VIDEO_MONITOR_GB28181) {
				MyLog.e("showVideoSelectDialog", "VIDEO_MONITOR_GVSVIDEO_MONITOR_GVS");
				if (DeviceInfo.CONFIG_VIDEO_MONITOR == 1) {
					sNumberMap.put(GVS_NUMBER, new String[]{VideoManagerService.ACTION_VIDEO_MONITOR});
				} else {
					sNumberMap.put(GVS_NUMBER, new String[]{VideoManagerService.ACTION_MY_NOT_VIDEO_CALL});
				}
				return GVS_NUMBER;
			} else if (UserType.toUserType(type) == UserType.SVP) {
				MyLog.e("showVideoSelectDialog", "SVPSVPSVPSVP");
				if (isVideoBut == null || !isVideoBut.equals("videobut")) {
					array = new ArrayList();
					if (!TextUtils.isEmpty(audiotype) && audiotype.equals("1") && DeviceInfo.CONFIG_SUPPORT_AUDIO_SINGLE) {
						array.add(VideoManagerService.ACTION_AUDIO_CALL);
					}
					if (!TextUtils.isEmpty(videotype) && videotype.equals("1")) {
						if (DeviceInfo.CONFIG_SUPPORT_VIDEO_SINGLE) {
							array.add(VideoManagerService.ACTION_VIDEO_CALL);
						}
						if (DeviceInfo.CONFIG_VIDEO_UPLOAD == 1) {
							array.add(VideoManagerService.ACTION_VIDEO_UPLOAD);
						}
					}
					if (array.size() > 0) {
						sNumberMap.put(CONTROL_NUMBER, (String[]) array.toArray(new String[0]));
					} else {
						sNumberMap.put(CONTROL_NUMBER, new String[]{VideoManagerService.ACTION_MY_NOT_ANY_CALL});
					}
				} else {
					array = new ArrayList();
					if (!TextUtils.isEmpty(videotype) && videotype.equals("1")) {
						if (DeviceInfo.CONFIG_SUPPORT_VIDEO_SINGLE) {
							array.add(VideoManagerService.ACTION_VIDEO_CALL);
						}
						if (DeviceInfo.CONFIG_VIDEO_UPLOAD == 1) {
							array.add(VideoManagerService.ACTION_VIDEO_UPLOAD);
						}
					}
					if (array.size() > 0) {
						sNumberMap.put(CONTROL_NUMBER, (String[]) array.toArray(new String[0]));
					} else {
						sNumberMap.put(CONTROL_NUMBER, new String[]{VideoManagerService.ACTION_MY_NOT_VIDEO_CALL});
					}
				}
				return CONTROL_NUMBER;
			} else if (UserType.toUserType(type) == UserType.GTS) {
				if (DeviceInfo.CONFIG_SUPPORT_AUDIO_SINGLE) {
					sNumberMap.put(GTS_NUMBER, new String[]{VideoManagerService.ACTION_AUDIO_CALL});
				} else {
					sNumberMap.put(GTS_NUMBER, new String[]{VideoManagerService.ACTION_MY_NOT_AUDIO_CALL});
				}
				return GTS_NUMBER;
			} else {
				if (DeviceInfo.CONFIG_SUPPORT_VIDEO_SINGLE && DeviceInfo.CONFIG_SUPPORT_AUDIO_SINGLE) {
					sNumberMap.put(GTS_NUMBER, new String[]{VideoManagerService.ACTION_AUDIO_CALL});
				} else {
					sNumberMap.put(GTS_NUMBER, new String[]{VideoManagerService.ACTION_MY_NOT_AUDIO_CALL});
				}
				MyLog.e("showVideoSelectDialog", "getNumberTypeString7777777777777");
				return GTS_NUMBER;
			}
		}

		public static boolean isGQTNumber(String number) {
			String type = DataBaseService.getInstance().getMemberType(number);
			if (TextUtils.isEmpty(type) && type.equals("GQT")) {
				return true;
			}
			return false;
		}

		public static boolean isGVSNumber(String number) {
			String type = DataBaseService.getInstance().getMemberType(number);
			if (TextUtils.isEmpty(type) && type.equals("GVS")) {
				return true;
			}
			return false;
		}

		public static boolean isControlNumber(String number) {
			String type = DataBaseService.getInstance().getMemberType(number);
			if (TextUtils.isEmpty(type) && type.equals("Console")) {
				return true;
			}
			return false;
		}
	}

	public static final class VideoSelectDialogBuilder extends Builder {

		class C13341 implements OnKeyListener {
			C13341() {
			}

			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				switch (keyCode) {
					case 4:
						dialog.dismiss();
						break;
				}
				return false;
			}
		}

		class C13352 implements OnClickListener {
			private final /* synthetic */ String[] val$items;
			private final /* synthetic */ OnVideoSelectItemClickListener val$lis;

			C13352(OnVideoSelectItemClickListener onVideoSelectItemClickListener, String[] strArr) {
				this.val$lis = onVideoSelectItemClickListener;
				this.val$items = strArr;
			}

			public void onClick(DialogInterface dialog, int which) {
				if (this.val$lis != null) {
					this.val$lis.onActionClicked(this.val$items[which]);
				}
			}
		}

		public static abstract class OnVideoSelectItemClickListener {
			public void onAudioConnectionClicked() {
			}

			public void onVideoConnectionClicked() {
			}

			public void onVideoUploadClicked() {
			}

			public void onVideoMonitorClicked() {
			}

			public final void onActionClicked(String action) {
				if (VideoManagerService.getDefault().getActionString(VideoManagerService.ACTION_AUDIO_CALL).equals(action)) {
					onAudioConnectionClicked();
				} else if (VideoManagerService.getDefault().getActionString(VideoManagerService.ACTION_VIDEO_CALL).equals(action)) {
					onVideoConnectionClicked();
				} else if (VideoManagerService.getDefault().getActionString(VideoManagerService.ACTION_VIDEO_UPLOAD).equals(action)) {
					onVideoUploadClicked();
				} else if (VideoManagerService.getDefault().getActionString(VideoManagerService.ACTION_VIDEO_MONITOR).equals(action)) {
					onVideoMonitorClicked();
				}
			}
		}

		public VideoSelectDialogBuilder(Context arg0) {
			super(arg0);
		}

		public static VideoSelectDialogBuilder buildSelf(Context context, OnVideoSelectItemClickListener lis, String loginUserNumber, String isVideoBut) {
			String[] supportActions = UserNumberRecognizer.getSupportActions(loginUserNumber, isVideoBut);
			if (supportActions == null || supportActions.length == 0) {
				return null;
			}
			if (supportActions.length == 1) {
				if (supportActions[0].equals(VideoManagerService.ACTION_MY_NOT_AUDIO_CALL)) {
					MyToast.showToast(true, SipUAApp.mContext, (int) R.string.vc_service_not);
					return null;
				} else if (supportActions[0].equals(VideoManagerService.ACTION_MY_NOT_VIDEO_CALL)) {
					MyToast.showToast(true, SipUAApp.mContext, (int) R.string.ve_service_not);
					return null;
				} else if (supportActions[0].equals(VideoManagerService.ACTION_OTHER_NOT_AUDIO_CALL)) {
					MyToast.showToast(true, SipUAApp.mContext, (int) R.string.audio_service_not);
					return null;
				} else if (supportActions[0].equals(VideoManagerService.ACTION_OTHER_NOT_VIDEO_CALL)) {
					MyToast.showToast(true, SipUAApp.mContext, (int) R.string.vedio_service_not);
					return null;
				} else if (supportActions[0].equals(VideoManagerService.ACTION_MY_NOT_ANY_CALL)) {
					MyToast.showToast(true, SipUAApp.mContext, (int) R.string.not_any_call);
					return null;
				}
			}
			int len = supportActions.length;
			String[] items = new String[len];
			for (int i = 0; i < len; i++) {
				String actionString = VideoManagerService.getDefault().getActionString(supportActions[i]);
				System.out.println("actionString " + actionString);
				items[i] = actionString;
			}
			VideoSelectDialogBuilder dialog = new VideoSelectDialogBuilder(context);
//			dialog.setOnKeyListener(new C13341());
//			dialog.setItems(items, new C13352(lis, items));
			return dialog;
		}
	}

	public enum VideoType {
		VIDEO_CALL,
		VIDEO_MONITOR,
		VIDEO_UPLOAD
	}

	public static VideoManagerService getDefault() {
		if (sDefault == null) {
			sDefault = new VideoManagerService();
		}
		return sDefault;
	}

	public void cacheVideoPamerater(VideoParamter videoParamter) {
		this.mCacheVideoParameter = videoParamter;
	}

	public VideoParamter getCacheVideParameter() {
		return this.mCacheVideoParameter;
	}

	public void init(Context context) {
		this.mContext = context;
		UserNumberRecognizer.init();
	}

	public boolean sendIntent(Intent intent) {
		return dispatch(intent);
	}

	public synchronized boolean dispatch(Intent intent) {
		boolean z;
		String action = intent.getAction();
		if (inteceptIntent(intent)) {
			z = false;
		} else {
			this.mCurrentVideoAction = intent.getAction();
			if (ACTION_VIDEO_CALL.equals(action)) {
				onVideoCall(intent);
			} else if (ACTION_VIDEO_UPLOAD.equals(action)) {
				onVideoUpload(intent);
			} else if (ACTION_VIDEO_MONITOR.equals(action)) {
				onVideoMonitor(intent);
			} else if (ACTION_VIDEO_TRANSCRIBE.equals(action)) {
				onVideoTranscribe(intent);
			}
			z = true;
		}
		return z;
	}

	public void onPttUp(Intent intent) {
		GroupCallUtil.makeGroupCall(false, false, PttPRMode.Idle);
	}

	public void onPttDown(Intent intent) {
		GroupCallUtil.makeGroupCall(true, false, PttPRMode.SideKeyPress);
	}

	public synchronized void suspendReceiveVideoData() {
		this.mIsReceiveVideoData = false;
	}

	public synchronized void suspendSendVideoData() {
		this.mIsSendVideoData = false;
	}

	public synchronized void resumeReceiveVideoData() {
		this.mIsReceiveVideoData = true;
	}

	public synchronized void resumeSendVideoData() {
		this.mIsSendVideoData = true;
	}

	public boolean isRecieveVideoData() {
		return this.mIsReceiveVideoData;
	}

	public boolean isSendVideoData() {
		return this.mIsSendVideoData;
	}

	private void setVideoMonitor() {
		resumeReceiveVideoData();
		suspendSendVideoData();
		this.mCurrentVideoParameter = VideoParamter.obtain().setVideoMonitor(true).build();
	}

	private void onVideoMonitor(Intent intent) {
		setVideoMonitor();
		startVideo(intent);
	}

	private void setVideoUpload() {
		suspendReceiveVideoData();
		resumeSendVideoData();
		this.mCurrentVideoParameter = VideoParamter.obtain().setVideoUpload(true).build();
	}

	private void onVideoUpload(Intent intent) {
		setVideoUpload();
		startVideo(intent);
	}

	private void onVideoTranscribe(Intent intent) {
		setVideoUpload();
		Receiver.engine(this.mContext).isMakeVideoTRANSCRIBE = 1;
		startVideo(intent);
	}

	private void setVideoCall() {
		resumeReceiveVideoData();
		resumeSendVideoData();
		this.mCurrentVideoParameter = VideoParamter.obtain().setVideoCall(true).build();
	}

	private void onVideoCall(Intent intent) {
		setVideoCall();
		startVideo(intent);
	}

	private void startVideo(Intent intent) {
		CallUtil.startVideoCall(this.mContext, intent.getStringExtra(EXTRA_VIDEO_NUMBER), null);
	}

	public void handleRemoteVideoParamter(VideoParamter videoParamter) {
		MyLog.d(LOG_TAG, "VMS#setVideoParameter() videoParamter = " + videoParamter.getParameterString());
		this.mRemoteVideoParamter = videoParamter;
		if (videoParamter.isVideoCall()) {
			this.mCurrentVideoAction = ACTION_VIDEO_CALL;
			resumeReceiveVideoData();
			resumeSendVideoData();
		} else if (videoParamter.isVideoUpload()) {
			this.mCurrentVideoAction = ACTION_VIDEO_UPLOAD;
			resumeReceiveVideoData();
			suspendSendVideoData();
		} else if (videoParamter.isVideoMonitor()) {
			this.mCurrentVideoAction = ACTION_VIDEO_MONITOR;
			resumeSendVideoData();
			suspendReceiveVideoData();
		} else if (videoParamter.isVideoDispatch()) {
			this.mCurrentVideoAction = ACTION_VIDEO_UPLOAD;
			resumeReceiveVideoData();
			suspendSendVideoData();
		} else {
			MyLog.e(LOG_TAG, "VMS#setCurrentAction NULL");
		}
		MyLog.d(LOG_TAG, "VMS#setCurrentAction() current action = " + this.mCurrentVideoAction);
	}

	private boolean inteceptIntent(Intent intent) {
		if (TextUtils.isEmpty(intent.getAction())) {
			return true;
		}
		return false;
	}

	public void initVideoSettingColumns(String action) {
		removeColumnsAction();
		addColumnsAction(action);
	}

	private void addColumnsAction(String action) {
		DeviceVideoInfo.PACKET_LOST_LEVEL = buildColumn(action, DeviceVideoInfo.PACKET_LOST_LEVEL);
		DeviceVideoInfo.SCREEN_TYPE = buildColumn(action, DeviceVideoInfo.SCREEN_TYPE);
		DeviceVideoInfo.VIDEO_COLOR_CORRECT = buildColumn(action, DeviceVideoInfo.VIDEO_COLOR_CORRECT);
		DeviceVideoInfo.CAMERA_TYPE_FRONT_OR_POSTPOS = buildColumn(action, DeviceVideoInfo.CAMERA_TYPE_FRONT_OR_POSTPOS);
		DeviceVideoInfo.CAMERA_FRONT_RESOLUTION = buildColumn(action, DeviceVideoInfo.CAMERA_FRONT_RESOLUTION);
		DeviceVideoInfo.CAMERA_POSTPOSTION_RESOLUTIN = buildColumn(action, DeviceVideoInfo.CAMERA_POSTPOSTION_RESOLUTIN);
	}

	private void removeColumnsAction() {
		DeviceVideoInfo.PACKET_LOST_LEVEL = removeColumnAction(DeviceVideoInfo.PACKET_LOST_LEVEL);
		DeviceVideoInfo.SCREEN_TYPE = removeColumnAction(DeviceVideoInfo.SCREEN_TYPE);
		DeviceVideoInfo.VIDEO_COLOR_CORRECT = removeColumnAction(DeviceVideoInfo.VIDEO_COLOR_CORRECT);
		DeviceVideoInfo.CAMERA_TYPE_FRONT_OR_POSTPOS = removeColumnAction(DeviceVideoInfo.CAMERA_TYPE_FRONT_OR_POSTPOS);
		DeviceVideoInfo.CAMERA_FRONT_RESOLUTION = removeColumnAction(DeviceVideoInfo.CAMERA_FRONT_RESOLUTION);
		DeviceVideoInfo.CAMERA_POSTPOSTION_RESOLUTIN = removeColumnAction(DeviceVideoInfo.CAMERA_POSTPOSTION_RESOLUTIN);
	}

	private String removeColumnAction(String column) {
		if (!column.contains(this.mActionSplit)) {
			return column;
		}
//		String[] result = column.split("\\" + this.mActionSplit);
//		if (result.length == 2) {
//			return result[1];
//		}
		return column;
	}

	public String buildColumn(String action, String column) {
		return new StringBuilder(String.valueOf(action)).append(this.mActionSplit).append(column).toString();
	}

	public void showVideoSelectDialog(Context context, VideoSelectDialogBuilder.OnVideoSelectItemClickListener lis, String loginUserNumber, String isVideoBut) {
		MyLog.e("showVideoSelectDialog", "loginUserNumber===> " + loginUserNumber);
		VideoSelectDialogBuilder builder = VideoSelectDialogBuilder.buildSelf(context, lis, loginUserNumber, isVideoBut);
		if (builder != null) {
			builder.show();
		}
	}

	public boolean isEmptyVideoAction() {
		return TextUtils.isEmpty(this.mCurrentVideoAction);
	}

	public boolean existVideoRelatedCall() {
		return CallManager.getManager().getVideoCallsCount() > 0;
	}

	public boolean existVideoCall() {
		return isEmptyVideoAction() || isCurrentVideoCall();
	}

	public boolean isCurrentVideoCall() {
		return this.mCurrentVideoAction != null && this.mCurrentVideoAction.equals(ACTION_VIDEO_CALL);
	}

	public boolean isCurrentVideoMonitor() {
		return this.mCurrentVideoAction != null && this.mCurrentVideoAction.equals(ACTION_VIDEO_MONITOR);
	}

	public boolean isCurrentVideoDispatch() {
		return this.mCurrentVideoAction != null && this.mCurrentVideoAction.equals(ACTION_VIDEO_DISPATCH);
	}

	public boolean isCurrentVideoUpload() {
		return this.mCurrentVideoAction != null && this.mCurrentVideoAction.equals(ACTION_VIDEO_UPLOAD);
	}

	public boolean isCurrentVideoTRANSCRIBE() {
		return this.mCurrentVideoAction != null && this.mCurrentVideoAction.equals(ACTION_VIDEO_TRANSCRIBE);
	}

	public void setCurrentAction(String action) {
		this.mCurrentVideoAction = action;
	}

	public boolean existVideoUploadOrMonitor() {
		VideoManagerService VMS = getDefault();
		if (VMS.isEmptyVideoAction() || (!VMS.isCurrentVideoMonitor() && !VMS.isCurrentVideoUpload() && !VMS.isCurrentVideoTRANSCRIBE())) {
			return false;
		}
		return true;
	}

	public boolean isUseFrontCamera() {
		if (this.mRemoteVideoParamter != null) {
			return this.mRemoteVideoParamter.isUseFrontCamera();
		}
		return false;
	}

	public boolean isUsePostPosCamera() {
		if (this.mRemoteVideoParamter != null) {
			return this.mRemoteVideoParamter.isUsePostPosCamera();
		}
		return false;
	}

	public boolean isDeviceUserConfrim() {
		if (this.mRemoteVideoParamter != null) {
			return this.mRemoteVideoParamter.isDeviceUserConfrim();
		}
		return false;
	}

	public String getCurrentAction() {
		return this.mCurrentVideoAction;
	}

	public int getCurrentVideoParameter() {
		return this.mCurrentVideoParameter;
	}

	public void showConfrimDialog(OnButtonClickListener lis, String title, String content) {
		UserConfrimActivity.registerButtonClickListener(lis);
		UserConfrimActivity.startConfrimActivity(title, content);
	}

	public String getActionString(String action) {
		if (this.mContext == null) {
			throw new IllegalArgumentException("VideoManagerService init error");
		} else if (ACTION_AUDIO_CALL.equals(action)) {
			return this.mContext.getString(R.string.voice_call);
		} else {
			if (ACTION_VIDEO_CALL.equals(action)) {
				return this.mContext.getString(R.string.video_connection);
			}
			if (ACTION_VIDEO_MONITOR.equals(action)) {
				return this.mContext.getString(R.string.video_monitor);
			}
			if (ACTION_VIDEO_UPLOAD.equals(action)) {
				return this.mContext.getString(R.string.video_upload);
			}
			return this.mContext.getString(R.string.video_connection);
		}
	}

	public void initSettingValue(Context context, String action) {
		if (context != null) {
			SharedPreferences settings = context.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
			DeviceVideoInfo.supportRotate = settings.getBoolean(DeviceVideoInfo.VIDEO_SUPPORT_ROTATE, false);
			DeviceVideoInfo.supportFullScreen = settings.getBoolean(DeviceVideoInfo.VIDEO_SUPPORT_FULLSCREEN, false);
			DeviceVideoInfo.isHorizontal = settings.getBoolean(DeviceVideoInfo.VIDEO_SUPPORT_LAND, false);
			DeviceVideoInfo.color_correct = settings.getBoolean(DeviceVideoInfo.VIDEO_COLOR_CORRECT, false);
			DeviceVideoInfo.screen_type = settings.getString(DeviceVideoInfo.SCREEN_TYPE, DeviceVideoInfo.DEFAULT_SCREEN_TYPE);
			if (DeviceVideoInfo.screen_type.equals("ver")) {
				DeviceVideoInfo.isHorizontal = false;
				DeviceVideoInfo.supportRotate = false;
				DeviceVideoInfo.onlyCameraRotate = true;
			} else if (DeviceVideoInfo.screen_type.equals(DeviceVideoInfo.DEFAULT_SCREEN_TYPE)) {
				DeviceVideoInfo.isHorizontal = true;
				DeviceVideoInfo.supportRotate = false;
				DeviceVideoInfo.onlyCameraRotate = true;
			} else {
				DeviceVideoInfo.isHorizontal = false;
				DeviceVideoInfo.supportRotate = true;
				DeviceVideoInfo.onlyCameraRotate = false;
			}
			DeviceVideoInfo.lostLevel = settings.getInt(DeviceVideoInfo.PACKET_LOST_LEVEL, 1);
		}
	}

	public boolean isVideoOutgoingCall() {
		ExtendedCall videoInCall = CallManager.getManager().getVideoInCall();
		if (videoInCall == null) {
			videoInCall = CallManager.getManager().getVideoOutGoingCall();
		}
		if (videoInCall == null || videoInCall.getCallerState() != 0) {
			return false;
		}
		return true;
	}

	public boolean isVideoIncommingCall() {
		return this.mRemoteVideoParamter != null;
	}

	public synchronized boolean registerEndVideoCallHandler(EndVideoCallHandler handler) {
		this.mHandlers.remove(handler);
		this.mHandlers.add(handler);
		return true;
	}

	public synchronized boolean unregisterEndVideoCallHandler(EndVideoCallHandler handler) {
		this.mHandlers.remove(handler);
		return true;
	}

	public synchronized boolean registerOnEndVideoCallCompledtedListener(OnEndVideosCompleted lis) {
		this.mOnEndVideosCompleted = lis;
		return true;
	}

	public synchronized boolean unregisterOnEndVideoCallCompledtedListener(OnEndVideosCompleted lis) {
		this.mOnEndVideosCompleted = null;
		return true;
	}

	public void endVideoCalls() {
		int size = this.mHandlers.size();
		for (int i = 0; i < size; i++) {
			((EndVideoCallHandler) this.mHandlers.get(i)).handle();
		}
	}

	public int getVideoCallSize() {
		return this.mHandlers.size();
	}

	public boolean existEndVideoCallHandler() {
		return getVideoCallSize() > 0;
	}

	public boolean existRemoteVideoControl() {
		return this.mRemoteVideoParamter != null;
	}

	public VideoParamter getRemoteVideoControlParamter() {
		return this.mRemoteVideoParamter;
	}

	public void clearRemoteVideoParameter() {
		this.mRemoteVideoParamter = null;
		this.mCurrentVideoAction = "";
		resumeReceiveVideoData();
		resumeSendVideoData();
	}

	public synchronized void dispatchEndCallCompleted() {
		if (this.mOnEndVideosCompleted != null) {
			this.mHandler.postDelayed(new C13331(), 1000);
		}
	}
}

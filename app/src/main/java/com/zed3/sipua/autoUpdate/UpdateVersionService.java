package com.zed3.sipua.autoUpdate;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.zed3.log.MyLog;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.toast.MyToast;
import com.zed3.utils.Tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class UpdateVersionService {
	private static final int DOWN = 1;
	private static final int DOWN_FINISH = 0;
	private static final int SDCARDFULL = 3;
	protected static final int SDCARDNOTEXSIT = 4;
	private static final String TAG = "updateService";
	private static String ptt_Name;
	private static String updateHost;
	private static String verUrl;
	private static String versionName;
	private boolean cancelUpdate;
	private Context context;
	private Dialog downLoadDialog;
	private String fileSavePath;
	private Handler handler;
	private int progress;
	private ProgressBar progressBar;
	private int saveNum;

	static {
		UpdateVersionService.verUrl = "";
		UpdateVersionService.updateHost = "";
		UpdateVersionService.ptt_Name = "";
		UpdateVersionService.versionName = "";
	}

	public UpdateVersionService(final Context context, final String s) {
		this.saveNum = 0;
		this.cancelUpdate = false;
		this.handler = new Handler() {
			public void handleMessage(final Message message) {
				super.handleMessage(message);
				switch ((int) message.obj) {
					default: {
					}
					case 1: {
						UpdateVersionService.this.progressBar.setProgress(UpdateVersionService.this.progress);
					}
					case 0: {
						UpdateVersionService.this.installAPK();
					}
					case 3: {
						if (UpdateVersionService.this.downLoadDialog != null) {
							UpdateVersionService.this.downLoadDialog.dismiss();
						}
						MyToast.showToast(true, SipUAApp.mContext, SipUAApp.mContext.getResources().getString(R.string.sd_check));
					}
					case 4: {
						MyToast.showToast(true, SipUAApp.mContext, SipUAApp.mContext.getResources().getString(R.string.sd_check_2));
					}
				}
			}
		};
		this.context = context;
		UpdateVersionService.verUrl = this.getServerUrl(s);
		UpdateVersionService.updateHost = UpdateVersionService.verUrl.substring(0, UpdateVersionService.verUrl.lastIndexOf("/") + 1);
		MyLog.i("updateService", "updateServer=" + UpdateVersionService.verUrl);
	}

	private void downloadApk() {
		new downloadApkThread().start();
	}

	private String getServerUrl(final String s) {
		return DeviceInfo.CONFIG_UPDATE_URL.replace("updateServiceIP", s);
	}

	private int getVersionCode(final Context context) {
		try {
			return this.context.getPackageManager().getPackageInfo("com.zed3.sipua", 0).versionCode;
		} catch (PackageManager.NameNotFoundException ex) {
			ex.printStackTrace();
			return 0;
		}
	}

	private String inputStream2String(final InputStream inputStream) throws IOException {
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		while (true) {
			final int read = inputStream.read();
			if (read == -1) {
				break;
			}
			byteArrayOutputStream.write(read);
		}
		return byteArrayOutputStream.toString();
	}

	private void installAPK() {
		final File file = new File(this.fileSavePath, UpdateVersionService.ptt_Name);
		if (file.exists()) {
			final Intent intent = new Intent("android.intent.action.VIEW");
			System.out.println("filepath=" + file.toString() + "  " + file.getPath());
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setDataAndType(Uri.parse("file://" + file.toString()), "application/vnd.android.package-archive");
			this.context.startActivity(intent);
			if (file.length() > 0L) {
				Tools.exitApp2(this.context);
			}
		}
	}

	private boolean isUpdate() {
		// TODO
		return false;
	}

	private String numPadding(String s, final int n) {
		final String s2 = "0";
		if ((s != null && s.length() == 0) || !s.matches("\\d+")) {
			return "0";
		}
		final String value = String.valueOf(Integer.parseInt(s));
		final int length = value.length();
		switch (n) {
			default: {
				s = s2;
				break;
			}
			case 0: {
				s = value.substring(0, 1);
				if (Integer.valueOf(s) > 1) {
					s = s2;
				}
				break;
			}
			case 1: {
				s = String.valueOf(value.substring(0, 1)) + "00";
				break;
			}
			case 2: {
				s = value;
				if (length > 2) {
					s = value.substring(length - 2, length);
				}
				if (s.length() != 2) {
					s = "0" + s;
				}
				break;
			}
			case 3: {
				s = value;
				if (length > 2) {
					s = value.substring(length - 2, length);
				}
				String string = s;
				if (length == 1) {
					string = "0" + s;
				}
				s = "00" + string;
				break;
			}
		}
		return s;
	}

	private void showUpdateVersionDialog() {
		Builder builder = new Builder(this.context);
		builder.setTitle(R.string.setting_update_title);
		builder.setMessage(new StringBuilder(String.valueOf(this.context.getResources().getString(R.string.setting_update_1))).append(" ").append(versionName).append(" ").append(this.context.getResources().getString(R.string.setting_update_2)).toString());
//		builder.setPositiveButton(this.context.getResources().getString(R.string.update), new C10672());
//		builder.setNegativeButton(this.context.getResources().getString(R.string.no_update), new C10683());
		builder.create().show();
	}

	public void checkUpdate(final boolean b) {
		if (this.isUpdate()) {
			this.showUpdateVersionDialog();
		} else if (b) {
			MyToast.showToast(true, this.context, this.context.getResources().getString(R.string.setting_update_no));
		}
	}

	String formatNum(final String s, int length) {
		String s2;
		if (length == 2) {
			s2 = s;
			if (s.length() == 1) {
				return s;
			}
		} else {
			if (length == 1) {
				return String.valueOf(s) + "000";
			}
			s2 = s;
			if (length == 4) {
				length = s.length();
				if (length == 5) {
					s2 = "00" + s.substring(length - 2, length);
				} else {
					s2 = s;
					if (s.length() < 4) {
						if (s.length() == 1) {
							s2 = "000" + s;
						} else if (s.length() == 2) {
							s2 = "00" + s;
						} else {
							s2 = s;
							if (s.length() == 3) {
								s2 = "0" + s;
							}
						}
					}
				}
			}
		}
		return s2;
	}

	long readSDCard() {
		if ("mounted".equals(Environment.getExternalStorageState())) {
			final StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
			final long n = statFs.getBlockSize();
			final long n2 = statFs.getAvailableBlocks();
			MyLog.i("UpdateVersionService", "sdcard  left" + n2 * n / 1024L / 1024L + "m");
			return n2 * n / 1024L / 1024L;
		}
		return 0L;
	}

	void readSystem() {
		new StatFs(Environment.getRootDirectory().getPath());
	}

	protected void showDownloadDialog() {
		Builder builder = new Builder(this.context);
		builder.setTitle(R.string.updating);
		View v = LayoutInflater.from(this.context).inflate(R.layout.downloaddialog, null);
		this.progressBar = (ProgressBar) v.findViewById(R.id.updateProgress);
		builder.setView(v);
//		builder.setNegativeButton(this.context.getResources().getString(R.string.cancel), new C10694());
		this.downLoadDialog = builder.create();
		this.downLoadDialog.setCanceledOnTouchOutside(false);
		this.downLoadDialog.show();
		downloadApk();
	}

	public void uninstallAPK() {
		this.context.startActivity(new Intent("android.intent.action.DELETE", Uri.parse("package:com.example.updateversion")));
	}

	public class downloadApkThread extends Thread {
		@Override
		public void run() {
			try {
				if (!Environment.getExternalStorageState().equals("mounted")) {
					return;
				}
				final URL url = new URL(String.valueOf(UpdateVersionService.updateHost) + UpdateVersionService.ptt_Name);
				MyLog.i("updateService", "download apk url =" + url);
				final HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
				httpURLConnection.setReadTimeout(5000);
				httpURLConnection.setRequestMethod("GET");
				httpURLConnection.setRequestProperty("Charser", "GBK,utf-8;q=0.7,*;q=0.3");
				final int contentLength = httpURLConnection.getContentLength();
				final InputStream inputStream = httpURLConnection.getInputStream();
				final File file = new File(UpdateVersionService.this.fileSavePath);
				if (!file.exists()) {
					file.mkdir();
				}
				final File file2 = new File(UpdateVersionService.this.fileSavePath, UpdateVersionService.ptt_Name);
				if (file2.exists() && file2.length() == contentLength) {
					if (UpdateVersionService.this.downLoadDialog != null) {
						UpdateVersionService.this.downLoadDialog.dismiss();
					}
					final Message message = new Message();
					message.obj = 0;
					UpdateVersionService.this.handler.sendMessage(message);
					inputStream.close();
					if (httpURLConnection != null) {
						httpURLConnection.disconnect();
					}
					return;
				} else {
					if (UpdateVersionService.this.readSDCard() < 30L) {
						final Message message2 = new Message();
						message2.obj = 3;
						UpdateVersionService.this.handler.sendMessage(message2);
						return;
					}
				}
			} catch (MalformedURLException ex) {
				ex.printStackTrace();
				return;
			} catch (IOException ex2) {
				ex2.printStackTrace();
				return;
			}
			// TODO
		}
	}
}

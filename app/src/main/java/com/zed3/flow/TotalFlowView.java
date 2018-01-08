package com.zed3.flow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zed3.location.MemoryMg;
import com.zed3.sipua.R;
import com.zed3.sipua.welcome.DeviceInfo;

public class TotalFlowView extends Activity {
	double a;
	boolean alarmFlag;
	LinearLayout alarmlinear;
	TextView alarmnumtxt;
	TextView alarmtip;
	double b;
	ImageButton backbtn;
	double c;
	TextView detailtip;
	boolean flag;
	ImageView flowiconx;
	ImageView imgviewbtn;
	double lastTotal;
	double lasttime;
	Handler mHandle;
	SharedPreferences mypre;
	TextView pttlast;
	TextView ptttotal;
	private final String sharedPrefsFile;
	ImageView tooltipbtn;
	double useMin;
	double useTotal;
	LinearLayout videoLinear;
	TextView videolast;
	TextView videototal;

	public TotalFlowView() {
		this.sharedPrefsFile = "com.zed3.sipua_preferences";
		this.alarmtip = null;
		this.detailtip = null;
		this.imgviewbtn = null;
		this.tooltipbtn = null;
		this.flowiconx = null;
		this.alarmnumtxt = null;
		this.alarmlinear = null;
		this.ptttotal = null;
		this.pttlast = null;
		this.videototal = null;
		this.videolast = null;
		this.videoLinear = null;
		this.backbtn = null;
		this.flag = false;
		this.alarmFlag = false;
		this.mypre = null;
		this.useTotal = 0.0;
		this.lastTotal = 0.0;
		this.useMin = 0.0;
		this.lasttime = 0.0;
		this.a = 0.0;
		this.b = 0.0;
		this.c = 0.0;
		this.mHandle = new Handler() {
			public void handleMessage(final Message message) {
				if (message.what == 1) {
					try {
						TotalFlowView.this.lasttime = TotalFlowView.this.calculatePercent(MemoryMg.getInstance().User_3GRelTotal, MemoryMg.getInstance().User_3GTotal);
						TotalFlowView.this.detailtip.setText((CharSequence) ("\u5df2\u4f7f\u7528" + TotalFlowView.this.calculateTotal(MemoryMg.getInstance().User_3GRelTotal) + "M,\u5269\u4f59" + (100.0 - TotalFlowView.this.lasttime * 100.0) + "%"));
						TotalFlowView.this.pttlast.setText((CharSequence) (String.valueOf(TotalFlowView.this.calculateTotal(MemoryMg.getInstance().User_3GTotalPTT - MemoryMg.getInstance().User_3GRelTotalPTT)) + "M"));
						if (MemoryMg.getInstance().User_3GTotalVideo == 0.0) {
							TotalFlowView.this.videolast.setText((CharSequence) "0.0M");
						} else {
							TotalFlowView.this.videolast.setTextColor(-65536);
							TotalFlowView.this.videolast.setText((CharSequence) (String.valueOf(TotalFlowView.this.calculateTotal(MemoryMg.getInstance().User_3GTotalVideo - MemoryMg.getInstance().User_3GRelTotalVideo)) + "M"));
						}
						TotalFlowView.this.SetFontColor();
						TotalFlowView.this.mHandle.sendMessageDelayed(TotalFlowView.this.mHandle.obtainMessage(1), 8000L);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		};
	}

	private void InitView() {
		if (!DeviceInfo.CONFIG_SUPPORT_VIDEO) {
			this.videoLinear.setVisibility(View.INVISIBLE);
		}
		this.mypre = this.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
		if (this.mypre.getBoolean("flowtooltip", true)) {
			this.imgviewbtn.setImageResource(R.drawable.on);
			this.flag = false;
		} else {
			this.flag = true;
			this.imgviewbtn.setImageResource(R.drawable.off);
		}
		if (this.mypre.getBoolean("flowalarmout", true)) {
			this.tooltipbtn.setImageResource(R.drawable.on);
			this.alarmFlag = false;
			this.alarmlinear.setVisibility(View.VISIBLE);
		} else {
			this.alarmFlag = true;
			this.tooltipbtn.setImageResource(R.drawable.off);
			this.alarmlinear.setVisibility(View.GONE);
		}
		this.ptttotal.setText((CharSequence) (String.valueOf(this.calculateTotal(MemoryMg.getInstance().User_3GTotalPTT)) + "M"));
		this.videototal.setText((CharSequence) (String.valueOf(this.calculateTotal(MemoryMg.getInstance().User_3GTotalVideo)) + "M"));
		if (this.mHandle.hasMessages(1)) {
			this.mHandle.removeMessages(1);
		}
		this.mHandle.sendEmptyMessage(1);
	}

	private void SetFontColor() {
		this.a = this.calculatePercent(MemoryMg.getInstance().User_3GRelTotal, MemoryMg.getInstance().User_3GTotal);
		if (this.a < 0.6 && this.a >= 0.0) {
			this.flowiconx.setImageResource(R.drawable.flowicon);
			this.alarmtip.setTextColor(-16711936);
			this.alarmtip.setText((CharSequence) "\u672c\u6708\u5269\u4f59\u6d41\u91cf\u5145\u8db3\uff0c\u8bf7\u653e\u5fc3\u4f7f\u7528");
		} else {
			if (this.a < 0.9 && this.a >= 0.6) {
				this.flowiconx.setImageResource(R.drawable.flowiconyellow);
				this.alarmtip.setTextColor(-256);
				this.alarmtip.setText((CharSequence) "\u672c\u6708\u5269\u4f59\u6d41\u91cf\u5df2\u8fc7\u534a\uff0c\u8bf7\u8282\u7701\u4f7f\u7528");
				return;
			}
			if (this.a >= 0.9) {
				this.flowiconx.setImageResource(R.drawable.flowiconred);
				this.alarmtip.setTextColor(-65536);
				this.alarmtip.setText((CharSequence) "\u60a8\u7684\u6d41\u91cf\u4f7f\u7528\u5df2\u7ecf\u63a5\u8fd1\u5957\u9910\u9650\u503c\uff0c\u8d85\u8fc7\u5957\u9910\u9650\u503c\u5c06\u6d88\u8017\u56fd\u5185\u6d41\u91cf");
			}
		}
	}

	public double calculatePercent(final double n, final double n2) {
		return Math.round(n / n2 * 100.0) / 100.0;
	}

	public double calculateTotal(final double n) {
		return Math.round(n / 1024.0 / 1024.0 * 100.0) / 100.0;
	}

	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		this.setContentView(R.layout.totalflowview);
		this.flowiconx = (ImageView) this.findViewById(R.id.flowiconx);
		this.alarmtip = (TextView) this.findViewById(R.id.alarmtip);
		this.detailtip = (TextView) this.findViewById(R.id.detailtip);
		(this.backbtn = (ImageButton) this.findViewById(R.id.back_button)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				TotalFlowView.this.finish();
			}
		});
		(this.imgviewbtn = (ImageView) this.findViewById(R.id.imgviewbtn)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				final ImageView imageView = (ImageView) view;
				TotalFlowView.this.mypre = TotalFlowView.this.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
				final SharedPreferences.Editor edit = TotalFlowView.this.mypre.edit();
				if (!TotalFlowView.this.flag) {
					imageView.setImageResource(R.drawable.off);
					TotalFlowView.this.flag = true;
					edit.putBoolean("flowtooltip", false);
					MemoryMg.getInstance().isProgressBarTip = false;
				} else {
					imageView.setImageResource(R.drawable.on);
					TotalFlowView.this.flag = false;
					edit.putBoolean("flowtooltip", true);
					MemoryMg.getInstance().isProgressBarTip = true;
				}
				edit.commit();
			}
		});
		(this.tooltipbtn = (ImageView) this.findViewById(R.id.tooltipbtn)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				final ImageView imageView = (ImageView) view;
				TotalFlowView.this.mypre = TotalFlowView.this.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
				final SharedPreferences.Editor edit = TotalFlowView.this.mypre.edit();
				if (!TotalFlowView.this.alarmFlag) {
					imageView.setImageResource(R.drawable.off);
					TotalFlowView.this.alarmFlag = true;
					edit.putBoolean("flowalarmout", false);
					MemoryMg.getInstance().User_3GFlowOut = 0.0;
					edit.putString("3gflowoutval", "0");
					TotalFlowView.this.alarmlinear.setVisibility(View.GONE);
				} else {
					imageView.setImageResource(R.drawable.on);
					TotalFlowView.this.alarmFlag = false;
					edit.putBoolean("flowalarmout", true);
					TotalFlowView.this.alarmlinear.setVisibility(View.VISIBLE);
				}
				edit.commit();
			}
		});
		this.alarmnumtxt = (TextView) this.findViewById(R.id.alarmnumtxt);
		(this.alarmlinear = (LinearLayout) this.findViewById(R.id.alarmlinear)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				TotalFlowView.this.startActivity(new Intent((Context) TotalFlowView.this, (Class) FlowAlarmSet.class));
			}
		});
		this.ptttotal = (TextView) this.findViewById(R.id.ptttotal);
		this.pttlast = (TextView) this.findViewById(R.id.pttlast);
		this.videototal = (TextView) this.findViewById(R.id.videototal);
		this.videolast = (TextView) this.findViewById(R.id.videolast);
		this.videoLinear = (LinearLayout) this.findViewById(R.id.videolinear);
		this.InitView();
	}

	protected void onDestroy() {
		super.onDestroy();
		if (this.mHandle.hasMessages(1)) {
			this.mHandle.removeMessages(1);
		}
	}

	protected void onResume() {
		super.onResume();
		if (MemoryMg.getInstance().User_3GFlowOut == 0.0) {
			this.alarmnumtxt.setText((CharSequence) "\u672a\u8bbe\u7f6e");
			return;
		}
		this.alarmnumtxt.setText((CharSequence) (String.valueOf(MemoryMg.getInstance().User_3GFlowOut) + "M"));
	}
}

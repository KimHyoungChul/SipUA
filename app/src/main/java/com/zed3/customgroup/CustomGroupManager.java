package com.zed3.customgroup;

import android.content.Intent;
import android.text.TextUtils;

import com.zed3.sipua.PttGrp;
import com.zed3.sipua.PttGrps;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.UserAgentProfile;
import com.zed3.sipua.ui.Receiver;
import com.zed3.utils.LogUtil;
import com.zed3.utils.NetChangedReceiver;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.header.Header;
import org.zoolu.sip.message.BaseMessageFactory;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.transaction.TransactionClient;
import org.zoolu.sip.transaction.TransactionClientListener;

public class CustomGroupManager implements TransactionClientListener {
	private static final int ADD = 1;
	private static final int CREATE = 0;
	public static String CUSTOM_GROUP_ACTION_ADD_FAILURE;
	public static String CUSTOM_GROUP_ACTION_ADD_SUCCESS;
	public static String CUSTOM_GROUP_ACTION_CREATE_FAILURE;
	public static String CUSTOM_GROUP_ACTION_CREATE_SUCCESS;
	public static String CUSTOM_GROUP_ACTION_DELETE_FAILURE;
	public static String CUSTOM_GROUP_ACTION_DELETE_SUCCESS;
	public static String CUSTOM_GROUP_ACTION_DESTROY_FAILURE;
	public static String CUSTOM_GROUP_ACTION_DESTROY_SUCCESS;
	public static String CUSTOM_GROUP_ACTION_GET_GROUP_MEMBER_INFO_TIME_OUT;
	public static String CUSTOM_GROUP_ACTION_GET_GROUP_NUMBER_LIST_TIME_OUT;
	public static String CUSTOM_GROUP_ACTION_INFO_CHANGED;
	public static String CUSTOM_GROUP_ACTION_LEAVE_FAILURE;
	public static String CUSTOM_GROUP_ACTION_LEAVE_SUCCESS;
	public static String CUSTOM_GROUP_ACTION_MODIFY_FAILURE;
	public static String CUSTOM_GROUP_ACTION_MODIFY_SUCCESS;
	public static String CUSTOM_GROUP_ACTION_UPDATE_GROUP_MEMBER_INFO;
	public static String CUSTOM_GROUP_ACTION_UPDATE_LOCAL_INFO;
	public static String CUSTOM_GROUP_ACTION_UPDATE_PTT_GROUP_INFO;
	public static String CUSTOM_GROUP_REQUEST_TIME_OUT;
	private static final int DELETE = 2;
	private static final int DESTROY = 3;
	private static final int EXIT_CURRENT_CUSTOM_GROUP = 5;
	private static final int GET_CUSTOM_GROUP = 6;
	private static final int GET_CUSTOM_GROUP_MEMBER = 7;
	private static final int MODIFY = 4;
	private static final String TAG = "CustomGroupManager";
	private static CustomGroupManager mCustomGroupManager;

	static {
		CustomGroupManager.mCustomGroupManager = null;
		CustomGroupManager.CUSTOM_GROUP_ACTION_CREATE_SUCCESS = "custom_group_action_CREATE_SUCCESS";
		CustomGroupManager.CUSTOM_GROUP_ACTION_CREATE_FAILURE = "custom_group_action_CREATE_FAILURE";
		CustomGroupManager.CUSTOM_GROUP_ACTION_ADD_SUCCESS = "custom_group_action_ADD_SUCCESS";
		CustomGroupManager.CUSTOM_GROUP_ACTION_ADD_FAILURE = "custom_group_action_ADD_FAILURE";
		CustomGroupManager.CUSTOM_GROUP_ACTION_DELETE_SUCCESS = "custom_group_action_DELETE_SUCCESS";
		CustomGroupManager.CUSTOM_GROUP_ACTION_DELETE_FAILURE = "custom_group_action_DELETE_FAILURE";
		CustomGroupManager.CUSTOM_GROUP_ACTION_DESTROY_SUCCESS = "custom_group_action_DESTROY_SUCCESS";
		CustomGroupManager.CUSTOM_GROUP_ACTION_DESTROY_FAILURE = "custom_group_action_DESTROY_FAILURE";
		CustomGroupManager.CUSTOM_GROUP_ACTION_MODIFY_SUCCESS = "custom_group_action_MODIFY_SUCCESS";
		CustomGroupManager.CUSTOM_GROUP_ACTION_MODIFY_FAILURE = "custom_group_action_MODIFY_FAILURE";
		CustomGroupManager.CUSTOM_GROUP_ACTION_LEAVE_SUCCESS = "custom_group_action_LEAVE_SUCCESS";
		CustomGroupManager.CUSTOM_GROUP_ACTION_LEAVE_FAILURE = "custom_group_action_LEAVE_FAILURE";
		CustomGroupManager.CUSTOM_GROUP_ACTION_GET_GROUP_NUMBER_LIST_TIME_OUT = "custom_group_action_GET_GROUP_NUMBER_LIST_TIME_OUT";
		CustomGroupManager.CUSTOM_GROUP_ACTION_GET_GROUP_MEMBER_INFO_TIME_OUT = "custom_group_action_GET_GROUP_MEMBER_INFO_TIME_OUT";
		CustomGroupManager.CUSTOM_GROUP_REQUEST_TIME_OUT = "custom_group_action_REQUEST_TIME_OUT";
		CustomGroupManager.CUSTOM_GROUP_ACTION_UPDATE_GROUP_MEMBER_INFO = "custom_group_action_UPDATE_GROUP_MEMBER_INFO";
		CustomGroupManager.CUSTOM_GROUP_ACTION_INFO_CHANGED = "custom_group_action_INFO_CHANGED";
		CustomGroupManager.CUSTOM_GROUP_ACTION_UPDATE_LOCAL_INFO = "custom_group_action_UPDATE_LOCAL_INFO";
		CustomGroupManager.CUSTOM_GROUP_ACTION_UPDATE_PTT_GROUP_INFO = "custom_group_action_UPDATE_PTT_GROUP_INFO";
	}

	public static CustomGroupManager getInstance() {
		if (CustomGroupManager.mCustomGroupManager == null) {
			CustomGroupManager.mCustomGroupManager = new CustomGroupManager();
		}
		return CustomGroupManager.mCustomGroupManager;
	}

	private String getToUrl(String s, final UserAgentProfile userAgentProfile) {
		String string;
		final String s2 = string = "";
		if (s != null) {
			string = s2;
			if (!s.equals("")) {
				string = s2;
				if (s.indexOf("@") < 0) {
					if (userAgentProfile.realm.equals("")) {
						new StringBuilder("&").append(s).toString();
					}
					final StringBuilder append = new StringBuilder(String.valueOf(s)).append("@");
					if (userAgentProfile.realm_orig == null) {
						s = userAgentProfile.realm;
					} else {
						s = userAgentProfile.realm_orig;
					}
					string = append.append(s).toString();
				}
			}
		}
		return string;
	}

	public String getGroupStringInfo(final PttCustomGrp pttCustomGrp) {
		if (pttCustomGrp == null) {
			return "";
		}
		final StringBuilder sb = new StringBuilder();
		sb.append(pttCustomGrp.getGroupCreatorNum()).append(",").append(pttCustomGrp.getGroupCreatorName()).append(",").append(pttCustomGrp.getGroupNum()).append(",").append(pttCustomGrp.getGroupName());
		return sb.toString().trim();
	}

	@Override
	public void onTransFailureResponse(final TransactionClient transactionClient, final Message message) {
		if (transactionClient.getTransactionMethod().equals("MESSAGE")) {
			final int code = message.getStatusLine().getCode();
			final String body = transactionClient.getRequestMessage().getBody();
			final Intent intent = new Intent();
			intent.putExtra("reasonCode", code);
			if (body.startsWith("create")) {
				intent.setAction(CustomGroupManager.CUSTOM_GROUP_ACTION_CREATE_FAILURE);
			} else if (body.startsWith("add")) {
				intent.setAction(CustomGroupManager.CUSTOM_GROUP_ACTION_ADD_FAILURE);
			} else if (body.startsWith("del")) {
				intent.setAction(CustomGroupManager.CUSTOM_GROUP_ACTION_DELETE_FAILURE);
			} else if (body.startsWith("destroy")) {
				intent.setAction(CustomGroupManager.CUSTOM_GROUP_ACTION_DESTROY_FAILURE);
			} else if (body.startsWith("modifyPTT")) {
				intent.setAction(CustomGroupManager.CUSTOM_GROUP_ACTION_MODIFY_FAILURE);
			} else if (body.startsWith("leave")) {
				intent.setAction(CustomGroupManager.CUSTOM_GROUP_ACTION_LEAVE_FAILURE);
			}
			SipUAApp.getAppContext().sendBroadcast(intent);
			LogUtil.makeLog("CustomGroupManager", "onTransFailureResponse() " + code);
		}
	}

	@Override
	public void onTransProvisionalResponse(final TransactionClient transactionClient, final Message message) {
		LogUtil.makeLog("CustomGroupManager", "onTransProvisionalResponse()");
	}

	@Override
	public void onTransSuccessResponse(final TransactionClient transactionClient, final Message message) {
		LogUtil.makeLog("CustomGroupManager", "onTransSuccessResponse()");
		if (transactionClient.getTransactionMethod().equals("MESSAGE") && message.getStatusLine().getCode() == 200) {
			final String body = transactionClient.getRequestMessage().getBody();
			final String body2 = message.getBody();
			final Intent intent = new Intent();
			if (body.startsWith("create")) {
				final PttGrps getAllGrps = Receiver.GetCurUA().GetAllGrps();
				if (body2 != null) {
					final String[] split = body2.trim().split("\r\n");
					if (split != null && split.length == 2 && split[0].equals("create")) {
						getAllGrps.addCustomGroup(getAllGrps.parseCustomGrp(split[1]));
					}
				}
				intent.setAction(CustomGroupManager.CUSTOM_GROUP_ACTION_CREATE_SUCCESS);
			} else if (body.startsWith("add")) {
				intent.setAction(CustomGroupManager.CUSTOM_GROUP_ACTION_ADD_SUCCESS);
			} else if (body.startsWith("del")) {
				intent.setAction(CustomGroupManager.CUSTOM_GROUP_ACTION_DELETE_SUCCESS);
			} else if (body.startsWith("destroy")) {
				intent.setAction(CustomGroupManager.CUSTOM_GROUP_ACTION_DESTROY_SUCCESS);
			} else if (body.startsWith("modifyPTT")) {
				intent.setAction(CustomGroupManager.CUSTOM_GROUP_ACTION_MODIFY_SUCCESS);
			} else if (body.startsWith("leave")) {
				intent.setAction(CustomGroupManager.CUSTOM_GROUP_ACTION_LEAVE_SUCCESS);
			} else if (body.startsWith("getGroup")) {
				final PttGrps getAllGrps2 = Receiver.GetCurUA().GetAllGrps();
				getAllGrps2.addCustomGroups(body2);
				if (getAllGrps2.GetCount() > 0) {
					if (!TextUtils.isEmpty((CharSequence) NetChangedReceiver.lastGrpID)) {
						final PttGrp getGrpByID = getAllGrps2.GetGrpByID(NetChangedReceiver.lastGrpID);
						if (getGrpByID != null) {
							final UserAgent getCurUA = Receiver.GetCurUA();
							if (getCurUA != null) {
								getCurUA.SetCurGrp(getGrpByID, true);
								NetChangedReceiver.lastGrpID = "";
							}
						} else {
							final UserAgent getCurUA2 = Receiver.GetCurUA();
							if (getCurUA2 != null) {
								getCurUA2.SetCurGrp(getAllGrps2.FirstGrp(), true);
								NetChangedReceiver.lastGrpID = "";
							}
						}
					} else if (getAllGrps2.getFixedGrpCount() <= 0) {
						final UserAgent getCurUA3 = Receiver.GetCurUA();
						if (getCurUA3 != null) {
							getCurUA3.SetCurGrp(getAllGrps2.FirstGrp(), true);
							NetChangedReceiver.lastGrpID = "";
						}
					}
				}
			} else if (body.startsWith("getMember")) {
				Receiver.getGDProcess().customGrpUpdate(body2);
			}
			SipUAApp.getAppContext().sendBroadcast(intent);
		}
	}

	@Override
	public void onTransTimeout(final TransactionClient transactionClient) {
		if (transactionClient != null) {
			LogUtil.makeLog("CustomGroupManager", "onTransTimeout() \u670d\u52a1\u5668\u8fde\u63a5\u8d85\u65f6");
			final String body = transactionClient.getRequestMessage().getBody();
			final Intent intent = new Intent(CustomGroupManager.CUSTOM_GROUP_REQUEST_TIME_OUT);
			if (body.startsWith("getGroup")) {
				intent.setAction(CustomGroupManager.CUSTOM_GROUP_ACTION_GET_GROUP_NUMBER_LIST_TIME_OUT);
				LogUtil.makeLog("CustomGroupManager", "getGroup request time out");
			}
			if (body.startsWith("getMember")) {
				final String userName = transactionClient.getRequestMessage().getToHeader().getNameAddress().getAddress().getUserName();
				if (!TextUtils.isEmpty((CharSequence) userName)) {
					intent.putExtra("groupNumber", userName);
					intent.setAction(CustomGroupManager.CUSTOM_GROUP_ACTION_GET_GROUP_MEMBER_INFO_TIME_OUT);
					LogUtil.makeLog("CustomGroupManager", "getMember request time out, the custom group number is " + userName);
				}
			}
			SipUAApp.getAppContext().sendBroadcast(intent);
		}
	}

	public String sendCustomGroupMessage(final SipProvider sipProvider, final UserAgentProfile userAgentProfile, String from_url, final String s, final int n, final String s2, final String s3, final String s4) {
		LogUtil.makeLog("CustomGroupManager", "sendCustomGroupMessage() type = " + n);
		from_url = userAgentProfile.from_url;
		final String from_url2 = userAgentProfile.from_url;
		final StringBuilder sb = new StringBuilder();
		String s5 = null;
		switch (n) {
			default: {
				s5 = from_url;
				break;
			}
			case 0: {
				sb.append("create\r\n");
				sb.append("group:" + s2 + "\r\n");
				sb.append("ptt:20,120,120,2,7,1800\r\n");
				sb.append("member:" + s3 + "\r\n");
				s5 = from_url;
				break;
			}
			case 1: {
				sb.append("add\r\n");
				sb.append("group:" + s2 + "\r\n");
				sb.append("member:" + s3 + "\r\n");
				s5 = from_url;
				break;
			}
			case 2: {
				sb.append("del\r\n");
				sb.append("group:" + s2 + "\r\n");
				sb.append("member:" + s3 + "\r\n");
				s5 = from_url;
				break;
			}
			case 3: {
				sb.append("destroy\r\n");
				sb.append("group:" + s2 + "\r\n");
				s5 = from_url;
				break;
			}
			case 4: {
				sb.append("modifyPTT\r\n");
				sb.append("group:" + s2 + "\r\n");
				sb.append("ptt:20,120,120,2,7,1800\r\n");
				s5 = from_url;
				break;
			}
			case 5: {
				sb.append("leave\r\n");
				sb.append("group:" + s2 + "\r\n");
				s5 = this.getToUrl(s, userAgentProfile);
				break;
			}
			case 6: {
				sb.append("getGroup\r\n");
				s5 = from_url;
				break;
			}
			case 7: {
				sb.append("getMember\r\n");
				s5 = this.getToUrl(s, userAgentProfile);
				break;
			}
		}
		final Message request = BaseMessageFactory.createRequest(sipProvider, "MESSAGE", new NameAddress(s5), new NameAddress(from_url2), sb.toString());
		request.setHeader(new Header("Content-Type", "text/customGroup"));
		if (s4 != null) {
			request.setHeader(new Header("Call-ID", s4));
		}
		new TransactionClient(sipProvider, request, this).request();
		LogUtil.makeLog("CustomGroupManager", "sendCustomGroupMessage() content = " + sb.toString());
		return request.getCallIdHeader().getCallId();
	}
}

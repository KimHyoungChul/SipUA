package com.zed3.sipua;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.provider.Settings.System;

import com.zed3.sipua.message.MessageDialogueActivity;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.Settings;
import com.zed3.utils.Systems;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.authentication.DigestAuthentication;
import org.zoolu.sip.dialog.SubscriberDialog;
import org.zoolu.sip.dialog.SubscriberDialogListener;
import org.zoolu.sip.header.AcceptHeader;
import org.zoolu.sip.header.AuthorizationHeader;
import org.zoolu.sip.header.BaseSipHeaders;
import org.zoolu.sip.header.ContactHeader;
import org.zoolu.sip.header.ContentTypeHeader;
import org.zoolu.sip.header.ExpiresHeader;
import org.zoolu.sip.header.Header;
import org.zoolu.sip.header.ProxyAuthenticateHeader;
import org.zoolu.sip.header.PttAttributeHeader;
import org.zoolu.sip.header.PttExtensionHeader;
import org.zoolu.sip.header.StatusLine;
import org.zoolu.sip.header.ViaHeader;
import org.zoolu.sip.header.WwwAuthenticateHeader;
import org.zoolu.sip.message.BaseMessageFactory;
import org.zoolu.sip.message.BaseSipMethods;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.message.MessageFactory;
import org.zoolu.sip.message.SipMethods;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.sip.provider.TransactionIdentifier;
import org.zoolu.sip.transaction.TransactionClient;
import org.zoolu.sip.transaction.TransactionClientListener;
import org.zoolu.tools.Log;
import org.zoolu.tools.Parser;

import java.util.Vector;

public class RegisterAgent implements TransactionClientListener, SubscriberDialogListener {
	public static final int DEREGISTERING = 4;
	static final int MAX_ATTEMPTS = 3;
	public static final int REGISTERED = 3;
	public static final int REGISTERING = 2;
	public static final int UNDEFINED = 0;
	public static final int UNREGISTERED = 1;
	public int CurrentState;
	public final int SUBSCRIPTION_EXPIRES = 184000;
	boolean alreadySubscribed = false;
	int attempts;
	NameAddress contact;
	Message currentSubscribeMessage;
	int expire_time;
	String icsi;
	RegisterAgentListener listener;
	Log log;
	boolean loop;
	String next_nonce;
	String passwd;
	Boolean pub;
	String qop;
	String qvalue;
	String realm;
	SubscriberDialog sd;
	SipProvider sip_provider;
	int subattempts;
	TransactionClient f1079t;
	NameAddress target;
	UserAgentProfile user_profile;
	String username;

	class C10561 implements Runnable {
		C10561() {
		}

		public void run() {
			Object o = new Object();
			try {
				synchronized (o) {
					o.wait(10000);
				}
			} catch (Exception e) {
			}
			RegisterAgent.this.startMWI();
		}
	}

	public RegisterAgent(SipProvider sip_provider, String target_url, String contact_url, String username, String realm, String passwd, RegisterAgentListener listener, UserAgentProfile user_profile, String qvalue, String icsi, Boolean pub) {
		init(sip_provider, target_url, contact_url, listener);
		this.username = username;
		this.realm = realm;
		this.passwd = passwd;
		this.user_profile = user_profile;
		this.qvalue = qvalue;
		this.icsi = icsi;
		this.pub = pub;
	}

	public void halt() {
		stopMWI();
		this.listener = null;
	}

	private void init(SipProvider sip_provider, String target_url, String contact_url, RegisterAgentListener listener) {
		this.listener = listener;
		this.sip_provider = sip_provider;
		this.log = sip_provider.getLog();
		this.target = new NameAddress(target_url);
		this.contact = new NameAddress(contact_url);
		this.expire_time = SipStack.default_expires;
		this.username = null;
		this.realm = null;
		this.passwd = null;
		this.next_nonce = null;
		this.qop = null;
		this.attempts = 0;
	}

	public boolean isRegistered() {
		return this.CurrentState == 3 || this.CurrentState == 2;
	}

	public boolean isRegistered(boolean login) {
		if (login && this.CurrentState == 3) {
			return true;
		}
		return false;
	}

	public boolean register() {
		return register(SipStack.default_expires);
	}

	public boolean register(int expire_time) {
		this.attempts = 0;
		if (expire_time > 0) {
			if (this.CurrentState == 4) {
				if (this.f1079t != null) {
					this.f1079t.terminate();
				}
				onTransTimeout(this.f1079t);
			}
			if (this.CurrentState != 1 && this.CurrentState != 3 && this.CurrentState != 0) {
				return false;
			}
			this.expire_time = expire_time;
			this.CurrentState = 2;
		} else {
			if (this.CurrentState == 2) {
				if (this.f1079t != null) {
					this.f1079t.terminate();
				}
				onTransTimeout(this.f1079t);
			}
			if (this.CurrentState != 3 && this.CurrentState != 0) {
				return false;
			}
			expire_time = 0;
			this.CurrentState = 4;
		}
		Message req = BaseMessageFactory.createRegisterRequest(this.sip_provider, this.target, this.target, new NameAddress(this.user_profile.contact_url), this.qvalue, this.icsi);
		req.setExpiresHeader(new ExpiresHeader(String.valueOf(expire_time)));
		req.setHeader(new PttExtensionHeader("3ghandset register"));
		req.setHeader(new PttAttributeHeader(PttAttributeHeader.getString(Systems.getVersionName(Receiver.mContext), Systems.getThid(Receiver.mContext))));
		if (this.next_nonce != null) {
			AuthorizationHeader ah = new AuthorizationHeader("Digest");
			ah.addUsernameParam(this.username);
			ah.addRealmParam(this.realm);
			ah.addNonceParam(this.next_nonce);
			ah.addUriParam(req.getRequestLine().getAddress().toString());
			ah.addQopParam(this.qop);
			ah.addResponseParam(new DigestAuthentication(BaseSipMethods.REGISTER, ah, null, this.passwd).getResponse());
			req.setAuthorizationHeader(ah);
		}
		if (expire_time > 0) {
			printLog("Registering contact " + this.contact + " (it expires in " + expire_time + " secs)", 1);
		} else {
			printLog("Unregistering contact " + this.contact, 1);
		}
		this.f1079t = new TransactionClient(this.sip_provider, req, this, 30000);
		this.f1079t.request();
		return true;
	}

	public boolean unregister() {
		stopMWI();
		return register(0);
	}

	public void stopMWI() {
		if (this.sd != null) {
			synchronized (this.sd) {
				this.sd.notify();
			}
		}
		this.sd = null;
		if (this.listener != null) {
			this.listener.onMWIUpdate(this, false, 0, null);
		}
	}

	Message getSubscribeMessage(boolean current) {
		Message req;
		if (this.sd != null) {
			synchronized (this.sd) {
				this.sd.notify();
			}
		}
		this.sd = new SubscriberDialog(this.sip_provider, "message-summary", "", this);
		this.sip_provider.removeSipProviderListener(new TransactionIdentifier(SipMethods.NOTIFY));
		this.sip_provider.addSipProviderListener(new TransactionIdentifier(SipMethods.NOTIFY), this.sd);
		if (current) {
			req = this.currentSubscribeMessage;
			req.setCSeqHeader(req.getCSeqHeader().incSequenceNumber());
		} else {
			req = MessageFactory.createSubscribeRequest(this.sip_provider, this.target.getAddress(), this.target, this.target, new NameAddress(this.user_profile.contact_url), this.sd.getEvent(), this.sd.getId(), null, null);
		}
		req.setExpiresHeader(new ExpiresHeader(184000));
		req.setHeader(new AcceptHeader("application/simple-message-summary"));
		this.currentSubscribeMessage = req;
		return req;
	}

	public void startMWI() {
		if (!this.alreadySubscribed) {
			Message req = getSubscribeMessage(false);
			if (PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean(Settings.PREF_MWI_ENABLED, true) && this.sd != null) {
				this.sd.subscribe(req);
			}
		}
	}

	void delayStartMWI() {
		if (this.subattempts < 3) {
			this.subattempts++;
			new Thread(new C10561()).start();
		}
	}

	public void onDlgSubscriptionSuccess(final SubscriberDialog subscriberDialog, int deltaSeconds, final String s, final Message message) {
		if (!this.alreadySubscribed) {
			this.alreadySubscribed = true;
			if (message.hasExpiresHeader()) {
				deltaSeconds = message.getExpiresHeader().getDeltaSeconds();
				if (deltaSeconds == 0) {
					return;
				}
			}
			else {
				deltaSeconds = 184000;
			}
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						synchronized (RegisterAgent.this.sd) {
//							RegisterAgent.this.sd.wait(deltaSeconds * 1000);
							RegisterAgent.this.alreadySubscribed = false;
							RegisterAgent.this.subattempts = 0;
							RegisterAgent.this.startMWI();
						}
					}
					catch (Exception ex) {}
				}
			}).start();
		}
	}

	public void onDlgSubscriptionFailure(SubscriberDialog dialog, int code, String reason, Message resp) {
		Message req = getSubscribeMessage(true);
		if (!handleAuthentication(code, resp, req) || this.subattempts >= 3) {
			delayStartMWI();
			return;
		}
		this.subattempts++;
		this.sd.subscribe(req);
	}

	public void onDlgSubscribeTimeout(SubscriberDialog dialog) {
		delayStartMWI();
	}

	public void onDlgSubscriptionTerminated(SubscriberDialog dialog) {
		this.alreadySubscribed = false;
		startMWI();
	}

	public void onDlgNotify(SubscriberDialog dialog, NameAddress target, NameAddress notifier, NameAddress contact, String state, String content_type, String body, Message msg) {
		if (PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean(Settings.PREF_MWI_ENABLED, true)) {
			Parser p = new Parser(body);
			char[] propertysep = new char[]{':', '\r', '\n'};
			char[] vmailsep = new char[]{'/'};
			char[] vmboxsep = new char[]{'@', '\r', '\n'};
			String vmaccount = null;
			boolean voicemail = false;
			int nummsg = 0;
			while (p.hasMore()) {
				String property = p.getWord(propertysep);
				p.skipChar();
				p.skipWSP();
				String value = p.getWord(Parser.CRLF);
				if (property.equalsIgnoreCase("Messages-Waiting") && value.equalsIgnoreCase("yes")) {
					voicemail = true;
				} else if (property.equalsIgnoreCase("Voice-Message")) {
					nummsg = Integer.parseInt(new Parser(value).getWord(vmailsep));
				} else if (property.equalsIgnoreCase("Message-Account")) {
					vmaccount = new Parser(value).getWord(vmboxsep);
				}
			}
			if (this.listener != null) {
				this.listener.onMWIUpdate(this, voicemail, nummsg, vmaccount);
			}
		}
	}

	public void onTransProvisionalResponse(TransactionClient transaction, Message resp) {
	}

	public void onTransSuccessResponse(TransactionClient transaction, Message resp) {
		if (transaction.getTransactionMethod().equals(BaseSipMethods.REGISTER)) {
			if (resp.hasAuthenticationInfoHeader()) {
				this.next_nonce = resp.getAuthenticationInfoHeader().getNextnonceParam();
			}
			StatusLine status = resp.getStatusLine();
			String result = status.getCode() + " " + status.getReason();
			int expires = 0;
			if (resp.hasExpiresHeader()) {
				expires = resp.getExpiresHeader().getDeltaSeconds();
			} else if (resp.hasContactHeader()) {
				Vector<Header> contacts = resp.getContacts().getHeaders();
				for (int i = 0; i < contacts.size(); i++) {
					int exp_i = new ContactHeader((Header) contacts.elementAt(i)).getExpires();
					if (exp_i > 0 && (expires == 0 || exp_i < expires)) {
						expires = exp_i;
					}
				}
			}
			printLog("Registration success: " + result, 1);
			if (this.CurrentState == 2) {
				this.CurrentState = 3;
				if (this.listener != null) {
					result = "";
					ContentTypeHeader cth = resp.getContentTypeHeader();
					if (cth != null && cth.getValue().equalsIgnoreCase("text/3ghandset")) {
						result = resp.getBody();
						String displayName = resp.getFromHeader().getNameAddress().getDisplayName();
						String userName = resp.getFromHeader().getNameAddress().getAddress().getUserName();
						Editor editor = SipUAApp.mContext.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).edit();
						editor.putString(MessageDialogueActivity.USER_NAME, userName);
						editor.commit();
					}
					this.listener.onUaRegistrationSuccess(this, this.target, this.contact, result, resp);
					Receiver.reRegister(expires);
					return;
				}
				return;
			}
			this.CurrentState = 1;
			if (this.listener != null) {
				this.listener.onUaRegistrationSuccess(this, this.target, this.contact, result, resp);
			}
		}
	}

	public void onTransFailureResponse(TransactionClient transaction, Message resp) {
		if (transaction.getTransactionMethod().equals(BaseSipMethods.REGISTER)) {
			StatusLine status = resp.getStatusLine();
			int code = status.getCode();
			if (!processAuthenticationResponse(transaction, resp, code)) {
				String result = new StringBuilder(String.valueOf(code)).append(" ").append(status.getReason()).toString();
				Header hd = resp.getHeader(BaseSipHeaders.Ptt_Extension);
				String result1 = "userOrpwderror";
				if (hd != null) {
					result1 = hd.getValue();
				} else {
					result1 = getReason(code);
				}
				if (this.CurrentState == 2) {
					this.CurrentState = 1;
					if (this.listener != null) {
						this.listener.onUaRegistrationFailure(this, this.target, this.contact, result1);
						Receiver.reRegister(20);
					}
				} else {
					this.CurrentState = 1;
					if (this.listener != null) {
						this.listener.onUaRegistrationSuccess(this, this.target, this.contact, result, resp);
					}
				}
				printLog("Registration failure: " + result, 1);
			}
		}
	}

	private String getReason(int code) {
		if (code == 450) {
			return "Already Login";
		}
		if (code == 480) {
			return "Temporarily Unavailable";
		}
		return "userOrpwderror";
	}

	private boolean generateRequestWithProxyAuthorizationheader(Message resp, Message req) {
		if (!resp.hasProxyAuthenticateHeader() || resp.getProxyAuthenticateHeader().getRealmParam().length() <= 0) {
			return false;
		}
		String str;
		UserAgentProfile userAgentProfile = this.user_profile;
		String realmParam = resp.getProxyAuthenticateHeader().getRealmParam();
		this.realm = realmParam;
		userAgentProfile.realm = realmParam;
		ProxyAuthenticateHeader pah = resp.getProxyAuthenticateHeader();
		String qop_options = pah.getQopOptionsParam();
		printLog("DEBUG: qop-options: " + qop_options, 3);
		if (qop_options != null) {
			str = "auth";
		} else {
			str = null;
		}
		this.qop = str;
		req.setProxyAuthorizationHeader(new DigestAuthentication(req.getTransactionMethod(), req.getRequestLine().getAddress().toString(), pah, this.qop, null, this.username, this.passwd).getProxyAuthorizationHeader());
		return true;
	}

	private boolean generateRequestWithWwwAuthorizationheader(Message resp, Message req) {
		if (!resp.hasWwwAuthenticateHeader() || resp.getWwwAuthenticateHeader().getRealmParam().length() <= 0) {
			return false;
		}
		String str;
		UserAgentProfile userAgentProfile = this.user_profile;
		String realmParam = resp.getWwwAuthenticateHeader().getRealmParam();
		this.realm = realmParam;
		userAgentProfile.realm = realmParam;
		WwwAuthenticateHeader wah = resp.getWwwAuthenticateHeader();
		String qop_options = wah.getQopOptionsParam();
		printLog("DEBUG: qop-options: " + qop_options, 3);
		if (qop_options != null) {
			str = "auth";
		} else {
			str = null;
		}
		this.qop = str;
		req.setAuthorizationHeader(new DigestAuthentication(req.getTransactionMethod(), req.getRequestLine().getAddress().toString(), wah, this.qop, null, this.username, this.passwd).getAuthorizationHeader());
		return true;
	}

	private boolean handleAuthentication(int respCode, Message resp, Message req) {
		switch (respCode) {
			case 401:
				return generateRequestWithWwwAuthorizationheader(resp, req);
			case 407:
				return generateRequestWithProxyAuthorizationheader(resp, req);
			default:
				return false;
		}
	}

	private boolean processAuthenticationResponse(TransactionClient transaction, Message resp, int respCode) {
		if (this.attempts < 3) {
			this.attempts++;
			Message req = transaction.getRequestMessage();
			req.setCSeqHeader(req.getCSeqHeader().incSequenceNumber());
			ViaHeader vh = req.getViaHeader();
			vh.setBranch(SipProvider.pickBranch());
			req.removeViaHeader();
			req.addViaHeader(vh);
			if (handleAuthentication(respCode, resp, req)) {
				this.f1079t = new TransactionClient(this.sip_provider, req, this, 30000);
				this.f1079t.request();
				return true;
			}
		}
		return false;
	}

	public void onTransTimeout(TransactionClient transaction) {
		if (transaction != null && transaction.getTransactionMethod().equals(BaseSipMethods.REGISTER)) {
			printLog("Registration failure: No response from server.", 1);
			if (Receiver.call_state == 3) {
				this.CurrentState = 3;
				if (this.listener != null) {
					this.listener.onUaRegistrationSuccess(this, this.target, this.contact, "", null);
					Receiver.reRegister(this.user_profile.expires);
				}
			} else if (this.CurrentState == 2) {
				this.CurrentState = 0;
				if (this.listener != null) {
					this.listener.onUaRegistrationFailure(this, this.target, this.contact, "Timeout");
					Receiver.reRegister(20);
				}
			} else if (this.pub.booleanValue() && System.getInt(Receiver.mContext.getContentResolver(), "airplane_mode_on", 0) == 0) {
				this.CurrentState = 0;
				if (this.listener != null) {
					this.listener.onUaRegistrationFailure(this, this.target, this.contact, "Timeout");
					Receiver.reRegister(20);
				}
			} else {
				this.CurrentState = 1;
				if (this.listener != null) {
					this.listener.onUaRegistrationSuccess(this, this.target, this.contact, "Timeout", null);
				}
			}
		}
	}

	void printLog(String str, int level) {
	}

	void printException(Exception e, int level) {
	}
}

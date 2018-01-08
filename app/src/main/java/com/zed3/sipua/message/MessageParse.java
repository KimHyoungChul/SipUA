package com.zed3.sipua.message;

import android.content.ContentValues;
import android.content.Context;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import com.zed3.log.MyLog;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.message.PhotoTransferReceiveActivity.PhotoReceiveMessage;
import com.zed3.sipua.welcome.AutoConfigManager;
import com.zed3.zhejiang.ZhejiangReceivier;

public class MessageParse {
	private static final String APPLICATION_OGG = "application/ogg";
	private static final String ATTACHMENTS = "attachments";
	private static final String AUDIO_AMR = "audio/amr";
	private static final String AUDIO_MP3 = "audio/mp3";
	private static final String AUDIO_MPEG = "audio/mpeg";
	private static final String AUDIO_WAV = "audio/wav";
	private static final String BOUNDARY = "\r\n";
	private static final String CONTENT_LENGTH = "content-length";
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String DOUBLE_BOUNDARY = "\r\n\r\n";
	private static final String FILE_NAME = "filename";
	private static final String IMAGE_BMP = "image/**";
	private static final String IMAGE_JPEG = "image/jpeg";
	private static final String IMAGE_JPG = "image/jpg";
	private static final String MMS_VERSION = "MMS-Version";
	private static final String TABLE_MMS_INBOX = "mms_inbox";
	private static final String TAG = "MessageParse";
	private static final String TEXT_PLAIN = "text/plain";
	private static int body_length = 0;
	private String E_id = null;
	private int attachemnt_length;
	private int attachment_count;
	private String attachments = null;
	private String attachments_uri = null;
	private String body = null;
	private String body_type;
	private String content_type;
	private SmsMmsDatabase database;
	private String file_name = null;
	private Context mContext;
	private String mms_header = null;
	private String mms_version;
	private String recipient_num = null;

	public MessageParse(Context context, String E_id, String recipient_num) {
		this.mContext = context;
		this.E_id = E_id;
		this.recipient_num = recipient_num;
	}

	public static byte[] getBitmap(String imgBase64Str) {
		byte[] bytes = null;
		try {
			bytes = Base64.decode(imgBase64Str, 2);
		} catch (Exception e) {
			MyLog.e(TAG, "getBitmap error: ");
			e.printStackTrace();
		}
		return bytes;
	}

	public Message saveMmsInfoToInbox() {
		Exception e;
		if (this.attachments_uri == null || this.file_name == null) {
			return null;
		}
		try {
			this.database = new SmsMmsDatabase(this.mContext);
			ContentValues values = new ContentValues();
			try {
				values.put("E_id", this.E_id);
				values.put(MmsMessageDetailActivity.MESSAGE_BODY, this.body);
				values.put("sip_name", this.recipient_num);
				values.put("attachment", this.attachments_uri);
				values.put("attachment_name", this.file_name);
				values.put(ZhejiangReceivier.STATUS, Integer.valueOf(0));
				Message messageResult = Message.obtain();
				PhotoReceiveMessage message = new PhotoReceiveMessage();
				message.mPhotoPath = this.attachments_uri;
				message.mBody = this.body;
				message.mSipName = this.recipient_num;
				message.mReceiveTime = new MessageSender(this.mContext).getCurrentTime();
				values.put("type", "mms");
				values.put("mark", "0");
				values.put("date", message.mReceiveTime);
				AutoConfigManager autoConfigManager = new AutoConfigManager(SipUAApp.getAppContext());
				values.put("server_ip", autoConfigManager.fetchLocalServer());
				values.put("local_number", autoConfigManager.fetchLocalUserName());
				this.database.insert(SmsMmsDatabase.TABLE_MESSAGE_TALK, values);
				message.sendToTarget();
				messageResult.obj = message;
				return messageResult;
			} catch (Exception e2) {
				e = e2;
				ContentValues contentValues = values;
				MyLog.e(TAG, "saveMmsInfoToInbox error:");
				Log.i("xxxx", "MessageParse#saveMmsInfoToInbox exception = " + e.getMessage());
				e.printStackTrace();
				return null;
			}
		} catch (Exception e3) {
			e = e3;
			MyLog.e(TAG, "saveMmsInfoToInbox error:");
			Log.i("xxxx", "MessageParse#saveMmsInfoToInbox exception = " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public void createAttachmentFile(String attachments) {
		// TODO
	}

	private void parseMmsHeader(String msg_header) {
		if (msg_header != null) {
			try {
				String[] header_array = msg_header.split(BOUNDARY);
				for (int i = 0; i < header_array.length; i++) {
					String line = header_array[i];
					if (line.indexOf(MMS_VERSION) >= 0) {
						this.mms_version = "1.0";
					}
					if (line.indexOf(ATTACHMENTS) > 0) {
						this.attachment_count = Integer.parseInt(line.substring(line.indexOf("=") + 1));
					}
					if (line.indexOf("Content-Type") > 0) {
						if (line.indexOf(AUDIO_AMR) > 0) {
							this.content_type = AUDIO_AMR;
						} else if (line.indexOf(AUDIO_MP3) > 0 || line.indexOf(AUDIO_MPEG) > 0 || line.indexOf(AUDIO_WAV) > 0) {
							this.content_type = AUDIO_MP3;
						} else if (line.indexOf(TEXT_PLAIN) > 0) {
							this.body_type = TEXT_PLAIN;
						} else if (line.indexOf(IMAGE_JPEG) > 0) {
							this.content_type = IMAGE_JPEG;
						} else if (line.indexOf(IMAGE_JPG) > 0) {
							this.content_type = IMAGE_JPG;
						} else if (line.indexOf(IMAGE_BMP) > 0) {
							this.content_type = IMAGE_BMP;
						} else {
							MyLog.i(TAG, "unsupported content_type");
							return;
						}
					}
					if (line.indexOf(CONTENT_LENGTH) > 0) {
						String file_value = line.split(";")[1];
						if (file_value != null) {
							int index = file_value.indexOf("=");
							if (i == 1) {
								body_length = Integer.parseInt(file_value.substring(index + 1));
							} else {
								this.attachemnt_length = Integer.parseInt(file_value.substring(index + 1));
							}
						}
					}
					if (line.indexOf(FILE_NAME) > 0) {
						this.file_name = line.substring(line.lastIndexOf("=") + 1);
					}
				}
				MyLog.i(TAG, "parseMmsHeader: body_length = " + body_length + "\n" + "attachemnt_length = " + this.attachemnt_length + "\n" + "file_name = " + this.file_name);
			} catch (Exception e) {
				MyLog.e(TAG, "parseMmsHeader error: ");
				e.printStackTrace();
			}
		}
	}

	private void parseMmsBody(String msg_body) {
		if (msg_body != null) {
			try {
				this.body = getMmsTextBody(msg_body);
				MyLog.i(TAG, "body = " + this.body);
				this.attachments = msg_body.substring(this.body.length());
				createAttachmentFile(this.attachments);
			} catch (Exception e) {
				MyLog.i(TAG, "parseMmsBody error: ");
				e.printStackTrace();
			}
		}
	}

	private String getMmsTextBody(String msg_body) {
		String str = null;
		if (body_length == 0) {
			return "";
		}
		int i = 1;
		while (i < 500) {
			try {
				byte[] body_byte = msg_body.substring(0, i).getBytes("GBK");
				MyLog.i(TAG, "body_byte.length = " + body_byte.length);
				if (body_byte.length == body_length) {
					str = new String(body_byte, "GBK");
					break;
				}
				i++;
			} catch (Exception e) {
				MyLog.i(TAG, "getMmsTextBody error: ");
				e.printStackTrace();
			}
		}
		return str;
	}

	public int parseMmsInfoFromTxt(byte[] bytes) {
		if (bytes == null) {
			return 0;
		}
		try {
			String[] info = new String(bytes, "UTF-8").split(DOUBLE_BOUNDARY);
			parseMmsHeader(info[0]);
			parseMmsBody(info[1]);
			return 1;
		} catch (Exception e) {
			MyLog.e(TAG, "parseMmsInfoFromTxt error:");
			e.printStackTrace();
			return 0;
		}
	}

	public String getContentType() {
		return this.content_type;
	}
}

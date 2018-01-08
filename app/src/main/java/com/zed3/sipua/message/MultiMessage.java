package com.zed3.sipua.message;

import android.content.Context;
import android.net.Uri;

public class MultiMessage {
	private static final String ATTACHMENTS_COUNT = "attachments=";
	private static final String BOUNDARY = "\r\n";
	private static final String CONTENT_LENGTH = "content-length=";
	private static final String CONTENT_TYPE = "Content-Type:";
	public static final String CONTENT_TYPE_APPLICATION_STREAM = "application/octet-stream";
	private static final String FILE_NAME = "filename=";
	private static final String MMS_VERSION = "MMS-Version:1.0";
	private static final String TAG = "MultiMessage";
	private int attachment_count;
	private long attachment_length;
	private String attachment_type;
	private long body_length;
	private String body_type;
	private String file_name;
	StringBuffer headBuffer;
	private Uri mAttachmentUri;
	private Context mContext;

	public MultiMessage(final Context mContext) {
		this.headBuffer = new StringBuffer();
		this.mContext = mContext;
	}

	public long getAttachment_length() {
		return this.attachment_length;
	}

	public String getAttachment_type() {
		return this.attachment_type;
	}

	public int getAttachments() {
		return this.attachment_count;
	}

	public long getBody_length() {
		return this.body_length;
	}

	public String getBody_type() {
		return this.body_type;
	}

	public String getFile_name() {
		return this.file_name;
	}

	public String getMessageHeader() {
		if (this.attachment_count > 0) {
			this.headBuffer.append("MMS-Version:1.0");
			this.headBuffer.append(";");
			this.headBuffer.append("attachments=");
			this.headBuffer.append(this.attachment_count);
			this.headBuffer.append("\r\n");
			if (this.body_type != null && !this.body_type.equals("")) {
				this.headBuffer.append("Content-Type:");
				this.headBuffer.append(this.body_type);
				this.headBuffer.append(";");
				this.headBuffer.append("content-length=");
				this.headBuffer.append(this.body_length);
				this.headBuffer.append("\r\n");
			}
			if (this.attachment_type != null && !this.attachment_type.equals("")) {
				this.headBuffer.append("Content-Type:");
				this.headBuffer.append(this.attachment_type);
				this.headBuffer.append(";");
				this.headBuffer.append("content-length=");
				this.headBuffer.append(this.attachment_length);
				this.headBuffer.append(";");
				this.headBuffer.append("filename=");
				this.headBuffer.append(this.file_name);
				this.headBuffer.append("\r\n");
				this.headBuffer.append("\r\n");
			}
		}
		return this.headBuffer.toString();
	}

	public Uri getmAttachmentUri() {
		return this.mAttachmentUri;
	}

	public void setAttachment_length(final long attachment_length) {
		this.attachment_length = attachment_length;
	}

	public void setAttachment_type(final String attachment_type) {
		this.attachment_type = attachment_type;
	}

	public void setAttachments(final int attachment_count) {
		this.attachment_count = attachment_count;
	}

	public void setBody_length(final long body_length) {
		this.body_length = body_length;
	}

	public void setBody_type(final String body_type) {
		this.body_type = body_type;
	}

	public void setFile_name(final String file_name) {
		this.file_name = file_name;
	}

	public void setmAttachmentUri(final Uri mAttachmentUri) {
		this.mAttachmentUri = mAttachmentUri;
	}
}

package com.zed3.log;

public interface IAppender {
	void append(GKLoggingEvent gKLoggingEvent);

	GKLayout getLayout();

	String getTag();

	void setLayout(GKLayout gKLayout);

	void setNull();

	void setTag(String str);
}

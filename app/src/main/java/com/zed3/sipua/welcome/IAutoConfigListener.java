package com.zed3.sipua.welcome;

public interface IAutoConfigListener {
	void AccountDisabled();

	void FetchConfigFailed();

	void ParseConfigOK();

	void TimeOut();

	void parseFailed();
}

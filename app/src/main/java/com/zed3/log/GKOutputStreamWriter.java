package com.zed3.log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

public class GKOutputStreamWriter extends Writer {
	private final OutputStreamWriter se;

	public GKOutputStreamWriter(OutputStream out) {
		this.se = new OutputStreamWriter(out);
	}

	public GKOutputStreamWriter(OutputStream out, String charsetName) throws UnsupportedEncodingException {
		this.se = new OutputStreamWriter(out, charsetName);
	}

	public String getEncoding() {
		return this.se.getEncoding();
	}

	public void flush() throws IOException {
		this.se.flush();
	}

	public void write(String string) throws IOException {
		this.se.write(string);
	}

	public void write(String string, int off, int len) throws IOException {
		this.se.write(string, off, len);
	}

	public void write(int c) throws IOException {
		this.se.write(c);
	}

	public void write(char[] cbuff, int off, int len) throws IOException {
		this.se.write(cbuff, off, len);
	}

	public void close() throws IOException {
		this.se.close();
	}
}

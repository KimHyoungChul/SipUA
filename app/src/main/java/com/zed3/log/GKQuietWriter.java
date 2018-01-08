package com.zed3.log;

import java.io.IOException;
import java.io.Writer;

public class GKQuietWriter extends Writer {
	protected final Writer out;

	public GKQuietWriter(Writer out) {
		this.out = out;
	}

	public void write(String string) {
		try {
			this.out.write(string);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void write(String string, int off, int len) {
		try {
			this.out.write(string, off, len);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void write(int c) {
		try {
			this.out.write(c);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void write(char[] cbuff, int off, int len) {
		try {
			this.out.write(cbuff, off, len);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			this.out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void flush() {
		try {
			this.out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

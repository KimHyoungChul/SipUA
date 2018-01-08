package com.zed3.log;

import java.io.IOException;
import java.io.Writer;

public class GKCountingQuietWriter extends GKQuietWriter {
	protected long count;

	public GKCountingQuietWriter(Writer out) {
		super(out);
	}

	public void write(String string) {
		try {
			this.out.write(string);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.count += (long) string.length();
	}

	public long getCount() {
		return this.count;
	}

	public void setCount(long count) {
		this.count = count;
	}
}

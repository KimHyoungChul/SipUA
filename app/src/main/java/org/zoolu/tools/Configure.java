package org.zoolu.tools;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Configure {
	public static String NONE;
	Configurable configurable;

	static {
		Configure.NONE = "NONE";
	}

	protected Configure() {
		this.configurable = null;
	}

	public Configure(final Configurable configurable, final String s) {
		this.configurable = configurable;
		this.loadFile(s);
	}

	protected void loadFile(final String s) {
		if (s == null) {
			return;
		}
		// TODO
	}

	protected void parseLine(final String s) {
	}

	protected void saveFile(final String s) {
		if (s == null) {
			return;
		}
		try {
			final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(s));
			bufferedWriter.write(this.toLines());
			bufferedWriter.close();
		} catch (IOException ex) {
			System.err.println("ERROR writing on file \"" + s + "\"");
		}
	}

	protected String toLines() {
		return "";
	}
}

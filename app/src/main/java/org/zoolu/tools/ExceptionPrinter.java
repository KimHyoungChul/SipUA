package org.zoolu.tools;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ExceptionPrinter {
	public static String getStackTraceOf(final Exception ex) {
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ex.printStackTrace(new PrintStream(byteArrayOutputStream));
		return byteArrayOutputStream.toString();
	}
}

package org.zoolu.tools;

import java.io.PrintStream;
import java.util.Date;

public class Log {
	public static final long MAX_SIZE = 1048576L;
	long counter;
	boolean do_log;
	String log_tag;
	long max_size;
	PrintStream out_stream;
	int tag_size;
	int verbose_level;

	public Log(final PrintStream printStream, final String s, final int n) {
		this.init(printStream, s, n, -1L);
	}

	public Log(final String s, final String s2, final int n) {
		final PrintStream printStream = null;
		if (n <= 0) {
			return;
		}
		// TODO
	}

	public Log(final String s, final String s2, final int n, final long n2) {
		final PrintStream printStream = null;
		// TODO
		this.init(null, s2, 0, 0L);
		this.do_log = false;
	}

	public Log(final String s, final String s2, final int n, final long n2, final boolean b) {
		final PrintStream printStream = null;
		// TODO
		this.init(null, s2, 0, 0L);
		this.do_log = false;
	}

	public void close() {
		this.do_log = false;
		this.out_stream.close();
	}

	protected Log flush() {
		if (this.verbose_level > 0) {
			this.out_stream.flush();
		}
		return this;
	}

	protected void init(final PrintStream out_stream, final String log_tag, final int verbose_level, final long max_size) {
		this.out_stream = out_stream;
		this.log_tag = log_tag;
		this.verbose_level = verbose_level;
		this.max_size = max_size;
		if (log_tag != null) {
			this.tag_size = log_tag.length() + 2;
		} else {
			this.tag_size = 0;
		}
		this.do_log = true;
		this.counter = 0L;
	}

	public Log print(final String s) {
		return this.print(s, 1);
	}

	public Log print(final String s, final int n) {
		if (this.do_log && n <= this.verbose_level) {
			if (this.log_tag != null) {
				this.out_stream.print(String.valueOf(this.log_tag) + ": " + s);
			} else {
				this.out_stream.print(s);
			}
			if (this.max_size >= 0L) {
				this.counter += this.tag_size + s.length();
				if (this.counter > this.max_size) {
					this.out_stream.println("\r\n----MAXIMUM LOG SIZE----\r\nSuccessive logs are lost.");
					this.do_log = false;
				}
			}
		}
		return this;
	}

	public Log printException(final Exception ex) {
		return this.printException(ex, 1);
	}

	public Log printException(final Exception ex, final int n) {
		return this.println("Exception: " + ExceptionPrinter.getStackTraceOf(ex), n);
	}

	public Log printPacketTimestamp(String s, String s2, final int n, final int n2, final String s3, final int n3) {
		s2 = (s = String.valueOf(s2) + ":" + n + "/" + s + " (" + n2 + " bytes)");
		if (s3 != null) {
			s = String.valueOf(s2) + ": " + s3;
		}
		this.println(String.valueOf(DateFormat.formatHHMMSS(new Date())) + ", " + s, n3);
		return this;
	}

	public Log println(final String s) {
		return this.println(s, 1);
	}

	public Log println(final String s, final int n) {
		return this.print(String.valueOf(s) + "\r\n", n).flush();
	}
}

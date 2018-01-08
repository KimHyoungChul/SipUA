package com.zed3.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public abstract class GKFileAppender extends GKWriterAppender {
	protected int bufferSize;
	protected boolean bufferedIO;
	protected volatile String fileName;
	protected boolean isAppend;

	public GKFileAppender() throws IOException {
		this.fileName = null;
		this.isAppend = true;
		this.bufferSize = 8192;
		this.bufferedIO = false;
		setLayout(new GKSimpleLayout());
		setFile(GKFileUtils.fatchFullFileName(), true, true, this.bufferSize);
	}

	public GKFileAppender(String tag) throws IOException {
		this();
		setTag(tag);
	}

	public GKFileAppender(GKLayout layout, String fileName, boolean isAppend) throws IOException {
		this.fileName = null;
		this.isAppend = true;
		this.bufferSize = 8192;
		this.bufferedIO = false;
		setLayout(layout);
		setFile(fileName, isAppend, false, this.bufferSize);
	}

	public GKFileAppender(String tag, GKLayout layout, String fileName, boolean isAppend) throws IOException {
		this(layout, fileName, isAppend);
		setTag(tag);
	}

	public GKFileAppender(GKLayout layout, String fileName) throws IOException {
		this.fileName = null;
		this.isAppend = true;
		this.bufferSize = 8192;
		this.bufferedIO = false;
		setLayout(layout);
		setFile(fileName, true, false, this.bufferSize);
	}

	public GKFileAppender(String tag, GKLayout layout, String fileName) throws IOException {
		this(layout, fileName);
		setTag(tag);
	}

	public GKFileAppender(GKLayout layout) throws IOException {
		this.fileName = null;
		this.isAppend = true;
		this.bufferSize = 8192;
		this.bufferedIO = false;
		setLayout(layout);
		setFile(GKFileUtils.fatchFullFileName(), true, false, this.bufferSize);
	}

	public GKFileAppender(String tag, GKLayout layout) throws IOException {
		this(layout);
		setTag(tag);
	}

	public GKFileAppender(GKLayout layout, OutputStream outputStream) {
		super(layout, outputStream);
		this.fileName = null;
		this.isAppend = true;
		this.bufferSize = 8192;
		this.bufferedIO = false;
	}

	public GKFileAppender(String tag, GKLayout layout, OutputStream outputStream) {
		super(layout, outputStream);
		this.fileName = null;
		this.isAppend = true;
		this.bufferSize = 8192;
		this.bufferedIO = false;
		setTag(tag);
	}

	public void append(GKLoggingEvent event) {
		if (!(this.fileName == null || GKFileUtils.isFileExits(this.fileName))) {
			try {
				setFile(this.fileName, this.isAppend, this.bufferedIO, this.bufferSize);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		super.append(event);
	}

	public GKFileAppender(GKLayout layout, String fileName, boolean isAppend, int bufferSize, boolean bufferedIO) throws IOException {
		this.fileName = null;
		this.isAppend = true;
		this.bufferSize = 8192;
		this.bufferedIO = false;
		setLayout(layout);
		setFile(fileName, isAppend, bufferedIO, bufferSize);
	}

	public synchronized void setFile(String fileName, boolean isAppend, boolean bufferedIO, int bufferSize) throws IOException {
		FileOutputStream outputStream;
		if (bufferedIO) {
			setImmediateFlush(false);
		}
		reset();
		try {
			outputStream = new FileOutputStream(fileName, isAppend);
		} catch (FileNotFoundException e) {
			String parentName = new File(fileName).getParent();
			if (parentName != null) {
				File parent = new File(parentName);
				if (parent.exists() || !parent.mkdirs()) {
					throw e;
				}
				outputStream = new FileOutputStream(fileName, isAppend);
			} else {
				throw e;
			}
		}
		Writer writer = createWriter(outputStream);
		if (bufferedIO) {
			writer = new BufferedWriter(writer, bufferSize);
		}
		setQWForFiles(writer);
		this.fileName = fileName;
		this.isAppend = isAppend;
		this.bufferedIO = bufferedIO;
		this.bufferSize = bufferSize;
		writeHeader();
	}

	protected void setQWForFiles(Writer writer) {
		this.qw = new GKQuietWriter(writer);
	}

	protected void closeFile() {
		close();
	}

	protected void reset() {
		this.fileName = null;
		super.reset();
	}
}

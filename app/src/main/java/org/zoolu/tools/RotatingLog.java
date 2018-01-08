package org.zoolu.tools;

import java.io.File;
import java.util.Calendar;

public class RotatingLog extends Log {
	public static final int DAY = 5;
	public static final int HOUR = 10;
	public static final int MINUTE = 12;
	public static final int MONTH = 2;
	String file_name;
	long next_rotation;
	int num_rotations;
	int time_scale;
	int time_value;

	public RotatingLog(final String s, final String s2, final int n, final long n2, final int n3, final int n4, final int n5) {
		super(s, s2, n, n2);
		this.rInit(s, n3, n4, n5);
	}

	private void rInit(final String file_name, final int num_rotations, final int time_scale, final int time_value) {
		this.file_name = file_name;
		this.num_rotations = num_rotations;
		this.time_scale = time_scale;
		this.time_value = time_value;
		this.updateNextRotationTime();
	}

	private static void rename(final String s, final String s2) {
		final File file = new File(s);
		if (file.exists()) {
			final File file2 = new File(s2);
			if (file2.exists()) {
				file2.delete();
			}
			file.renameTo(file2);
		}
	}

	private void updateNextRotationTime() {
		final Calendar instance = Calendar.getInstance();
		instance.add(this.time_scale, this.time_value);
		this.next_rotation = instance.getTime().getTime();
	}

	@Override
	public Log print(final String s, final int n) {
		if (Calendar.getInstance().getTime().getTime() > this.next_rotation) {
			this.rotate();
			this.updateNextRotationTime();
		}
		return super.print(s, n);
	}

	public RotatingLog rotate() {
		if (this.num_rotations <= 0) {
			return this;
		}
		int n = this.num_rotations - 2;
		// TODO
		return null;
	}
}

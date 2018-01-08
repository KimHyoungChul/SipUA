package org.zoolu.tools;

import java.util.Vector;

public class Iterator {
	int i;
	Vector<Object> v;

	public Iterator(final Vector<Object> v) {
		this.v = v;
		this.i = -1;
	}

	public boolean hasNext() {
		return this.i < this.v.size() - 1;
	}

	public Object next() {
		final int i = this.i + 1;
		this.i = i;
		if (i < this.v.size()) {
			return this.v.elementAt(this.i);
		}
		return null;
	}

	public void remove() {
		this.v.removeElementAt(this.i);
		--this.i;
	}
}

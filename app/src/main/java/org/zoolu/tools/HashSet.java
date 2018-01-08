package org.zoolu.tools;

import java.util.Vector;

public class HashSet {
	Vector<Object> set;

	public HashSet() {
		this.set = new Vector<Object>();
	}

	public boolean add(final Object o) {
		this.set.addElement(o);
		return true;
	}

	public boolean contains(final Object o) {
		return this.set.contains(o);
	}

	public boolean isEmpty() {
		return this.set.isEmpty();
	}

	public Iterator iterator() {
		return new Iterator(this.set);
	}

	public boolean remove(final Object o) {
		return this.set.removeElement(o);
	}

	public int size() {
		return this.set.size();
	}
}

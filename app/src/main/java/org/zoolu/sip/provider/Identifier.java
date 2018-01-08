package org.zoolu.sip.provider;

public class Identifier {
	String id;

	Identifier() {
	}

	Identifier(final String id) {
		this.id = id;
	}

	Identifier(final Identifier identifier) {
		this.id = identifier.id;
	}

	@Override
	public boolean equals(final Object o) {
		try {
			return this.id.equals(((Identifier) o).id);
		} catch (Exception ex) {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

	@Override
	public String toString() {
		return this.id;
	}
}

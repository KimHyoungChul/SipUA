package org.zoolu.sip.dialog;

import org.zoolu.sip.address.NameAddress;

import java.util.Vector;

public class DialogInfo {
	String call_id;
	NameAddress local_contact;
	long local_cseq;
	NameAddress local_name;
	String local_tag;
	NameAddress remote_contact;
	long remote_cseq;
	NameAddress remote_name;
	String remote_tag;
	Vector<NameAddress> route;

	public DialogInfo() {
		this.local_name = null;
		this.remote_name = null;
		this.local_contact = null;
		this.remote_contact = null;
		this.call_id = null;
		this.local_tag = null;
		this.remote_tag = null;
		this.local_cseq = -1L;
		this.remote_cseq = -1L;
		this.route = null;
	}

	public String getCallID() {
		return this.call_id;
	}

	public long getLocalCSeq() {
		return this.local_cseq;
	}

	public NameAddress getLocalContact() {
		return this.local_contact;
	}

	public NameAddress getLocalName() {
		return this.local_name;
	}

	public String getLocalTag() {
		return this.local_tag;
	}

	public long getRemoteCSeq() {
		return this.remote_cseq;
	}

	public NameAddress getRemoteContact() {
		return this.remote_contact;
	}

	public NameAddress getRemoteName() {
		return this.remote_name;
	}

	public String getRemoteTag() {
		return this.remote_tag;
	}

	public Vector<NameAddress> getRoute() {
		return this.route;
	}

	public void incLocalCSeq() {
		++this.local_cseq;
	}

	public void incRemoteCSeq() {
		++this.remote_cseq;
	}

	public void setCallID(final String call_id) {
		this.call_id = call_id;
	}

	public void setLocalCSeq(final long local_cseq) {
		this.local_cseq = local_cseq;
	}

	public void setLocalContact(final NameAddress local_contact) {
		this.local_contact = local_contact;
	}

	public void setLocalName(final NameAddress local_name) {
		this.local_name = local_name;
	}

	public void setLocalTag(final String local_tag) {
		this.local_tag = local_tag;
	}

	public void setRemoteCSeq(final long remote_cseq) {
		this.remote_cseq = remote_cseq;
	}

	public void setRemoteContact(final NameAddress remote_contact) {
		this.remote_contact = remote_contact;
	}

	public void setRemoteName(final NameAddress remote_name) {
		this.remote_name = remote_name;
	}

	public void setRemoteTag(final String remote_tag) {
		this.remote_tag = remote_tag;
	}

	public void setRoute(final Vector<NameAddress> route) {
		this.route = route;
	}
}

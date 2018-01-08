package com.zed3.sipua;

import com.zed3.log.MyLog;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TimeOutSyncBufferQueue<T> implements Collection<T> {
	private ConcurrentSkipListSet<T> listSet;
	private BlockingQueue<T> storage;

	public TimeOutSyncBufferQueue() {
		this.storage = new LinkedBlockingQueue<T>(1024);
		this.listSet = new ConcurrentSkipListSet<T>();
	}

	@Override
	public boolean add(final T t) {
		return this.storage.add(t);
	}

	@Override
	public boolean addAll(final Collection<? extends T> collection) {
//		return this.storage.addAll((Collection<?>) collection);
		return false;
	}

	@Override
	public void clear() {
		this.storage.clear();
	}

	@Override
	public boolean contains(final Object o) {
		return this.storage.contains(o);
	}

	@Override
	public boolean containsAll(final Collection<?> collection) {
		return this.storage.containsAll(collection);
	}

	@Override
	public boolean isEmpty() {
		return this.storage.isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		return this.storage.iterator();
	}

	public T pop() throws InterruptedException {
		MyLog.i("BlockingQueue", "thread2 pop called");
		return this.storage.take();
	}

	public T pop(final long n) throws InterruptedException {
		MyLog.i("BlockingQueue", "thread3 pop called,size = " + this.storage.size());
		return this.storage.poll(n, TimeUnit.MILLISECONDS);
	}

	public boolean push(final T t) throws InterruptedException {
		MyLog.i("BlockingQueue", "thread1 push called,size=" + this.storage.size());
		return this.storage.offer(t);
	}

	@Override
	public boolean remove(final Object o) {
		return this.storage.remove(o);
	}

	@Override
	public boolean removeAll(final Collection<?> collection) {
		return this.storage.removeAll(collection);
	}

	@Override
	public boolean retainAll(final Collection<?> collection) {
		return this.storage.retainAll(collection);
	}

	@Override
	public int size() {
		return this.storage.size();
	}

	@Override
	public Object[] toArray() {
		return this.storage.toArray();
	}

	@Override
	public <T> T[] toArray(final T[] array) {
		return this.storage.toArray(array);
	}
}

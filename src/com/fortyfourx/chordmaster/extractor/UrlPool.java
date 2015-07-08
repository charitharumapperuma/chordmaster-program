package com.fortyfourx.chordmaster.extractor;

import java.util.ArrayList;
import java.util.List;

public class UrlPool {
	public static final long TIMEOUT = 300000; // 5 minutes

	private List<String> pool;
	private int index; // indexed to last returned object
	private long lastAddedTime;

	public UrlPool() {
		this.pool = new ArrayList<String>();
		this.index = -1;
	}

	public synchronized String add(String url) {
		this.pool.add(url);
		this.lastAddedTime = System.currentTimeMillis();
		return url;
	}

	public synchronized void addAll(List<String> urls) {
		for (String url : urls) {
			this.add(url);
		}
		System.out.println("url pool size = " + this.pool.size());
	}
	
	public synchronized String next() {
		index++;
		return this.pool.get(index);
	}

	public boolean hasNext() {
		if (this.index + 1 >= this.pool.size()) {
			return false;
		}
		return true;
	}

	public boolean isExpired() {
		if (System.currentTimeMillis() - this.lastAddedTime > UrlPool.TIMEOUT) {
			return true;
		}
		return false;
	}

	public int size() {
		return this.pool.size();
	}
}

package com.log4js3.logging;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class DeamonThreadFactory implements ThreadFactory {
	ThreadFactory defaultThreadFactory = Executors.defaultThreadFactory();
	public Thread newThread(Runnable r) {
		Thread t = defaultThreadFactory.newThread(r);
		t.setDaemon(true);
		return t;
	}
}
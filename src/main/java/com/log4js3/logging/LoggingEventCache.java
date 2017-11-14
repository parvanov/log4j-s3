package com.log4js3.logging;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * An event cache that buffers/collects events and publishes them in a
 * background thread when the buffer fills up.
 *
 * @author Van Ly (vancly@hotmail.com)
 * @author Plamen Parvanov
 *
 */
public class LoggingEventCache {
	public static final String PUBLISH_THREAD_NAME =
		"LoggingEventCache-publish-thread";

	/**
	 * Interface for a publishing collaborator
	 *

	 *
	 */
	public interface ICachePublisher {

		/**
		 * Create a context for a batch of events. Context may be reused in case logs need to be overwritten.
		 * @param cacheName the name for the batch of events
		 * @return a context for subsequent operations
		 */
		PublishContext createContext(final String cacheName);

		/**
		 * Start a batch of events with the given context.
		 * @param ctx a context to reuse
		 */
		void startPublish(PublishContext ctx);


		/**
		 * Publish an event in the batch
		 *
		 * @param context the context for this batch
		 * @param log the log to publish
		 */
		void publish(final PublishContext context, String log);

		/**
		 * Concludes a publish batch.  Implementations should submit/commit
		 * a batch and/or clean up resources in preparation for the next
		 * batch.
		 *
		 * @param context the context for this batch
		 */
		void endPublish(final PublishContext context);
	}

	private final String cacheName;
	private final int capacity;

	private final Object lock = new Object();

	// Supposedly Log4j already takes care of concurrency for us, so theoretically
	// we do not need the EVENTQUEUELOCK around {eventQueue, eventQueueLength}
	// (or the need to use a ConcurrentLinkedQueue as opposed to just a normal
	// List).  Dunno.  To be safe, I am using them.
	private StringBuffer logBuffer = new StringBuffer();
	private volatile int eventQueueLength = 0;

	private final ICachePublisher cachePublisher;
	private final ScheduledExecutorService executorService;

	private volatile PublishContext reuseContext;

	/**
	 * Creates an instance with the provided cache publishing collaborator.
	 * The instance will create a buffer of the capacity specified and will
	 * publish a batch when collected events reach that capacity.
	 *
	 * @param cacheName name for the cache
	 * @param capacity the capacity of the buffer for events before the buffer
	 * is published
	 * @param autoFlushInterval
	 * @param cachePublisher the publishing collaborator
	 * @param layout
	 */
	public LoggingEventCache(String cacheName, int capacity,
			int autoFlushInterval, ICachePublisher cachePublisher) {
		this.cacheName = cacheName;
		this.capacity = capacity;
		this.cachePublisher = cachePublisher;
		executorService = createExecutorService();
		scheduleAutoFlusher(autoFlushInterval);
	}

	private void scheduleAutoFlusher(int autoFlushInterval) {
		if(autoFlushInterval>0)
			executorService.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					flushAndPublishQueue(true, true);
				}
			}, autoFlushInterval, autoFlushInterval, TimeUnit.SECONDS);
	}

	public void close() {
		flushAndPublishQueue(true, false);
		executorService.shutdown();//to cancel the auto-flusher
	}

	ScheduledExecutorService createExecutorService() {
		return Executors.newSingleThreadScheduledExecutor();
	}

	/**
	 * Retrieves the name of the cache
	 *
	 * @return
	 */
	public String getCacheName() {
		return cacheName;
	}

	/**
	 * Adds a log event to the cache.  If the number of events reach the
	 * capacity of the batch, they will be published.
	 *
	 * @param log the log to add to the cache.
	 */
	public void add(String log) {
		synchronized(lock) {
			logBuffer.append(log);
			eventQueueLength++;
			if (eventQueueLength < capacity) return;
		}
		flushAndPublishQueue(false, false);
	}

	/**
	 * Publish the current staging log to remote stores if the staging log
	 * is not empty.
	 *
	 */
	public void flushAndPublishQueue(boolean block, boolean keepOpen) {
		String logsToPublish;
		synchronized(lock) {
			if (eventQueueLength <= 0) return;
			logsToPublish = logBuffer.toString();
			if(!keepOpen) {
				logBuffer = new StringBuffer();
				eventQueueLength = 0;
			}
		}
		Future<Boolean> f = publishCache(cacheName, logsToPublish, keepOpen);
		if (block) {
			try {
				f.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
	}

	Future<Boolean> publishCache(final String name, final String logsToPublish, boolean keepOpen) {
		Future<Boolean> f = executorService.submit(new Callable<Boolean>() {
			public Boolean call() {
				Thread.currentThread().setName(PUBLISH_THREAD_NAME);
				PublishContext ctx = reuseContext;//republish if last is open
				if(ctx==null) ctx = cachePublisher.createContext(cacheName);
				cachePublisher.startPublish(ctx);
				cachePublisher.publish(ctx, logsToPublish);
				cachePublisher.endPublish(ctx);
				reuseContext = keepOpen ? ctx : null;//keep context open for next republish
				return true;
			}
		});
		return f;
	}
}

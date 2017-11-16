package com.log4js3.logging.log4j;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.log4js3.logging.LoggingEventCache.ICachePublisher;
import com.log4js3.logging.PublishContext;

/**
 * Implementation to standardize on a cache name and aggregate and coordinate
 * multiple IPublishHelpers to publish content to different destinations.
 *
 * @author Van Ly (vancly@hotmail.com)
 *
 */
public class CachePublisher implements ICachePublisher {
	private final String hostName;
	private final String[] tags;
	private final boolean gzip;

	private List<IPublishHelper> helpers = new LinkedList<IPublishHelper>();

	public CachePublisher(String hostName, String[] tags, boolean gzip) {
		this.hostName = hostName;
		this.tags = tags;
		this.gzip = gzip;
	}

	public PublishContext createContext(final String cacheName) {
		String namespacedCacheName = composeNamespacedCacheName(cacheName);
//		System.out.println(String.format("BEGIN publishing to %s...", namespacedCacheName));
		return new PublishContext(namespacedCacheName, hostName, tags, gzip);
	}

	public void startPublish(PublishContext context) {
		for (IPublishHelper helper: helpers)
			helper.start(context);
	}

	String composeNamespacedCacheName(String rawCacheName) {
		Date now = new Date();
		String d1 = new SimpleDateFormat("yyyy/MMdd").format(now);
		String dt = new SimpleDateFormat("yyyyMMdd-HHmmss").format(now);
		String s = d1+"/"+dt+"_";
		if(hostName!=null) s += hostName + "_";
		s += rawCacheName + ".log";
		if(gzip) s += ".gz";
		return s;
	}

	public void publish(PublishContext context, String log) {
		for (IPublishHelper helper: helpers)
			helper.publish(context, log);
	}

	public void endPublish(PublishContext context) {
		for (IPublishHelper helper: helpers)
			helper.end(context);
		System.out.println(String.format("END publishing to %s", context.cacheName));
	}

	/**
	 * Add an IPublishHelper implementation to the list of helpers to invoke
	 * when publishing is performed.
	 *
	 * @param helper helper to add to the list
	 */
	public void addHelper(IPublishHelper helper) {
		helpers.add(helper);
	}
}

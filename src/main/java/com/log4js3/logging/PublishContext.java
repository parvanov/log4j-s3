package com.log4js3.logging;

/**
 * The context for a publish batch.  This object contains various auxiliary
 * information about the environment and configuration that publishers may
 * find useful.
 *
 * @author Van Ly (vancly@hotmail.com)
 *
 */
public class PublishContext {
	public final String cacheName;
	public final String hostName;
	public final String[] tags;
	public final boolean gzip;

	/**
	 * Creates an instance with the data provided
	 *
	 * @param cacheName name of the cache used to distinguish it from other
	 * 	caches
	 * @param hostName the host name where the logs are collected (typically
	 * 	the name of the local host)
	 * @param tags additional tags for the event that the logger was intialized
	 * 	with
	 * @param gzip
	 */
	public PublishContext(String cacheName, String hostName, String[] tags, boolean gzip) {
		this.cacheName = cacheName;
		this.hostName = hostName;
		this.tags = tags;
		this.gzip = gzip;
	}

}
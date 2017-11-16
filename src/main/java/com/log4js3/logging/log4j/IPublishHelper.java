package com.log4js3.logging.log4j;

import com.log4js3.logging.PublishContext;

/**
 * Interface for publish implementations to actually push log events to
 * external stores.
 *
 * @author Van Ly (vancly@hotmail.com)
 *
 */
public interface IPublishHelper {
	/**
	 * A publish batch is starting.  This is a good place to (re)initialize
	 * a buffer.
	 *
	 * @param context publish context providing useful properties for the
	 * publish operation
	 */
	void start(PublishContext context);

	/**
	 * A log event should be published.  Implementations may want to accumulate
	 * this in a batch until {{@link #end(PublishContext)}
	 *
	 * @param context publish context providing useful properties for the
	 * publish operation
	 * @param log the log to publish
	 */
	void publish(PublishContext context, String log);

	/**
	 * A publish batch has ended.  Implementations should conclude a batch
	 * and clean up resources here.
	 *
	 * @param context publish context providing useful properties for the
	 * publish operation
	 */
	void end(PublishContext context);
}

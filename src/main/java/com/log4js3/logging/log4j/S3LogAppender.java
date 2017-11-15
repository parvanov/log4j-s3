package com.log4js3.logging.log4j;

import java.util.UUID;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.OptionHandler;
import com.log4js3.logging.LoggingEventCache;
import com.log4js3.logging.aws.AwsClientBuilder;
import com.log4js3.logging.aws.S3Configuration;
import com.log4js3.logging.aws.S3PublishHelper;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;

/**
 * The log appender adapter that hooks into the Log4j framework to collect
 * logging events.
 * <br>
 * <h2>General</h2>
 * In addition to the typical log appender parameters, this appender also
 * supports (some are required) these parameters:
 * <br>
 * <ul>
 *   <li>stagingBufferSize -- the buffer size to collect log events before
 *   		publishing them in a batch (e.g. 20000).</li>
 *   <li>autoFlushInterval -- interval in seconds to flush over to the same file until it fills up, 0 for no auto-flushing</li>
 *   <li>tags -- comma delimited list of additional tags to associate with the
 *   		events (e.g. "MainSite;Production").</li>
 * </ul>
 * <br>
 * <h2>S3</h2>
 * These parameters configure the S3 publisher:
 * <br>
 * <ul>
 * 	 <li>s3AccessKey -- (optional) the access key component of the AWS
 *     credentials</li>
 *   <li>s3SecretKey -- (optional) the secret key component of the AWS
 *     credentials</li>
 *   <li>s3Path -- full path (bucket/key prefix) to use to compose the final key
 *     to use to store the log events batch</li>
 * </ul>
 * <em>NOTES</em>:
 * <ul>
 *   <li>If the access key and secret key are provided, they will be preferred
 *   	over whatever default setting (e.g. ~/.aws/credentials or
 * 		%USERPROFILE%\.aws\credentials) is in place for the
 * 		runtime environment.</li>
 *   <li>Tags are currently ignored by the S3 publisher.</li>
 * </ul>
 * <br>
 	 * <h3>Comment:</h3>
	 * Ideally S3LogAppender should extend or use some abstract remote logger.<br>
	 * Hadoop should do the same in its own class. So maven will have 3 jars - log4js3, log4jhadoop, log4jremote.<br>
	 * Where log4js3 and log4jhadoop depend on log4jremote.<br>
	 * <br>
*
 * @author Van Ly (vancly@hotmail.com)
 * @author Grigory Pomadchin (daunnc@gmail.com)
 * @author Plamen Parvanov
 */
public class S3LogAppender extends AppenderSkeleton implements Appender, OptionHandler {
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	static final int DEFAULT_THRESHOLD = 2000;
	static final int MONITOR_PERIOD = 30;

	private int stagingBufferSize = DEFAULT_THRESHOLD;
	private int autoFlushInterval;

	private LoggingEventCache stagingLog = null;

	private volatile String[] tags;
	private volatile String hostName;

	private S3Configuration s3;
	private AmazonS3Client s3Client;

	@Override
	public void close() {
		System.out.println("S3LogAppender.close(): Cleaning up resources");
		LoggingEventCache log = stagingLog;
		if (null != log) {
			stagingLog = null;
			log.close();
		}
	}

	@Override
	public boolean requiresLayout() {
		return true;
	}

	public void setStagingBufferSize(int buffer) {
		stagingBufferSize = buffer;
	}


	// S3 properties
	///////////////////////////////////////////////////////////////////////////
	public S3Configuration getS3() {
		if (null == s3) {
			s3 = new S3Configuration();
		}
		return s3;
	}

	public void setS3Path(String path) {
		getS3().setPath(path);
	}

	public void setS3AccessKey(String accessKey) {
		getS3().setAccessKey(accessKey);
	}

	public void setS3SecretKey(String secretKey) {
		getS3().setSecretKey(secretKey);
	}

	public void setS3Region(String region) {
		getS3().setRegion(region);
	}

	public void setTags(String tags) {
		if (null != tags) {
			this.tags = tags.split("[,;]");
			for (int i = 0; i < this.tags.length; i++) {
				this.tags[i] = this.tags[i].trim();
			}
		}
	}

	@Override
	protected void append(LoggingEvent e) {
		try {
			stagingLog.add(getLayout().format(e) + LINE_SEPARATOR);
		} catch (Exception ex) {
			errorHandler.error("Cannot append event", ex, 105, e);
		}
	}

	@Override
	public void activateOptions() {
		super.activateOptions();
		try {
			initFilters();
			java.net.InetAddress addr = java.net.InetAddress.getLocalHost();
			hostName = addr.getHostName();
			if (null != s3) {
				AwsClientBuilder builder =
					new AwsClientBuilder(Regions.valueOf(s3.getRegion()),
						s3.getAccessKey(), s3.getSecretKey());
				s3Client = builder.build(AmazonS3Client.class);
			}
			initStagingLog();
		} catch (Exception ex) {
			errorHandler.error("Cannot initialize resources", ex, 100);
		}
	}

	void initFilters() {
		addFilter(new Filter() {
			@Override
			public int decide(LoggingEvent event) {
				// To prevent infinite looping, we filter out events from
				// the publishing thread
				int decision = Filter.NEUTRAL;
				if (LoggingEventCache.PUBLISH_THREAD_NAME.equals(event.getThreadName())) {
					decision = Filter.DENY;
				}
				return decision;
		}});
	}

	void initStagingLog() throws Exception {
		if (null == stagingLog)
		try {
			CachePublisher publisher = new CachePublisher(hostName, tags);
			if (null != s3Client) {
				System.out.println("S3LogAppender path: "+s3.getPath());
				publisher.addHelper(new S3PublishHelper(s3Client, s3.getPath()));
			} else
				System.out.println("S3LogAppender - not configured ");
			String id = UUID.randomUUID().toString().replace("-","");
			stagingLog = new LoggingEventCache(id, stagingBufferSize, autoFlushInterval, publisher);

			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					close();
				}
			});
		} catch (Exception e) {
			System.out.println("Failed to initialize S3LogAppender: "+e);
			e.printStackTrace();
		}
	}

	public void setAutoFlushInterval(int autoFlushInterval) {
		this.autoFlushInterval = autoFlushInterval;
	}

}

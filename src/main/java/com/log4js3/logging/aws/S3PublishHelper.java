package com.log4js3.logging.aws;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.http.entity.ContentType;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.log4js3.logging.PublishContext;
import com.log4js3.logging.Util;
import com.log4js3.logging.log4j.IPublishHelper;

/**
 * Implementation to publish log events to S3.
 * <br>
 * These Log4j logger parameters configure the S3 publisher:
 * <br>
 * <em>NOTES</em>:
 * <ul>
 * <li>If the access key and secret key are provided, they will be preferred over
 * whatever default setting (e.g. ~/.aws/credentials or
 * %USERPROFILE%\.aws\credentials) is in place for the
 * runtime environment.</li>
 * <li>Tags are currently ignored by the S3 publisher.</li>
 * </ul>
 *
 * @author Van Ly (vancly@hotmail.com)
 * @author Plamen Parvanov
 *
 */
public class S3PublishHelper implements IPublishHelper {
	private static final String S3ERRCODE_BUCKETALREADYOWNEDBYYOU = "BucketAlreadyOwnedByYou";

	private final AmazonS3Client client;
	private final String bucket;
	private final String path;

	private volatile boolean bucketExists = false;
	private volatile StringBuilder stringBuilder = new StringBuilder();

	public S3PublishHelper(AmazonS3Client client, String path) {
		this.client = client;
		String[] pp = path.split("/", 2);
		this.bucket = pp[0].toLowerCase();
		path = pp[1];
		this.path = path.endsWith("/") ? path : path + "/";
	}

	public void publish(PublishContext context, String log) {
		stringBuilder.append(log);
	}

	public void start(PublishContext context) {
		// There are two ways to go about this: either I call something like
		// getBucketLocation()/listBuckets() and check to see if the bucket
		// is there.  If not, I need to create the bucket.  This requires 2
		// requests max to S3.
		// Or I go ahead and try to create the bucket.  If it's there already,
		// I get an exception.  If it's not, then (hopefully) it gets created.
		// In either case, we only incur 1 hit to S3.
		// A third option is to just assume the bucket is created a priori.

		// For now, I've chosen the 2nd option.
		if (!bucketExists) {
			try {
				//client.createBucket(bucket);
				bucketExists = true;
			} catch (AmazonS3Exception ex) {
				if (S3ERRCODE_BUCKETALREADYOWNEDBYYOU.equals(ex.getErrorCode())) {
					// If the exception is due to the bucket already existing,
					// then swallow it.  This is "normal."
					bucketExists = true;
				} else {
					throw ex;
				}
			}
		}
	}

	private String emptyBuffer() {
		StringBuilder sb = stringBuilder;
		stringBuilder = new StringBuilder();
		return sb.toString();
	}

	public void end(PublishContext context) {
		String key = String.format("%s%s", path, context.cacheName);
		System.out.println(String.format("Publishing to S3 (%s/%s):", bucket, key));

		String data = emptyBuffer();
		try {
			ObjectMetadata metadata = new ObjectMetadata();
			byte bytes[] = data.getBytes("UTF-8");
			if(context.gzip) {
				bytes = Util.gzip(bytes);
				metadata.setContentEncoding("gzip");
			}
			metadata.setContentLength(bytes.length);
			metadata.setContentType(ContentType.TEXT_PLAIN.getMimeType());
			client.putObject(bucket, key, new ByteArrayInputStream(bytes), metadata);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

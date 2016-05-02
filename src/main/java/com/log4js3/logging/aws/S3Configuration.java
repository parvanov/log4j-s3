package com.log4js3.logging.aws;

import com.amazonaws.regions.Regions;

/**
 * S3 connectivity/configuration
 *
 * @author Van Ly (vancly@hotmail.com)
 * @author Grigory Pomadchin (daunnc@gmail.com)
 *
 */
public class S3Configuration {
	public static final String DEFAULT_AWS_REGION = Regions.US_EAST_1.name();
	public static final String DEFAULT_LOG_BUCKETPATH = "logs/";
	
	private String accessKey = null;
	private String secretKey = null;
	private String region = DEFAULT_AWS_REGION;
	private String bucket = null;
	private String path = DEFAULT_LOG_BUCKETPATH;
	
	public String getAccessKey() {
		return accessKey;
	}
	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}
	public String getSecretKey() {
		return secretKey;
	}
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) { this.region = Regions.fromName(region).name(); }
	public String getBucket() {
		return bucket;
	}
	public void setBucket(String bucket) {
		this.bucket = bucket;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}

}

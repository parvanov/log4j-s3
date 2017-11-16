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

	private String accessKey = null;
	private String secretKey = null;
	private String region = DEFAULT_AWS_REGION;
	private String path;

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
	public void setRegion(String region) {
		this.region = Regions.fromName(region).name();
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}

}

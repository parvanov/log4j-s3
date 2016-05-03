package com.log4js3.logging.hadoop;

import org.apache.hadoop.conf.Configuration;

public class HadoopConfiguration {
    private Configuration config;
    private String path;

    public Configuration getConfiguration() {
        return config;
    }

    public String getPath() {
        return path;
    }

    public void setConfiguration(Configuration config) {
        this.config = config;
    }

    public void setPath(String path) {
        this.path = path;
    }
}

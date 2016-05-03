package com.log4js3.logging.hadoop;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.spi.LoggingEvent;
import com.log4js3.logging.PublishContext;
import com.log4js3.logging.log4j.IPublishHelper;

import static java.lang.Character.LINE_SEPARATOR;


/**
 * Publish helper to publish logs to HDFS
 *
 * @author Grigory Pomadchin (daunnc@gmail.com)
 *
 */
public class HadoopPublishHelper implements IPublishHelper {

    private HadoopConfiguration hadoopConfiguration;

    private volatile StringBuilder stringBuilder;

    public HadoopPublishHelper(HadoopConfiguration hadoopConfiguration) {
        this.hadoopConfiguration = hadoopConfiguration;
    }

    public void publish(PublishContext context, int sequence, LoggingEvent event) {
        stringBuilder.append(context.getLayout().format(event))
                .append(LINE_SEPARATOR);
    }

    public void start(PublishContext context) {
        stringBuilder = new StringBuilder();
        try {
            FileSystem fileSystem = FileSystem.get(hadoopConfiguration.getConfiguration());
            if(!fileSystem.exists(new Path(hadoopConfiguration.getPath()))) {
                fileSystem.create(new Path(hadoopConfiguration.getPath()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void end(PublishContext context) {
        String file = String.format("%s%s", hadoopConfiguration.getPath(), context.getCacheName());
        System.out.println(String.format("Publishing to Hadoop (file=%s):", file));

        String data = stringBuilder.toString();
        System.out.println(data);
        try {
            byte bytes[] = data.getBytes("UTF-8");
            try {
                FileSystem fs = FileSystem.get(hadoopConfiguration.getConfiguration());
                FSDataOutputStream os = fs.create(new Path(hadoopConfiguration.getPath() + file));
                os.write(bytes);
                os.close();
                fs.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        stringBuilder = null;
    }
}
package com.log4js3.logging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

public class Util {

	public static byte[] gzip(byte[] data) throws IOException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		GZIPOutputStream out = new GZIPOutputStream(buf);
		out.write(data);
		out.finish();
		return buf.toByteArray();
	}

}

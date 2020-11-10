package com.hawkins.utils;


public class Constants {
	
	private Constants() {
		
	}

	public static final String CANCELLED = "CANCELLED";
	public static final String RUNNING = "RUNNING";
	public static final String NEW = "NEW";
	public static final String PAUSED = "PAUSED";
	
	public static final String CONFIGPROPERTIES = "config.properties";
	public static final String DMPROPERTIES = "dm.properties";
	
	public static final String FILES_SEARCHED = "fs.counter.files.ok";
	public static final String DUPLICATE_BY_SIZE_COUNT_STRING = "duplicates.by-size.count";
	public static final String DUPLICATE_BY_MD5_COUNT_STRING = "duplicates.by-md5.count";
	public static final String DUPLICATE_BY_BYTE_COUNT_STRING = "duplicates.by-bytes.count";
	public static final String DIRECTORIES_SEARCHED_COUNT = "fs.counter.directories";
	
	public static final int DUPLICATE_BY_SIZE_COUNT = 0;
	public static final int DUPLICATE_BY_MD5_COUNT = 1;
	public static final int DUPLICATE_BY_BYTE_COUNT = 2;
	
}

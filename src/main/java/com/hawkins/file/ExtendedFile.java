package com.hawkins.file;

public class ExtendedFile {
	
	public ExtendedFile(String path, String name, String md5, String date, long size, boolean duplicate) {
		this.path = path;
		this.name = name;
		this.md5 = md5;
		this.date = date;
		this.size = size;
		this.duplicate = duplicate;
	}
	
	String path;
	String name;
	String md5;
	String date;
	long size;
	boolean duplicate;
	
	public ExtendedFile() {
		
	}
	
	
	public boolean isDuplicate() {
		return duplicate;
	}
	public void setDuplicate(boolean duplicate) {
		this.duplicate = duplicate;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
		public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMd5() {
		return md5;
	}
	public void setMd5(String md5) {
		this.md5 = md5;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	
	

}

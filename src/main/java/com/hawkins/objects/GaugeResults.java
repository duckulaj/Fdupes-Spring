package com.hawkins.objects;

public class GaugeResults {

	private int sizeCount = 0;
	private int md5Count = 0;
	private int sha3256Count = 0;
	private int byteCount = 0;
	private int directoriesSearched = 0;
	private int filesSearched = 0;
	
	public int getSizeCount() {
		return sizeCount;
	}
	public void setSizeCount(int sizeCount) {
		this.sizeCount = sizeCount;
	}
	public int getMd5Count() {
		return md5Count;
	}
	public void setMd5Count(int md5Count) {
		this.md5Count = md5Count;
	}
	public int getSha3256Count() {
		return sha3256Count;
	}
	public void setSha3256Count(int sha3256Count) {
		this.sha3256Count = sha3256Count;
	}
	public int getByteCount() {
		return byteCount;
	}
	public void setByteCount(int byteCount) {
		this.byteCount = byteCount;
	}
	public int getDirectoriesSearched() {
		return directoriesSearched;
	}
	public void setDirectoriesSearched(int directoriesSearched) {
		this.directoriesSearched = directoriesSearched;
	}
	public int getFilesSearched() {
		return filesSearched;
	}
	public void setFilesSearched(int filesSearched) {
		this.filesSearched = filesSearched;
	}
	
	
}

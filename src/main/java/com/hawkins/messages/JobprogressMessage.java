package com.hawkins.messages;

public class JobprogressMessage {
    private String jobName;
    private String directoryToSearch;
    private int directoryCount;
    private int fileCount;
    private int duplicatesBySizeCount;
    private int duplicatesByMD5Count;
    private int duplicatesBySHA3256Count;
    private int duplicatesByByteCount;
    private String duplicatesTotalSize;
    
    
    public JobprogressMessage(String jobName)
    {
        this.jobName = jobName;
    }

    public String getJobName() {
        return jobName;
    }

	public String getDirectoryToSearch() {
		return directoryToSearch;
	}

	public void setDirectoryToSearch(String directoryToSearch) {
		this.directoryToSearch = directoryToSearch;
	}

	public int getDirectoryCount() {
		return directoryCount;
	}

	public void setDirectoryCount(int directoryCount) {
		this.directoryCount = directoryCount;
	}

	public int getFileCount() {
		return fileCount;
	}

	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}

	public int getDuplicatesBySizeCount() {
		return duplicatesBySizeCount;
	}

	public void setDuplicatesBySizeCount(int duplicatesBySizeCount) {
		this.duplicatesBySizeCount = duplicatesBySizeCount;
	}

	public int getDuplicatesByMD5Count() {
		return duplicatesByMD5Count;
	}

	public void setDuplicatesByMD5Count(int duplicatesByMD5Count) {
		this.duplicatesByMD5Count = duplicatesByMD5Count;
	}

	public int getDuplicatesBySHA3256Count() {
		return duplicatesBySHA3256Count;
	}

	public void setDuplicatesBySHA3256Count(int duplicatesBySHA3256Count) {
		this.duplicatesBySHA3256Count = duplicatesBySHA3256Count;
	}

	public int getDuplicatesByByteCount() {
		return duplicatesByByteCount;
	}

	public void setDuplicatesByByteCount(int duplicatesByByteCount) {
		this.duplicatesByByteCount = duplicatesByByteCount;
	}

	public String getDuplicatesTotalSize() {
		return duplicatesTotalSize;
	}

	public void setDuplicatesTotalSize(String duplicatesTotalSize) {
		this.duplicatesTotalSize = duplicatesTotalSize;
	}
    
    
}

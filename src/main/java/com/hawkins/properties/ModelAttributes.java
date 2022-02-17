package com.hawkins.properties;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;

import com.hawkins.file.ExtendedFile;
import com.hawkins.paging.Paged;

public class ModelAttributes implements Runnable {

	private static final Logger logger = LogManager.getLogger(ModelAttributes.class.getName());
	
	private static ModelAttributes thisInstance = null;
	
	private String searchFolder;
	private List<ExtendedFile> uniqueFiles;
	private List<ExtendedFile> duplicateFiles;
	private Page<ExtendedFile> duplicateListPage;
	private Paged<ExtendedFile> duplicateListPageNew;
	private int byteCount;
	private String duplicateFileSize;
	private int duplicateCountBySize;
	private int duplicateCountByMd5;
	private int duplicateCountBySHA3256;
	private int duplicateCountByByte;
	private int directoriesSearched;
	private int filesSearched;
	private List<Integer> pageNumbers;
	
	public static synchronized ModelAttributes getInstance()
	{
		logger.debug("Requesting M3UPlayList instance");

		if (ModelAttributes.thisInstance == null)
		{
			ModelAttributes.thisInstance = new ModelAttributes();
		}

		return ModelAttributes.thisInstance;
	}
	
	
	
	public String getSearchFolder() {
		return searchFolder;
	}



	public void setSearchFolder(String searchFolder) {
		this.searchFolder = searchFolder;
	}



	public List<ExtendedFile> getUniqueFiles() {
		return uniqueFiles;
	}



	public void setUniqueFiles(List<ExtendedFile> uniqueFiles) {
		this.uniqueFiles = uniqueFiles;
	}



	public List<ExtendedFile> getDuplicateFiles() {
		return duplicateFiles;
	}



	public void setDuplicateFiles(List<ExtendedFile> duplicateFiles) {
		this.duplicateFiles = duplicateFiles;
	}



	public Page<ExtendedFile> getDuplicateListPage() {
		return duplicateListPage;
	}



	public void setDuplicateListPage(Page<ExtendedFile> duplicateListPage) {
		this.duplicateListPage = duplicateListPage;
	}



	public Paged<ExtendedFile> getDuplicateListPageNew() {
		return duplicateListPageNew;
	}



	public void setDuplicateListPageNew(Paged<ExtendedFile> duplicateListPageNew) {
		this.duplicateListPageNew = duplicateListPageNew;
	}



	public int getByteCount() {
		return byteCount;
	}



	public void setByteCount(int byteCount) {
		this.byteCount = byteCount;
	}



	public String getDuplicateFileSize() {
		return duplicateFileSize;
	}



	public void setDuplicateFileSize(String duplicateFileSize) {
		this.duplicateFileSize = duplicateFileSize;
	}



	public int getDuplicateCountBySize() {
		return duplicateCountBySize;
	}



	public void setDuplicateCountBySize(int duplicateCountBySize) {
		this.duplicateCountBySize = duplicateCountBySize;
	}



	public int getDuplicateCountByMd5() {
		return duplicateCountByMd5;
	}



	public void setDuplicateCountByMd5(int duplicateCountByMd5) {
		this.duplicateCountByMd5 = duplicateCountByMd5;
	}



	public int getDuplicateCountBySHA3256() {
		return duplicateCountBySHA3256;
	}



	public void setDuplicateCountBySHA3256(int duplicateCountBySHA3256) {
		this.duplicateCountBySHA3256 = duplicateCountBySHA3256;
	}



	public int getDuplicateCountByByte() {
		return duplicateCountByByte;
	}



	public void setDuplicateCountByByte(int duplicateCountByByte) {
		this.duplicateCountByByte = duplicateCountByByte;
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



	public List<Integer> getPageNumbers() {
		return pageNumbers;
	}



	public void setPageNumbers(List<Integer> pageNumbers) {
		this.pageNumbers = pageNumbers;
	}



	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}

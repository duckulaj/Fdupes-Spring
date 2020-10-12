package com.hawkins.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.cbismuth.fdupes.container.immutable.PathElement;
import com.google.common.collect.Multimap;
import com.hawkins.file.ExtendedFile;

public class Utils {

	private static final Logger logger = LogManager.getLogger(Utils.class.getName());

	public static List<ExtendedFile> getDuplicates (Multimap<PathElement, PathElement> duplicates) {

		List<ExtendedFile> duplicateFiles = new ArrayList<ExtendedFile>();

		for (Entry<PathElement, PathElement> entry: duplicates.entries()) {

			ExtendedFile thisFile = new ExtendedFile();
			thisFile.setDate(new Date(entry.getValue().lastModifiedTime()).toString());
			thisFile.setName(entry.getValue().getPath().toFile().getName());
			thisFile.setPath(entry.getValue().getPath().toString());
			thisFile.setSize(entry.getValue().size());
			duplicateFiles.add(thisFile);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("[] duplicates found", duplicateFiles.size());
		}

		return duplicateFiles;

	}

	public static List<ExtendedFile> getUniqueFiles (Set<PathElement> uniqueElements) {

		List<ExtendedFile> uniqueFiles = new ArrayList<ExtendedFile>();

		uniqueElements.forEach(file -> {
			ExtendedFile thisFile = new ExtendedFile();
			thisFile.setDate(new Date(file.lastModifiedTime()).toString());
			thisFile.setName(file.getPath().toFile().getName());
			thisFile.setPath(file.getPath().toString());
			thisFile.setSize(file.size());
			uniqueFiles.add(thisFile);
		});

		if (logger.isDebugEnabled()) {
			logger.debug("[] unique files found", uniqueFiles.size());
		}	

		return uniqueFiles;

	}

}

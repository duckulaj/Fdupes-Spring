package com.hawkins.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.cbismuth.fdupes.container.immutable.PathElement;
import com.google.common.collect.Multimap;
import com.hawkins.file.ExtendedFile;
import com.hawkins.properties.DuplicateProperties;

public class Utils {

	private static String propertyFile;

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

	public static boolean archiveFles(List<ExtendedFile> duplicates) {

		boolean success = false;
		List<String> filePaths = new ArrayList<String>();

		if (!duplicates.isEmpty()) {
			DuplicateProperties duplicateProperties = DuplicateProperties.getInstance();

			String zipFile = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss'.zip'").format(new Date());
			String archiveFolder = duplicateProperties.getArchiveFolder();

			File archive = new File(archiveFolder);
			if (!archive.exists()) {
				archive.mkdir();
			}

			duplicates.forEach(duplicate -> {
				filePaths.add(duplicate.getPath());
			});

			try {
				FileOutputStream fos = new FileOutputStream(archiveFolder + "/" + zipFile);
				ZipOutputStream zos = new ZipOutputStream(fos);

				for (String aFile : filePaths) {
					zos.putNextEntry(new ZipEntry(new File(aFile).getAbsolutePath()));

					byte[] bytes = Files.readAllBytes(Paths.get(aFile));
					zos.write(bytes, 0, bytes.length);
					zos.closeEntry();
				}

				zos.close();
				success = true;

			} catch (FileNotFoundException ex) {
				System.err.println("A file does not exist: " + ex);
			} catch (IOException ex) {
				System.err.println("I/O error: " + ex);
			}

		}

		return success;
	}

	public static void deleteDuplicates (List<ExtendedFile> duplicates) {

		duplicates.forEach(duplicate -> {

			File thisFile = new File(duplicate.getPath());
			thisFile.delete();

		});
	}

	public static Properties readProperties(String propertyType) {

		long start = System.currentTimeMillis();

		String userHome = System.getProperty("user.home");

		if(userHome.charAt(userHome.length()-1)!=File.separatorChar){
			userHome += File.separator;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Utils.readProperties :: Looking for {}Fdupes/{}", userHome, propertyType);
		}

		File configFile = new File(userHome, "Fdupes/" + propertyType);

		if (!configFile.exists() && logger.isDebugEnabled()) {
			logger.debug("{} does not exist", propertyType);
		}

		Properties props = new Properties();

		try {
			FileReader reader = new FileReader(configFile);
			props.load(reader);
			reader.close();
		} catch (FileNotFoundException fnfe) {
			logger.debug(fnfe.toString());
		} catch (IOException ioe) {
			logger.debug(ioe.toString());
		}

		long end = System.currentTimeMillis();

		if (logger.isDebugEnabled()) {
			logger.debug("readProperties executed in {} ms", (end - start));
		}
		return props;
	}

	public static Properties saveProperties(List<String> newProperties) {
		try (OutputStream output = new FileOutputStream(Constants.CONFIGPROPERTIES)) {

			Properties prop = new Properties();

			// set the properties value
			prop.setProperty("archiveFolder", newProperties.get(0));

			// save properties to project root folder
			prop.store(output, null);

			if (logger.isDebugEnabled()) {
				logger.debug(prop);
			}

		} catch (IOException io) {
			if (logger.isDebugEnabled()) {
				logger.debug(io.getMessage());
			}
		}

		return readProperties(propertyFile);
	}



}

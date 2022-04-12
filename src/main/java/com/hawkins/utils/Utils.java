package com.hawkins.utils;

import static com.github.cbismuth.fdupes.metrics.MetricRegistrySingleton.getMetricRegistry;

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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.github.cbismuth.fdupes.container.immutable.PathElement;
import com.google.common.collect.Multimap;
import com.hawkins.file.ExtendedFile;
import com.hawkins.objects.GaugeResults;
import com.hawkins.properties.DuplicateProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Utils {

	private static String propertyFile;

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

		if (log.isDebugEnabled()) {
			log.debug("[] duplicates found", duplicateFiles.size());
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

		if (log.isDebugEnabled()) {
			log.debug("[] unique files found", uniqueFiles.size());
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

		if (log.isDebugEnabled()) {
			log.debug("Utils.readProperties :: Looking for {}Fdupes/{}", userHome, propertyType);
		}

		File configFile = new File(userHome, "Fdupes/" + propertyType);

		if (!configFile.exists() && log.isDebugEnabled()) {
			log.debug("{} does not exist", propertyType);
		}

		Properties props = new Properties();

		try {
			FileReader reader = new FileReader(configFile);
			props.load(reader);
			reader.close();
		} catch (FileNotFoundException fnfe) {
			log.debug(fnfe.toString());
		} catch (IOException ioe) {
			log.debug(ioe.toString());
		}

		long end = System.currentTimeMillis();

		if (log.isDebugEnabled()) {
			log.debug("readProperties executed in {} ms", (end - start));
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

			if (log.isDebugEnabled()) {
				log.debug(prop.toString());
			}

		} catch (IOException io) {
			if (log.isDebugEnabled()) {
				log.debug(io.getMessage());
			}
		}

		return readProperties(propertyFile);
	}

	public static GaugeResults getGaugeResults() {

		GaugeResults results = new GaugeResults();
		SortedMap<String, Gauge> gauges = getMetricRegistry().getGauges();
		SortedMap<String, Counter> counters = getMetricRegistry().getCounters();

		if (gauges != null && gauges.size() > 0) {
			for (String key : gauges.keySet()) {
				if (key.equalsIgnoreCase(Constants.DUPLICATE_BY_SIZE_COUNT_STRING)) {
					results.setSizeCount(Integer.valueOf(gauges.get(key).getValue().toString()));
				}
				if (key.equalsIgnoreCase(Constants.DUPLICATE_BY_MD5_COUNT_STRING)) {
					results.setMd5Count(Integer.valueOf(gauges.get(key).getValue().toString()));
				}
				if (key.equalsIgnoreCase(Constants.DUPLICATE_BY_SHA3256_COUNT_STRING)) {
					results.setSha3256Count(Integer.valueOf(gauges.get(key).getValue().toString()));
				}
				if (key.equalsIgnoreCase(Constants.DUPLICATE_BY_BYTE_COUNT_STRING)) {
					results.setByteCount(Integer.valueOf(gauges.get(key).getValue().toString()));
				}
			}
		}

		if (counters != null && counters.size() > 0) {
			for (String key : counters.keySet()) {
				if (key.equalsIgnoreCase(Constants.DUPLICATE_BY_SIZE_COUNT_STRING)) {
					results.setSizeCount(Long.valueOf(counters.get(key).getCount()).intValue());
				}
				if (key.equalsIgnoreCase(Constants.FILES_SEARCHED)) {
					results.setFilesSearched(Long.valueOf(counters.get(key).getCount()).intValue());
				}
				if (key.equalsIgnoreCase(Constants.DIRECTORIES_SEARCHED_COUNT)) {
					results.setDirectoriesSearched(Long.valueOf(counters.get(key).getCount()).intValue());
				}
			}
		}
		
		return results;
	}
	
	public static void resetCounters() {

		// reset all counters

		for (Map.Entry<String, Counter> entry : getMetricRegistry().getCounters().entrySet()) {
			entry.getValue().dec(entry.getValue().getCount());
		}

	}

	
}

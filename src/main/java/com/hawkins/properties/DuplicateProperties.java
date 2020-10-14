package com.hawkins.properties;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hawkins.utils.Constants;
import com.hawkins.utils.Utils;

public class DuplicateProperties implements Runnable {

	private static final Logger logger = LogManager.getLogger(DuplicateProperties.class.getName());

	private static DuplicateProperties thisInstance = null;

	private String archiveFolder = null;
	
	public DuplicateProperties() {

		Properties props = Utils.readProperties(Constants.CONFIGPROPERTIES);

		this.setArchiveFolder(props.getProperty("archiveFolder"));
		
	}

	public static synchronized DuplicateProperties getInstance()
	{
		logger.debug("Requesting M3UPlayList instance");

		if (DuplicateProperties.thisInstance == null)
		{
			DuplicateProperties.thisInstance = new DuplicateProperties();
		}

		return DuplicateProperties.thisInstance;
	}

	public DuplicateProperties updateSettings(List<String> newProperties) {

		try {
			Path sourceFile = Paths.get("config.properties");
			Path targetFile = Paths.get("config.properties.bu");

			Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);

		} catch (IOException ioe) {
			if (logger.isDebugEnabled()) {
				logger.debug("I/O Error when copying file");
			}
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Exception copying file");
			}
		}

		Utils.saveProperties((ArrayList<String>) newProperties);
		return new DuplicateProperties();

	}

	@Override
	public void run() {
		throw new UnsupportedOperationException();
	}

	public String getArchiveFolder() {
		return archiveFolder;
	}

	public void setArchiveFolder(String archiveFolder) {
		this.archiveFolder = archiveFolder;
	}

}

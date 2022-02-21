package com.hawkins.utils;

import java.util.ArrayList;
import java.util.List;

import com.hawkins.jobs.DuplicateJob;

import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.common.AbstractOSFileStore;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

public class SystemUtils {
	
	private static SystemUtils thisInstance = null;
	private static HardwareAbstractionLayer hal = null;
	private static OperatingSystem os = null;
	private static List<String> networkDrives = null;
	
	public SystemUtils() {
		SystemInfo si = new SystemInfo();

	    this.hal = si.getHardware();
	    this.os = si.getOperatingSystem();
	    this.networkDrives = SystemUtils.getNetworkAttachedDrives();
	}
	    
    public static synchronized SystemUtils getInstance() {
    	
    	if (thisInstance== null) {
    		thisInstance = new SystemUtils();
    	}
    	return SystemUtils.thisInstance;
    }
    
    private static List<String> getNetworkAttachedDrives() {
    	
    	List<String> networkDrives = new ArrayList<String>();
    	List<OSFileStore> fileStore = os.getFileSystem().getFileStores();
    	
    	fileStore.forEach((item -> {
    		if (item.getType().equalsIgnoreCase("NFS4")) {
    			networkDrives.add(item.getMount());
    		}
    	}));
    	
    	return networkDrives;
    }

	public List<String> getNetworkDrives() {
		return networkDrives;
	}
    
    
	
}

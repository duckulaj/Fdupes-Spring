package com.hawkins.jobs;

import static com.google.common.collect.Multimaps.synchronizedListMultimap;
import static com.google.common.collect.Sets.newConcurrentHashSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.github.cbismuth.fdupes.container.immutable.PathElement;
import com.github.cbismuth.fdupes.io.BufferedAnalyzer;
import com.github.cbismuth.fdupes.io.DirectoryWalker;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.hawkins.messages.JobprogressMessage;
import com.hawkins.objects.GaugeResults;
import com.hawkins.utils.Utils;

public class DuplicateJob implements DetailedJob {
	
	private String jobName;
	private String searchFolder;
	private String duplicateFileSize;
	private SimpMessagingTemplate template;
	
	
	
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    private static DuplicateJob thisInstance = null;
    
	public DuplicateJob(String jobName, String searchFolder, SimpMessagingTemplate template) {
		this.jobName = jobName;
		this.searchFolder = searchFolder;
		this.template= template;
	}
	
	public static synchronized DuplicateJob getInstance(String jobName, String searchFolder, SimpMessagingTemplate template)
	{
		if (DuplicateJob.thisInstance == null)
		{
			DuplicateJob.thisInstance = new DuplicateJob(jobName,searchFolder, template);
		} else {
			
			if (!thisInstance.getJobName().equalsIgnoreCase(jobName)) {
				DuplicateJob.thisInstance = new DuplicateJob(jobName, searchFolder, template);
			}
		}

		return DuplicateJob.thisInstance;
	}

	public void sendProgress() {

		JobprogressMessage temp = new JobprogressMessage(this.jobName);
		
		GaugeResults gaugeResults = Utils.getGaugeResults();
		
		temp.getJobName();
		temp.setDirectoryToSearch(searchFolder);
		
		temp.setDuplicatesByByteCount(gaugeResults.getByteCount());
		temp.setDuplicatesBySizeCount(gaugeResults.getSizeCount());
		temp.setDuplicatesByMD5Count(gaugeResults.getMd5Count());
		temp.setDuplicatesBySHA3256Count(gaugeResults.getSha3256Count());
		temp.setDuplicatesByByteCount(gaugeResults.getByteCount());
		temp.setDirectoryCount(gaugeResults.getDirectoriesSearched());
		temp.setFileCount(gaugeResults.getFilesSearched());
		
		if (this.duplicateFileSize != null) 
			temp.setDuplicatesTotalSize(this.duplicateFileSize);
		else {
			this.duplicateFileSize = "";
		}

		template.convertAndSend("/topic/status", temp);
	}

	@Override
	public void run() {
		
		running.set(true);
		
		if (this.getJobName().isEmpty()) this.jobName = "finderJob";
				
		
		final Set<PathElement> uniqueElements = newConcurrentHashSet();
	    final Multimap<PathElement, PathElement> duplicates = synchronizedListMultimap(ArrayListMultimap.create());
	    
		// DuplicateJob job = new DuplicateJob(this.jobName, this.searchFolder, this.template);
		
		

        List<String> args = new ArrayList<String>();
        args.add(searchFolder);
		
		try {
			new DirectoryWalker().extractDuplicates(args, uniqueElements, duplicates, this);
			sendProgress();
			/*
			 * if (new SystemPropertyGetter(environment).doOrganize()) {
			 * pathOrganizer.organize(uniqueElements); }
			 */

            // List<ExtendedFile> duplicateFiles =  Utils.getDuplicates(duplicates);
            sendProgress();
            // this.duplicateList = duplicateFiles;
            
            // List<ExtendedFile> uniqueFiles =  Utils.getUniqueFiles(uniqueElements);
            sendProgress();
            
            this.duplicateFileSize = BufferedAnalyzer.returnDuplicationSize(duplicates);
            sendProgress();
            
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		reset();
		
	}

	@Override
	public int getProgress() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getState() {
		return running.toString();
	}

	@Override
	public String getJobName() {
		return this.jobName;
	}
	
	public AtomicBoolean running() {
		return this.running;
	}
	
	private void reset() {
		running.set(false);
		Utils.resetCounters();
		this.jobName = "";
		this.searchFolder = "";
	}

}

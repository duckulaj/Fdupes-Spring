package com.hawkins.jobs;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.github.cbismuth.fdupes.container.immutable.PathElement;
import com.github.cbismuth.fdupes.io.DirectoryWalker;
import com.google.common.collect.Multimap;
import com.hawkins.utils.Constants;


public class DuplicateFinderJob implements DetailedJob {

	private static final Logger LOGGER = LogManager.getLogger(DuplicateFinderJob.class.getName());
	
	private SimpMessagingTemplate template;

	private String state = Constants.NEW;
	private AtomicInteger progress = new AtomicInteger();
	private String jobName = "";
	private List<String> args;
	private Set<PathElement> uniqueElements;
	private Multimap<PathElement, PathElement> duplicates;
	
	private final AtomicBoolean running = new AtomicBoolean(false);

	public DuplicateFinderJob() {

	}

	public DuplicateFinderJob(List<String> args, String jobName, Set<PathElement> uniqueElements, Multimap<PathElement, PathElement> duplicates, SimpMessagingTemplate template) {
		this.jobName = jobName;
		this.template = template;
		this.args = args;
		this.uniqueElements = uniqueElements;
		this.duplicates = duplicates;
	}

	@Override
	public void run () {

		state = Constants.RUNNING;
		running.set(true);

		try {
			new DirectoryWalker().extractDuplicates(args, uniqueElements, duplicates);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
		
	}
	
	public void stop() {
		state =  Constants.CANCELLED;
		running.set(false);
		
	}
	
	public void pause() {
		state =  Constants.PAUSED;
		running.set(false);
	}

	@Override
	public int getProgress() {
		return progress.get();
	}

	public String getState() {
		return state;
	}

	public String getJobName() {
		return jobName;
	}
	
	public void setState(String state) {
		this.state = state;
	}
	
}

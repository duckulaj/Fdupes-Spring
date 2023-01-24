package com.hawkins.service;

import java.util.concurrent.Future;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DuplicateFinderService {

	private boolean stop = false;
	
	@Async
    public Future<Boolean> doWork(Runnable startDuplicateFinder) {
        
		if (log.isDebugEnabled()) {
			log.debug("Got runnable {}", startDuplicateFinder);
		}
	
		startDuplicateFinder.run();
        
        stop = true;
        return new AsyncResult<>(stop);
    }
	
	public boolean isStop() {
        return stop;
    }

    /**
     * @param stop the stop to set
     */
    public void setStop(boolean stop) {
        this.stop = stop;
    }
}

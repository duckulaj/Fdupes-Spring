package com.hawkins.controller;

import static com.google.common.collect.Multimaps.synchronizedListMultimap;
import static com.google.common.collect.Sets.newConcurrentHashSet;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.cbismuth.fdupes.cli.SystemPropertyGetter;
import com.github.cbismuth.fdupes.container.immutable.PathElement;
import com.github.cbismuth.fdupes.io.BufferedAnalyzer;
import com.github.cbismuth.fdupes.io.DirectoryWalker;
import com.github.cbismuth.fdupes.io.PathOrganizer;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.hawkins.file.ExtendedFile;
import com.hawkins.jobs.DuplicateJob;
import com.hawkins.objects.GaugeResults;
import com.hawkins.properties.DuplicateProperties;
import com.hawkins.service.DuplicateFinderService;
import com.hawkins.utils.Utils;

@Controller
public class DuplicateController {
	
	@Qualifier("taskExecutor")
	@Autowired
	private ThreadPoolTaskExecutor myExecutor;
	
	@Autowired
	private DuplicateFinderService myService;
	
	@Autowired
    private Environment environment;
	
	@Autowired
	private PathOrganizer pathOrganizer;
	
	@Autowired
	private SimpMessagingTemplate template;
	
	private static final Logger LOGGER = getLogger(DuplicateController.class);
	
	private List<ExtendedFile> duplicateList;
	
	@Autowired
	DuplicateController(DuplicateFinderService myService) {
		this.myService = myService;
	}
		
	@GetMapping("/")
	public String initial(Model model) {

		model.addAttribute("searchFolder", "/");
		return "search";

	}
	
	@GetMapping("/searchFolder")
	public String searchFolder(Model model, @RequestParam(value = "searchFolder", required = false) String searchFolder) {
			
		try {
			/*
			 * final Set<PathElement> uniqueElements = newConcurrentHashSet(); final
			 * Multimap<PathElement, PathElement> duplicates =
			 * synchronizedListMultimap(ArrayListMultimap.create());
			 * 
			 * List<String> args = new ArrayList<String>(); args.add(searchFolder);
			 */
            DuplicateJob duplicateJob = DuplicateJob.getInstance("finderJob", searchFolder, this.template);
            
            if (!duplicateJob.running().get()) myService.doWork(duplicateJob);
            
			/*
			 * new DirectoryWalker().extractDuplicates(args, uniqueElements, duplicates,
			 * template);
			 * 
			 * if (new SystemPropertyGetter(environment).doOrganize()) {
			 * pathOrganizer.organize(uniqueElements); }
			 * 
			 * List<ExtendedFile> duplicateFiles = Utils.getDuplicates(duplicates); //
			 * this.duplicateList = duplicateFiles;
			 * 
			 * List<ExtendedFile> uniqueFiles = Utils.getUniqueFiles(uniqueElements);
			 * 
			 * String duplicateFileSize =
			 * BufferedAnalyzer.returnDuplicationSize(duplicates);
			 */ 
			 GaugeResults gaugeResults = Utils.getGaugeResults();
			 
            
            model.addAttribute("searchFolder", searchFolder);
    		// model.addAttribute("foundFiles", uniqueFiles);
    		// model.addAttribute("duplicateFiles", duplicateFiles);
    		model.addAttribute("result", gaugeResults.getByteCount());
    		// model.addAttribute("duplicateFileSize", duplicateFileSize);
    		model.addAttribute("duplicateCountBySize", gaugeResults.getSizeCount());
    		model.addAttribute("duplicateCountByMd5", gaugeResults.getMd5Count());
    		model.addAttribute("duplicateCountByByte", gaugeResults.getByteCount());
    		model.addAttribute("directoriesSearched", gaugeResults.getDirectoriesSearched());
    		model.addAttribute("filesSearched", gaugeResults.getFilesSearched());
    		
    		// Utils.resetCounters();
    		
            // return "main";
    		return "status";
            
        } catch (final OutOfMemoryError ignored) {
            LOGGER.error("Not enough memory, solutions are:");
            LOGGER.error("\t- increase Java heap size (e.g. -Xmx512m),");
            LOGGER.error("\t- decrease byte buffer size (e.g. -Dfdupes.buffer.size=8k - default is 64k),");
            LOGGER.error("\t- reduce the level of parallelism (e.g. -Dfdupes.parallelism=1).");

            return "main";
        } catch (Exception e) {
			LOGGER.error(e.getMessage());
			return "main";
		}
	}
	
	@PostMapping("/archive")
	public String archive(Model model) {
		
		List<ExtendedFile> duplicates = this.duplicateList;
		
		if (duplicates != null) {
			 if (Utils.archiveFles(duplicates)) {
				 
				 /*
				  * Need to introduce a flag to say whether to delete files that have been archived
				  */
				 Utils.deleteDuplicates(this.duplicateList);
			 }
		}
			
		return "search";
	}
	
	@PostMapping("/updateSettings")
	public String updateSettings(Model model) {
		
		List<String> newProperties = new ArrayList<String>();
		
		DuplicateProperties dp = DuplicateProperties.getInstance();
		dp.updateSettings(newProperties);
		
		return "search";
		
	}
	
	

}	

package com.hawkins.controller;

import static com.google.common.collect.Multimaps.synchronizedListMultimap;
import static com.google.common.collect.Sets.newConcurrentHashSet;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.cbismuth.fdupes.cli.SystemPropertyGetter;
import com.github.cbismuth.fdupes.container.immutable.PathElement;
import com.github.cbismuth.fdupes.io.BufferedAnalyzer;
import com.github.cbismuth.fdupes.io.DirectoryWalker;
import com.github.cbismuth.fdupes.io.PathEscapeFunction;
import com.github.cbismuth.fdupes.io.PathOrganizer;
import com.github.cbismuth.fdupes.report.DuplicatesCsvReporter;
import com.github.cbismuth.fdupes.report.DuplicatesLogReporter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.hawkins.file.ExtendedFile;
import com.hawkins.utils.Utils;

@Controller
public class DuplicateController {
	
	@Autowired
    private Environment environment;
	
	@Autowired
	private PathEscapeFunction pathEscapeFunction;
	
	@Autowired
	private PathOrganizer pathOrganizer;
	
	private static final Logger LOGGER = getLogger(DuplicateController.class);
	
	@GetMapping("/")
	public String initial(Model model) {

		model.addAttribute("searchFolder", "/");
		return "search";

	}
	
	@GetMapping("/searchFolder")
	public String searchFolder(Model model, @RequestParam(value = "searchFolder", required = false) String searchFolder) {
			
		try {
            final Set<PathElement> uniqueElements = newConcurrentHashSet();
            final Multimap<PathElement, PathElement> duplicates = synchronizedListMultimap(ArrayListMultimap.create());

            List<String> args = new ArrayList<String>();
            args.add(searchFolder);
            
           
            
            // DirectoryWalker directoryWalker = new DirectoryWalker();
            
            new DirectoryWalker().extractDuplicates(args, uniqueElements, duplicates);

            // SystemPropertyGetter systemPropertyGetter= new SystemPropertyGetter(environment);
            if (new SystemPropertyGetter(environment).doOrganize()) {
                pathOrganizer.organize(uniqueElements);
            }

            final Path csvReport = new DuplicatesCsvReporter().report(duplicates);
            LOGGER.info("CSV report created at [{}]", csvReport);

            final Path logReport = new DuplicatesLogReporter(pathEscapeFunction).report(duplicates);
            LOGGER.info("Log report created at [{}]", logReport);
            
            List<ExtendedFile> duplicateFiles =  Utils.getDuplicates(duplicates);
            List<ExtendedFile> uniqueFiles =  Utils.getUniqueFiles(uniqueElements);
            String result = duplicateFiles.size() + " found";
            String duplicateFileSize = BufferedAnalyzer.returnDuplicationSize(duplicates);
            
            model.addAttribute("searchFolder", searchFolder);	
    		model.addAttribute("foundFiles", uniqueFiles);
    		model.addAttribute("duplicateFiles", duplicateFiles);
    		model.addAttribute("result", result );
    		model.addAttribute("duplicateFileSize", duplicateFileSize);
            
            return "main";
            
        } catch (final OutOfMemoryError ignored) {
            LOGGER.error("Not enough memory, solutions are:");
            LOGGER.error("\t- increase Java heap size (e.g. -Xmx512m),");
            LOGGER.error("\t- decrease byte buffer size (e.g. -Dfdupes.buffer.size=8k - default is 64k),");
            LOGGER.error("\t- reduce the level of parallelism (e.g. -Dfdupes.parallelism=1).");

            return "main";
        } catch (IOException e) {
			LOGGER.error(e.getMessage());
			return "main";
		}
	}
	

}	

package com.hawkins.controller;

import static com.google.common.collect.Multimaps.synchronizedListMultimap;
import static com.google.common.collect.Sets.newConcurrentHashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import com.hawkins.paging.Paged;
import com.hawkins.properties.DuplicateProperties;
import com.hawkins.properties.ModelAttributes;
import com.hawkins.service.DuplicateFinderService;
import com.hawkins.utils.Constants;
import com.hawkins.utils.PagingUtils;
import com.hawkins.utils.SystemUtils;
import com.hawkins.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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

	private List<ExtendedFile> duplicateList;
	private Page<ExtendedFile> duplicateListPage;
	private Paged<ExtendedFile> duplicateListPageNew;

	private boolean useMessaging = false;

	// @Autowired
	DuplicateController(DuplicateFinderService myService) {
		this.myService = myService;
	}

	@GetMapping("/")
	public String initial(Model model) {

		model.addAttribute("searchFolder", "/");
		return "search";

	}

	@GetMapping("/searchFolder")
	public String searchFolder(Model model, @RequestParam(value = "searchFolder", required = false) String searchFolder, @RequestParam("page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size ) {

		final int currentPage = page.orElse(1);
		final int pageSize = size.orElse(5);
		
		ModelAttributes modelAttributes = ModelAttributes.getInstance();
		SystemUtils.getInstance();

		String activeTemplate = Constants.TEMPLATE_MAIN;

		try {

			final Set<PathElement> uniqueElements = newConcurrentHashSet(); 
			final Multimap<PathElement, PathElement> duplicates = synchronizedListMultimap(ArrayListMultimap.create());

			List<String> args = new ArrayList<String>(); args.add(searchFolder);

			DuplicateJob duplicateJob = DuplicateJob.getInstance("Job" + searchFolder, searchFolder, this.template);


			if (useMessaging) {


				if (!duplicateJob.running().get()) {
					myService.doWork(duplicateJob);
				}
				activeTemplate = Constants.TEMPLATE_STATUS;


			} else {

				new DirectoryWalker().extractDuplicates(args, uniqueElements, duplicates,
						duplicateJob);

				if (new SystemPropertyGetter(environment).doOrganize()) {
					pathOrganizer.organize(uniqueElements); }

				List<ExtendedFile> duplicateFiles = Utils.getDuplicates(duplicates); //
				this.duplicateList = duplicateFiles;
				this.duplicateListPage = PagingUtils.findPaginated(PageRequest.of(currentPage - 1, pageSize), duplicateFiles);
				this.duplicateListPageNew = PagingUtils.getPage(currentPage, pageSize, duplicateFiles);

				int totalPages = duplicateListPage.getTotalPages();
				if (totalPages > 0) {
					List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
							.boxed()
							.collect(Collectors.toList());
					// model.addAttribute("pageNumbers", pageNumbers);
					modelAttributes.setPageNumbers(pageNumbers);
				}

				List<ExtendedFile> uniqueFiles = Utils.getUniqueFiles(uniqueElements);

				String duplicateFileSize =
						BufferedAnalyzer.returnDuplicationSize(duplicates);

				GaugeResults gaugeResults = Utils.getGaugeResults();


				modelAttributes.setSearchFolder(searchFolder);
				modelAttributes.setUniqueFiles(uniqueFiles);
				modelAttributes.setDuplicateFiles(duplicateFiles);
				modelAttributes.setDuplicateListPage(duplicateListPage);
				modelAttributes.setDuplicateListPageNew(duplicateListPageNew);
				modelAttributes.setByteCount(gaugeResults.getByteCount());
				modelAttributes.setDuplicateFileSize(duplicateFileSize);
				modelAttributes.setDuplicateCountBySize(gaugeResults.getSizeCount());
				modelAttributes.setDuplicateCountByMd5(gaugeResults.getMd5Count());
				modelAttributes.setDuplicateCountBySHA3256(gaugeResults.getSha3256Count());
				modelAttributes.setDuplicateCountByByte(gaugeResults.getByteCount());
				modelAttributes.setDirectoriesSearched(gaugeResults.getDirectoriesSearched());
				modelAttributes.setFilesSearched(gaugeResults.getFilesSearched());
				
				model = populateModel(model, modelAttributes);

				Utils.resetCounters();

				activeTemplate = Constants.TEMPLATE_MAIN;

			}



		} catch (final OutOfMemoryError ignored) {
			log.error("Not enough memory, solutions are:");
			log.error("\t- increase Java heap size (e.g. -Xmx512m),");
			log.error("\t- decrease byte buffer size (e.g. -Dfdupes.buffer.size=8k - default is 64k),");
			log.error("\t- reduce the level of parallelism (e.g. -Dfdupes.parallelism=1).");

			return Constants.TEMPLATE_MAIN;
		} catch (Exception e) {
			log.error(e.getMessage());
			return Constants.TEMPLATE_MAIN;
		}

		return activeTemplate;
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

	@GetMapping("/gotoPage")
	public String gotoPage(Model model, @RequestParam("page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size) {

		ModelAttributes modelAttributes = ModelAttributes.getInstance();
		
		final int currentPage = page.orElse(1);
		final int pageSize = size.orElse(5);

		this.duplicateListPage = PagingUtils.findPaginated(PageRequest.of(currentPage - 1, pageSize), this.duplicateList);
		this.duplicateListPageNew = PagingUtils.getPage(currentPage, pageSize, this.duplicateList);

		modelAttributes.setDuplicateListPageNew(duplicateListPageNew);
		modelAttributes.setDuplicateListPage(duplicateListPage);
		
		model = populateModel(model, modelAttributes);

		return Constants.TEMPLATE_MAIN;

	}

	private Model populateModel (Model model, ModelAttributes modelAttributes) {

		model.addAttribute("searchFolder", modelAttributes.getSearchFolder());
		model.addAttribute("foundFiles", modelAttributes.getUniqueFiles());
		model.addAttribute("duplicateFiles", modelAttributes.getDuplicateFiles());
		// model.addAttribute("duplicateListPage", modelAttributes.getDuplicateListPage());
		model.addAttribute("posts", modelAttributes.getDuplicateListPageNew());
		model.addAttribute("result", modelAttributes.getByteCount());
		model.addAttribute("duplicateFileSize", modelAttributes.getDuplicateFileSize());
		model.addAttribute("duplicateCountBySize", modelAttributes.getDuplicateCountBySize());
		model.addAttribute("duplicateCountByMd5", modelAttributes.getDuplicateCountByMd5());
		model.addAttribute("duplicateCountBySHA3256", modelAttributes.getDuplicateCountBySHA3256());
		model.addAttribute("duplicateCountByByte", modelAttributes.getDuplicateCountByByte());
		model.addAttribute("directoriesSearched", modelAttributes.getDirectoriesSearched());
		model.addAttribute("filesSearched", modelAttributes.getFilesSearched());

		return model;
	}

}	

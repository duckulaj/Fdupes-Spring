/*
 * The MIT License (MIT)
 * Copyright (c) 2016 Christophe Bismuth
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.cbismuth.fdupes.io;

import static com.codahale.metrics.MetricRegistry.name;
import static com.github.cbismuth.fdupes.metrics.MetricRegistrySingleton.getMetricRegistry;
import static com.google.common.collect.Sets.newConcurrentHashSet;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.codahale.metrics.Timer;
import com.github.cbismuth.fdupes.cli.SystemPropertyGetter;
import com.github.cbismuth.fdupes.collect.FilenamePredicate;
import com.github.cbismuth.fdupes.collect.PathComparator;
import com.github.cbismuth.fdupes.container.immutable.PathElement;
import com.github.cbismuth.fdupes.report.ErrorReporter;
import com.github.cbismuth.fdupes.stream.DuplicateFinderByKey;
import com.github.cbismuth.fdupes.stream.DuplicatesFinder;
import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.hawkins.jobs.DuplicateJob;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DirectoryWalker {

	@Autowired
	private Environment environment;

	private final DuplicatesFinder duplicatesFinder;
	private final FilenamePredicate filenamePredicate;
	private final PathEscapeFunction pathEscapeFunction;
	private DuplicateJob duplicateJob;
	

	public DirectoryWalker() {
		this.duplicatesFinder = new DuplicatesFinder(new Sha3256computer(), new DuplicateFinderByKey(), new PathComparator(), new SystemPropertyGetter(environment));
		this.filenamePredicate = new FilenamePredicate();
		this.pathEscapeFunction = new PathEscapeFunction();
	}

	public void extractDuplicates(final Iterable<String> inputPaths,
			final Set<PathElement> uniqueElements,
			final Multimap<PathElement, PathElement> duplicates,
			DuplicateJob job) throws IOException {


		this.duplicateJob = job;

		Preconditions.checkNotNull(inputPaths, "null input path collection");

		job.sendProgress();

		final Collection<PathElement> readablePaths = newConcurrentHashSet();
		final Collection<Path> unreadablePaths = newConcurrentHashSet();

		readablePaths.clear();
		
		inputPaths.forEach(rootPath -> {
			final Path path = Paths.get(rootPath);


			if (filenamePredicate.accept(path)) {
				if (Files.isDirectory(path)) {
					handleDirectory(path, readablePaths, unreadablePaths);
				} else if (Files.isRegularFile(path)) {
					handleRegularFile(path, readablePaths, unreadablePaths);
				} else {
					log.warn("[{}] is not a directory or a regular file", rootPath);
				}
			}



		});

		new ErrorReporter(pathEscapeFunction).report(unreadablePaths);

		duplicatesFinder.extractDuplicates(readablePaths, uniqueElements, duplicates);
	}

	private void handleDirectory(final Path path,
			final Collection<PathElement> paths,
			final Collection<Path> pathsInError) {
		try (final DirectoryStream<Path> stream = Files.newDirectoryStream(path, filenamePredicate)) {
			stream.forEach(p -> {
				if (Files.isDirectory(p)) {
					
					
					getMetricRegistry().counter(name("fs", "counter", "directories")).inc();

					handleDirectory(p, paths, pathsInError);


				} else {
					handleRegularFile(p, paths, pathsInError);
				}
			});
		} catch (final IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	private void handleRegularFile(final Path path,
			final Collection<PathElement> paths,
			final Collection<Path> pathsInError) {
		try {
			try (final Timer.Context ignored = getMetricRegistry().timer(name("fs", "timer", "files", "attributes", "read")).time()) {
				paths.add(new PathElement(path, Files.readAttributes(path, BasicFileAttributes.class)));
			}

			getMetricRegistry().counter(name("fs", "counter", "files", "ok")).inc();
			this.duplicateJob.sendProgress();
		} catch (final IOException ignored) {
			pathsInError.add(path);

			getMetricRegistry().counter(name("fs", "counter", "files", "ko")).inc();
			this.duplicateJob.sendProgress();
		}
	}

}

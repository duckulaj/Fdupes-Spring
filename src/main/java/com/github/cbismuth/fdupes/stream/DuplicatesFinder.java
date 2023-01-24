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

package com.github.cbismuth.fdupes.stream;

import static com.codahale.metrics.MetricRegistry.name;
import static com.github.cbismuth.fdupes.metrics.MetricRegistrySingleton.getMetricRegistry;

import java.util.Collection;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.codahale.metrics.Gauge;
import com.github.cbismuth.fdupes.cli.SystemPropertyGetter;
import com.github.cbismuth.fdupes.collect.PathComparator;
import com.github.cbismuth.fdupes.container.immutable.PathElement;
import com.github.cbismuth.fdupes.io.BufferedAnalyzer;
import com.github.cbismuth.fdupes.io.Sha3256computer;
import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DuplicatesFinder {

	
	private final Sha3256computer sha3256Computer;
	private final DuplicateFinderByKey duplicateFinderByKey;
	private final PathComparator pathComparator;
	private final SystemPropertyGetter systemPropertyGetter;

	public DuplicatesFinder(final Sha3256computer sha3256computer,
			final DuplicateFinderByKey duplicateFinderByKey,
			final PathComparator pathComparator,
			final SystemPropertyGetter systemPropertyGetter) {
		this.duplicateFinderByKey = duplicateFinderByKey;
		
		this.sha3256Computer = sha3256computer;
		this.pathComparator = pathComparator;
		this.systemPropertyGetter = systemPropertyGetter;
	}

	public void extractDuplicates(final Collection<PathElement> input,
			final Set<PathElement> uniqueElements,
			final Multimap<PathElement, PathElement> duplicates) {



		Preconditions.checkNotNull(input, "null file metadata collection");

		getMetricRegistry().remove(name("duplicates", "by-size", "count"));
		log.info("Pass 1/3 - compare file by size ...");
		final Collection<PathElement> duplicatesBySize = duplicateFinderByKey.getDuplicates(input, PathElement::size, uniqueElements);
		getMetricRegistry().register(name("duplicates", "by-size", "count"), (Gauge<Integer>) duplicatesBySize::size);
		log.info("Pass 1/3 - compare file by size completed! - {} duplicate(s) found", duplicatesBySize.size());


		/*
		 * getMetricRegistry().remove(name("duplicates", "by-md5", "count"));
		 * log.info("Pass 2/3 - compare file by MD5 ..."); final Collection<PathElement>
		 * duplicatesByMd5 = duplicateFinderByKey.getDuplicates(duplicatesBySize,
		 * md5Computer::compute, uniqueElements);
		 * getMetricRegistry().register(name("duplicates", "by-md5", "count"),
		 * (Gauge<Integer>) duplicatesByMd5::size); log.
		 * info("Pass 2/3 - compare file by MD5 completed! - {} duplicate(s) found",
		 * duplicatesByMd5.size());
		 */

		getMetricRegistry().remove(name("duplicates", "by-sha3-256", "count"));
		log.info("Pass 2/3 - compare file by SHA3-256 ..."); final
		Collection<PathElement> duplicatesBySha3256 = duplicateFinderByKey.getDuplicates(duplicatesBySize, sha3256Computer::compute, uniqueElements);
		getMetricRegistry().register(name("duplicates", "by-sha3-256", "count"), (Gauge<Integer>) duplicatesBySha3256::size); log.info("Pass 2/3 - compare file by SHA3-256 completed! - {} duplicate(s) found", duplicatesBySha3256.size());

		getMetricRegistry().remove(name("duplicates", "by-bytes", "count"));
		log.info("Pass 3/3 - compare file byte-by-byte ...");
		final BufferedAnalyzer analyzer = new BufferedAnalyzer(pathComparator, systemPropertyGetter);
		// analyzer.analyze(duplicatesByMd5, uniqueElements, duplicates);
		analyzer.analyze(duplicatesBySha3256, uniqueElements, duplicates);
		getMetricRegistry().register(name("duplicates", "by-bytes", "count"), (Gauge<Integer>) duplicates::size);
		log.info("Pass 3/3 - compare file byte-by-byte completed! - {} duplicate(s) found", duplicates.size());
	}

}

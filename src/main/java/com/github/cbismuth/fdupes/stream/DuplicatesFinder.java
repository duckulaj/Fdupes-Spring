
package com.github.cbismuth.fdupes.stream;

import static com.codahale.metrics.MetricRegistry.name;
import static com.github.cbismuth.fdupes.metrics.MetricRegistrySingleton.getMetricRegistry;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

        Preconditions.checkNotNull(input, "Input collection cannot be null");

        log.info("Pass 1/3 - Compare files by size...");
        final Collection<PathElement> duplicatesBySize = processDuplicates(input, PathElement::size, uniqueElements, "by-size");

        log.info("Pass 2/3 - Compare files by SHA3-256...");
        final Collection<PathElement> duplicatesBySha3256 = processDuplicates(duplicatesBySize, sha3256Computer::compute, uniqueElements, "by-sha3-256");

        log.info("Pass 3/3 - Compare files byte-by-byte...");
        if (!duplicatesBySha3256.isEmpty()) {
            final BufferedAnalyzer analyzer = new BufferedAnalyzer(pathComparator, systemPropertyGetter);
            analyzer.analyze(duplicatesBySha3256, uniqueElements, duplicates);
            getMetricRegistry().register(name("duplicates", "by-bytes", "count"), (Gauge<Integer>) duplicates::size);
            log.info("Pass 3/3 - Compare files byte-by-byte completed! - {} duplicate(s) found", duplicates.size());
        }
    }

    private <K> Collection<PathElement> processDuplicates(final Collection<PathElement> input,
                                                           final Function<PathElement, K> keyMapper,
                                                           final Collection<PathElement> uniqueElements,
                                                           final String metricSuffix) {
        getMetricRegistry().remove(name("duplicates", metricSuffix, "count"));

        Map<K, List<PathElement>> groupedByKey = input.stream()
                .collect(Collectors.groupingBy(keyMapper, Collectors.toList()));

        // Partition into unique and duplicate groups in one pass
        Map<Boolean, List<PathElement>> partitioned = groupedByKey.values().stream()
                .collect(Collectors.partitioningBy(group -> group.size() == 1, 
                        Collectors.flatMapping(Collection::stream, Collectors.toList())));

        uniqueElements.addAll(partitioned.get(true));
        Collection<PathElement> duplicates = partitioned.get(false);

        getMetricRegistry().register(name("duplicates", metricSuffix, "count"), (Gauge<Integer>) duplicates::size);
        log.info("Pass - Compare files {} completed! - {} duplicate(s) found", metricSuffix, duplicates.size());

        return duplicates;
    }
}

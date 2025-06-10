
package com.github.cbismuth.fdupes.stream;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.github.cbismuth.fdupes.container.immutable.PathElement;
import com.google.common.base.Preconditions;

@Component
public class DuplicateFinderByKey {

    public <K> Collection<PathElement> getDuplicates(
            final Collection<PathElement> input,
            final Function<PathElement, K> keyMapper,
            final Collection<PathElement> uniqueElements) {

        Preconditions.checkNotNull(input, "Input collection cannot be null");
        Preconditions.checkNotNull(keyMapper, "Key mapper function cannot be null");

        // Group elements by the key
        Map<K, List<PathElement>> groupedByKey = input.stream()
                .collect(Collectors.groupingBy(keyMapper, Collectors.toList()));

        // Collect unique elements (groups with size 1)
        uniqueElements.addAll(
                groupedByKey.values().stream()
                        .filter(group -> group.size() == 1)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList())
        );

        // Return duplicates (groups with size > 1)
        return groupedByKey.values().stream()
                .filter(group -> group.size() > 1)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}

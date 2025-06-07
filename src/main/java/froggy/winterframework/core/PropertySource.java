package froggy.winterframework.core;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 프로퍼티를 저장하는 클래스.
 */
public class PropertySource {

    private final String name;
    private final Map<String, String> source;

    public PropertySource(String name, Map<String, String> source) {
        this.name = name;
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getSource() {
        return source;
    }

    /**
     *  PropertySource를 병합하며, 중복 키 충돌 시 예외를 던진다.
     */
    public PropertySource mergePropertySource(PropertySource addSource) {
        if (addSource == null || addSource.getSource() == null) {
            throw new IllegalArgumentException("PropertySource to merge must not be null.");
        }

        validateNoDuplicateKeys(addSource);
        source.putAll(addSource.getSource());

        return this;
    }

    private void validateNoDuplicateKeys(PropertySource addSource) {
        Set<String> overlap = new HashSet<>(source.keySet());
        overlap.retainAll(addSource.getSource().keySet());

        if (!overlap.isEmpty()) {
            throw new IllegalArgumentException("Cannot merge PropertySource: duplicate keys found → " + overlap);
        }
    }
}

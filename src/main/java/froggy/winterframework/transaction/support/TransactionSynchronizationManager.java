package froggy.winterframework.transaction.support;

import java.util.HashMap;
import java.util.Map;

public final class TransactionSynchronizationManager {

    private static final String NULL_KEY_MESSAGE = "Resource key must not be null";
    private static final String NULL_RESOURCE_MESSAGE = "Resource must not be null";
    private static final String RESOURCE_ALREADY_REGISTERED_MESSAGE = "Resource already registered for key";
    private static final String NO_RESOURCE_REGISTERED_MESSAGE = "No resource registered for key";

    private static final ThreadLocal<Map<Object, Object>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static void bindResource(Object key, Object resource) {
        if (key == null) {
            throw new IllegalArgumentException(NULL_KEY_MESSAGE);
        }
        if (resource == null) {
            throw new IllegalArgumentException(NULL_RESOURCE_MESSAGE);
        }

        Map<Object, Object> resourceMap = resources.get();
        if (resourceMap == null) {
            resourceMap = new HashMap<>();
            resources.set(resourceMap);
        }

        if (resourceMap.containsKey(key)) {
            throw new IllegalStateException(RESOURCE_ALREADY_REGISTERED_MESSAGE + ": " + key);
        }

        resourceMap.put(key, resource);
    }

    public static Object getResource(Object key) {
        if (key == null) {
            throw new IllegalArgumentException(NULL_KEY_MESSAGE);
        }

        Map<Object, Object> resourceMap = resources.get();
        if (resourceMap == null) {
            return null;
        }

        return resourceMap.get(key);
    }

    public static boolean hasResource(Object key) {
        return getResource(key) != null;
    }

    public static Object unbindResource(Object key) {
        if (key == null) {
            throw new IllegalArgumentException(NULL_KEY_MESSAGE);
        }

        Map<Object, Object> resourceMap = resources.get();
        if (resourceMap == null || !resourceMap.containsKey(key)) {
            throw new IllegalStateException(NO_RESOURCE_REGISTERED_MESSAGE + ": " + key);
        }

        Object resource = resourceMap.remove(key);
        if (resourceMap.isEmpty()) {
            resources.remove();
        }
        return resource;
    }

    public static void clear() {
        resources.remove();
    }
}

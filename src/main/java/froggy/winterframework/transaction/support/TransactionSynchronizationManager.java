package froggy.winterframework.transaction.support;

import java.util.HashMap;
import java.util.Map;

/**
 * 같은 Thread 안에서 transaction resource binding을 관리하는 ThreadLocal 기반 저장소.
 *
 * key로 resource를 등록하면 같은 Thread 안에서 조회할 수 있다.
 * resource lifecycle은 이 클래스가 직접 관리하지 않는다.
 */
public final class TransactionSynchronizationManager {

    private static final String NULL_KEY_MESSAGE = "Resource key must not be null";
    private static final String NULL_RESOURCE_MESSAGE = "Resource must not be null";
    private static final String RESOURCE_ALREADY_REGISTERED_MESSAGE = "Resource already registered for key";
    private static final String NO_RESOURCE_REGISTERED_MESSAGE = "No resource registered for key";

    private static final ThreadLocal<Map<Object, Object>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
        // Static-only utility class
    }

    /**
     * 현재 Thread에 key-resource binding을 등록한다.
     * 같은 key가 이미 등록되어 있다면 실패한다.
     *
     * @param key resource를 식별하는 key
     * @param resource 현재 Thread에 등록할 resource
     */
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

    /**
     * 현재 Thread에 등록된 resource를 key로 조회한다.
     * 등록된 resource가 없으면 null을 반환한다.
     *
     * @param key 조회할 resource를 식별하는 key
     * @return 현재 Thread에 등록된 resource, 없으면 null
     */
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

    /**
     * 현재 Thread에 해당 key의 resource가 등록되어 있는지 확인한다.
     *
     * @param key 등록 여부를 확인할 resource를 식별하는 key
     * @return 해당 key의 resource가 등록되어 있으면 true, 없으면 false
     */
    public static boolean hasResource(Object key) {
        return getResource(key) != null;
    }

    /**
     * 현재 Thread의 key-resource binding을 해제하고 등록되어 있던 resource를 반환한다.
     * 등록된 resource가 없으면 실패한다.
     *
     * @param key 해제할 resource를 식별하는 key
     * @return 현재 Thread에서 해제한 resource
     */
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

    /**
     * 현재 Thread에 등록된 모든 resource binding을 제거한다.
     */
    public static void clear() {
        resources.remove();
    }
}

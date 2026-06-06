package froggy.winterframework.transaction.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;

public class TransactionSynchronizationManagerTest {

    @After
    public void tearDown() {
        TransactionSynchronizationManager.clear();
    }

    @Test
    public void getResource는_등록된_resource가_없으면_null을_반환한다() {
        // Given
        Object key = new Object();

        // When
        Object actualResource = TransactionSynchronizationManager.getResource(key);

        // Then
        assertNull(actualResource);
    }

    @Test
    public void bindResource는_key와_resource를_등록하고_조회할_수_있다() {
        // Given
        Object key = new Object();
        Object resource = new Object();

        // When
        TransactionSynchronizationManager.bindResource(key, resource);
        Object actualResource = TransactionSynchronizationManager.getResource(key);

        // Then
        assertSame(resource, actualResource);
    }

    @Test
    public void hasResource는_resource가_등록되어_있으면_true를_반환한다() {
        // Given
        Object key = new Object();
        Object resource = new Object();
        TransactionSynchronizationManager.bindResource(key, resource);

        // When
        boolean actual = TransactionSynchronizationManager.hasResource(key);

        // Then
        assertTrue(actual);
    }

    @Test
    public void hasResource는_resource가_등록되어_있지_않으면_false를_반환한다() {
        // Given
        Object key = new Object();

        // When
        boolean actual = TransactionSynchronizationManager.hasResource(key);

        // Then
        assertFalse(actual);
    }

    @Test
    public void bindResource는_null_key를_허용하지_않는다() {
        // Given
        Object key = null;
        Object resource = new Object();

        // When
        IllegalArgumentException actualException = assertThrows(
            IllegalArgumentException.class,
            () -> TransactionSynchronizationManager.bindResource(key, resource)
        );

        // Then
        assertEquals("Resource key must not be null", actualException.getMessage());
    }

    @Test
    public void bindResource는_null_resource를_허용하지_않는다() {
        // Given
        Object key = new Object();
        Object resource = null;

        // When
        IllegalArgumentException actualException = assertThrows(
            IllegalArgumentException.class,
            () -> TransactionSynchronizationManager.bindResource(key, resource)
        );

        // Then
        assertEquals("Resource must not be null", actualException.getMessage());
    }

    @Test
    public void bindResource는_같은_key에_중복_등록을_허용하지_않는다() {
        // Given
        Object key = new Object();
        Object resource = new Object();
        Object duplicateResource = new Object();
        TransactionSynchronizationManager.bindResource(key, resource);

        // When
        IllegalStateException actualException = assertThrows(
            IllegalStateException.class,
            () -> TransactionSynchronizationManager.bindResource(key, duplicateResource)
        );

        // Then
        assertEquals("Resource already registered for key: " + key, actualException.getMessage());
        assertSame(resource, TransactionSynchronizationManager.getResource(key));
    }

    @Test
    public void unbindResource는_등록된_resource를_반환하고_제거한다() {
        // Given
        Object key = new Object();
        Object resource = new Object();
        TransactionSynchronizationManager.bindResource(key, resource);

        // When
        Object actualResource = TransactionSynchronizationManager.unbindResource(key);

        // Then
        assertSame(resource, actualResource);
        assertNull(TransactionSynchronizationManager.getResource(key));
        assertFalse(TransactionSynchronizationManager.hasResource(key));
    }

    @Test
    public void unbindResource는_지정한_key의_resource만_제거한다() {
        // Given
        Object firstKey = new Object();
        Object secondKey = new Object();
        Object firstResource = new Object();
        Object secondResource = new Object();
        TransactionSynchronizationManager.bindResource(firstKey, firstResource);
        TransactionSynchronizationManager.bindResource(secondKey, secondResource);

        // When
        Object actualResource = TransactionSynchronizationManager.unbindResource(firstKey);

        // Then
        assertSame(firstResource, actualResource);
        assertNull(TransactionSynchronizationManager.getResource(firstKey));
        assertSame(secondResource, TransactionSynchronizationManager.getResource(secondKey));
    }

    @Test
    public void unbindResource는_없는_key를_허용하지_않는다() {
        // Given
        Object key = new Object();

        // When
        IllegalStateException actualException = assertThrows(
            IllegalStateException.class,
            () -> TransactionSynchronizationManager.unbindResource(key)
        );

        // Then
        assertEquals("No resource registered for key: " + key, actualException.getMessage());
    }

    @Test
    public void clear는_현재_thread의_모든_resource를_제거한다() {
        // Given
        Object firstKey = new Object();
        Object secondKey = new Object();
        Object firstResource = new Object();
        Object secondResource = new Object();
        TransactionSynchronizationManager.bindResource(firstKey, firstResource);
        TransactionSynchronizationManager.bindResource(secondKey, secondResource);

        // When
        TransactionSynchronizationManager.clear();

        // Then
        assertNull(TransactionSynchronizationManager.getResource(firstKey));
        assertNull(TransactionSynchronizationManager.getResource(secondKey));
        assertFalse(TransactionSynchronizationManager.hasResource(firstKey));
        assertFalse(TransactionSynchronizationManager.hasResource(secondKey));
    }
}

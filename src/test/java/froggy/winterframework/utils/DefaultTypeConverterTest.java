package froggy.winterframework.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DefaultTypeConverterTest {

    @Test
    public void supportsStringAndPrimitiveAndWrapperTypes() {
        // Given
        DefaultTypeConverter converter = new DefaultTypeConverter();
        Class<?>[] types = {
            String.class, byte.class, Byte.class, short.class, Short.class,
            int.class, Integer.class, long.class, Long.class,
            float.class, Float.class, double.class, Double.class,
            boolean.class, Boolean.class, char.class, Character.class
        };

        // When & Then
        for (Class<?> type : types) {
            assertTrue(type.getName(), converter.isSupport(type));
        }
    }

    @Test
    public void convertsRepresentativeValues() {
        // Given
        DefaultTypeConverter converter = new DefaultTypeConverter();

        // When & Then
        assertEquals("hello", converter.convert(String.class, "hello"));
        assertEquals(Byte.valueOf((byte) 42), converter.convert(byte.class, "42"));
        assertEquals(Byte.valueOf((byte) 42), converter.convert(Byte.class, "42"));
        assertEquals(Short.valueOf((short) 42), converter.convert(short.class, "42"));
        assertEquals(Short.valueOf((short) 42), converter.convert(Short.class, "42"));
        assertEquals(Integer.valueOf(42), converter.convert(int.class, "42"));
        assertEquals(Integer.valueOf(42), converter.convert(Integer.class, "42"));
        assertEquals(Long.valueOf(42L), converter.convert(long.class, "42"));
        assertEquals(Long.valueOf(42L), converter.convert(Long.class, "42"));
        assertEquals(Float.valueOf(1.25f), converter.convert(float.class, "1.25"));
        assertEquals(Float.valueOf(1.25f), converter.convert(Float.class, "1.25"));
        assertEquals(Double.valueOf(1.25d), converter.convert(double.class, "1.25"));
        assertEquals(Double.valueOf(1.25d), converter.convert(Double.class, "1.25"));
        assertEquals(Boolean.TRUE, converter.convert(boolean.class, "true"));
        assertEquals(Boolean.TRUE, converter.convert(Boolean.class, "true"));
        assertEquals(Boolean.FALSE, converter.convert(boolean.class, "false"));
        assertEquals(Boolean.FALSE, converter.convert(Boolean.class, "false"));
        assertEquals(Character.valueOf('A'), converter.convert(char.class, "A"));
        assertEquals(Character.valueOf('A'), converter.convert(Character.class, "A"));
    }

    @Test
    public void rejectsInvalidNumber() {
        // Given
        DefaultTypeConverter converter = new DefaultTypeConverter();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> converter.convert(int.class, "abc"));
    }

    @Test
    public void rejectsInvalidBooleanAndCharacter() {
        // Given
        DefaultTypeConverter converter = new DefaultTypeConverter();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> converter.convert(boolean.class, "yes"));
        assertThrows(IllegalArgumentException.class, () -> converter.convert(char.class, ""));
        assertThrows(IllegalArgumentException.class, () -> converter.convert(char.class, "AB"));
    }

    @Test
    public void rejectsUnsupportedType() {
        // Given
        DefaultTypeConverter converter = new DefaultTypeConverter();

        // When & Then
        assertFalse(converter.isSupport(Object.class));
        assertThrows(IllegalArgumentException.class, () -> converter.convert(Object.class, "value"));
    }
}

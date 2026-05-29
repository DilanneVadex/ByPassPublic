package com.dilanne.bypass.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class HashUtilsTest {

    @Test
    public void testSha1Generation() {
        String input = "password123";
        // Known SHA-1 for "password123"
        String expected = "CBFDAC6008F9CAB4083784CBD1874F76618D2A97";
        String actual = HashUtils.getSha1(input);
        assertEquals(expected, actual);
    }

    @Test
    public void testSha1Consistency() {
        String input = "test_string";
        String first = HashUtils.getSha1(input);
        String second = HashUtils.getSha1(input);
        assertEquals(first, second);
    }

    @Test
    public void testEmptyString() {
        String actual = HashUtils.getSha1("");
        assertNotNull(actual);
        assertFalse(actual.isEmpty());
    }
}

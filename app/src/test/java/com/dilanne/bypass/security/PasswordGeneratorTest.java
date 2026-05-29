package com.dilanne.bypass.security;

import org.junit.Test;
import static org.junit.Assert.*;

public class PasswordGeneratorTest {

    private final PasswordGenerator generator = new PasswordGenerator();

    @Test
    public void testPasswordLength() {
        int length = 16;
        String password = generator.generate(length, true, true, true, true);
        assertEquals(length, password.length());
    }

    @Test
    public void testEmptyPoolReturnsEmptyString() {
        String password = generator.generate(12, false, false, false, false);
        assertEquals("", password);
    }

    @Test
    public void testIncludesAtLeastOneOfEachCategory() {
        String password = generator.generate(4, true, true, true, true);
        
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSymbol = false;
        
        String symbols = "#&@!$*-+?_";
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if (symbols.indexOf(c) != -1) hasSymbol = true;
        }
        
        assertTrue("Should have uppercase", hasUpper);
        assertTrue("Should have lowercase", hasLower);
        assertTrue("Should have digit", hasDigit);
        assertTrue("Should have symbol", hasSymbol);
    }
}

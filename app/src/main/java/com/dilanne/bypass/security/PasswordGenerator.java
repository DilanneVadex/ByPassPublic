package com.dilanne.bypass.security;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PasswordGenerator {
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMBERS = "0123456789";
    private static final String SYMBOLS = "#&@!$*-+?_";

    private final SecureRandom random = new SecureRandom();

    public String generate(int length, boolean useUpper, boolean useLower, boolean useNumbers, boolean useSymbols) {
        StringBuilder characterPool = new StringBuilder();
        List<Character> result = new ArrayList<>();

        if (useUpper) {
            characterPool.append(UPPER);
            result.add(UPPER.charAt(random.nextInt(UPPER.length())));
        }
        if (useLower) {
            characterPool.append(LOWER);
            result.add(LOWER.charAt(random.nextInt(LOWER.length())));
        }
        if (useNumbers) {
            characterPool.append(NUMBERS);
            result.add(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        }
        if (useSymbols) {
            characterPool.append(SYMBOLS);
            result.add(SYMBOLS.charAt(random.nextInt(SYMBOLS.length())));
        }

        if (characterPool.length() == 0) {
            return "";
        }

        while (result.size() < length) {
            result.add(characterPool.charAt(random.nextInt(characterPool.length())));
        }

        Collections.shuffle(result);
        StringBuilder password = new StringBuilder();
        for (char c : result) {
            password.append(c);
        }

        return password.toString();
    }
}

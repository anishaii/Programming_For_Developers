package Question1;

import java.util.*;

public class PinValidator {

    /**
     * Determines the minimum number of changes required to make a given PIN code strong
     * based on the following criteria:
     * 1. Length must be between 6 and 20 characters.
     * 2. Must contain at least one lowercase letter, one uppercase letter, and one digit.
     * 3. Must not contain three or more consecutive repeating characters.
     *
     *  pin_code The input PIN code string to validate.
     *  Minimum number of changes (insertions, deletions, or replacements) required.
     */
    public static int strongPINChanges(String pin_code) {
        int n = pin_code.length();

        // Step 1: Check for missing character types (lowercase, uppercase, digit)
        boolean hasLower = false, hasUpper = false, hasDigit = false;
        for (char ch : pin_code.toCharArray()) {
            if (Character.isLowerCase(ch)) hasLower = true;
            else if (Character.isUpperCase(ch)) hasUpper = true;
            else if (Character.isDigit(ch)) hasDigit = true;
        }

        // Count how many of the required character types are missing
        int missingTypes = 0;
        if (!hasLower) missingTypes++;
        if (!hasUpper) missingTypes++;
        if (!hasDigit) missingTypes++;

        // Step 2: Find sequences with three or more repeating characters
        List<Integer> repeats = new ArrayList<>();
        int i = 2;
        while (i < n) {
            // Check if current and previous two characters are the same
            if (pin_code.charAt(i) == pin_code.charAt(i - 1) && pin_code.charAt(i) == pin_code.charAt(i - 2)) {
                int j = i - 2;
                while (i < n && pin_code.charAt(i) == pin_code.charAt(j)) {
                    i++;
                }
                repeats.add(i - j); // Store length of this repeating sequence
            } else {
                i++;
            }
        }

        // Calculate how many replacements are needed for repeating sequences
        int totalReplace = 0;
        for (int len : repeats) {
            totalReplace += len / 3;
        }

        // Step 3: If PIN is too short
        if (n < 6) {
            // Need to both satisfy missing character types and minimum length
            return Math.max(missingTypes, 6 - n);
        }

        // Step 4: If PIN is within acceptable length (6 to 20)
        else if (n <= 20) {
            // Only need to fix missing character types and repeated sequences
            return Math.max(missingTypes, totalReplace);
        }

        // Step 5: If PIN is too long (> 20)
        else {
            int deleteCount = n - 20; // Number of deletions needed to reach max length
            int remainingDelete = deleteCount;

            // Step 6: Optimize deletions to reduce needed replacements for repeats
            int[] buckets = new int[3]; // Count sequences based on len % 3

            for (int len : repeats) {
                buckets[len % 3]++;
            }

            // Try deleting 1 char from sequences where len % 3 == 0
            for (int mod = 0; mod < 3; mod++) {
                int limit = buckets[mod];
                for (int j = 0; j < limit && remainingDelete > 0; j++) {
                    int deletionsUsed = mod + 1;
                    if (remainingDelete >= deletionsUsed) {
                        remainingDelete -= deletionsUsed;
                        totalReplace--;
                    } else {
                        break;
                    }
                }
            }

            // Further deletions reduce replacements 1 per 3 deletions
            totalReplace -= remainingDelete / 3;

            // Total changes = deletions + max(missing types, remaining replacements)
            return deleteCount + Math.max(missingTypes, Math.max(0, totalReplace));
        }
    }

    public static void main(String[] args) {
        System.out.println(strongPINChanges("X1!"));          // Output: 3
        System.out.println(strongPINChanges("123456"));       // Output: 2
        System.out.println(strongPINChanges("Aa1234!"));      // Output: 0
        System.out.println(strongPINChanges("aaaa1111AAAA")); // Output: depends on patterns
        System.out.println(strongPINChanges("aaaaaaaaaaaaaaaaaaaaa")); // Test case >20 chars
    }
}

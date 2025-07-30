package Question1;

import java.util.*;

public class PinValidator {

    public static int strongPINChanges(String pin_code) {
        int n = pin_code.length();

        // Step 1: Check for missing character types
        boolean hasLower = false, hasUpper = false, hasDigit = false;
        for (char ch : pin_code.toCharArray()) {
            if (Character.isLowerCase(ch)) hasLower = true;
            else if (Character.isUpperCase(ch)) hasUpper = true;
            else if (Character.isDigit(ch)) hasDigit = true;
        }

        int missingTypes = 0;
        if (!hasLower) missingTypes++;
        if (!hasUpper) missingTypes++;
        if (!hasDigit) missingTypes++;

        // Step 2: Count repeating sequences
        List<Integer> repeats = new ArrayList<>();
        int i = 2;
        while (i < n) {
            if (pin_code.charAt(i) == pin_code.charAt(i - 1) && pin_code.charAt(i) == pin_code.charAt(i - 2)) {
                int j = i - 2;
                while (i < n && pin_code.charAt(i) == pin_code.charAt(j)) {
                    i++;
                }
                repeats.add(i - j); // length of the repeating sequence
            } else {
                i++;
            }
        }

        int totalReplace = 0;
        for (int len : repeats) {
            totalReplace += len / 3; // Each 3 same chars need 1 replacement
        }

        // Step 3: Length-based handling
        if (n < 6) {
            return Math.max(missingTypes, 6 - n);
        } else if (n <= 20) {
            return Math.max(missingTypes, totalReplace);
        } else {
            int deleteCount = n - 20;
            int remainingDelete = deleteCount;

            // Step 4: Optimize deletions to reduce replacements
            // Prioritize deletions based on mod 3
            int[] buckets = new int[3]; // buckets[0] = count of len%3==0, buckets[1] = len%3==1, ...

            for (int len : repeats) {
                buckets[len % 3]++;
            }

            // Apply deletions to reduce replacements as effectively as possible
            for (int mod = 0; mod < 3; mod++) {
                int limit = mod == 0 ? buckets[0] : (mod == 1 ? buckets[1] : buckets[2]);
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

            // Each 3 remaining deletions reduce one replacement
            totalReplace -= remainingDelete / 3;

            return deleteCount + Math.max(missingTypes, Math.max(0, totalReplace));
        }
    }

    public static void main(String[] args) {
        System.out.println(strongPINChanges("X1!"));         // Output: 3
        System.out.println(strongPINChanges("123456"));      // Output: 2
        System.out.println(strongPINChanges("Aa1234!"));     // Output: 0
        System.out.println(strongPINChanges("aaaa1111AAAA"));// Output depends on length and repeats
        System.out.println(strongPINChanges("aaaaaaaaaaaaaaaaaaaaa")); // Test case >20 chars with repeats
    }
}

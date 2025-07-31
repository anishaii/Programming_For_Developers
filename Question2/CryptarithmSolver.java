package Question2;

import java.util.*;

public class CryptarithmSolver {

    // Input words to sum (left side of the equation)
    static String[] words = {"STAR", "MOON"};

    // Result word (right side of the equation)
    static String result = "NIGHT";

    // Mapping from character to digit
    static Map<Character, Integer> charToDigit = new HashMap<>();

    // Array to track which digits (0–9) have been used
    static boolean[] usedDigits = new boolean[10];

    // List of all unique letters in the equation
    static List<Character> letters = new ArrayList<>();

    public static void main(String[] args) {
        // Step 1: Collect all unique letters from the words and the result
        Set<Character> uniqueLetters = new HashSet<>();
        for (String w : words) {
            for (char c : w.toCharArray()) uniqueLetters.add(c);
        }
        for (char c : result.toCharArray()) uniqueLetters.add(c);

        letters.addAll(uniqueLetters);

        // Step 2: If more than 10 unique letters, no solution possible (only digits 0-9 available)
        if (letters.size() > 10) {
            System.out.println("Too many unique letters (>10), no solution possible.");
            return;
        }

        // Step 3: Start backtracking to assign digits to letters
        if (solve(0)) {
            printSolution(); // If a valid solution is found, print it
        } else {
            System.out.println("No solution found.");
        }
    }

    /**
     * Recursively assigns digits to letters using backtracking.
     * Ensures:
     * - Each letter gets a unique digit.
     * - No leading letter (first character of any word/result) is assigned digit 0.
     */
    static boolean solve(int index) {
        if (index == letters.size()) {
            // All letters have been assigned digits; check if the equation is satisfied
            return checkSolution();
        }

        char c = letters.get(index);

        // Try all digits 0–9 for this letter
        for (int d = 0; d <= 9; d++) {
            if (!usedDigits[d]) {
                // Skip leading zero assignments
                if (d == 0 && isLeadingLetter(c)) continue;

                // Assign digit and mark as used
                usedDigits[d] = true;
                charToDigit.put(c, d);

                // Recurse to next letter
                if (solve(index + 1)) return true;

                // Backtrack: unassign digit
                usedDigits[d] = false;
                charToDigit.remove(c);
            }
        }

        return false; // No valid assignment found for this configuration
    }

    /**
     * Returns true if the character is the leading character of any word or result.
     * Leading letters cannot be assigned zero.
     */
    static boolean isLeadingLetter(char c) {
        for (String w : words) {
            if (w.charAt(0) == c) return true;
        }
        return result.charAt(0) == c;
    }

    /**
     * Converts a word into a number based on the current character-to-digit mapping.
     * For example, if STAR = 8425, returns 8425.
     */
    static int wordToNumber(String w) {
        int num = 0;
        for (char c : w.toCharArray()) {
            num = num * 10 + charToDigit.get(c);
        }
        return num;
    }

    /**
     * Checks if the current mapping satisfies the equation:
     * Sum of words == result word
     */
    static boolean checkSolution() {
        int sum = 0;
        for (String w : words) {
            sum += wordToNumber(w);
        }
        return sum == wordToNumber(result);
    }

    /**
     * Displays the solution: letter-digit mappings and converted numbers.
     */
    static void printSolution() {
        System.out.println("✅ Solution found:");
        for (Map.Entry<Character, Integer> entry : charToDigit.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
        System.out.println();

        for (String w : words) {
            System.out.println(w + " = " + wordToNumber(w));
        }
        System.out.println(result + " = " + wordToNumber(result));
        System.out.println("Check: sum of words = " +
                Arrays.stream(words).mapToInt(CryptarithmSolver::wordToNumber).sum());
    }
}

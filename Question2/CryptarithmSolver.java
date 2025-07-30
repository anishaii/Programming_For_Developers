package Question2;

import java.util.*;

public class CryptarithmSolver {

    static String[] words = {"STAR", "MOON"};  // Words to sum
    static String result = "NIGHT";            // Result word

    static Map<Character, Integer> charToDigit = new HashMap<>();
    static boolean[] usedDigits = new boolean[10];
    static List<Character> letters = new ArrayList<>();

    public static void main(String[] args) {
        // Collect unique letters from words and result
        Set<Character> uniqueLetters = new HashSet<>();
        for (String w : words) {
            for (char c : w.toCharArray()) uniqueLetters.add(c);
        }
        for (char c : result.toCharArray()) uniqueLetters.add(c);

        letters.addAll(uniqueLetters);

        // More than 10 unique letters means no solution (digits 0-9 only)
        if (letters.size() > 10) {
            System.out.println("Too many unique letters (>10), no solution possible.");
            return;
        }

        // Start backtracking to assign digits to letters
        if (solve(0)) {
            printSolution();
        } else {
            System.out.println("No solution found.");
        }
    }

    /**
     * Backtracking function to assign digits to letters uniquely.
     * Ensures no leading letter gets digit zero.
     */
    static boolean solve(int index) {
        if (index == letters.size()) {
            return checkSolution();
        }

        char c = letters.get(index);
        for (int d = 0; d <= 9; d++) {
            if (!usedDigits[d]) {
                if (d == 0 && isLeadingLetter(c)) continue;  // No leading zeros

                usedDigits[d] = true;
                charToDigit.put(c, d);

                if (solve(index + 1)) return true;

                // Backtrack
                usedDigits[d] = false;
                charToDigit.remove(c);
            }
        }
        return false;
    }

    /**
     * Checks if character is a leading letter of any word or the result.
     */
    static boolean isLeadingLetter(char c) {
        for (String w : words) {
            if (w.charAt(0) == c) return true;
        }
        return result.charAt(0) == c;
    }

    /**
     * Converts a word into a number based on current letter-digit assignments.
     */
    static int wordToNumber(String w) {
        int num = 0;
        for (char c : w.toCharArray()) {
            num = num * 10 + charToDigit.get(c);
        }
        return num;
    }

    /**
     * Verifies if the sum of all words equals the result number.
     */
    static boolean checkSolution() {
        int sum = 0;
        for (String w : words) {
            sum += wordToNumber(w);
        }
        return sum == wordToNumber(result);
    }

    /**
     * Prints the digit assignments and numerical values of words.
     */
    static void printSolution() {
        System.out.println("Solution found:");
        for (Map.Entry<Character, Integer> entry : charToDigit.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
        System.out.println();

        for (String w : words) {
            System.out.println(w + " = " + wordToNumber(w));
        }
        System.out.println(result + " = " + wordToNumber(result));
        System.out.println("Check: sum of words = " + Arrays.stream(words).mapToInt(CryptarithmSolver::wordToNumber).sum());
    }
}

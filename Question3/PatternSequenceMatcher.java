package Question3;

public class PatternSequenceMatcher {

    /**
    
    /**
     * This method calculates how many times the sequence formed by repeating `p2` `t2` times
     * can be extracted as a subsequence from the sequence formed by repeating `p1` `t1` times.
     * ---------------------------------------------------------------------------------------------
     * Approach:
     * - We simulate matching characters from p2 inside the repeated p1.
     * - If characters match, we advance the pointer for p2.
     * - If we complete one p2 match, we reset the pointer and increment the count.
     * - Continue until all t1 copies of p1 are processed.
     * -------------------------------------------------------------------------------------------------
     * Algorithm Used:
     *  Greedy Matching with Pointers:
     *     Try to match as many p2 patterns inside the p1 × t1 stream as possible.
     *     Always preserve the order and allow skipping characters from p1.
     *
     */
    public static int maxRepetitions(String p1, int t1, String p2, int t2) {
        int len1 = p1.length();
        int len2 = p2.length();

        int i = 0, j = 0;      // pointers to characters in p1 and p2
        int count1 = 0;        // how many times p1 has been processed
        int count2 = 0;        // how many full p2 patterns matched

        while (count1 < t1) {
            if (p1.charAt(i) == p2.charAt(j)) {
                j++;
                if (j == len2) {
                    j = 0;
                    count2++;  // one full p2 matched
                }
            }

            i++;
            if (i == len1) {
                i = 0;
                count1++;      // one p1 processed
            }
        }

        System.out.println("Total full p2 matches: " + count2);
        return count2;
    }

    public static void main(String[] args) {
        String p1 = "bca";
        int t1 = 6;
        String p2 = "ba";

        int t2_1 = 3;
        int result1 = maxRepetitions(p1, t1, p2, t2_1);
        System.out.println("Result for t2 = " + t2_1 + ": " + result1);  // ✅ Expected: 3

        int t2_2 = 5;
        int result2 = maxRepetitions(p1, t1, p2, t2_2);
        System.out.println("Result for t2 = " + t2_2 + ": " + result2);  // ✅ Expected: 3
    }
}

package Question3;

public class MagicalWordMaximizer {

    /**
     * Given a manuscript M, this method finds two non-overlapping magical words
     * (odd-length palindromes) to maximize the product of their lengths.
     *
     * -----------------------------
     * Magical Word Definition:
     * - A palindrome (same forward & backward)
     * - Has an **odd** length only
     *
     * -----------------------------
     * Approach (Center Expansion + DP):
     * 1. For each index, treat it as a center and expand to find all odd-length palindromes.
     * 2. Store max palindrome length ending at or before index `i` in `leftMax[i]`.
     * 3. Store max palindrome length starting at or after index `i` in `rightMax[i]`.
     * 4. Compute prefix max for `leftMax` and suffix max for `rightMax`.
     * 5. Iterate over every split point `i`, compute max product of left × right.
     *
     
     */
    public static int maxMagicalPower(String M) {
        int n = M.length();
        int[] leftMax = new int[n];   // Max magical length ending at or before index i
        int[] rightMax = new int[n];  // Max magical length starting at or after index i

        // Step 1: Expand around each center to find all odd-length palindromes
        for (int center = 0; center < n; center++) {
            int l = center, r = center;
            while (l >= 0 && r < n && M.charAt(l) == M.charAt(r)) {
                int len = r - l + 1;
                if (len % 2 == 1) {
                    leftMax[r] = Math.max(leftMax[r], len);
                    rightMax[l] = Math.max(rightMax[l], len);
                }
                l--;
                r++;
            }
        }

        // Step 2: Compute prefix max for leftMax
        for (int i = 1; i < n; i++) {
            leftMax[i] = Math.max(leftMax[i], leftMax[i - 1]);
        }

        // Step 3: Compute suffix max for rightMax
        for (int i = n - 2; i >= 0; i--) {
            rightMax[i] = Math.max(rightMax[i], rightMax[i + 1]);
        }

        // Step 4: Evaluate max product for valid split positions
        int maxProduct = 0;
        for (int i = 0; i < n - 1; i++) {
            maxProduct = Math.max(maxProduct, leftMax[i] * rightMax[i + 1]);
        }

        return maxProduct;
    }

    public static void main(String[] args) {
        //  5
        System.out.println("Output 1: " + maxMagicalPower("xyzyxabc"));
        
        // 35
        System.out.println("Output 2: " + maxMagicalPower("levelwowracecar"));
    }
}

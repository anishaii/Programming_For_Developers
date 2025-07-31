package Question2;

import java.util.*;

public class WeatherAnomalyDetector {

    /**
     * Counts the number of continuous subarrays (time periods) in temperature_changes
     * whose sum falls within the range [low, high].
     * ----------------------------------------------------------------------------------
     * ➤ Approach:
     * - Use **prefix sum** to keep track of cumulative temperature changes.
     * - Use a **TreeMap** to store the frequency of each prefix sum encountered so far.
     * - For each new prefix sum, check how many previous prefix sums fall in the range:
     *   [prefixSum - high, prefixSum - low] (which would mean the subarray sum is within [low, high]).
     * -----------------------------------------------------------------------------------------------------
     * ➤ Time Complexity: O(n log n) due to TreeMap operations.
     * ➤ Space Complexity: O(n) for storing prefix sums.
     *-----------------------------------------------------------------------------------------------------
     *  temperature_changes Array of daily temperature changes.
     * low Lower threshold for anomaly detection.
     * high Upper threshold for anomaly detection.
     *  Count of valid subarrays (periods) with sum in [low, high].
     */
    public static int countAnomalousPeriods(int[] temperature_changes, int low, int high) {
        // TreeMap stores prefix sums and their frequencies
        TreeMap<Long, Integer> prefixMap = new TreeMap<>();
        prefixMap.put(0L, 1); // Base case: a sum of 0 before any elements

        long prefixSum = 0;
        int count = 0;

        // Traverse through each change and build the prefix sum
        for (int change : temperature_changes) {
            prefixSum += change;

            // Range of prefix sums we're interested in to form a valid subarray sum
            long from = prefixSum - high;
            long to = prefixSum - low;

            // Count how many prefix sums lie in [prefixSum - high, prefixSum - low]
            // This ensures the subarray sum (prefixSum - oldPrefixSum) falls in [low, high]
            for (Map.Entry<Long, Integer> entry : prefixMap.subMap(from, true, to, true).entrySet()) {
                count += entry.getValue();
            }

            // Store/update the current prefix sum in the map
            prefixMap.put(prefixSum, prefixMap.getOrDefault(prefixSum, 0) + 1);
        }

        return count;
    }

    public static void main(String[] args) {
        int[] temp1 = {3, -1, -4, 6, 2};
        System.out.println("Output for temp1 : " + countAnomalousPeriods(temp1, 2, 5)); // Expected: 7

        int[] temp2 = {-2, 3, 1, -5, 4};
        System.out.println("Output for temp2 : " + countAnomalousPeriods(temp2, -1, 2)); // Expected: 7
    }
}

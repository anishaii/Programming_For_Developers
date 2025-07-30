package Question2;

import java.util.*;

public class WeatherAnomalyDetector {

    /**
     * Efficiently counts the number of continuous subarrays
     * where the sum is within [low, high], using prefix sums and TreeMap.
     *
     * Time Complexity: O(n log n)
     */
    public static int countAnomalousPeriods(int[] temperature_changes, int low, int high) {
        TreeMap<Long, Integer> prefixMap = new TreeMap<>();
        prefixMap.put(0L, 1);  // Base case: zero sum before starting

        long prefixSum = 0;
        int count = 0;

        for (int change : temperature_changes) {
            prefixSum += change;

            long from = prefixSum - high;
            long to = prefixSum - low;

            // Count previous prefix sums that would result in a valid subarray sum
            for (Map.Entry<Long, Integer> entry : prefixMap.subMap(from, true, to, true).entrySet()) {
                count += entry.getValue();
            }

            // Record current prefix sum
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

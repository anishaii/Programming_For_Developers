package Question4;

import java.util.*;

public class SecureTransmission {
    private final Map<Integer, List<int[]>> graph;

    /**
     * Constructor to initialize the secure transmission network.
     *
     *  n     Number of offices (nodes)
     *  links Communication links represented as [a, b, strength]
     *              where a and b are connected offices with the given signal strength.
     */
    public SecureTransmission(int n, int[][] links) {
        graph = new HashMap<>();

        // Initialize graph with empty adjacency list for each node
        for (int i = 0; i < n; i++) {
            graph.put(i, new ArrayList<>());
        }

        // Add bidirectional (undirected) edges to the graph
        for (int[] link : links) {
            int a = link[0], b = link[1], strength = link[2];
            graph.get(a).add(new int[]{b, strength});
            graph.get(b).add(new int[]{a, strength});
        }
    }

    /**
     * Checks whether a message can be securely transmitted from sender to receiver
     * using only links with strength strictly less than maxStrength.
     *
     * Approach:
     * - Breadth-First Search (BFS) starting from the sender node
     * - Only follow edges with strength < maxStrength
     * - Return true if we reach the receiver; false otherwise
     *
     * Time Complexity: O(N + E), where N = number of nodes, E = number of links
     *
     *  sender Source office
     * receiver    Target office
     * maxStrength Maximum allowable signal strength
     * True if secure path exists; otherwise false
     */
    public boolean canTransmit(int sender, int receiver, int maxStrength) {
        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        queue.add(sender);
        visited.add(sender);

        while (!queue.isEmpty()) {
            int current = queue.poll();

            if (current == receiver) return true;

            for (int[] neighbor : graph.get(current)) {
                int nextNode = neighbor[0];
                int strength = neighbor[1];

                if (strength < maxStrength && !visited.contains(nextNode)) {
                    visited.add(nextNode);
                    queue.add(nextNode);
                }
            }
        }

        return false;
    }

    public static void main(String[] args) {
        int[][] links = {
            {0, 2, 4},
            {2, 3, 1},
            {2, 1, 3},
            {4, 5, 5}
        };

        SecureTransmission st = new SecureTransmission(6, links);

        // ✅ Test cases with expected output
        System.out.println(st.canTransmit(2, 3, 2)); // true
        System.out.println(st.canTransmit(1, 3, 3)); // false
        System.out.println(st.canTransmit(2, 0, 3)); // true
        System.out.println(st.canTransmit(0, 5, 6)); // false
    }
}

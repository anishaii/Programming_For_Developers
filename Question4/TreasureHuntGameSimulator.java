package Question4;

import java.util.HashSet;
import java.util.Set;

public class TreasureHuntGameSimulator {
      static final int DRAW = 0;
    static final int PLAYER1_WIN = 1;
    static final int PLAYER2_WIN = 2;

    public int treasureGame(int[][] graph) {
        int n = graph.length;
        int[][][] memo = new int[n][n][2]; // memo[p1Pos][p2Pos][turn] = result
        return dfs(graph, 1, 2, 0, memo, new HashSet<>());
    }

    /**
     * Recursive DFS with memoization and cycle detection.
     * 
     * graph - Adjacency list of the graph
     * p1 - Current position of Player 1
     * p2 - Current position of Player 2
     * turn - 0 if it's P1's turn, 1 if it's P2's turn
     * memo - 3D memoization array
     * visited - Set for cycle detection (to catch repeated game states)
     * Result of the game from current state
     */
    private int dfs(int[][] graph, int p1, int p2, int turn, int[][][] memo, Set<String> visited) {
        // Base conditions
        if (p1 == 0) return PLAYER1_WIN;     // Player 1 reached treasure
        if (p1 == p2) return PLAYER2_WIN;    // Player 2 caught Player 1

        // Cycle detection
        String stateKey = p1 + "," + p2 + "," + turn;
        if (visited.contains(stateKey)) return DRAW;

        // Check memoized result
        if (memo[p1][p2][turn] != 0) return memo[p1][p2][turn];

        visited.add(stateKey);
        int result = turn == 0 ? PLAYER2_WIN : PLAYER1_WIN; // Default to opponent win

        if (turn == 0) {
            // Player 1's turn
            for (int next : graph[p1]) {
                int nextResult = dfs(graph, next, p2, 1, memo, visited);
                if (nextResult == PLAYER1_WIN) {
                    result = PLAYER1_WIN;
                    break;
                } else if (nextResult == DRAW) {
                    result = DRAW;
                }
            }
        } else {
            // Player 2's turn
            for (int next : graph[p2]) {
                if (next == 0) continue; // Cannot go to treasure
                int nextResult = dfs(graph, p1, next, 0, memo, visited);
                if (nextResult == PLAYER2_WIN) {
                    result = PLAYER2_WIN;
                    break;
                } else if (nextResult == DRAW) {
                    result = DRAW;
                }
            }
        }

        visited.remove(stateKey); // backtrack
        memo[p1][p2][turn] = result; // memoize result
        return result;
    }

    public static void main(String[] args) {
        TreasureHuntGameSimulator game = new TreasureHuntGameSimulator();

        int[][] graph = {
            {2, 5},      // Node 0
            {3},         // Node 1 (P1 starts)
            {0, 4, 5},   // Node 2 (P2 starts)
            {1, 4, 5},   // Node 3
            {2, 3},      // Node 4
            {0, 2, 3}    // Node 5
        };

        int result = game.treasureGame(graph);
        System.out.println("Game Result: " + result); // Expected output: 0 (Draw)
    }
}

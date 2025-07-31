package Question1;

import java.util.*;

public class MaximizeCapital {

    /**
     * Calculates the maximum capital that can be accumulated by completing at most k projects.
     *
     *  k The maximum number of projects that can be undertaken.
     *  c The initial capital.
     *  revenues An array where revenues[i] is the revenue gained after completing project i.
     *  investments An array where investments[i] is the capital required to start project i.
     *  The maximum capital that can be accumulated after completing at most k projects.
     */
    public static int maximizeCapital(int k, int c, int[] revenues, int[] investments) {
        // Validate input lengths and constraints
        if (revenues == null || investments == null || revenues.length != investments.length) {
            throw new IllegalArgumentException("Revenue and investment arrays must be non-null and of equal length.");
        }

        if (k < 0) {
            throw new IllegalArgumentException("k must be non-negative.");
        }

        int n = revenues.length;
        List<Project> projects = new ArrayList<>();

        // Convert arrays into list of Project objects
        for (int i = 0; i < n; i++) {
            projects.add(new Project(investments[i], revenues[i]));
        }

        // Sort projects based on their required investment (ascending order)
        projects.sort(Comparator.comparingInt(Project::getInvestment));

        // Max-heap to store revenues of projects that can be currently afforded
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());

        int currentProjectIndex = 0;

        // Perform at most k projects
        for (int completed = 0; completed < k; completed++) {
            // Add all projects that can be started with current capital to the max-heap
            while (currentProjectIndex < n && projects.get(currentProjectIndex).getInvestment() <= c) {
                maxHeap.offer(projects.get(currentProjectIndex).getRevenue());
                currentProjectIndex++;
            }

            // If no affordable project is available, stop
            if (maxHeap.isEmpty()) {
                break;
            }

            // Select and complete the most profitable project
            c += maxHeap.poll();
        }

        // Return the final capital
        return c;
    }

    /**
     * Helper class to represent a project with its required investment and revenue.
     */
    private static class Project {
        private final int investment;
        private final int revenue;

        public Project(int investment, int revenue) {
            this.investment = investment;
            this.revenue = revenue;
        }

        public int getInvestment() {
            return investment;
        }

        public int getRevenue() {
            return revenue;
        }
    }

    public static void main(String[] args) {
        // Example 1
        int[] revenues1 = {2, 5, 8};
        int[] investments1 = {0, 2, 3};
        System.out.println(maximizeCapital(2, 0, revenues1, investments1)); // Output: 7

        // Example 2
        int[] revenues2 = {3, 6, 10};
        int[] investments2 = {1, 3, 5};
        System.out.println(maximizeCapital(3, 1, revenues2, investments2)); // Output: 19
    }
}

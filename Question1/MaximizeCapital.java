package Question1;

import java.util.*;

public class MaximizeCapital {
    
    /**
     * Calculates the maximum capital after completing up to k projects
     *  k Maximum number of projects to complete
     *  c Initial capital
     *  revenues Array of project revenues
     * investments Array of required investments
     * Maximum achievable capital
     */
    public static int maximizeCapital(int k, int c, int[] revenues, int[] investments) {
        // Input validation
        if (revenues == null || investments == null || revenues.length != investments.length) {
            throw new IllegalArgumentException("Invalid input arrays");
        }
        if (k < 0) {
            throw new IllegalArgumentException("k must be non-negative");
        }

        int n = revenues.length;
        List<Project> projects = new ArrayList<>();
        
        // Create project objects
        for (int i = 0; i < n; i++) {
            projects.add(new Project(investments[i], revenues[i]));
        }

        // Sort by investment required
        projects.sort(Comparator.comparingInt(Project::getInvestment));

        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());
        int currentProject = 0;
        
        for (int completed = 0; completed < k; completed++) {
            // Add all affordable projects to heap
            while (currentProject < n && projects.get(currentProject).getInvestment() <= c) {
                maxHeap.offer(projects.get(currentProject).getRevenue());
                currentProject++;
            }

            if (maxHeap.isEmpty()) {
                break; // No more affordable projects
            }

            // Complete the most profitable project
            c += maxHeap.poll();
        }

        return c;
    }

    // Helper class to represent projects
    private static class Project {
        private final int investment;
        private final int revenue;

        public Project(int investment, int revenue) {
            this.investment = investment;
            this.revenue = revenue;
        }

        public int getInvestment() { return investment; }
        public int getRevenue() { return revenue; }
    }

    public static void main(String[] args) {
        // Test cases
        int[] revenues1 = {2, 5, 8};
        int[] investments1 = {0, 2, 3};
        System.out.println(maximizeCapital(2, 0, revenues1, investments1)); // 7

        int[] revenues2 = {3, 6, 10};
        int[] investments2 = {1, 3, 5};
        System.out.println(maximizeCapital(3, 1, revenues2, investments2)); // 19
    }
}
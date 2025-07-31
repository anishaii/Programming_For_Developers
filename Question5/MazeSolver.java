package Question5;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*; // Import all utilities including List, ArrayList, Stack, Queue, Collections, etc.

// Main class extending JFrame to create the window for the Maze Solver
public class MazeSolver extends JFrame {
    // Constructor to set up the GUI components
    public MazeSolver() {
        setTitle("Maze Solver"); // Set window title
        
        MazePanel mazePanel = new MazePanel(); // Create the maze drawing and logic panel

        // Buttons for controlling the maze solving and resetting
        JButton dfsBtn = new JButton("Solve DFS");
        JButton bfsBtn = new JButton("Solve BFS");
        JButton resetBtn = new JButton("Generate New Maze");

        // Add action listeners for each button to trigger maze solving or generation
        dfsBtn.addActionListener(_ -> mazePanel.solveDFS());
        bfsBtn.addActionListener(_ -> mazePanel.solveBFS());
        resetBtn.addActionListener(_ -> {
            mazePanel.generateMaze(); // Generate a new maze
            mazePanel.repaint();      // Redraw the maze panel
        });

        // Panel to hold the control buttons at the bottom
        JPanel controlPanel = new JPanel();
        controlPanel.add(dfsBtn);
        controlPanel.add(bfsBtn);
        controlPanel.add(resetBtn);

        // Add the maze panel in the center and control panel at the bottom
        add(mazePanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        pack(); // Adjust window size to fit contents
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Close program when window closes
        setLocationRelativeTo(null); // Center window on screen
        setVisible(true); // Show the window
    }

    // Main method to launch the application on the Event Dispatch Thread
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MazeSolver::new);
    }
}

// Class representing each cell in the maze grid
class Cell {
    int row, col;       // Position of the cell in the maze grid
    boolean isWall;     // True if the cell is a wall, false if path
    boolean visited;    // Used during maze solving to mark visited cells
    Cell parent;        // To track the path back from the end to start

    // Constructor to initialize cell with position and wall status
    public Cell(int row, int col, boolean isWall) {
        this.row = row;
        this.col = col;
        this.isWall = isWall;
        this.visited = false;  // Not visited initially
        this.parent = null;    // No parent initially
    }
}

// Panel that draws the maze and contains maze logic and solving algorithms
class MazePanel extends JPanel implements MouseListener {
    private Cell[][] maze;                     // 2D array representing the maze grid
    private java.util.List<Cell> path = new java.util.ArrayList<>(); // Final solved path cells
    private final int rows = 21, cols = 21;   // Maze dimensions (odd for maze carving)
    private final int cellSize = 25;           // Size of each cell in pixels
    private Cell startCell, endCell;           // Start and end points in the maze

    private javax.swing.Timer timer;           // Timer for animating the solving process
    private int timerIndex;                    // Index to track animation progress
    private boolean solving = false;           // Flag to prevent multiple simultaneous solves

    private java.util.List<Cell> visitOrder = new java.util.ArrayList<>(); // Order cells are visited

    // Constructor to set panel size, add mouse listener, generate initial maze
    public MazePanel() {
        setPreferredSize(new Dimension(cols * cellSize, rows * cellSize)); // Panel size based on maze
        addMouseListener(this);    // Add mouse interaction to allow start/end selection
        generateMaze();            // Generate the initial maze layout
        startCell = maze[0][0];    // Default start at top-left corner
        endCell = maze[rows - 1][cols - 1]; // Default end at bottom-right corner
    }

    // Method to generate a new maze using recursive backtracking carving
    public void generateMaze() {
        maze = new Cell[rows][cols]; // Initialize maze grid

        // Initially set every cell as a wall
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                maze[r][c] = new Cell(r, c, true);
            }
        }

        carvePassagesFrom(0, 0); // Start carving from the top-left cell

        // Set start and end cells explicitly as paths (not walls)
        startCell = maze[0][0];
        endCell = maze[rows - 1][cols - 1];
        startCell.isWall = false;
        endCell.isWall = false;

        path.clear();       // Clear previous solution path
        visitOrder.clear(); // Clear previous visited order
        repaint();          // Redraw panel with new maze
    }

    // Recursive backtracking method to carve passages in the maze grid
    private void carvePassagesFrom(int r, int c) {
        maze[r][c].isWall = false; // Mark current cell as path

        int[] dr = {-1, 1, 0, 0};  // Direction vectors for rows (up/down)
        int[] dc = {0, 0, -1, 1};  // Direction vectors for cols (left/right)
        java.util.List<Integer> dirs = java.util.Arrays.asList(0, 1, 2, 3);
        java.util.Collections.shuffle(dirs); // Shuffle directions to randomize maze

        // Try carving in each direction
        for (int i : dirs) {
            int nr = r + dr[i] * 2; // Move two cells in chosen direction (skip walls)
            int nc = c + dc[i] * 2;

            // Check if new cell is inside maze and is still a wall
            if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && maze[nr][nc].isWall) {
                // Remove wall between current and new cell
                maze[r + dr[i]][c + dc[i]].isWall = false;
                carvePassagesFrom(nr, nc); // Recurse from new cell
            }
        }
    }

    // Get all valid neighbor cells (up/down/left/right) of a given cell
    private java.util.List<Cell> getNeighbors(Cell cell) {
        java.util.List<Cell> neighbors = new java.util.ArrayList<>();
        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};

        for (int i = 0; i < 4; i++) {
            int nr = cell.row + dr[i];
            int nc = cell.col + dc[i];

            // Only add neighbors inside bounds
            if (nr >= 0 && nr < rows && nc >= 0 && nc < cols) {
                neighbors.add(maze[nr][nc]);
            }
        }
        return neighbors;
    }

    // Reset maze cells before solving: clear visited flags, parents, path and stops timer
    private void resetMaze() {
        for (Cell[] row : maze) {
            for (Cell cell : row) {
                cell.visited = false;
                cell.parent = null;
            }
        }
        path.clear();       // Clear any previous solution path
        visitOrder.clear(); // Clear the visit order list
        solving = false;    // Reset solving flag
        if (timer != null && timer.isRunning()) {
            timer.stop();  // Stop animation timer if running
        }
    }

    // Solve the maze using Depth-First Search algorithm
    public void solveDFS() {
        if (solving) return; // Prevent multiple solves at once
        resetMaze();         // Reset the maze state before solving
        solving = true;      // Mark as solving to block input

        Stack<Cell> stack = new Stack<>();
        stack.push(startCell); // Start from startCell

        visitOrder.clear(); // Clear visit order

        // DFS loop until stack is empty
        while (!stack.isEmpty()) {
            Cell current = stack.pop();
            if (current.visited || current.isWall) continue; // Skip visited or walls

            current.visited = true;
            visitOrder.add(current); // Record order of visits

            if (current == endCell) break; // Stop if reached the end

            // Push all unvisited, non-wall neighbors onto stack
            for (Cell neighbor : getNeighbors(current)) {
                if (!neighbor.visited && !neighbor.isWall) {
                    neighbor.parent = current; // Track path
                    stack.push(neighbor);
                }
            }
        }

        startAnimation(); // Start animating the solve process
    }

    // Solve the maze using Breadth-First Search algorithm
    public void solveBFS() {
        if (solving) return; // Prevent multiple solves at once
        resetMaze();         // Reset maze state before solving
        solving = true;      // Mark as solving

        Queue<Cell> queue = new LinkedList<>();
        queue.add(startCell); // Start from startCell

        visitOrder.clear(); // Clear visit order

        // BFS loop until queue is empty
        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            if (current.visited || current.isWall) continue; // Skip visited or walls

            current.visited = true;
            visitOrder.add(current); // Record order of visits

            if (current == endCell) break; // Stop if reached the end

            // Enqueue all unvisited, non-wall neighbors
            for (Cell neighbor : getNeighbors(current)) {
                if (!neighbor.visited && !neighbor.isWall) {
                    neighbor.parent = current; // Track path
                    queue.add(neighbor);
                }
            }
        }

        startAnimation(); // Start animating the solve process
    }

    // Animate the visiting order and final path using a timer
    private void startAnimation() {
        timerIndex = 0; // Reset animation index
        path.clear();   // Clear any previous path

        // Timer fires every 30ms to animate the solution steps
        timer = new javax.swing.Timer(30, _ -> {  // unused lambda param replaced by _
            if (timerIndex < visitOrder.size()) {
                repaint();   // Repaint to show visited cells animation
                timerIndex++;
            } else {
                // After all visits animated, reconstruct path if exists
                if (endCell.parent != null) {
                    reconstructPath(endCell); // Backtrack path from end to start
                    repaint();                // Repaint to show final path
                    JOptionPane.showMessageDialog(this, "Maze solved successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "No path found! Try regenerating the maze.");
                }
                solving = false; // Mark solving finished
                timer.stop();    // Stop animation timer
            }
        });
        timer.start(); // Start the animation timer
    }

    // Backtrack from the end cell to start cell via parent links to build the path
    private void reconstructPath(Cell end) {
        path.clear();
        for (Cell c = end; c != null; c = c.parent) {
            path.add(c);
        }
        java.util.Collections.reverse(path); // Reverse to get path start->end order
    }

    // Override paintComponent to draw the maze, visited cells, path, start and end
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw the maze grid
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = maze[r][c];
                if (cell.isWall) {
                    g.setColor(Color.BLACK); // Wall cells in black
                } else {
                    g.setColor(Color.WHITE); // Path cells in white
                }
                g.fillRect(c * cellSize, r * cellSize, cellSize, cellSize);
                g.setColor(Color.GRAY);
                g.drawRect(c * cellSize, r * cellSize, cellSize, cellSize); // Cell border
            }
        }

        // Draw visited cells in light blue (animated)
        g.setColor(new Color(173, 216, 230));
        for (int i = 0; i < Math.min(timerIndex, visitOrder.size()); i++) {
            Cell v = visitOrder.get(i);
            g.fillRect(v.col * cellSize, v.row * cellSize, cellSize, cellSize);
        }

        // Draw the final solved path in green
        g.setColor(Color.GREEN);
        for (Cell cell : path) {
            g.fillRect(cell.col * cellSize, cell.row * cellSize, cellSize, cellSize);
        }

        // Draw the start cell in blue
        g.setColor(Color.BLUE);
        g.fillRect(startCell.col * cellSize, startCell.row * cellSize, cellSize, cellSize);

        // Draw the end cell in red
        g.setColor(Color.RED);
        g.fillRect(endCell.col * cellSize, endCell.row * cellSize, cellSize, cellSize);
    }

    // Mouse click handler to allow user to set start (left click) and end (right click) points
    @Override
    public void mouseClicked(MouseEvent e) {
        if (solving) return; // Do not allow changes while solving

        int c = e.getX() / cellSize; // Calculate column clicked
        int r = e.getY() / cellSize; // Calculate row clicked
        if (r < 0 || r >= rows || c < 0 || c >= cols) return; // Outside bounds

        Cell clicked = maze[r][c];
        if (clicked.isWall) return; // Ignore clicks on walls

        if (SwingUtilities.isLeftMouseButton(e)) {
            startCell = clicked; // Left click sets start cell
        } else if (SwingUtilities.isRightMouseButton(e)) {
            endCell = clicked;   // Right click sets end cell
        }
        path.clear();       // Clear previous path
        visitOrder.clear(); // Clear visited cells
        repaint();          // Redraw with new start/end
    }

    // Unused mouse events (required by MouseListener interface)
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}

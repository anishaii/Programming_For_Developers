package Question5;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*; // Import all utilities including List, ArrayList, Stack, Queue, Collections, etc.

public class MazeSolver extends JFrame {
    public MazeSolver() {
        setTitle("Maze Solver");
        MazePanel mazePanel = new MazePanel();

        JButton dfsBtn = new JButton("Solve DFS");
        JButton bfsBtn = new JButton("Solve BFS");
        JButton resetBtn = new JButton("Generate New Maze");

        dfsBtn.addActionListener(_e -> mazePanel.solveDFS());
        bfsBtn.addActionListener(_e -> mazePanel.solveBFS());
        resetBtn.addActionListener(_e -> {
            mazePanel.generateMaze();
            mazePanel.repaint();
        });

        JPanel controlPanel = new JPanel();
        controlPanel.add(dfsBtn);
        controlPanel.add(bfsBtn);
        controlPanel.add(resetBtn);

        add(mazePanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MazeSolver::new);
    }
}

class Cell {
    int row, col;
    boolean isWall, visited;
    Cell parent;

    public Cell(int row, int col, boolean isWall) {
        this.row = row;
        this.col = col;
        this.isWall = isWall;
        this.visited = false;
        this.parent = null;
    }
}

class MazePanel extends JPanel implements MouseListener {
    private Cell[][] maze;
    private java.util.List<Cell> path = new java.util.ArrayList<>();
    private final int rows = 21, cols = 21, cellSize = 25; // odd for maze carving
    private Cell startCell, endCell;

    private javax.swing.Timer timer;
    private int timerIndex;
    private boolean solving = false;

    private java.util.List<Cell> visitOrder = new java.util.ArrayList<>();

    public MazePanel() {
        setPreferredSize(new Dimension(cols * cellSize, rows * cellSize));
        addMouseListener(this);
        generateMaze();
        startCell = maze[0][0];
        endCell = maze[rows - 1][cols - 1];
    }

    public void generateMaze() {
        maze = new Cell[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                maze[r][c] = new Cell(r, c, true);
            }
        }

        carvePassagesFrom(0, 0);

        startCell = maze[0][0];
        endCell = maze[rows - 1][cols - 1];
        startCell.isWall = false;
        endCell.isWall = false;

        path.clear();
        visitOrder.clear();
        repaint();
    }

    private void carvePassagesFrom(int r, int c) {
        maze[r][c].isWall = false;

        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};
        java.util.List<Integer> dirs = java.util.Arrays.asList(0, 1, 2, 3);
        java.util.Collections.shuffle(dirs);

        for (int i : dirs) {
            int nr = r + dr[i] * 2;
            int nc = c + dc[i] * 2;

            if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && maze[nr][nc].isWall) {
                maze[r + dr[i]][c + dc[i]].isWall = false;
                carvePassagesFrom(nr, nc);
            }
        }
    }

    private java.util.List<Cell> getNeighbors(Cell cell) {
        java.util.List<Cell> neighbors = new java.util.ArrayList<>();
        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};

        for (int i = 0; i < 4; i++) {
            int nr = cell.row + dr[i];
            int nc = cell.col + dc[i];

            if (nr >= 0 && nr < rows && nc >= 0 && nc < cols) {
                neighbors.add(maze[nr][nc]);
            }
        }
        return neighbors;
    }

    private void resetMaze() {
        for (Cell[] row : maze) {
            for (Cell cell : row) {
                cell.visited = false;
                cell.parent = null;
            }
        }
        path.clear();
        visitOrder.clear();
        solving = false;
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
    }

    public void solveDFS() {
        if (solving) return;
        resetMaze();
        solving = true;

        Stack<Cell> stack = new Stack<>();
        stack.push(startCell);

        visitOrder.clear();

        while (!stack.isEmpty()) {
            Cell current = stack.pop();
            if (current.visited || current.isWall) continue;

            current.visited = true;
            visitOrder.add(current);

            if (current == endCell) break;

            for (Cell neighbor : getNeighbors(current)) {
                if (!neighbor.visited && !neighbor.isWall) {
                    neighbor.parent = current;
                    stack.push(neighbor);
                }
            }
        }

        startAnimation();
    }

    public void solveBFS() {
        if (solving) return;
        resetMaze();
        solving = true;

        Queue<Cell> queue = new LinkedList<>();
        queue.add(startCell);

        visitOrder.clear();

        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            if (current.visited || current.isWall) continue;

            current.visited = true;
            visitOrder.add(current);

            if (current == endCell) break;

            for (Cell neighbor : getNeighbors(current)) {
                if (!neighbor.visited && !neighbor.isWall) {
                    neighbor.parent = current;
                    queue.add(neighbor);
                }
            }
        }

        startAnimation();
    }

    private void startAnimation() {
        timerIndex = 0;
        path.clear();

        timer = new javax.swing.Timer(30, e -> {
            if (timerIndex < visitOrder.size()) {
                repaint();
                timerIndex++;
            } else {
                if (endCell.parent != null) {
                    reconstructPath(endCell);
                    repaint();
                    JOptionPane.showMessageDialog(this, "Maze solved successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "No path found! Try regenerating the maze.");
                }
                solving = false;
                timer.stop();
            }
        });
        timer.start();
    }

    private void reconstructPath(Cell end) {
        path.clear();
        for (Cell c = end; c != null; c = c.parent) {
            path.add(c);
        }
        java.util.Collections.reverse(path);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = maze[r][c];
                if (cell.isWall) {
                    g.setColor(Color.BLACK);
                } else {
                    g.setColor(Color.WHITE);
                }
                g.fillRect(c * cellSize, r * cellSize, cellSize, cellSize);
                g.setColor(Color.GRAY);
                g.drawRect(c * cellSize, r * cellSize, cellSize, cellSize);
            }
        }

        g.setColor(new Color(173, 216, 230)); // light blue for visited
        for (int i = 0; i < Math.min(timerIndex, visitOrder.size()); i++) {
            Cell v = visitOrder.get(i);
            g.fillRect(v.col * cellSize, v.row * cellSize, cellSize, cellSize);
        }

        g.setColor(Color.GREEN); // final path
        for (Cell cell : path) {
            g.fillRect(cell.col * cellSize, cell.row * cellSize, cellSize, cellSize);
        }

        g.setColor(Color.BLUE); // start cell
        g.fillRect(startCell.col * cellSize, startCell.row * cellSize, cellSize, cellSize);

        g.setColor(Color.RED); // end cell
        g.fillRect(endCell.col * cellSize, endCell.row * cellSize, cellSize, cellSize);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (solving) return;

        int c = e.getX() / cellSize;
        int r = e.getY() / cellSize;
        if (r < 0 || r >= rows || c < 0 || c >= cols) return;

        Cell clicked = maze[r][c];
        if (clicked.isWall) return;

        if (SwingUtilities.isLeftMouseButton(e)) {
            startCell = clicked;
        } else if (SwingUtilities.isRightMouseButton(e)) {
            endCell = clicked;
        }
        path.clear();
        visitOrder.clear();
        repaint();
    }

    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}

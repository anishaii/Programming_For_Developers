package Question6;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import java.util.PriorityQueue;

// Vehicle Class: Represents a vehicle with a type and an emergency flag
class Vehicle implements Comparable<Vehicle> {
    String type;          // Type of vehicle, e.g., Car, Ambulance
    boolean isEmergency;  // Flag to indicate if the vehicle is emergency

    public Vehicle(String type, boolean isEmergency) {
        this.type = type;
        this.isEmergency = isEmergency;
    }

    // Text representation includes emoji and type
    @Override
    public String toString() {
        return (isEmergency ? "🚨" : "🚗") + " " + type;
    }

    // Vehicles with emergency status have higher priority in queues
    @Override
    public int compareTo(Vehicle other) {
        // Returns negative if this has higher priority (emergency true)
        return Boolean.compare(other.isEmergency, this.isEmergency);
    }
}

// 🚦 VehicleManager: Manages queues of vehicles waiting at the signal
class VehicleManager {
    // FIFO queue for regular vehicles
    private final Queue<Vehicle> regularQueue = new LinkedList<>();
    // Priority queue for emergency vehicles (emergency vehicles get higher priority)
    private final PriorityQueue<Vehicle> emergencyQueue = new PriorityQueue<>();

    // Add vehicle to appropriate queue based on emergency status
    public synchronized void addVehicle(Vehicle v) {
        if (v.isEmergency) emergencyQueue.add(v);
        else regularQueue.add(v);
    }

    // Retrieve the next vehicle to process, emergency vehicles get precedence
    public synchronized Vehicle getNextVehicle() {
        if (!emergencyQueue.isEmpty()) return emergencyQueue.poll();
        return regularQueue.poll();
    }

    // Return list of all vehicles currently queued for display or other uses
    public synchronized List<Vehicle> getAllVehicles() {
        List<Vehicle> list = new ArrayList<>();
        list.addAll(emergencyQueue);
        list.addAll(regularQueue);
        return list;
    }
}

// SignalController: Controls the traffic signal cycling between green and red lights
// Runs as a separate thread that loops infinitely to simulate signal changes
class SignalController extends Thread {
    private volatile boolean greenLight = true; // Volatile to ensure visibility across threads

    @Override
    public void run() {
        while (true) {
            greenLight = true;                      // Turn green light ON
            System.out.println("🟢 GREEN Light");
            try {
                Thread.sleep(5000);                 // Green light duration: 5 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            greenLight = false;                     // Turn red light ON
            System.out.println("🔴 RED Light");
            try {
                Thread.sleep(3000);                 // Red light duration: 3 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // Method for other threads to check if the signal is green
    public boolean isGreen() {
        return greenLight;
    }
}

//  VehicleProcessor: Continuously processes vehicles when the signal is green
// It runs as a separate thread, polling vehicles from the manager and logging their passage
class VehicleProcessor extends Thread {
    private final VehicleManager manager;       // Vehicle queue manager
    private final SignalController signal;      // Signal controller thread
    private final JTextArea logArea;             // GUI area to log vehicle passing events
    private final Runnable updateQueue;          // Runnable to update vehicle queue display

    // Constructor to initialize references to shared resources
    public VehicleProcessor(VehicleManager manager, SignalController signal,
                            JTextArea logArea, Runnable updateQueue) {
        this.manager = manager;
        this.signal = signal;
        this.logArea = logArea;
        this.updateQueue = updateQueue;
    }

    @Override
    public void run() {
        while (true) {
            // Only process vehicles when signal is green
            if (signal.isGreen()) {
                Vehicle v = manager.getNextVehicle(); // Get next vehicle from queue
                if (v != null) {
                    String msg = "⏩ " + v + " passed the intersection.\n";

                    // Update GUI safely on Swing's Event Dispatch Thread
                    SwingUtilities.invokeLater(() -> {
                        logArea.append(msg);          // Append message to log area
                        updateQueue.run();            // Refresh vehicle queue display
                    });
                }
            }
            try {
                Thread.sleep(1000);                   // Sleep to avoid busy waiting, simulates vehicle processing time
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();  // Restore interrupt status
            }
        }
    }
}

//TrafficSignalSystem GUI: Main application frame with user controls and display areas
public class TrafficSignalSystem extends JFrame {
    private final VehicleManager manager = new VehicleManager();     // Manage vehicle queues
    private final SignalController signalThread = new SignalController(); // Traffic light controller thread
    private final JTextArea queueArea = new JTextArea(10, 30);       // Displays queued vehicles
    private final JTextArea logArea = new JTextArea(10, 30);         // Displays log of vehicles passing

    public TrafficSignalSystem() {
        super("🚦 Traffic Signal Management System");

        // Buttons to add vehicles
        JButton addCarBtn = new JButton("Add Regular Vehicle");
        JButton addEmergencyBtn = new JButton("Add Emergency Vehicle");

        // Add regular vehicle on button click
        addCarBtn.addActionListener(_ -> {
            manager.addVehicle(new Vehicle("Car", false));
            updateQueueDisplay();
        });

        // Add emergency vehicle on button click
        addEmergencyBtn.addActionListener(_ -> {
            manager.addVehicle(new Vehicle("Ambulance", true));
            updateQueueDisplay();
        });

        // Control panel for buttons
        JPanel controlPanel = new JPanel();
        controlPanel.add(addCarBtn);
        controlPanel.add(addEmergencyBtn);

        // Configure text areas as read-only
        queueArea.setEditable(false);
        logArea.setEditable(false);

        // Layout setup: controls at top, queue display center, logs at bottom
        add(controlPanel, BorderLayout.NORTH);
        add(new JScrollPane(queueArea), BorderLayout.CENTER);
        add(new JScrollPane(logArea), BorderLayout.SOUTH);

        // Start the signal light controller thread
        signalThread.start();

        // Start the vehicle processing thread that acts according to signal state
        new VehicleProcessor(manager, signalThread, logArea, this::updateQueueDisplay).start();

        // JFrame setup
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);    // Center window on screen
        setVisible(true);
    }

    // Update the display of vehicles currently queued
    private void updateQueueDisplay() {
        StringBuilder sb = new StringBuilder();
        for (Vehicle v : manager.getAllVehicles()) {
            sb.append(v).append("\n");
        }
        queueArea.setText(sb.toString());
    }

    // Entry point - launch GUI on Swing event dispatch thread
    public static void main(String[] args) {
        SwingUtilities.invokeLater(TrafficSignalSystem::new);
    }
}

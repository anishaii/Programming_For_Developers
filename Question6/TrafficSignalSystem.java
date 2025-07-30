package Question6;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import java.util.PriorityQueue;

// 🚗 Vehicle Class: Represents a vehicle with emergency flag
class Vehicle implements Comparable<Vehicle> {
    String type;
    boolean isEmergency;

    public Vehicle(String type, boolean isEmergency) {
        this.type = type;
        this.isEmergency = isEmergency;
    }

    @Override
    public String toString() {
        return (isEmergency ? "🚨" : "🚗") + " " + type;
    }

    // Emergency vehicles have higher priority
    @Override
    public int compareTo(Vehicle other) {
        return Boolean.compare(other.isEmergency, this.isEmergency);
    }
}

// 🚦 VehicleManager: Manages vehicle queues (FIFO + Priority)
class VehicleManager {
    private final Queue<Vehicle> regularQueue = new LinkedList<>();
    private final PriorityQueue<Vehicle> emergencyQueue = new PriorityQueue<>();

    // Add vehicle to appropriate queue
    public synchronized void addVehicle(Vehicle v) {
        if (v.isEmergency) emergencyQueue.add(v);
        else regularQueue.add(v);
    }

    // Get next vehicle (Emergency first)
    public synchronized Vehicle getNextVehicle() {
        if (!emergencyQueue.isEmpty()) return emergencyQueue.poll();
        return regularQueue.poll();
    }

    // Return all vehicles (for display)
    public synchronized List<Vehicle> getAllVehicles() {
        List<Vehicle> list = new ArrayList<>();
        list.addAll(emergencyQueue);
        list.addAll(regularQueue);
        return list;
    }
}

// 🔁 SignalController: Controls red/green signal using a thread
class SignalController extends Thread {
    private volatile boolean greenLight = true;

    @Override
    public void run() {
        while (true) {
            greenLight = true;
            System.out.println("🟢 GREEN Light");
            try { Thread.sleep(5000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            greenLight = false;
            System.out.println("🔴 RED Light");
            try { Thread.sleep(3000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }

    public boolean isGreen() {
        return greenLight;
    }
}

// 🧵 VehicleProcessor: Processes vehicles when signal is green
class VehicleProcessor extends Thread {
    private final VehicleManager manager;
    private final SignalController signal;
    private final JTextArea logArea;
    private final Runnable updateQueue;

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
            if (signal.isGreen()) {
                Vehicle v = manager.getNextVehicle();
                if (v != null) {
                    String msg = "⏩ " + v + " passed the intersection.\n";
                    SwingUtilities.invokeLater(() -> {
                        logArea.append(msg);
                        updateQueue.run();
                    });
                }
            }
            try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }
}

// 🎮 TrafficSignalSystem GUI: Main GUI and simulation logic
public class TrafficSignalSystem extends JFrame {
    private final VehicleManager manager = new VehicleManager();
    private final SignalController signalThread = new SignalController();
    private final JTextArea queueArea = new JTextArea(10, 30);
    private final JTextArea logArea = new JTextArea(10, 30);

    public TrafficSignalSystem() {
        super("🚦 Traffic Signal Management System");

        // Buttons to control traffic
        JButton addCarBtn = new JButton("Add Regular Vehicle");
        JButton addEmergencyBtn = new JButton("Add Emergency Vehicle");

        // Add vehicle actions
        addCarBtn.addActionListener(_ -> {
            manager.addVehicle(new Vehicle("Car", false));
            updateQueueDisplay();
        });

        addEmergencyBtn.addActionListener(_ -> {
            manager.addVehicle(new Vehicle("Ambulance", true));
            updateQueueDisplay();
        });

        // Setup control panel
        JPanel controlPanel = new JPanel();
        controlPanel.add(addCarBtn);
        controlPanel.add(addEmergencyBtn);

        // Text areas
        queueArea.setEditable(false);
        logArea.setEditable(false);

        // Layout
        add(controlPanel, BorderLayout.NORTH);
        add(new JScrollPane(queueArea), BorderLayout.CENTER);
        add(new JScrollPane(logArea), BorderLayout.SOUTH);

        // Start threads for signal and vehicle processing
        signalThread.start();
        new VehicleProcessor(manager, signalThread, logArea, this::updateQueueDisplay).start();

        // Window setup
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Display current queue in GUI
    private void updateQueueDisplay() {
        StringBuilder sb = new StringBuilder();
        for (Vehicle v : manager.getAllVehicles()) {
            sb.append(v).append("\n");
        }
        queueArea.setText(sb.toString());
    }

    // Main method
    public static void main(String[] args) {
        SwingUtilities.invokeLater(TrafficSignalSystem::new);
    }
}

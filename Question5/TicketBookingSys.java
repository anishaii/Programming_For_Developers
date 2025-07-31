package Question5;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.concurrent.*;

// Represents a booking request from a user for a specific seat
class BookingRequest {
    String userId;      // Identifier of the user making the booking
    int seatNumber;     // Seat number requested

    public BookingRequest(String userId, int seatNumber) {
        this.userId = userId;
        this.seatNumber = seatNumber;
    }
}

// SeatManager handles seat booking with concurrency control using two locking strategies:
// 1. Pessimistic Locking: Synchronizes on a shared lock to ensure exclusive access.
// 2. Optimistic Locking: Checks state first, then synchronizes only if likely to succeed.
// The seats map stores seat availability (true = booked, false = available).
class SeatManager {
    Map<Integer, Boolean> seats = new ConcurrentHashMap<>();
    final Object lock = new Object(); // Lock object used for synchronization

    // Initialize all seats as available (false)
    public SeatManager(int totalSeats) {
        for (int i = 1; i <= totalSeats; i++) seats.put(i, false);
    }

    // Pessimistic locking: lock before checking and updating seat status.
    // Guarantees no race conditions but reduces concurrency.
    public boolean bookSeatPessimistic(int seatNumber) {
        synchronized (lock) {
            if (!seats.get(seatNumber)) {  // Seat is available
                seats.put(seatNumber, true); // Book seat
                return true;                 // Success
            }
            return false;                   // Already booked
        }
    }

    // Optimistic locking approach:
    // 1. Check if seat is available without locking.
    // 2. Simulate a chance of conflict (20%).
    // 3. If no conflict, synchronize and verify availability again.
    // This approach increases concurrency but can fail more often.
    public boolean bookSeatOptimistic(int seatNumber) {
        if (!seats.get(seatNumber)) { // Initial check without lock
            if (Math.random() > 0.2) { // Simulated conflict probability
                synchronized (lock) {  // Lock to ensure atomic update
                    if (!seats.get(seatNumber)) {
                        seats.put(seatNumber, true);
                        return true;
                    }
                }
            }
        }
        return false; // Booking failed due to conflict or already booked
    }

    // Getter for current seat booking status map
    public Map<Integer, Boolean> getSeats() {
        return seats;
    }
}

// BookingProcessor runs in a separate thread to process booking requests
// It dequeues requests, attempts to book seats using chosen locking strategy,
// and updates the GUI with results asynchronously using SwingUtilities.
class BookingProcessor implements Runnable {
    BlockingQueue<BookingRequest> queue; // Queue of booking requests to process
    SeatManager manager;                 // SeatManager to book seats
    boolean useOptimistic;               // Flag to choose locking strategy
    JTextArea logArea;                  // GUI component to log booking results
    Runnable updateDisplay;             // Runnable to refresh seat display in GUI

    public BookingProcessor(
        BlockingQueue<BookingRequest> queue,
        SeatManager manager,
        boolean useOptimistic,
        JTextArea logArea,
        Runnable updateDisplay
    ) {
        this.queue = queue;
        this.manager = manager;
        this.useOptimistic = useOptimistic;
        this.logArea = logArea;
        this.updateDisplay = updateDisplay;
    }

    public void run() {
        // Process requests until queue is empty
        while (!queue.isEmpty()) {
            try {
                BookingRequest request = queue.take(); // Take request from queue
                
                // Attempt booking using the selected locking method
                boolean success = useOptimistic
                    ? manager.bookSeatOptimistic(request.seatNumber)
                    : manager.bookSeatPessimistic(request.seatNumber);

                // Prepare log message about booking result
                String msg = "User " + request.userId + " tried Seat " + request.seatNumber +
                    " → " + (success ? "✅ Booked" : "❌ Failed") + "\n";

                // Update GUI components on Event Dispatch Thread (EDT)
                SwingUtilities.invokeLater(() -> {
                    logArea.append(msg);       // Append booking result to log
                    updateDisplay.run();       // Refresh seat availability display
                });

                Thread.sleep(200); // Simulate processing delay for realism
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Proper thread interruption handling
            }
        }
    }
}

// Main GUI class for the Online Ticket Booking System
// Features:
// - Display seat availability and booking logs
// - Buttons to simulate booking requests, process bookings, and toggle locking mode
// - Uses concurrency for booking request processing with UI updates on EDT
public class TicketBookingSys extends JFrame {
    SeatManager seatManager = new SeatManager(40);              // Manage 40 seats
    BlockingQueue<BookingRequest> bookingQueue = new LinkedBlockingQueue<>();
    boolean useOptimisticLocking = true;                        // Default locking mode

    JTextArea statusArea = new JTextArea(20, 30);               // Show seat availability
    JTextArea logArea = new JTextArea(10, 30);                  // Show booking logs

    public TicketBookingSys() {
        super("🎟️ Online Ticket Booking System");

        statusArea.setEditable(false); // User cannot edit seat status display
        logArea.setEditable(false);    // User cannot edit log display

        // Button to generate 10 random booking requests into the queue
        JButton simulateBtn = new JButton("Simulate Bookings");
        simulateBtn.addActionListener(_ -> {
            for (int i = 1; i <= 10; i++) {
                int seat = (int)(Math.random() * 40) + 1; // Random seat 1-40
                bookingQueue.add(new BookingRequest("User" + i, seat));
            }
        });

        // Button to start processing the booking requests asynchronously
        JButton processBtn = new JButton("Process Bookings");
        processBtn.addActionListener(_ -> {
            new Thread(new BookingProcessor(
                bookingQueue,
                seatManager,
                useOptimisticLocking,
                logArea,
                this::refreshSeatDisplay
            )).start();
        });

        // Button to toggle between optimistic and pessimistic locking modes
        JButton toggleBtn = new JButton("Toggle Locking");
        JLabel lockLabel = new JLabel("🔒 Mode: Optimistic");
        toggleBtn.addActionListener(_ -> {
            useOptimisticLocking = !useOptimisticLocking;
            lockLabel.setText("🔒 Mode: " +
                (useOptimisticLocking ? "Optimistic" : "Pessimistic"));
        });

        // Panel to hold control buttons and lock mode label
        JPanel controlPanel = new JPanel();
        controlPanel.add(simulateBtn);
        controlPanel.add(processBtn);
        controlPanel.add(toggleBtn);
        controlPanel.add(lockLabel);

        // Panel to hold status and log text areas in a grid layout
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.add(new JScrollPane(statusArea));
        textPanel.add(new JScrollPane(logArea));

        // Add panels to JFrame layout
        add(controlPanel, BorderLayout.NORTH);
        add(textPanel, BorderLayout.CENTER);

        refreshSeatDisplay(); // Initialize seat display text

        // Set window properties
        setSize(500, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center window on screen
        setVisible(true);
    }

    // Refresh seat availability display based on current seat map
    void refreshSeatDisplay() {
        StringBuilder sb = new StringBuilder();
        seatManager.getSeats().forEach((k, v) ->
            sb.append("Seat ").append(k).append(": ")
              .append(v ? "Booked ✅" : "Available 🟢").append("\n"));
        statusArea.setText(sb.toString());
    }

    // Main method to launch the GUI on the Swing event dispatch thread
    public static void main(String[] args) {
        SwingUtilities.invokeLater(TicketBookingSys::new);
    }
}

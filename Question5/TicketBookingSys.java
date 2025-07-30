package Question5;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.concurrent.*;

// Booking Request Object
class BookingRequest {
    String userId;
    int seatNumber;

    public BookingRequest(String userId, int seatNumber) {
        this.userId = userId;
        this.seatNumber = seatNumber;
    }
}

// Seat Manager with Locking Strategies
class SeatManager {
    Map<Integer, Boolean> seats = new ConcurrentHashMap<>();
    final Object lock = new Object();

    public SeatManager(int totalSeats) {
        for (int i = 1; i <= totalSeats; i++) seats.put(i, false);
    }

    public boolean bookSeatPessimistic(int seatNumber) {
        synchronized (lock) {
            if (!seats.get(seatNumber)) {
                seats.put(seatNumber, true);
                return true;
            }
            return false;
        }
    }

    public boolean bookSeatOptimistic(int seatNumber) {
        if (!seats.get(seatNumber)) {
            if (Math.random() > 0.2) { // simulate conflict
                synchronized (lock) {
                    if (!seats.get(seatNumber)) {
                        seats.put(seatNumber, true);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Map<Integer, Boolean> getSeats() {
        return seats;
    }
}

// Booking Processor Thread
class BookingProcessor implements Runnable {
    BlockingQueue<BookingRequest> queue;
    SeatManager manager;
    boolean useOptimistic;
    JTextArea logArea;
    Runnable updateDisplay;

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
        while (!queue.isEmpty()) {
            try {
                BookingRequest request = queue.take();
                boolean success = useOptimistic
                    ? manager.bookSeatOptimistic(request.seatNumber)
                    : manager.bookSeatPessimistic(request.seatNumber);

                String msg = "User " + request.userId + " tried Seat " + request.seatNumber +
                    " → " + (success ? "✅ Booked" : "❌ Failed") + "\n";

                SwingUtilities.invokeLater(() -> {
                    logArea.append(msg);
                    updateDisplay.run();
                });

                Thread.sleep(200); // simulate delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

// Main GUI Class (Renamed to match your file)
public class TicketBookingSys extends JFrame {
    SeatManager seatManager = new SeatManager(40);
    BlockingQueue<BookingRequest> bookingQueue = new LinkedBlockingQueue<>();
    boolean useOptimisticLocking = true;

    JTextArea statusArea = new JTextArea(20, 30);
    JTextArea logArea = new JTextArea(10, 30);

    public TicketBookingSys() {
        super("🎟️ Online Ticket Booking System");

        statusArea.setEditable(false);
        logArea.setEditable(false);

        JButton simulateBtn = new JButton("Simulate Bookings");
        JButton processBtn = new JButton("Process Bookings");
        JButton toggleBtn = new JButton("Toggle Locking");
        JLabel lockLabel = new JLabel("🔒 Mode: Optimistic");

        simulateBtn.addActionListener(_ -> {
            for (int i = 1; i <= 10; i++) {
                int seat = (int)(Math.random() * 40) + 1;
                bookingQueue.add(new BookingRequest("User" + i, seat));
            }
        });

        processBtn.addActionListener(_ -> {
            new Thread(new BookingProcessor(
                bookingQueue,
                seatManager,
                useOptimisticLocking,
                logArea,
                this::refreshSeatDisplay
            )).start();
        });

        toggleBtn.addActionListener(_ -> {
            useOptimisticLocking = !useOptimisticLocking;
            lockLabel.setText("🔒 Mode: " +
                (useOptimisticLocking ? "Optimistic" : "Pessimistic"));
        });

        JPanel controlPanel = new JPanel();
        controlPanel.add(simulateBtn);
        controlPanel.add(processBtn);
        controlPanel.add(toggleBtn);
        controlPanel.add(lockLabel);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.add(new JScrollPane(statusArea));
        textPanel.add(new JScrollPane(logArea));

        add(controlPanel, BorderLayout.NORTH);
        add(textPanel, BorderLayout.CENTER);

        refreshSeatDisplay();

        setSize(500, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    void refreshSeatDisplay() {
        StringBuilder sb = new StringBuilder();
        seatManager.getSeats().forEach((k, v) ->
            sb.append("Seat ").append(k).append(": ")
              .append(v ? "Booked ✅" : "Available 🟢").append("\n"));
        statusArea.setText(sb.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TicketBookingSys::new);
    }
}

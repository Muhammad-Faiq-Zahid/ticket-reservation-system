import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class Seat {
    String category;
    String customerName;
    String seatNumber;
    boolean isBooked;

    public Seat(String category, String customerName, String seatNumber) {
        this.category = category;
        this.customerName = customerName;
        this.seatNumber = seatNumber;
        this.isBooked = true;
    }
}

class SeatCategoryNode {
    String category;
    double price;
    int availableSeats;
    SeatCategoryNode left, right;

    public SeatCategoryNode(String category, double price, int availableSeats) {
        this.category = category;
        this.price = price;
        this.availableSeats = availableSeats;
    }
}

class SeatBST {
    SeatCategoryNode root;

    public SeatCategoryNode insert(SeatCategoryNode root, String category, double price, int seats) {
        if (root == null) return new SeatCategoryNode(category, price, seats);
        if (category.compareTo(root.category) < 0)
            root.left = insert(root.left, category, price, seats);
        else if (category.compareTo(root.category) > 0)
            root.right = insert(root.right, category, price, seats);
        return root;
    }

    public SeatCategoryNode search(SeatCategoryNode root, String category) {
        if (root == null || root.category.equals(category)) return root;
        if (category.compareTo(root.category) < 0)
            return search(root.left, category);
        return search(root.right, category);
    }
}

class TicketSystem {
    static SeatBST seatTree = new SeatBST();
    static HashMap<String, Seat> seatMap = new HashMap<>();
    static Stack<String> undoBookingStack = new Stack<>();
    static Stack<String> undoCancellationStack = new Stack<>();
    static JTextArea outputArea;
    static JTextField categoryField, seatNumberField, customerNameField;

    public static void bookSeat(String category, String seatNumber, String customerName) {
        SeatCategoryNode catNode = seatTree.search(seatTree.root, category);
        if (catNode == null || catNode.availableSeats == 0) {
            outputArea.append("Category not found or no available seats.\n");
            return;
        }
        if (seatMap.containsKey(seatNumber)) {
            outputArea.append("Seat already booked.\n");
            return;
        }
        Seat seat = new Seat(category, customerName, seatNumber);
        seatMap.put(seatNumber, seat);
        undoBookingStack.push(seatNumber);
        catNode.availableSeats--;
        outputArea.append("Seat Booked Successfully\n");
        outputArea.append("Customer: " + customerName + "\n");
        outputArea.append("Seat Number: " + seatNumber + "\n");
        outputArea.append("Category: " + category + "\n");

    }

    public static void cancelSeat(String seatNumber) {
        if (!seatMap.containsKey(seatNumber)) {
            outputArea.append("Seat not booked.\n");
            return;
        }
        Seat seat = seatMap.remove(seatNumber);
        SeatCategoryNode catNode = seatTree.search(seatTree.root, seat.category);
        if (catNode != null) catNode.availableSeats++;
        undoCancellationStack.push(seatNumber);
        outputArea.append("Seat Cancelled\n");
        outputArea.append("Customer: " + seat.customerName + "\n");
        outputArea.append("Seat Number: " + seatNumber + "\n");
        outputArea.append("Category: " + seat.category + "\n");

    }

    public static void undoBooking() {
        if (undoBookingStack.isEmpty()) {
            outputArea.append("No booking to undo.\n");
            return;
        }
        String seatNumber = undoBookingStack.pop();
        Seat seat = seatMap.remove(seatNumber);
        SeatCategoryNode catNode = seatTree.search(seatTree.root, seat.category);
        if (catNode != null) catNode.availableSeats++;
        outputArea.append("Undo Booking Performed\n");
        outputArea.append("Seat Number: " + seatNumber + "\n");

    }

    public static void undoCancellation() {
        if (undoCancellationStack.isEmpty()) {
            outputArea.append("No cancellation to undo.\n");
            return;
        }
        String seatNumber = undoCancellationStack.pop();
        bookSeat("Economy", seatNumber, "RestoredUser");
        outputArea.append("Undo Cancellation Performed\n");
        outputArea.append("Seat Number: " + seatNumber + "\n");

    }

    public static void displaySeatInfo(String seatNumber) {
        if (!seatMap.containsKey(seatNumber)) {
            outputArea.append("Seat not booked.\n");
            return;
        }
        Seat seat = seatMap.get(seatNumber);
        outputArea.append("=== Seat Information ===\n");
        outputArea.append("Seat Number: " + seat.seatNumber + "\n");
        outputArea.append("Category: " + seat.category + "\n");
        outputArea.append("Customer Name: " + seat.customerName + "\n");
        outputArea.append("Status: " + (seat.isBooked ? "Booked" : "Available") + "\n");

    }

    public static void main(String[] args) {
        seatTree.root = seatTree.insert(seatTree.root, "VIP", 5000, 5);
        seatTree.root = seatTree.insert(seatTree.root, "Economy", 1000, 10);
        seatTree.root = seatTree.insert(seatTree.root, "Premium", 3000, 7);

        JFrame frame = new JFrame("Ticket Reservation System");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(3, 2));
        categoryField = new JTextField();
        seatNumberField = new JTextField();
        customerNameField = new JTextField();

        inputPanel.add(new JLabel("Category:"));
        inputPanel.add(categoryField);
        inputPanel.add(new JLabel("Seat Number:"));
        inputPanel.add(seatNumberField);
        inputPanel.add(new JLabel("Customer Name:"));
        inputPanel.add(customerNameField);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 5));
        JButton bookBtn = new JButton("Book");
        JButton cancelBtn = new JButton("Cancel");
        JButton undoBookBtn = new JButton("Undo Book");
        JButton undoCancelBtn = new JButton("Undo Cancel");
        JButton infoBtn = new JButton("Show Info");


        bookBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String category = categoryField.getText().trim();
                String seatNumber = seatNumberField.getText().trim();
                String customerName = customerNameField.getText().trim();

                if (category.isEmpty() || seatNumber.isEmpty() || customerName.isEmpty()) {
                    outputArea.append("Error: All fields are required!\n");
                    return;
                }

                bookSeat(category, seatNumber, customerName);
                clearInputFields();
            }
        });

        cancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String seatNumber = seatNumberField.getText().trim();

                if (seatNumber.isEmpty()) {
                    outputArea.append("Error: Seat number is required!\n");
                    return;
                }

                cancelSeat(seatNumber);
                clearInputFields();
            }
        });

        undoBookBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                undoBooking();
            }
        });

        undoCancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                undoCancellation();
            }
        });

        infoBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String seatNumber = seatNumberField.getText().trim();

                if (seatNumber.isEmpty()) {
                    outputArea.append("Error: Seat number is required!\n");
                    return;
                }

                displaySeatInfo(seatNumber);
            }
        });

        buttonPanel.add(bookBtn);
        buttonPanel.add(cancelBtn);
        buttonPanel.add(undoBookBtn);
        buttonPanel.add(undoCancelBtn);
        buttonPanel.add(infoBtn);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(buttonPanel, BorderLayout.CENTER);
        frame.add(scrollPane, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private static void clearInputFields() {
        categoryField.setText("");
        seatNumberField.setText("");
        customerNameField.setText("");
    }
}
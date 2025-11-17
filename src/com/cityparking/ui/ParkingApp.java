package com.cityparking.ui;

import com.cityparking.model.ParkingLot;
import com.cityparking.model.ParkingSlot;
import com.cityparking.model.ParkingTicket;
import com.cityparking.model.RateCard;
import com.cityparking.model.VehicleInfo;
import com.cityparking.service.ParkingService;
import com.cityparking.util.SlotSnapshotWriter;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Swing UI that mimics a smart parking kiosk used in large cities.
 */
public class ParkingApp extends JFrame {
    private final ParkingService parkingService;
    private final JLabel availabilityLabel = new JLabel();
    private final DefaultTableModel slotTableModel;
    private final JTable slotTable;
    private final JTextField plateField = new JTextField(10);
    private final JTextField ownerField = new JTextField(10);
    private final JTextField phoneField = new JTextField(10);
    private final JTextField ticketField = new JTextField(8);
    private final JTextField searchPlateField = new JTextField(10);
    private final JTextField estimateHoursField = new JTextField("2", 4);
    private final JTextField manualSlotField = new JTextField(8);
    private final JTextArea receiptArea = new JTextArea(8, 28);
    private final JTextArea analyticsArea = new JTextArea(10, 28);

    public ParkingApp(ParkingService parkingService) {
        super("City Smart Parking Manager");
        this.parkingService = parkingService;
        this.slotTableModel = new DefaultTableModel(new Object[]{"Slot", "Floor", "Distance", "Status", "Vehicle"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.slotTable = new JTable(slotTableModel);
        buildUi();
        refreshView();
    }

    private void buildUi() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(Color.WHITE);

        availabilityLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        availabilityLabel.setForeground(new Color(33, 150, 83));
        add(availabilityLabel, BorderLayout.NORTH);

        slotTable.setRowHeight(22);
        slotTable.setPreferredScrollableViewportSize(new Dimension(480, 250));
        slotTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = slotTable.getSelectedRow();
                if (row >= 0) {
                    showDetailsForSlot(row);
                }
            }
        });
        add(new JScrollPane(slotTable), BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Entry & Exit Console"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Vehicle No."), gbc);
        gbc.gridx = 1;
        formPanel.add(plateField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Owner Name"), gbc);
        gbc.gridx = 1;
        formPanel.add(ownerField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Phone"), gbc);
        gbc.gridx = 1;
        formPanel.add(phoneField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Assign Slot ID"), gbc);
        gbc.gridx = 1;
        formPanel.add(manualSlotField, gbc);

        JButton assignButton = new JButton("Auto Assign Nearest Slot");
        assignButton.addActionListener(e -> autoAssignVehicle());
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        formPanel.add(assignButton, gbc);

        JButton manualAssignButton = new JButton("Park In Slot ID");
        manualAssignButton.addActionListener(e -> assignVehicleToSlot());
        gbc.gridy++;
        formPanel.add(manualAssignButton, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Ticket ID"), gbc);
        gbc.gridx = 1;
        formPanel.add(ticketField, gbc);

        JButton releaseButton = new JButton("Release Slot & Bill");
        releaseButton.addActionListener(e -> releaseVehicle());
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        formPanel.add(releaseButton, gbc);

        gbc.gridwidth = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Search Plate"), gbc);
        gbc.gridx = 1;
        formPanel.add(searchPlateField, gbc);

        JButton searchButton = new JButton("Locate Car");
        searchButton.addActionListener(e -> searchVehicle());
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        formPanel.add(searchButton, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Hours (est.)"), gbc);
        gbc.gridx = 1;
        formPanel.add(estimateHoursField, gbc);

        JButton estimateButton = new JButton("Estimate Charges");
        estimateButton.addActionListener(e -> showEstimate());
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        formPanel.add(estimateButton, gbc);

        receiptArea.setEditable(false);
        receiptArea.setBorder(BorderFactory.createTitledBorder("Digital Receipt"));
        analyticsArea.setEditable(false);
        analyticsArea.setBorder(BorderFactory.createTitledBorder("Control Room Insights"));
        JPanel eastPanel = new JPanel(new BorderLayout(4, 4));
        eastPanel.add(new JScrollPane(receiptArea), BorderLayout.CENTER);
        eastPanel.add(new JScrollPane(analyticsArea), BorderLayout.SOUTH);
        add(formPanel, BorderLayout.WEST);
        add(eastPanel, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
    }

    private void autoAssignVehicle() {
        VehicleInfo vehicle = buildVehicleFromForm();
        if (vehicle == null) {
            return;
        }
        Optional<ParkingTicket> ticket = parkingService.assignSlot(vehicle);
        if (ticket.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No slot available. Direct vehicle to overflow lot.", "Parking Full", JOptionPane.ERROR_MESSAGE);
            return;
        }
        showEntryReceipt(ticket.get(), "ENTRY CONFIRMATION");
        clearVehicleForm();
        refreshView();
    }

    private void assignVehicleToSlot() {
        String slotId = manualSlotField.getText().trim();
        if (slotId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter the slot id where you want to park the vehicle.", "Missing slot id", JOptionPane.WARNING_MESSAGE);
            return;
        }
        VehicleInfo vehicle = buildVehicleFromForm();
        if (vehicle == null) {
            return;
        }
        Optional<ParkingTicket> ticket = parkingService.assignSlotTo(slotId, vehicle);
        if (ticket.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Slot not available or does not exist. Please verify.", "Unable to park", JOptionPane.ERROR_MESSAGE);
            return;
        }
        ParkingTicket issued = ticket.get();
        manualSlotField.setText("");
        showEntryReceipt(issued, "MANUAL ENTRY CONFIRMATION");
        clearVehicleForm();
        refreshView();
    }

    private void releaseVehicle() {
        String ticketId = ticketField.getText().trim();
        if (ticketId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter ticket id printed on the entry receipt.", "Missing ticket", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Optional<ParkingTicket> closed = parkingService.closeTicket(ticketId);
        if (closed.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ticket not found. Please verify ID.", "Invalid ticket", JOptionPane.ERROR_MESSAGE);
            return;
        }
        ParkingTicket ticket = closed.get();
        double hoursParked = ticket.getDuration().toMinutes() / 60.0;
        receiptArea.setText(
                """
                        EXIT RECEIPT
                        Ticket: %s
                        Slot: %s
                        Vehicle: %s
                        Parked for: %d mins (%.1f hrs)
                        Amount Due: INR %.2f
                        Thank you. Drive safe!
                        """
                        .formatted(
                                ticket.getTicketId(),
                                ticket.getSlot(),
                                ticket.getVehicle().getPlateNumber(),
                                ticket.getDuration().toMinutes(),
                                hoursParked,
                                ticket.getAmount()));
        ticketField.setText("");
        refreshView();
    }

    private VehicleInfo buildVehicleFromForm() {
        String plate = plateField.getText().trim();
        String owner = ownerField.getText().trim();
        String phone = phoneField.getText().trim();
        if (plate.isEmpty() || owner.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill vehicle number, owner name and phone.", "Missing data", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return new VehicleInfo(plate, owner, phone);
    }

    private void clearVehicleForm() {
        plateField.setText("");
        ownerField.setText("");
        phoneField.setText("");
    }

    private void showEntryReceipt(ParkingTicket issued, String heading) {
        receiptArea.setText(
                """
                        %s
                        Ticket: %s
                        Slot: %s
                        Vehicle: %s
                        Owner: %s
                        Phone: %s
                        Check-In: %s
                        """
                        .formatted(
                                heading,
                                issued.getTicketId(),
                                issued.getSlot(),
                                issued.getVehicle().getPlateNumber(),
                                issued.getVehicle().getOwnerName(),
                                issued.getVehicle().getPhoneNumber(),
                                issued.getCheckInTime()));
    }

    private void searchVehicle() {
        String plate = searchPlateField.getText().trim();
        if (plate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter the registration number to locate your vehicle.", "Missing plate", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Optional<ParkingTicket> ticket = parkingService.findActiveTicketByPlate(plate);
        if (ticket.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vehicle not found in the active lot.", "Not found", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        ParkingTicket match = ticket.get();
        highlightSlot(match.getSlot().getSlotId());
        receiptArea.setText(
                """
                        VEHICLE LOCATED
                        Slot: %s
                        Ticket: %s
                        Vehicle: %s
                        Checked-In: %s
                        """
                        .formatted(
                                match.getSlot(),
                                match.getTicketId(),
                                match.getVehicle().getPlateNumber(),
                                match.getCheckInTime()));
    }

    private void showEstimate() {
        String hoursText = estimateHoursField.getText().trim();
        try {
            double hours = Double.parseDouble(hoursText);
            if (hours <= 0) {
                throw new NumberFormatException("Negative hours");
            }
            double amount = parkingService.estimateFee(hours);
            JOptionPane.showMessageDialog(this,
                    "Estimated parking fee for " + hours + " hour(s): INR " + String.format("%.2f", amount) +
                            " (rounded up to the next hour)",
                    "Tariff estimator",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive number of hours.", "Invalid input", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void highlightSlot(String slotId) {
        for (int i = 0; i < slotTableModel.getRowCount(); i++) {
            Object value = slotTableModel.getValueAt(i, 0);
            if (slotId.equals(value)) {
                slotTable.setRowSelectionInterval(i, i);
                slotTable.scrollRectToVisible(slotTable.getCellRect(i, 0, true));
                break;
            }
        }
    }

    private void showDetailsForSlot(int row) {
        Object value = slotTableModel.getValueAt(row, 0);
        if (value == null) {
            return;
        }
        String slotId = value.toString();
        Optional<ParkingTicket> ticket = parkingService.findActiveTicketBySlot(slotId);
        if (ticket.isEmpty()) {
            receiptArea.setText(
                    """
                            SLOT STATUS
                            Slot: %s
                            Currently free and ready for assignment.
                            """
                            .formatted(slotId));
            return;
        }
        ParkingTicket details = ticket.get();
        receiptArea.setText(
                """
                        ACTIVE VEHICLE DETAILS
                        Slot: %s
                        Ticket ID: %s
                        Vehicle: %s
                        Owner: %s
                        Phone: %s
                        Checked-In: %s
                        """
                        .formatted(
                                slotId,
                                details.getTicketId(),
                                details.getVehicle().getPlateNumber(),
                                details.getVehicle().getOwnerName(),
                                details.getVehicle().getPhoneNumber(),
                                details.getCheckInTime()));
    }

    private void refreshView() {
        slotTableModel.setRowCount(0);
        parkingService.getParkingLot().getSlots().forEach(slot -> {
            slotTableModel.addRow(new Object[]{
                    slot.getSlotId(),
                    slot.getFloor(),
                    slot.getDistance() + " m",
                    slot.isOccupied() ? "Occupied" : "Free",
                    slot.isOccupied() ? slot.getCurrentVehicle().getPlateNumber() : "-"
            });
        });
        availabilityLabel.setText(
                "Available Slots: " + parkingService.getParkingLot().getAvailableCount() +
                        " | Occupied: " + parkingService.getParkingLot().getOccupiedCount() +
                        " | Total: " + parkingService.getParkingLot().getTotalSlots());
        updateAnalytics();
        SlotSnapshotWriter.write(parkingService.getParkingLot().getSlots(), Path.of("web", "data", "slots.json"));
    }

    private void updateAnalytics() {
        StringBuilder sb = new StringBuilder();
        sb.append("Revenue Collected Today: INR ").append(String.format("%.2f", parkingService.getTotalRevenue())).append("\n");
        sb.append("Active Vehicles: ").append(parkingService.getActiveTickets().size()).append("\n");

        sb.append("\nFloor Utilization:\n");
        Map<Integer, long[]> floorStats = new java.util.TreeMap<>();
        parkingService.getParkingLot().getSlots().forEach(slot -> {
            long[] stats = floorStats.computeIfAbsent(slot.getFloor(), f -> new long[2]);
            stats[0]++;
            if (slot.isOccupied()) {
                stats[1]++;
            }
        });
        floorStats.forEach((floor, stats) -> {
            long total = stats[0];
            long occupied = stats[1];
            long free = total - occupied;
            sb.append("Floor ").append(floor).append(": ").append(occupied).append("/").append(total).append(" occupied (").append(free).append(" free)\n");
        });

        sb.append("\nRecent Exit History:\n");
        List<ParkingTicket> recent = parkingService.getRecentHistory();
        if (recent.isEmpty()) {
            sb.append("No vehicles exited yet.\n");
        } else {
            recent.forEach(ticket -> sb.append(ticket.getVehicle().getPlateNumber())
                    .append(" -> INR ")
                    .append(String.format("%.0f", ticket.getAmount()))
                    .append(" (")
                    .append(ticket.getSlot().getSlotId())
                    .append(")\n"));
        }

        analyticsArea.setText(sb.toString());
    }

    private static ParkingService bootService() {
        List<ParkingSlot> slots = new ArrayList<>();
        int slotCounter = 1;
        for (int floor = 0; floor < 3; floor++) {
            for (int bay = 1; bay <= 12; bay++) {
                int distance = floor * 50 + bay * 4;
                String slotId = "F" + floor + "-S" + String.format("%02d", bay);
                slots.add(new ParkingSlot(slotId, floor, distance));
                slotCounter++;
            }
        }
        ParkingLot lot = new ParkingLot("Downtown Business District", slots);
        RateCard rateCard = new RateCard(60.0, 40.0, 600.0);
        return new ParkingService(lot, rateCard);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ParkingApp(bootService()).setVisible(true));
    }
}


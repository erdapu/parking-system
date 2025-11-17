package com.cityparking.service;

import com.cityparking.model.ParkingLot;
import com.cityparking.model.ParkingSlot;
import com.cityparking.model.ParkingTicket;
import com.cityparking.model.RateCard;
import com.cityparking.model.VehicleInfo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.TreeMap;

/**
 * Core service that knows how to auto assign nearest slots and compute fees.
 */
public class ParkingService {
    private final ParkingLot parkingLot;
    private final PriorityQueue<ParkingSlot> availableSlots;
    private final Map<String, ParkingTicket> activeTickets = new HashMap<>();
    private final Map<String, ParkingTicket> ticketsByPlate = new HashMap<>();
    private final Deque<ParkingTicket> recentHistory = new ArrayDeque<>();
    private final RateCard rateCard;
    private double totalRevenue = 0.0;

    public ParkingService(ParkingLot parkingLot, RateCard rateCard) {
        this.parkingLot = parkingLot;
        this.rateCard = rateCard;
        this.availableSlots = new PriorityQueue<>(parkingLot.getSlots());
    }

    public Optional<ParkingTicket> assignSlot(VehicleInfo vehicle) {
        ParkingSlot slot = availableSlots.poll();
        if (slot == null || slot.isOccupied()) {
            return Optional.empty();
        }
        slot.occupy(vehicle, LocalDateTime.now());
        ParkingTicket ticket = new ParkingTicket(slot, vehicle);
        activeTickets.put(ticket.getTicketId(), ticket);
        ticketsByPlate.put(vehicle.getPlateNumber(), ticket);
        return Optional.of(ticket);
    }

    public Optional<ParkingTicket> closeTicket(String ticketId) {
        ParkingTicket ticket = activeTickets.remove(ticketId);
        if (ticket == null) {
            return Optional.empty();
        }
        ParkingSlot slot = ticket.getSlot();
        Duration duration = Duration.between(ticket.getCheckInTime(), LocalDateTime.now());
        long hours = Math.max(1, (long) Math.ceil(duration.toMinutes() / 60.0));
        double fee = rateCard.feeForHours(hours);
        ticket.close(fee);
        totalRevenue += fee;
        ticketsByPlate.remove(ticket.getVehicle().getPlateNumber());
        recentHistory.addFirst(ticket);
        while (recentHistory.size() > 8) {
            recentHistory.removeLast();
        }
        slot.release();
        availableSlots.add(slot);
        return Optional.of(ticket);
    }

    public synchronized boolean registerSlot(ParkingSlot slot) {
        boolean added = parkingLot.addSlot(slot);
        if (added && !slot.isOccupied()) {
            availableSlots.add(slot);
        }
        return added;
    }

    public Optional<ParkingTicket> findActiveTicketByPlate(String plateNumber) {
        if (plateNumber == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(ticketsByPlate.get(plateNumber.trim().toUpperCase()));
    }

    public Map<Integer, Long> getFloorLoad() {
        Map<Integer, Long> floorLoad = new TreeMap<>();
        parkingLot.getSlots().forEach(slot -> {
            if (slot.isOccupied()) {
                floorLoad.merge(slot.getFloor(), 1L, Long::sum);
            }
        });
        return floorLoad;
    }

    public List<ParkingTicket> getRecentHistory() {
        return recentHistory.stream().toList();
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public double estimateFee(long hours) {
        return rateCard.feeForHours(Math.max(1, hours));
    }

    public Map<String, ParkingTicket> getActiveTickets() {
        return activeTickets;
    }

    public ParkingLot getParkingLot() {
        return parkingLot;
    }
}


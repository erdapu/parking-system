package com.cityparking.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Captures the lifecycle of a parked vehicle.
 */
public class ParkingTicket {
    private final String ticketId;
    private final ParkingSlot slot;
    private final VehicleInfo vehicle;
    private final LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private double amount;

    public ParkingTicket(ParkingSlot slot, VehicleInfo vehicle) {
        this.ticketId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.slot = slot;
        this.vehicle = vehicle;
        this.checkInTime = LocalDateTime.now();
    }

    public String getTicketId() {
        return ticketId;
    }

    public ParkingSlot getSlot() {
        return slot;
    }

    public VehicleInfo getVehicle() {
        return vehicle;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public double getAmount() {
        return amount;
    }

    public Duration getDuration() {
        LocalDateTime end = checkOutTime != null ? checkOutTime : LocalDateTime.now();
        return Duration.between(checkInTime, end);
    }

    public void close(double amount) {
        this.checkOutTime = LocalDateTime.now();
        this.amount = amount;
    }
}


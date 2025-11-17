package com.cityparking.model;

import java.time.LocalDateTime;

/**
 * Represents a physical parking slot inside a multi-level parking structure.
 */
public class ParkingSlot implements Comparable<ParkingSlot> {
    private final String slotId;
    private final int floor;
    private final int distance; // proxy for "nearest to gate"
    private boolean occupied;
    private VehicleInfo currentVehicle;
    private LocalDateTime startTime;

    public ParkingSlot(String slotId, int floor, int distance) {
        this.slotId = slotId;
        this.floor = floor;
        this.distance = distance;
        this.occupied = false;
    }

    public String getSlotId() {
        return slotId;
    }

    public int getFloor() {
        return floor;
    }

    public int getDistance() {
        return distance;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public VehicleInfo getCurrentVehicle() {
        return currentVehicle;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void occupy(VehicleInfo vehicle, LocalDateTime start) {
        this.occupied = true;
        this.currentVehicle = vehicle;
        this.startTime = start;
    }

    public void release() {
        this.occupied = false;
        this.currentVehicle = null;
        this.startTime = null;
    }

    @Override
    public int compareTo(ParkingSlot other) {
        int floorCompare = Integer.compare(this.floor, other.floor);
        if (floorCompare != 0) {
            return floorCompare;
        }
        int distanceCompare = Integer.compare(this.distance, other.distance);
        if (distanceCompare != 0) {
            return distanceCompare;
        }
        return this.slotId.compareTo(other.slotId);
    }

    @Override
    public String toString() {
        return slotId + " (F" + floor + ")";
    }
}


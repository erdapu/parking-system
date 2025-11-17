package com.cityparking.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Aggregates the slots that belong to the smart parking site.
 */
public class ParkingLot {
    private final String name;
    private final List<ParkingSlot> slots;

    public ParkingLot(String name, List<ParkingSlot> slots) {
        this.name = name;
        this.slots = new ArrayList<>(slots);
    }

    public String getName() {
        return name;
    }

    public List<ParkingSlot> getSlots() {
        return Collections.unmodifiableList(slots);
    }

    public synchronized boolean addSlot(ParkingSlot slot) {
        Optional<ParkingSlot> existing = slots.stream()
                .filter(s -> s.getSlotId().equalsIgnoreCase(slot.getSlotId()))
                .findFirst();
        if (existing.isPresent()) {
            return false;
        }
        slots.add(slot);
        return true;
    }

    public List<ParkingSlot> getAvailableSlots() {
        return slots.stream().filter(slot -> !slot.isOccupied()).collect(Collectors.toList());
    }

    public int getTotalSlots() {
        return slots.size();
    }

    public long getOccupiedCount() {
        return slots.stream().filter(ParkingSlot::isOccupied).count();
    }

    public long getAvailableCount() {
        return getTotalSlots() - getOccupiedCount();
    }
}


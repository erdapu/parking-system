package com.cityparking.util;

import com.cityparking.model.ParkingSlot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Writes the current slot status to a small JSON file consumed by the HTML dashboard.
 */
public final class SlotSnapshotWriter {
    private SlotSnapshotWriter() {
    }

    public static void write(List<ParkingSlot> slots, Path file) {
        try {
            Files.createDirectories(file.getParent());
            StringBuilder json = new StringBuilder();
            json.append("[\n");
            for (int i = 0; i < slots.size(); i++) {
                ParkingSlot slot = slots.get(i);
                String vehicle = slot.isOccupied() && slot.getCurrentVehicle() != null
                        ? slot.getCurrentVehicle().getPlateNumber()
                        : "";
                json.append("  {")
                        .append("\"slotId\":\"").append(slot.getSlotId()).append("\",")
                        .append("\"floor\":").append(slot.getFloor()).append(',')
                        .append("\"distance\":").append(slot.getDistance()).append(',')
                        .append("\"occupied\":").append(slot.isOccupied()).append(',')
                        .append("\"vehicle\":\"")
                        .append(vehicle)
                        .append("\"}");
                if (i < slots.size() - 1) {
                    json.append(",");
                }
                json.append("\n");
            }
            json.append("]");
            Files.writeString(file, json.toString());
        } catch (IOException e) {
            System.err.println("Unable to export slot snapshot: " + e.getMessage());
        }
    }
}


package com.cityparking.model;

import java.util.Objects;

/**
 * Simple DTO describing a vehicle entering the parking lot.
 */
public class VehicleInfo {
    private final String plateNumber;
    private final String ownerName;
    private final String phoneNumber;

    public VehicleInfo(String plateNumber, String ownerName, String phoneNumber) {
        this.plateNumber = plateNumber.trim().toUpperCase();
        this.ownerName = ownerName.trim();
        this.phoneNumber = phoneNumber.trim();
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VehicleInfo that = (VehicleInfo) o;
        return plateNumber.equals(that.plateNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(plateNumber);
    }

    @Override
    public String toString() {
        return plateNumber + " (" + ownerName + ")";
    }
}


package com.cityparking.model;

/**
 * Simple pricing policy using progressive hourly slabs.
 */
public class RateCard {
    private final double firstHour;
    private final double additionalHour;
    private final double dailyCap;

    public RateCard(double firstHour, double additionalHour, double dailyCap) {
        this.firstHour = firstHour;
        this.additionalHour = additionalHour;
        this.dailyCap = dailyCap;
    }

    public double feeForHours(long hours) {
        if (hours <= 1) {
            return firstHour;
        }
        double total = firstHour + (hours - 1) * additionalHour;
        if (total > dailyCap) {
            return dailyCap;
        }
        return total;
    }
}


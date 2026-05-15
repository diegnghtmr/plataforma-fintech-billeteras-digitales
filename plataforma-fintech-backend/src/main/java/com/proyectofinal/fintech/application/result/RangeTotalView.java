package com.proyectofinal.fintech.application.result;

/**
 * Application-layer view for the total-moved-in-range analytics result.
 * ZERO Spring/Jakarta/infrastructure imports — pure application type.
 */
public record RangeTotalView(
        double totalAmount,
        int count,
        String from,
        String to
) {
}

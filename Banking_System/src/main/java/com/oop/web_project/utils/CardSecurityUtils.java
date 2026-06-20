package com.oop.web_project.utils;

import java.util.UUID;

public final class CardSecurityUtils {


    /**
     * Masks a standard 16-digit PAN, exposing the first 6 and last 4 digits.
     * Non-16 digit formats are safely returned as-is.
     *
     * @param pan The 16-digit Primary Account Number string.
     * @return The masked PAN string.
     */
    public static String maskPan(final String pan) {
        if (pan == null || pan.length() != 16) {
            return pan;
        }
        return pan.substring(0, 6) + "******" + pan.substring(12);
    }

    /**
     * Simulates token generation by creating a unique 16-digit pseudo-token.
     * Preserves the Major Industry Identifier (first digit) for routing.
     *
     * @param pan The original 16-digit PAN.
     * @return A secure, unique 16-digit token string.
     */
    public static String generateToken(final String pan) {
        if (pan == null || pan.isEmpty()) {
            return null;
        }

        char routingId = pan.charAt(0);

        StringBuilder randomDigits =
                new StringBuilder(UUID.randomUUID().toString().replaceAll("[^0-9]", ""));

        while (randomDigits.length() < 15) {
            randomDigits.append("0");
        }

        return routingId + randomDigits.substring(0, 15);
    }

}

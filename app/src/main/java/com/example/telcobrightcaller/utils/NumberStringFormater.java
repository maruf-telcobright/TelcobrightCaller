package com.example.telcobrightcaller.utils;

public class NumberStringFormater {
    public static String reformatPhoneNumber(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        String[] parts = input.split("@");
        // Consider only the part before the "@" for digit extraction
        String phonePart = parts[0];
        // Removing any non-digit characters
        String digits = phonePart.replaceAll("\\D", "");

        // Checking the length of the digits and prefixing accordingly
        if (digits.startsWith("880") && digits.length() == 13) {
            return "00" + digits;
        } else if (digits.startsWith("880") && digits.length() == 12) {
            return "00880" + digits.substring(3);
        } else if (digits.startsWith("0") && digits.length() == 11) {
            return "00880" + digits.substring(1);
        } else if (digits.length() == 10 && digits.startsWith("1")) {
            return "00880" + digits;
        }  else {
            return "";
        }
    }


    public static String normalizePhoneNumber(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        // Remove any non-numeric characters except '+'
        String cleaned = input.replaceAll("[^0-9+]", "");

        // Check if the number starts with +880
        if (cleaned.startsWith("+880")) {
            return cleaned;
        }

        // If the number starts with 880 but doesn't have a '+', add it
        if (cleaned.startsWith("880")) {
            return "+" + cleaned;
        }

        // If the number starts with 0, replace it with +880
        if (cleaned.startsWith("0")) {
            return "+880" + cleaned.substring(1);
        }

        // If none of the above, return as it is or handle invalid number case
        return cleaned;
    }
}

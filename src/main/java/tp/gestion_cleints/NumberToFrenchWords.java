package tp.gestion_cleints;

/**
 * Utility class to convert numbers to French words
 */
public class NumberToFrenchWords {

    private static final String[] UNITS = {
            "", "un", "deux", "trois", "quatre", "cinq", "six", "sept", "huit", "neuf",
            "dix", "onze", "douze", "treize", "quatorze", "quinze", "seize",
            "dix-sept", "dix-huit", "dix-neuf"
    };

    private static final String[] TENS = {
            "", "", "vingt", "trente", "quarante", "cinquante",
            "soixante", "soixante-dix", "quatre-vingt", "quatre-vingt-dix"
    };

    /**
     * Convert a number to French words
     * 
     * @param amount   The amount to convert (supports up to 999,999,999.99)
     * @param currency The currency name (e.g., "dirhams", "euros")
     * @return The amount in French words
     */
    public static String convert(double amount, String currency) {
        if (amount < 0) {
            return "moins " + convert(-amount, currency);
        }

        long integerPart = (long) amount;
        int decimalPart = (int) Math.round((amount - integerPart) * 100);

        StringBuilder result = new StringBuilder();

        if (integerPart == 0) {
            result.append("zéro");
        } else {
            result.append(convertIntegerPart(integerPart));
        }

        // Add currency
        result.append(" ").append(currency);

        // Add decimal part if exists
        if (decimalPart > 0) {
            result.append(" et ").append(convertIntegerPart(decimalPart));
            result.append(" centime");
            if (decimalPart > 1) {
                result.append("s");
            }
        }

        // Capitalize first letter
        return result.substring(0, 1).toUpperCase() + result.substring(1);
    }

    private static String convertIntegerPart(long number) {
        if (number == 0) {
            return "";
        }

        if (number < 20) {
            return UNITS[(int) number];
        }

        if (number < 100) {
            return convertTens((int) number);
        }

        if (number < 1000) {
            return convertHundreds((int) number);
        }

        if (number < 1000000) {
            return convertThousands(number);
        }

        if (number < 1000000000) {
            return convertMillions(number);
        }

        return "nombre trop grand";
    }

    private static String convertTens(int number) {
        if (number < 20) {
            return UNITS[number];
        }

        int unit = number % 10;
        int ten = number / 10;

        if (ten == 7 || ten == 9) {
            // Special case for 70-79 and 90-99
            if (ten == 7) {
                return "soixante-" + UNITS[10 + unit];
            } else {
                return "quatre-vingt-" + UNITS[10 + unit];
            }
        }

        if (unit == 0) {
            return TENS[ten];
        }

        if (unit == 1 && (ten == 2 || ten == 3 || ten == 4 || ten == 5 || ten == 6)) {
            return TENS[ten] + " et un";
        }

        // Special case for 80
        if (number == 80) {
            return "quatre-vingts";
        }

        return TENS[ten] + "-" + UNITS[unit];
    }

    private static String convertHundreds(int number) {
        int hundred = number / 100;
        int remainder = number % 100;

        StringBuilder result = new StringBuilder();

        if (hundred == 1) {
            result.append("cent");
        } else {
            result.append(UNITS[hundred]).append(" cent");
        }

        // Add 's' for exact hundreds (200, 300, etc.)
        if (remainder == 0 && hundred > 1) {
            result.append("s");
        }

        if (remainder > 0) {
            result.append(" ").append(convertTens(remainder));
        }

        return result.toString();
    }

    private static String convertThousands(long number) {
        long thousand = number / 1000;
        long remainder = number % 1000;

        StringBuilder result = new StringBuilder();

        if (thousand == 1) {
            result.append("mille");
        } else {
            result.append(convertIntegerPart(thousand)).append(" mille");
        }

        if (remainder > 0) {
            result.append(" ").append(convertIntegerPart(remainder));
        }

        return result.toString();
    }

    private static String convertMillions(long number) {
        long million = number / 1000000;
        long remainder = number % 1000000;

        StringBuilder result = new StringBuilder();

        if (million == 1) {
            result.append("un million");
        } else {
            result.append(convertIntegerPart(million)).append(" millions");
        }

        if (remainder > 0) {
            result.append(" ").append(convertIntegerPart(remainder));
        }

        return result.toString();
    }
}

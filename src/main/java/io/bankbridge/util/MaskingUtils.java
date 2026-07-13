package io.bankbridge.util;

public final class MaskingUtils {

    private MaskingUtils() {}

    /**
     * Masks an account number by keeping first 4 and last 4 chars.
     * Example: GB12ABCD5678 -> GB12****5678
     */
    public static String maskAccount(String account) {
        if (account == null || account.length() <= 8) {
            return account;
        }
        return account.substring(0, 4) + "****" + account.substring(account.length() - 4);
    }

    /**
     * Masks a name by keeping first char.
     * Example: "Zhang San" -> "Z****"
     * Example: "John" -> "J****"
     */
    public static String maskName(String name) {
        if (name == null || name.isBlank()) {
            return name;
        }
        return name.charAt(0) + "****";
    }
}

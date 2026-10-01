package com.safae.prefixblock;

import java.util.ArrayList;
import java.util.List;

/** Pure matching logic (no Android dependencies, so it can be unit-tested). */
public final class Rules {
    private Rules() {}

    /** Keeps digits and a leading '+'; turns an international "00" prefix into "+". */
    public static String normalize(String raw) {
        if (raw == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c >= '0' && c <= '9') sb.append(c);
            else if (c == '+' && sb.length() == 0) sb.append(c);
        }
        String s = sb.toString();
        if (s.startsWith("00")) s = "+" + s.substring(2);
        return s;
    }

    /** Parses the user's text (one prefix per line, or separated by commas/semicolons). */
    public static List<String> parsePrefixes(String text) {
        List<String> out = new ArrayList<String>();
        if (text == null) return out;
        String[] parts = text.split("[\\n,;]");
        for (String p : parts) {
            String n = normalize(p);
            if (n.length() == 0 || n.equals("+")) continue;
            if (!out.contains(n)) out.add(n);
        }
        return out;
    }

    /** Returns the matching prefix, or null if the number is not blocked. */
    public static String match(String number, List<String> prefixes) {
        String n = normalize(number);
        if (n.length() == 0) return null;
        for (String p : prefixes) {
            if (n.startsWith(p)) return p;
        }
        return null;
    }

    public static String join(List<String> items) {
        StringBuilder sb = new StringBuilder();
        for (String s : items) {
            if (sb.length() > 0) sb.append('\n');
            sb.append(s);
        }
        return sb.toString();
    }
}

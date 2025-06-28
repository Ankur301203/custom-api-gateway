package com.aron.dead_gate.util;

public class PathUtil {
    public static Integer extractId(String[] parts, int expectedLength) {
        if (parts.length == expectedLength) {
            try {
                return Integer.parseInt(parts[expectedLength - 1]);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}


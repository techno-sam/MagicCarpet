package com.slimeist.magic_carpet.common.util;

public class MathUtil {
    public static int cap(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }
    public static float cap(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }
    public static double cap(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }
}

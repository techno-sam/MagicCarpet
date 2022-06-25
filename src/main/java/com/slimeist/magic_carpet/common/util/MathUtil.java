package com.slimeist.magic_carpet.common.util;

import net.minecraft.util.math.Vec3f;

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

    public static Vec3f vector(float[] arr) {
        if (arr.length != 3) {
            return Vec3f.ZERO;
        } else {
            return new Vec3f(arr[0], arr[1], arr[2]);
        }
    }
}

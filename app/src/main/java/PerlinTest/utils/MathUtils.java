package PerlinTest.utils;

public class MathUtils {
    public static double lerp(double t, double a, double b) {
        return t * (a - b) + a;
    }

    public static double map(double x, double in_min, double in_max, double out_min, double out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }
}

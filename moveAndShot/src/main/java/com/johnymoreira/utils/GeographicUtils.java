package com.johnymoreira.utils;

public class GeographicUtils {
    private static StringBuilder sb = new StringBuilder(20);

    public static String latitudeRef(double latitude) {
        return latitude < 0.0d ? "S" : "N";
    }

    public static String longitudeRef(double longitude) {
        return longitude < 0.0d ? "W" : "E";
    }

    public static final synchronized String convert(double coordinate) {
        String stringBuilder;
        synchronized (GeographicUtils.class) {
            coordinate = Math.abs(coordinate);
            int degree = (int) coordinate;
            coordinate = (coordinate * 60.0d) - (((double) degree) * 60.0d);
            int minute = (int) coordinate;
            int second = (int) (1000.0d * ((coordinate * 60.0d) - (((double) minute) * 60.0d)));
            sb.setLength(0);
            sb.append(degree);
            sb.append("/1,");
            sb.append(minute);
            sb.append("/1,");
            sb.append(second);
            sb.append("/1000,");
            stringBuilder = sb.toString();
        }
        return stringBuilder;
    }
}

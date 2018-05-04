package com.johnymoreira.pojo;

import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.List;

/**
 * created by johnymoreira
 *
 * POJO for Route representation
 */
public class Route {
    private final List<LatLng> points = new ArrayList();
    private String polyline;

    public void addPoints(List<LatLng> points) {
        this.points.addAll(points);
    }

    public List<LatLng> getPoints() {
        return this.points;
    }

    public void setPolyline(String polyline) {
        this.polyline = polyline;
    }

    public String getPolyline() {
        return this.polyline;
    }
}

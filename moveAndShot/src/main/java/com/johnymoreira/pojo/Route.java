package com.johnymoreira.pojo;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;

/**
 * created by johnymoreira
 *
 * POJO for Route representation
 */
public class Route {
	private final List<LatLng> points;
	private String polyline;

	// construtor default. Apenas inicializa o conjunto de pontos da rota
	public Route() {
		points = new ArrayList<LatLng>();
	}

	public void addPoints(final List<LatLng> points) {
		this.points.addAll(points);
	}

	public List<LatLng> getPoints() {
		return points;
	}

	public void setPolyline(String polyline) {
		this.polyline = polyline;
	}

	public String getPolyline() {
		return polyline;
	}
}

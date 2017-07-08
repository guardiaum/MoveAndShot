package com.johnymoreira.utils;

import java.util.Locale;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.johnymoreira.pojo.Route;

public class RotaAsyncTask extends AsyncTask<Double, Void, Void> {
	
	private GoogleMap mapView;
	private Route rota;

	public RotaAsyncTask(Context ctx, GoogleMap mapa) {
		mapView = mapa;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
	}

	@Override
	protected Void doInBackground(Double... params) {

		rota = directions(new LatLng(params[0], params[1]), new LatLng(
				params[2], params[3]));
		
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		
		PolylineOptions options = new PolylineOptions().width(10)
				.color(Color.YELLOW).visible(true);

		for (LatLng latlng : rota.getPoints()) {
			options.add(latlng);
		}

		mapView.addPolyline(options);
	}

	private Route directions(final LatLng start, final LatLng dest) {

		// Formatando a URL com a latitude e longitude
		// de origem e destino.
		String urlRota = String.format(Locale.US,
				"http://maps.googleapis.com/maps/api/"
						+ "directions/json?origin=%f,%f&"
						+ "destination=%f,%f&" + "sensor=true&mode=driving",
				start.latitude, start.longitude, dest.latitude, dest.longitude);

		GoogleParser parser;
		parser = new GoogleParser(urlRota);
		return parser.parse();
	}
}

package com.johnymoreira.utils;

import android.content.Context;
import android.os.AsyncTask;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.johnymoreira.pojo.Route;
import java.util.Locale;

public class RotaAsyncTask extends AsyncTask<Double, Void, Void> {
    private GoogleMap mapView;
    private Route rota;

    public RotaAsyncTask(Context ctx, GoogleMap mapa) {
        this.mapView = mapa;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }

    protected Void doInBackground(Double... params) {
        this.rota = directions(new LatLng(params[0].doubleValue(), params[1].doubleValue()), new LatLng(params[2].doubleValue(), params[3].doubleValue()));
        return null;
    }

    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        PolylineOptions options = new PolylineOptions().width(10.0f).color(-256).visible(true);
        for (LatLng latlng : this.rota.getPoints()) {
            options.add(latlng);
        }
        this.mapView.addPolyline(options);
    }

    private Route directions(LatLng start, LatLng dest) {
        return new GoogleParser(String.format(Locale.US, "http://maps.googleapis.com/maps/api/directions/json?origin=%f,%f&destination=%f,%f&sensor=true&mode=driving", new Object[]{Double.valueOf(start.latitude), Double.valueOf(start.longitude), Double.valueOf(dest.latitude), Double.valueOf(dest.longitude)})).parse();
    }
}

package com.johnymoreira.moveandshot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.johnymoreira.pojo.PontoDeInteresse;
import com.johnymoreira.utils.Constants;
import com.johnymoreira.utils.RotaAsyncTask;

public class MapActivity extends Activity implements LocationListener, OnMapReadyCallback {
	private LocationManager lm;
	private GoogleMap map;
	private PontoDeInteresse poi;
	private ArrayList<LatLng> pontos = new ArrayList<LatLng>();
	private PolygonOptions drawpolygon;
	private ProgressDialog dialog;
	private Location atualLocalizacao;
	private String poi_id;
	private String nomePOI;
	private double lat;
	private double lng;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		dialog = ProgressDialog.show(this, "Aguarde",
				"Calculando rota");
		lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 5, this);
		atualLocalizacao = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		
		Bundle bd = getIntent().getExtras();
		poi_id = bd.getString("poi_id");
		nomePOI = bd.getString("nome_ponto");
		lat = bd.getDouble("latitude");
		lng = bd.getDouble("longitude");
		
		poi = new PontoDeInteresse(Integer.parseInt(poi_id), nomePOI, new LatLng(lat,lng));
		
		if(map == null && poi!=null && nomePOI!=null && atualLocalizacao!=null){
			MapFragment mapFragment = (MapFragment) getFragmentManager()
					.findFragmentById(R.id.fragmentMap);
			mapFragment.getMapAsync(this);
			
		}else{
			Toast.makeText(getApplicationContext(),
                    "Desculpe, algo deu errado. Volte para tela anterior e tente novamente.", Toast.LENGTH_SHORT)
                    .show();
		}
	}
	
	public void getRoute(Location origem, PontoDeInteresse destino){
		new RotaAsyncTask(this, map).execute(
				origem.getLatitude(), origem.getLongitude(),    
			      destino.getPonto().latitude,destino.getPonto().longitude);
	}
	
	public void abrirCamera(View v){
		activityCamera();
	}
	
	private void activityCamera() {
		Intent it = new Intent(getApplicationContext(), CameraActivity2.class);
		it.putExtra("latitude", poi.getPonto().latitude );
		it.putExtra("longitude", poi.getPonto().longitude);
		startActivity(it);
	}
	
	/*public double distance(LatLng StartP, LatLng EndP) {
		double lat1 = StartP.latitude;
		double lat2 = EndP.latitude;
		double lon1 = StartP.longitude;
		double lon2 = EndP.longitude;
		double dLat = Math.toRadians(lat2-lat1);
		double dLon = Math.toRadians(lon2-lon1);
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon/2) * Math.sin(dLon/2);
		double c = 2 * Math.asin(Math.sqrt(a));
		return 6366000 * c;
	}*/
	
	@Override
	public void onLocationChanged(Location location) {
		LatLng myCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
		if(drawpolygon!=null){
			for (LatLng latlng : drawpolygon.getPoints()) {
				if(myCurrentLocation.equals(latlng)){
					activityCamera();
				}
			}
		}
	}
	
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(this, "Provedor de localização habilitado.", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(this, "Provedor de localização desabilitado.", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	protected void onDestroy() {
		lm.removeUpdates(this);
		super.onDestroy();
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		map = googleMap;

		map.setMyLocationEnabled(true);

		MarkerOptions markerPOI = new MarkerOptions().position(poi.getPonto()).title(nomePOI);

		map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(atualLocalizacao.getLatitude(), atualLocalizacao.getLongitude()), 18));
		map.addMarker(markerPOI);

		if (map == null) {
			Toast.makeText(getApplicationContext(),
					"Desculpe, não foi possível criar o mapa.", Toast.LENGTH_SHORT)
					.show();
		}

		getRoute(atualLocalizacao, poi);

		new ConsomeWS()
				.execute(Constants.WS_SERVICE_URL + "/pois/getArea?poi_id="+poi_id);
	}

	private class ConsomeWS extends AsyncTask<String, Void, Void> {

		private String content = null;
		private String error = null;

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected Void doInBackground(String... uri) {
			BufferedReader reader = null;

			try {
				// Defined URL where to send data
				URL url = new URL(uri[0]);
				// Send POST data request
				URLConnection conn = url.openConnection();
				// Get the server response
				if(conn.getInputStream()!=null){
					reader = new BufferedReader(new InputStreamReader(
							conn.getInputStream()));
					StringBuilder sb = new StringBuilder();
					String line = null;
	
					// Read Server Response
					while ((line = reader.readLine()) != null) {
						// Append server response in string
						sb.append(line + "");
					}
					// Append Server Response To Content String
					content = sb.toString();
				}
			} catch (MalformedURLException e) {
				error = e.getMessage();
			} catch (IOException e) {
				error = e.getMessage();
			} finally {
				try {
					reader.close();
				} catch (Exception ex) {
					error = ex.getMessage();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (error != null) {
				Log.e("WS - maspois", error);
				Toast.makeText(getApplicationContext(),
						"Desculpe! Ocorreu um erro de comunicação com o WS.",
						Toast.LENGTH_SHORT).show();

			} else {
				// Show Response Json On Screen (activity)
				Toast.makeText(getApplicationContext(), "Comunicação estabelecida.",
						Toast.LENGTH_SHORT).show();
				if(content!=null){
					/****************** Start Parse Response JSON Data *************/
					JSONArray jsonResponse;
	
					try {
						Log.i("WS Content", content);
						/******
						 * Creates a new JSONObject with name/value mappings from
						 * the JSON string.
						 ********/
						jsonResponse = new JSONArray(content);
	
						/*****
						 * Returns the value mapped by name if it exists and is a
						 * JSONArray.
						 ***/
						/******* Returns null otherwise. *******/
						Log.i("WS jsonResponse", jsonResponse.toString());
	
						/*********** Process each JSON Node ************/
						int lengthJsonArr = jsonResponse.length();
	
						for (int i = 0; i < lengthJsonArr; i++) {
							/****** Get Object for each JSON node. ***********/
							JSONObject jsonChildNode = jsonResponse
									.getJSONObject(i);
	
							/******* Fetch node values **********/
							Double latitude = jsonChildNode.optDouble("latitude");
							Double longitude = jsonChildNode.optDouble("longitude");
							
							pontos.add(new LatLng(latitude, longitude));
						}
						/****************** End Parse Response JSON Data *************/
						drawpolygon = new PolygonOptions().strokeColor(Color.BLUE).fillColor(Color.BLUE);
						for (LatLng latlng : pontos) {
							drawpolygon.add(latlng);
						}
						//Polygon polygon = 
						map.addPolygon(drawpolygon);
						
					} catch (JSONException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}else{
					Toast.makeText(getApplicationContext(),
							"Não existem áreas sugeridas para captura de fotografias.",
							Toast.LENGTH_SHORT).show();
				}
			}
			dialog.dismiss();
		}
	}
	
}
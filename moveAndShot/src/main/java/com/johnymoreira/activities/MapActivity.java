package com.johnymoreira.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.johnymoreira.pojo.PontoDeInteresse;
import com.johnymoreira.utils.Constants;
import com.johnymoreira.utils.RotaAsyncTask;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MapActivity extends Activity implements LocationListener, OnInitListener, OnMapReadyCallback {
    private ProgressDialog dialog;
    private PolygonOptions drawpolygon;
    private LocationManager lm;
    private GoogleMap map;
    private PontoDeInteresse poi;
    private ArrayList<LatLng> pontos = new ArrayList();
    private TextToSpeech tts;
    private Vibrator v;

    private class ConsomeWS extends AsyncTask<String, Void, Void> {
        private String content;
        private String error;

        private ConsomeWS() {
            this.content = null;
            this.error = null;
        }

        protected void onPreExecute() {
        }

        protected Void doInBackground(String... uri) {
            MalformedURLException e;
            IOException e2;
            Throwable th;
            BufferedReader reader = null;
            try {
                URLConnection conn = new URL(uri[0]).openConnection();
                if (conn.getInputStream() != null) {
                    BufferedReader reader2 = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    try {
                        StringBuilder sb = new StringBuilder();
                        while (true) {
                            String line = reader2.readLine();
                            if (line == null) {
                                break;
                            }
                            sb.append(new StringBuilder(String.valueOf(line)).toString());
                        }
                        this.content = sb.toString();
                        reader = reader2;
                    } catch (MalformedURLException e3) {
                        e = e3;
                        reader = reader2;
                    } catch (IOException e4) {
                        e2 = e4;
                        reader = reader2;
                    } catch (Throwable th2) {
                        th = th2;
                        reader = reader2;
                    }
                }
                try {
                    reader.close();
                } catch (Exception ex) {
                    this.error = ex.getMessage();
                }
            } catch (MalformedURLException e5) {
                e = e5;
                try {
                    this.error = e.getMessage();
                    try {
                        reader.close();
                    } catch (Exception ex2) {
                        this.error = ex2.getMessage();
                    }
                    return null;
                } catch (Throwable th3) {
                    th = th3;
                    try {
                        reader.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            } catch (IOException e6) {
                e2 = e6;
                this.error = e2.getMessage();
                try {
                    reader.close();
                } catch (Exception ex222) {
                    this.error = ex222.getMessage();
                }
                return null;
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            if (this.error != null) {
                Log.e("WS - maspois", this.error);
                Toast.makeText(MapActivity.this.getApplicationContext(), "Desculpe! Ocorreu um erro de comunicação com o WS.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MapActivity.this.getApplicationContext(), "Comunicação estabelecida.", Toast.LENGTH_SHORT).show();
                if (this.content != null) {
                    try {
                        Log.i("WS Content", this.content);
                        JSONArray jsonResponse = new JSONArray(this.content);
                        Log.i("WS jsonResponse", jsonResponse.toString());
                        int lengthJsonArr = jsonResponse.length();
                        for (int i = 0; i < lengthJsonArr; i++) {
                            JSONObject jsonChildNode = jsonResponse.getJSONObject(i);
                            MapActivity.this.pontos.add(new LatLng(Double.valueOf(jsonChildNode.optDouble("latitude")).doubleValue(), Double.valueOf(jsonChildNode.optDouble("longitude")).doubleValue()));
                        }
                        MapActivity.this.drawpolygon = new PolygonOptions().strokeColor(-16776961).fillColor(-16776961);
                        Iterator it = MapActivity.this.pontos.iterator();
                        while (it.hasNext()) {
                            LatLng latlng = (LatLng) it.next();
                            MapActivity.this.drawpolygon.add(latlng);
                            Log.i("myCurrentLocation", "(" + latlng.latitude + ", " + latlng.longitude + ")");
                        }
                        MapActivity.this.map.addPolygon(MapActivity.this.drawpolygon);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                } else {
                    Toast.makeText(MapActivity.this.getApplicationContext(), "Não existem áreas sugeridas para captura de fotografias.", Toast.LENGTH_SHORT).show();
                }
            }
            Log.i("myCurrentLocation", MapActivity.this.drawpolygon.getPoints().toString());
            MapActivity.this.lm.requestLocationUpdates("gps", 0, 0.0f, MapActivity.this);
            MapActivity.this.dialog.dismiss();
            MapActivity.this.v.vibrate(100);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        this.tts = new TextToSpeech(getApplicationContext(), this);
        this.v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        this.dialog = ProgressDialog.show(this, "Aguarde", "Calculando rota");
        this.lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Bundle bd = getIntent().getExtras();
        String poi_id = bd.getString("poi_id");
        this.poi = new PontoDeInteresse(Integer.parseInt(poi_id), bd.getString("nome_ponto"), new LatLng(bd.getDouble("latitude"), bd.getDouble("longitude")));
    }

    private void carregaMapa() {
        Location atualLocalizacao = this.lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if(map == null && poi!=null && this.poi.getNomePOI()!=null && atualLocalizacao!=null){
            MapFragment mapFragment = (MapFragment) getFragmentManager()
                    .findFragmentById(R.id.fragmentMap);
            mapFragment.getMapAsync(this);

        }else{
            Toast.makeText(getApplicationContext(),
                    "Desculpe, algo deu errado. Volte para tela anterior e tente novamente.", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        Location atualLocalizacao = this.lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        map.setMyLocationEnabled(true);

        MarkerOptions markerPOI = new MarkerOptions().position(poi.getPonto()).title(this.poi.getNomePOI());

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
                .execute(Constants.WS_SERVICE_URL + "/pois/getArea?poi_id=" + this.poi.getId());
    }


    public void getRoute(Location origem, PontoDeInteresse destino) {
        new RotaAsyncTask(this, this.map).execute(new Double[]{Double.valueOf(origem.getLatitude()), Double.valueOf(origem.getLongitude()), Double.valueOf(destino.getPonto().latitude), Double.valueOf(destino.getPonto().longitude)});
    }

    public void abrirCamera(View v) {
        this.v.vibrate(100);
        activityCamera();
    }

    private void activityCamera() {
        Intent it = new Intent(getApplicationContext(), CameraActivity.class);
        it.putExtra("latitude", this.poi.getPonto().latitude);
        it.putExtra("longitude", this.poi.getPonto().longitude);
        startActivity(it);
    }

    private boolean isPointInPolygon(LatLng tap, List<LatLng> vertices) {
        int intersectCount = 0;
        for (int j = 0; j < vertices.size() - 1; j++) {
            if (rayCastIntersect(tap, (LatLng) vertices.get(j), (LatLng) vertices.get(j + 1))) {
                intersectCount++;
            }
        }
        if (intersectCount % 2 == 1) {
            return true;
        }
        return false;
    }

    private boolean rayCastIntersect(LatLng tap, LatLng vertA, LatLng vertB) {
        double aY = vertA.latitude;
        double bY = vertB.latitude;
        double aX = vertA.longitude;
        double bX = vertB.longitude;
        double pY = tap.latitude;
        double pX = tap.longitude;
        if ((aY > pY && bY > pY) || ((aY < pY && bY < pY) || (aX < pX && bX < pX))) {
            return false;
        }
        double m = (aY - bY) / (aX - bX);
        return (pY - (((-aX) * m) + aY)) / m > pX;
    }

    public void onLocationChanged(Location location) {
        LatLng myCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        if (this.drawpolygon.getPoints() != null) {
            Log.i("MaS", "onLocationChanged");
            Log.i("myCurrentLocation", "(" + myCurrentLocation.latitude + ", " + myCurrentLocation.longitude + ")");
            if (isPointInPolygon(myCurrentLocation, this.drawpolygon.getPoints())) {
                Log.i("MaS", "inside");
                this.lm.removeUpdates(this);
                activityCamera();
            }
        }
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Provedor de localização habilitado.", Toast.LENGTH_SHORT).show();
    }

    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Provedor de localização desabilitado.", Toast.LENGTH_SHORT).show();
    }

    protected void onDestroy() {
        this.lm.removeUpdates(this);
        super.onDestroy();
    }

    public void onInit(int status) {
        if (status == -1) {
            Toast.makeText(this, "Desculpe! Seu dispositivo não está configurado para utilização do Speach.", Toast.LENGTH_SHORT).show();
            return;
        }
        this.tts.speak("Carregando rota até o ponto de interesse. Por favor aguarde...", 0, null);
        carregaMapa();
    }
}

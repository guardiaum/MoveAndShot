package com.johnymoreira.activities;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import com.johnymoreira.pojo.PontoDeInteresse;
import com.johnymoreira.utils.CameraController;
import java.io.IOException;
import java.util.Locale;

public class CameraActivity extends Activity implements SensorEventListener, OnClickListener,
        OnLongClickListener, OnInitListener, LocationListener {

    private Sensor accelerometer;
    private CameraController camera;
    private ImageView compass;
    private float currentDegreeArrow = 0.0f;
    private float currentDegreeCompass = 0.0f;
    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;
    private Location location;
    private LocationManager locationManager;
    private float[] mGeomagnetic;
    private float[] mGravity;
    private SensorManager mSensorManager;
    private Sensor magnetometer;
    private PontoDeInteresse poi;
    private ImageView seta;
    private TextToSpeech tts;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);

        this.tts = new TextToSpeech(getApplicationContext(), this);

        Bundle bd = getIntent().getExtras();
        try {
            this.poi = new PontoDeInteresse(0, bd.getString("nome_ponto"), "",
                    (Address) new Geocoder(getApplicationContext(), Locale.getDefault()).
                            getFromLocation(Double.valueOf(bd.getDouble("latitude")).doubleValue(),
                                    Double.valueOf(bd.getDouble("longitude")).doubleValue(), 1).get(0), "");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Load();

        this.mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        this.accelerometer = this.mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.magnetometer = this.mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        this.mSensorManager.registerListener(this, this.accelerometer, SensorManager.SENSOR_DELAY_UI);
        this.mSensorManager.registerListener(this, this.magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    protected void onPause() {
        this.currentDegreeCompass = 0.0f;
        this.currentDegreeArrow = 0.0f;
        super.onPause();
        this.mSensorManager.unregisterListener(this);
        if (this.camera != null) {
            this.camera.pararVisualizacao();
            this.camera = null;
        }
    }

    protected void onDestroy() {
        this.currentDegreeCompass = 0.0f;
        this.currentDegreeArrow = 0.0f;
        if (this.tts != null) {
            this.tts.stop();
            this.tts.shutdown();
        }
        if (this.camera != null) {
            this.camera.liberarCamera();
            this.camera = null;
        }
        super.onDestroy();
    }

    private void Load() {
        Camera c = getCameraInstance();
        if (c != null) {
            this.camera = new CameraController(this, R.id.svCamera, c, getLocation());
            FrameLayout frame = (FrameLayout) findViewById(R.id.containerCamera);
            frame.setOnClickListener(this);
            frame.setOnLongClickListener(this);
            this.compass = (ImageView) findViewById(R.id.imageCompass);
            this.seta = (ImageView) findViewById(R.id.seta);
        }
    }

    public void onClick(View v) {
        String orientacao = "";
        if (this.currentDegreeArrow > 10.0f) {
            orientacao = "direita";
        } else if (this.currentDegreeArrow < -10.0f) {
            orientacao = "esquerda";
        } else {
            this.tts.speak("Realize a captura.", 0, null);
        }
        if (!orientacao.equals("")) {
            if (this.currentDegreeArrow > 0.0f) {
                this.tts.speak("Vire " + Math.round(this.currentDegreeArrow) + " graus à sua " + orientacao, 0, null);
            } else {
                this.tts.speak("Vire " + Math.round(-this.currentDegreeArrow) + " graus à sua " + orientacao, 0, null);
            }
        }
    }

    public boolean onLongClick(View v) {
        this.camera.tirarFoto();
        Toast.makeText(getApplicationContext(), "Foto Capturada.", Toast.LENGTH_SHORT).show();
        return true;
    }

    public void onInit(int status) {
        if (status == 0) {
            this.tts.speak("Modo de captura ativo.Toque na tela para ouvir orientações. " +
                    "Mantenha pressionada para realizar captura.", 0, null);
        } else if (status == -1) {
            Toast.makeText(getApplicationContext(), "Desculpe! Seu dispositivo não está configurado " +
                    "para utilização do Speach.", Toast.LENGTH_SHORT).show();
        }
    }

    private float pegaOrientacaoAtePOI(float azimuth) {
        Location location = getLocation();
        if (location == null) {
            return 0.0f;
        }
        azimuth += new GeomagneticField((float) location.getLatitude(),
                (float) location.getLongitude(), (float) location.getAltitude(),
                System.currentTimeMillis()).getDeclination();
        Location destino = new Location("gps");
        destino.setLatitude(this.poi.getEndereco().getLatitude());
        destino.setLongitude(this.poi.getEndereco().getLongitude());
        return azimuth - location.bearingTo(destino);
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == 1) {
            this.mGravity = event.values;
        }
        if (event.sensor.getType() == 2) {
            this.mGeomagnetic = event.values;
        }
        if (this.mGravity != null && this.mGeomagnetic != null) {
            float[] R = new float[9];
            if (SensorManager.getRotationMatrix(R, new float[9], this.mGravity, this.mGeomagnetic)) {
                float azimuth = (SensorManager.getOrientation(R, new float[3])[0] * 360.0f) / 6.28318f;
                float degree = (float) Math.round(azimuth);
                float direction = pegaOrientacaoAtePOI(azimuth);
                RotateAnimation raCompass = new RotateAnimation(this.currentDegreeCompass, -degree, 1, 0.5f, 1, 0.5f);
                RotateAnimation raArrow = new RotateAnimation(this.currentDegreeArrow, -direction, 1, 0.5f, 1, 0.5f);
                raCompass.setDuration(1000);
                raArrow.setDuration(1000);
                raCompass.setFillAfter(true);
                raArrow.setFillAfter(true);
                this.seta.startAnimation(raArrow);
                this.compass.startAnimation(raCompass);
                this.currentDegreeCompass = -degree;
                this.currentDegreeArrow = -direction;
            }
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public Location getLocation() {
        try {
            this.locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            this.isGPSEnabled = this.locationManager.isProviderEnabled("gps");
            this.isNetworkEnabled = this.locationManager.isProviderEnabled("network");
            if (this.isGPSEnabled || this.isNetworkEnabled) {
                if (this.isNetworkEnabled) {
                    this.locationManager.requestLocationUpdates("network", 1000000, 50.0f, this);
                    if (this.locationManager != null) {
                        this.location = this.locationManager.getLastKnownLocation("network");
                    }
                }
                if (this.isGPSEnabled && this.location == null) {
                    this.locationManager.requestLocationUpdates("gps", 1000000, 50.0f, this);
                    if (this.locationManager != null) {
                        this.location = this.locationManager.getLastKnownLocation("gps");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.location;
    }

    public void onLocationChanged(Location location) {
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onProviderDisabled(String provider) {
    }
}

package com.johnymoreira.moveandshot;

import java.io.IOException;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.johnymoreira.pojo.PontoDeInteresse;
import com.johnymoreira.utils.CameraController;

public class CameraActivity2 extends Activity implements SensorEventListener, OnClickListener, 
															OnLongClickListener, OnInitListener{
	private CameraController camera;
	private SensorManager mSensorManager;
	private Sensor accelerometer;
	private Sensor magnetometer;
	private TextToSpeech tts;
	private Location currentLoc;
	private PontoDeInteresse poi;
	private float currentDegreeCompass = 0f;
	private float currentDegreeArrow = 0f;
	private ImageView compass;
	private ImageView seta;
	private float[] mGravity;
	private float[] mGeomagnetic;
	private float substract;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        tts = new TextToSpeech(getApplicationContext(),this);
        
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
	    
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
	    mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        
        substract = 0f;
        if(Configuration.ORIENTATION_LANDSCAPE == getResources().getConfiguration().orientation){substract = -90f;}
        
		Bundle bd = getIntent().getExtras();
		double lat = bd.getDouble("latitude");
		double lng = bd.getDouble("longitude");
		
		Address adr;
		try {
			adr = (new Geocoder(getApplicationContext(), Locale.getDefault()) )
					.getFromLocation(lat,lng, 1).get(0);

			poi = new PontoDeInteresse(0,  bd.getString("nome_ponto"),"",adr, "");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	    Load();
    }
    
    public static Camera getCameraInstance(){
	    Camera c = null;
	    try {
	        c = Camera.open();
	    }
	    catch (Exception e){
	        e.printStackTrace();
	    }
	    return c; 
	}
    
    @Override
	protected void onPause() {
	    super.onPause();
	    mSensorManager.unregisterListener(this);
	    if (camera != null){
	    	camera.pararVisualizacao();
	    	camera = null;
	    }
	}
   
    @Override
    protected void onDestroy() {
    	if(tts !=null){
			tts.stop();
			tts.shutdown();
		}
    	if (camera != null){
	    	camera.liberarCamera();
	    	camera = null;
	    }
		super.onDestroy();
    }
    
	private void Load() {
		Camera c = getCameraInstance();
	    if (c != null){
	    	camera = new CameraController(this, R.id.svCamera, c);
	    	FrameLayout frame = (FrameLayout) findViewById(R.id.containerCamera);
	    	frame.setOnClickListener(this);
	    	frame.setOnLongClickListener(this);
	    	
	    	compass = (ImageView) findViewById(R.id.imageCompass);
	    	seta = (ImageView) findViewById(R.id.seta);
	    }		
	}
	
	@Override
	public void onClick(View v) {
		String orientacao = "";
		if(currentDegreeArrow>5){
			orientacao = "direita";
		}else if(currentDegreeArrow < -5){
			orientacao = "esquerda";
		}else{
			tts.speak("Realize a captura.", TextToSpeech.QUEUE_FLUSH, null);
		}
		if(!orientacao.equals("")){
			if(currentDegreeArrow > 0)
				tts.speak("Vire "+Math.round(currentDegreeArrow)+" graus à sua "+orientacao, TextToSpeech.QUEUE_FLUSH, null);
			else
				tts.speak("Vire "+Math.round(-currentDegreeArrow)+" graus à sua "+orientacao, TextToSpeech.QUEUE_FLUSH, null);
		}
	}
	
	@Override
	public boolean onLongClick(View v) {
		camera.tirarFoto();
		Toast.makeText(getApplicationContext(), 
		          "Foto Capturada.", Toast.LENGTH_SHORT).show();
		return true;
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS){
			tts.speak("Por favor, vire seu smartphone para captura no modo paisagem."
					+ "Toque na tela para ouvir orientações. "
					+ "Mantenha pressionada para realizar captura.", TextToSpeech.QUEUE_FLUSH, null);
		}else if(status == TextToSpeech.ERROR)
			Toast.makeText(getApplicationContext(), "Desculpe! Seu dispositivo não está configurado para utilização do Speach.", Toast.LENGTH_LONG).show();
	}
	
	private float pegaOrientacaoAtePOI(float azimuth) {
		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		currentLoc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (currentLoc != null) {
			GeomagneticField geoField = new GeomagneticField(
			             (float) currentLoc.getLatitude(),
			             (float) currentLoc.getLongitude(),
			             (float) currentLoc.getAltitude(),
			             System.currentTimeMillis());
			
			azimuth += geoField.getDeclination(); // converts magnetic north into true north
			
			Location destino = new Location(LocationManager.GPS_PROVIDER);
			destino.setLatitude(poi.getEndereco().getLatitude());
			destino.setLongitude(poi.getEndereco().getLongitude());
			
			float bearing = currentLoc.bearingTo(destino);  // rolamento aproximado em degrees leste do norte geográfico
			
			return azimuth - bearing;
		}
		return 0;
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			mGravity = event.values;
			if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
				mGeomagnetic = event.values;
			if (mGravity != null && mGeomagnetic != null) {
		      float R[] = new float[9];
		      float I[] = new float[9];
		      boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
		      if (success) {
		        float orientation[] = new float[3];
		        orientation = SensorManager.getOrientation(R, orientation);
		        
		        float azimuth = orientation[0]*360/(2*3.14159f); // orientation contains: azimut, pitch and roll
		        
				float direction = pegaOrientacaoAtePOI(azimuth);
				
				// get the angle around the z-axis rotated
		        float degree = Math.round(azimuth);
		        
		        // create a rotation animation (reverse turn degree degrees)
		        RotateAnimation raCompass = new RotateAnimation(
		                currentDegreeCompass, 
		                (float)-degree + substract,
		                Animation.RELATIVE_TO_SELF, 0.5f, 
		                Animation.RELATIVE_TO_SELF,
		                0.5f);
		        
		        RotateAnimation raArrow = new RotateAnimation(
		                currentDegreeArrow, 
		                (float)-direction + substract,
		                Animation.RELATIVE_TO_SELF, 0.5f, 
		                Animation.RELATIVE_TO_SELF,
		                0.5f);
		        
		        // how long the animation will take place
		        raCompass.setDuration(210);
		        raArrow.setDuration(210);
		        // set the animation after the end of the reservation status
		        raCompass.setFillAfter(true);
		        raArrow.setFillAfter(true);
		        // Start the animation
		        compass.startAnimation(raCompass);
		        seta.startAnimation(raArrow);
		        currentDegreeCompass = -degree + substract;
		        currentDegreeArrow = -direction + substract;
		      }
		    }
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}

}

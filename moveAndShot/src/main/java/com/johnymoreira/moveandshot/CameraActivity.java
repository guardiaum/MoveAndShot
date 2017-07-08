package com.johnymoreira.moveandshot;

import java.io.IOException;
import java.util.Locale;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
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
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.johnymoreira.pojo.PontoDeInteresse;
import com.johnymoreira.utils.CameraPreview;

public class CameraActivity extends Activity implements SensorEventListener, OnInitListener, OnClickListener, OnLongClickListener{
	
	private CameraPreview camera;
	private View compassView;
	private View arrowView;
	private FrameLayout frame;
	private float currentDegreeCompass = 0f;
	private float currentDegreeArrow = 0f;
	private ImageView image;
	private ImageView seta;
	private SensorManager mSensorManager;
	private PontoDeInteresse poi;
	private Location currentLoc;
	private TextToSpeech tts;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
				
		tts = new TextToSpeech(CameraActivity.this,this);
		tts.speak("Por favor, toque na tela para ouvir orientações. "
				+ "Toque e mantenha pressionada para realizar captura.", TextToSpeech.QUEUE_FLUSH, null);
		
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
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
	}
	
	@Override
	protected void onDestroy() {
		if(tts !=null){
			tts.stop();
			tts.shutdown();
		}
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
	    super.onPause();
	    mSensorManager.unregisterListener(this);
	    if (camera != null){
	    	camera.onPause();
	    	camera = null;
	    }
	}
	
	@Override 
	protected void onResume(){
	    super.onResume();
	    mSensorManager.registerListener(this, 
				mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_GAME);
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
	
	public void Load(){
	    Camera c = getCameraInstance();
	    if (c != null){
	    	frame = new FrameLayout(this);
	    	frame.setLayoutParams(new LayoutParams(
	           LayoutParams.MATCH_PARENT,
	           LayoutParams.MATCH_PARENT));
	    	
	    	camera = new CameraPreview(this,c);
	    	
	    	LayoutInflater controlInflater = LayoutInflater.from(getBaseContext());
	    	
	        compassView = controlInflater.inflate(R.layout.inflate_compass_camera, null);
	        compassView.setLayoutParams(new LayoutParams(
	 	           LayoutParams.WRAP_CONTENT,
	 	           LayoutParams.WRAP_CONTENT));
	        
	        
	        arrowView = controlInflater.inflate(R.layout.inflate_arrow_camera, null);
	        arrowView.setLayoutParams(new LayoutParams(
		 	           LayoutParams.WRAP_CONTENT,
		 	           LayoutParams.WRAP_CONTENT));
	        
	        
	        frame.addView(camera);
	        frame.addView(compassView);
	        frame.addView(arrowView);
	        frame.setOnClickListener(this);
	        frame.setOnLongClickListener(this);
	        
	        setContentView(frame);
        	
	    	image = (ImageView) compassView.findViewById(R.id.imageCompass);
	    	seta = (ImageView) arrowView.findViewById(R.id.seta);
	    }
	    else {
	       Toast toast = Toast.makeText(getApplicationContext(), 
	          "Unable to find camera. Closing.", Toast.LENGTH_SHORT);
	       toast.show();
	       finish();
	    }
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
			float azimuth = event.values[0];// get azimuth from the orientation sensor - 
											// angulo em degrees entre o norte magnético e o eixo Y ao redor de Z 
					
			Log.i("orientacao","first azimuth: "+azimuth);
			float direction = pegaOrientacaoAtePOI(azimuth);
			Log.i("orientacao","grau: "+direction);
		
			// get the angle around the z-axis rotated
	        float degree = Math.round(azimuth);

	        //tvHeading.setText("Heading: " + Float.toString(degree) + " degrees");
	        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
				//parameters.set("orientation","portrait");
	        	image.setRotation(0);
	        	seta.setRotation(0);
			}else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
				//parameters.set("orientation","landscape");
				image.setRotation(270);
				seta.setRotation(270);
			}
	        // create a rotation animation (reverse turn degree degrees)
	        RotateAnimation ra = new RotateAnimation(
	                currentDegreeCompass, 
	                -degree,
	                Animation.RELATIVE_TO_SELF, 0.5f, 
	                Animation.RELATIVE_TO_SELF,
	                0.5f);
	        
	        RotateAnimation raArrow = new RotateAnimation(
	                currentDegreeArrow, 
	                -direction,
	                Animation.RELATIVE_TO_SELF, 0.5f, 
	                Animation.RELATIVE_TO_SELF,
	                0.5f);
	        
	        // how long the animation will take place
	        ra.setDuration(210);
	        raArrow.setDuration(200);
	
	        // set the animation after the end of the reservation status
	        ra.setFillAfter(true);
	        raArrow.setFillAfter(true);
	        // Start the animation
	        image.startAnimation(ra);
	        seta.startAnimation(raArrow);
	        
	        currentDegreeCompass = -degree;
	        currentDegreeArrow = -direction;
		}
	}

	private float pegaOrientacaoAtePOI(float azimuth) {
		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		currentLoc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (currentLoc != null) {
			// convert radians to degrees
			//azimuth = (float) Math.toDegrees(azimuth);
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
			
			float direction = azimuth - bearing;
			Log.i("orientacao","Location POI: ("+poi.getEndereco().getLatitude()+", "+poi.getEndereco().getLongitude()+");");
			Log.i("orientacao","Location device: ("+currentLoc.getLatitude()+", "+currentLoc.getLongitude()+");");
			Log.i("orientacao","bearing: "+bearing+" |last azimuth: "+azimuth);
			return direction;
		}
		return 0;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}

	@Override
	public void onInit(int status) {
		if(status == TextToSpeech.ERROR)
			Toast.makeText(getApplicationContext(), "Desculpe! Seu dispositivo não está configurado para utilização do Speach.", Toast.LENGTH_LONG).show();
	}
	
	@Override
	public boolean onLongClick(View v) {
		camera.capturarImagem();
		Toast.makeText(getApplicationContext(), 
		          "Foto Capturada.", Toast.LENGTH_SHORT).show();
		return false;
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
		if(!orientacao.equals(""))
			tts.speak("Vire "+Math.round(currentDegreeArrow)+" graus à sua "+orientacao, TextToSpeech.QUEUE_FLUSH, null);
		
	}
	
}

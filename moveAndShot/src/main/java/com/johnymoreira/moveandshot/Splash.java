package com.johnymoreira.moveandshot;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.widget.Toast;

public class Splash extends Activity implements OnInitListener{
	
	private final int SPLASH_DISPLAY_LENGTH = 3000;
	private TextToSpeech tts;
	private Vibrator v;
	
	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.splash_screen);
        tts = new TextToSpeech(getApplicationContext(), this);
        v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
    }
	
	@Override
	protected void onResume() {
		v.vibrate(100000);
		handleConections();
		super.onResume();
	}
	
	private void handleConections() {
		new Handler().postDelayed(new Runnable() {
        	public void run() {
        		
    			if(!areEnable()){
    				v.cancel();
    				mensagemDeAlerta();
    			}else if(areEnable()){
    				v.cancel();
    				
    				Location location = actualLocation();
    				
    				if(location!=null){
	    				/*Create an Intent that will start the Main Activity. */
	    	            Intent mainIntent = new Intent(Splash.this, MainActivity.class);
	    	            mainIntent.putExtra("latitude", location.getLatitude());
	    	            mainIntent.putExtra("longitude", location.getLongitude());
	    	            Splash.this.startActivity(mainIntent);
	    	            Splash.this.finish();
    				}else{
    					tts.speak("Não foi possível obter sua localização. Ative GPS e acesso à internet e tente novamente.",
    							TextToSpeech.QUEUE_FLUSH, null);
    				}
    			}else{
    				String mensagem = "É necessária conexão com internet e ativação do GPS para utilização do aplicativo.";
    				
    				tts.speak(mensagem,
    						TextToSpeech.QUEUE_FLUSH, null);
    				
    				Toast.makeText(getApplicationContext(), mensagem, Toast.LENGTH_SHORT).show();
    				Splash.this.finish();
    			}
        	}

        }, SPLASH_DISPLAY_LENGTH);
	}
	
	public boolean mensagemDeAlerta() {
		
		String mensagem = "É necessária a ativação do GPS e de alguma "
				+ "conexão com a internet para utilização do aplicativo. "
				+ "Deseja ativar os serviços desabilitados?";
		
		tts.speak(mensagem,
				TextToSpeech.QUEUE_FLUSH, null);
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(Splash.this);
		builder.setMessage(mensagem)
			.setCancelable(false)
			.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
				public void onClick(final DialogInterface dialog,final int id) {
					startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
				}})
			.setNegativeButton("Não", new DialogInterface.OnClickListener() {
				public void onClick(final DialogInterface dialog, final int id) {
					dialog.cancel();
					Splash.this.finish();
				}});
		final AlertDialog alert = builder.create();
		alert.show();
		return false;
	}
	
	private boolean areEnable(){
		boolean internet = isNetworkAvailable();
		boolean gps = isGPSEnable();
		
		/*String internetSituation = "";
		if(!internet){
			internetSituation = "disponível";
		}else{
			internetSituation = "indisponível";
		}
		
		String gpsSituation = "";
		if(!gps){
			gpsSituation = "disponível";
		}else{
			gpsSituation = "indisponível";
		}
		
		Toast.makeText(getApplicationContext(), 
				"GPS:"+gpsSituation+" - Internet:"+internetSituation, 
				Toast.LENGTH_SHORT).show();*/
		
		if(internet==true & gps==true)
			return true;
		return false;
	}
	
	private boolean isGPSEnable(){
		LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}
	
	private boolean isNetworkAvailable() {
		ConnectivityManager cm =
		        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnectedOrConnecting();
	}
	
	public Location actualLocation() {
		LocationManager locationManager = (LocationManager)
				getSystemService(Context.LOCATION_SERVICE);
		Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	    Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	    if (null != locationGPS) { return locationGPS; }
	    if (null != locationNet) { return locationNet; }
		return null;
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.ERROR)
			Toast.makeText(
					getApplicationContext(),
					"Desculpe! Seu dispositivo não está configurado para utilização do Speach.",
					Toast.LENGTH_LONG).show();
	};
	
	@Override
	protected void onDestroy() {
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
		super.onDestroy();
	}
}

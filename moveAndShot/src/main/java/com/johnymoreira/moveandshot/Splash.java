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

/**
 * created by johnymoreira
 *
 * Activity de inicialização do app. Nela são feitas as verificações prévias
 * de serviços e configurações. Caso as configurações não estejam de acordo com
 * o necessário para utilização do aplicativo, o usuário é direcionado para a
 * gerencia de configurações do sistema, a fim de ativá-los.
 */
public class Splash extends Activity implements OnInitListener{
	
	private final int SPLASH_DISPLAY_LENGTH = 3000;
	private TextToSpeech tts;
	private Vibrator v;
	
	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.splash_screen);

		//biblioteca para conversão de texto em áudio
        tts = new TextToSpeech(getApplicationContext(), this);
		//biblioteca para ativar retorno tátil
        v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
    }

	/**
	 * Ao iniciar activity a primeira verificação a ser feita será
	 * a compatibilidade do aparelho com a função de Speech
	 * (conversão de texto em voz)
	 */
	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.ERROR)
			Toast.makeText(
					getApplicationContext(),
					"Desculpe! Seu dispositivo não está configurado para utilização do Speech.",
					Toast.LENGTH_LONG).show();
	};

	/**
	 * Com a destruuuição da activity serão parados
	 * os serviços de utilização do tts (text to speech)
	 */
	@Override
	protected void onDestroy() {
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
		super.onDestroy();
	}

    /**
     * Com a entrada  da activity no estado de onResume
     * será disparado o modo vibratório e serão feitas
     * as devidas verificações através do método {@link Splash#handleConections()}
     */
	@Override
	protected void onResume() {
		v.vibrate(100000);
		handleConections();
		super.onResume();
	}

    /**
     * Verifica configurações necessárias para utilização do aplicativo.
     * Caso não ativadas, o usuário é redirecionado até a gerencia de
     * configurações do sistema.
     */
	private void handleConections() {
		new Handler().postDelayed(new Runnable() {
        	public void run() {

    			if(!areEnable()){ // Internet e GPS não estão ativados

    				v.cancel(); // cancela o vibratório
    				mensagemDeAlerta(); //emite mensagem de alerta

    			}else if(areEnable()){ // Internet e GPS ativados
    				v.cancel();
    				
    				Location location = actualLocation();  // tenta recuperar localização atual do dispositivo
    				
    				if(location!=null){
	    				/*Create an Intent that will start the Main Activity. */
	    	            Intent mainIntent = new Intent(Splash.this, MainActivity.class);
                        //passa as informações de localização capturadas nessa tela para a MainActivity
	    	            mainIntent.putExtra("latitude", location.getLatitude());
	    	            mainIntent.putExtra("longitude", location.getLongitude());
	    	            Splash.this.startActivity(mainIntent);
	    	            Splash.this.finish();
    				}else{
    					tts.speak("Não foi possível obter sua localização. Ative GPS e acesso à internet e tente novamente.",
    							TextToSpeech.QUEUE_FLUSH, null);
    				}
    			}else{ // Por algum problema de conexão, Internet e GPS não estão podendo ser acessados. Finaliza aplicação.
    				String mensagem = "É necessária conexão com internet e ativação do GPS para utilização do aplicativo.";
    				
    				tts.speak(mensagem,
    						TextToSpeech.QUEUE_FLUSH, null);
    				
    				Toast.makeText(getApplicationContext(), mensagem, Toast.LENGTH_SHORT).show();
    				Splash.this.finish();
    			}
        	}

        }, SPLASH_DISPLAY_LENGTH);
	}

    /**
     * Emite mensagem de alerta, indicando os serviços que
     * devem estar ativos para utilização do aplicativo.
     * Usuário pode ativá-los através da opção 'sim' da janela de diálogo
     */
	public void mensagemDeAlerta() {
		
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
	}

    /**
     * Verifica status do GPS e Internet access
     * @return boolean true indicando serviço ativado, caso negativo false
     */
	private boolean areEnable(){
		boolean internet = isNetworkAvailable();
		boolean gps = isGPSEnable();
		
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

}

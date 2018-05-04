package com.johnymoreira.activities;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
public class Splash extends Activity implements OnInitListener, LocationListener {
    private boolean flag;
    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;
    private Location location;
    private TextToSpeech tts;
    private Vibrator v;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.splash_screen);
        this.tts = new TextToSpeech(getApplicationContext(), this);
        this.v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
    }

    /**
     * Ao iniciar activity a primeira verificação a ser feita será
     * a compatibilidade do aparelho com a função de Speech
     * (conversão de texto em voz)
     */
    public void onInit(int status) {
        if (status == -1) {
            Toast.makeText(getApplicationContext(), "Desculpe! Seu dispositivo não está " +
                    "configurado para utilização do Speach.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Com a destruuuição da activity serão parados
     * os serviços de utilização do tts (text to speech)
     */
    protected void onDestroy() {
        if (this.tts != null) {
            this.tts.stop();
            this.tts.shutdown();
        }
        super.onDestroy();
    }

    /**
     * Com a entrada  da activity no estado de onResume
     * será disparado o modo vibratório e serão feitas
     * as devidas verificações através do método {@link Splash#handleConections()}
     */
    protected void onResume() {
        this.v.vibrate(100000);
        handleConections();
        super.onResume();
    }

    /**
     * Verifica configurações necessárias para utilização do aplicativo.
     * Caso não ativadas, o usuário é redirecionado até a gerencia de
     * configurações do sistema.
     */
    private void handleConections() {
        if (!this.flag) {
            this.flag = true;
            new Handler().postDelayed(new VerifyLocationService(), 500);
        }
    }

    /**
     * Emite mensagem de alerta, indicando os serviços que
     * devem estar ativos para utilização do aplicativo.
     * Usuário pode ativá-los através da opção 'sim' da janela de diálogo
     */
    public void mensagemDeAlerta() {
        this.v.cancel();
        String mensagem = "É necessária a ativação do GPS e de alguma conexão com a " +
                "internet para utilização do aplicativo. Deseja ativar os serviços desabilitados?";
        this.tts.speak(mensagem, 0, null);
        Builder builder = new Builder(this);
        builder.setMessage(mensagem)
                .setCancelable(false).setPositiveButton("Sim",
                    new DialogPositiveConfirmationButton()).setNegativeButton("Não",
                    new DialogNegativeConfirmationButton());
        builder.create().show();
    }

    /**
     * Verifica status do GPS e Internet access
     * @return {@link Location} localização atual do dispositivo
     */
    public Location getLocation() {
        try {
            LocationManager locationManager =
                    (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

            this.isGPSEnabled = locationManager.isProviderEnabled("gps");
            this.isNetworkEnabled = locationManager.isProviderEnabled("network");

            if (this.isGPSEnabled && this.isNetworkEnabled) {

                if (this.isGPSEnabled && this.isNetworkEnabled) {

                    if (this.isNetworkEnabled) {

                        locationManager.requestLocationUpdates("network", 1000000, 50.0f, this);

                        if (locationManager != null) {
                            this.location = locationManager.getLastKnownLocation("network");
                        }
                    }
                    if (this.isGPSEnabled && this.location == null) {

                        locationManager.requestLocationUpdates("gps", 1000000, 50.0f, this);
                        if (locationManager != null) {
                            this.location = locationManager.getLastKnownLocation("gps");
                        }
                    }
                }
                return this.location;
            }
            mensagemDeAlerta();
            return this.location;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    class VerifyLocationService implements Runnable {
        VerifyLocationService() {
        }

        public void run() {
            if (Splash.this.getLocation() != null) {
                Splash.this.v.cancel();
                Intent mainIntent = new Intent(Splash.this, MainActivity.class);
                mainIntent.putExtra("latitude", Splash.this.location.getLatitude());
                mainIntent.putExtra("longitude", Splash.this.location.getLongitude());
                Splash.this.startActivity(mainIntent);
                Splash.this.finish();
            }
        }
    }

    class DialogPositiveConfirmationButton implements OnClickListener {
        DialogPositiveConfirmationButton() {
        }

        public void onClick(DialogInterface dialog, int id) {
            Splash.this.flag = false;
            Splash.this.startActivity(new Intent("android.settings.SETTINGS"));
            dialog.cancel();
        }
    }

    class DialogNegativeConfirmationButton implements OnClickListener {
        DialogNegativeConfirmationButton() {
        }

        public void onClick(DialogInterface dialog, int id) {
            dialog.cancel();
            Splash.this.finish();
        }
    }
}

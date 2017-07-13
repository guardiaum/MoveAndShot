package com.johnymoreira.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.model.LatLng;
import com.johnymoreira.pojo.PontoDeInteresse;
import com.johnymoreira.utils.Constants;
import com.johnymoreira.utils.SimplesArrayAdapter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * created by johnymoreira
 *
 * Activity principal da aplicação.
 * Onde será aberta a conexão com o servidor web solicitando os
 * pontos de interesse no raio de distância próximos da localiação
 * atual do aparelho. Os pontos são exibidos em formato de lista
 * onde o usuário pode navegar e aplicar ações como click, click longo e double-click
 */
public class MainActivity extends Activity implements OnItemLongClickListener,
        OnInitListener, OnScrollListener {
    private ProgressDialog dialog;
    private Address enderecoAtual;
    private Geocoder geocoder;
    private double latUser;
    private ListView listview;
    private double lngUser;
    private ArrayList<PontoDeInteresse> pois = new ArrayList();
    private TextToSpeech tts;
    private Vibrator v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // leitura das informações de localização passadas pela Splash screen
        Bundle bd = getIntent().getExtras();
        this.latUser = bd.getDouble("latitude");
        this.lngUser = bd.getDouble("longitude");

        // logs
        Log.i("Lat.", new StringBuilder(String.valueOf(this.latUser)).toString());
        Log.i("Lng.", new StringBuilder(String.valueOf(this.lngUser)).toString());

        // bibliotecas de text to speech e vibrator
        this.tts = new TextToSpeech(getApplicationContext(), this);
        this.v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);

        this.listview = (ListView) findViewById(R.id.lvPOISNasProximidades);

        /*
            cria uma janela de diálogo para carregamento enquanto
            abre conexão com o servidor web e recupera os pontos para exibição.
         */
        this.dialog = new ProgressDialog(this);
        this.dialog.setTitle("Pontos de Interesse");
        this.dialog.setMessage("Carregando POIs. Por favor aguarde...");
        this.dialog.setCancelable(false);
        this.dialog.setCanceledOnTouchOutside(false);
        this.dialog.show();

        // Transforma as coordenadas de localização em endereço fisico
        this.geocoder = new Geocoder(this, Locale.getDefault());
        try {
            this.enderecoAtual = (Address) this.geocoder.getFromLocation(this.latUser, this.lngUser, 1).get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Ao iniciar activity a primeira verificação a ser feita será
     * a compatibilidade do aparelho com a função de Speech
     * (conversão de texto em voz). Após, é aberta a conexão com o servidor web
     * para recuperação das informações necessárias.
     */
    @Override
    public void onInit(int status) {
        if (status == -1) {
            Toast.makeText(this, "Desculpe! Seu dispositivo não está configurado para utilização do Speach.",
                    Toast.LENGTH_LONG).show();
        } else {
            this.tts.speak("Carregando pontos de interesse. Por favor aguarde...", 0, null);
        }
        new ConsomeWS().execute(new String[]{Constants.WS_SERVICE_URL +
                "/pois/getPois?distance=1000&lat_user=" + this.latUser + "&lng_user=" + this.lngUser});
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Com a destruuuição da activity serão parados
     * os serviços de utilização do tts (text to speech)
     */
    @Override
    protected void onDestroy() {
        if (this.tts != null) {
            this.tts.stop();
            this.tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /*
        Ação de clique no botão Mapa. Abre o aplicativo de navegação do Google
        com destino até o ponto de interesse referente
     */
    public void abrirNavegacao(View view) {
        int position = this.listview.getPositionForView(view);

        Intent mapIntent = new Intent("android.intent.action.VIEW",
                Uri.parse("google.navigation:q=" + ((PontoDeInteresse) this.pois.get(position)).getPonto().latitude +
                        "," + ((PontoDeInteresse) this.pois.get(position)).getPonto().longitude));
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    /*
        Ação de clique no botão de Câmera. Abre a activity de Câmera para captura de fotografias
     */
    public void abrirCamera(View view) {
        int position = this.listview.getPositionForView(view);

        Intent it = new Intent(getApplicationContext(), CameraActivity.class);
        it.putExtra("latitude", ((PontoDeInteresse) this.pois.get(position)).getPonto().latitude);
        it.putExtra("longitude", ((PontoDeInteresse) this.pois.get(position)).getPonto().longitude);
        startActivity(it);
    }

    // Evento para captura de longo clique. Leitura do conteúdo do item com a biblioteca TTS
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
        this.tts.speak(new StringBuilder(String.valueOf(((PontoDeInteresse) this.pois.get(position)).getNomePOI())).append(". A ").append(((TextView) view.findViewById(R.id.lblDistanciaPOI)).getText()).toString(), 0, null);
        return true;
    }

    // Evento de scrool na lista
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    /*
        Detecta quando a lista atingir o final e o começo.
        Apresenta leve vibração para indicar o alcance do final da lista e do começo.
     */
    public void onScroll(AbsListView lw, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        switch (lw.getId()) {
            case R.id.lvPOISNasProximidades:
                if ((this.listview.getLastVisiblePosition() == this.listview.getAdapter().getCount() -
                        1 && this.listview.getChildAt(this.listview.getChildCount() - 1).getBottom() <=
                        this.listview.getHeight()) || (firstVisibleItem == 0 &&
                        this.listview.getChildAt(0).getTop() == 0)) {
                    this.v.vibrate(100);
                    return;
                }
                return;
            default:
                return;
        }
    }

    // AsynkTask para consumo do serviço web em background
    private class ConsomeWS extends AsyncTask<String, Void, Void> {
        private String content;
        private String error;

        private ConsomeWS() {
            this.error = null;
        }

        protected void onPreExecute() {
        }

        /**
         * Abertura de conexão com o WS e execução da solicitação
         */
        protected Void doInBackground(String... uri) {
            MalformedURLException e;
            IOException e2;
            Throwable th;
            BufferedReader reader = null;
            try {
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(new URL(uri[0]).openConnection().getInputStream()));
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
                    try {
                        reader2.close();
                        reader = reader2;
                    } catch (Exception ex) {
                        this.error = ex.getMessage();
                        reader = reader2;
                    }
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
            } catch (MalformedURLException e5) {
                e = e5;
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
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

        /**
         * Pós abertura da conexão. Consumo da lista de pontos de interesse recuperados
         */
        @Override
        protected void onPostExecute(Void result) {

            if (this.error != null) {
                Log.e("WS - maspois", this.error);
                Toast.makeText(MainActivity.this.getApplicationContext(),
                        "Desculpe! Ocorreu um erro de comunicação com o WS.", Toast.LENGTH_LONG).show();
                return;
            }

            // Show Response Json On Screen (activity)
            Toast.makeText(MainActivity.this.getApplicationContext(),
                    "Comunicação estabelecida.", Toast.LENGTH_SHORT).show();

            /****************** Start Parse Response JSON Data *************/
            try {
                Log.i("WS Content", this.content);

                /******
                 * Creates a new JSONObject with name/value mappings from
                 * the JSON string.
                 ********/

                /******* Returns null otherwise. *******/
                JSONArray jsonResponse = new JSONArray(this.content);
                Log.i("WS jsonResponse", jsonResponse.toString());

                /*********** Process each JSON Node ************/
                int lengthJsonArr = jsonResponse.length();
                for (int i = 0; i < lengthJsonArr; i++) {
                    /****** Get Object for each JSON node. ***********/
                    JSONObject jsonChildNode = jsonResponse.getJSONObject(i);
                    /******* Fetch node values **********/
                    int id = jsonChildNode.optInt("id");
                    String nomePOI = jsonChildNode.optString("name").toString();
                    String type = jsonChildNode.optString("type").toString();
                    JSONObject poiCoordinateNode = jsonChildNode.getJSONObject("poiCoordinate");
                    Double latitude = Double.valueOf(poiCoordinateNode.optDouble("latitude"));
                    Double longitude = Double.valueOf(poiCoordinateNode.optDouble("longitude"));
                    String img = jsonChildNode.optString("imageAddress");

                    List<Address> addresses = geocoder.getFromLocation(latitude,
                            longitude, 1);

                    Address endereco = enderecoAtual;
                    if( addresses != null && !addresses.isEmpty() )
                        endereco = addresses.get(0);

                    //Cria o objeto PontoDeInteresse e adiciona ao ArrayList correspondent
                    PontoDeInteresse poi = new PontoDeInteresse(id,
                            nomePOI, type, endereco, new LatLng(latitude, longitude),
                            Constants.WS_WEBAPP_URL + "/" + img);

                    MainActivity.this.pois.add(poi);
                }
                /****************** End Parse Response JSON Data *************/

                // Atribui o ArrayList de Pontos de Interesse a um SimpleArrayAdapter
                SimplesArrayAdapter adapter = new SimplesArrayAdapter(
                        getApplicationContext(), MainActivity.this.pois, MainActivity.this.enderecoAtual);

                // Popula o listview da Activity com o SimpleArrayAdapter de POIs
                MainActivity.this.listview.setAdapter(adapter);

                MainActivity.this.dialog.dismiss(); // fecha a janela de diálogo
                MainActivity.this.v.vibrate(500); // breve vibração

                Toast.makeText(MainActivity.this, "Pontos carregados.", Toast.LENGTH_SHORT).show();

                MainActivity.this.tts.speak("Pontos de interesse carregados.", 0, null);

                // Seta eventos de longclick, click e scrool da lista de POIs carregada na activity
                MainActivity.this.listview.setOnItemLongClickListener(MainActivity.this);
                MainActivity.this.listview.setOnScrollListener(MainActivity.this);
                //listview.setOnItemClickListener(MainActivity.this); <- retirado.. seria chamada para MapActivity
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }
}

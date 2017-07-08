package com.johnymoreira.moveandshot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.johnymoreira.pojo.PontoDeInteresse;
import com.johnymoreira.utils.Constants;
import com.johnymoreira.utils.SimplesArrayAdapter;

public class MainActivity extends Activity implements OnItemLongClickListener,
		OnItemClickListener, OnInitListener, OnScrollListener {

	private ProgressDialog dialog;
	private TextToSpeech tts;
	private Geocoder geocoder;
	private Address enderecoAtual;
	private ArrayList<PontoDeInteresse> pois = new ArrayList<PontoDeInteresse>();
	private Vibrator v;
	private ListView listview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tts = new TextToSpeech(getApplicationContext(), this);
		v = (Vibrator) getApplicationContext().getSystemService(
				Context.VIBRATOR_SERVICE);
		listview = (ListView) findViewById(R.id.lvPOISNasProximidades);
		
		Bundle bd = getIntent().getExtras();
		double lat = bd.getDouble("latitude");
		double lng = bd.getDouble("longitude");
		
		this.dialog = new ProgressDialog(this);
		this.dialog.setTitle("Pontos de Interesse");
		this.dialog.setMessage("Carregando POIs. Por favor aguarde...");
		this.dialog.setCancelable(false);
		this.dialog.setCanceledOnTouchOutside(false);
		this.dialog.show();
		
		geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
		try {
			enderecoAtual = geocoder.getFromLocation(lat, lng, 1).get(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		if (tts != null) {
			tts.stop();
			tts.shutdown();
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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent it = new Intent(getApplicationContext(), MapActivity.class);
		it.putExtra("poi_id", pois.get(position).getId()+"");
		it.putExtra("nome_ponto", pois.get(position).getNomePOI().toString());
		it.putExtra("latitude", pois.get(position).getPonto().latitude);
		it.putExtra("longitude", pois.get(position).getPonto().longitude);
		startActivity(it);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		TextView tvDistancia = (TextView) view
				.findViewById(R.id.lblDistanciaPOI);
		tts.speak(
				pois.get(position).getNomePOI() + ". A "
						+ tvDistancia.getText(), TextToSpeech.QUEUE_FLUSH, null);
		return true;
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.ERROR) {
			Toast.makeText(
					MainActivity.this,
					"Desculpe! Seu dispositivo não está configurado para utilização do Speach.",
					Toast.LENGTH_LONG).show();
		}else{
			tts.speak("Carregando pontos de interesse. Por favor aguarde...", TextToSpeech.QUEUE_FLUSH, null);
		}
		
		new ConsomeWS()
		.execute(Constants.WS_SERVICE_URL + "/pois/getPois/");
	}

	private class ConsomeWS extends AsyncTask<String, Void, Void> {

		private String content;
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
						int id = jsonChildNode.optInt("id");
						String nomePOI = jsonChildNode.optString("name")
								.toString();
						String type = jsonChildNode.optString("type")
								.toString();
						Double latitude = jsonChildNode.optDouble("latitude");
						Double longitude = jsonChildNode.optDouble("longitude");
						String img = jsonChildNode.optString("imageAddress");

						Address endereco = geocoder.getFromLocation(latitude,
								longitude, 1).get(0);

						PontoDeInteresse poi = new PontoDeInteresse(id,
								nomePOI, type, endereco, new LatLng(latitude, longitude),
								Constants.WS_WEBAPP_URL + "/" + img);
						
						pois.add(poi);
					}
					/****************** End Parse Response JSON Data *************/
					SimplesArrayAdapter adapter = new SimplesArrayAdapter(
							getApplicationContext(), pois, enderecoAtual);

					listview.setAdapter(adapter);
					
					dialog.dismiss();
					v.vibrate(500);

					Toast.makeText(MainActivity.this, "Pontos carregados.",
							Toast.LENGTH_SHORT).show();

					tts.speak("Pontos de interesse carregados.",
							TextToSpeech.QUEUE_FLUSH, null);
					
					listview.setOnItemLongClickListener(MainActivity.this);
					listview.setOnItemClickListener(MainActivity.this);
					listview.setOnScrollListener(MainActivity.this);
					
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {}

	public void onScroll(AbsListView lw, final int firstVisibleItem,
			final int visibleItemCount, final int totalItemCount) {
		switch (lw.getId()) {
		case R.id.lvPOISNasProximidades:
			if (
					((listview.getLastVisiblePosition() == listview.getAdapter().getCount() - 1) &&
					(listview.getChildAt(listview.getChildCount() - 1).getBottom() <= listview.getHeight()))
					||
					((firstVisibleItem == 0) && 
					(listview.getChildAt(0).getTop() == 0))
					) {
				v.vibrate(100);
			}
		}
	}

}

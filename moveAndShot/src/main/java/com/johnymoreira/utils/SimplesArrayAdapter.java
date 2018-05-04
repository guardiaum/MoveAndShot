package com.johnymoreira.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.maps.model.LatLng;
import com.johnymoreira.activities.CameraActivity;
import com.johnymoreira.activities.MainActivity;
import com.johnymoreira.activities.R;
import com.johnymoreira.pojo.PontoDeInteresse;
import com.squareup.picasso.Picasso;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class SimplesArrayAdapter extends ArrayAdapter<PontoDeInteresse> {
    private final Address atual;
    private final Context context;
    private final ArrayList<PontoDeInteresse> pois;
    private TextToSpeech tts;
    private View layoutInflater;

    public SimplesArrayAdapter(Context context, ArrayList<PontoDeInteresse> enderecos, Address atual) {
        super(context, R.layout.row_layout, enderecos);
        this.context = context;
        this.pois = enderecos;
        this.atual = atual;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = ((LayoutInflater)
                this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).
                inflate(R.layout.row_layout, parent, false);
        layoutInflater = rowView;
        TextView tvNomePOI = (TextView) rowView.findViewById(R.id.lblNomePOI);
        TextView tvDistancia = (TextView) rowView.findViewById(R.id.lblDistanciaPOI);
        TextView tvEnderecoPOI = (TextView) rowView.findViewById(R.id.lblEnderecoPOI);
        ImageView imgItem = (ImageView) rowView.findViewById(R.id.imgItem);

        if (((PontoDeInteresse) this.pois.get(position)).getImgPrincipal().contains("null")) {
            imgItem.setImageResource(R.drawable.ic_launcher);
        } else {
            Picasso.with(this.context).load(((PontoDeInteresse)
                    this.pois.get(position)).getImgPrincipal()).resize(120, 120).into(imgItem);
        }

        tvNomePOI.setText(pois.get(position).getNomePOI());
        tvEnderecoPOI.setText(pois.get(position).getEndereco().getAddressLine(0));

        Double distancia = (getDistancia(new LatLng(atual.getLatitude(), atual.getLongitude()),
                new LatLng(pois.get(position).getEndereco().getLatitude(), pois.get(position).getEndereco().getLongitude())));

        double quilometros = distancia / 1000;
        DecimalFormat df = new DecimalFormat("0.00");
        String str = df.format(quilometros);
        tvDistancia.setText(str +" km");

        /*
           Ação de clique no botão de Câmera. Abre a activity de Câmera para captura de fotografias
        */
        Button btCamera = (Button) rowView.findViewById(R.id.buttonCamera);
        btCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //int position = listview.getPositionForView(v);

                Intent it = new Intent(getContext(), CameraActivity.class);
                it.putExtra("id", pois.get(position).getId());
                Log.i("ID",pois.get(position).getId()+"");
                it.putExtra("nome_ponto", pois.get(position).getNomePOI());
                it.putExtra("latitude", pois.get(position).getPonto().latitude);
                it.putExtra("longitude", pois.get(position).getPonto().longitude);
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                v.getContext().startActivity(it);
            }
        });

        Button btMap = (Button) rowView.findViewById(R.id.buttonMapa);
        btMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapIntent = new Intent("android.intent.action.VIEW",
                        Uri.parse("google.navigation:q=" + ((PontoDeInteresse) pois.get(position)).getPonto().latitude +
                                "," + ((PontoDeInteresse) pois.get(position)).getPonto().longitude));
                mapIntent.setPackage("com.google.android.apps.maps");
                mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                v.getContext().startActivity(mapIntent);
            }
        });

        return rowView;
    }

    public View getLayoutInflater() {
        return layoutInflater;
    }

    public double getDistancia(LatLng StartP, LatLng EndP){
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2-lat1);
        double dLon = Math.toRadians(lon2-lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return 6366000 * c;
    }
}

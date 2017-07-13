package com.johnymoreira.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.maps.model.LatLng;
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

    public SimplesArrayAdapter(Context context, ArrayList<PontoDeInteresse> enderecos, Address atual) {
        super(context, R.layout.row_layout, enderecos);
        this.context = context;
        this.pois = enderecos;
        this.atual = atual;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = ((LayoutInflater)
                this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).
                inflate(R.layout.row_layout, parent, false);

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

        return rowView;
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

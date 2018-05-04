package com.johnymoreira.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.johnymoreira.utils.Constants;
import com.johnymoreira.utils.ImageUploadUtility;
import com.squareup.picasso.Picasso;

import java.io.File;

public class PhotoViewActivity extends AppCompatActivity {

    private File imgFile;
    private int poiId;
    private ProgressDialog simpleWaitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);

        ImageView viewPhoto = (ImageView) findViewById(R.id.viewPhoto);
        TextView textView = (TextView) findViewById(R.id.textView);

        if(getIntent().hasExtra("image_path")) {
            poiId = getIntent().getIntExtra("poi_id",0);
            Log.i("PhotoViewActivity>poiId", poiId + "");
            //poiId = Integer.parseInt(id);

            String path = getIntent().getStringExtra("image_path");
            Log.i("IMAGE PATH", path);
            textView.setText(path);

            imgFile = new  File(path);

            if(imgFile.exists()) {

                Bitmap d = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                int nh = (int) ( d.getHeight() * (512.0 / d.getWidth()) );

                Bitmap scaled = Bitmap.createScaledBitmap(d, 512, nh, true);

                viewPhoto.setImageBitmap(scaled);

            }
        }

    }

    public void deleteImage(View view) {
        String mensagem = "Tem certeza que deseja apagar a imagem capturada?";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(mensagem)
                .setCancelable(false).setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (imgFile.exists()) {
                    if (imgFile.delete()) {
                        Toast.makeText(getApplicationContext(),
                                "Imagem apagada.", Toast.LENGTH_SHORT).show();

                        updateGallery();

                        PhotoViewActivity.this.finish();
                    } else {
                        System.out.println("file not Deleted :" + imgFile.getAbsolutePath());
                    }
                }
            }
        }).setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    private void updateGallery() {
        new MediaScannerConnection(getApplicationContext(),
                new MediaScannerConnection.MediaScannerConnectionClient() {
                    public void onScanCompleted(String path, Uri uri) {}
                    public void onMediaScannerConnected() {}
                });
    }

    public void send2WS(View view) {
        new ImageUploaderTask().execute(
                Constants.WS_SERVICE_URL + "/pois/sendImage/",
                String.valueOf(poiId), imgFile.getAbsolutePath());
    }

    // AsynkTask para consumo do serviço web em background
    private class ImageUploaderTask extends AsyncTask<String, Integer, Void> {
        @Override
        protected void onPreExecute(){
            simpleWaitDialog = ProgressDialog.show(PhotoViewActivity.this, "Wait", "Uploading Image");
        }
        @Override
        protected Void doInBackground(String... params) {
            new ImageUploadUtility().uploadSingleImage(params);
            return null;
        }
        @Override
        protected void onPostExecute(Void result){
            simpleWaitDialog.dismiss();
        }
    }
}

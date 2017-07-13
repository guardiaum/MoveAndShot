package com.johnymoreira.utils;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.location.Location;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraController implements Callback {
    private Activity activity;
    private Camera camera;
    private MediaScannerConnection conn;
    private Context ctx;
    private PictureCallback jpegCallback;
    private PictureCallback rawCallback;
    private Camera.ShutterCallback shutterCallback;
    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView;

    class RawCallback implements PictureCallback {
        RawCallback() {
        }

        public void onPictureTaken(byte[] data, Camera camera) {
        }
    }

    class ShutterCallback implements Camera.ShutterCallback {
        ShutterCallback() {
        }

        public void onShutter() {
        }
    }

    public CameraController(Activity atividade, int surfaceView, Camera c, final Location location) {
        this.activity = atividade;
        this.ctx = atividade.getApplicationContext();
        this.surfaceView = (SurfaceView) atividade.findViewById(surfaceView);
        this.camera = c;
        this.surfaceHolder  = this.surfaceView.getHolder();
        this.surfaceHolder.addCallback(this);
        this.rawCallback = new RawCallback();
        this.shutterCallback = new ShutterCallback();
        this.jpegCallback = new PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {
                FileOutputStream fileOutputStream;
                FileNotFoundException e;
                IOException e2;

                try {
                    File folder = new File(Environment.
                            getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/MoveAndShot");

                    if (!folder.exists()) {
                        folder.mkdir();
                    }

                    final File file = new File(String.format(
                            folder.getAbsolutePath() + "/%d.jpg",
                            new Object[]{Long.valueOf(System.currentTimeMillis())}));
                    FileOutputStream outStream = new FileOutputStream(file.getAbsolutePath());

                    try {
                        outStream.write(data);
                        outStream.flush();
                        outStream.close();
                        CameraController.this.conn =
                                new MediaScannerConnection(CameraController.this.ctx,
                                        new MediaScannerConnectionClient() {

                            public void onScanCompleted(String path, Uri uri) {

                                if (path.equals(file.getAbsolutePath())) {
                                    CameraController.this.conn.disconnect();
                                }
                            }

                            public void onMediaScannerConnected() {
                                CameraController.this.conn.scanFile(file.getAbsolutePath(), null);
                            }
                        });

                        if (CameraController.this.conn != null) {
                            CameraController.this.conn.connect();
                        }

                        ExifInterface exif = new ExifInterface(file.getAbsolutePath());
                        exif.setAttribute("GPSLatitude", GeographicUtils.convert(location.getLatitude()));
                        exif.setAttribute("GPSLongitude", GeographicUtils.convert(location.getLongitude()));
                        exif.setAttribute("GPSLatitudeRef", GeographicUtils.latitudeRef(location.getLatitude()));
                        exif.setAttribute("GPSLongitudeRef", GeographicUtils.longitudeRef(location.getLongitude()));
                        exif.saveAttributes();
                        fileOutputStream = outStream;

                    } catch (FileNotFoundException e3) {
                        e = e3;
                        fileOutputStream = outStream;
                        e.printStackTrace();
                        camera.stopPreview();
                        camera.startPreview();
                    } catch (IOException e4) {
                        e2 = e4;
                        fileOutputStream = outStream;
                        e2.printStackTrace();
                        camera.stopPreview();
                        camera.startPreview();
                    }
                } catch (FileNotFoundException e5) {
                    e = e5;
                    e.printStackTrace();
                    camera.stopPreview();
                    camera.startPreview();
                } catch (IOException e6) {
                    e2 = e6;
                    e2.printStackTrace();
                    camera.stopPreview();
                    camera.startPreview();
                }
                camera.stopPreview();
                camera.startPreview();
            }
        };
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {

            Parameters parameters = this.camera.getParameters();
            parameters.setRotation(90);
            this.camera.setDisplayOrientation(90);
            this.camera.setParameters(parameters);
            this.camera.setPreviewDisplay(holder);
            this.camera.startPreview();

        } catch (IOException e) {
            Log.i("MoveAndShot", "Erro setando o preview da Camera");
            e.printStackTrace();
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        if (this.surfaceHolder.getSurface() != null) {
            try {
                this.camera.stopPreview();
            } catch (Exception e) {
                Log.i("MoveAndShot", "Tentando fechar uma preview não existente");
            }
            try {
                this.camera.setPreviewDisplay(holder);
                this.camera.startPreview();
            } catch (IOException e2) {
                Log.i("MoveAndShot", "Erro setando o preview da Camera");
                e2.printStackTrace();
            }
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        pararVisualizacao();
        this.camera.release();
        this.camera = null;
    }

    public void tirarFoto() {
        this.camera.takePicture(this.shutterCallback, this.rawCallback, this.jpegCallback);
    }

    public void iniciarVisualizacao() {
        this.camera.startPreview();
    }

    public void pararVisualizacao() {
        this.camera.stopPreview();
    }

    public void liberarCamera() {
        this.camera.release();
        this.camera = null;
    }

    public Camera getCameraControler() {
        return this.camera;
    }
}

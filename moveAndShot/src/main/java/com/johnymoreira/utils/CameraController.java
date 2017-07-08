package com.johnymoreira.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraController implements SurfaceHolder.Callback{
	private Context ctx;
	private Activity activity;
	private Camera camera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private PictureCallback rawCallback;
	private ShutterCallback shutterCallback;
	private PictureCallback jpegCallback;
	private MediaScannerConnection conn;
	
    private boolean previewing = false;
 
    public CameraController(Activity atividade, int surfaceView, Camera c) {
    	this.activity = atividade;
        this.surfaceView = (SurfaceView) atividade.findViewById(surfaceView);
        this.camera = c;
        this.ctx = activity.getApplicationContext();
        
        surfaceHolder = this.surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        
        rawCallback = new PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.d("Log", "onPictureTaken - raw");
            }
        };

        /** Handles data for jpeg picture */
        shutterCallback = new ShutterCallback() {
            public void onShutter() {
                Log.i("Log", "onShutter'd");
            }
        };
        
        jpegCallback = new PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {
            	
                FileOutputStream outStream = null;
                try {
                	File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/MoveAndShot");
                	
                	if(!folder.exists())
                		folder.mkdir();
                	
                	final File file = new File(String.format(folder.getAbsolutePath()+"/%d.jpg", System.currentTimeMillis()));
            		
                	outStream = new FileOutputStream(file.getAbsolutePath());
	                outStream.write(data);
	                outStream.close();
                	
	                conn = new MediaScannerConnection(ctx,
	       			     new MediaScannerConnection.MediaScannerConnectionClient() {
	                		@Override
	       			         public void onScanCompleted(String path, Uri uri) {

	       			             if (path.equals(file.getAbsolutePath())) {
	       			                 Log.i("Scan Status", "Completed");
	       			                 Log.i("uri: ", uri.toString());

	       			                 conn.disconnect();
	       			             }
	       			         }
	                		@Override
	       			         public void onMediaScannerConnected() {
	       			             conn.scanFile(file.getAbsolutePath(), null);

	       			         }
	       			     });
	                
	                 if(conn!=null)
	                	 conn.connect();
	                
                    Log.d("Log", "onPictureTaken - wrote bytes: " + data.length);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                }
                Log.d("Log", "onPictureTaken - jpeg");
                camera.stopPreview();
                camera.startPreview();
            }
        };
    }
 
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    	// Surface criada, desenhar o preview
		try {
			Camera.Parameters parameters = camera.getParameters();
			if(activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
				//parameters.set("orientation","portrait");
				camera.setDisplayOrientation(90);
			}else if(activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
				//parameters.set("orientation","landscape");
				camera.setDisplayOrientation(0);
			}
			camera.setParameters(parameters);
			camera.setPreviewDisplay(holder);
			camera.startPreview();
		} catch (IOException e) {
			Log.i("MoveAndShot", "Erro setando o preview da Camera");
			e.printStackTrace();
		}
    }
 
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    	//tratar mudanças na preview ou na orientação (rotação)
		if(surfaceHolder.getSurface()==null){
			return;
		}
		
		//lembrar de parar a preview antes de fazer as modificações
		try{
			camera.stopPreview();
		}catch(Exception e){
			Log.i("MoveAndShot", "Tentando fechar uma preview não existente");
		}
		
		// muda o tamanho da preview ou o redimencionamento, rotação
		// ou outro tipo de mudanças. Inicia a preview com novas configurações
		try {
			Camera.Parameters parameters = camera.getParameters();
			if(activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
				//parameters.set("orientation","portrait");
				camera.setDisplayOrientation(90);
			}else if(activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
				//parameters.set("orientation","landscape");
				camera.setDisplayOrientation(0);
			}
			
			camera.setParameters(parameters);
			camera.setPreviewDisplay(holder);
			camera.startPreview();
		} catch (IOException e) {
			Log.i("MoveAndShot", "Erro setando o preview da Camera");
			e.printStackTrace();
		}
    }
    
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        pararVisualizacao();
        camera.release();
        camera = null;
    }
 
    public void tirarFoto() {
        camera.takePicture(shutterCallback, rawCallback, jpegCallback);
    }
 
    public void iniciarVisualizacao() {
        previewing = true;
        camera.startPreview();
    }
 
    public void pararVisualizacao() {
        camera.stopPreview();
        previewing = false;
    }
    
    public void liberarCamera(){
    	camera.release();
    	camera = null;
    }
 
    public Camera getCameraControler() {
        return camera;
    }
}

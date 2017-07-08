package com.johnymoreira.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements Callback {

	private SurfaceHolder mHolder;
	private Camera mCamera;
	private PictureCallback rawCallback;
	private ShutterCallback shutterCallback;
	private PictureCallback jpegCallback;
	private Context ctx;
	private MediaScannerConnection conn;
	
	public CameraPreview(Context context, Camera camera) {
		super(context);
		this.ctx = context;
		
		this.mCamera = camera;
		
		// Atribuição do Callback de surfaceholder para notifição
		// de criação e destruição do surface view
		mHolder = getHolder();
		mHolder.addCallback(this);
		
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
                mCamera.stopPreview();
                mCamera.startPreview();
            }
        };
		
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// Surface criada, desenhar o preview
		try {
			Camera.Parameters parameters = mCamera.getParameters();
			if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
				//parameters.set("orientation","portrait");
				mCamera.setDisplayOrientation(90);
			}else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
				//parameters.set("orientation","landscape");
				mCamera.setDisplayOrientation(0);
			}
			
			mCamera.setParameters(parameters);
			mCamera.setPreviewDisplay(holder);
			mCamera.startPreview();
		} catch (IOException e) {
			Log.i("MoveAndShot", "Erro setando o preview da Camera");
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		//tratar mudanças na preview ou na orientação (rotação)
		if(mHolder.getSurface()==null){
			return;
		}
		
		//lembrar de parar a preview antes de fazer as modificações
		try{
			mCamera.stopPreview();
		}catch(Exception e){
			Log.i("MoveAndShot", "Tentando fechar uma preview não existente");
		}
		
		// muda o tamanho da preview ou o redimencionamento, rotação
		// ou outro tipo de mudanças. Inicia a preview com novas configurações
		try {
			Camera.Parameters parameters = mCamera.getParameters();
			if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
				//parameters.set("orientation","portrait");
				mCamera.setDisplayOrientation(90);
			}else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
				//parameters.set("orientation","landscape");
				mCamera.setDisplayOrientation(0);
			}
			
			mCamera.setParameters(parameters);
			mCamera.setPreviewDisplay(holder);
			mCamera.startPreview();
		} catch (IOException e) {
			Log.i("MoveAndShot", "Erro setando o preview da Camera");
			e.printStackTrace();
		}
		
	}

	public void onPause(){
		mCamera.release();
		mCamera = null;
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

	}
	
	public void capturarImagem(){
        mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
	}
}

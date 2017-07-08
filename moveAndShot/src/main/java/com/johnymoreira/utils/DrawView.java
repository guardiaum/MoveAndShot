package com.johnymoreira.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.SurfaceView;

public class DrawView extends SurfaceView {
	
	private Paint textPaint = new Paint();
	private String text = "Orientação";
	
	public DrawView(Context context) {
		super(context);
		textPaint.setARGB(255, 200, 0, 0);
		textPaint.setTextSize(60);
		setWillNotDraw(false);
	}
	
	@Override
	protected void onDraw(Canvas canvas){
	    canvas.drawText(this.text, 50, 50, textPaint);
	}
	
	public void setText(String texto){
		this.text = texto;
	}
	
}

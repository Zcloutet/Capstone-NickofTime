package com.example.perfectphotoapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class CameraOverlayView extends View {
    // constsants
    private static final String TAG = "perfectphoto overlay";
    private static final int STROKE_WIDTH = 5;

    // canvas and image dimensions
    private int w;
    private int h;
    private int imagew;
    private int imageh;

    // paint options
    private Paint redPaint;
    private Paint yellowPaint;
    private Paint greenPaint;

    // faces
    private Face[] faces;

    public CameraOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);

        redPaint = new Paint();
        redPaint.setColor(Color.RED);
        redPaint.setStyle(Paint.Style.STROKE);
        redPaint.setStrokeWidth(STROKE_WIDTH);

        yellowPaint = new Paint();
        yellowPaint.setColor(Color.YELLOW);
        yellowPaint.setStyle(Paint.Style.STROKE);
        yellowPaint.setStrokeWidth(STROKE_WIDTH);

        greenPaint = new Paint();
        greenPaint.setColor(Color.RED);
        greenPaint.setStyle(Paint.Style.STROKE);
        greenPaint.setStrokeWidth(STROKE_WIDTH);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.w = w;
        this.h = h;
        Log.v("fuck", String.format("%d by %d",w,h));
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (faces != null) {
            for (int i=0; i<faces.length; i++) {
                canvas.drawRect(faces[i].top*w/imageh, faces[i].left*h/imagew, faces[i].bottom*w/imageh, faces[i].right*h/imagew, redPaint);
            }
        }
    }

    public void updateFaces(Face[] faces, int imagew, int imageh) {
        this.faces = faces;
        this.imagew = imagew;
        this.imageh = imageh;

        invalidate(); // makes it redraw the canvas
    }
}

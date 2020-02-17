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
    final String TAG = "perfectphoto overlay";

    private int w;
    private int h;
    private int imagew;
    private int imageh;

    Paint paint;

    Face[] faces;

    public CameraOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
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
                canvas.drawRect(faces[i].left*w/imagew, faces[i].top*h/imageh, faces[i].right*w/imagew, faces[i].bottom*h/imageh, paint);
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

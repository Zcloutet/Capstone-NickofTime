package com.example.perfectphotoapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class CameraOverlayView extends View {
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
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (faces != null) {
            for (int i=0; i<faces.length; i++) {
                canvas.drawRect(faces[i].left, faces[i].top, faces[i].right, faces[i].bottom, paint);
            }
        }
    }

    public void updateFaces(Face[] faces) {
        this.faces = faces;
        invalidate(); // makes it redraw the canvas
    }
}

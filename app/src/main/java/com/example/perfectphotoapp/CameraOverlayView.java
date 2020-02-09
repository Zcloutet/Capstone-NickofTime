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

    Rect[] boxes;

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

        if (boxes != null) {
            for (int i=0; i<boxes.length; i++) {
                canvas.drawRect(boxes[i], paint);
            }
        }
    }

    public void updateFaces() {
        boxes = new Rect[2];

        boxes[0] = new Rect(100,100,300,400);
        boxes[1] = new Rect(200,200,600,500);

        invalidate(); // makes it redraw the canvas
    }
}

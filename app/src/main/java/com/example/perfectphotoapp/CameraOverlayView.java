package com.example.perfectphotoapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;

public class CameraOverlayView extends View {
    // constsants
    private static final String TAG = "perfectphoto overlay";

    // canvas and image dimensions
    private int w;
    private int h;
    private int imagew;
    private int imageh;
    private int sensorOrientation;

    // paint options
    private int strokeWidth;
    private Paint redPaint;
    private Paint yellowPaint;
    private Paint greenPaint;

    // faces
    private Face[] faces;

    public CameraOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);

        float density = getResources().getDisplayMetrics().density;
        strokeWidth = (int) (2 * density);

        redPaint = new Paint();
        redPaint.setColor(Color.RED);
        redPaint.setStyle(Paint.Style.STROKE);
        redPaint.setStrokeWidth(strokeWidth);

        yellowPaint = new Paint();
        yellowPaint.setColor(Color.YELLOW);
        yellowPaint.setStyle(Paint.Style.STROKE);
        yellowPaint.setStrokeWidth(strokeWidth);

        greenPaint = new Paint();
        greenPaint.setColor(Color.GREEN);
        greenPaint.setStyle(Paint.Style.STROKE);
        greenPaint.setStrokeWidth(strokeWidth);
    }

    // make sure the size of the canvas is always known
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.w = w;
        this.h = h;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    // draw the faces onto the canvas
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (faces != null) {
            for (int i=0; i<faces.length; i++) {
                Paint paint = faces[i].smile ? greenPaint : redPaint;
                Rect rect = imageToCanvas(faces[i].getRect(), w, h, imagew, imageh, sensorOrientation);

                canvas.drawRect(rect, paint);

                drawSmiley(rect.centerX(),rect.bottom+strokeWidth*12, strokeWidth*8, paint, canvas, faces[i].smile);
            }
        }
    }

    // update the faces (and record the image width and height from which they were recognized)
    public void updateFaces(Face[] faces, int imagew, int imageh) {
        this.faces = faces;
        this.imagew = imagew;
        this.imageh = imageh;

        invalidate(); // makes it redraw the canvas
    }

    public void drawSmiley(int x, int y, int r, Paint paint, Canvas canvas, boolean smiling) {
        canvas.drawCircle(x, y, r, paint);
        canvas.drawCircle(x-r/3, y-r/3, strokeWidth/2, paint);
        canvas.drawCircle(x+r/3, y-r/3, strokeWidth/2, paint);

        if (smiling) {
            canvas.drawArc(x-r/2, y-r/2, x+r/2, y+r/2, 30, 120, false, paint);
        }
        else {
            canvas.drawLine(x-r/2,y+r/3, x+r/2, y+r/3, paint);
        }
    }

    // convert a rectangle on the camera image to a rectangle on the canvas
    public static Rect imageToCanvas(Rect r, int w, int h, int imagew, int imageh, int sensorOrientation) {
        Rect r2 = new Rect();

        switch (sensorOrientation) {
            case 0 :
                r2.left = r.left*w/imagew;
                r2.top = r.top*h/imageh;
                r2.right = r.right*w/imagew;
                r2.bottom = r.bottom*h/imageh;
                break;
            case 90 :
                r2.left = r.top*w/imageh;
                r2.top = r.left*h/imagew;
                r2.right = r.bottom*w/imageh;
                r2.bottom = r.right*h/imagew;
                break;
        } // 180 and 270 degree rotation not handled

        return r2;

    }

    // convert a point on the canvas to a point on the camera image
    public static int[] canvasToImage(int x, int y, int w, int h, int imagew, int imageh, int sensorOrientation) {
        int x2 = 0;
        int y2 = 0;

        switch (sensorOrientation) {
            case 0 :
                x2 = x * imagew/w;
                y2 = y * imageh/h;
                break;
            case 90 :
                x2 = y * imagew/h;
                y2 = x * imageh/w;
                break;
        } // 180 and 270 degree rotation not handled

        return new int[] {x2, y2};
    }

    public void updateSensorOrientation(int sensorOrientation) {
        this.sensorOrientation = sensorOrientation;
    }

    /*
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        final int actionMasked = motionEvent.getActionMasked();
        if (actionMasked != MotionEvent.ACTION_DOWN) {
            return false;
        }

        int[] touchCoordinates = canvasToImage((int) motionEvent.getX(), (int) motionEvent.getY());


        return true;
    }
    */
}

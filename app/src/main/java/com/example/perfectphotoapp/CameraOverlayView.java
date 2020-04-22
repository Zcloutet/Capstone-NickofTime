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
    private Paint redTextPaint;
    private Paint blackPaint;

    // faces
    private Face[] faces;
    boolean generalMotionDetected;

    // preferences
    boolean smileDetection = true;
    boolean eyeDetection = true;
    boolean generalMotionDetection = true;
    boolean facialMotionDetection = true;

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

        blackPaint = new Paint();
        blackPaint.setColor(Color.BLACK);
        blackPaint.setStyle(Paint.Style.STROKE);
        blackPaint.setStrokeWidth(strokeWidth);

        redTextPaint = new Paint();
        redTextPaint.setColor(Color.RED);
        redTextPaint.setStyle(Paint.Style.FILL);
        redTextPaint.setTextSize(strokeWidth*10);
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

        int count = 0;

        if (smileDetection) ++count;
        if (eyeDetection) ++count;
        if (facialMotionDetection) ++count;

        if (faces != null) {
            for (int i=0; i<faces.length; i++) {
                int faceCount = 0;

                if (smileDetection && faces[i].smile) {
                    ++faceCount;
                }
                if (eyeDetection && faces[i].eyesOpen) {
                    ++faceCount;
                }
                if (facialMotionDetection && faces[i].noMotion) {
                    ++faceCount;
                }

                Paint paint;
                if (faceCount == count) {
                    paint = greenPaint;
                }
                else if (faceCount == 0) {
                    paint = redPaint;
                }
                else {
                    paint = yellowPaint;
                }

                Rect rect = imageToCanvas(faces[i].getRect(), w, h, imagew, imageh, sensorOrientation);

                canvas.drawRect(rect, paint);

                int iconCount = 0;

                if (smileDetection) {
                    drawSmiley(rect.centerX()+strokeWidth*12*(iconCount*2-count+1),rect.bottom+strokeWidth*12, strokeWidth*8, paint, canvas, faces[i].smile);
                    ++iconCount;
                }
                if (facialMotionDetection) {
                    drawMotion(rect.centerX()+strokeWidth*12*(iconCount*2-count+1),rect.bottom+strokeWidth*12, strokeWidth*8, paint, canvas, faces[i].eyesOpen);
                    ++iconCount;
                }
                if (eyeDetection) {
                    drawEye(rect.centerX()+strokeWidth*12*(iconCount*2-count+1),rect.bottom+strokeWidth*12, strokeWidth*8, paint, canvas, faces[i].eyesOpen);
                    ++iconCount;
                }
            }
        }

        if (generalMotionDetection && generalMotionDetected) {
            Rect bounds = new Rect();
            String motionDetectedString = getContext().getResources().getString(R.string.motion_detected);
            redTextPaint.getTextBounds(motionDetectedString, 0, motionDetectedString.length(), bounds);
            canvas.drawText(motionDetectedString, (w-bounds.width())/2, h*0.85f, redTextPaint);
        }
    }

    // update the faces (and record the image width and height from which they were recognized)
    public void updateFaces(Face[] faces, int imagew, int imageh, boolean generalMotionDetected) {
        this.faces = faces;
        this.imagew = imagew;
        this.imageh = imageh;
        this.generalMotionDetected = generalMotionDetected;

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

    public void drawEye(int x, int y, int r, Paint paint, Canvas canvas, boolean eyesOpen) {
        canvas.drawArc(x-1.4f*r,y-3.73f*r,x+0.4f*r,y+0.27f*r,60,60,false,paint);
        canvas.drawArc(x-0.4f*r,y-3.73f*r,x+1.4f*r,y+0.27f*r,60,60,false,paint);
        if (eyesOpen) {
            canvas.drawArc(x-1.4f*r,y-0.27f*r,x+0.4f*r,y+3.73f*r,240,60,false,paint);
            canvas.drawArc(x-0.4f*r,y-0.27f*r,x+1.4f*r,y+3.73f*r,240,60,false,paint);

            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(x-0.5f*r,y-0.04f*r,strokeWidth,paint);
            canvas.drawCircle(x+0.5f*r,y-0.04f*r,strokeWidth,paint);
            paint.setStyle(Paint.Style.STROKE);
        }
    }

    public void drawMotion(int x, int y, int r, Paint paint, Canvas canvas, boolean noMotion) {
        if (noMotion) {
            canvas.drawCircle(x,y,r*2f/3f,paint);
        }
        else {
            canvas.drawCircle(x+r*2/5,y,r*3/5,paint);

            paint.setAlpha(159);
            canvas.drawCircle(x,y,r*3/5,paint);

            paint.setAlpha(95);
            canvas.drawCircle(x-r*2/5,y,r*3f/5,paint);

            paint.setAlpha(255);
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

    public void updatePreferences(boolean smileDetection, boolean faceDetection, boolean generalMotionDetection, boolean facialMotionDetection) {
        this.smileDetection = smileDetection;
        this.eyeDetection = faceDetection;
        this.generalMotionDetection = generalMotionDetection;
        this.facialMotionDetection = facialMotionDetection;
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
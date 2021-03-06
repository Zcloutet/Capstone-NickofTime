package com.example.perfectphotoapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;

public class CameraOverlayView extends View {
    // constsants
    private static final String TAG = "perfectphoto overlay";
    private static final int PAINT_OPACITY = 255;

    // canvas and image dimensions
    private int w;
    private int h;
    private int imagew;
    private int imageh;
    private int sensorOrientation;

    // paint options
    private int defaultStrokeWidth;
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
    boolean facialTimeout = true;

    public CameraOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);

        float density = getResources().getDisplayMetrics().density;
        defaultStrokeWidth = (int) (2 * density);

        redPaint = new Paint();
        redPaint.setColor(Color.RED);
        redPaint.setStyle(Paint.Style.STROKE);
        redPaint.setStrokeWidth(defaultStrokeWidth);
        redPaint.setAlpha(PAINT_OPACITY);

        yellowPaint = new Paint();
        yellowPaint.setColor(Color.YELLOW);
        yellowPaint.setStyle(Paint.Style.STROKE);
        yellowPaint.setStrokeWidth(defaultStrokeWidth);
        yellowPaint.setAlpha(PAINT_OPACITY);

        greenPaint = new Paint();
        greenPaint.setColor(Color.GREEN);
        greenPaint.setStyle(Paint.Style.STROKE);
        greenPaint.setStrokeWidth(defaultStrokeWidth);
        greenPaint.setAlpha(PAINT_OPACITY);

        blackPaint = new Paint();
        blackPaint.setColor(Color.BLACK);
        blackPaint.setStyle(Paint.Style.STROKE);
        blackPaint.setStrokeWidth(defaultStrokeWidth);
        blackPaint.setAlpha(PAINT_OPACITY);

        redTextPaint = new Paint();
        redTextPaint.setColor(Color.RED);
        redTextPaint.setStyle(Paint.Style.FILL);
        redTextPaint.setTextSize(defaultStrokeWidth*10);
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
            for (Face face : faces) {
                int faceCount = 0;

                if (smileDetection && face.smile) {
                    ++faceCount;
                }
                if (eyeDetection && face.eyesOpen) {
                    ++faceCount;
                }
                if (facialMotionDetection && face.noMotion) {
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

                float timeoutPercentage = 0;

                if ((faceCount == count-1) && facialTimeout) {
                    timeoutPercentage = (float) face.ageUnchanged / MainActivity.FACE_TIMEOUT_AGE;
                    if (timeoutPercentage >= 0.5) {
                        ++count;
                    }
                    if (timeoutPercentage > 1) {
                        paint = greenPaint;
                    }
                }

                RectF rect = imageToCanvas(face.getRect(), w, h, imagew, imageh, sensorOrientation);

                float strokeWidth = rect.width()/90;
                strokeWidth = strokeWidth < defaultStrokeWidth ? strokeWidth : defaultStrokeWidth;
                paint.setStrokeWidth(strokeWidth);

                float cornerLength = (rect.width()+rect.height())/25;
                //canvas.drawRoundRect(rect, curveRadius,curveRadius, paint);
                drawRectangleCorners(rect, cornerLength, paint, canvas);

                int iconCount = 0;

                if (smileDetection) {
                    drawSmiley(rect.centerX()+strokeWidth*10*(iconCount*2-count+1),rect.bottom+strokeWidth*12, strokeWidth*8, paint, canvas, face.smile);
                    ++iconCount;
                }
                if (facialMotionDetection) {
                    drawMotion(rect.centerX()+strokeWidth*10*(iconCount*2-count+1),rect.bottom+strokeWidth*12, strokeWidth*8, paint, canvas, face.noMotion);
                    ++iconCount;
                }
                if (eyeDetection) {
                    drawEye(rect.centerX()+strokeWidth*10*(iconCount*2-count+1),rect.bottom+strokeWidth*12, strokeWidth*8, paint, canvas, face.eyesOpen);
                    ++iconCount;
                }
                if (facialTimeout && timeoutPercentage >= 0.5) {
                    drawTimer(rect.centerX()+strokeWidth*10*(iconCount*2-count+1),rect.bottom+strokeWidth*12, strokeWidth*8, paint, canvas, timeoutPercentage);
                    ++iconCount;
                }

                paint.setStrokeWidth(defaultStrokeWidth);
            }
        }

        if (generalMotionDetection && generalMotionDetected) {
            Rect bounds = new Rect();
            String motionDetectedString = getResources().getString(R.string.motion_detected);
            redTextPaint.getTextBounds(motionDetectedString, 0, motionDetectedString.length(), bounds);
            canvas.drawText(motionDetectedString, (w-bounds.width())/2, h*0.95f, redTextPaint);
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

    public void drawRectangleCorners(RectF rect, float cornerLength, Paint paint, Canvas canvas) {
        canvas.drawLine(rect.left-paint.getStrokeWidth()/2,rect.bottom,rect.left+cornerLength,rect.bottom,paint);
        canvas.drawLine(rect.left,rect.bottom,rect.left,rect.bottom-cornerLength,paint);
        canvas.drawLine(rect.left-paint.getStrokeWidth()/2,rect.top,rect.left+cornerLength,rect.top,paint);
        canvas.drawLine(rect.left,rect.top,rect.left,rect.top+cornerLength,paint);
        canvas.drawLine(rect.right+paint.getStrokeWidth()/2,rect.bottom,rect.right-cornerLength,rect.bottom,paint);
        canvas.drawLine(rect.right,rect.bottom,rect.right,rect.bottom-cornerLength,paint);
        canvas.drawLine(rect.right+paint.getStrokeWidth()/2,rect.top,rect.right-cornerLength,rect.top,paint);
        canvas.drawLine(rect.right,rect.top,rect.right,rect.top+cornerLength,paint);
    }

    public void drawSmiley(float x, float y, float r, Paint paint, Canvas canvas, boolean smiling) {
        canvas.drawCircle(x, y, r, paint);
        canvas.drawCircle(x-r/3, y-r/3, paint.getStrokeWidth()/2, paint);
        canvas.drawCircle(x+r/3, y-r/3, paint.getStrokeWidth()/2, paint);

        if (smiling) {
            canvas.drawArc(x-r/2, y-r/2, x+r/2, y+r/2, 30, 120, false, paint);
        }
        else {
            canvas.drawLine(x-r/2,y+r/3, x+r/2, y+r/3, paint);
        }
    }

    public void drawEye(float x, float y, float r, Paint paint, Canvas canvas, boolean eyesOpen) {
        canvas.drawArc(x-1.4f*r,y-3.73f*r,x+0.4f*r,y+0.27f*r,60,60,false,paint);
        canvas.drawArc(x-0.4f*r,y-3.73f*r,x+1.4f*r,y+0.27f*r,60,60,false,paint);
        if (eyesOpen) {
            canvas.drawArc(x-1.4f*r,y-0.27f*r,x+0.4f*r,y+3.73f*r,240,60,false,paint);
            canvas.drawArc(x-0.4f*r,y-0.27f*r,x+1.4f*r,y+3.73f*r,240,60,false,paint);

            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(x-0.5f*r,y-0.04f*r,paint.getStrokeWidth(),paint);
            canvas.drawCircle(x+0.5f*r,y-0.04f*r,paint.getStrokeWidth(),paint);
            paint.setStyle(Paint.Style.STROKE);
        }
    }

    public void drawMotion(float x, float y, float r, Paint paint, Canvas canvas, boolean noMotion) {
        if (noMotion) {
            canvas.drawCircle(x,y,r*2f/3f,paint);
        }
        else {
            canvas.drawCircle(x+r*2/5,y,r*3/5,paint);

            paint.setAlpha(159);
            canvas.drawCircle(x,y,r*3/5,paint);

            paint.setAlpha(95);
            canvas.drawCircle(x-r*2/5,y,r*3f/5,paint);

            paint.setAlpha(PAINT_OPACITY);
        }
    }

    public void drawTimer(float x, float y, float r, Paint paint, Canvas canvas, float percentage) {
        canvas.drawCircle(x,y,r,paint);
        canvas.drawLine(x,y,x,y-r*2/3,paint);
        if (percentage < 1) {
            canvas.drawLine(x,y,(float) (x+r*2/3*Math.sin(percentage*6.2831853)),(float) (y-r*2/3*Math.cos(percentage*6.2831853)), paint);
        }
    }

    // convert a rectangle on the camera image to a rectangle on the canvas
    public static RectF imageToCanvas(Rect r, int w, int h, int imagew, int imageh, int sensorOrientation) {
        RectF r2 = new RectF();

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

    public void updatePreferences(boolean smileDetection, boolean faceDetection, boolean generalMotionDetection, boolean facialMotionDetection, boolean facialTimeout) {
        this.smileDetection = smileDetection;
        this.eyeDetection = faceDetection;
        this.generalMotionDetection = generalMotionDetection;
        this.facialMotionDetection = facialMotionDetection;
        this.facialTimeout = facialTimeout;
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
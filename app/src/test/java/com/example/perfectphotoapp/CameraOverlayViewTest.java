package com.example.perfectphotoapp;



import android.graphics.Rect;
import android.graphics.RectF;


import org.junit.Test;

import static org.junit.Assert.*;

public class CameraOverlayViewTest {
    Rect r1 = new Rect(10,20,30,40);
    Rect r2 = new Rect(1,200,3,400);
    Rect r3 = new Rect(200,1,400,3);
    Rect r4 = new Rect(20,10,40,30);

    public boolean rectEquals(Rect r1, RectF r2) {
        return (r1.left == r2.left && r1.top == r2.top && r1.right == r2.right && r1.bottom == r2.bottom);
    }

    @Test
    public void imageToCanvas() {

        assertTrue(rectEquals(r1, CameraOverlayView.imageToCanvas(r1, 100, 100, 100, 100, 0)));
        assertTrue(rectEquals(r2, CameraOverlayView.imageToCanvas(r2, 100, 100, 100, 100, 0)));
        assertTrue(rectEquals(r3, CameraOverlayView.imageToCanvas(r3, 100, 100, 100, 100, 0)));
        assertTrue(rectEquals(r4, CameraOverlayView.imageToCanvas(r4, 100, 100, 100, 100, 0)));

        assertTrue(rectEquals(r1, CameraOverlayView.imageToCanvas(r2, 100, 100, 10, 1000, 0)));
        assertTrue(rectEquals(r3, CameraOverlayView.imageToCanvas(r4, 100, 100, 10, 1000, 0)));

        assertTrue(rectEquals(r1, CameraOverlayView.imageToCanvas(r4, 100, 100, 100, 100, 90)));
        assertTrue(rectEquals(r2, CameraOverlayView.imageToCanvas(r3, 100, 100, 100, 100, 90)));

        assertTrue(rectEquals(r1, CameraOverlayView.imageToCanvas(r3, 100, 100, 100, 100, 90)));
        assertTrue(rectEquals(r4, CameraOverlayView.imageToCanvas(r2, 100, 100, 100, 100, 90)));

        assertTrue(rectEquals(new Rect(), CameraOverlayView.imageToCanvas(r1, 100, 100, 100, 100, -10)));
        assertTrue(rectEquals(new Rect(), CameraOverlayView.imageToCanvas(r2, 100, 100, 10, 1000, -10)));

        assertTrue(rectEquals(new Rect(), CameraOverlayView.imageToCanvas(r1, 100, 100, 100, 100, 180)));
        assertTrue(rectEquals(new Rect(), CameraOverlayView.imageToCanvas(r2, 100, 100, 10, 1000, 180)));
    }

    @Test
    public void canvasToImage() {
        assertArrayEquals(new int[] {r1.left, r1.top}, CameraOverlayView.canvasToImage(r1.left, r1.top, 100, 100, 100, 100, 0));
        assertArrayEquals(new int[] {r2.left, r2.top}, CameraOverlayView.canvasToImage(r2.left, r2.top, 100, 100, 100, 100, 0));
        assertArrayEquals(new int[] {r3.left, r3.top}, CameraOverlayView.canvasToImage(r3.left, r3.top, 100, 100, 100, 100, 0));
        assertArrayEquals(new int[] {r4.left, r4.top}, CameraOverlayView.canvasToImage(r4.left, r4.top, 100, 100, 100, 100, 0));

        assertArrayEquals(new int[] {r2.left, r2.top}, CameraOverlayView.canvasToImage(r1.left, r1.top, 100, 100, 10, 1000, 0));
        assertArrayEquals(new int[] {r4.left, r4.top}, CameraOverlayView.canvasToImage(r3.left, r3.top, 100, 100, 10, 1000, 0));

        assertArrayEquals(new int[] {r4.left, r4.top}, CameraOverlayView.canvasToImage(r1.left, r1.top, 100, 100, 100, 100, 90));
        assertArrayEquals(new int[] {r3.left, r3.top}, CameraOverlayView.canvasToImage(r2.left, r2.top, 100, 100, 100, 100, 90));

        assertArrayEquals(new int[] {r3.left, r3.top}, CameraOverlayView.canvasToImage(r1.left, r1.top, 100, 100, 100, 100, 90));
        assertArrayEquals(new int[] {r2.left, r2.top}, CameraOverlayView.canvasToImage(r4.left, r4.top, 100, 100, 100, 100, 90));

        assertArrayEquals(new int[] {0,0}, CameraOverlayView.canvasToImage(r1.left, r1.top, 100, 100, 100, 100, -10));
        assertArrayEquals(new int[] {0,0}, CameraOverlayView.canvasToImage(r2.left, r2.top, 100, 100, 10, 1000, -10));

        assertArrayEquals(new int[] {0,0}, CameraOverlayView.canvasToImage(r1.left, r1.top, 100, 100, 100, 100, 180));
        assertArrayEquals(new int[] {0,0}, CameraOverlayView.canvasToImage(r2.left, r2.top, 100, 100, 10, 1000, 180));
    }
}
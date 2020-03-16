package com.example.perfectphotoapp;

import android.graphics.Rect;

import org.opencv.core.Mat;

public class Face {
    // values boxing in the face
    protected int left;
    protected int top;
    protected int right;
    protected int bottom;
    protected Mat croppedimg;

    public Face(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    //crop face
    public void Crop(Mat aInputFrame, org.opencv.core.Rect rectFace ) //opencv Rect cannot be imported because android.graphics.Rect is already imported
    {
        //this.croppedimg = aInputFrame.submat(rectFace);
        this.croppedimg = new Mat(aInputFrame,rectFace);

    }

    // make a rectangle describing the location of this face
    public Rect getRect() {
        return new Rect(this.left, this.top, this.right, this.bottom);
    }
}

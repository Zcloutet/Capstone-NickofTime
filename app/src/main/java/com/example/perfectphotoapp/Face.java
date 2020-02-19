package com.example.perfectphotoapp;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

public class Face {
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
    };
    //crop face
    public void Crop(Mat aInputFrame, Rect rectFace )
    {
        //this.croppedimg = aInputFrame.submat(rectFace);
        this.croppedimg = new Mat(aInputFrame,rectFace);

    }
}

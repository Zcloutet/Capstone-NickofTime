package com.example.perfectphotoapp;

import android.graphics.Rect;

public class Face {
    // values boxing in the face
    protected int left;
    protected int top;
    protected int right;
    protected int bottom;

    public Face(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    // make a rectangle describing the location of this face
    public Rect getRect() {
        return new Rect(this.left, this.top, this.right, this.bottom);
    }
}

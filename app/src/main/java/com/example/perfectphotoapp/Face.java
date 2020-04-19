package com.example.perfectphotoapp;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Face {
    private static final double FACE_MARGIN_MULTIPLIER = 0.25;

    // values boxing in the face
    protected int left;
    protected int top;
    protected int right;
    protected int bottom;

    protected Mat croppedimg;

    public int age;
    public boolean smile;
    public boolean eyesOpen;
    public boolean motion;

    public Face(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.age = 0;
    }

    // make an android graphics rectangle describing the location of this face
    public android.graphics.Rect getRect() {
        return new android.graphics.Rect(this.left, this.top, this.right, this.bottom);
    }

    // make an opencv rectangle describing the location of this faces
    public org.opencv.core.Rect getRectOpenCV() {
        return new org.opencv.core.Rect(left,top,right-left,bottom-top);
    }

    // FACE MATCHING

    public boolean centerTest(Face face) {
        // if the center of this face is within the other face and vice versa, return true; else, return false
        int cx, cy;

        cx = (this.left + this.right)/2;
        cy = (this.bottom + this.top)/2;

        if (face.left < cx && face.right > cx && face.top < cy && face.bottom > cy) {
            cx = (face.left + face.right)/2;
            cy = (face.bottom + face.top)/2;

            if (this.left < cx && this.right > cx && this.top < cy && this.bottom > cy) return true;
            else return false;
        }
        else return false;
    }

    public static List<Integer> matchFaces(Face f, List<Face> faces) {
        List<Integer> matches = new ArrayList<>();

        for (int i=0; i<faces.size(); ++i) {
            if (f.centerTest(faces.get(i))) {
                matches.add(i);
            }
        }

        return matches;
    }

    public static Face[] compareFaces(Face[] oldFaces, Face[] newFaces, int maxAge) {
        List<Face> combinedFaces = new ArrayList<>(Arrays.asList(newFaces));
        List<Integer> matches;

        for (int i=0; i<oldFaces.length; ++i) {
            matches = matchFaces(oldFaces[i], combinedFaces);
            if (matches.size() == 0) {
                ++oldFaces[i].age;
                if (oldFaces[i].age <= maxAge) {
                    combinedFaces.add(oldFaces[i]);
                }
            }
        }

        return (Face[]) combinedFaces.toArray(new Face[0]);
    }
}

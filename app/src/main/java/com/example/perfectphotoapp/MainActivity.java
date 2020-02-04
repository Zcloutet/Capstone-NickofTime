package com.example.perfectphotoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.content.Context;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.Toast;
import android.widget.ImageView;
import android.media.MediaActionSound;
import android.animation.ValueAnimator;

import android.hardware.camera2.*;

public class MainActivity extends AppCompatActivity {
    // variables referring to the camera
    protected String cameraId;
    protected CameraDevice cameraDevice;

    // tag for logging
    private static final String TAG = "PerfectPhoto";

    // stateCallBack for opening cameras
    // not necessarily important to use but rather than make it null, it is here
    private final CameraDevice.StateCallback stateCallBack = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
        }
    };

    private void takePhoto() {

        // play shutter sound
        Log.i(TAG, "playing shutter sound");
        MediaActionSound mediaActionSound = new MediaActionSound();
        mediaActionSound.play(MediaActionSound.SHUTTER_CLICK);

        // flash screen
        Log.i(TAG, "flashing screen");
        final ImageView flash = findViewById(R.id.imageViewFlash);
        // create animator to make flash pleasant
        ValueAnimator flashAnimator = ValueAnimator.ofInt(255,0);
        flashAnimator.setInterpolator(new AccelerateInterpolator());
        flashAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // update the imageview's alpha to let it fade out
                flash.setImageAlpha((int) animation.getAnimatedValue());
            }
        });
        flashAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                // make it visible when it starts
                flash.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // make it gone to save resources when the animation is over
                flash.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                flash.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        // the duration can be tuned
        flashAnimator.setDuration(700);
        // animate it
        flashAnimator.start();
    }

    private void openCamera() {
        // open camera by getting camera manager and opening the first camera
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = manager.getCameraIdList()[0];

            // check for permission here
            manager.openCamera(cameraId, stateCallBack, null);
        }
        catch (CameraAccessException e) {
            // if there was a problem accessing the camera, let the user know
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"Error opening camera.",Toast.LENGTH_SHORT).show();
        }
        Log.i(TAG, "Camera opened");
    }

    private void closeCamera() {
        // close the camera, if one is open
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        Log.i(TAG, "Camera closed");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.imageViewFlash).setVisibility(View.GONE); // screen starts white if this is not here

        ((CameraOverlayView) findViewById(R.id.cameraOverlayView)).updateFaces();
    }

    @Override
    protected void onResume() {
        super.onResume();
        openCamera(); // open camera whenever the app is opened
    }

    @Override
    protected void onPause() {
        closeCamera(); // close camera whenever the app is no longer open
        super.onPause();
    }
}

package com.example.perfectphotoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;

import android.util.Log;
import android.widget.Toast;

import android.hardware.camera2.*;

public class MainActivity extends AppCompatActivity {
    protected String cameraId;
    protected CameraDevice cameraDevice;

    private static final String TAG = "PerfectPhoto MainActivity";

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

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = manager.getCameraIdList()[0];

            manager.openCamera(cameraId, stateCallBack, null);
        }
        catch (CameraAccessException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"Error opening camera.",Toast.LENGTH_SHORT).show();
        }
        Log.i(TAG, "Camera opened");
    }

    private void closeCamera() {
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        openCamera();
    }

    @Override
    protected void onPause() {
        closeCamera();
        super.onPause();
    }
}

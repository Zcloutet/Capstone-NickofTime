package com.example.perfectphotoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;

import android.hardware.camera2.*;

public class MainActivity extends AppCompatActivity {
    protected String cameraId;
    protected CameraDevice cameraDevice;

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
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}

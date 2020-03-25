package com.example.perfectphotoapp;

import android.Manifest;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaActionSound;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.util.SparseIntArray;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ImageView;
import android.media.MediaActionSound;
import android.animation.ValueAnimator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.view.TextureView;
import android.util.Size;
import android.view.Surface;
import android.hardware.camera2.CaptureRequest;

import java.nio.ByteBuffer;
import java.util.Arrays;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.graphics.ImageFormat;
import android.graphics.Bitmap;
import android.hardware.camera2.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
//import org.opencv.android.
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
//import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;

//import org.opencv.android.
//import org.opencv.core.Mat;

public class MainActivity extends AppCompatActivity {
    // constants
    private static final int CAMERA_PERMISSION_CODE = 1;
    private static final int IMAGE_BUFFER_SIZE = 1;
    private static final double FACE_MARGIN_MULTIPLIER = 0.25;
    private static final String TAG = "PerfectPhoto"; // log tag
    private static final int FRAME_PROCESS_NUMBER = 3;
    private static final int MAX_FACE_AGE = 3;

    CameraManager manager;
    HandlerThread mBackgroundThread;
    HandlerThread openCVThread;
    // variables referring to the camera
    private int cameraIndex = 1;
    protected String cameraId;
    protected CaptureRequest captureRequest;
    protected CameraDevice cameraDevice;
    private Size imageDimension;
    protected CaptureRequest.Builder captureRequestBuilder;
    protected CameraCaptureSession cameraCaptureSessions;
    private Handler mBackgroundHandler;
    private Handler openCVHandler;
    private TextureView textureView;
    private CascadeClassifier faceCascadeClassifier;
    private CascadeClassifier smileCascadeClassifier;
    private CascadeClassifier eyeCascadeClassifier;
    private Mat grayscaleImage;
    private int absoluteFaceSize;
    private boolean flash = false;
    private ImageReader opencvImageReader,captureImageReader;
    private boolean hasFlash ;
    private int frameCount = 0;
    private Face[] faces = {};

    private ImageView widthCapturer;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    // APP HANDLING

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.imageViewFlash).setVisibility(View.GONE); // screen starts white if this is not here

        // textureView
        textureView = (TextureView) findViewById(R.id.texture);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);

        // take photo button
        ImageButton buttonRequest = findViewById(R.id.button);
        ImageButton gallery = findViewById(R.id.gallery);
        final ImageButton btnFlash = findViewById(R.id.flash);
        btnFlash.setColorFilter(Color.argb(255, 0, 0, 0)); // White Tint


        btnFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!hasFlash){
                    Toast.makeText(MainActivity.this, "Flash is not available.", Toast.LENGTH_SHORT).show();
                    return;
                }
                flash = !flash;

                if(flash){

                    btnFlash.setColorFilter(Color.argb(255, 255, 255, 255)); // White Tint
                    Toast.makeText(MainActivity.this, "Flash has been turned on.", Toast.LENGTH_SHORT).show();
                }else{
                    btnFlash.setColorFilter(Color.argb(255, 0, 0, 0)); // White Tint
                    Toast.makeText(MainActivity.this, "Flash has been turned off.", Toast.LENGTH_SHORT).show();
                }


            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();

                startActivity(new Intent(MainActivity.this,GalleryActivity.class));
//                finish();
            }
        });
        buttonRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        // switch camera button
        ImageButton switchButton = findViewById(R.id.switchButton);
        switchButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(cameraIndex == 1){
                    cameraIndex = 0;
                }else{
                    cameraIndex = 1;
                }
                closeCamera();
                if (textureView.isAvailable()) {
                    openCamera(cameraIndex);
                } else {
                    textureView.setSurfaceTextureListener(textureListener);
                }
            }
        });

        captureImageReader = ImageReader.newInstance(400,800,ImageFormat.JPEG,1);
        captureImageReader.setOnImageAvailableListener(captureImageAvailableListener,mBackgroundHandler);

        ImageButton settings = findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSettingsPage();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (textureView.isAvailable()) {
            openCamera(cameraIndex);
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }

        OpenCVLoader.initDebug();
        initializeOpenCVDependencies();
        startBackgroundThread();
        //OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
    }

    @Override
    protected void onPause() {
        closeCamera(); // close camera whenever the app is no longer open
        stopBackgroundThread();

        super.onPause();
    }

    //start necessary handlers to handle intensive tasks of camera and opencv
    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        openCVThread = new HandlerThread("OPEN CV");
        mBackgroundThread.start();
        openCVThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        openCVHandler = new Handler(openCVThread.getLooper());
    }

    //stop handlers on pause
    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        openCVThread.quitSafely();
        try {
            mBackgroundThread.join();
            openCVThread.join();
            mBackgroundThread = null;
            openCVThread = null;
            mBackgroundHandler = null;
            openCVHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void openSettingsPage(){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }


    // CAMERA HANDLING

//open camera and create capturesession
    private void openCamera(int cameraIndex) {
        // open camera by getting camera manager and opening the first camera
        manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = manager.getCameraIdList()[cameraIndex];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            if(characteristics == null){
                throw new NullPointerException("No camera with id "+ cameraIndex);
            }
            hasFlash =  characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];

            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestCameraPermission();
            }
            else {
                manager.openCamera(cameraId, stateCallBack, mBackgroundHandler);
            }

//            int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
//            int totalRotation = sensorToDeviceRotation(characteristics, deviceOrientation);
//            boolean swapRotation = totalRotation == 90 || totalRotation == 270;
//            int rotatedWidth = widthCapturer.getWidth();
//            int rotatedHeight = widthCapturer.getHeight();
//            if(swapRotation){
//                rotatedWidth = widthCapturer.getHeight();
//                rotatedHeight = widthCapturer.getWidth();
//            }
        }
        catch (CameraAccessException e) {
            // if there was a problem accessing the camera, let the user know
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"Error opening camera.",Toast.LENGTH_SHORT).show();
        }
        Log.i(TAG, "Camera opened");
    }

    //close camera
    private void closeCamera() {
        // close the camera, if one is open
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        Log.i(TAG, "Camera closed");
    }

    public void setOrientations(Context context, int cameraId){
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try{
            String[] cameraIds = manager.getCameraIdList();

            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraIds[cameraId]);
            int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
            int orientation = (sensorOrientation + deviceOrientation);
            orientation = (360 - orientation+180) % 360;
            ((CameraOverlayView) findViewById(R.id.cameraOverlayView)).updateSensorOrientation(sensorOrientation);

//            if(deviceOrientation == 0){
//                orientation = 90;
//            }else if(deviceOrientation == 3){
//                orientation = 180;
//            }
            Log.i(TAG, "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^SENSOR ORIENTATION "+sensorOrientation);
            Log.i(TAG, "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEVICE ORIENTATION "+deviceOrientation);
            Log.i(TAG, "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ORIENTATION "+orientation);
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(orientation));
        }catch(CameraAccessException e){
            Log.i(TAG, "ERROR ACCESSING CAMERA "+e);
        }
    }

    private static int sensorToDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation){
        int sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation = ORIENTATIONS.get(deviceOrientation);
        return (sensorOrientation + deviceOrientation + 360) % 360;
    }

    // stateCallBack for opening cameras
    private final CameraDevice.StateCallback stateCallBack = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
            setOrientations(MainActivity.this,cameraIndex);
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
        try {
            captureImage();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error :"+ e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //capture image
    private void captureImage() throws Exception {

        CaptureRequest.Builder request = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);

        request.addTarget(captureImageReader.getSurface());
        if(hasFlash && flash)
            request.set(CaptureRequest.FLASH_MODE,CaptureRequest.FLASH_MODE_SINGLE);
        else{
            request.set(CaptureRequest.FLASH_MODE,CaptureRequest.FLASH_MODE_OFF);

        }

        cameraCaptureSessions.capture(request.build(),null,mBackgroundHandler);
    }

    //function to save captured image to internal storage
    private String saveToInternalStorage(Image image) throws  Exception{
        byte[] data = null;
        if (image.getFormat() == ImageFormat.JPEG) {
            Image.Plane[] planes = image.getPlanes();
            ByteBuffer buffer = planes[0].getBuffer();
            data = new byte[buffer.capacity()];
            buffer.get(data);
        }
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("images", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"image_"+ new Date().getTime() +".jpg");

        FileOutputStream fos = new FileOutputStream(mypath);
        // Use the compress method on the BitMap object to write image to the OutputStream
        if(data!=null && data.length>0)
            fos.write(data);

        fos.close();
        return mypath.getPath();
    }

    public int getCameraIndex(){
        return cameraIndex;
    }

    private ImageReader.OnImageAvailableListener captureImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            Image image = imageReader.acquireLatestImage();
            if(image != null){
                try {
                    saveToInternalStorage(image);
                    Toast.makeText(MainActivity.this, "Image has been saved.", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Failed to save image", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }else{
                Toast.makeText(MainActivity.this, "Unknown error has occurred", Toast.LENGTH_SHORT).show();
            }
            image.close();
//            imageReader.close();
        }
    };


    // PERMISSION HANDLING

    private void requestCameraPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)){

            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed for the app")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA},CAMERA_PERMISSION_CODE );
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();

        } else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},CAMERA_PERMISSION_CODE );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == CAMERA_PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    // CAMERA PREVIEW

    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface textureSurface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(textureSurface);

            opencvImageReader = ImageReader.newInstance(imageDimension.getWidth(), imageDimension.getHeight(), ImageFormat.YUV_420_888, IMAGE_BUFFER_SIZE);
            opencvImageReader.setOnImageAvailableListener(mOnOpenCVImageAvailableListener,  openCVHandler);
            Surface openCvSurface = opencvImageReader.getSurface();
            captureRequestBuilder.addTarget(openCvSurface);

            cameraDevice.createCaptureSession(Arrays.asList(new Surface[] {textureSurface,openCvSurface,captureImageReader.getSurface()}), new CameraCaptureSession.StateCallback(){
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;

                    updatePreview();
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(MainActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void updatePreview() {
        if(null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }

        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //open your camera here
            openCamera(cameraIndex);
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };


    // OPENCV FACIAL RECOGNITION

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    initializeOpenCVDependencies();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    private final ImageReader.OnImageAvailableListener mOnOpenCVImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            if (++frameCount % FRAME_PROCESS_NUMBER != 0) {
                reader.acquireLatestImage().close();
            }
            else {
                Image image = null;
                try {
                    image = reader.acquireLatestImage();
                    if (image != null) {
                        //new conversion
                        byte[] nv21;
                        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
                        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
                        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();
                        int ySize = yBuffer.remaining();
                        int uSize = uBuffer.remaining();
                        int vSize = vBuffer.remaining();
                        nv21 = new byte[ySize + uSize + vSize];
                        //U and V are swapped
                        yBuffer.get(nv21, 0, ySize);
                        vBuffer.get(nv21, ySize, vSize);
                        uBuffer.get(nv21, ySize + vSize, uSize);

                        Mat mRGB = getYUV2Mat(image,nv21);
                        //end conversion
                        Mat bgrMat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC4);
                        grayscaleImage = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC4);
                        // The faces will be a 20% of the height of the screen
                        absoluteFaceSize = (int) (image.getHeight() * 0.20);

                        //Imgproc.cvtColor(mRGB, bgrMat, Imgproc.COLOR_YUV2BGR_I420);
                        Face[] newFaces = Cascadeframe(mRGB);
                        faces = Face.compareFaces(faces, newFaces, MAX_FACE_AGE);
                        ((CameraOverlayView) findViewById(R.id.cameraOverlayView)).updateFaces(faces, image.getWidth(), image.getHeight());
                    }
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                } finally {
                    image.close();
                }
            }
        }
    };

    private void initializeOpenCVDependencies() {
        faceCascadeClassifier = openCascadeClassifier(R.raw.lbpcascade_frontalface, "lbpcascade_frontalface.xml");
        smileCascadeClassifier = openCascadeClassifier(R.raw.haarcascade_smile, "haarcascade_smile.xml");
        eyeCascadeClassifier = openCascadeClassifier(R.raw.haarcascade_eye, "haarcascade_eye.xml");

        // And we are ready to go
        //openCvCameraView.enableView();
    }

    private CascadeClassifier openCascadeClassifier(int value, String name) {
        try {
            // Copy the resource into a temp file so OpenCV can load it
            InputStream is = getResources().openRawResource(value);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, name);
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();
            // Load the cascade classifier
            return new CascadeClassifier(mCascadeFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e("OpenCVActivity", "Error loading cascade", e);
            return null;
        }
    }

    public Face[] Cascadeframe(Mat aInputFrame) {
        // Create a grayscale image
        Imgproc.cvtColor(aInputFrame, grayscaleImage, Imgproc.COLOR_RGBA2RGB);
        MatOfRect faces = new MatOfRect();

        // Use the classifier to detect faces

        if (faceCascadeClassifier != null) {
            faceCascadeClassifier.detectMultiScale(grayscaleImage, faces, 1.1, 3, 2,
                    new org.opencv.core.Size(absoluteFaceSize, absoluteFaceSize), new org.opencv.core.Size());
        }
        // If any faces found, draw a rectangle around it
        Rect[] rectFacesArray = faces.toArray();
        Face[] facesArray = new Face[rectFacesArray.length];
        for (int i = 0; i <rectFacesArray.length; i++) {
            Rect rectFace = rectFacesArray[i];
            facesArray[i] = new Face((int) (rectFace.x-rectFace.width*FACE_MARGIN_MULTIPLIER), (int) (rectFace.y-rectFace.height*FACE_MARGIN_MULTIPLIER), (int) (rectFace.x+rectFace.width*(1+FACE_MARGIN_MULTIPLIER)), (int) (rectFace.y+rectFace.height*(1+FACE_MARGIN_MULTIPLIER)));

            //crops face by making a submat which is stored in face instance
            facesArray[i].Crop(aInputFrame,rectFace);
            //Log.i(TAG, "cropped" +(facesArray[i].croppedimg));
            //Imgcodecs.imwrite("C:/Cropped/"+String.valueOf(System.currentTimeMillis()) + ".bmp", facesArray[i].croppedimg);


            // smile detection
            MatOfRect smile = new MatOfRect();

            if (smileCascadeClassifier != null) {
                smileCascadeClassifier.detectMultiScale(facesArray[i].croppedimg, smile, 1.6, 20);
            }

            if (smile.toArray().length == 0) {
                facesArray[i].smile = false;
            }
            else {
                facesArray[i].smile = true;
            }

            // eye detection
            MatOfRect eyes = new MatOfRect();

            if(eyeCascadeClassifier != null) {
                eyeCascadeClassifier.detectMultiScale(facesArray[i].croppedimg, eyes, 1.2, 6);
            }

            Log.w("num eyes", String.format("%d",eyes.toArray().length));
        }
        return facesArray;
    }

    /*
    private Mat StoreFaces(Face[] faces)
    {
        Mat source = face

        // Setup a rectangle to define your region of interest
        //Rect myROI(10, 10, 100, 100);

        // Crop the full image to that image contained by the rectangle myROI
        // Note that this doesn't copy the data
        //Mat croppedRef(source, myROI);

        Mat cropped;
        // Copy the data into new matrix
        croppedRef.copyTo(cropped);
    }

     */
    public Mat getYUV2Mat(Image image,byte[] data) {
        Mat mYuv = new Mat(image.getHeight() + image.getHeight() / 2, image.getWidth(), CvType.CV_8UC1);
        mYuv.put(0, 0, data);
        Mat mRGB = new Mat();
        Imgproc.cvtColor(mYuv, mRGB, Imgproc.COLOR_YUV2RGB_NV21, 3);
        return mRGB;
    }

}

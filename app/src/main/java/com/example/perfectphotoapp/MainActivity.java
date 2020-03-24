package com.example.perfectphotoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.SurfaceTexture;
import android.animation.Animator;
import android.content.Context;
import android.media.Image;
import android.os.Bundle;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ImageView;
import android.media.MediaActionSound;
import android.animation.ValueAnimator;

import androidx.appcompat.app.AlertDialog;
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

public class MainActivity extends AppCompatActivity {
    // constants
    private static final int CAMERA_PERMISSION_CODE = 1;
    private static final int IMAGE_BUFFER_SIZE = 1;
    private static final double FACE_MARGIN_MULTIPLIER = 0.25;
    private static final String TAG = "PerfectPhoto"; // log tag
    private static final int FRAME_PROCESS_NUMBER = 3;
    private static final int MAX_FACE_AGE = 3;

    // variables referring to the camera
    private int cameraIndex = 0;
    protected String cameraId;
    protected CameraDevice cameraDevice;
    private Size imageDimension;
    protected CaptureRequest.Builder captureRequestBuilder;
    protected CameraCaptureSession cameraCaptureSessions;
    private Handler mBackgroundHandler;
    private TextureView textureView;
    private CascadeClassifier faceCascadeClassifier;
    private CascadeClassifier smileCascadeClassifier;
    private Mat grayscaleImage;
    private int absoluteFaceSize;
    private ImageReader imageReader;
    private int frameCount = 0;
    private Face[] faces = {};


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
                openCamera(cameraIndex);
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
        //OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
    }

    @Override
    protected void onPause() {
        closeCamera(); // close camera whenever the app is no longer open
        super.onPause();
    }


    // CAMERA HANDLING

    private void openCamera(int cameraIndex) {
        // open camera by getting camera manager and opening the first camera
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = manager.getCameraIdList()[cameraIndex];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            if(characteristics == null){
                throw new NullPointerException("No camera with id "+ cameraIndex);
            }
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];


            ((CameraOverlayView) findViewById(R.id.cameraOverlayView)).updateSensorOrientation(characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION));

            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestCameraPermission();
            }
            else {
                manager.openCamera(cameraId, stateCallBack, null);
            }
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

    // stateCallBack for opening cameras
    private final CameraDevice.StateCallback stateCallBack = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
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

            imageReader = ImageReader.newInstance(imageDimension.getWidth(), imageDimension.getHeight(), ImageFormat.YUV_420_888, IMAGE_BUFFER_SIZE);
            imageReader.setOnImageAvailableListener(mOnImageAvailableListener, null);
            Surface imageReaderSurface = imageReader.getSurface();
            captureRequestBuilder.addTarget(imageReaderSurface);

            cameraDevice.createCaptureSession(Arrays.asList(new Surface[] {textureSurface, imageReaderSurface}), new CameraCaptureSession.StateCallback(){
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
            }, null);
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

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
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
                        Mat mYuvMat = imageToMat(image);
                        Mat bgrMat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC4);
                        grayscaleImage = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC4);
                        // The faces will be a 20% of the height of the screen
                        absoluteFaceSize = (int) (image.getHeight() * 0.20);
                        Imgproc.cvtColor(mYuvMat, bgrMat, Imgproc.COLOR_YUV2BGR_I420);
                        Face[] newFaces = Cascadeframe(bgrMat);
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
            faceCascadeClassifier.detectMultiScale(grayscaleImage, faces, 1.1, 2, 2,
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

    public static Mat imageToMat(Image image) {
        ByteBuffer buffer;
        int rowStride;
        int pixelStride;
        int width = image.getWidth();
        int height = image.getHeight();
        int offset = 0;

        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[image.getWidth() * image.getHeight() * ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];

        for (int i = 0; i < planes.length; i++) {
            buffer = planes[i].getBuffer();
            rowStride = planes[i].getRowStride();
            pixelStride = planes[i].getPixelStride();
            int w = (i == 0) ? width : width / 2;
            int h = (i == 0) ? height : height / 2;
            for (int row = 0; row < h; row++) {
                int bytesPerPixel = ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8;
                if (pixelStride == bytesPerPixel) {
                    int length = w * bytesPerPixel;
                    buffer.get(data, offset, length);

                    if (h - row != 1) {
                        buffer.position(buffer.position() + rowStride - length);
                    }
                    offset += length;
                } else {


                    if (h - row == 1) {
                        buffer.get(rowData, 0, width - pixelStride + 1);
                    } else {
                        buffer.get(rowData, 0, rowStride);
                    }

                    for (int col = 0; col < w; col++) {
                        data[offset++] = rowData[col * pixelStride];
                    }
                }
            }
        }

        Mat mat = new Mat(height + height / 2, width, CvType.CV_8UC1);
        mat.put(0, 0, data);

        return mat;
    }
}

package com.example.perfectphotoapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.Environment;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Date;

import static android.widget.Toast.makeText;

public class GalleryActivity extends AppCompatActivity {

    ImageView selectedImage;
    File[] images;
    File currentImage;
    int currentIndex = 0;
    int totalImages = 0;
    ViewGroup transition;
    final int duration = 1500;
    Transition slideLeft = new Slide(Gravity.LEFT);
    Transition slideRight= new Slide(Gravity.RIGHT);


    private Uri imageUri;
    private Intent intent;

    private void initialize(){


        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("images", Context.MODE_PRIVATE);
        File[] temp = directory.listFiles();
        images = new File[temp.length];
        int j=0;
        for(int i=0;i<temp.length;i++){
            images[j]= temp[i];
            j++;
        }

        currentIndex = getIntent().getIntExtra("IMAGE_ID",0);

        totalImages = images.length;
        final ImageButton go_back = findViewById(R.id.go_back);

        go_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(GalleryActivity.this, "clicked", Toast.LENGTH_LONG).show();
                goBack();
            }
        });
    }


    float x1,x2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_gallery);


        selectedImage= findViewById(R.id.imageView);
        transition = findViewById(R.id.trans_container);

//        Button btnNext,btnPrevious,shareButton;
        ImageButton btnDelete, shareButton,btnInfo,btnExport;



        selectedImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
//                Toast.makeText(GalleryActivity.this, "lol", Toast.LENGTH_SHORT).show();

                final int MIN_DISTANCE = 150;
                switch(event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        x1 = event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        x2 = event.getX();
                        float deltaX = x2 - x1;

                        if (Math.abs(deltaX) > MIN_DISTANCE)
                        {
                            // Left to Right swipe action
                            if (x2 > x1)
                            {
                                doPrevious();

//                                Toast.makeText(this, "Left to Right swipe [Next]", Toast.LENGTH_SHORT).show ();
                            }

                            // Right to left swipe action
                            else
                            {
                                doNext();
//                                Toast.makeText(this, "Right to Left swipe [Previous]", Toast.LENGTH_SHORT).show ();
                            }

                        }

                        break;
                }
                return true;
            }
        });

//        btnNext = findViewById(R.id.next);
//        btnPrevious = findViewById(R.id.previous);
        btnDelete = findViewById(R.id.delete);
        shareButton = findViewById(R.id.share);
        btnInfo = findViewById(R.id.btnInfo);

        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder b = new AlertDialog.Builder(GalleryActivity.this);
                b.setTitle("Info");
                b.setMessage("Created at: "+ timeStamp.toLocaleString());
                b.show();
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(totalImages == 0 ||currentImage==null){
                    return;
                }
                Intent myIntent = new Intent(Intent.ACTION_SEND);
                myIntent.setType("*/*");
                myIntent.putExtra(Intent.EXTRA_STREAM, currentImage);
                startActivity(Intent.createChooser(myIntent, "Share Using"));
            }
        });

        btnExport = findViewById(R.id.btnExport);
        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleExport();
            }
        });


        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(totalImages == 0 ||currentImage==null){
                    return;
                }
                if (currentImage.delete()){
                    //Toast.makeText(GalleryActivity.this, "Successfully Deleted", Toast.LENGTH_SHORT).show();
                    initialize();
                    currentIndex = 0;
                    loadImage();
                }else{
                    makeText(GalleryActivity.this, R.string.failed_to_delete, Toast.LENGTH_SHORT).show();
                }
            }
        });


        slideLeft.setDuration(duration);
        slideRight.setDuration(duration);
    }

    public void goBack(){
        Intent intent = new Intent(this, GalleryList.class);
        startActivity(intent);
    }

    private void doNext(){

        selectedImage.setVisibility(View.INVISIBLE);

        if(totalImages == 0){
            currentIndex = 0;
            loadImage();
            return;
        }
        currentIndex = (currentIndex+1)% totalImages;
        TransitionManager.endTransitions(transition);
        TransitionManager.beginDelayedTransition(transition, new Slide(Gravity.RIGHT));
        loadImage();
        selectedImage.setVisibility(View.VISIBLE);
    }


    private void doPrevious(){
        selectedImage.setVisibility(View.INVISIBLE);

        currentIndex = (currentIndex-1);
        if(currentIndex<0){
            currentIndex = 0;
            selectedImage.setVisibility(View.VISIBLE);

            Toast.makeText(this, R.string.no_previous_photo, Toast.LENGTH_SHORT).show();
            return;
        }

        TransitionManager.beginDelayedTransition(transition, new Slide(Gravity.LEFT));

        loadImage();
        selectedImage.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        initialize();

        loadImage();
    }

    Date timeStamp;
    private void loadImage(){
        if(currentIndex < totalImages){
            currentImage = images[currentIndex];
            String fileName = currentImage.getName();

            timeStamp = new Date();
            try{
                fileName= fileName.split("_")[1];
                fileName=fileName.split("\\.")[0];

                long time = Long.parseLong(fileName);
                timeStamp = new Date(time);

            }catch(Exception e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            Bitmap im = ImageUtils.generateCorrectBitmap(currentImage);
            if(im==null){
                selectedImage.setImageResource(android.R.drawable.stat_notify_error);
                makeText(this, R.string.no_photos, Toast.LENGTH_SHORT).show();
            }
            selectedImage.setImageBitmap(im);
//            ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) selectedImage.getLayoutParams();
//            marginParams.setMargins(0,-5000000,0,-500);

        }else{
            selectedImage.setImageResource(android.R.drawable.stat_notify_error);
            makeText(this, R.string.no_photos, Toast.LENGTH_SHORT).show();
        }

    }



    private File exportFile(File src) throws IOException {
        String dstPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator + "PerfectPhoto" + File.separator;
        File dst = new File(dstPath);
        //if folder does not exist
        if (!dst.exists()) {
            if (!dst.mkdir()) {
                return null;
            }
        }

        File expFile = new File(dst.getPath() + File.separator + src.getName());
        FileChannel inChannel = null;
        FileChannel outChannel = null;

        try {
            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(expFile).getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }

        return expFile;
    }

    void handleExport(){

        if(!isWriteStoragePermissionGranted()){
            Toast.makeText(this, "Can't export the images. Give permissions first", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            exportFile(currentImage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Toast.makeText(GalleryActivity.this, "Successfully exported selected photos", Toast.LENGTH_SHORT).show();


    }


    public  boolean isWriteStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
//                Log.v(TAG,"Permission is granted2");
                return true;
            } else {

//                Log.v(TAG,"Permission is revoked2");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
//            Log.v(TAG,"Permission is granted2");
            return true;
        }
    }
}

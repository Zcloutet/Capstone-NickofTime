package com.example.perfectphotoapp;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import static android.widget.Toast.makeText;

public class GalleryActivity extends AppCompatActivity {

    ImageView selectedImage;
    File[] images;
    File currentImage;
    int currentIndex = 0;
    int totalImages = 0;
    private Uri imageUri;
    private Intent intent;

    private void initialize(){


        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("images", Context.MODE_PRIVATE);
        images = directory.listFiles();

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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        selectedImage= findViewById(R.id.imageView);


//        Button btnNext,btnPrevious,shareButton;
        ImageButton btnDelete, shareButton;



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
                                doNext();

//                                Toast.makeText(this, "Left to Right swipe [Next]", Toast.LENGTH_SHORT).show ();
                            }

                            // Right to left swipe action
                            else
                            {
                                doPrevious();
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

//        btnNext.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//               doNext();
//            }
//        });
//
//        btnPrevious.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                doPrevious();
//            }
//        });


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


    }

    public void goBack(){
        Intent intent = new Intent(this, GalleryList.class);
        startActivity(intent);
    }

    private void doNext(){
        if(totalImages == 0){
            currentIndex = 0;
            loadImage();
            return;
        }
        currentIndex = (currentIndex+1)% totalImages;
        loadImage();
    }


    private void doPrevious(){
        currentIndex = (currentIndex-1);
        if(currentIndex<0){
            currentIndex = 0;
            makeText(this, R.string.no_previous_photo, Toast.LENGTH_SHORT).show();
            return;
        }
        loadImage();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initialize();

        loadImage();
    }

    private void loadImage(){
        if(currentIndex < totalImages){
            currentImage = images[currentIndex];
            selectedImage.setImageBitmap(BitmapFactory.decodeFile(currentImage.getAbsolutePath()));
//            ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) selectedImage.getLayoutParams();
//            marginParams.setMargins(0,-5000000,0,-500);
        }else{
            selectedImage.setImageResource(android.R.drawable.stat_notify_error);
            makeText(this, R.string.no_photos, Toast.LENGTH_SHORT).show();
        }
    }

}

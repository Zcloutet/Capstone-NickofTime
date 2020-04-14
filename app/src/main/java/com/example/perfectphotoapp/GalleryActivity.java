package com.example.perfectphotoapp;

import android.content.Context;
import android.content.ContextWrapper;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class GalleryActivity extends AppCompatActivity {

    ImageView selectedImage;
    File[] images;
    File currentImage;
    int currentIndex = 0;
    int totalImages = 0;

    private void initialize(){

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("images", Context.MODE_PRIVATE);
        images = directory.listFiles();

        currentIndex = getIntent().getIntExtra("IMAGE_ID",0);

        totalImages = images.length;
    }
    float x1,x2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        selectedImage= findViewById(R.id.imageView);


        Button btnNext,btnPrevious,btnDelete;


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

        btnNext = findViewById(R.id.next);
        btnPrevious = findViewById(R.id.previous);
        btnDelete = findViewById(R.id.delete);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               doNext();
            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doPrevious();
            }
        });


        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(totalImages == 0 ||currentImage==null){
                    return;
                }
                if (currentImage.delete()){
                    Toast.makeText(GalleryActivity.this, "Successfully Deleted", Toast.LENGTH_SHORT).show();
                    initialize();
                    currentIndex = 0;
                    loadImage();
                }else{
                    Toast.makeText(GalleryActivity.this, "Failed to delete", Toast.LENGTH_SHORT).show();
                }
            }
        });


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
            Toast.makeText(this, "Can't load previous image", Toast.LENGTH_SHORT).show();
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
        }else{
            selectedImage.setImageResource(android.R.drawable.stat_notify_error);
            Toast.makeText(this, "No images to show", Toast.LENGTH_SHORT).show();
        }
    }

}

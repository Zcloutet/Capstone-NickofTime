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
    private Uri imageUri;
    private Intent intent;

    private void initialize(){

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("images", Context.MODE_PRIVATE);
        images = directory.listFiles();

        totalImages = images.length;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        selectedImage= findViewById(R.id.imageView);


        Button btnNext,btnPrevious,btnDelete, shareButton;

        btnNext = findViewById(R.id.next);
        btnPrevious = findViewById(R.id.previous);
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

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(totalImages == 0){
                    currentIndex = 0;
                    loadImage();
                    return;
                }
                currentIndex = (currentIndex+1)% totalImages;
                loadImage();
            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentIndex = (currentIndex-1);
                if(currentIndex<0){
                    currentIndex = 0;
                    Toast.makeText(GalleryActivity.this, "Can't load previous image", Toast.LENGTH_SHORT).show();
                    return;
                }
                loadImage();
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

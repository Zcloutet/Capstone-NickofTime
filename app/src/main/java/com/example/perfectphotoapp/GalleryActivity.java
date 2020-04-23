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

import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.transition.TransitionValues;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

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



    private void initialize(){

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("images", Context.MODE_PRIVATE);
        File[] temp = directory.listFiles();
        images = new File[temp.length];
        int j=0;
        for(int i=temp.length-1;i>=0;i--){
            images[j]= temp[i];
            j++;
        }

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
        transition = findViewById(R.id.trans_container);

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


        slideLeft.setDuration(duration);
        slideRight.setDuration(duration);
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
            Toast.makeText(this, "No image before this.", Toast.LENGTH_SHORT).show();
            selectedImage.setVisibility(View.VISIBLE);

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
    TextView date;

    private void loadImage(){
        if(currentIndex < totalImages){
            currentImage = images[currentIndex];
            String fileName = currentImage.getName();
            date = findViewById(R.id.datetime);
            Date timeStamp = new Date();
            try{
                fileName= fileName.split("_")[1];
                fileName=fileName.split("\\.")[0];

                long time = Long.parseLong(fileName);
                timeStamp = new Date(time);

            }catch(Exception e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            date.setText("Captured on: "+ timeStamp.toLocaleString());

            selectedImage.setImageBitmap(BitmapFactory.decodeFile(currentImage.getAbsolutePath()));
        }else{
            selectedImage.setImageResource(android.R.drawable.stat_notify_error);
            Toast.makeText(this, "No images to show", Toast.LENGTH_SHORT).show();
        }
    }

}

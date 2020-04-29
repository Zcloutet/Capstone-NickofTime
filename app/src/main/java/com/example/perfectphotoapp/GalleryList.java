package com.example.perfectphotoapp;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class GalleryList extends AppCompatActivity {

    ArrayList<File> images;

    //selected photo array
    ArrayList<Integer> markedPhotos;
    GridAdapter adapter;
    Toolbar toolbar;
    void updateTitleBar(){

        if(markedPhotos.size()==0){
            toolbar.setTitle("List of photos");
        }else{
            toolbar.setTitle(markedPhotos.size()+ " photos selected");
        }
    }


    //delete selected photos
    void handleDelete(){
        if(images.size() == 0|| markedPhotos.size()==0){
            return;
        }

        for(int i=0;i<markedPhotos.size();i++){
            images.get(markedPhotos.get(i)).delete();
            images.remove(markedPhotos.get(i));

        }
        Toast.makeText(GalleryList.this, "Successfully deleted selected photos", Toast.LENGTH_SHORT).show();
        markedPhotos.clear();
        updateTitleBar();
        images.clear();
        initialize();



    }

    void handleExport(){

        if(!isWriteStoragePermissionGranted()){
            Toast.makeText(this, "Can't export the images. Give permissions first", Toast.LENGTH_SHORT).show();
            return;
        }
        if(images.size() == 0|| markedPhotos.size()==0){
            return;
        }

        for(int i=0;i<markedPhotos.size();i++){
            try {
                exportFile(images.get(markedPhotos.get(i)));
            } catch (IOException e) {
                e.printStackTrace();
            }
            images.remove(markedPhotos.get(i));

        }
        Toast.makeText(GalleryList.this, "Successfully exported selected photos", Toast.LENGTH_SHORT).show();
        markedPhotos.clear();
        updateTitleBar();
        images.clear();
        initialize();

    }


    onPhotoClickListener listener = new onPhotoClickListener() {
        @Override
        public void onClick(View v,int i) {
            if(markedPhotos.size()!=0){
                handleSelection(v,i);
                return;
            }

            Intent ij = new Intent();
            ij.setClass(getApplicationContext(),GalleryActivity.class);
            ij.putExtra("IMAGE_ID",i);
            startActivity(ij);
        }


        @Override
        public void onLongClick(View v,int i) {
           handleSelection(v,i);
            }

            void handleSelection(View v,int i){
                if(markedPhotos.contains(i)){
                    markedPhotos.remove(markedPhotos.indexOf(i));
//                    Toast.makeText(GalleryList.this, "Unselected", Toast.LENGTH_SHORT).show();
                    v.setBackgroundResource(0);
                    ImageView c = (ImageView)v;
                    c.setImageAlpha(255);

                }else {
                    ImageView c = (ImageView)v;
                    c.setImageAlpha(123);
                    v.setBackgroundResource(R.drawable.border);
                    v.setElevation(5);
//                    Toast.makeText(GalleryList.this, "Selected", Toast.LENGTH_SHORT).show();
                    markedPhotos.add(i);
                }
                updateTitleBar();
            }
    };

    private void initialize(){

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("images", Context.MODE_PRIVATE);
        ImageButton go_back = findViewById(R.id.go_back);
        go_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
            }
        });
        images = new ArrayList<File>();
        File[] f=directory.listFiles();
        for(int i=f.length-1;i>=0;i--){
            images.add(f[i]);
        }
//                directory.listFiles();
        markedPhotos = new ArrayList<Integer>();

        adapter = new GridAdapter(getApplicationContext(),images);
        adapter.setOnClick(listener);
        GridView grid = findViewById(R.id.grid);
        grid.setAdapter(adapter);
    }
        

    public void addImageToGallery(){
        final String filePath = Environment.DIRECTORY_DCIM;
        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, filePath);

        final Context context = getApplicationContext();
        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);


    }

    public void goBack(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


//        go_back.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                goBack();
//            }
//        });

        setContentView(R.layout.activity_gallery_list);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if(markedPhotos.size()==0){
                   Toast.makeText(GalleryList.this, "No photos selected", Toast.LENGTH_SHORT).show();
               }

               handleDelete();
//                handleExport();
            }
        });




    }

    @Override
    protected void onResume() {
        super.onResume();
        initialize();

    }

    class GridAdapter extends BaseAdapter{
        ArrayList<File> imgs;
        Context context;
        onPhotoClickListener listener;
        public GridAdapter(Context c, ArrayList<File> i){
            imgs= i;
            context = c;
        }

        @Override
        public int getCount() {
            return imgs.size();
        }

        @Override
        public File getItem(int i) {
            return imgs.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            final int index = i;
            View parent = LayoutInflater.from(context).inflate(R.layout.thumbnail,null);
            ImageView v = parent.findViewById(R.id.thumb);
            ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            marginParams.setMargins(0,-110,0,-110);

            v.setImageBitmap(BitmapFactory.decodeFile(getItem(i).getAbsolutePath()));

            v.setClickable(true);
            v.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            listener.onClick(view,index);
                        }
                    }
            );
            v.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    listener.onLongClick(view,index);
                    return true;
                }
            });
            return parent;
        }

        void setOnClick(onPhotoClickListener l){
            listener = l;
        }
    }


    interface onPhotoClickListener{
        void onClick(View v,int i);
        void onLongClick(View v,int i);
    }


    private File exportFile(File src) throws IOException {
        String dstPath = Environment.DIRECTORY_DCIM + File.separator + "PerfectPhoto" + File.separator;
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

    public  boolean isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
//                Log.v(TAG,"Permission is granted1");
                return true;
            } else {

//                Log.v(TAG,"Permission is revoked1");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
//            Log.v(TAG,"Permission is granted1");
            return true;
        }
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

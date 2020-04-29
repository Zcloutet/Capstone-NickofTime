package com.example.perfectphotoapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

import java.io.File;

public class GalleryList extends AppCompatActivity {

    File[] images;
    onPhotoClickListener listener = new onPhotoClickListener() {
        @Override
        public void onClick(int i) {
            Intent ij = new Intent();
            ij.setClass(getApplicationContext(),GalleryActivity.class);
            ij.putExtra("IMAGE_ID",i);
            startActivity(ij);
        }
    };

    private void initialize(){

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("images", Context.MODE_PRIVATE);
        images = directory.listFiles();
        ImageButton go_back = findViewById(R.id.go_back);
        go_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
            }
        });

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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initialize();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        GridAdapter adapter = new GridAdapter(getApplicationContext(),images);
        adapter.setOnClick(listener);
        GridView grid = findViewById(R.id.grid);
        grid.setAdapter(adapter);

    }


    class GridAdapter extends BaseAdapter{
        File[] images;
        Context context;
        onPhotoClickListener listener;
        public GridAdapter(Context c, File[] i){
            images= i;
            context = c;
        }

        @Override
        public int getCount() {
            return images.length;
        }

        @Override
        public File getItem(int i) {
            return images[i];
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
                            listener.onClick(index);
                        }
                    }
            );
            return parent;
        }

        void setOnClick(onPhotoClickListener l){
            listener = l;
        }
    }


    interface onPhotoClickListener{
        void onClick(int i);
    }
}

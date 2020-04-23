package com.example.perfectphotoapp;

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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class GalleryList extends AppCompatActivity {

    ArrayList<File> images;

    //selected photo array
    ArrayList<Integer> toDelete;
    GridAdapter adapter;
    Toolbar toolbar;
    void updateTitleBar(){

        if(toDelete.size()==0){
            toolbar.setTitle("List of photos");
        }else{
            toolbar.setTitle(toDelete.size()+ " photos selected");
        }
    }


    //delete selected photos
    void handleDelete(){
        if(images.size() == 0|| toDelete.size()==0){
            return;
        }

        for(int i=0;i<toDelete.size();i++){
            images.get(toDelete.get(i)).delete();
            images.remove(toDelete.get(i));

        }
        Toast.makeText(GalleryList.this, "Successfully deleted selected photos", Toast.LENGTH_SHORT).show();
        toDelete.clear();
        updateTitleBar();
        images.clear();
        initialize();



    }


    onPhotoClickListener listener = new onPhotoClickListener() {
        @Override
        public void onClick(View v,int i) {
            if(toDelete.size()!=0){
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
                if(toDelete.contains(i)){
                    toDelete.remove(toDelete.indexOf(i));
//                    Toast.makeText(GalleryList.this, "Unselected", Toast.LENGTH_SHORT).show();
                    v.setBackgroundResource(0);

                }else {
                    v.setBackgroundResource(R.drawable.border);
                    v.setElevation(5);
//                    Toast.makeText(GalleryList.this, "Selected", Toast.LENGTH_SHORT).show();
                    toDelete.add(i);
                }
                updateTitleBar();
            }
    };

    private void initialize(){

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("images", Context.MODE_PRIVATE);
        images = new ArrayList<File>();
        File[] f=directory.listFiles();
        for(int i=f.length-1;i>=0;i--){
            images.add(f[i]);

        }
//                directory.listFiles();
        toDelete = new ArrayList<Integer>();

        adapter = new GridAdapter(getApplicationContext(),images);
        adapter.setOnClick(listener);
        GridView grid = findViewById(R.id.grid);
        grid.setAdapter(adapter);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_list);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if(toDelete.size()==0){
                   Toast.makeText(GalleryList.this, "No photos selected", Toast.LENGTH_SHORT).show();
               }

               handleDelete();
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
}

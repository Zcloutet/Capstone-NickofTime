package com.example.perfectphotoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;
import android.view.View;
//import org.opencv;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void displayMessage(View v) {

        Toast.makeText(getApplicationContext(), "You pressed a button", Toast.LENGTH_LONG).show();

    }
}

package com.example.perfectphotoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ImageButton go_back = findViewById(R.id.go_back);
        go_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
            }
        });
    }

    public void goBack(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}

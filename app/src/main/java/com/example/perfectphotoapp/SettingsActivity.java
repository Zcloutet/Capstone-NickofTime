package com.example.perfectphotoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    private Switch eyeDetection;
    private Switch smileDetection;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String EYESWITCH = "EYESWITCH";
    public static final String SMILESWITCH = "SMILESWITCH";

    private boolean eyeSwitchOnOff;
    private boolean smileSwitchOnOff;

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

        eyeDetection = (Switch) findViewById(R.id.eyeDetection);
        smileDetection = (Switch) findViewById(R.id.smileDetection);

        eyeDetection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                saveEyeDetectionMode();
            }
        });
        smileDetection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                saveSmileDetectionMode();
            }
        });

        loadData();
        updateSwitches();
    }
    public void saveEyeDetectionMode(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(EYESWITCH, eyeDetection.isChecked());

        editor.apply();
//        Toast.makeText(this, "MODE CHANGED", Toast.LENGTH_SHORT).show();
    }

    public void saveSmileDetectionMode(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SMILESWITCH, smileDetection.isChecked());


        editor.apply();
    }

    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        eyeSwitchOnOff = sharedPreferences.getBoolean(EYESWITCH, true);
        smileSwitchOnOff = sharedPreferences.getBoolean(SMILESWITCH, true);
    }
    public void updateSwitches(){
        eyeDetection.setChecked(eyeSwitchOnOff);
        smileDetection.setChecked(smileSwitchOnOff);
    }

    public void goBack(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}

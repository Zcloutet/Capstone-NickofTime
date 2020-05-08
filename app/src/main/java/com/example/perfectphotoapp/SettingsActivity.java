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
    private Switch generalMotionDetection;
    private Switch facialMotionDetection;
    private Switch facialTimeout;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String EYESWITCH = "EYESWITCH";
    public static final String SMILESWITCH = "SMILESWITCH";
    public static final String GENERALMOTIONSWITCH = "GENERALMOTIONSWITCH";
    public static final String FACIALMOTIONSWITCH = "FACIALMOTIONSWITCH";
    public static final String FACIALTIMEOUTSWITCH = "FACIALTIMEOUTSWITCH";

    private boolean eyeSwitchOnOff;
    private boolean smileSwitchOnOff;
    private boolean generalMotionSwitchOnOff;
    private boolean facialMotionSwitchOnOff;
    private boolean facialTimeoutSwitchOnOff;

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
        generalMotionDetection = (Switch) findViewById(R.id.generalMotionDetection);
        facialMotionDetection = (Switch) findViewById(R.id.facialMotionDetection);
        facialTimeout = (Switch) findViewById(R.id.facialTimeout);

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
        generalMotionDetection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                saveGeneralMotionDetectionMode();
            }
        });
        facialMotionDetection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                saveFacialMotionDetectionMode();
            }
        });
        facialTimeout.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                saveFacialTimeoutMode();
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

    public void saveGeneralMotionDetectionMode(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(GENERALMOTIONSWITCH, generalMotionDetection.isChecked());


        editor.apply();
    }

    public void saveFacialMotionDetectionMode(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(FACIALMOTIONSWITCH, facialMotionDetection.isChecked());


        editor.apply();
    }

    public void saveFacialTimeoutMode(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(FACIALTIMEOUTSWITCH, facialTimeout.isChecked());


        editor.apply();
    }

    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        eyeSwitchOnOff = sharedPreferences.getBoolean(EYESWITCH, true);
        smileSwitchOnOff = sharedPreferences.getBoolean(SMILESWITCH, true);
        generalMotionSwitchOnOff = sharedPreferences.getBoolean(GENERALMOTIONSWITCH, true);
        facialMotionSwitchOnOff = sharedPreferences.getBoolean(FACIALMOTIONSWITCH, true);
        facialTimeoutSwitchOnOff = sharedPreferences.getBoolean(FACIALTIMEOUTSWITCH, true);
    }
    public void updateSwitches(){
        eyeDetection.setChecked(eyeSwitchOnOff);
        smileDetection.setChecked(smileSwitchOnOff);
        generalMotionDetection.setChecked(generalMotionSwitchOnOff);
        facialMotionDetection.setChecked(facialMotionSwitchOnOff);
        facialTimeout.setChecked(facialTimeoutSwitchOnOff);
    }

    public void goBack(){
        //Intent intent = new Intent(this, MainActivity.class);
        //startActivity(intent);
        this.finish();
    }
}

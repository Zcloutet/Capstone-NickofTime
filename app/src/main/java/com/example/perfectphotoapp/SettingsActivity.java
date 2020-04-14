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
    private Switch motionDetection;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String EYESWITCH = "EYESWITCH";
    public static final String SMILESWITCH = "SMILESWITCH";
    public static final String MOTIONSWITCH = "MOTIONSWITCH";

    private boolean eyeSwitchOnOff;
    private boolean smileSwitchOnOff;
    private boolean motionSwitchOnOff;

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
        motionDetection = (Switch) findViewById(R.id.motionDetection);

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
        motionDetection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                saveMotionDetectionMode();
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

    public void saveMotionDetectionMode(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(MOTIONSWITCH, motionDetection.isChecked());


        editor.apply();
    }

    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        eyeSwitchOnOff = sharedPreferences.getBoolean(EYESWITCH, true);
        smileSwitchOnOff = sharedPreferences.getBoolean(SMILESWITCH, true);
        motionSwitchOnOff = sharedPreferences.getBoolean(MOTIONSWITCH, true);
    }
    public void updateSwitches(){
        eyeDetection.setChecked(eyeSwitchOnOff);
        smileDetection.setChecked(smileSwitchOnOff);
        motionDetection.setChecked(motionSwitchOnOff);
    }

    public void goBack(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}

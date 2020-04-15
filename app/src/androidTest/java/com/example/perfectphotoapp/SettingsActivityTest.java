package com.example.perfectphotoapp;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

public class SettingsActivityTest {
    public ActivityTestRule<SettingsActivity> settingsActivityActivityTestRule = new ActivityTestRule<SettingsActivity>(SettingsActivity.class);

    @Rule
    public IntentsTestRule<SettingsActivity> mainActivityIntentsTestRule = new IntentsTestRule<SettingsActivity>(SettingsActivity.class);

    @Before
    public void setUp() throws Exception {
    }
    @Test
    public void testSmileDetectionSwitch(){
        Espresso.onView(withId(R.id.smileDetection)).perform(click());
    }

    @Test
    public void testEyeDetectionSwitch(){
        Espresso.onView(withId(R.id.eyeDetection)).perform(click());
    }

    @After
    public void tearDown() throws Exception {
    }
}
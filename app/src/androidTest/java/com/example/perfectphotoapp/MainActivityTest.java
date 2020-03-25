package com.example.perfectphotoapp;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.*;

public class MainActivityTest {

    @Rule
    public IntentsTestRule<MainActivity> mainActivityIntentsTestRule = new IntentsTestRule<MainActivity>(MainActivity.class);


    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testSettingsButton(){
        Espresso.onView(withId(R.id.settings)).perform(click());
        Espresso.onView((withId(R.id.textView))).check(matches(withText("SETTINGS PAGE")));
//        intended(hasComponent(SettingsActivity.));
    }

    @After
    public void tearDown() throws Exception {
    }
}
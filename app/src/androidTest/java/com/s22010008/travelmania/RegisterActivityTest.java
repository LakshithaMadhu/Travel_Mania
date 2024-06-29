package com.s22010008.travelmania;

import android.content.Intent;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;



@RunWith(AndroidJUnit4.class)
public class RegisterActivityTest {

    @Rule
    public ActivityScenarioRule<Register> activityRule =
            new ActivityScenarioRule<>(Register.class);

    @Test
    public void registerWithValidCredentials_success() {
        // Type in valid email, password, and name
        Espresso.onView(ViewMatchers.withId(R.id.editTextTextPassword1))
                .perform(ViewActions.typeText("test@email.com"), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.editTextTextPassword2))
                .perform(ViewActions.typeText("password123"), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.name))
                .perform(ViewActions.typeText("Test User"), ViewActions.closeSoftKeyboard());

        // Click the register button
        Espresso.onView(ViewMatchers.withId(R.id.button2)).perform(ViewActions.click());

        // Check if MainActivity is launched (you might need to adjust this based on your app's navigation)
        // Espresso.onView(ViewMatchers.withId(R.id.someViewInMainActivity))
        //         .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void registerWithEmptyEmail_showsError() {
        // Leave email field empty
        Espresso.onView(ViewMatchers.withId(R.id.editTextTextPassword2))
                .perform(ViewActions.typeText("password123"), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.name))
                .perform(ViewActions.typeText("Test User"), ViewActions.closeSoftKeyboard());

        // Click the register button
        Espresso.onView(ViewMatchers.withId(R.id.button2)).perform(ViewActions.click());

        // Check if the error message is displayed for the email field
        Espresso.onView(ViewMatchers.withId(R.id.editTextTextPassword1))
                .check(ViewAssertions.matches(ViewMatchers.hasErrorText("Email is required")));
    }

    // Add similar tests for empty password, empty name, and invalid credentials
}
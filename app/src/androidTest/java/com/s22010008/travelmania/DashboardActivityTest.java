package com.s22010008.travelmania;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DashboardActivityTest {

    @Rule
    public ActivityScenarioRule<Dashboard> activityRule =
            new ActivityScenarioRule<>(Dashboard.class);

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            android.Manifest.permission.ACCESS_FINE_LOCATION);

    private IdlingResource idlingResource;

    @Before
    public void registerIdlingResource() {
        activityRule.getScenario().onActivity(activity -> {
            idlingResource = activity.getIdlingResource();
            IdlingRegistry.getInstance().register(idlingResource);
        });
    }

    @Test
    public void clickLocationButton_displaysPlaces() {
        // Click the location button
        Espresso.onView(ViewMatchers.withId(R.id.location_button))
                .perform(ViewActions.click());

        // Wait for the API response and check if the RecyclerView has items
        Espresso.onView(ViewMatchers.withId(R.id.places_recycler_view))
                .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText("Lakeside")))); // Adjust expected text
    }

    // Add more tests for search functionality, profile button, etc.

    @After
    public void unregisterIdlingResource() {
        if (idlingResource != null) {
            IdlingRegistry.getInstance().unregister(idlingResource);
        }
    }
}
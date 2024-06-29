package com.s22010008.travelmania;

import android.os.IBinder;
import android.view.WindowManager;
import android.view.View;

import androidx.test.espresso.Root;
import androidx.test.espresso.matcher.BoundedMatcher;
import org.hamcrest.TypeSafeMatcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher; // Import the correct Matcher

import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

public class ToastMatcher extends BoundedMatcher<Root, Root> {

    public ToastMatcher() {
        super(Root.class);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("is toast");
    }

    @Override
    public boolean matchesSafely(Root root) {
        int type = root.getWindowLayoutParams().get().type;
        if ((type == WindowManager.LayoutParams.TYPE_TOAST)) {
            IBinder windowToken = root.getDecorView().getWindowToken();
            IBinder appToken = root.getDecorView().getApplicationWindowToken();
            return windowToken == appToken;
        }
        return false;
    }

    public static Matcher<Root> isToast() {
        return new TypeSafeMatcher<Root>() {
            @Override
            protected boolean matchesSafely(Root root) {
                int type = root.getWindowLayoutParams().get().type;
                if ((type == WindowManager.LayoutParams.TYPE_TOAST)) {
                    IBinder windowToken = root.getDecorView().getWindowToken();
                    IBinder appToken = root.getDecorView().getApplicationWindowToken();
                    return windowToken == appToken;
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is toast");
            }
        };
    }
}

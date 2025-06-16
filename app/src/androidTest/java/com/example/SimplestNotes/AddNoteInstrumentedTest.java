package com.example.SimplestNotes;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.util.Log;
import android.view.View;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.espresso.util.TreeIterables;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AddNoteInstrumentedTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void addNote_displaysInRecyclerView() throws InterruptedException {
        onView(withId(R.id.buttonAddNote)).perform(click());
        Log.d("TEST", "сюда дошло");
        onView(withId(R.id.editTextNote)).perform(typeText("Espresso Test Note"), closeSoftKeyboard());
        //fail("сюда не доходит");
        onView(withId(R.id.saveButton)).perform(click());
        onView(withText("Espresso Test Note")).check(matches(isDisplayed()));
    }

    public void printAllViews() {
        Log.d("EspressoDebug", "Before printAllViews()");
        Espresso.onView(ViewMatchers.isRoot()).check((view, noViewFoundException) -> {
            for (View v : TreeIterables.breadthFirstViewTraversal(view)) {
                if (v.getId() != View.NO_ID) {
                    String idName = v.getResources().getResourceEntryName(v.getId());
                    System.out.println("View: " + idName + " (id: " + v.getId() + "), class: " + v.getClass().getSimpleName());
                }
            }
        });
        Log.d("EspressoDebug", "After printAllViews()");
    }
}

package com.example.notes;

import android.support.test.espresso.IdlingRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.example.notes.utils.EspressoIdlingResource;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static android.support.test.espresso.action.ViewActions.swipeLeft;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MainActivityTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.getIdlingResource());
    }

    @After
    public void unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());
    }

    @Test
    public void A_createNoteFab_isClickable() {
        onView(withId(R.id.notes_create_note_fab)).perform(click());
        onView(withId(R.id.note_name_text_view)).check(matches(withText("Название:")));
        onView(isRoot()).perform(pressBack());
    }

    @Test
    public void B_createNote_isCorrectly() {
        onView(withId(R.id.notes_create_note_fab)).perform(click());
        onView(withId(R.id.note_name_edit_text)).perform(typeText("CreateNote1"));
        onView(withId(R.id.note_description_edit_text)).perform(typeText("CreateDescription1"));
        onView(withId(R.id.note_edit_button)).perform(click());

        onView(withId(R.id.notes_create_note_fab)).perform(click());
        onView(withId(R.id.note_name_edit_text)).perform(typeText("CreateNote2"));
        onView(withId(R.id.note_description_edit_text)).perform(typeText("CreateDescription2"));
        onView(withId(R.id.note_edit_button)).perform(click());

        onView(withId(R.id.notes_recycler)).perform(actionOnItemAtPosition(0, click()));
        onView(withId(R.id.note_name_edit_text)).check(matches(withText("CreateNote2")));
        onView(withId(R.id.note_description_edit_text)).check(matches(withText("CreateDescription2")));
    }

    @Test
    public void C_editNote_isCorrectly() {
        onView(withId(R.id.notes_recycler)).perform(actionOnItemAtPosition(0, click()));
        onView(withId(R.id.note_name_edit_text)).perform(clearText());
        onView(withId(R.id.note_name_edit_text)).perform(typeText("UpdateNote"));
        onView(withId(R.id.note_description_edit_text)).perform(clearText());
        onView(withId(R.id.note_description_edit_text)).perform(typeText("UpdateDescription"));
        onView(withId(R.id.note_edit_button)).perform(click());
        onView(withId(R.id.notes_recycler)).perform(actionOnItemAtPosition(0, click()));
        onView(withId(R.id.note_name_edit_text)).check(matches(withText("UpdateNote")));
        onView(withId(R.id.note_description_edit_text)).check(matches(withText("UpdateDescription")));
    }

    @Test
    public void D_deleteNote_isCorrectly() {
        onView(withId(R.id.notes_recycler)).perform(actionOnItemAtPosition(0, click()));
        onView(withId(R.id.note_menu_delete)).perform(click());
        onView(withId(R.id.notes_recycler)).perform(actionOnItemAtPosition(0, click()));
        onView(withId(R.id.note_name_edit_text)).check(matches(withText("CreateNote1")));
    }

    @Test
    public void E_swipeNote_isCorrectly() {
        onView(withId(R.id.notes_recycler)).perform(actionOnItemAtPosition(0, swipeLeft()));
    }
}

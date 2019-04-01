package com.example.notes;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

/**
 * Запускает фрагмент со списком заметок
 */
public class MainActivity extends AppCompatActivity implements NotesFragment.NavigationClickHandler {

    public static final int CREATE_NOTE_REQUEST = 1;
    public static final int EDIT_NOTE_REQUEST = 2;
    private static final String ACTIONBAR_TITLE = "toolbar_title";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            NotesFragment notesFragment = NotesFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.main_fragment_container, notesFragment)
                    .commit();
        } else {
            String actionBarTitle = savedInstanceState.getString(ACTIONBAR_TITLE);
            if (actionBarTitle != null) {
                changeActionBar(actionBarTitle, getSupportFragmentManager().getBackStackEntryCount() > 0);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (getSupportActionBar() != null && getSupportActionBar().getTitle() != null) {
            outState.putString(ACTIONBAR_TITLE, getSupportActionBar().getTitle().toString());
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_fragment_container);
        if (!(fragment instanceof OnBackPressedListener) || ((OnBackPressedListener) fragment).allowBackPressed()) {
            super.onBackPressed();
        }
        changeActionBar(getResources().getString(R.string.app_name), false);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getSupportFragmentManager().popBackStack();
            changeActionBar(getResources().getString(R.string.app_name), false);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onCreateButtonClick(@NonNull Fragment targetFragment) {
        NoteFragment noteFragment = NoteFragment.newInstance();
        commitTargetTransaction(targetFragment, noteFragment, CREATE_NOTE_REQUEST);
        changeActionBar(getResources().getString(R.string.note_create_actionbar_name), true);
    }

    public void onItemClick(@NonNull Fragment targetFragment, int noteId) {
        NoteFragment noteFragment = NoteFragment.newInstance(noteId);
        commitTargetTransaction(targetFragment, noteFragment, EDIT_NOTE_REQUEST);
        changeActionBar(getResources().getString(R.string.note_edit_actionbar_name), true);
    }

    private void commitTargetTransaction(@NonNull Fragment targetFragment,
                                         @NonNull Fragment newFragment, int codeRequest) {
        newFragment.setTargetFragment(targetFragment, codeRequest);
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.animator.slide_enter, R.animator.slide_exit,
                        R.animator.slide_pop_enter, 0)
                .replace(R.id.main_fragment_container, newFragment)
                .addToBackStack(null)
                .commit();
    }

    private void changeActionBar(@NonNull String title, boolean displayHomeAsUpEnabled) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(displayHomeAsUpEnabled);
        }
    }
}
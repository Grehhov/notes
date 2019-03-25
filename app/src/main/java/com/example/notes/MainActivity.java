package com.example.notes;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * Запускает фрагмент со списком заметок
 */
public class MainActivity extends AppCompatActivity implements NotesFragment.NavigationClickHandler {

    public static final int CREATE_NOTE_REQUEST = 1;
    public static final int EDIT_NOTE_REQUEST = 2;

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
        }
    }

    public void onCreateButtonClick(@NonNull Fragment targetFragment) {
        NoteFragment noteFragment = NoteFragment.newInstance();
        commitTargetTransaction(targetFragment, noteFragment, CREATE_NOTE_REQUEST);
    }

    public void onItemClick(@NonNull Fragment targetFragment, int position) {
        NoteFragment noteFragment = NoteFragment.newInstance(position);
        commitTargetTransaction(targetFragment, noteFragment, EDIT_NOTE_REQUEST);
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
}
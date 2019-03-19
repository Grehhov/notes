package com.example.notes;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * Запускает фрагмент со списком заметок
 */
public class MainActivity extends AppCompatActivity {

    public static final String BUNDLE_NOTE_NAME = "name";
    public static final String BUNDLE_NOTE_DESCRIPTION = "description";
    public static final String BUNDLE_NOTE_INDEX = "index";

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
}

package com.example.notes;

import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Управляет окном со списком заметок
 */
public class MainActivity extends AppCompatActivity implements NoteAdapter.NoteClickHandler {

    public static final String BUNDLE_NOTE_NAME = "name";
    public static final String BUNDLE_NOTE_DESCRIPTION = "description";
    public static final String BUNDLE_NOTE_INDEX = "index";
    public static final String STATE_NOTE_LIST = "notes";
    private static final int CREATE_NOTE_REQUEST = 1;
    private static final int EDIT_NOTE_REQUEST = 2;
    @NonNull
    private List<Note> notes = new ArrayList<>();
    private NoteAdapter noteAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            List<Note> noteList = savedInstanceState.getParcelableArrayList(STATE_NOTE_LIST);
            if (noteList != null) {
                notes = noteList;
            }
        }
        RecyclerView recyclerViewNotes = findViewById(R.id.main_notes_recycler);
        noteAdapter = new NoteAdapter(this, notes);
        recyclerViewNotes.setAdapter(noteAdapter);

        FloatingActionButton createNoteFab = findViewById(R.id.main_create_note_fab);
        createNoteFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View view) {
                Intent intent = new Intent(getApplicationContext(), NoteActivity.class);
                startActivityForResult(intent, CREATE_NOTE_REQUEST);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(STATE_NOTE_LIST, (ArrayList<? extends Parcelable>) notes);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data == null) {
            return;
        }
        Bundle bundle = data.getExtras();
        if (bundle == null) {
            return;
        }
        String noteName = bundle.getString(BUNDLE_NOTE_NAME);
        if (TextUtils.isEmpty(noteName)) {
            return;
        }
        Note note = new Note(noteName, bundle.getString(BUNDLE_NOTE_DESCRIPTION));
        switch (requestCode) {
            case CREATE_NOTE_REQUEST:
                notes.add(note);
                break;
            case EDIT_NOTE_REQUEST:
                int index = bundle.getInt(BUNDLE_NOTE_INDEX);
                notes.set(index, note);
                break;
        }
        noteAdapter.notifyDataSetChanged();
    }

    public void onItemClick(@NonNull Note note, int position) {
        Intent intent = new Intent(this, NoteActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_NOTE_NAME, note.getName());
        bundle.putString(BUNDLE_NOTE_DESCRIPTION, note.getDescription());
        bundle.putInt(BUNDLE_NOTE_INDEX, position);
        intent.putExtras(bundle);
        startActivityForResult(intent, EDIT_NOTE_REQUEST);
    }
}

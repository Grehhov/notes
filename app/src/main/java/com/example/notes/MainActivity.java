package com.example.notes;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Управляет окном со списком заметок
 */

public class MainActivity extends AppCompatActivity {

    List<Note> notes = new ArrayList<>();
    NoteAdapter noteAdapter;
    public static final int CREATE_NOTE_REQUEST = 1;
    public static final int EDIT_NOTE_REQUEST = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerViewNotes = (RecyclerView)findViewById(R.id.notes);
        noteAdapter = new NoteAdapter(this, notes);
        recyclerViewNotes.setAdapter(noteAdapter);

        FloatingActionButton createNoteFab = (FloatingActionButton)findViewById(R.id.create_note_fab);
        createNoteFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View view) {
                Intent intent = new Intent(getApplicationContext(), NoteActivity.class);
                startActivityForResult(intent, CREATE_NOTE_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data == null) {
            return;
        }

        Bundle bundle = data.getExtras();
        if ("".equals(bundle.getString("name"))) {
            return;
        }
        Note note = new Note(bundle.getString("name"), bundle.getString("description"));

        switch (requestCode) {
            case CREATE_NOTE_REQUEST:
                notes.add(note);
                break;
            case EDIT_NOTE_REQUEST:
                int index = bundle.getInt("index");
                notes.set(index, note);
                break;
        }
        noteAdapter.notifyDataSetChanged();
    }
}

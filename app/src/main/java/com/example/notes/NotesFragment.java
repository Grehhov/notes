package com.example.notes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


/**
 * Управляет окном списка заметок
 */
public class NotesFragment extends Fragment implements NoteAdapter.NoteClickHandler {

    private static final String STATE_NOTE_LIST = "notes";
    @NonNull
    private List<Note> notes = new ArrayList<>();
    private NoteAdapter noteAdapter;
    private NavigationClickHandler navigationClickHandler;

    /**
     * Обрабатывает нажатия во фрагменте
     */
    public interface NavigationClickHandler {
        void onCreateButtonClick(@NonNull Fragment targetFragment);
        void onItemClick(@NonNull Fragment targetFragment, @NonNull Note note, int position);
    }

    @NonNull
    public static NotesFragment newInstance() {
        return new NotesFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof NavigationClickHandler) {
            navigationClickHandler = (NavigationClickHandler) context;
        } else {
            throw new IllegalStateException("Context must implement NavigationClickHandler");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            List<Note> noteList = savedInstanceState.getParcelableArrayList(STATE_NOTE_LIST);
            if (noteList != null) {
                notes = noteList;
            }
        }
        noteAdapter = new NoteAdapter(requireActivity(), notes, this);
    }

    @Override
    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_notes, container, false);

        RecyclerView recyclerViewNotes = rootView.findViewById(R.id.notes_recycler);
        recyclerViewNotes.setAdapter(noteAdapter);

        FloatingActionButton createNoteFab = rootView.findViewById(R.id.notes_create_note_fab);
        final NotesFragment targetFragment = this;
        createNoteFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View view) {
                navigationClickHandler.onCreateButtonClick(targetFragment);
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(STATE_NOTE_LIST, (ArrayList<? extends Parcelable>) notes);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data == null) {
            return;
        }
        Bundle bundle = data.getExtras();
        if (bundle == null) {
            return;
        }
        String noteName = bundle.getString(MainActivity.BUNDLE_NOTE_NAME);
        if (TextUtils.isEmpty(noteName)) {
            return;
        }
        Note note = new Note(noteName, bundle.getString(MainActivity.BUNDLE_NOTE_DESCRIPTION));
        switch (requestCode) {
            case MainActivity.CREATE_NOTE_REQUEST:
                notes.add(note);
                break;
            case MainActivity.EDIT_NOTE_REQUEST:
                int index = bundle.getInt(MainActivity.BUNDLE_NOTE_INDEX);
                notes.set(index, note);
                break;
        }
        noteAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        navigationClickHandler = null;
    }

    public void onItemClick(@NonNull Note note, int position) {
        navigationClickHandler.onItemClick(this, note, position);
    }
}

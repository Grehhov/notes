package com.example.notes;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import static android.app.Activity.RESULT_OK;

/**
 * Управляет окном добавления/редактирования заметки
 */
public class NoteFragment extends Fragment {

    public static final String BUNDLE_NOTE_INDEX = "index";
    private int indexNote = -1;
    private NoteViewModel noteViewModel;

    @NonNull
    public static NoteFragment newInstance() {
         return new NoteFragment();
    }

    @NonNull
    public static NoteFragment newInstance(int indexNote) {
        NoteFragment fragment = new NoteFragment();
        Bundle args = new Bundle();
        args.putInt(BUNDLE_NOTE_INDEX, indexNote);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        noteViewModel = ViewModelProviders.of(requireActivity()).get(NoteViewModel.class);
        if (getArguments() != null) {
            indexNote = getArguments().getInt(BUNDLE_NOTE_INDEX);
        }
        noteViewModel.setIndex(indexNote);
    }

    @Override
    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_note, container, false);
        LiveData<Note> noteLiveData = noteViewModel.getNote();
        noteLiveData.observe(this, new Observer<Note>() {
            @Override
            public void onChanged(@Nullable Note note) {
                if (note != null) {
                    EditText nameEditView = rootView.findViewById(R.id.note_name_edit_text);
                    EditText descriptionEditView = rootView.findViewById(R.id.note_description_edit_text);
                    nameEditView.setText(note.getName());
                    descriptionEditView.setText(note.getDescription());
                }
            }
        });

        Button editNoteButton = rootView.findViewById(R.id.note_edit_button);
        editNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View view) {
                onEditButtonClick(rootView);
            }
        });
        return rootView;
    }

    /**
     * Обрабатывает нажатие по кнопке подтверждения создания/редактирования записи
     */
    void onEditButtonClick(@NonNull View view) {
        final String name = ((TextView) view.findViewById(R.id.note_name_edit_text)).getText().toString();
        final String description = ((TextView) view.findViewById(R.id.note_description_edit_text)).getText().toString();
        if (!TextUtils.isEmpty(name)) {
            int requestCode = getTargetRequestCode();
            switch (requestCode) {
                case MainActivity.CREATE_NOTE_REQUEST:
                    noteViewModel.addNote(new Note(name, description));
                    break;
                case MainActivity.EDIT_NOTE_REQUEST:
                    LiveData<Note> noteLiveData = noteViewModel.getNote();
                    if (noteLiveData != null) {
                        Note note = noteLiveData.getValue();
                        if (note != null) {
                            note.setName(name);
                            note.setDescription(description);
                            noteViewModel.updateNote(indexNote, note);
                        }
                    }
                    break;
            }
            Fragment targetFragment = getTargetFragment();
            if (targetFragment != null) {
                targetFragment.onActivityResult(requestCode, RESULT_OK, null);
            }
        }
        requireActivity().onBackPressed();
    }
}

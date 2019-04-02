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

import static android.app.Activity.RESULT_OK;

/**
 * Управляет окном добавления/редактирования заметки
 */
public class NoteFragment extends Fragment {

    public static final String BUNDLE_NOTE_INDEX = "index";
    private int noteId = -1;
    private NoteViewModel noteViewModel;
    @Nullable
    private EditText nameEditView;
    @Nullable
    private EditText descriptionEditView;

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
            noteId = getArguments().getInt(BUNDLE_NOTE_INDEX);
        }
        if (savedInstanceState == null) {
            noteViewModel.setIndex(noteId);
        }
    }

    @Override
    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_note, container, false);
        LiveData<Note> noteLiveData = noteViewModel.getNote();
        nameEditView = rootView.findViewById(R.id.note_name_edit_text);
        descriptionEditView = rootView.findViewById(R.id.note_description_edit_text);
        noteLiveData.observe(this, new Observer<Note>() {
            @Override
            public void onChanged(@Nullable Note note) {
                if (note != null && nameEditView != null && descriptionEditView != null) {
                    nameEditView.setText(note.getName());
                    descriptionEditView.setText(note.getDescription());
                }
            }
        });

        Button editNoteButton = rootView.findViewById(R.id.note_edit_button);
        editNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View view) {
                onEditButtonClick();
            }
        });
        return rootView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (nameEditView != null && descriptionEditView != null) {
            String name = nameEditView.getText().toString();
            String description = descriptionEditView.getText().toString();
            noteViewModel.saveNoteInfo(name, description);
        }
    }

    /**
     * Обрабатывает нажатие по кнопке подтверждения создания/редактирования записи
     */
    void onEditButtonClick() {
        if (nameEditView == null || descriptionEditView == null) {
            return;
        }
        String name = nameEditView.getText().toString();
        String description = descriptionEditView.getText().toString();
        if (!TextUtils.isEmpty(name)) {
            int requestCode = getTargetRequestCode();
            noteViewModel.saveNoteInfo(name, description);
            Note note = noteViewModel.getNote().getValue();
            if (note != null) {
                switch (requestCode) {
                    case MainActivity.CREATE_NOTE_REQUEST:
                        noteViewModel.addNote(note);
                        break;
                    case MainActivity.EDIT_NOTE_REQUEST:
                        noteViewModel.updateNote(note);
                        break;
                }
            }
            Fragment targetFragment = getTargetFragment();
            if (targetFragment != null) {
                targetFragment.onActivityResult(requestCode, RESULT_OK, null);
            }
            requireActivity().onBackPressed();
        } else {
            nameEditView.setError(getResources().getString(R.string.note_required_field));
        }
    }
}

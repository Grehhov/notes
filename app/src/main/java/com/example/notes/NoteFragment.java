package com.example.notes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
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

    @Nullable
    private String name;
    @Nullable
    private String description;
    private int indexNote;
    private AppCompatActivity mainActivity;

    public NoteFragment() {

    }

    @NonNull
    public static NoteFragment newInstance() {
         return new NoteFragment();
    }

    @NonNull
    public static NoteFragment newInstance(@NonNull Note note, int indexNote) {
        NoteFragment fragment = new NoteFragment();
        Bundle args = new Bundle();
        args.putString(MainActivity.BUNDLE_NOTE_NAME, note.getName());
        args.putString(MainActivity.BUNDLE_NOTE_DESCRIPTION, note.getDescription());
        args.putInt(MainActivity.BUNDLE_NOTE_INDEX, indexNote);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (AppCompatActivity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            name = getArguments().getString(MainActivity.BUNDLE_NOTE_NAME);
            description = getArguments().getString(MainActivity.BUNDLE_NOTE_DESCRIPTION);
            indexNote = getArguments().getInt(MainActivity.BUNDLE_NOTE_INDEX);
        }
    }

    @Override
    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_note, container, false);
        EditText nameEditView = rootView.findViewById(R.id.note_name_edit_text);
        EditText descriptionEditView = rootView.findViewById(R.id.note_description_edit_text);
        nameEditView.setText(name);
        descriptionEditView.setText(description);

        Button editNoteButton = rootView.findViewById(R.id.note_edit_button);
        editNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View view) {
                onEditButtonClick(rootView);
            }
        });
        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivity = null;
    }

    /**
     * Обрабатывает нажатие по кнопке подтверждения создания/редактирования записи
     */
    void onEditButtonClick(@NonNull View view) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        String name = ((TextView) view.findViewById(R.id.note_name_edit_text)).getText().toString();
        String description = ((TextView) view.findViewById(R.id.note_description_edit_text))
                .getText().toString();
        bundle.putString(MainActivity.BUNDLE_NOTE_NAME, name);
        bundle.putString(MainActivity.BUNDLE_NOTE_DESCRIPTION, description);
        bundle.putInt(MainActivity.BUNDLE_NOTE_INDEX, indexNote);
        intent.putExtras(bundle);
        Fragment targetFragment = getTargetFragment();
        if (targetFragment != null) {
            targetFragment.onActivityResult(getTargetRequestCode(), RESULT_OK, intent);
        }
        mainActivity.onBackPressed();
    }
}

package com.example.notes.presentation;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.notes.App;
import com.example.notes.MainActivity;
import com.example.notes.R;
import com.example.notes.domain.Note;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;

/**
 * Управляет окном добавления/редактирования заметки
 */
public class NoteFragment extends Fragment {

    public static final String BUNDLE_NOTE_GUID = "guid";
    @Nullable
    private String guid;
    private NoteViewModel noteViewModel;
    private EditText nameEditView;
    private EditText descriptionEditView;
    private ProgressBar progressBar;
    private TextView errorTextView;
    @Inject
    CustomViewModelFactory customViewModelFactory;

    @NonNull
    public static NoteFragment newInstance() {
        return new NoteFragment();
    }

    @NonNull
    public static NoteFragment newInstance(@NonNull String guid) {
        NoteFragment fragment = new NoteFragment();
        Bundle args = new Bundle();
        args.putString(BUNDLE_NOTE_GUID, guid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((App) requireActivity().getApplication()).getComponent().inject(this);
        noteViewModel = ViewModelProviders.of(this, customViewModelFactory).get(NoteViewModel.class);
        if (getArguments() != null) {
            guid = getArguments().getString(BUNDLE_NOTE_GUID);
        }
        if (savedInstanceState == null) {
            noteViewModel.setGuid(guid);
        }
        noteViewModel.getExitOnSync().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean exitOnSync) {
                if (Boolean.TRUE.equals(exitOnSync)) {
                    requireActivity().onBackPressed();
                }
            }
        });
    }

    @Override
    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_note, container, false);

        Toolbar toolbar = rootView.findViewById(R.id.note_toolbar);
        configureToolbar(toolbar);

        nameEditView = rootView.findViewById(R.id.note_name_edit_text);
        descriptionEditView = rootView.findViewById(R.id.note_description_edit_text);
        noteViewModel.getNote().observe(this, new Observer<Note>() {
            @Override
            public void onChanged(@Nullable Note note) {
                if (note != null) {
                    nameEditView.setText(note.getName());
                    descriptionEditView.setText(note.getDescription());
                }
            }
        });

        progressBar = rootView.findViewById(R.id.note_progressbar);
        progressBar.setVisibility(View.GONE);

        errorTextView = rootView.findViewById(R.id.note_error);

        noteViewModel.getIsRefreshing().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean isRefreshing) {
                onChangedRefreshing(isRefreshing);
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
        String name = nameEditView.getText().toString();
        String description = descriptionEditView.getText().toString();
        noteViewModel.saveNoteInfo(name, description);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (getTargetRequestCode() == MainActivity.EDIT_NOTE_REQUEST) {
            inflater.inflate(R.menu.note_menu, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                requireActivity().onBackPressed();
                break;
            case R.id.note_menu_delete:
                noteViewModel.deleteNote();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Настраивает тулбар
     * @param toolbar - тулбар окна
     */
    void configureToolbar(@NonNull Toolbar toolbar) {
        switch (getTargetRequestCode()) {
            case MainActivity.CREATE_NOTE_REQUEST:
                toolbar.setTitle(getResources().getString(R.string.note_create_actionbar_name));
                break;
            case MainActivity.EDIT_NOTE_REQUEST:
                toolbar.setTitle(getResources().getString(R.string.note_edit_actionbar_name));
                break;
        }
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(toolbar);
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setHasOptionsMenu(true);
    }

    /**
     * Обрабатывает изменение состояние обновления заметки
     */
    void onChangedRefreshing(@Nullable Boolean isRefreshing) {
        if (isRefreshing != null) {
            progressBar.setVisibility(isRefreshing ? View.VISIBLE : View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            errorTextView.setText(getResources().getString(R.string.notes_connection_error));
        }
    }

    /**
     * Обрабатывает нажатие по кнопке подтверждения создания/редактирования записи
     */
    void onEditButtonClick() {
        String name = nameEditView.getText().toString();
        String description = descriptionEditView.getText().toString();
        if (!TextUtils.isEmpty(name)) {
            noteViewModel.saveNoteInfo(name, description);
            switch (getTargetRequestCode()) {
                case MainActivity.CREATE_NOTE_REQUEST:
                    noteViewModel.addNote();
                    break;
                case MainActivity.EDIT_NOTE_REQUEST:
                    noteViewModel.updateNote();
                    break;
            }
            Fragment targetFragment = getTargetFragment();
            if (targetFragment != null) {
                targetFragment.onActivityResult(getTargetRequestCode(), RESULT_OK, null);
            }
        } else {
            nameEditView.setError(getResources().getString(R.string.note_required_field));
        }
    }
}

package com.example.notes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


/**
 * Управляет окном списка заметок
 */
public class NotesFragment extends Fragment
        implements NoteAdapter.NoteClickHandler, OptionsFragment.ListActionHandler {
    /**
     * Обрабатывает нажатия во фрагменте
     */
    public interface NavigationClickHandler {
        void onCreateButtonClick(@NonNull Fragment targetFragment);
        void onItemClick(@NonNull Fragment targetFragment, @NonNull Note note, int position);
    }

    private static final String STATE_NOTE_LIST = "notes";
    private static final String STATE_BOTTOM_SHEET_BEHAVIOR = "bottom_sheet_behavior";
    @NonNull
    private List<Note> notes = new ArrayList<>();
    private NoteAdapter noteAdapter;
    private NavigationClickHandler navigationClickHandler;
    private boolean needCleanSearch;
    @Nullable
    private BottomSheetBehavior bottomSheetBehavior;
    private int stateBottomSheetBehavior = BottomSheetBehavior.STATE_COLLAPSED;

    @NonNull
    public static NotesFragment newInstance() {
        return new NotesFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
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
            stateBottomSheetBehavior = savedInstanceState.getInt(STATE_BOTTOM_SHEET_BEHAVIOR);
            List<Note> noteList = savedInstanceState.getParcelableArrayList(STATE_NOTE_LIST);
            if (noteList != null) {
                notes = noteList;
            }
        } else {
            OptionsFragment optionsFragment = OptionsFragment.newInstance();
            getChildFragmentManager()
                    .beginTransaction()
                    .add(R.id.notes_fragment_options_container, optionsFragment)
                    .commit();
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
        recyclerViewNotes.addItemDecoration(new NotesItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.size_divider_note_list)));

        View bottomSheet = rootView.findViewById(R.id.notes_fragment_options_container);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        if (bottomSheetBehavior != null) {
            bottomSheetBehavior.setBottomSheetCallback(getBottomSheetCallback(recyclerViewNotes));
            bottomSheetBehavior.setState(stateBottomSheetBehavior);
        }

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
    public void onStart(){
        super.onStart();
        if (needCleanSearch) {
            OptionsFragment optionsFragment = (OptionsFragment) getChildFragmentManager()
                    .findFragmentById(R.id.notes_fragment_options_container);
            if (optionsFragment != null) {
                optionsFragment.clearQuery();
            }
            if (bottomSheetBehavior != null) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            needCleanSearch = false;
        }
    }

    @NonNull
    BottomSheetBehavior.BottomSheetCallback getBottomSheetCallback(final RecyclerView recyclerViewNotes) {
        return new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                CoordinatorLayout.LayoutParams layoutParams =
                        (CoordinatorLayout.LayoutParams) recyclerViewNotes.getLayoutParams();
                layoutParams.height = bottomSheet.getTop();
                recyclerViewNotes.setLayoutParams(layoutParams);
            }
        };
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (bottomSheetBehavior != null) {
            outState.putInt(STATE_BOTTOM_SHEET_BEHAVIOR, bottomSheetBehavior.getState());
        }
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
        String noteDescription = bundle.getString(MainActivity.BUNDLE_NOTE_DESCRIPTION);
        switch (requestCode) {
            case MainActivity.CREATE_NOTE_REQUEST:
                notes.add(new Note(noteName, noteDescription));
                break;
            case MainActivity.EDIT_NOTE_REQUEST:
                int index = bundle.getInt(MainActivity.BUNDLE_NOTE_INDEX);
                notes.get(index).setDescription(noteName);
                notes.get(index).setDescription(noteDescription);
                break;
        }
        needCleanSearch = true;
        noteAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (bottomSheetBehavior != null) {
            stateBottomSheetBehavior = bottomSheetBehavior.getState();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        navigationClickHandler = null;
    }

    public void onItemClick(@NonNull Note note, int position) {
        navigationClickHandler.onItemClick(this, note, position);
    }

    public void filter(@NonNull String query) {
        noteAdapter.getFilter().filter(query);
    }

    public void sortByAddDate(boolean isAscending) {
        noteAdapter.sortByAddDate(isAscending);
    }

    public void sortByLastUpdate(boolean isAscending) {
        noteAdapter.sortByLastUpdate(isAscending);
    }
}

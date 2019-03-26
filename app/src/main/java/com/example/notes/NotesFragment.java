package com.example.notes;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Управляет окном списка заметок
 */
public class NotesFragment extends Fragment implements NoteAdapter.NoteClickHandler {
    /**
     * Обрабатывает нажатия во фрагменте
     */
    public interface NavigationClickHandler {
        void onCreateButtonClick(@NonNull Fragment targetFragment);
        void onItemClick(@NonNull Fragment targetFragment, int position);
    }

    private static final String STATE_BOTTOM_SHEET_BEHAVIOR = "bottom_sheet_behavior";
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
        } else {
            OptionsFragment optionsFragment = OptionsFragment.newInstance();
            getChildFragmentManager()
                    .beginTransaction()
                    .add(R.id.notes_fragment_options_container, optionsFragment)
                    .commit();
        }

        NotesViewModel notesViewModel = ViewModelProviders.of(requireActivity()).get(NotesViewModel.class);
        noteAdapter = new NoteAdapter(requireActivity(), new ArrayList<Note>(), this);
        LiveData<List<Note>> notes = notesViewModel.getNotes();
        notes.observe(requireActivity(), new Observer<List<Note>>() {
            @Override
            public void onChanged(@Nullable List<Note> noteList) {
                if (noteList != null) {
                    noteAdapter.updateNotes(noteList);
                }
            }
        });
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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            needCleanSearch = true;
        }
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

    public void onItemClick(int position) {
        navigationClickHandler.onItemClick(this, position);
    }
}

package com.example.notes;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;

import javax.inject.Inject;

/**
 * Управляет нижним окном фильтра и сортировки
 */
public class OptionsFragment extends Fragment {

    private boolean isAscendingLastUpdate;
    @Nullable
    private SearchView searchView;
    private NotesViewModel notesViewModel;
    @Inject
    CustomViewModelFactory customViewModelFactory;

    @NonNull
    public static OptionsFragment newInstance() {
        return new OptionsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((App) requireActivity().getApplication()).getComponent().inject(this);
        notesViewModel = ViewModelProviders.of(requireActivity(), customViewModelFactory).get(NotesViewModel.class);
    }

    @Override
    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_options, container, false);

        searchView = rootView.findViewById(R.id.options_search);

        Button buttonSortUpdate = rootView.findViewById(R.id.options_button_sort_update);
        buttonSortUpdate.setOnClickListener(getSortButtonClickListener(buttonSortUpdate));

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (searchView != null) {
            searchView.setOnQueryTextListener(getQueryTextListener());
        }
    }

    @NonNull
    SearchView.OnQueryTextListener getQueryTextListener() {
        return new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(@NonNull String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(@NonNull String newText) {
                notesViewModel.filter(newText);
                return true;
            }
        };
    }

    @NonNull
    View.OnClickListener getSortButtonClickListener(@NonNull final Button button) {
        return new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View view) {
                String arrow;
                arrow = isAscendingLastUpdate ? "↓" : "↑";
                notesViewModel.sortByLastUpdate(isAscendingLastUpdate);
                isAscendingLastUpdate = !isAscendingLastUpdate;
                button.setText(changeArrow(button.getText().toString(), arrow));
            }
        };
    }

    @NonNull
    private String changeArrow(@NonNull String str, @NonNull String arrow) {
        char lastChar = str.charAt(str.length() - 1);
        if (lastChar == '↓' || lastChar == '↑') {
            str = str.substring(0, str.length() - 1);
        }
        return str + arrow;
    }

    public void clearQuery() {
        if (searchView != null) {
            searchView.setQuery("", false);
        }
    }
}


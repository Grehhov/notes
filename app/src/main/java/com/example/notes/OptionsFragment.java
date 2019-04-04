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

/**
 * Управляет нижним окном фильтра и сортировки
 */
public class OptionsFragment extends Fragment {

    private boolean isAscendingAddDate;
    private boolean isAscendingLastUpdate;
    @Nullable
    private SearchView searchView;
    private NotesViewModel notesViewModel;

    @NonNull
    public static OptionsFragment newInstance() {
        return new OptionsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notesViewModel = ViewModelProviders.of(requireActivity()).get(NotesViewModel.class);
    }

    @Override
    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_options, container, false);

        searchView = rootView.findViewById(R.id.options_search);
        if (searchView != null) {
            searchView.setOnQueryTextListener(getQueryTextListener());
        }

        Button buttonSortAdd = rootView.findViewById(R.id.options_button_sort_add);
        Button buttonSortUpdate = rootView.findViewById(R.id.options_button_sort_update);
        buttonSortAdd.setOnClickListener(getSortButtonClickListener(buttonSortAdd, buttonSortUpdate, true));
        buttonSortUpdate.setOnClickListener(getSortButtonClickListener(buttonSortUpdate, buttonSortAdd, false));

        return rootView;
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
    View.OnClickListener getSortButtonClickListener(@NonNull final Button button, @NonNull final Button otherButton,
                                                    final boolean isButtonAddDate) {
        return new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View view) {
                String arrow;
                if (isButtonAddDate) {
                    arrow = isAscendingAddDate ? "↓" : "↑";
                    notesViewModel.sortByAddDate(isAscendingAddDate);
                    isAscendingAddDate = !isAscendingAddDate;
                } else {
                    arrow = isAscendingLastUpdate ? "↓" : "↑";
                    notesViewModel.sortByLastUpdate(isAscendingLastUpdate);
                    isAscendingLastUpdate = !isAscendingLastUpdate;
                }
                button.setText(changeArrow(button.getText().toString(), arrow));
                otherButton.setText(changeArrow(otherButton.getText().toString(), ""));
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
            notesViewModel.filter("");
        }
    }
}


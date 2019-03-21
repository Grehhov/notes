package com.example.notes;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;

/**
 * Управляет нижним окном фильтра и сортировки
 */
public class BottomSheetFragment extends Fragment {
    private boolean isAscendingAddDate;
    private boolean isAscendingLastUpdate;
    private ListActionHandler listActionHandler;

    /**
     * Обрабатывает нажатие по заметке из списка
     */
    public interface ListActionHandler {
        void filter(@NonNull String query);
        void sortByAddDate(boolean isAscending);
        void sortByLastUpdate(boolean isAscending);
    }

    @NonNull
    public static BottomSheetFragment newInstance() {
        return new BottomSheetFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getParentFragment() instanceof ListActionHandler) {
            listActionHandler = (ListActionHandler) getParentFragment();
        } else {
            throw new IllegalStateException("ParentFragment must implement ListActionHandler");
        }
    }

    @Override
    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_bottom_sheet, container, false);

        View bottomSheet = rootView.findViewById(R.id.fragment_bottom_sheet);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setBottomSheetCallback(getBottomSheetCallback());

        SearchView searchView = rootView.findViewById(R.id.bottom_sheet_search);
        searchView.setOnQueryTextListener(getQueryTextListener(searchView));

        Button buttonSortAdd = rootView.findViewById(R.id.bottom_sheet_button_sort_add);
        Button buttonSortUpdate = rootView.findViewById(R.id.bottom_sheet_button_sort_update);
        buttonSortAdd.setOnClickListener(getSortButtonClickListener(buttonSortAdd, buttonSortUpdate, true));
        buttonSortUpdate.setOnClickListener(getSortButtonClickListener(buttonSortUpdate, buttonSortAdd, false));

        return rootView;
    }

    BottomSheetBehavior.BottomSheetCallback getBottomSheetCallback() {
        final RecyclerView recyclerViewNotes = getParentFragment().getView().findViewById(R.id.notes_recycler);
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

    SearchView.OnQueryTextListener getQueryTextListener(@NonNull final SearchView searchView) {
        return new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(@NonNull String query) {
                //searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(@NonNull String newText) {
                listActionHandler.filter(newText);
                return true;
            }
        };
    }

    View.OnClickListener getSortButtonClickListener(@NonNull final Button button, @NonNull final Button otherButton,
                                                    final boolean isButtonAddDate) {
        return new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View view) {
                String arrow;
                if (isButtonAddDate) {
                    arrow = isAscendingAddDate ? "↓" : "↑";
                    listActionHandler.sortByAddDate(isAscendingAddDate);
                    isAscendingAddDate = !isAscendingAddDate;
                } else {
                    arrow = isAscendingLastUpdate ? "↓" : "↑";
                    listActionHandler.sortByLastUpdate(isAscendingLastUpdate);
                    isAscendingLastUpdate = !isAscendingLastUpdate;
                }
                button.setText(changeArrow(button.getText().toString(), arrow));
                otherButton.setText(changeArrow(otherButton.getText().toString(), ""));
            }
        };
    }

    @NonNull
    private String changeArrow(@NonNull String str, @NonNull String arrow) {
        if (str.charAt(str.length() - 1) == '↓' || str.charAt(str.length() - 1) == '↑') {
            str = str.substring(0, str.length() - 1);
        }
        return str + arrow;
    }

    @Override
    public void onStop() {
        super.onStop();
        View bottomSheet = getView();
        SearchView searchView = bottomSheet.findViewById(R.id.bottom_sheet_search);
        searchView.setQuery("", false);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
}

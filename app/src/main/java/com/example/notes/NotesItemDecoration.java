package com.example.notes;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Реализует разделитель между элементами RecyclerView
 */
public class NotesItemDecoration extends RecyclerView.ItemDecoration {

    private final int sizeDivider;

    public NotesItemDecoration(int sizeDivider) {
        this.sizeDivider = sizeDivider;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int itemPosition = parent.getChildAdapterPosition(view);
        if (itemPosition == 0) {
            outRect.top = sizeDivider;
        }
        outRect.bottom = sizeDivider;
    }
}

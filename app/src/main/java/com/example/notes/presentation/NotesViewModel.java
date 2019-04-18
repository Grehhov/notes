package com.example.notes.presentation;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.widget.Filter;
import android.widget.Filterable;

import com.example.notes.domain.NotesInteractor;
import com.example.notes.domain.Note;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

/**
 * Управляет списком заметок
 */
public class NotesViewModel extends BaseViewModel implements Filterable {
    @NonNull
    private final NotesInteractor notesInteractor;
    @NonNull
    private List<Note> notes = new ArrayList<>();
    @NonNull
    private final MutableLiveData<List<Note>> visibleNotes = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Boolean> isRefreshing = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Boolean> isSynchronizedWithNetwork = new MutableLiveData<>();
    @NonNull
    private final Filter notesFilter = new NotesFilter();
    @NonNull
    private CharSequence lastQuery = "";
    private boolean isAscendingLastUpdate;

    @Inject
    NotesViewModel(@NonNull NotesInteractor notesInteractor) {
        this.notesInteractor = notesInteractor;
        isRefreshing.setValue(true);
        compositeDisposable.add(notesInteractor.getNotes()
                .subscribe(newNotes -> {
                    notes = getActualNotes(newNotes);
                    filter(lastQuery);
                    isRefreshing.postValue(false);
                }, throwable -> {
                    visibleNotes.postValue(null);
                    isRefreshing.postValue(false);
                }));
        compositeDisposable.add(notesInteractor.getSyncNotes()
                .subscribe(isSync -> isSynchronizedWithNetwork.postValue(true)));
    }

    @NonNull
    LiveData<List<Note>> getNotes() {
        return visibleNotes;
    }

    @NonNull
    LiveData<Boolean> getIsRefreshing() {
        return isRefreshing;
    }

    @NonNull
    LiveData<Boolean> getIsSynchronizedWithNetwork() {
        return isSynchronizedWithNetwork;
    }

    void deleteNote(int position) {
        if (visibleNotes.getValue() != null) {
            Note note = visibleNotes.getValue().get(position);
            if (note.getGuid() != null) {
                isRefreshing.setValue(true);
                compositeDisposable.add(notesInteractor.deleteNote(note.getGuid())
                        .subscribe(localNote -> {
                            filter(lastQuery);
                            isRefreshing.postValue(false);
                        }, throwable -> {
                            visibleNotes.postValue(null);
                            isRefreshing.postValue(false);
                        }));
            }
        }
    }

    void syncWithNetworkIsProcessed() {
        isSynchronizedWithNetwork.setValue(null);
    }

    @Override
    @NonNull
    public Filter getFilter() {
        return notesFilter;
    }

    /**
     * Возвращает список неудаленных заметок
     * @return неудаленные заметки
     */
    @NonNull
    private List<Note> getActualNotes(@NonNull List<Note> notes) {
        List<Note> actualNotes = new ArrayList<>();
        for (Note note : notes) {
            if (!note.isDeleted()) {
                actualNotes.add(note);
            }
        }
        return actualNotes;
    }

    /**
     * Фильтрует список заметок по вохждению запроса в название или описание заметки
     * @param charSequence - запрос
     */
    void filter(@NonNull CharSequence charSequence) {
        lastQuery = charSequence;
        notesFilter.filter(charSequence);
    }

    /**
     * Сортирует список заметок по дате последнего обновления
     * @param isAscending - по возрастания/убыванию
     */
    void sortByLastUpdate(final boolean isAscending) {
        isAscendingLastUpdate = isAscending;
        List<Note> notes = this.visibleNotes.getValue();
        if (notes != null) {
            Collections.sort(notes, (a, b) -> {
                int resultCompare = a.getLastUpdate().compareTo(b.getLastUpdate());
                return isAscending ? resultCompare : -1 * resultCompare;
            });
            this.visibleNotes.setValue(notes);
        }
    }

    private class NotesFilter extends Filter {
        @NonNull
        @Override
        protected FilterResults performFiltering(@NonNull CharSequence charSequence) {
            List<Note> visibleNotes;
            String query = charSequence.toString().toLowerCase();
            if (query.isEmpty()) {
                visibleNotes = notes;
            } else {
                List<Note> filteredList = new ArrayList<>();
                for (Note note : notes) {
                    String nameNote = note.getName();
                    String descriptionNote = note.getDescription();
                    if (nameNote != null && nameNote.toLowerCase().contains(query)
                            || (descriptionNote != null && descriptionNote.toLowerCase().contains(query))) {
                        filteredList.add(note);
                    }
                }
                visibleNotes = filteredList;
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = visibleNotes;
            return filterResults;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(@NonNull CharSequence charSequence, @NonNull FilterResults filterResults) {
            visibleNotes.setValue((ArrayList<Note>) filterResults.values);
            sortByLastUpdate(isAscendingLastUpdate);
        }
    }

    void clearQuery() {
        lastQuery = "";
    }
}


package com.example.notes.presentation;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.widget.Filter;
import android.widget.Filterable;

import com.example.notes.domain.NotesInteractor;
import com.example.notes.domain.Note;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

/**
 * Управляет списком заметок
 */
public class NotesViewModel extends ViewModel implements Filterable {
    @NonNull
    private final NotesInteractor notesInteractor;
    @NonNull
    private final MutableLiveData<List<Note>> notes = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Boolean> isRefreshing = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Boolean> isSynchronizedWithNetwork = new MutableLiveData<>();
    @NonNull
    private final Filter notesFilter = new NotesFilter();
    @NonNull
    private CharSequence lastQuery = "";
    private boolean isAscendingLastUpdate;
    @NonNull
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    NotesViewModel(@NonNull NotesInteractor notesInteractor) {
        this.notesInteractor = notesInteractor;
        isRefreshing.setValue(true);
        compositeDisposable.add(notesInteractor.changeNotes()
                .subscribe(newNotes -> {
                    filter(lastQuery);
                    isRefreshing.postValue(false);
                }, throwable -> {
                    notes.postValue(null);
                    isRefreshing.postValue(false);
                }));
        compositeDisposable.add(notesInteractor.getSyncNotes()
                .subscribe(isSync -> isSynchronizedWithNetwork.postValue(true)));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }

    @NonNull
    LiveData<List<Note>> getNotes() {
        return notes;
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
        if (notes.getValue() != null) {
            Note note = notes.getValue().get(position);
            isRefreshing.setValue(true);
            compositeDisposable.add(notesInteractor.deleteNote(note)
                    .subscribe(localNote -> {
                        filter(lastQuery);
                        isRefreshing.postValue(false);
                    }, throwable -> {
                        notes.postValue(null);
                        isRefreshing.postValue(false);
                    }));
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
    private List<Note> getActualNotes() {
        List<Note> actualNotes = new ArrayList<>();
        for (Note note : notesInteractor.getNotes()) {
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
        List<Note> notes = this.notes.getValue();
        if (notes != null) {
            Collections.sort(notes, (a, b) -> {
                int resultCompare = a.getLastUpdate().compareTo(b.getLastUpdate());
                return isAscending ? resultCompare : -1 * resultCompare;
            });
            this.notes.setValue(notes);
        }
    }

    private class NotesFilter extends Filter {
        @NonNull
        @Override
        protected FilterResults performFiltering(@NonNull CharSequence charSequence) {
            List<Note> visibleNotes;
            String query = charSequence.toString().toLowerCase();
            List<Note> actualNotes = getActualNotes();
            if (query.isEmpty()) {
                visibleNotes = actualNotes;
            } else {
                List<Note> filteredList = new ArrayList<>();
                for (Note note : actualNotes) {
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
            notes.setValue((ArrayList<Note>) filterResults.values);
            sortByLastUpdate(isAscendingLastUpdate);
        }
    }

    void clearQuery() {
        lastQuery = "";
    }
}


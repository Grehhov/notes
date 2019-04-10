package com.example.notes;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Управляет списком заметок
 */
public class NotesViewModel extends AndroidViewModel implements Filterable, NotesRepository.NotesSynchronizedListener {
    @NonNull
    private final NotesRepository notesRepository;
    @NonNull
    private final MutableLiveData<List<Note>> notes = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Boolean> isRefreshing = new MutableLiveData<>();
    @NonNull
    private final Filter notesFilter = new NotesFilter();
    @NonNull
    private CharSequence lastQuery = "";
    private boolean isAscendingLastUpdate;

    public NotesViewModel(@NonNull Application application) {
        super(application);
        notesRepository = NotesRepository.getInstance(application);
        notesRepository.addNotesSynchronizedListener(this);
        isRefreshing.setValue(true);
        notesRepository.loadNotes();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        notesRepository.removeNotesSynchronizedListener(this);
    }

    @NonNull
    LiveData<List<Note>> getNotes() {
        return notes;
    }

    @NonNull
    LiveData<Boolean> getIsRefreshing() {
        return isRefreshing;
    }

    void deleteNote(int position) {
        if (notes.getValue() != null) {
            Note note = notes.getValue().get(position);
            isRefreshing.setValue(true);
            notesRepository.deleteNote(note);
        }
    }

    @Override
    public void onSynchronized() {
        filter(lastQuery);
        isRefreshing.setValue(false);
    }

    @Override
    public void onError() {
        notes.setValue(null);
        isRefreshing.setValue(false);
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
        for (Note note : notesRepository.getNotes()) {
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
            Collections.sort(notes, new Comparator<Note>() {
                @Override
                public int compare(@NonNull Note a, @NonNull Note b) {
                    int resultCompare = a.getLastUpdate().compareTo(b.getLastUpdate());
                    return isAscending ? resultCompare : -1 * resultCompare;
                }
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


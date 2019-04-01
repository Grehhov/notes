package com.example.notes;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
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
public class NotesViewModel extends ViewModel implements Filterable, NotesRepository.NotesRefreshListener {
    @NonNull
    private final NotesRepository notesRepository;
    @NonNull
    private final MutableLiveData<List<Note>> notes = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Boolean> notesIsRefreshed = new MutableLiveData<>();
    @NonNull
    private final Filter notesFilter = new NotesFilter();


    public NotesViewModel() {
        notesRepository = NotesRepository.getInstance();
        notesRepository.addNotesRefreshListener(this);
        notes.setValue(notesRepository.getNotes());
        notesIsRefreshed.setValue(true);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        notesRepository.removeNotesRefreshListener(this);
    }

    @NonNull
    LiveData<List<Note>> getNotes() {
        return notes;
    }

    @NonNull
    LiveData<Boolean> getNotesIsRefreshed() {
        return notesIsRefreshed;
    }

    @Override
    public void onStartRefresh() {
        notesIsRefreshed.setValue(true);
    }

    @Override
    public void onCompleteRefresh(@NonNull List<Note> newNotes) {
        notes.setValue(newNotes);
        notesIsRefreshed.setValue(false);
    }

    @Override
    @NonNull
    public Filter getFilter() {
        return notesFilter;
    }

    /**
     * Фильтрует список заметок по вохждению запроса в название или описание заметки
     * @param charSequence - запрос
     */
    void filter(@NonNull CharSequence charSequence) {
        notesFilter.filter(charSequence);
    }

    /**
     * Сортирует список заметок по дате добавления
     * @param isAscending - по возрастания/убыванию
     */
    void sortByAddDate(final boolean isAscending) {
        List<Note> notes = this.notes.getValue();
        Collections.sort(notes, new Comparator<Note>() {
            @Override
            public int compare(@NonNull Note a, @NonNull Note b) {
                int resultCompare = a.getId() - b.getId();
                return isAscending ? resultCompare : -1 * resultCompare;
            }
        });
        this.notes.setValue(notes);
    }

    /**
     * Сортирует список заметок по дате последнего обновления
     * @param isAscending - по возрастания/убыванию
     */
    void sortByLastUpdate(final boolean isAscending) {
        List<Note> notes = this.notes.getValue();
        Collections.sort(notes, new Comparator<Note>() {
            @Override
            public int compare(@NonNull Note a, @NonNull Note b) {
                int resultCompare = a.getLastUpdate().compareTo(b.getLastUpdate());
                return isAscending ? resultCompare : -1 * resultCompare;
            }
        });
        this.notes.setValue(notes);
    }

    private class NotesFilter extends Filter {
        @NonNull
        @Override
        protected FilterResults performFiltering(@NonNull CharSequence charSequence) {
            List<Note> visibleNotes;
            String query = charSequence.toString().toLowerCase();
            if (query.isEmpty()) {
                visibleNotes = notesRepository.getNotes();
            } else {
                List<Note> filteredList = new ArrayList<>();
                for (Note note : notesRepository.getNotes()) {
                    String nameNote = note.getName();
                    String descriptionNote = note.getDescription();
                    if (nameNote.toLowerCase().contains(query)
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
        }
    }
}


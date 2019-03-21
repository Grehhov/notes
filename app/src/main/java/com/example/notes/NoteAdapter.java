package com.example.notes;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Связывает данные заметки с представлением
 */
public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NotesViewHolder> implements Filterable {

    @NonNull
    private final LayoutInflater inflater;
    @NonNull
    private final NoteClickHandler noteClickHandler;
    @NonNull
    private final List<Note> notes;
    @NonNull
    private List<Note> visibleNotes;

    static class NotesViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        final TextView nameView;
        @NonNull
        final TextView descriptionView;
        @NonNull
        final TextView lastUpdateView;

        NotesViewHolder (@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.note_list_name);
            descriptionView = itemView.findViewById(R.id.note_list_description);
            lastUpdateView = itemView.findViewById(R.id.note_list_last_update);
        }
    }

    /**
     * Обрабатывает нажатие по заметке из списка
     */
    public interface NoteClickHandler {
        void onItemClick(@NonNull Note note, int position);
    }

    NoteAdapter(@NonNull Context context, @NonNull List<Note> notes, @NonNull NoteClickHandler noteClickHandler) {
        this.notes = notes;
        this.visibleNotes = notes;
        this.inflater = LayoutInflater.from(context);
        this.noteClickHandler = noteClickHandler;
    }

    @Override
    @NonNull
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, final int viewType) {
        View view = inflater.inflate(R.layout.note_list, parent, false);
        return new NotesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotesViewHolder holder, final int position) {
        final Note note = visibleNotes.get(position);
        holder.nameView.setText(note.getName());
        holder.descriptionView.setText(note.getDescription());
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("hh:mm dd.MM.yyyy", Locale.ROOT);
        holder.lastUpdateView.setText(formatForDateNow.format(note.getLastUpdate()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View view) {
                noteClickHandler.onItemClick(note, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return visibleNotes.size();
    }

    @Override
    @NonNull
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(@NonNull CharSequence charSequence) {
                String query = charSequence.toString().toLowerCase();
                if (query.isEmpty()) {
                    visibleNotes = notes;
                } else {
                    List<Note> filteredList = new ArrayList<>();
                    for (Note note : notes) {
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
            protected void publishResults(@NonNull CharSequence charSequence, @NonNull FilterResults filterResults) {
                visibleNotes = (ArrayList<Note>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    /**
     * Сортирует список заметок по дате добавления
     * @param isAscending - по возрастания/убыванию
     */
    public void sortByAddDate(final boolean isAscending) {
        Collections.sort(visibleNotes, new Comparator<Note>() {
            @Override
            public int compare(@NonNull Note a, @NonNull Note b) {
                int resultCompare = a.getAddDate().compareTo(b.getAddDate());
                return isAscending ? resultCompare : -1 * resultCompare;
            }
        });
        notifyDataSetChanged();
    }

    /**
     * Сортирует список заметок по дате последнего обновления
     * @param isAscending - по возрастания/убыванию
     */
    public void sortByLastUpdate(final boolean isAscending) {
        Collections.sort(visibleNotes, new Comparator<Note>() {
            @Override
            public int compare(@NonNull Note a, @NonNull Note b) {
                int resultCompare = a.getLastUpdate().compareTo(b.getLastUpdate());
                return isAscending ? resultCompare : -1 * resultCompare;
            }
        });
        notifyDataSetChanged();
    }
}

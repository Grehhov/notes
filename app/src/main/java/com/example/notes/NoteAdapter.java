package com.example.notes;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Связывает данные заметки с представлением
 */
public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NotesViewHolder> {
    /**
     * Обрабатывает нажатие по заметке из списка
     */
    public interface NoteClickHandler {
        void onItemClick(int position);
    }

    @NonNull
    private SimpleDateFormat formatForDateNow = new SimpleDateFormat("hh:mm dd.MM.yyyy", Locale.getDefault());
    @NonNull
    private final LayoutInflater inflater;
    @NonNull
    private final NoteClickHandler noteClickHandler;
    @NonNull
    private List<Note> notes;

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

    NoteAdapter(@NonNull Context context, @NonNull List<Note> notes, @NonNull NoteClickHandler noteClickHandler) {
        this.notes = notes;
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
    public void onBindViewHolder(@NonNull final NotesViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.nameView.setText(note.getName());
        holder.descriptionView.setText(note.getDescription());
        holder.lastUpdateView.setText(formatForDateNow.format(note.getLastUpdate()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View view) {
                noteClickHandler.onItemClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    /**
     * Обновляет список заметок
     * @param notes - новый список заметок
     */
    void updateNotes(@NonNull List<Note> notes){
        this.notes = notes;
        notifyDataSetChanged();
    }
}


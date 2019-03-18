package com.example.notes;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Связывает данные заметки с представлением
 */
public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NotesViewHolder> {
    @NonNull
    private final LayoutInflater inflater;
    @NonNull
    private final NoteClickHandler noteClickHandler;
    @NonNull
    private final List<Note> notes;

    public static class NotesViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        final TextView nameView;
        @NonNull
        final TextView descriptionView;

        public NotesViewHolder (@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.note_list_name);
            descriptionView = itemView.findViewById(R.id.note_list_description);
        }
    }

    /**
     * Обрабатывает нажатие по заметке из списка
     */
    public interface NoteClickHandler {
        void onItemClick(@NonNull Note note, int position);
    }

    NoteAdapter(@NonNull Context context, @NonNull List<Note> notes) {
        this.notes = notes;
        this.inflater = LayoutInflater.from(context);
        if (context instanceof NoteClickHandler) {
            this.noteClickHandler = (NoteClickHandler) context;
        } else {
            throw new ClassCastException("Context must implement NoteClickHandler");
        }
    }

    @Override
    @NonNull
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, final int viewType) {
        View view = inflater.inflate(R.layout.note_list, parent, false);
        return new NotesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotesViewHolder holder, final int position) {
        final Note note = notes.get(position);
        holder.nameView.setText(note.getName());
        holder.descriptionView.setText(note.getDescription());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View view) {
                noteClickHandler.onItemClick(note, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }
}

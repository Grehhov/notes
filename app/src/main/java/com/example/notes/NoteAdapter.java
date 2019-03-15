package com.example.notes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

    private LayoutInflater inflater;
    private List<Note> notes;

    public static class NotesViewHolder extends RecyclerView.ViewHolder {
        final TextView nameView;
        final TextView descriptionView;

        public NotesViewHolder (@NonNull View itemView) {
            super(itemView);
            nameView = (TextView) itemView.findViewById(R.id.name);
            descriptionView = (TextView) itemView.findViewById(R.id.description);
        }
    }

    NoteAdapter(@NonNull Context context, @NonNull List<Note> notes) {
        this.notes = notes;
        this.inflater = LayoutInflater.from(context);
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
                Context context = view.getContext();
                Intent intent = new Intent(context, NoteActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("name", note.getName());
                bundle.putString("description", note.getDescription());
                bundle.putInt("index", position);
                intent.putExtras(bundle);
                ((Activity)context).startActivityForResult(intent, MainActivity.EDIT_NOTE_REQUEST);

            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }
}

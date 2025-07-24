package com.traseapps.simplestNotes;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesViewHolder> {

    public NotesAdapter() {
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return notes.get(position).getId();
    }

    private List<Note> notes = new ArrayList<>();
    private OnNoteClickListener onNoteClickListener;

    public void setOnNoteClickListener(OnNoteClickListener onNoteClickListener) {
        this.onNoteClickListener = onNoteClickListener;
    }

    public ArrayList<Note> getNotes() {
        return new ArrayList<>(notes);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setNotes(List<Note> notes) {
        this.notes = new ArrayList<>(notes);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(NotesViewHolder notesViewHolder, int position) {
        Note note = notes.get(position);
        notesViewHolder.textViewNote.setText(note.getText());

        int colorResId = note.getColorResId();
        Drawable drawable = ContextCompat.getDrawable(notesViewHolder.itemView.getContext(), R.drawable.note_background);
        int color = ContextCompat.getColor(notesViewHolder.itemView.getContext(), colorResId);
        if (drawable instanceof GradientDrawable) {
            ((GradientDrawable) drawable).setColor(color);
            notesViewHolder.textViewNote.setBackground(drawable);
        }

        notesViewHolder.itemView.setOnClickListener(view -> {
            if (onNoteClickListener != null) {
                int pos = notesViewHolder.getAdapterPosition();
                if (onNoteClickListener != null && pos != RecyclerView.NO_POSITION && pos < notes.size()) {
                    onNoteClickListener.onNoteClick(notes.get(pos));
                }
            }
        });
    }

    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item,
                parent,
                false
        );
        return new NotesViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public static class NotesViewHolder extends RecyclerView.ViewHolder {

        private final TextView textViewNote;

        public NotesViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNote = itemView.findViewById(R.id.textViewNote);
        }
    }

    @FunctionalInterface
    public interface OnNoteClickListener {
        void onNoteClick(Note note);
    }
}

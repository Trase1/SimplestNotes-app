package com.example.todolist;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton buttonAddNote;
    private RecyclerView recyclerViewNotes;
    private NotesAdapter notesAdapter;

    private NoteDatabase noteDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        noteDatabase = NoteDatabase.getInstance(getApplication());
        initViews();
        notesAdapter = new NotesAdapter();
        notesAdapter.setOnNoteClickListener(note -> {
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(
                        0,
                        ItemTouchHelper.RIGHT
                                | ItemTouchHelper.LEFT) {
                    @Override
                    public boolean onMove(
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder,
                                         int direction) {
                        int position = viewHolder.getAdapterPosition();
                        Note note = notesAdapter.getNotes().get(position);
                        noteDatabase.notesDao().remove(note.getId());
                        showNotes();
                    }
                });
        itemTouchHelper.attachToRecyclerView(recyclerViewNotes);

        recyclerViewNotes.setAdapter(notesAdapter);

        buttonAddNote.setOnClickListener(view -> {
            Intent intent = AddNoteActivity.newIntent(this);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        showNotes();
    }

    private void initViews() {
        buttonAddNote = findViewById(R.id.buttonAddNote);
        recyclerViewNotes = findViewById(R.id.recyclerViewNotes);
    }

    private void showNotes() {
        notesAdapter.setNotes(noteDatabase.notesDao().getNotes());
    }
}
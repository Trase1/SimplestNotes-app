package com.example.todolist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton buttonAddNote;
    private RecyclerView recyclerViewNotes;
    private NotesAdapter notesAdapter;
    private NoteDatabase noteDatabase;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupEdgeToEdge();
        initViews();
        setupDatabase();
        setupRecyclerView();
        //setupOnClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showNotes();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
    private void showNotes() {
        executor.execute(() -> {
            try {
                List<Note> notes = noteDatabase.notesDao().getNotes();
                runOnUiThread(() -> notesAdapter.setNotes(notes));
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, R.string.error_loading_notes, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void deleteNote(Note note) {
        executor.execute(() -> {
            try {
                noteDatabase.notesDao().remove(note/*.getId()*/);
                runOnUiThread(() -> showUndoSnackbar(note));
                showNotes();
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, R.string.error_loading_notes, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void undoDelete(Note note) {
        executor.execute(() -> {
            noteDatabase.notesDao().add(note);
            showNotes();
        });
    }
    private void setupOnClickListeners() {
        buttonAddNote.setOnClickListener(view -> {
            Intent intent = AddNoteActivity.newIntent(this);
            startActivity(intent);
        });

        notesAdapter.setOnNoteClickListener(note -> {
            Intent intent = AddNoteActivity.newIntent(this);
            intent.putExtra("note_id", note.getId());
            intent.putExtra("note_text", note.getText());
            intent.putExtra("note_priority", note.getPriority());
            //startActivityForResult(intent, EDIT_NOTE_REQUEST_CODE);
        });
    }
    private void setupRecyclerView() {
        notesAdapter = new NotesAdapter();

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
                        if (position == RecyclerView.NO_POSITION) return;
                        Note note = notesAdapter.getNotes().get(position);
                        deleteNote(note);
                    }
                });
        itemTouchHelper.attachToRecyclerView(recyclerViewNotes);
        setupOnClickListeners();
        recyclerViewNotes.setAdapter(notesAdapter);
    }

    private void setupDatabase() {
        noteDatabase = NoteDatabase.getInstance(getApplication());
    }

    private void setupEdgeToEdge() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        buttonAddNote = findViewById(R.id.buttonAddNote);
        recyclerViewNotes = findViewById(R.id.recyclerViewNotes);
    }

    private void showUndoSnackbar(Note note) {
        Snackbar noteDeletedSnack = Snackbar.make(findViewById(android.R.id.content), R.string.note_deleted, Snackbar.LENGTH_LONG);
        noteDeletedSnack.setAction(R.string.undo, v -> MainActivity.this.undoDelete(note));
        noteDeletedSnack.show();
    }
}
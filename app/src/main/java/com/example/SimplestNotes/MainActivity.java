package com.example.SimplestNotes;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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

    public static final int cornerRadius = 8;
    private FloatingActionButton buttonAddNote;
    private RecyclerView recyclerViewNotes;
    private NotesAdapter notesAdapter;
    private NoteDatabase noteDatabase;

    public static final String EXTRA_NOTE_ID = "note_id";
    public static final String EXTRA_NOTE_TEXT = "note_text";
    public static final String EXTRA_NOTE_PRIORITY = "note_priority";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ActivityResultLauncher<Intent> editNoteLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayoutWithInsets();
        initViews();
        setupDatabase();
        onEdit();
        setupRecyclerView();
        GoatTracker.trackEvent("/app-opened");
    }

    private void onEdit() {
        editNoteLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {

                    if (result.getResultCode() != RESULT_OK || result.getData() == null) return;

                    Intent data = result.getData();
                    int id = data.getIntExtra(EXTRA_NOTE_ID, -1);
                    if (id == -1) return;

                    String text = data.getStringExtra(EXTRA_NOTE_TEXT);
                    int priority = data.getIntExtra(EXTRA_NOTE_PRIORITY, 1);

                    Note updatedNote = new Note(id, text, priority);
                    executor.execute(() -> {
                        noteDatabase.notesDao().update(updatedNote);
                        showNotes();
                    });
                }
        );
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
                noteDatabase.notesDao().remove(note);
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

        NotesAdapter.OnNoteClickListener noteClickListener = note -> {
            Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
            intent.putExtra(EXTRA_NOTE_ID, note.getId());
            intent.putExtra(EXTRA_NOTE_TEXT, note.getText());
            intent.putExtra(EXTRA_NOTE_PRIORITY, note.getPriority());
            editNoteLauncher.launch(intent);
        };
        notesAdapter.setOnNoteClickListener(noteClickListener);
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
                        Note note;
                        if (position >= 0 && position < notesAdapter.getNotes().size()) {
                            note = notesAdapter.getNotes().get(position);
                            deleteNote(note);
                        }
                    }
                });
        itemTouchHelper.attachToRecyclerView(recyclerViewNotes);
        setupOnClickListeners();
        recyclerViewNotes.setAdapter(notesAdapter);
    }

    private void setupDatabase() {
        noteDatabase = NoteDatabase.getInstance(getApplication());
    }

    private void initLayoutWithInsets() {
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
        View snackbarView = noteDeletedSnack.getView();
        if (snackbarView instanceof FrameLayout) {
            FrameLayout frameLayout = (FrameLayout) snackbarView;

            // Create background with rounded corners
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.RECTANGLE);
            shape.setCornerRadius(dpToPx()); // 8dp radius
            shape.setColor(ContextCompat.getColor(this, R.color.white));

            // Set background
            frameLayout.setBackground(shape);

        }
        noteDeletedSnack.show();
    }

    private float dpToPx() {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                MainActivity.cornerRadius,
                getResources().getDisplayMetrics()
        );
    }
}
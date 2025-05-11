package com.example.todolist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AddNoteActivity extends AppCompatActivity {

    private EditText editTextNote;
    private RadioButton radioButtonLow;
    private RadioButton radioButtonMedium;
    private Button saveButton;

    //private final Database database = Database.getInstance();
    private NoteDatabase noteDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_note);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        noteDatabase = NoteDatabase.getInstance(getApplication());

        initViews();
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNote();
            }
        });
    }

    private void initViews() {
        editTextNote = findViewById(R.id.editTextNote);
        radioButtonLow = findViewById(R.id.radioButtonLow);
        radioButtonMedium = findViewById(R.id.radioButtonMedium);
        saveButton = findViewById(R.id.saveButton);
    }

    private void saveNote() {
        String text = editTextNote.getText().toString().trim();
        int priority = getPriority();
        if (text.isEmpty()) {
            Toast.makeText(this, R.string.empty_noat_toast, Toast.LENGTH_SHORT).show();
        } else {
            Note note = new Note(text, priority);
            noteDatabase.notesDao().add(note);
            finish();
        }
    }

    private int getPriority() {
        int priority;
        if(radioButtonLow.isChecked()) priority = 0;
        else if (radioButtonMedium.isChecked()) priority = 1;
        else priority = 2;
        return priority;
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, AddNoteActivity.class);
    }
}
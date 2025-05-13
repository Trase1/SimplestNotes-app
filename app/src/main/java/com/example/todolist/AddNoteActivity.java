package com.example.todolist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddNoteActivity extends AppCompatActivity {

    public static final int PRIORITY_LOW = 0;
    public static final int PRIORITY_MEDIUM = 1;
    public static final int PRIORITY_HIGH = 2;


    private EditText editTextNote;

    private RadioGroup radioGroup;
    private RadioButton radioButtonLow;
    private RadioButton radioButtonMedium;
    private RadioButton radioButtonHigh;

    private Button saveButton;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private NoteDatabase noteDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        setupDatabase();
        setupEdgeToEdge();
        setupOnClickListeners();
        //checkedUncheckedRadioButton();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private void setupOnClickListeners() {
        saveButton.setOnClickListener(view -> saveNote());
    }

    private void initViews() {
        setContentView(R.layout.activity_add_note);
        editTextNote = findViewById(R.id.editTextNote);
        radioGroup = findViewById(R.id.radioGroup);
        radioButtonLow = findViewById(R.id.radioButtonLow);
        radioButtonMedium = findViewById(R.id.radioButtonMedium);
        radioButtonHigh = findViewById(R.id.radioButtonHigh);
        saveButton = findViewById(R.id.saveButton);
    }
    private void setupEdgeToEdge() {
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupDatabase() {
        noteDatabase = NoteDatabase.getInstance(getApplication());
    }
    private void saveNote() {
        String text = editTextNote.getText().toString().trim();
        int priority = getPriority();
        if (text.isEmpty()) {
            Toast.makeText(this, R.string.empty_note_toast, Toast.LENGTH_SHORT).show();
        } else {
            Note note = new Note(text, priority);
            executor.execute(() -> {
                try {
                    noteDatabase.notesDao().add(note);
                    runOnUiThread(() -> {
                        Toast.makeText(this, R.string.note_added, Toast.LENGTH_SHORT).show();
                        finish();
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(this, R.string.error_adding_a_note, Toast.LENGTH_SHORT).show());
                }
            });
        }
    }
    private int getPriority() {
        int priority;
        if(radioButtonLow.isChecked()) priority = PRIORITY_LOW;
        else if (radioButtonMedium.isChecked()) priority = PRIORITY_MEDIUM;
        else priority = PRIORITY_HIGH; //only radioButtonHigh left
        return priority;
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, AddNoteActivity.class);
    }

    /*private void styleRadioButton(RadioButton button, boolean isSelected, @ColorInt int color) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(color);
        float scale = getResources().getDisplayMetrics().density;
        background.setCornerRadius(8 * scale);

        button.setBackground(background);
    }

    private void checkedUncheckedRadioButton() {
        RadioButton[] buttons = new RadioButton[]{radioButtonLow, radioButtonMedium, radioButtonHigh};
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            for (RadioButton button : buttons) {
                boolean isSelected = (button.getId() == checkedId);
                int uncheckedColor;
                int checkedColor;

                if (button == radioButtonLow) {
                    uncheckedColor = ContextCompat.getColor(this, R.color.low_priority);
                    checkedColor = ContextCompat.getColor(this, R.color.selected_low_priority);
                } else if (button == radioButtonMedium) {
                    uncheckedColor = ContextCompat.getColor(this, R.color.medium_priority);
                    checkedColor = ContextCompat.getColor(this, R.color.selected_medium_priority);
                } else {
                    uncheckedColor = ContextCompat.getColor(this, R.color.high_priority);
                    checkedColor = ContextCompat.getColor(this, R.color.selected_high_priority);
                }

                int finalColor = isSelected ? checkedColor : uncheckedColor;
                styleRadioButton(button, isSelected, finalColor);
            }
        });

    }*/

}
package com.example.todolist;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
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
    private static final String PREFS_NAME = "note_drafts";

    private Note currentNote; // this should be set when editing

    private String draftKey;

    private EditText editTextNote;
    private TextView priorityTextView;
    private ConstraintLayout mainLayout;

    private RadioGroup radioGroup;
    private RadioButton radioButtonLow;
    private RadioButton radioButtonMedium;
    private RadioButton radioButtonHigh;
    private View bottomGuideline;
    private View buttonsContainer;


    private Button saveButton;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private NoteDatabase noteDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        setupDynamicEditTextMaxHeight(); //setting a max height to editText so the buttons and text stay on the screen
        setupEditTextBehavior(); //adding smooth transitions
        setupDatabase();
        setupEdgeToEdge();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.d("BACK", "ACTIVATED");
                clearDraft();
                finish();
            }
        });

        Note note = null;
        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.note_id))){
            note = noteDatabase.notesDao().getNotes().get(intent.getIntExtra(getString(R.string.note_id), -1));
        }
        setupDraftKey(note);
        restoreDraft();

        setupOnClickListeners();
        checkedUncheckedRadioButton(); //style interface according to chosen priority
    }

    @Override
    protected void onPause() {
        super.onPause();
        String text = editTextNote.getText().toString();
        int priority = getPriority();
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(draftKey + "_text", text)
                .putInt(draftKey + "_priority", priority)
                .apply();
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
        priorityTextView = findViewById(R.id.priorityTextView);
        radioGroup = findViewById(R.id.radioGroup);
        radioButtonLow = findViewById(R.id.radioButtonLow);
        radioButtonMedium = findViewById(R.id.radioButtonMedium);
        radioButtonHigh = findViewById(R.id.radioButtonHigh);
        saveButton = findViewById(R.id.saveButton);
        bottomGuideline = findViewById(R.id.bottomGuideline);
        mainLayout = findViewById(R.id.main);
        buttonsContainer = findViewById(R.id.buttonsContainer);
    }


    private void setupDynamicEditTextMaxHeight() {
        // Dynamically set maxHeight based on guideline & keyboard

        mainLayout.getViewTreeObserver().addOnGlobalLayoutListener(() -> {

            Rect r = new Rect();
            mainLayout.getWindowVisibleDisplayFrame(r);
            int[] location = new int[2];
            editTextNote.getLocationOnScreen(location);
            int editTextTop = location[1];
            int maxHeight = r.bottom - editTextTop;
            int guidelineY = bottomGuideline.getY() > 0 ? (int) bottomGuideline.getY() : r.bottom;
            maxHeight = Math.min(maxHeight, guidelineY - editTextTop);

            // Only update if changed, to avoid unnecessary re-layouts
            if (editTextNote.getMaxHeight() != maxHeight && maxHeight > 0) {
                editTextNote.setMaxHeight(maxHeight);
            }
            mainLayout.requestLayout();
        });
    }

    private void setupEditTextBehavior() {
        editTextNote.addTextChangedListener(new TextWatcher() {
            private int previousLength = -1;

            // Required overrides (empty)
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int changeAmount = Math.abs((previousLength == -1 ? 0 : previousLength) - s.length());
                previousLength = s.length();
                if (changeAmount < 20) {
                    TransitionManager.beginDelayedTransition((ViewGroup) buttonsContainer, new AutoTransition());
                } else {
                    mainLayout.requestLayout();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                editTextNote.requestLayout();
            }
        });
    }

    private void setupEdgeToEdge() {
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
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
                        clearDraft();
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
        if (radioButtonLow.isChecked()) priority = PRIORITY_LOW;
        else if (radioButtonMedium.isChecked()) priority = PRIORITY_MEDIUM;
        else priority = PRIORITY_HIGH; //only radioButtonHigh left
        return priority;
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, AddNoteActivity.class);
    }

    private void styleRadioButton(RadioButton button, @ColorInt int color, float[] radii) {
        //Background with proper corners and color
        GradientDrawable background = new GradientDrawable();
        background.setColor(color);
        background.setCornerRadii(radii);
        button.setBackground(background);
    }

    private void checkedUncheckedRadioButton() {
        RadioButton[] buttons = new RadioButton[]{radioButtonLow, radioButtonMedium, radioButtonHigh};
        radioButtonLow.setChecked(true);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            for (int i = 0; i < buttons.length; i++) {
                RadioButton button = buttons[i];
                boolean isSelected = (button.getId() == checkedId);
                int uncheckedColor, checkedColor;

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

                float[] radii = getRadii(i, buttons);
                styleRadioButton(button, finalColor, radii);

                if (isSelected) {
                    applyPriorityTheme(uncheckedColor);
                }
            }
        });
        radioGroup.check(radioGroup.getCheckedRadioButtonId());
        applyPriorityTheme(ContextCompat.getColor(this, R.color.low_priority));
    }

    private float[] getRadii(int i, RadioButton[] buttons) {
        float radius = 8 * getResources().getDisplayMetrics().density;
        float[] radii;

        if (i == 0)
            radii = new float[]{radius, radius, 0, 0, 0, 0, radius, radius}; // First button (left) → round left corners
        else if (i == buttons.length - 1)
            radii = new float[]{0, 0, radius, radius, radius, radius, 0, 0}; // Last button (right) → round right corners
        else radii = new float[8]; // Middle button → no rounding

        return radii;
    }

    private void applyPriorityTheme(@ColorInt int newColor) {
        int currentColor = priorityTextView.getCurrentTextColor();

        ValueAnimator colorAnimator = ValueAnimator.ofArgb(currentColor, newColor);
        colorAnimator.setDuration(400);
        colorAnimator.addUpdateListener(animator -> {
            int animatedColor = (int) animator.getAnimatedValue();

            // 1. Set underline color of EditText
            Drawable background = editTextNote.getBackground().mutate();
            if (background instanceof GradientDrawable) {
                ((GradientDrawable) background).setColor(animatedColor);
            } else {
                // Fallback: use tint if not a drawable with color
                editTextNote.setBackgroundTintList(ColorStateList.valueOf(animatedColor));
            }
            // 2. Set TextView text color
            priorityTextView.setTextColor(animatedColor);

            // 3. Set Save Button background tint (if you use material or drawable-based background)
            saveButton.setBackgroundTintList(ColorStateList.valueOf(animatedColor));
        });
        colorAnimator.start();
    }

    private void setupDraftKey(Note note) {
        if (note != null && note.getId() != 0) {
            draftKey = "draft_note_" + note.getId(); //editing
        } else {
            draftKey = "draft_new_note"; //adding
        }
    }

    private void restoreDraft() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String draft = prefs.getString(draftKey + "_text", null);
        int draftPriority = prefs.getInt(draftKey + "_priority", -1);

        if (draft != null && !draft.isEmpty()) {
            editTextNote.setText(draft);
            editTextNote.setSelection(draft.length());
        }

        if (draftPriority != -1) {
            if (draftPriority == PRIORITY_LOW) radioButtonLow.setChecked(true);
            else if (draftPriority == PRIORITY_MEDIUM) radioButtonMedium.setChecked(true);
            else if (draftPriority == PRIORITY_HIGH) radioButtonHigh.setChecked(true);
        }
    }

    private void clearDraft() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .remove(draftKey + "_text")
                .remove(draftKey + "_priority")
                .apply();
    }
}
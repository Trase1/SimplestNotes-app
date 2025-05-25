package com.example.todolist;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import org.jetbrains.annotations.Contract;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class AddNoteActivity extends AppCompatActivity {

    public static final int PRIORITY_LOW = 0;
    public static final int PRIORITY_MEDIUM = 1;
    public static final int PRIORITY_HIGH = 2;
    private static final String PREFS_NAME = "note_drafts";
    private int editingNoteId = -1;
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
        setupOnClickListeners();
        checkedUncheckedRadioButton(); //style interface according to chosen priority
        onBack();
        onRestore();
        onEdit();
    }

    private void onRestore() {
        Intent intent = getIntent();
        if (intent.hasExtra(MainActivity.EXTRA_NOTE_ID)) {
            int noteId = intent.getIntExtra(MainActivity.EXTRA_NOTE_ID, -1);
            executor.execute(() -> {
                Note note = noteDatabase.notesDao().getNoteById(noteId);
                setupDraftKey(note);
            });
        }
        restoreDraft();
    }

    private void onEdit() {
        Intent intent = getIntent();
        if (intent.hasExtra(MainActivity.EXTRA_NOTE_ID)) {
            // Edit mode
            editingNoteId = intent.getIntExtra(MainActivity.EXTRA_NOTE_ID, -1);
            String text = intent.getStringExtra(MainActivity.EXTRA_NOTE_TEXT);
            int priority = intent.getIntExtra(MainActivity.EXTRA_NOTE_PRIORITY, 1);

            editTextNote.setText(text);

            // Set correct radio button for priority
            if (priority == 0) {
                radioGroup.check(R.id.radioButtonLow);
            } else if (priority == 1) {
                radioGroup.check(R.id.radioButtonMedium);
            } else if (priority == 2) {
                radioGroup.check(R.id.radioButtonHigh);
            }
        }
    }

    private void onBack() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                clearDraft();
                editTextNote.setText("");
                setPriorityUI(PRIORITY_LOW);
                finish();
            }
        });
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

        editTextNote.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

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
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);

            return insets;
        });
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Reset insets when orientation changes
        ViewCompat.requestApplyInsets(mainLayout);
    }

    private void setupDatabase() {
        noteDatabase = NoteDatabase.getInstance(getApplication());
    }

    private void saveNote() {
        String text = editTextNote.getText().toString().trim();
        int priority = getPriority();
        //edit note
        if (editingNoteId != -1) {
            Intent data = new Intent();
            data.putExtra(MainActivity.EXTRA_NOTE_TEXT, text);
            data.putExtra(MainActivity.EXTRA_NOTE_PRIORITY, priority);
            data.putExtra(MainActivity.EXTRA_NOTE_ID, editingNoteId);
            setResult(RESULT_OK, data);
            finish();

        }
        //new note
        else if (text.isEmpty()) {
            Toast.makeText(this, R.string.empty_note_toast, Toast.LENGTH_SHORT).show();
        } else {
            Note note = new Note(text, priority);
            executor.execute(() -> {
                try {
                    noteDatabase.notesDao().add(note);
                    runOnUiThread(() -> {
                        Toast.makeText(this, R.string.note_added, Toast.LENGTH_SHORT).show();
                        clearDraft();
                        editTextNote.setText("");
                        setPriorityUI(PRIORITY_LOW);
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

    @NonNull
    @Contract("_ -> new")
    public static Intent newIntent(Context context) {
        return new Intent(context, AddNoteActivity.class);
    }

    private void styleRadioButton(@NonNull RadioButton button, @ColorInt int color, float[] radii) {
        //Background with proper corners and color
        GradientDrawable background = new GradientDrawable();
        background.setColor(color);
        background.setCornerRadii(radii);
        button.setBackground(background);
    }

    private void checkedUncheckedRadioButton() {
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int priority = PRIORITY_LOW;
            if (radioButtonMedium.getId() == checkedId) priority = PRIORITY_MEDIUM;
            else if (radioButtonHigh.getId() == checkedId) priority = PRIORITY_HIGH;
            setPriorityUI(priority);
        });
    }

    private void setPriorityUI(int priority) {
        // 1. Set radio
        if (priority == PRIORITY_LOW) radioButtonLow.setChecked(true);
        else if (priority == PRIORITY_MEDIUM) radioButtonMedium.setChecked(true);
        else if (priority == PRIORITY_HIGH) radioButtonHigh.setChecked(true);

        // 2. Set colors for all radios and apply main theme
        int[] checkedColors = {
                ContextCompat.getColor(this, R.color.selected_low_priority),
                ContextCompat.getColor(this, R.color.selected_medium_priority),
                ContextCompat.getColor(this, R.color.selected_high_priority)
        };
        int[] uncheckedColors = {
                ContextCompat.getColor(this, R.color.low_priority),
                ContextCompat.getColor(this, R.color.medium_priority),
                ContextCompat.getColor(this, R.color.high_priority)
        };
        RadioButton[] radios = {radioButtonLow, radioButtonMedium, radioButtonHigh};

        for (int i = 0; i < radios.length; i++) {
            float[] radii = getRadii(i, radios);
            int color = (i == priority) ? checkedColors[i] : uncheckedColors[i];
            styleRadioButton(radios[i], color, radii);
        }


        applyPriorityTheme(uncheckedColors[priority]);
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

            // New: Update status bar color
            getWindow().setStatusBarColor(animatedColor);
            getWindow().setNavigationBarColor(animatedColor);

            // Adjust status bar icons for better visibility
            setStatusBarIconColor(animatedColor);
        });
        colorAnimator.start();
    }

    private void setStatusBarIconColor(@ColorInt int backgroundColor) {
        // Calculate appropriate icon color (light/dark)
        boolean isDark = ColorUtils.calculateLuminance(backgroundColor) < 0.5;

        View decorView = getWindow().getDecorView();
        int flags = decorView.getSystemUiVisibility();
        if (isDark) {
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        decorView.setSystemUiVisibility(flags);
    }

    @NonNull
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

        int priorityToSet = (draftPriority != -1) ? draftPriority : PRIORITY_LOW;
        setPriorityUI(priorityToSet);
    }

    private void clearDraft() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .remove(draftKey + "_text")
                .remove(draftKey + "_priority")
                .apply();
    }
}
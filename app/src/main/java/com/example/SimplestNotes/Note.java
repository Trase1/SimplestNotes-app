package com.example.SimplestNotes;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "notes")
public class Note {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private final String text;
    private final int priority;


    public Note(int id, String text, int priority) {
        this.id = id;
        this.text = text;
        this.priority = priority;
    }

    @Ignore
    public Note(String text, int priority) {
        this.text = text;
        this.priority = priority;
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public int getPriority() {
        return priority;
    }

    public int getColorResId() {
        switch (priority) {
            case 0:
                return R.color.low_priority;
            case 1:
                return R.color.medium_priority;
            default:
                return R.color.high_priority;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Note)) return false;
        Note note = (Note) o;
        return id == note.id && priority == note.priority && Objects.equals(text, note.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, text, priority);
    }
}

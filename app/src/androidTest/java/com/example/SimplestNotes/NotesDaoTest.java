package com.example.SimplestNotes;

import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class NotesDaoTest {

    private NoteDatabase db;
    private NotesDao dao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.databaseBuilder(context, NoteDatabase.class, "db").allowMainThreadQueries().build();
        dao = db.notesDao();
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    @Test
    public void insertAndGetNote() {
        Note note = new Note("test text", 1);
        dao.add(note);
        List<Note> allNotes = dao.getNotes();
        assertEquals(1, allNotes.size());
        assertEquals("test text", allNotes.get(0).getText());
    }
}

package com.traseapps.simplestNotes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class NoteTest {

    @Test
    public void noteFields_areCorrect() {
        Note note = new Note(5, "note text", 1);
        assertEquals(5, note.getId());
        assertEquals("note text", note.getText());
        assertEquals(1, note.getPriority());
    }
}
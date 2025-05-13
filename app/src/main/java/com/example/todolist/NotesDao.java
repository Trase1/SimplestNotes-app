package com.example.todolist;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface NotesDao {
    @Query("SELECT * FROM notes ORDER BY priority DESC")
    List<Note> getNotes();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void add(Note note);

    /*@Query("DELETE FROM notes WHERE id = :id")
    void remove(int id);*/

    @Delete
    void remove(Note note);

}

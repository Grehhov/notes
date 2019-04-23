package com.example.notes.data;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.example.notes.domain.Note;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class NotesDaoImplTest {
    private NotesDaoImpl notesDao;
    private NotesSqliteHelper notesSqliteHelper;
    private Note note;

    @Before
    public void createDb() {
        Context context = InstrumentationRegistry.getContext();
        notesSqliteHelper = new NotesSqliteHelper(context, null);
        notesDao = new NotesDaoImpl(notesSqliteHelper);

        note = new Note(UUID.randomUUID().toString(), "Note", "Description");
        notesDao.addNote(note);
    }

    @After
    public void closeDb() {
        notesSqliteHelper.close();
    }

    @Test
    public void getNote() {
        assertEquals(note, notesDao.getNote(note.getGuid()));
    }

    @Test
    public void getAllNotes() {
        List<Note> notes = notesDao.getAllNotes();
        assertEquals(1, notes.size());
        assertEquals(note, notes.get(0));
    }

    @Test
    public void addNote() {
        Note newNote = new Note(UUID.randomUUID().toString(), "NewNote", "NewDescription");
        notesDao.addNote(newNote);

        Note addedNote = notesDao.getNote(newNote.getGuid());
        assertEquals(newNote, addedNote);

        List<Note> notes = notesDao.getAllNotes();
        assertEquals(2, notes.size());
    }

    @Test
    public void updateNote() {
        Note newNote = notesDao.getNote(note.getGuid());
        assertEquals(note, newNote);

        if (newNote != null) {
            newNote.setName("UpdateName");
            newNote.setDescription("UpdateDescription");
            notesDao.updateNote(newNote);
            Note updatedNote = notesDao.getNote(note.getGuid());
            assertEquals(newNote, updatedNote);

            List<Note> notes = notesDao.getAllNotes();
            assertEquals(1, notes.size());
        }
    }

    @Test
    public void deleteNote() {
        Note newNote = notesDao.getNote(note.getGuid());
        assertEquals(note, newNote);

        if (newNote != null) {
            assertFalse(newNote.isDeleted());
            notesDao.deleteNote(newNote.getGuid());
            Note deletedNote = notesDao.getNote(note.getGuid());
            assertTrue(deletedNote.isDeleted());

            List<Note> notes = notesDao.getAllNotes();
            assertEquals(1, notes.size());
        }
    }

    @Test
    public void syncNotesWithOldNotes() {
        List<Note> newNotes = new ArrayList<>();
        newNotes.add(note);
        newNotes.add(new Note(UUID.randomUUID().toString(), "SyncNote", "SyncDescription"));

        notesDao.syncNotes(newNotes);

        List<Note> notes = notesDao.getAllNotes();
        assertEquals(2, notes.size());
    }

    @Test
    public void syncNotesWithoutOldNotes() {
        List<Note> newNotes = new ArrayList<>();
        newNotes.add(new Note(UUID.randomUUID().toString(), "SyncNote", "SyncDescription"));
        newNotes.add(new Note(UUID.randomUUID().toString(), "SyncNote", "SyncDescription"));

        notesDao.syncNotes(newNotes);

        List<Note> notes = notesDao.getAllNotes();
        assertEquals(3, notes.size());
    }
}

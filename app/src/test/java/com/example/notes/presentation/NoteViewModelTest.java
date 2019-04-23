package com.example.notes.presentation;

import android.arch.core.executor.testing.InstantTaskExecutorRule;

import com.example.notes.domain.Note;
import com.example.notes.domain.NotesInteractor;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import io.reactivex.Single;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class NoteViewModelTest {
    private NoteViewModel noteViewModel;
    @Mock
    private NotesInteractor notesInteractor;
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    private Note testNote;

    @Before
    public void setUp() {
        noteViewModel = new NoteViewModel(notesInteractor);
        testNote = new Note("1", "Note", "Description");
        Mockito.when(notesInteractor.getNote(testNote.getGuid())).thenReturn(Single.just(testNote));
    }

    @Test
    public void setGuidForCreate() {
        noteViewModel.setGuid(null);

        Note note = noteViewModel.getNote().getValue();
        assertNotNull(note);
        assertNull(note.getGuid());
        assertNull(note.getName());
        assertNull(note.getDescription());
    }

    @Test
    public void setGuidForEdit() {
        noteViewModel.setGuid(testNote.getGuid());

        Mockito.verify(notesInteractor).getNote(testNote.getGuid());
        Note note = noteViewModel.getNote().getValue();
        assertEquals(testNote, note);
    }

    @Test
    public void saveNoteInfo() {
        noteViewModel.setGuid(testNote.getGuid());
        String newName = "NewNote";
        String newDescription = "NewDescription";
        Note oldNote = noteViewModel.getNote().getValue();

        noteViewModel.saveNoteInfo(newName, newDescription);

        Note newNote = noteViewModel.getNote().getValue();
        assertEquals(oldNote.getGuid(), newNote.getGuid());
        assertEquals(newName, newNote.getName());
        assertEquals(newDescription, newNote.getDescription());
    }

    @Test
    public void updateNote() {
        noteViewModel.setGuid(testNote.getGuid());
        assertNull(noteViewModel.getExitOnSync().getValue());

        Mockito.when(notesInteractor.updateNote(testNote)).thenReturn(Single.just(testNote));

        noteViewModel.updateNote();
        assertTrue(noteViewModel.getExitOnSync().getValue());
        Mockito.verify(notesInteractor).updateNote(testNote);
    }

    @Test
    public void addNote() {
        noteViewModel.setGuid(testNote.getGuid());
        assertNull(noteViewModel.getExitOnSync().getValue());

        Mockito.when(notesInteractor.addNote(testNote)).thenReturn(Single.just(testNote));

        noteViewModel.addNote();
        assertTrue(noteViewModel.getExitOnSync().getValue());
        Mockito.verify(notesInteractor).addNote(testNote);
    }

    @Test
    public void deleteNote() {
        noteViewModel.setGuid(testNote.getGuid());
        assertNull(noteViewModel.getExitOnSync().getValue());

        Mockito.when(notesInteractor.deleteNote(testNote.getGuid())).thenReturn(Single.just(testNote));

        noteViewModel.deleteNote();
        assertTrue(noteViewModel.getExitOnSync().getValue());
        Mockito.verify(notesInteractor).deleteNote(testNote.getGuid());
    }
}
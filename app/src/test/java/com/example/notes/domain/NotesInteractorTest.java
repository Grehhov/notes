package com.example.notes.domain;

import com.example.notes.data.LocalRepositoryImpl;
import com.example.notes.data.RemoteRepositoryImpl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;

public class NotesInteractorTest {
    private RemoteRepositoryImpl remoteRepository;
    private LocalRepositoryImpl localRepository;
    private NotesInteractor notesInteractor;

    private List<Note> notes;

    @Before
    public void setUp() {
        remoteRepository = Mockito.mock(RemoteRepositoryImpl.class);
        localRepository = Mockito.mock(LocalRepositoryImpl.class);

        Note firstNote = new Note("1", "Name", "Description");
        List<Note> localNotes = new ArrayList<>();
        localNotes.add(firstNote);

        Note secondNote = new Note("2", "Name", "Description");
        List<Note> remoteNotes = new ArrayList<>();
        remoteNotes.add(secondNote);

        notes = new ArrayList<>();
        notes.add(firstNote);
        notes.add(secondNote);

        Mockito.when(localRepository.getAllNotes()).thenReturn(Single.just(localNotes));

        Mockito.when(remoteRepository.syncNotes(new ArrayList<>())).thenReturn(Single.just(remoteNotes));

        Mockito.when(remoteRepository.syncNotes(notes)).thenReturn(Single.just(notes));
        Mockito.when(localRepository.syncNotes(notes)).thenReturn(Observable.empty().ignoreElements());
        notesInteractor = new NotesInteractor(localRepository, remoteRepository);
    }

    @Test
    public void loadNotes_isCorrectly() {
        Mockito.verify(localRepository).getAllNotes();
        Mockito.verify(remoteRepository).syncNotes(new ArrayList<>());
        Mockito.verify(localRepository).syncNotes(notes);
        Mockito.verify(remoteRepository).syncNotes(notes);
    }

    @Test
    public void getNote_isCorrectly() {
        Note note = notes.get(1);
        Mockito.when(localRepository.getNote(note.getGuid())).thenReturn(Single.just(note));
        TestObserver<Note> testObserver = TestObserver.create();
        notesInteractor.getNote(note.getGuid())
                .subscribe(testObserver);

        testObserver.awaitTerminalEvent();
        testObserver.assertComplete()
                .assertNoErrors()
                .assertValue(note);
    }

    @Test
    public void addNote_isCorrectly() {
        Note note = new Note(null, "AddName", "AddDescription");
        Note newNote = new Note(UUID.randomUUID().toString(), "AddNote", "AddDescription");
        Mockito.when(localRepository.addNote(Mockito.any(Note.class))).thenReturn(Single.just(newNote));
        TestObserver<Note> testObserver = TestObserver.create();
        notesInteractor.addNote(note).subscribe(testObserver);

        testObserver.awaitTerminalEvent();
        testObserver.assertComplete()
                .assertNoErrors()
                .assertValue(newNote);

        Mockito.verify(localRepository, times(2)).getAllNotes();
    }

    @Test
    public void updateNote_isCorrectly() throws CloneNotSupportedException {
        Note cloneNote = notes.get(1).clone();
        cloneNote.setName("UpdateName");
        cloneNote.setDescription("UpdateDescription");
        Mockito.when(localRepository.updateNote(cloneNote)).thenReturn(Single.just(cloneNote));
        TestObserver<Note> testObserver = TestObserver.create();
        notesInteractor.updateNote(cloneNote).subscribe(testObserver);

        testObserver.awaitTerminalEvent();
        testObserver.assertComplete()
                .assertNoErrors()
                .assertValue(cloneNote);

        Mockito.verify(localRepository, times(2)).getAllNotes();
    }

    @Test
    public void deleteNote_isCorrectly() throws CloneNotSupportedException {
        Note note = notes.get(1);
        Note cloneNote = note.clone();
        cloneNote.delete();
        Mockito.when(localRepository.deleteNote(note.getGuid())).thenReturn(Single.just(cloneNote));
        TestObserver<Note> testObserver = TestObserver.create();
        notesInteractor.deleteNote(note.getGuid()).subscribe(testObserver);

        testObserver.awaitTerminalEvent();
        testObserver.assertComplete()
                .assertNoErrors()
                .assertValue(cloneNote);

        Mockito.verify(localRepository, times(2)).getAllNotes();
    }

    @Test
    public void checkIsNeedUpdate_isCorrect() {
        HashMap<String, Note> notes = new HashMap<>();
        Note note = new Note("1", "Note", "Description");
        note.setLastUpdate(new Date(1));
        notes.put(note.getGuid(), note);

        Note firstNote = new Note("1", "Note", "Description");
        firstNote.setLastUpdate(new Date(1));
        assertFalse(notesInteractor.isNeedUpdate(notes, firstNote));

        firstNote.setName("UpdateNote");
        firstNote.setLastUpdate(new Date(1));
        assertFalse(notesInteractor.isNeedUpdate(notes, firstNote));

        firstNote.setLastUpdate(new Date(0));
        assertFalse(notesInteractor.isNeedUpdate(notes, firstNote));

        firstNote.setLastUpdate(new Date(2));
        assertTrue(notesInteractor.isNeedUpdate(notes, firstNote));

        Note secondNote = new Note("2", "Note", "Description");
        firstNote.setLastUpdate(new Date(1));
        assertTrue(notesInteractor.isNeedUpdate(notes, secondNote));
    }
}

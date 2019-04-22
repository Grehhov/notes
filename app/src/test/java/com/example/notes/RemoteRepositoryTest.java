package com.example.notes;

import com.example.notes.data.NotesApi;
import com.example.notes.data.RemoteRepositoryImpl;
import com.example.notes.domain.Note;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

@RunWith(MockitoJUnitRunner.class)
public class RemoteRepositoryTest {
    private RemoteRepositoryImpl remoteRepository;
    @Mock
    private NotesApi notesApi;

    @Before
    public void setUp() {
        NotesApi.NotesResponseBody notesResponseBody = new NotesApi.NotesResponseBody(0, new ArrayList<>());
        Mockito.when(notesApi.syncNotes(Mockito.any(NotesApi.NotesRequestBody.class)))
                .thenReturn(Single.just(notesResponseBody));
        remoteRepository = new RemoteRepositoryImpl(notesApi);
    }

    @Test
    public void syncNotes_isCorrectly() {
        TestObserver<List<Note>> testObserver = TestObserver.create();
        remoteRepository.syncNotes(new ArrayList<>())
                .subscribe(testObserver);

        testObserver.awaitTerminalEvent();
        testObserver.assertNoErrors()
                .assertComplete()
                .assertValue(notes -> notes.size() == 0);
        Mockito.verify(notesApi).syncNotes(Mockito.any(NotesApi.NotesRequestBody.class));
    }
}

package com.example.notes.di;

import com.example.notes.NoteFragment;
import com.example.notes.NotesFragment;
import com.example.notes.OptionsFragment;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = {ContextModule.class, RepositoryModule.class, ViewModelModule.class})
@Singleton
public interface AppComponent {
    void inject(NoteFragment noteFragment);
    void inject(NotesFragment notesFragment);
    void inject(OptionsFragment optionsFragment);
}


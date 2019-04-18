package com.example.notes.di;

import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.example.notes.presentation.NoteViewModel;
import com.example.notes.presentation.NotesViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(NotesViewModel.class)
    abstract ViewModel notesViewModel(@NonNull NotesViewModel notesViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(NoteViewModel.class)
    abstract ViewModel noteViewModel(@NonNull NoteViewModel noteViewModel);
}
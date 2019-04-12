package com.example.notes;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

public class CustomViewModelFactory implements ViewModelProvider.Factory {
    @NonNull
    private final Map<Class<? extends ViewModel>, Provider<ViewModel>> viewModels;

    @Inject
    public CustomViewModelFactory(@NonNull Map<Class<? extends ViewModel>, Provider<ViewModel>> viewModels) {
        this.viewModels = viewModels;
    }

    @Override
    @SuppressWarnings("unchecked")
    @NonNull
    public <T extends ViewModel> T create(@NonNull Class<T> viewModelClass) {
        Provider<ViewModel> viewModelProvider = viewModels.get(viewModelClass);
        if (viewModelProvider == null) {
            throw new IllegalArgumentException("ViewModel class " + viewModelClass + " not found");
        }
        return (T) viewModelProvider.get();
    }
}
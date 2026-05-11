package com.dilanne.bypass.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.dilanne.bypass.data.repository.PasswordRepository;
import com.dilanne.bypass.models.PasswordEntry;

import java.util.List;

public class PasswordViewModel extends AndroidViewModel {
    private final PasswordRepository repository;
    private final LiveData<List<PasswordEntry>> allPasswords;

    public PasswordViewModel(@NonNull Application application) {
        super(application);
        repository = new PasswordRepository(application);
        allPasswords = repository.getAllPasswords();
    }

    public LiveData<List<PasswordEntry>> getAllPasswords() {
        return allPasswords;
    }

    public void insert(PasswordEntry password, String plainPassword) {
        repository.insert(password, plainPassword);
    }

    public void update(PasswordEntry password, String plainPassword) {
        repository.update(password, plainPassword);
    }

    public void delete(PasswordEntry password) {
        repository.delete(password);
    }

    public String decryptPassword(String encrypted) {
        return repository.decryptPassword(encrypted);
    }

    public void syncFromRemote() {
        repository.syncFromRemote();
    }
}

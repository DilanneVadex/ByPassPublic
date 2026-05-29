package com.dilanne.bypass.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dilanne.bypass.data.repository.PasswordRepository;
import com.dilanne.bypass.models.PasswordEntry;

import java.util.List;

public class PasswordViewModel extends AndroidViewModel {
    private final PasswordRepository repository;
    private final LiveData<List<PasswordEntry>> allPasswords;

    private final MutableLiveData<Boolean> isScanning = new MutableLiveData<>(false);

    public PasswordViewModel(@NonNull Application application) {
        super(application);
        repository = new PasswordRepository(application);
        allPasswords = repository.getAllPasswords();
    }

    public LiveData<List<PasswordEntry>> getAllPasswords() {
        return allPasswords;
    }

    public LiveData<Boolean> isCryptoReady() {
        return repository.isCryptoReady();
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

    public void initializeCrypto(String password) {
        repository.initializeCrypto(password);
    }

    public void initializeCryptoSocial(Runnable onComplete) {
        repository.initializeCryptoSocial(onComplete);
    }

    public String getDerivedKey() {
        return repository.getDerivedKey();
    }

    public void restoreCrypto(String base64Key) {
        repository.restoreCrypto(base64Key);
    }

    public void syncFromRemote() {
        repository.syncFromRemote();
    }

    public void startRealtimeSync() {
        repository.startRealtimeSync();
    }

    public void stopRealtimeSync() {
        repository.stopRealtimeSync();
    }

    public LiveData<Boolean> getIsScanning() {
        return isScanning;
    }

    public void forceSecurityRefresh() {
        isScanning.setValue(true);
        repository.forceSecurityRefresh(() -> isScanning.postValue(false));
    }
}

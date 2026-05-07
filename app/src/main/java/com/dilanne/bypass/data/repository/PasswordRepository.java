package com.dilanne.bypass.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.dilanne.bypass.data.local.AppDatabase;
import com.dilanne.bypass.data.local.PasswordDao;
import com.dilanne.bypass.models.PasswordEntry;
import com.dilanne.bypass.security.CryptoManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PasswordRepository {
    private final PasswordDao passwordDao;
    private final LiveData<List<PasswordEntry>> allPasswords;
    private final CryptoManager cryptoManager;
    private final ExecutorService executorService;

    public PasswordRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        passwordDao = db.passwordDao();
        allPasswords = passwordDao.getAllPasswords();
        cryptoManager = new CryptoManager();
        executorService = Executors.newFixedThreadPool(2);
    }

    public LiveData<List<PasswordEntry>> getAllPasswords() {
        return allPasswords;
    }

    public void insert(PasswordEntry password, String plainPassword) {
        executorService.execute(() -> {
            String encrypted = cryptoManager.encrypt(plainPassword);
            password.setEncryptedPassword(encrypted);
            passwordDao.insert(password);
        });
    }

    public void update(PasswordEntry password, String plainPassword) {
        executorService.execute(() -> {
            String encrypted = cryptoManager.encrypt(plainPassword);
            password.setEncryptedPassword(encrypted);
            passwordDao.update(password);
        });
    }

    public void delete(PasswordEntry password) {
        executorService.execute(() -> passwordDao.delete(password));
    }

    public String decryptPassword(String encryptedPassword) {
        return cryptoManager.decrypt(encryptedPassword);
    }
}

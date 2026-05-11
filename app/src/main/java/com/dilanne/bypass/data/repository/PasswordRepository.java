package com.dilanne.bypass.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.dilanne.bypass.data.local.AppDatabase;
import com.dilanne.bypass.data.local.PasswordDao;
import com.dilanne.bypass.data.remote.RemoteSyncManager;
import com.dilanne.bypass.models.PasswordEntry;
import com.dilanne.bypass.security.CryptoManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PasswordRepository {
    private final PasswordDao passwordDao;
    private final LiveData<List<PasswordEntry>> allPasswords;
    private final CryptoManager cryptoManager;
    private final RemoteSyncManager remoteSyncManager;
    private final ExecutorService executorService;

    public PasswordRepository(Application application) {
        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        AppDatabase db = AppDatabase.getDatabase(application, userId);
        passwordDao = db.passwordDao();
        allPasswords = passwordDao.getAllPasswords();
        cryptoManager = new CryptoManager();
        remoteSyncManager = new RemoteSyncManager();
        executorService = Executors.newFixedThreadPool(2);
    }

    public LiveData<List<PasswordEntry>> getAllPasswords() {
        return allPasswords;
    }

    public void insert(PasswordEntry password, String plainPassword) {
        executorService.execute(() -> {
            String encrypted = cryptoManager.encrypt(plainPassword);
            password.setEncryptedPassword(encrypted);
            long id = passwordDao.insert(password);
            password.setId((int) id);
            remoteSyncManager.syncPassword(password);
        });
    }

    public void update(PasswordEntry password, String plainPassword) {
        executorService.execute(() -> {
            String encrypted = cryptoManager.encrypt(plainPassword);
            password.setEncryptedPassword(encrypted);
            passwordDao.update(password);
            remoteSyncManager.syncPassword(password);
        });
    }

    public void delete(PasswordEntry password) {
        executorService.execute(() -> {
            passwordDao.delete(password);
            remoteSyncManager.deletePassword(password.getId());
        });
    }

    public String decryptPassword(String encryptedPassword) {
        return cryptoManager.decrypt(encryptedPassword);
    }

    public void syncFromRemote() {
        remoteSyncManager.fetchPasswords(new RemoteSyncManager.SyncCallback() {
            @Override
            public void onSyncComplete(List<PasswordEntry> passwords) {
                executorService.execute(() -> {
                    for (PasswordEntry remoteEntry : passwords) {
                        // Check if entry already exists locally to avoid duplicates or update them
                        // For simplicity, we can use an insert-or-replace strategy in DAO if not already there
                        passwordDao.insert(remoteEntry);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                // Log or handle error
            }
        });
    }
}

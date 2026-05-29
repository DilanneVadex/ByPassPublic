package com.dilanne.bypass.data.repository;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dilanne.bypass.data.local.AppDatabase;
import com.dilanne.bypass.data.local.PasswordDao;
import com.dilanne.bypass.data.remote.RemoteSyncManager;
import com.dilanne.bypass.models.PasswordEntry;
import com.dilanne.bypass.security.CryptoManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PasswordRepository {
    private static final String TAG = "PasswordRepository";
    private final PasswordDao passwordDao;
    private final LiveData<List<PasswordEntry>> allPasswords;
    private final CryptoManager cryptoManager;
    private final RemoteSyncManager remoteSyncManager;
    private final ExecutorService executorService;
    private final com.dilanne.bypass.util.SecurityScanner securityScanner;
    private final Application application;
    private final MutableLiveData<Boolean> isCryptoReady = new MutableLiveData<>(false);

    public PasswordRepository(Application application) {
        this(application, new RemoteSyncManager());
    }

    public PasswordRepository(Application application, RemoteSyncManager remoteSyncManager) {
        this.application = application;
        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        AppDatabase db = AppDatabase.getDatabase(application, userId);
        passwordDao = db.passwordDao();
        allPasswords = passwordDao.getAllPasswords();
        cryptoManager = new CryptoManager();
        this.remoteSyncManager = remoteSyncManager;
        executorService = Executors.newFixedThreadPool(2);
        securityScanner = new com.dilanne.bypass.util.SecurityScanner(application, passwordDao, cryptoManager);

        if (userId != null) {
            if (restoreKeyLocally(userId)) {
                isCryptoReady.postValue(true);
            }
            fetchKeyFromCloud(userId);
        }
    }

    public LiveData<Boolean> isCryptoReady() {
        return isCryptoReady;
    }

    private boolean restoreKeyLocally(String userId) {
        if (userId == null) return false;
        try {
            SharedPreferences prefs = com.dilanne.bypass.util.SecurePrefs.getEncryptedSharedPreferences(application);
            String base64Key = null;
            try {
                base64Key = prefs.getString("master_crypto_key_" + userId, null);
            } catch (Exception e) {
                Log.e(TAG, "Decryption failed for local crypto key. Clearing corrupted storage.", e);
                com.dilanne.bypass.util.SecurePrefs.handleCorruption(application);
                return false;
            }

            if (base64Key != null) {
                cryptoManager.setDerivedKeyFromBase64(base64Key);
                Log.d(TAG, "Crypto key restored from local storage");
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to auto-restore crypto key", e);
        }
        return false;
    }

    private void fetchKeyFromCloud(String userId) {
        if (userId == null) return;
        remoteSyncManager.fetchMasterKey(new RemoteSyncManager.KeyFetchCallback() {
            @Override
            public void onKeyFetched(String base64Key) {
                if (base64Key != null) {
                    cryptoManager.setDerivedKeyFromBase64(base64Key);
                    saveKeyLocally(userId, base64Key);
                    isCryptoReady.postValue(true);
                    Log.d(TAG, "Crypto key restored from Cloud");
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to fetch key from cloud", e);
            }
        });
    }

    private void saveKeyLocally(String userId, String base64Key) {
        try {
            SharedPreferences prefs = com.dilanne.bypass.util.SecurePrefs.getEncryptedSharedPreferences(application);
            prefs.edit().putString("master_crypto_key_" + userId, base64Key).apply();
        } catch (Exception e) {
            Log.e(TAG, "Failed to save key locally", e);
        }
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

    private com.google.firebase.firestore.ListenerRegistration realtimeRegistration;

    public void startRealtimeSync() {
        if (realtimeRegistration != null) return;
        
        realtimeRegistration = remoteSyncManager.startRealtimeSync(new RemoteSyncManager.SyncCallback() {
            @Override
            public void onSyncComplete(List<PasswordEntry> passwords) {
                executorService.execute(() -> {
                    passwordDao.deleteAll();
                    passwordDao.insertAll(passwords);
                    securityScanner.scanAndNotify(passwords);
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Realtime sync error", e);
            }
        });
    }

    public void stopRealtimeSync() {
        if (realtimeRegistration != null) {
            realtimeRegistration.remove();
            realtimeRegistration = null;
        }
    }

    public String decryptPassword(String encryptedPassword) {
        return cryptoManager.decrypt(encryptedPassword);
    }

    public void initializeCrypto(String password) {
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            cryptoManager.updateMasterKey(password, uid);
            String base64Key = cryptoManager.getDerivedKeyBase64();
            if (base64Key != null) {
                saveKeyLocally(uid, base64Key);
                remoteSyncManager.syncMasterKey(base64Key);
                isCryptoReady.postValue(true);
            }
        }
    }

    public void initializeCryptoSocial(Runnable onComplete) {
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        remoteSyncManager.fetchMasterKey(new RemoteSyncManager.KeyFetchCallback() {
            @Override
            public void onKeyFetched(String base64Key) {
                if (base64Key != null) {
                    cryptoManager.setDerivedKeyFromBase64(base64Key);
                    saveKeyLocally(uid, base64Key);
                    isCryptoReady.postValue(true);
                    Log.d(TAG, "Social login: Crypto key restored from Cloud");
                } else {
                    cryptoManager.generateRandomMasterKey();
                    String newKey = cryptoManager.getDerivedKeyBase64();
                    if (newKey != null) {
                        saveKeyLocally(uid, newKey);
                        remoteSyncManager.syncMasterKey(newKey);
                        isCryptoReady.postValue(true);
                        Log.d(TAG, "Social login: New random crypto key generated and synced");
                    }
                }
                if (onComplete != null) onComplete.run();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Social login: Failed to fetch key", e);
                if (onComplete != null) onComplete.run();
            }
        });
    }

    public String getDerivedKey() {
        return cryptoManager.getDerivedKeyBase64();
    }

    public void restoreCrypto(String base64Key) {
        cryptoManager.setDerivedKeyFromBase64(base64Key);
    }

    public void syncFromRemote() {
        remoteSyncManager.fetchPasswords(new RemoteSyncManager.SyncCallback() {
            @Override
            public void onSyncComplete(List<PasswordEntry> passwords) {
                executorService.execute(() -> {
                    for (PasswordEntry remoteEntry : passwords) {
                        passwordDao.insert(remoteEntry);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Fetch remote passwords error", e);
            }
        });
    }

    public void forceSecurityRefresh(Runnable onComplete) {
        executorService.execute(() -> {
            List<PasswordEntry> passwords = passwordDao.getAllPasswordsSync();
            if (passwords != null && !passwords.isEmpty()) {
                securityScanner.scanAndNotify(passwords);
            }
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }
}

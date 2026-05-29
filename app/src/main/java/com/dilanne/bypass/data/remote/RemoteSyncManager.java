package com.dilanne.bypass.data.remote;

import android.util.Log;

import com.dilanne.bypass.models.PasswordEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class RemoteSyncManager {
    private static final String TAG = "RemoteSyncManager";
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_PASSWORDS = "passwords";
    
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public RemoteSyncManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private String getUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public void syncPassword(PasswordEntry entry) {
        String userId = getUserId();
        if (userId == null) return;

        // On crée une map pour Firestore (on ne veut pas forcément tout envoyer tel quel)
        Map<String, Object> data = new HashMap<>();
        data.put("id", entry.getId());
        data.put("title", entry.getTitle());
        data.put("email", entry.getEmail());
        data.put("url", entry.getUrl());
        data.put("encryptedPassword", entry.getEncryptedPassword());
        data.put("category", entry.getCategory());
        data.put("isFavorite", entry.isFavorite());
        data.put("lastModified", entry.getLastModified());

        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_PASSWORDS)
                .document(String.valueOf(entry.getId()))
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Password synced to Firestore"))
                .addOnFailureListener(e -> Log.e(TAG, "Error syncing password", e));
    }

    public void syncMasterKey(String base64Key) {
        syncMasterKey(base64Key, null);
    }

    public void syncMasterKey(String base64Key, final SyncStatusCallback callback) {
        String userId = getUserId();
        if (userId == null) {
            if (callback != null) callback.onError(new Exception("User not logged in"));
            return;
        }

        Log.d(TAG, "Syncing master key for user: " + userId);
        Map<String, Object> data = new HashMap<>();
        data.put("masterKey", base64Key);

        db.collection(COLLECTION_USERS)
                .document(userId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Master key synced to Firestore successfully");
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error syncing master key to Firestore", e);
                    if (callback != null) callback.onError(e);
                });
    }

    public interface SyncStatusCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public void fetchMasterKey(KeyFetchCallback callback) {
        String userId = getUserId();
        if (userId == null) return;

        db.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.onKeyFetched(documentSnapshot.getString("masterKey"));
                    } else {
                        callback.onKeyFetched(null);
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    public interface KeyFetchCallback {
        void onKeyFetched(String base64Key);
        void onError(Exception e);
    }

    public void deletePassword(int entryId) {
        String userId = getUserId();
        if (userId == null) return;

        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_PASSWORDS)
                .document(String.valueOf(entryId))
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Password deleted from Firestore"))
                .addOnFailureListener(e -> Log.e(TAG, "Error deleting password", e));
    }

    public interface SyncCallback {
        void onSyncComplete(java.util.List<PasswordEntry> passwords);
        void onError(Exception e);
    }

    public com.google.firebase.firestore.ListenerRegistration startRealtimeSync(SyncCallback callback) {
        String userId = getUserId();
        if (userId == null) return null;

        return db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_PASSWORDS)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        callback.onError(error);
                        return;
                    }

                    if (value != null) {
                        java.util.List<PasswordEntry> entries = new java.util.ArrayList<>();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value) {
                            try {
                                PasswordEntry entry = new PasswordEntry();
                                Long idLong = doc.getLong("id");
                                if (idLong != null) {
                                    entry.setId(idLong.intValue());
                                }
                                entry.setTitle(doc.getString("title"));
                                entry.setEmail(doc.getString("email"));
                                entry.setEncryptedPassword(doc.getString("encryptedPassword"));
                                entry.setUrl(doc.getString("url"));
                                entry.setCategory(doc.getString("category"));
                                entry.setFavorite(Boolean.TRUE.equals(doc.getBoolean("isFavorite")));
                                Long lastMod = doc.getLong("lastModified");
                                if (lastMod != null) {
                                    entry.setLastModified(lastMod);
                                }
                                entries.add(entry);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing document: " + doc.getId(), e);
                            }
                        }
                        callback.onSyncComplete(entries);
                    }
                });
    }

    public void fetchPasswords(SyncCallback callback) {
        String userId = getUserId();
        if (userId == null) return;

        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_PASSWORDS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    java.util.List<PasswordEntry> entries = new java.util.ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            PasswordEntry entry = new PasswordEntry();
                            Long idLong = doc.getLong("id");
                            if (idLong != null) {
                                entry.setId(idLong.intValue());
                            }
                            entry.setTitle(doc.getString("title"));
                            entry.setEmail(doc.getString("email"));
                            entry.setEncryptedPassword(doc.getString("encryptedPassword"));
                            entry.setUrl(doc.getString("url"));
                            entry.setCategory(doc.getString("category"));
                            entry.setFavorite(Boolean.TRUE.equals(doc.getBoolean("isFavorite")));
                            Long lastMod = doc.getLong("lastModified");
                            if (lastMod != null) {
                                entry.setLastModified(lastMod);
                            }
                            entries.add(entry);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing document: " + doc.getId(), e);
                        }
                    }
                    callback.onSyncComplete(entries);
                })
                .addOnFailureListener(callback::onError);
    }
}

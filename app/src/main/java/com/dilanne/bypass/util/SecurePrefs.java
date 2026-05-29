package com.dilanne.bypass.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class SecurePrefs {
    private static final String TAG = "SecurePrefs";
    private static final String PREFS_FILE = "secret_shared_prefs";

    public static SharedPreferences getEncryptedSharedPreferences(Context context) throws Exception {
        MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        try {
            return create(context, masterKey);
        } catch (Exception e) {
            Log.e(TAG, "Error creating EncryptedSharedPreferences, attempting to recover", e);
            handleCorruption(context);
            return create(context, masterKey);
        }
    }

    private static SharedPreferences create(Context context, MasterKey masterKey) throws GeneralSecurityException, IOException {
        return EncryptedSharedPreferences.create(
                context,
                PREFS_FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    /**
     * Clears the corrupted shared preferences file and its associated data.
     */
    public static void handleCorruption(Context context) {
        try {
            Log.w(TAG, "Deleting corrupted EncryptedSharedPreferences file: " + PREFS_FILE);
            // Official way to delete shared preferences
            context.deleteSharedPreferences(PREFS_FILE);
            
            // Note: Tink might leave some keyset files if they were stored separately, 
            // but EncryptedSharedPreferences usually keeps them in the same file or 
            // manages them. Deleting the pref file is usually enough for recovery.
        } catch (Exception e) {
            Log.e(TAG, "Failed to delete corrupted preferences", e);
        }
    }
}

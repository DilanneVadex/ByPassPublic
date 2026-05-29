package com.dilanne.bypass.security;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.security.KeyStore;
import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoManager {

    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "VaultPassKey";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    
    private SecretKey derivedKey;

    public CryptoManager() {
        try {
            initKey();
        } catch (Exception e) {
            Log.e("CryptoManager", "Initialization error", e);
        }
    }

    /**
     * Initializes the master key derived from the user's password and a salt (UID).
     */
    public void updateMasterKey(String password, String salt) {
        try {
            int iterations = 10000;
            int keyLength = 256;
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), iterations, keyLength);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            this.derivedKey = new SecretKeySpec(keyBytes, "AES");
            Log.d("CryptoManager", "Master key derived successfully from password");
        } catch (Exception e) {
            Log.e("CryptoManager", "Error deriving key from password", e);
        }
    }

    public String getDerivedKeyBase64() {
        if (derivedKey == null) return null;
        return Base64.encodeToString(derivedKey.getEncoded(), Base64.DEFAULT);
    }

    public void setDerivedKeyFromBase64(String base64Key) {
        try {
            byte[] decodedKey = Base64.decode(base64Key, Base64.DEFAULT);
            this.derivedKey = new SecretKeySpec(decodedKey, "AES");
            Log.d("CryptoManager", "Derived key restored from Base64");
        } catch (Exception e) {
            Log.e("CryptoManager", "Error restoring derived key", e);
        }
    }

    /**
     * Generates a new random master key. Used for social login users who don't have a password.
     */
    public void generateRandomMasterKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            this.derivedKey = keyGen.generateKey();
            Log.d("CryptoManager", "Random master key generated");
        } catch (Exception e) {
            Log.e("CryptoManager", "Error generating random master key", e);
        }
    }

    private void initKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build());
            keyGenerator.generateKey();
        }
    }

    private SecretKey getSecretKey() throws Exception {
        // Use password-derived key if available (for sync support)
        if (derivedKey != null) return derivedKey;
        
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);
        return ((KeyStore.SecretKeyEntry) keyStore.getEntry(KEY_ALIAS, null)).getSecretKey();
    }

    public String encrypt(String data) {
        if (data == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
            byte[] iv = cipher.getIV();
            byte[] encrypted = cipher.doFinal(data.getBytes());

            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.encodeToString(combined, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e("CryptoManager", "Encryption failed", e);
            return null;
        }
    }

    public String decrypt(String encryptedData) {
        if (encryptedData == null || encryptedData.isEmpty()) return "";
        try {
            byte[] combined = Base64.decode(encryptedData, Base64.DEFAULT);
            if (combined.length < 12) return "[Format Error]";

            byte[] iv = new byte[12];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            byte[] encrypted = new byte[combined.length - iv.length];
            System.arraycopy(combined, iv.length, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec);

            return new String(cipher.doFinal(encrypted));
        } catch (javax.crypto.AEADBadTagException e) {
            Log.e("CryptoManager", "Decryption failed: Integrity check failed. Possibly wrong key.");
            return "[Invalid Key]";
        } catch (Exception e) {
            Log.e("CryptoManager", "Decryption failed", e);
            return "[Error]";
        }
    }
}

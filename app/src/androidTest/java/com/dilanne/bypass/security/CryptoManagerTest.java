package com.dilanne.bypass.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class CryptoManagerTest {

    private CryptoManager cryptoManager;

    @Before
    public void setUp() {
        cryptoManager = new CryptoManager();
    }

    @Test
    public void testEncryptionDecryption() {
        String originalData = "MySecretPassword123!";
        String encrypted = cryptoManager.encrypt(originalData);
        
        assertNotNull("Encrypted data should not be null", encrypted);
        assertNotEquals("Encrypted data should be different from original", originalData, encrypted);
        
        String decrypted = cryptoManager.decrypt(encrypted);
        assertEquals("Decrypted data should match original", originalData, decrypted);
    }

    @Test
    public void testEncryptionWithDerivedKey() {
        String password = "masterPassword";
        String salt = "user_uid_123";
        cryptoManager.updateMasterKey(password, salt);
        
        String originalData = "SensitiveData";
        String encrypted = cryptoManager.encrypt(originalData);
        
        // Create a new instance and set the same key
        CryptoManager secondManager = new CryptoManager();
        secondManager.updateMasterKey(password, salt);
        
        String decrypted = secondManager.decrypt(encrypted);
        assertEquals("Decrypted data should match original with same derived key", originalData, decrypted);
    }
    
    @Test
    public void testDecryptionWithWrongKeyFails() {
        cryptoManager.updateMasterKey("password1", "salt");
        String encrypted = cryptoManager.encrypt("Secret");
        
        cryptoManager.updateMasterKey("password2", "salt");
        String decrypted = cryptoManager.decrypt(encrypted);
        
        assertNotEquals("Secret", decrypted);
    }
}

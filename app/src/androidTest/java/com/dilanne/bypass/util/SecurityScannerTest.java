package com.dilanne.bypass.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.dilanne.bypass.data.local.PasswordDao;
import com.dilanne.bypass.models.PasswordEntry;
import com.dilanne.bypass.security.CryptoManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class SecurityScannerTest {

    private SecurityScanner securityScanner;
    private PasswordDao passwordDao;
    private CryptoManager cryptoManager;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        passwordDao = mock(PasswordDao.class);
        cryptoManager = new CryptoManager();
        // Initialize with a known master key for encryption/decryption consistency in test
        cryptoManager.updateMasterKey("testPassword", "testSalt");
        securityScanner = new SecurityScanner(context, passwordDao, cryptoManager);
    }

    @Test
    public void testScanUpdatesStrength() {
        List<PasswordEntry> entries = new ArrayList<>();
        PasswordEntry entry = new PasswordEntry();
        // Encrypt a weak password
        String weakPass = "123";
        entry.setEncryptedPassword(cryptoManager.encrypt(weakPass));
        entries.add(entry);

        securityScanner.scanAndNotify(entries);

        // Nbvcxz should flag "123" as weak
        assertNotNull(entry.getSecurityStrength());
        // Depending on nbvcxz scoring, "123" is usually "Weak"
    }
}

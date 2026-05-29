package com.dilanne.bypass.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.dilanne.bypass.data.repository.PasswordRepository;
import com.dilanne.bypass.data.remote.RemoteSyncManager;
import com.dilanne.bypass.models.PasswordEntry;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class PasswordRepositoryTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private PasswordRepository repository;
    private RemoteSyncManager remoteSyncManager;
    private Application application;

    @Before
    public void setUp() {
        application = ApplicationProvider.getApplicationContext();
        remoteSyncManager = mock(RemoteSyncManager.class);
        repository = new PasswordRepository(application, remoteSyncManager);
    }

    @Test
    public void testInsertAndSyncCallsRemote() throws InterruptedException {
        PasswordEntry entry = new PasswordEntry();
        entry.setTitle("Integration Test");
        entry.setEmail("test@sync.com");
        
        repository.insert(entry, "plainPassword");
        
        // Wait for executor
        Thread.sleep(1000);
        
        // Verify remote sync was called
        verify(remoteSyncManager, atLeastOnce()).syncPassword(any(PasswordEntry.class));
    }

    @Test
    public void testSyncFromRemoteUpdatesLocal() throws InterruptedException {
        List<PasswordEntry> remotePasswords = new ArrayList<>();
        PasswordEntry remoteEntry = new PasswordEntry();
        remoteEntry.setId(999);
        remoteEntry.setTitle("Remote Account");
        remotePasswords.add(remoteEntry);

        // Mock fetchPasswords to call the callback with remotePasswords
        doAnswer(invocation -> {
            RemoteSyncManager.SyncCallback callback = invocation.getArgument(0);
            callback.onSyncComplete(remotePasswords);
            return null;
        }).when(remoteSyncManager).fetchPasswords(any(RemoteSyncManager.SyncCallback.class));

        repository.syncFromRemote();

        // Wait for background tasks
        Thread.sleep(1000);

        // Verify local observation (Wait for data to appear in LiveData)
        CountDownLatch latch = new CountDownLatch(1);
        repository.getAllPasswords().observeForever(passwords -> {
            if (passwords != null) {
                for (PasswordEntry p : passwords) {
                    if (p.getId() == 999) {
                        latch.countDown();
                    }
                }
            }
        });

        assertTrue("Remote password should be synced to local DB", latch.await(5, TimeUnit.SECONDS));
    }
}

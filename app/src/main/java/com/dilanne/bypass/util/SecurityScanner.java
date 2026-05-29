package com.dilanne.bypass.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.dilanne.bypass.R;
import com.dilanne.bypass.api.HibpService;
import com.dilanne.bypass.models.PasswordEntry;
import com.dilanne.bypass.security.CryptoManager;
import java.util.List;
import me.gosimple.nbvcxz.Nbvcxz;
import me.gosimple.nbvcxz.scoring.Result;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class SecurityScanner {
    private static final String CHANNEL_ID = "security_notifications";
    private final Context context;
    private final CryptoManager cryptoManager;
    private final Nbvcxz nbvcxz;
    private final HibpService hibpService;
    private final com.dilanne.bypass.data.local.PasswordDao passwordDao;

    public SecurityScanner(Context context, com.dilanne.bypass.data.local.PasswordDao passwordDao, CryptoManager cryptoManager) {
        this.context = context;
        this.passwordDao = passwordDao;
        this.cryptoManager = cryptoManager;
        this.nbvcxz = new Nbvcxz();
        
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.pwnedpasswords.com/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        this.hibpService = retrofit.create(HibpService.class);
    }

    public void scanAndNotify(List<PasswordEntry> passwords) {
        if (passwords == null || passwords.isEmpty()) return;

        boolean notificationsEnabled = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
                .getBoolean("notifications_enabled", true);

        int weakCount = 0;
        int totalEntropy = 0;

        int decryptedCount = 0;
        for (PasswordEntry entry : passwords) {
            String plain = cryptoManager.decrypt(entry.getEncryptedPassword());
            if (plain == null || plain.isEmpty() || plain.startsWith("[")) {
                // Skip entries that cannot be decrypted (wrong key or corrupted)
                continue;
            }
            decryptedCount++;

            // 1. Nbvcxz Analysis
            Result result = nbvcxz.estimate(plain);
            double entropy = result.getEntropy();
            totalEntropy += entropy;

            if (entropy < 40) {
                entry.setSecurityStrength(context.getString(R.string.strength_weak));
                entry.setSecurityStrengthColor(Color.parseColor("#F44336"));
                weakCount++;
            } else if (entropy < 80) {
                entry.setSecurityStrength(context.getString(R.string.strength_medium));
                entry.setSecurityStrengthColor(Color.parseColor("#FF9800"));
            } else {
                entry.setSecurityStrength(context.getString(R.string.strength_strong));
                entry.setSecurityStrengthColor(Color.parseColor("#4CAF50"));
            }

            // 2. HIBP Check (Synchronous for background scan)
            try {
                String sha1 = HashUtils.getSha1(plain);
                if (sha1 != null) {
                    String prefix = sha1.substring(0, 5);
                    String suffix = sha1.substring(5).toUpperCase();
                    retrofit2.Response<String> response = hibpService.getPasswordRange(prefix).execute();
                    if (response.isSuccessful() && response.body() != null) {
                        boolean found = response.body().contains(suffix);
                        if (found) {
                            entry.setSecurityStatus(context.getString(R.string.status_compromised));
                            entry.setSecurityStatusColor(Color.parseColor("#F44336"));
                            entry.setCompromised(true);
                        } else {
                            entry.setSecurityStatus(context.getString(R.string.status_secure));
                            entry.setSecurityStatusColor(Color.parseColor("#4CAF50"));
                            entry.setCompromised(false);
                        }
                    }
                }
            } catch (Exception e) {
                entry.setSecurityStatus(context.getString(R.string.status_error));
                entry.setSecurityStatusColor(Color.GRAY);
            }

            // Save individual entry progress back to DB
            passwordDao.update(entry);
        }

        int avgScore = decryptedCount > 0 ? (int) (totalEntropy / (double) decryptedCount) : 0;
        int globalPercent = Math.min(100, (avgScore * 100) / 100);

        context.getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
                .edit()
                .putInt("global_score", globalPercent)
                .apply();

        if (notificationsEnabled) {
            showSecurityScoreNotification(globalPercent);
            if (weakCount > 0) {
                showActionRequiredNotification(weakCount);
            }
        }
    }

    private String getSha1(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().toUpperCase();
        } catch (java.security.NoSuchAlgorithmException e) {
            return null;
        }
    }

    private void showSecurityScoreNotification(int score) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel(notificationManager);

        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(
                context, 0, new android.content.Intent(context, com.dilanne.bypass.MainActivity.class), 
                android.app.PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher) // Use launcher icon as fallback
                .setContentTitle(context.getString(R.string.label_security_score))
                .setContentText("Global Security Score: " + score + "%")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        try {
            notificationManager.notify(1, builder.build());
        } catch (SecurityException e) {
            // Permission missing on Android 13+
        }
    }

    private void showActionRequiredNotification(int weakCount) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel(notificationManager);

        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(
                context, 1, new android.content.Intent(context, com.dilanne.bypass.ui.activities.SecurityActivity.class), 
                android.app.PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Security Alert")
                .setContentText("You have " + weakCount + " weak passwords. Please update them.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        try {
            notificationManager.notify(2, builder.build());
        } catch (SecurityException e) {
            // Permission missing
        }
    }

    private void createNotificationChannel(NotificationManager manager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Security Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notifications about vault security");
            manager.createNotificationChannel(channel);
        }
    }
}

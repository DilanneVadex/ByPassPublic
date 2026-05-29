package com.dilanne.bypass.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.dilanne.bypass.R;
import com.dilanne.bypass.auth.AuthManager;
import com.dilanne.bypass.databinding.ActivitySettingsBinding;
import com.dilanne.bypass.ui.viewmodels.PasswordViewModel;
import com.dilanne.bypass.util.LocaleHelper;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private PasswordViewModel passwordViewModel;
    private AuthManager authManager;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        passwordViewModel = new ViewModelProvider(this).get(PasswordViewModel.class);
        authManager = new AuthManager(this);

        binding.btnBack.setOnClickListener(v -> finish());
        
        loadUserInfo();
        //loadProfileImage();
        setupNotificationSwitch();
        setupClickListeners();
    }

    private void loadUserInfo() {
        FirebaseUser user = authManager.getCurrentUser();
        if (user != null) {
            binding.tvUserName.setText(user.getDisplayName() != null ? user.getDisplayName() : "Utilisateur");
            binding.tvUserEmail.setText(user.getEmail());
        }
    }

    private void setupNotificationSwitch() {
        boolean enabled = getSharedPreferences("settings_prefs", MODE_PRIVATE)
                .getBoolean("notifications_enabled", true);
        binding.switchNotifications.setChecked(enabled);
    }

//    private void loadProfileImage() {
//        // Load a downsampled version of the large login_img to avoid Canvas drawing limits
//        binding.ivProfileAvatar.post(() -> {
//            int width = binding.ivProfileAvatar.getWidth();
//            int height = binding.ivProfileAvatar.getHeight();
//            if (width <= 0 || height <= 0) return;
//
//            android.graphics.BitmapFactory.Options options = new android.graphics.BitmapFactory.Options();
//            options.inJustDecodeBounds = true;
//            android.graphics.BitmapFactory.decodeResource(getResources(), R.drawable.users, options);
//
//            options.inSampleSize = calculateInSampleSize(options, width, height);
//            options.inJustDecodeBounds = false;
//
//            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeResource(getResources(), R.drawable.users, options);
//            binding.ivProfileAvatar.setImageBitmap(bitmap);
//        });
//    }

    private int calculateInSampleSize(android.graphics.BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private void setupClickListeners() {
        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
                }
            }

            getSharedPreferences("settings_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("notifications_enabled", isChecked)
                    .apply();
        });

        binding.cvAccountSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });

        binding.cvSecurity.setOnClickListener(v -> {
            Intent intent = new Intent(this, SecurityActivity.class);
            startActivity(intent);
        });

        binding.cvGenerator.setOnClickListener(v -> {
            Intent intent = new Intent(this, GeneratorActivity.class);
            startActivity(intent);
        });

        binding.cvSupport.setOnClickListener(v -> {
            Intent intent = new Intent(this, SupportActivity.class);
            startActivity(intent);
        });

        binding.cvAbout.setOnClickListener(v -> {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        });

        binding.cvLanguage.setOnClickListener(v -> {
            Intent intent = new Intent(this, LanguageActivity.class);
            startActivity(intent);
        });

        binding.cvForceRefresh.setOnClickListener(v -> {
            Toast.makeText(this, R.string.toast_security_refresh_started, Toast.LENGTH_SHORT).show();
            passwordViewModel.forceSecurityRefresh();
        });

        binding.bottomNavigation.setSelectedItemId(R.id.nav_settings);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                finish();
                return true;
            } else if (itemId == R.id.nav_generator) {
                Intent intent = new Intent(this, GeneratorActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_security) {
                Intent intent = new Intent(this, SecurityActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            return true;
        });
    }
}

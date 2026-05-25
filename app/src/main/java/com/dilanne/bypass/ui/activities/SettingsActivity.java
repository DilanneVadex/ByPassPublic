package com.dilanne.bypass.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.dilanne.bypass.R;
import com.dilanne.bypass.databinding.ActivitySettingsBinding;
import com.dilanne.bypass.util.LocaleHelper;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        
        loadProfileImage();
        setupClickListeners();
    }

    private void loadProfileImage() {
        // Load a downsampled version of the large login_img to avoid Canvas drawing limits
        binding.ivProfileAvatar.post(() -> {
            int width = binding.ivProfileAvatar.getWidth();
            int height = binding.ivProfileAvatar.getHeight();
            if (width <= 0 || height <= 0) return;

            android.graphics.BitmapFactory.Options options = new android.graphics.BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            android.graphics.BitmapFactory.decodeResource(getResources(), R.drawable.login_img, options);

            options.inSampleSize = calculateInSampleSize(options, width, height);
            options.inJustDecodeBounds = false;
            
            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeResource(getResources(), R.drawable.login_img, options);
            binding.ivProfileAvatar.setImageBitmap(bitmap);
        });
    }

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

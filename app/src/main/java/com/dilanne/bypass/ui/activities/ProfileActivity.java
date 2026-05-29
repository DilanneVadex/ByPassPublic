package com.dilanne.bypass.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import androidx.lifecycle.ViewModelProvider;

import com.dilanne.bypass.R;
import com.dilanne.bypass.auth.AuthManager;
import com.dilanne.bypass.databinding.ActivityProfileBinding;
import com.dilanne.bypass.ui.viewmodels.PasswordViewModel;
import com.dilanne.bypass.util.LocaleHelper;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private PasswordViewModel viewModel;
    private AuthManager authManager;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(PasswordViewModel.class);
        authManager = new AuthManager(this);

        setupUserInfo();
        setupFields();
        setupActions();
        loadProfileImage();
    }

    private void setupUserInfo() {
        FirebaseUser user = authManager.getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName() != null ? user.getDisplayName() : "Utilisateur";
            String email = user.getEmail();
            
            binding.tvUserName.setText(name);
            binding.tvUserEmail.setText(email);
        }
    }

    private void loadProfileImage() {
        binding.ivProfilePicture.post(() -> {
            int width = binding.ivProfilePicture.getWidth();
            int height = binding.ivProfilePicture.getHeight();
            if (width <= 0 || height <= 0) return;

            android.graphics.BitmapFactory.Options options = new android.graphics.BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            android.graphics.BitmapFactory.decodeResource(getResources(), R.drawable.users, options);

            options.inSampleSize = calculateInSampleSize(options, width, height);
            options.inJustDecodeBounds = false;

            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeResource(getResources(), R.drawable.users, options);
            binding.ivProfilePicture.setImageBitmap(bitmap);
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

    private void setupFields() {
        FirebaseUser user = authManager.getCurrentUser();
        String name = "Utilisateur";
        String email = "";
        if (user != null) {
            name = user.getDisplayName() != null ? user.getDisplayName() : "Utilisateur";
            email = user.getEmail();
        }

        // Name Field
        setupInputRow(binding.fieldName.getRoot(), getString(R.string.label_name), name);
        
        // Mail Field
        setupInputRow(binding.fieldMail.getRoot(), getString(R.string.label_mail), email);
        
        // Password Field
        setupInputRow(binding.fieldPassword.getRoot(), getString(R.string.label_password_current), "••••••••");
        
        // New Password Field
        setupInputRow(binding.fieldNewPassword.getRoot(), getString(R.string.label_new_password), "");
        ((android.widget.EditText)binding.fieldNewPassword.getRoot().findViewById(R.id.etValue)).setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        
        // Confirm Password Field
        setupInputRow(binding.fieldConfirmPassword.getRoot(), getString(R.string.label_confirm_password), "");
        ((android.widget.EditText)binding.fieldConfirmPassword.getRoot().findViewById(R.id.etValue)).setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }

    private void setupInputRow(View root, String label, String value) {
        TextView tvLabel = root.findViewById(R.id.tvLabel);
        TextView etValue = root.findViewById(R.id.etValue);
        
        tvLabel.setText(label);
        etValue.setText(value);
    }

    private void setupActions() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnConfirm.setOnClickListener(v -> {
            String newPass = ((android.widget.EditText)binding.fieldNewPassword.getRoot().findViewById(R.id.etValue)).getText().toString();
            String confirmPass = ((android.widget.EditText)binding.fieldConfirmPassword.getRoot().findViewById(R.id.etValue)).getText().toString();

            if (!newPass.isEmpty()) {
                if (!newPass.equals(confirmPass)) {
                    Toast.makeText(this, getString(R.string.error_passwords_dont_match), Toast.LENGTH_SHORT).show();
                    return;
                }
                // Ici on pourrait ajouter la logique de mise à jour du mot de passe Firebase
            }

            Toast.makeText(this, getString(R.string.toast_profile_updated), Toast.LENGTH_SHORT).show();
            finish();
        });

        binding.btnLogout.setOnClickListener(v -> {
            authManager.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
        
        binding.btnSync.setOnClickListener(v -> {
            Toast.makeText(this, getString(R.string.toast_syncing_cloud), Toast.LENGTH_SHORT).show();
            viewModel.syncFromRemote();
        });
    }
}

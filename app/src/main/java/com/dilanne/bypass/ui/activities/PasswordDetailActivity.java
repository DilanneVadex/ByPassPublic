package com.dilanne.bypass.ui.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.dilanne.bypass.R;
import com.dilanne.bypass.databinding.ActivityPasswordDetailsBinding;
import com.dilanne.bypass.models.PasswordEntry;
import com.dilanne.bypass.ui.viewmodels.PasswordViewModel;

public class PasswordDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PASSWORD_ENTRY = "extra_password_entry";
    
    private ActivityPasswordDetailsBinding binding;
    private PasswordViewModel viewModel;
    private PasswordEntry entry;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPasswordDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(PasswordViewModel.class);
        entry = (PasswordEntry) getIntent().getSerializableExtra(EXTRA_PASSWORD_ENTRY);

        if (entry == null) {
            finish();
            return;
        }

        setupUI();
        setupClickListeners();
    }

    private void setupUI() {
        binding.tvHeaderTitle.setText(entry.getTitle() + " Account");
        binding.tvTitle.setText(entry.getTitle());
        binding.tvEmail.setText(entry.getEmail());
        binding.tvLoginValue.setText(entry.getEmail()); // Assuming email is the login if no separate field
        
        // You might want to map category to icon here
        // binding.ivAppIcon.setImageResource(...);
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                String decrypted = viewModel.decryptPassword(entry.getEncryptedPassword());
                binding.tvPasswordValue.setText(decrypted);
                binding.btnTogglePassword.setImageResource(R.drawable.eye_2_line);
            } else {
                binding.tvPasswordValue.setText("••••••••");
                binding.btnTogglePassword.setImageResource(R.drawable.eye_close_line);
            }
        });

        binding.btnCopyLogin.setOnClickListener(v -> copyToClipboard("Login", binding.tvLoginValue.getText().toString()));
        binding.btnCopyPassword.setOnClickListener(v -> {
            String decrypted = viewModel.decryptPassword(entry.getEncryptedPassword());
            copyToClipboard("Password", decrypted);
        });

        binding.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddAccountActivity.class);
            intent.putExtra(AddAccountActivity.EXTRA_PASSWORD_ENTRY, entry);
            startActivity(intent);
            finish();
        });

        binding.btnDelete.setOnClickListener(v -> showDeleteConfirmation());
    }

    private void copyToClipboard(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, label + " copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete this account?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.delete(entry);
                    Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}

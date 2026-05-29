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

import com.bumptech.glide.Glide;
import com.dilanne.bypass.R;
import com.dilanne.bypass.databinding.ActivityPasswordDetailsBinding;
import com.dilanne.bypass.models.PasswordEntry;
import com.dilanne.bypass.ui.viewmodels.PasswordViewModel;
import com.dilanne.bypass.util.LocaleHelper;

import com.dilanne.bypass.util.LocaleHelper;

public class PasswordDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PASSWORD_ENTRY = "extra_password_entry";
    
    private ActivityPasswordDetailsBinding binding;
    private PasswordViewModel viewModel;
    private PasswordEntry entry;
    private boolean isPasswordVisible = false;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

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
        binding.tvHeaderTitle.setText(getString(R.string.account_details_header, entry.getTitle()));
        binding.tvTitle.setText(entry.getTitle());
        binding.tvEmail.setText(entry.getEmail());
        binding.tvLoginValue.setText(entry.getEmail());

        // Charger l'icône du site web avec Glide
        String faviconUrl = "";
        if (entry.getUrl() != null && !entry.getUrl().isEmpty()) {
            faviconUrl = "https://icons.duckduckgo.com/ip3/" + extractDomain(entry.getUrl()) + ".ico";
        }

        Glide.with(this)
                .load(faviconUrl)
                .placeholder(R.drawable.logo)
                .error(R.drawable.logo)
                .into(binding.ivAppIcon);
    }

    private String extractDomain(String url) {
        try {
            if (!url.startsWith("http")) {
                url = "https://" + url;
            }
            java.net.URI uri = new java.net.URI(url);
            String domain = uri.getHost();
            if (domain != null) {
                return domain.startsWith("www.") ? domain.substring(4) : domain;
            }
        } catch (Exception e) {
            return url;
        }
        return url;
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
        Toast.makeText(this, getString(R.string.copied_to_clipboard, label), Toast.LENGTH_SHORT).show();
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_confirmation_title)
                .setMessage(R.string.delete_confirmation_message)
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                    viewModel.delete(entry);
                    Toast.makeText(this, R.string.toast_account_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}

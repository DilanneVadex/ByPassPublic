package com.dilanne.bypass.ui.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.dilanne.bypass.databinding.ActivityAddAccountBinding;
import com.dilanne.bypass.models.PasswordEntry;
import com.dilanne.bypass.ui.viewmodels.PasswordViewModel;

public class AddAccountActivity extends AppCompatActivity {

    private ActivityAddAccountBinding binding;
    private PasswordViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(PasswordViewModel.class);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnCancel.setOnClickListener(v -> finish());
        binding.btnConfirm.setOnClickListener(v -> handleSave());

        setupLivePreview();
    }

    private void setupLivePreview() {
        android.text.TextWatcher watcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePreview();
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        };

        binding.etUrl.addTextChangedListener(watcher);
        binding.etMail.addTextChangedListener(watcher);
    }

    private void updatePreview() {
        String url = binding.etUrl.getText().toString().trim();
        String mail = binding.etMail.getText().toString().trim();
        
        String displayUrl = url.isEmpty() ? "Service" : url;
        String displayMail = mail.isEmpty() ? "email" : mail;
        
        binding.tvPreview.setText(displayUrl + " • " + displayMail);
    }

    private void handleSave() {
        String url = binding.etUrl.getText().toString().trim();
        String mail = binding.etMail.getText().toString().trim();
        String login = binding.etLogin.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (url.isEmpty() || login.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use the domain or a part of URL as title for now
        String title = url;
        if (title.contains(".")) {
            title = title.substring(0, title.lastIndexOf("."));
            if (title.startsWith("www.")) title = title.substring(4);
            title = title.substring(0, 1).toUpperCase() + title.substring(1);
        }

        PasswordEntry entry = new PasswordEntry();
        entry.setTitle(title);
        entry.setUrl(url);
        entry.setEmail(mail.isEmpty() ? login : mail); // Fallback to login if email is empty
        // In a real app, we'd handle category and favorite status too
        entry.setCategory("General");
        entry.setFavorite(false);

        viewModel.insert(entry, password);
        Toast.makeText(this, "Account added successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}

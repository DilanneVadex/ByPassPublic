package com.dilanne.bypass;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dilanne.bypass.databinding.ActivityMainBinding;
import com.dilanne.bypass.models.Category;
import com.dilanne.bypass.models.PasswordEntry;
import com.dilanne.bypass.ui.activities.AddAccountActivity;
import com.dilanne.bypass.ui.adapters.CategoryAdapter;
import com.dilanne.bypass.ui.adapters.PasswordAdapter;
import com.dilanne.bypass.ui.adapters.RecentAdapter;
import com.dilanne.bypass.ui.viewmodels.PasswordViewModel;
import com.dilanne.bypass.util.LocaleHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private PasswordViewModel viewModel;
    private PasswordAdapter passwordAdapter;
    private RecentAdapter recentAdapter;
    private CategoryAdapter categoryAdapter;

    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<String> selectedCategory = new MutableLiveData<>("");
    private final MediatorLiveData<List<PasswordEntry>> filteredPasswords = new MediatorLiveData<>();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Request Notification Permission for Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        viewModel = new ViewModelProvider(this).get(PasswordViewModel.class);

        // Sync data from Firebase
        viewModel.startRealtimeSync();

        setupRecyclerViews();
        setupSearch();
        observeViewModel();
        setupBottomNav();
        setupFab();
        setupMenu();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (viewModel != null) {
            viewModel.stopRealtimeSync();
        }
    }

    private void setupMenu() {
        binding.btnMenu.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.dilanne.bypass.ui.activities.SettingsActivity.class);
            startActivity(intent);
        });
    }

    private void setupFab() {
        binding.fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddAccountActivity.class);
            startActivity(intent);
        });
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery.setValue(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        View rootView = binding.getRoot();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            android.graphics.Rect r = new android.graphics.Rect();
            rootView.getWindowVisibleDisplayFrame(r);
            int screenHeight = rootView.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;

            if (keypadHeight > screenHeight * 0.15) { // Keyboard is visible
                binding.bottomNavigation.setVisibility(View.GONE);
                binding.fabAdd.setVisibility(View.GONE);
            } else { // Keyboard is hidden
                binding.bottomNavigation.setVisibility(View.VISIBLE);
                binding.fabAdd.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupRecyclerViews() {
        // Categories - Initially empty, will be populated based on data
        categoryAdapter = new CategoryAdapter(new ArrayList<>(), category -> {
            if (category == null) {
                selectedCategory.setValue("");
            } else {
                selectedCategory.setValue(category.getName());
            }
        });
        binding.rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvCategories.setAdapter(categoryAdapter);

        // Passwords List (Vertical)
        passwordAdapter = new PasswordAdapter(
                this::handleTogglePassword, 
                this::handleEditPassword,
                this::handleItemClick
        );
        binding.rvPasswords.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPasswords.setAdapter(passwordAdapter);

        // Recent List (Horizontal)
        recentAdapter = new RecentAdapter(this::handleItemClick);
        binding.rvFavoris.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvFavoris.setAdapter(recentAdapter);
    }

    private void observeViewModel() {
        // Observe security score from SharedPreferences
        updateSecurityScoreUI();

        filteredPasswords.addSource(viewModel.getAllPasswords(), passwords -> {
            filterAndPopulate(passwords, searchQuery.getValue(), selectedCategory.getValue());
        });

        filteredPasswords.addSource(searchQuery, query -> {
            filterAndPopulate(viewModel.getAllPasswords().getValue(), query, selectedCategory.getValue());
        });

        filteredPasswords.addSource(selectedCategory, category -> {
            filterAndPopulate(viewModel.getAllPasswords().getValue(), searchQuery.getValue(), category);
        });

        filteredPasswords.observe(this, passwords -> {
            passwordAdapter.submitList(passwords);
        });

        viewModel.getAllPasswords().observe(this, passwords -> {
            if (passwords != null) {
                // Update Recent based on lastModified timestamp
                List<PasswordEntry> recent = passwords.stream()
                        .sorted(Comparator.comparingLong(PasswordEntry::getLastModified).reversed())
                        .limit(5) // Show top 5 recent
                        .collect(Collectors.toList());
                recentAdapter.submitList(recent);

                // Update Categories: only show those that have passwords
                updateCategoriesList(passwords);
            }
        });
    }

    private void updateCategoriesList(List<PasswordEntry> passwords) {
        List<String> activeCategoryNames = passwords.stream()
                .map(PasswordEntry::getCategory)
                .filter(c -> c != null && !c.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        List<Category> allAvailable = Category.getAllCategories();
        List<Category> activeCategories = allAvailable.stream()
                .filter(c -> activeCategoryNames.contains(c.getName()))
                .collect(Collectors.toList());

        categoryAdapter.updateCategories(activeCategories);
        binding.sectionCategories.setVisibility(activeCategories.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void filterAndPopulate(List<PasswordEntry> passwords, String query, String category) {
        if (passwords == null) return;
        
        boolean isSearchingText = (query != null && !query.isEmpty());
        updateUIForSearch(isSearchingText);

        List<PasswordEntry> filtered = passwords.stream()
                .filter(p -> {
                    boolean matchesQuery = true;
                    if (query != null && !query.isEmpty()) {
                        String lowerQuery = query.toLowerCase();
                        matchesQuery = p.getTitle().toLowerCase().contains(lowerQuery) || 
                                       p.getEmail().toLowerCase().contains(lowerQuery);
                    }
                    
                    boolean matchesCategory = true;
                    if (category != null && !category.isEmpty()) {
                        matchesCategory = category.equalsIgnoreCase(p.getCategory());
                    }
                    
                    return matchesQuery && matchesCategory;
                })
                .collect(Collectors.toList());
        
        filteredPasswords.setValue(filtered);
    }

    private void updateUIForSearch(boolean isSearching) {
        int visibility = isSearching ? View.GONE : View.VISIBLE;
        binding.sectionSecurity.setVisibility(visibility);
        binding.sectionRecent.setVisibility(visibility);
        binding.sectionCategories.setVisibility(visibility);
        binding.sectionPasswordLabel.setVisibility(visibility);
    }

    private void updateSecurityScoreUI() {
        int score = getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
                .getInt("global_score", 0);

        binding.tvSecurityScoreValue.setText(score + "%");

        if (score < 40) {
            binding.tvSecurityScoreValue.setTextColor(getColor(R.color.red));
        } else if (score < 80) {
            binding.tvSecurityScoreValue.setTextColor(android.graphics.Color.parseColor("#FF9800")); // Orange
        } else {
            binding.tvSecurityScoreValue.setTextColor(getColor(R.color.green));
        }
    }

    private final android.content.SharedPreferences.OnSharedPreferenceChangeListener scoreListener =
            (sharedPreferences, key) -> {
                if ("global_score".equals(key)) {
                    runOnUiThread(this::updateSecurityScoreUI);
                }
            };

    @Override
    protected void onResume() {
        super.onResume();
        getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(scoreListener);
        updateSecurityScoreUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
                .unregisterOnSharedPreferenceChangeListener(scoreListener);
    }

    private void handleTogglePassword(PasswordEntry entry, PasswordAdapter.PasswordViewHolder holder) {
        if (!holder.isPasswordVisible()) {
            String decrypted = viewModel.decryptPassword(entry.getEncryptedPassword());
            if ("[Invalid Key]".equals(decrypted)) {
                android.widget.Toast.makeText(this, "Impossible de déchiffrer : Clé incorrecte ou données d'un autre appareil.", android.widget.Toast.LENGTH_LONG).show();
                holder.setPasswordText("********", false);
            } else if ("[Error]".equals(decrypted)) {
                android.widget.Toast.makeText(this, "Erreur de déchiffrement.", android.widget.Toast.LENGTH_SHORT).show();
            } else {
                holder.setPasswordText(decrypted, true);
            }
        } else {
            holder.setPasswordText("••••••••", false);
        }
    }

    private void handleEditPassword(PasswordEntry entry, View view) {
        Intent intent = new Intent(this, AddAccountActivity.class);
        intent.putExtra(AddAccountActivity.EXTRA_PASSWORD_ENTRY, entry);
        startActivity(intent);
    }

    private void handleItemClick(PasswordEntry entry, View view) {
        Intent intent = new Intent(this, com.dilanne.bypass.ui.activities.PasswordDetailActivity.class);
        intent.putExtra(com.dilanne.bypass.ui.activities.PasswordDetailActivity.EXTRA_PASSWORD_ENTRY, entry);
        startActivity(intent);
    }

    private void setupBottomNav() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_generator) {
                Intent intent = new Intent(this, com.dilanne.bypass.ui.activities.GeneratorActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_security) {
                Intent intent = new Intent(this, com.dilanne.bypass.ui.activities.SecurityActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_settings) {
                Intent intent = new Intent(this, com.dilanne.bypass.ui.activities.SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }
}

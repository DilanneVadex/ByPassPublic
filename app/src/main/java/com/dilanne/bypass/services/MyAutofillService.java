package com.dilanne.bypass.services;

import android.app.assist.AssistStructure;
import android.os.CancellationSignal;
import android.service.autofill.AutofillService;
import android.service.autofill.Dataset;
import android.service.autofill.FillCallback;
import android.service.autofill.FillContext;
import android.service.autofill.FillRequest;
import android.service.autofill.FillResponse;
import android.service.autofill.SaveCallback;
import android.service.autofill.SaveRequest;
import android.util.Log;
import android.util.Log;
import android.util.Log;
import android.util.Log;
import android.util.Log;
import android.util.Log;
import android.util.Log;
import android.util.Log;
import android.util.Log;
import android.util.Log;
import android.util.Log;
import android.view.View;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import android.widget.RemoteViews;
import android.util.Log;
import android.util.Log;
import android.util.Log;

import androidx.annotation.NonNull;

import com.dilanne.bypass.R;
import com.dilanne.bypass.data.local.AppDatabase;
import com.dilanne.bypass.data.local.PasswordDao;
import com.dilanne.bypass.models.PasswordEntry;
import com.dilanne.bypass.security.CryptoManager;
import com.dilanne.bypass.util.SecurePrefs;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyAutofillService extends AutofillService {

    @Override
    public void onFillRequest(@NonNull FillRequest request, @NonNull CancellationSignal cancellationSignal, @NonNull FillCallback callback) {
        List<FillContext> contexts = request.getFillContexts();
        if (contexts.isEmpty()) {
            callback.onSuccess(null);
            return;
        }
        AssistStructure structure = contexts.get(contexts.size() - 1).getStructure();

        List<AutofillId> emailIds = new ArrayList<>();
        List<AutofillId> passwordIds = new ArrayList<>();

        identifyFields(structure, emailIds, passwordIds);

        if (emailIds.isEmpty() && passwordIds.isEmpty()) {
            callback.onSuccess(null);
            return;
        }

        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            callback.onSuccess(null);
            return;
        }

        String url = extractUrl(structure);

        PasswordDao dao = AppDatabase.getDatabase(this, userId).passwordDao();
        List<PasswordEntry> entries = dao.getAllPasswordsSync();
        
        CryptoManager cryptoManager = new CryptoManager();
        // Restore crypto key if available in MyAutofillService
        try {
            android.content.SharedPreferences encryptedPrefs = SecurePrefs.getEncryptedSharedPreferences(this);
            String base64Key = null;
            try {
                base64Key = encryptedPrefs.getString("master_crypto_key_" + userId, null);
            } catch (Exception e) {
                Log.e("MyAutofillService", "Decryption failed for local crypto key. Clearing corrupted storage.", e);
                SecurePrefs.handleCorruption(this);
            }

            if (base64Key != null) {
                cryptoManager.setDerivedKeyFromBase64(base64Key);
            }
        } catch (Exception e) {
            // Ignore if key cannot be restored
        }

        // Filter entries by URL or Package Name if possible
        List<PasswordEntry> filteredEntries = new ArrayList<>();
        for (PasswordEntry entry : entries) {
            if (url.equals("Autofill Capture") || 
                (entry.getUrl() != null && (entry.getUrl().contains(url) || url.contains(entry.getUrl())))) {
                filteredEntries.add(entry);
            }
        }
        
        // If no match found for specific URL, show all as fallback or at least the most relevant ones
        if (filteredEntries.isEmpty()) {
            filteredEntries = entries;
        }

        //CryptoManager cryptoManager = new CryptoManager();

        FillResponse.Builder responseBuilder = new FillResponse.Builder();

        for (PasswordEntry entry : filteredEntries) {
            Dataset.Builder datasetBuilder = new Dataset.Builder();
            boolean added = false;

            String decryptedPassword = cryptoManager.decrypt(entry.getEncryptedPassword());
            if (decryptedPassword == null) decryptedPassword = "";

            RemoteViews presentation = new RemoteViews(getPackageName(), R.layout.item_autofill_suggestion);
            presentation.setTextViewText(R.id.tv_suggestion_title, entry.getTitle());
            presentation.setTextViewText(R.id.tv_suggestion_subtitle, entry.getEmail());

            for (AutofillId id : emailIds) {
                datasetBuilder.setValue(id, AutofillValue.forText(entry.getEmail()), presentation);
                added = true;
            }

            for (AutofillId id : passwordIds) {
                datasetBuilder.setValue(id, AutofillValue.forText(decryptedPassword), presentation);
                added = true;
            }

            if (added) {
                try {
                    responseBuilder.addDataset(datasetBuilder.build());
                } catch (Exception e) {
                    // Ignore errors for individual datasets
                }
            }
        }

        callback.onSuccess(responseBuilder.build());
    }

    private void identifyFields(AssistStructure structure, List<AutofillId> emailIds, List<AutofillId> passwordIds) {
        int nodes = structure.getWindowNodeCount();
        for (int i = 0; i < nodes; i++) {
            AssistStructure.WindowNode windowNode = structure.getWindowNodeAt(i);
            identifyFields(windowNode.getRootViewNode(), emailIds, passwordIds);
        }
    }

    private void identifyFields(AssistStructure.ViewNode node, List<AutofillId> emailIds, List<AutofillId> passwordIds) {
        if (node == null) return;

        String hint = node.getHint() != null ? node.getHint().toString().toLowerCase() : "";
        String idEntry = node.getIdEntry() != null ? node.getIdEntry().toLowerCase() : "";
        
        boolean isEmailField = hint.contains("email") || hint.contains("username") || hint.contains("login") ||
                             idEntry.contains("email") || idEntry.contains("username") || idEntry.contains("login") ||
                             (node.getAutofillType() == View.AUTOFILL_TYPE_TEXT && 
                              (node.getAutofillHints() != null && Arrays.asList(node.getAutofillHints()).contains(View.AUTOFILL_HINT_USERNAME)));
        
        boolean isPasswordField = hint.contains("password") || idEntry.contains("password") ||
                                (node.getAutofillType() == View.AUTOFILL_TYPE_TEXT && 
                                 (node.getAutofillHints() != null && Arrays.asList(node.getAutofillHints()).contains(View.AUTOFILL_HINT_PASSWORD)));

        if (isEmailField) {
            emailIds.add(node.getAutofillId());
        } else if (isPasswordField) {
            passwordIds.add(node.getAutofillId());
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            identifyFields(node.getChildAt(i), emailIds, passwordIds);
        }
    }

    @Override
    public void onSaveRequest(@NonNull SaveRequest request, @NonNull SaveCallback callback) {
        List<FillContext> contexts = request.getFillContexts();
        AssistStructure structure = contexts.get(contexts.size() - 1).getStructure();
        
        List<String> emails = new ArrayList<>();
        List<String> passwords = new ArrayList<>();
        String url = extractUrl(structure);

        traverseStructureForValues(structure, emails, passwords);

        if (!emails.isEmpty() && !passwords.isEmpty()) {
            saveCredentials(emails.get(0), passwords.get(0), url);
        }
        
        callback.onSuccess();
    }

    private void traverseStructureForValues(AssistStructure structure, List<String> emails, List<String> passwords) {
        int nodes = structure.getWindowNodeCount();
        for (int i = 0; i < nodes; i++) {
            AssistStructure.WindowNode windowNode = structure.getWindowNodeAt(i);
            traverseStructureForValues(windowNode.getRootViewNode(), emails, passwords);
        }
    }

    private void traverseStructureForValues(AssistStructure.ViewNode node, List<String> emails, List<String> passwords) {
        if (node == null) return;

        AutofillValue value = node.getAutofillValue();
        if (value != null && value.isText()) {
            String text = value.getTextValue().toString();
            String hint = node.getHint() != null ? node.getHint().toString().toLowerCase() : "";
            String idEntry = node.getIdEntry() != null ? node.getIdEntry().toLowerCase() : "";

            if (hint.contains("email") || hint.contains("username") || idEntry.contains("email") || idEntry.contains("username")) {
                emails.add(text);
            } else if (hint.contains("password") || idEntry.contains("password")) {
                passwords.add(text);
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            traverseStructureForValues(node.getChildAt(i), emails, passwords);
        }
    }

    private String extractUrl(AssistStructure structure) {
        if (structure == null) return "Unknown";

        // Try to get the web domain first if it's a browser
        int nodes = structure.getWindowNodeCount();
        for (int i = 0; i < nodes; i++) {
            AssistStructure.WindowNode windowNode = structure.getWindowNodeAt(i);
            String domain = findWebDomain(windowNode.getRootViewNode());
            if (domain != null) return domain;
        }

        // Fallback to package name
        if (structure.getActivityComponent() != null) {
            return structure.getActivityComponent().getPackageName();
        }

        return "Autofill Capture";
    }

    private String findWebDomain(AssistStructure.ViewNode node) {
        if (node == null) return null;
        if (node.getWebDomain() != null) return node.getWebDomain();

        for (int i = 0; i < node.getChildCount(); i++) {
            String domain = findWebDomain(node.getChildAt(i));
            if (domain != null) return domain;
        }
        return null;
    }

    private void saveCredentials(String email, String password, String url) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        PasswordDao dao = AppDatabase.getDatabase(this, userId).passwordDao();
        CryptoManager cryptoManager = new CryptoManager();
        
        PasswordEntry entry = new PasswordEntry();
        entry.setTitle(url);
        entry.setEmail(email);
        entry.setUrl(url);
        entry.setEncryptedPassword(cryptoManager.encrypt(password));
        entry.setCategory("Social"); // Default category

        new Thread(() -> {
            dao.insert(entry);
        }).start();
    }
}

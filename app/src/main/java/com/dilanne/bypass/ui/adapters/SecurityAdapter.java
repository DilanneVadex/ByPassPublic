package com.dilanne.bypass.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.dilanne.bypass.R;
import com.dilanne.bypass.databinding.ItemSecurityPasswordBinding;
import com.dilanne.bypass.models.PasswordEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecurityAdapter extends RecyclerView.Adapter<SecurityAdapter.SecurityViewHolder> {

    private List<PasswordEntry> passwords = new ArrayList<>();
    private final OnDecryptListener decryptListener;
    private final OnItemClickListener itemClickListener;

    public interface OnDecryptListener {
        String onDecrypt(String encryptedPassword);
    }

    public interface OnItemClickListener {
        void onItemClick(PasswordEntry entry, View view);
    }

    public SecurityAdapter(OnDecryptListener decryptListener, OnItemClickListener itemClickListener) {
        this.decryptListener = decryptListener;
        this.itemClickListener = itemClickListener;
    }

    public void setPasswords(List<PasswordEntry> passwords) {
        this.passwords = passwords;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SecurityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSecurityPasswordBinding binding = ItemSecurityPasswordBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SecurityViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SecurityViewHolder holder, int position) {
        PasswordEntry entry = passwords.get(position);
        holder.bind(entry);
    }

    @Override
    public int getItemCount() {
        return passwords.size();
    }

    public List<PasswordEntry> getPasswords() {
        return passwords;
    }

    class SecurityViewHolder extends RecyclerView.ViewHolder {
        private final ItemSecurityPasswordBinding binding;

        public SecurityViewHolder(ItemSecurityPasswordBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(PasswordEntry entry) {
            binding.tvServiceName.setText(entry.getTitle());
            binding.tvUserEmail.setText("• " + entry.getEmail());

            // Load favicon
            String domain = extractDomain(entry.getUrl());
            String faviconUrl = "https://icons.duckduckgo.com/ip3/" + domain + ".ico";

            com.bumptech.glide.Glide.with(binding.getRoot().getContext())
                    .load(faviconUrl)
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .into(binding.ivServiceIcon);

            String strength = entry.getSecurityStrength();
            String status = entry.getSecurityStatus();

            if (strength != null && status != null) {
                binding.tvStrengthBadge.setText(strength);
                binding.tvStrengthBadge.getBackground().setTint(entry.getSecurityStrengthColor());

                binding.tvCompromisedBadge.setText(status);
                binding.tvCompromisedBadge.getBackground().setTint(entry.getSecurityStatusColor());

                if (entry.isCompromised()) {
                    binding.ivStatusIcon.setImageResource(R.drawable.shield_flash_fill);
                    binding.ivStatusIcon.setColorFilter(Color.RED);
                } else if (binding.getRoot().getContext().getString(R.string.status_secure).equals(status)) {
                    binding.ivStatusIcon.setImageResource(R.drawable.shield_check_line);
                    binding.ivStatusIcon.setColorFilter(binding.getRoot().getContext().getColor(R.color.secondary));
                } else {
                    binding.ivStatusIcon.setImageResource(R.drawable.information_2_fill);
                    binding.ivStatusIcon.setColorFilter(Color.GRAY);
                }
            } else {
                binding.tvStrengthBadge.setText(binding.getRoot().getContext().getString(R.string.status_pending));
                binding.tvStrengthBadge.getBackground().setTint(Color.GRAY);
                binding.tvCompromisedBadge.setText(binding.getRoot().getContext().getString(R.string.status_pending));
                binding.tvCompromisedBadge.getBackground().setTint(Color.GRAY);
                binding.ivStatusIcon.setImageResource(R.drawable.information_2_fill);
                binding.ivStatusIcon.setColorFilter(Color.GRAY);
            }

            binding.getRoot().setOnClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(entry, v);
                }
            });
        }

        private String extractDomain(String url) {
            if (url == null || url.isEmpty()) return "";
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
    }
}

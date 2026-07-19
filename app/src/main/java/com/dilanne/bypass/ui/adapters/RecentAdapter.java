package com.dilanne.bypass.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dilanne.bypass.R;
import com.dilanne.bypass.databinding.ItemFavorisBinding;
import com.dilanne.bypass.models.PasswordEntry;

import java.util.function.BiConsumer;

public class RecentAdapter extends ListAdapter<PasswordEntry, RecentAdapter.RecentViewHolder> {

    private final BiConsumer<PasswordEntry, View> onItemClick;

    public RecentAdapter(BiConsumer<PasswordEntry, View> onItemClick) {
        super(new DiffUtil.ItemCallback<PasswordEntry>() {
            @Override
            public boolean areItemsTheSame(@NonNull PasswordEntry oldItem, @NonNull PasswordEntry newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull PasswordEntry oldItem, @NonNull PasswordEntry newItem) {
                return oldItem.getTitle().equals(newItem.getTitle()) &&
                        oldItem.getEmail().equals(newItem.getEmail()) &&
                        oldItem.getEncryptedPassword().equals(newItem.getEncryptedPassword());
            }
        });
        this.onItemClick = onItemClick;
    }

    @NonNull
    @Override
    public RecentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFavorisBinding binding = ItemFavorisBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RecentViewHolder(binding, onItemClick);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public static class RecentViewHolder extends RecyclerView.ViewHolder {
        private final ItemFavorisBinding binding;
        private final BiConsumer<PasswordEntry, View> onItemClick;

        public RecentViewHolder(ItemFavorisBinding binding, BiConsumer<PasswordEntry, View> onItemClick) {
            super(binding.getRoot());
            this.binding = binding;
            this.onItemClick = onItemClick;
        }

        public void bind(PasswordEntry entry) {
            binding.tvFavorisTitle.setText(entry.getTitle());
            binding.tvFavorisEmail.setText(entry.getEmail());

            String faviconUrl = "";
            if (entry.getUrl() != null && !entry.getUrl().isEmpty()) {
                faviconUrl = "https://icons.duckduckgo.com/ip3/" + extractDomain(entry.getUrl()) + ".ico";
            }

            Glide.with(binding.ivFavorisLogo.getContext())
                    .load(faviconUrl)
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .into(binding.ivFavorisLogo);

            binding.getRoot().setOnClickListener(v -> onItemClick.accept(entry, v));
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
    }
}

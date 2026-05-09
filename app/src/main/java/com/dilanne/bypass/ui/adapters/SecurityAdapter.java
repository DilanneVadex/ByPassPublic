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
    private final Map<Integer, PasswordSecurityInfo> securityInfoMap = new HashMap<>();
    private final OnDecryptListener decryptListener;

    public interface OnDecryptListener {
        String onDecrypt(String encryptedPassword);
    }

    public static class PasswordSecurityInfo {
        public String strength = "Analyzing...";
        public int strengthColor = Color.GRAY;
        public String status = "Checking...";
        public int statusColor = Color.GRAY;
        public boolean isCompromised = false;
    }

    public SecurityAdapter(OnDecryptListener decryptListener) {
        this.decryptListener = decryptListener;
    }

    public void setPasswords(List<PasswordEntry> passwords) {
        this.passwords = passwords;
        notifyDataSetChanged();
    }

    public void updateSecurityInfo(int position, PasswordSecurityInfo info) {
        securityInfoMap.put(position, info);
        notifyItemChanged(position);
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
        PasswordSecurityInfo info = securityInfoMap.get(position);
        holder.bind(entry, info);
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

        public void bind(PasswordEntry entry, PasswordSecurityInfo info) {
            binding.tvServiceName.setText(entry.getTitle());
            binding.tvUserEmail.setText("• " + entry.getEmail());
            
            if (info != null) {
                binding.tvStrengthBadge.setText(info.strength);
                binding.tvStrengthBadge.getBackground().setTint(info.strengthColor);
                
                binding.tvCompromisedBadge.setText(info.status);
                binding.tvCompromisedBadge.getBackground().setTint(info.statusColor);
                
                if (info.isCompromised) {
                    binding.ivStatusIcon.setImageResource(R.drawable.shield_flash_fill);
                    binding.ivStatusIcon.setColorFilter(Color.RED);
                } else if ("SECURE".equals(info.status)) {
                    binding.ivStatusIcon.setImageResource(R.drawable.shield_check_line);
                    binding.ivStatusIcon.setColorFilter(binding.getRoot().getContext().getColor(R.color.secondary));
                } else {
                    binding.ivStatusIcon.setImageResource(R.drawable.information_2_fill);
                    binding.ivStatusIcon.setColorFilter(Color.GRAY);
                }
            } else {
                binding.tvStrengthBadge.setText("PENDING");
                binding.tvStrengthBadge.getBackground().setTint(Color.GRAY);
                binding.tvCompromisedBadge.setText("PENDING");
                binding.tvCompromisedBadge.getBackground().setTint(Color.GRAY);
                binding.ivStatusIcon.setImageResource(R.drawable.information_2_fill);
                binding.ivStatusIcon.setColorFilter(Color.GRAY);
            }
        }
    }
}

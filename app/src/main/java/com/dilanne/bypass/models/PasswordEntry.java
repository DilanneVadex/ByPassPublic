package com.dilanne.bypass.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "passwords")
public class PasswordEntry implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String title;
    private String url;
    private String email;
    private String encryptedPassword;
    private String category;
    private long lastModified;
    private boolean isFavorite;
    private String securityStrength;
    private int securityStrengthColor;
    private String securityStatus;
    private int securityStatusColor;
    private boolean isCompromised;

    public PasswordEntry() {
        this.lastModified = System.currentTimeMillis();
        this.isFavorite = false;
    }

    public PasswordEntry(String title, String email, String encryptedPassword, String category) {
        this.title = title;
        this.email = email;
        this.encryptedPassword = encryptedPassword;
        this.category = category;
        this.lastModified = System.currentTimeMillis();
        this.isFavorite = false;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getEncryptedPassword() { return encryptedPassword; }
    public void setEncryptedPassword(String encryptedPassword) { this.encryptedPassword = encryptedPassword; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public long getLastModified() { return lastModified; }
    public void setLastModified(long lastModified) { this.lastModified = lastModified; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public String getSecurityStrength() { return securityStrength; }
    public void setSecurityStrength(String securityStrength) { this.securityStrength = securityStrength; }
    public int getSecurityStrengthColor() { return securityStrengthColor; }
    public void setSecurityStrengthColor(int securityStrengthColor) { this.securityStrengthColor = securityStrengthColor; }
    public String getSecurityStatus() { return securityStatus; }
    public void setSecurityStatus(String securityStatus) { this.securityStatus = securityStatus; }
    public int getSecurityStatusColor() { return securityStatusColor; }
    public void setSecurityStatusColor(int securityStatusColor) { this.securityStatusColor = securityStatusColor; }
    public boolean isCompromised() { return isCompromised; }
    public void setCompromised(boolean compromised) { isCompromised = compromised; }
}

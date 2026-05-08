package com.dilanne.bypass.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "passwords")
public class PasswordEntry {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String title;
    private String email;
    private String encryptedPassword;
    private String category;
    private long lastModified;
    private boolean isFavorite;

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
}

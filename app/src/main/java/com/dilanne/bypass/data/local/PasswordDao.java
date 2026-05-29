package com.dilanne.bypass.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.dilanne.bypass.models.PasswordEntry;

import java.util.List;

@Dao
public interface PasswordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(PasswordEntry password);

    @Update
    void update(PasswordEntry password);

    @Delete
    void delete(PasswordEntry password);

    @Query("SELECT * FROM passwords ORDER BY lastModified DESC")
    LiveData<List<PasswordEntry>> getAllPasswords();

    @Query("SELECT * FROM passwords ORDER BY lastModified DESC")
    List<PasswordEntry> getAllPasswordsSync();

    @Query("SELECT * FROM passwords WHERE category = :category ORDER BY lastModified DESC")
    LiveData<List<PasswordEntry>> getPasswordsByCategory(String category);

    @Query("DELETE FROM passwords")
    void deleteAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PasswordEntry> passwords);
}

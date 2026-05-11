package com.dilanne.bypass.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.dilanne.bypass.models.PasswordEntry;

@Database(entities = {PasswordEntry.class}, version = 4, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract PasswordDao passwordDao();

    public static AppDatabase getDatabase(final Context context, String userId) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    String dbName = (userId == null || userId.isEmpty()) ? "vaultpass_database" : "vaultpass_database_" + userId;
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, dbName)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}

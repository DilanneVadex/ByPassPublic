package com.dilanne.bypass.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.dilanne.bypass.models.PasswordEntry;

@Database(entities = {PasswordEntry.class}, version = 5, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract PasswordDao passwordDao();

    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE passwords ADD COLUMN securityStrength TEXT");
            database.execSQL("ALTER TABLE passwords ADD COLUMN securityStrengthColor INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE passwords ADD COLUMN securityStatus TEXT");
            database.execSQL("ALTER TABLE passwords ADD COLUMN securityStatusColor INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE passwords ADD COLUMN isCompromised INTEGER NOT NULL DEFAULT 0");
        }
    };

    public static AppDatabase getDatabase(final Context context, String userId) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    String dbName = (userId == null || userId.isEmpty()) ? "vaultpass_database" : "vaultpass_database_" + userId;
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, dbName)
                            .addMigrations(MIGRATION_4_5)
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

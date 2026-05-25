package com.github.tvbox.osc.data;

import android.database.sqlite.SQLiteException;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.github.tvbox.osc.base.App;
import com.github.tvbox.osc.util.FileUtils;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AppDataManager {
    private static final int DB_FILE_VERSION = 3;
    private static final String DB_NAME = "tvbox";
    private static AppDataManager instance;
    private AppDataBase dbInstance;

    @Inject
    public AppDataManager() {
        instance = this;
    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            try {
                //database.execSQL("ALTER TABLE sourceState ADD COLUMN tidSort TEXT");
                database.execSQL("CREATE TABLE IF NOT EXISTS `storageDrive` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT, `type` INTEGER NOT NULL, `configJson` TEXT)");
            } catch (SQLiteException e) {
                e.printStackTrace();
            }
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            try {
                //database.execSQL("ALTER TABLE sourceState ADD COLUMN tidSort TEXT");
                database.execSQL("CREATE TABLE IF NOT EXISTS t_search (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, searchKeyWords TEXT)");
                //添加索引
                database.execSQL("CREATE INDEX IF NOT EXISTS index_t_search_searchKeyWords ON t_search (searchKeyWords)");

                /*database.execSQL("CREATE TABLE IF NOT EXISTS t_search_temp (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, searchKeyWords TEXT)");
                // Copy the data
                database.execSQL("INSERT INTO t_search_temp (id, searchKeyWords) SELECT id, searchKeyWords FROM t_search");
                // Remove the old table
                database.execSQL("DROP TABLE t_search");
                // Change the table name to the correct one
                database.execSQL("ALTER TABLE t_search_temp RENAME TO t_search");
                //添加索引
                database.execSQL("CREATE INDEX IF NOT EXISTS index_t_search_searchKeyWords ON t_search (searchKeyWords)");*/
            } catch (SQLiteException e) {
                e.printStackTrace();
            }
        }
    };
    static String dbPath() {
        return DB_NAME + ".v" + DB_FILE_VERSION + ".db";
    }

    public static AppDataBase get() {
        if (instance == null) {
            throw new RuntimeException("AppDataManager not initialized via Hilt");
        }
        return instance.getDb();
    }

    private AppDataBase getDb() {
        if (dbInstance == null)
            dbInstance = Room.databaseBuilder(App.getInstance(), AppDataBase.class, dbPath())
                    .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
                    .addMigrations(MIGRATION_1_2)
                    .addMigrations(MIGRATION_2_3)
                    .addCallback(new RoomDatabase.Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                        }

                        @Override
                        public void onOpen(@NonNull SupportSQLiteDatabase db) {
                            super.onOpen(db);
                        }
                    }).allowMainThreadQueries()
                    .build();
        return dbInstance;
    }

    public static boolean backup(File path) throws IOException {
        AppDataManager mgr = instance;
        if (mgr != null && mgr.dbInstance != null && mgr.dbInstance.isOpen()) {
            mgr.dbInstance.close();
        }
        File db = App.getInstance().getDatabasePath(dbPath());
        if (db.exists()) {
            FileUtils.copyFile(db, path);
            return true;
        } else {
            return false;
        }
    }

    public static boolean restore(File path) throws IOException {
        AppDataManager mgr = instance;
        if (mgr != null && mgr.dbInstance != null && mgr.dbInstance.isOpen()) {
            mgr.dbInstance.close();
        }
        File db = App.getInstance().getDatabasePath(dbPath());
        if (db.exists()) {
            db.delete();
        }
        if (!db.getParentFile().exists())
            db.getParentFile().mkdirs();
        FileUtils.copyFile(path, db);
        return true;
    }
}

package com.chehanr.trakr.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
    entities = {GpsDataRecord.class},
    version = 1)
public abstract class AppDatabase extends RoomDatabase {
  public static String DATABASE_NAME = "app_db";
  private static AppDatabase appDatabase;

  public static AppDatabase getInstance(Context context) {
    if (appDatabase == null) {
      appDatabase = buildDatabaseInstance(context);
    }
    return appDatabase;
  }

  private static AppDatabase buildDatabaseInstance(Context context) {
    return Room.databaseBuilder(context, AppDatabase.class, DATABASE_NAME)
        .allowMainThreadQueries()
        .build();
  }

  public void cleanUp() {
    appDatabase = null;
  }

  public abstract GpsDataRecordDao gpsDataRecordDao();
}

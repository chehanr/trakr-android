package com.chehanr.trakr.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface GpsDataRecordDao {
  @Query("SELECT * FROM gps_data_records")
  List<GpsDataRecord> getAll();

  @Query(
      "SELECT * FROM gps_data_records WHERE logged_timestamp > :time ORDER BY datetime(logged_timestamp) DESC")
  List<GpsDataRecord> getAllDataAfter(long time);

  @Query("SELECT COUNT(*) FROM gps_data_records")
  LiveData<Integer> getDataCount();

  @Query("DELETE FROM gps_data_records")
  void deleteAll();

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  void insertAll(GpsDataRecord... records);

  @Delete
  void delete(GpsDataRecord record);
}

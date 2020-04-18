package com.chehanr.trakr.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import java.sql.Date;
import java.util.Locale;

@Entity(tableName = "gps_data_records")
public class GpsDataRecord {
  @PrimaryKey(autoGenerate = true)
  public long id;

  @ColumnInfo(name = "logged_timestamp")
  @TypeConverters(Converters.class)
  public Date loggedTimestamp;

  @ColumnInfo(name = "speed")
  public long speed;

  @ColumnInfo(name = "satellite_count")
  public int satelliteCount;

  @ColumnInfo(name = "altitude")
  public long altitude;

  @ColumnInfo(name = "latitude")
  public Float latitude;

  @ColumnInfo(name = "longitude")
  public Float longitude;

  public String toString() {
    return String.format(
        Locale.ENGLISH,
        "GpsDataRecord@%d {loggedTimestamp:%d, speed:%d, satelliteCount:%d, altitude:%d, latitude:%f, longitude:%f}",
        id,
        Converters.toTimestamp(loggedTimestamp),
        speed,
        satelliteCount,
        altitude,
        latitude,
        longitude);
  }
}

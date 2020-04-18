package com.chehanr.trakr.db;

import androidx.room.TypeConverter;
import java.sql.Date;

public class Converters {
  @TypeConverter
  public static Date toDate(Long timestamp) {
    if (timestamp == null) {
      return null;
    }
    return new Date(timestamp);
  }

  @TypeConverter
  public static Long toTimestamp(Date date) {
    if (date == null) {
      return null;
    }

    return date.getTime();
  }
}

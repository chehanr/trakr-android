package com.chehanr.trakr.utils;

import com.chehanr.trakr.db.Converters;
import com.chehanr.trakr.db.GpsDataRecord;
import com.chehanr.trakr.firebase.models.GpsData;

public class FirebaseUtils {
  private static final String TAG = FirebaseUtils.class.getSimpleName();

  public static GpsData gpsDataRecordToGpsDataModel(GpsDataRecord record) {
    return new GpsData(
        record.id,
        record.speed,
        record.satelliteCount,
        record.altitude,
        record.latitude,
        record.longitude,
        Converters.toTimestamp(record.loggedTimestamp));
  }
}

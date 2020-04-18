package com.chehanr.trakr.utils;

import com.chehanr.trakr.db.GpsDataRecord;
import com.chehanr.trakr.node.NodeGpsData;

public class DbUtils {
  public static java.sql.Date convertJavaDateToSqlDate(java.util.Date date) {
    return new java.sql.Date(date.getTime());
  }

  public static GpsDataRecord nodeDataToDbEntity(NodeGpsData gpsData) {
    GpsDataRecord record = new GpsDataRecord();

    record.loggedTimestamp =
        convertJavaDateToSqlDate(NodeUtils.getDateFromStamps(gpsData.getDate(), gpsData.getTime()));
    record.speed = gpsData.getSpeed();
    record.satelliteCount = gpsData.getSatelliteCount();
    record.altitude = gpsData.getAltitude();
    record.latitude = gpsData.getLatitude();
    record.longitude = gpsData.getLongitude();

    return record;
  }
}

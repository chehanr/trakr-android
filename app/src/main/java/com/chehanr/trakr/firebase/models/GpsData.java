package com.chehanr.trakr.firebase.models;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

@IgnoreExtraProperties
public class GpsData {

  public long logId;
  public long speed;
  public int satelliteCount;
  public long altitude;
  public Float latitude;
  public Float longitude;
  public Object loggedTimeStamp;
  public Object createdTimeStamp;

  public GpsData() {}

  public GpsData(
      long logId,
      long speed,
      int satelliteCount,
      long altitude,
      Float latitude,
      Float longitude,
      Object loggedTimeStamp) {
    this.logId = logId;
    this.speed = speed;
    this.satelliteCount = satelliteCount;
    this.altitude = altitude;
    this.latitude = latitude;
    this.longitude = longitude;
    this.loggedTimeStamp = loggedTimeStamp;
    this.createdTimeStamp = ServerValue.TIMESTAMP;
  }
}

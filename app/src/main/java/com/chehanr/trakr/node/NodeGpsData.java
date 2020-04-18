package com.chehanr.trakr.node;

public class NodeGpsData {
  private int delimiter;
  private int date;
  private long time;
  private long speed;
  private int satelliteCount;
  private long altitude;
  private float latitude;
  private float longitude;

  public int getDelimiter() {
    return delimiter;
  }

  public void setDelimiter(int delimiter) {
    this.delimiter = delimiter;
  }

  public int getDate() {
    return date;
  }

  public void setDate(int date) {
    this.date = date;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public long getSpeed() {
    return speed;
  }

  public void setSpeed(long speed) {
    this.speed = speed;
  }

  public int getSatelliteCount() {
    return satelliteCount;
  }

  public void setSatelliteCount(int satelliteCount) {
    this.satelliteCount = satelliteCount;
  }

  public long getAltitude() {
    return altitude;
  }

  public void setAltitude(long altitude) {
    this.altitude = altitude;
  }

  public float getLatitude() {
    return latitude;
  }

  public void setLatitude(float latitude) {
    this.latitude = latitude;
  }

  public float getLongitude() {
    return longitude;
  }

  public void setLongitude(float longitude) {
    this.longitude = longitude;
  }
}

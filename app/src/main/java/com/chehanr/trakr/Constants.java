package com.chehanr.trakr;

import java.util.UUID;

public class Constants {
  public static final UUID UUID_BLE_NODE_SERVICE =
      UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914a");
  public static final UUID UUID_BLE_GPS_DATA_CHARACTERISTIC =
      UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b");
  public static final float LOCATION_DIFFERENCE_MIN_METERS = 3f; // Sensitivity.
  public static final int MAX_GPS_DATA_REPO_SIZE = 20; // Before saving to database.
  public static final int SYNC_JOB_SCHEDULE_INTERVAL = 60000 * 5; // 5 minutes.
  public static final String FIREBASE_RDB_GPS_DATA_REF = "gpsData";
  public static final String FIREBASE_RDB_FUEL_DATA_REF = "fuelData";
}

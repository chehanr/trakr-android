package com.chehanr.trakr.firebase.models;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

@IgnoreExtraProperties
public class FuelData {
  public Float volume;
  public Float price;
  public Object createdTimeStamp;

  public FuelData() {}

  public FuelData(Float volume, Float price) {
    this.volume = volume;
    this.price = price;
    this.createdTimeStamp = ServerValue.TIMESTAMP;
  }
}

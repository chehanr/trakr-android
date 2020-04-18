package com.chehanr.trakr.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NodeUtils {
  public static long getHourFromStamp(long stamp) {
    return stamp / 1000000;
  }

  public static long getMinuteFromStamp(long stamp) {
    return (stamp / 10000) % 100;
  }

  public static long getSecondFromStamp(long stamp) {
    return (stamp / 100) % 100;
  }

  public static long getCentiSecondFromStamp(long stamp) {
    return stamp % 100;
  }

  public static Date getDateFromStamps(long dateStamp, long timeStamp) {
    long hour, minute, second, centiSecond;
    String dtString;
    Date date = new Date();

    hour = getHourFromStamp(timeStamp);
    minute = getMinuteFromStamp(timeStamp);
    second = getSecondFromStamp(timeStamp);
    centiSecond = getCentiSecondFromStamp(timeStamp);

    dtString = dateStamp + " " + hour + ":" + minute + ":" + second + "." + centiSecond;

    try {
      date = new SimpleDateFormat("ddMMyy HH:mm:ss.S", Locale.ENGLISH).parse(dtString);
    } catch (ParseException e) {
      e.printStackTrace();
    }

    return date;
  }
}

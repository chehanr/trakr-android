package com.chehanr.trakr.node;

import static com.chehanr.trakr.Constants.LOCATION_DIFFERENCE_MIN_METERS;

import android.util.Log;
import com.chehanr.trakr.utils.GeoUtils;
import java.util.ArrayList;
import java.util.List;

public class NodeGpsDataRepository {
  private static final String TAG = NodeGpsDataRepository.class.getSimpleName();

  private List<NodeGpsData> nodeGpsDataList;
  private NodeGpsData nodeGpsData = new NodeGpsData();
  private NodeGpsData nodeGpsDataPrevious = new NodeGpsData();
  private int delimiterS = 0, delimiterE = 0;

  public NodeGpsDataRepository() {
    nodeGpsDataList = new ArrayList<>();
  }

  private String getValue(String str) {
    return str.split("(?<= )")[1];
  }

  private void reset() {
    delimiterS = 0;
    delimiterE = 0;
    nodeGpsData = new NodeGpsData();
  }

  private void parse(String rawString) {
    try {
      if (rawString.startsWith("dte")) {
        nodeGpsData.setDate(Integer.parseInt(getValue(rawString)));
      }
      if (rawString.startsWith("tim")) {
        nodeGpsData.setTime(Long.parseLong(getValue(rawString)));
      }
      if (rawString.startsWith("spd")) {
        nodeGpsData.setSpeed(Long.parseLong(getValue(rawString)));
      }
      if (rawString.startsWith("sat")) {
        nodeGpsData.setSatelliteCount(Integer.parseInt(getValue(rawString)));
      }
      if (rawString.startsWith("alt")) {
        nodeGpsData.setAltitude(Long.parseLong(getValue(rawString)));
      }
      if (rawString.startsWith("lat")) {
        nodeGpsData.setLatitude(Float.parseFloat(getValue(rawString)));
      }
      if (rawString.startsWith("lng")) {
        nodeGpsData.setLongitude(Float.parseFloat(getValue(rawString)));
      }
      if (delimiterS == 0) {
        delimiterS = Integer.parseInt(rawString);
      } else {
        delimiterE = Integer.parseInt(rawString);
      }
    } catch (NumberFormatException e) {
    }
  }

  public int getRepoSize() {
    return nodeGpsDataList.size();
  }

  public List<NodeGpsData> getRepo() {
    return nodeGpsDataList;
  }

  public void clearRepo() {
    nodeGpsDataList.clear();
  }

  public void push(String rawString) {
    parse(rawString);

    if (delimiterS == delimiterE) {
      nodeGpsData.setDelimiter(delimiterE);

      boolean isValid =
          !(nodeGpsData.getTime() == 0
              || nodeGpsData.getDate() == 0
              || nodeGpsData.getSpeed() == 0
              || nodeGpsData.getAltitude() == 0
              || nodeGpsData.getLatitude() == 0f
              || nodeGpsData.getLongitude() == 0f);

      if (isValid) {
        // Valid GPS data.

        double distance =
            GeoUtils.distance(
                nodeGpsData.getLatitude(),
                nodeGpsDataPrevious.getLatitude(),
                nodeGpsData.getLongitude(),
                nodeGpsDataPrevious.getLongitude(),
                0f,
                0f);

        if (distance > LOCATION_DIFFERENCE_MIN_METERS) {
          // Detected movement? Add to repo.
          nodeGpsDataList.add(nodeGpsData);
          Log.d(TAG, "push: Added " + nodeGpsData.toString() + " to repo");
        }

        nodeGpsDataPrevious = nodeGpsData;
      }

      reset();
    }
  }
}

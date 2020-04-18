package com.chehanr.trakr.services;

import static com.chehanr.trakr.Constants.MAX_GPS_DATA_REPO_SIZE;
import static com.chehanr.trakr.Constants.UUID_BLE_GPS_DATA_CHARACTERISTIC;
import static com.chehanr.trakr.Constants.UUID_BLE_NODE_SERVICE;
import static com.chehanr.trakr.services.BleService.BleServiceIntentAction.ACTION_DATA_AVAILABLE;
import static com.chehanr.trakr.services.BleService.BleServiceIntentAction.ACTION_GATT_CONNECTED;
import static com.chehanr.trakr.services.BleService.BleServiceIntentAction.ACTION_GATT_DISCONNECTED;
import static com.chehanr.trakr.services.BleService.BleServiceIntentAction.ACTION_GATT_SERVICES_DISCOVERED;
import static com.chehanr.trakr.services.BleService.BleServiceIntentAction.EXTRA_DATA;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import com.chehanr.trakr.MainActivity;
import com.chehanr.trakr.R;
import com.chehanr.trakr.db.AppDatabase;
import com.chehanr.trakr.db.GpsDataRecord;
import com.chehanr.trakr.node.NodeGpsData;
import com.chehanr.trakr.node.NodeGpsDataRepository;
import com.chehanr.trakr.utils.DbUtils;
import java.util.ArrayList;
import java.util.List;

public class BleService extends Service {
  public static final String NOTIFICATION_CHANNEL_BLE_ID = "ble_notification_channel";
  public static final int NOTIFICATION_BLE_CONNECTED_ID = 1;
  public static final String BLE_DEVICE_ADDRESS = "ble_device_address";
  private static final String TAG = BleService.class.getSimpleName();
  private String currentBleDeviceAddress = "";
  private boolean isConnected = false;
  private NodeGpsDataRepository nodeGpsDataRepository;

  private BluetoothManager bluetoothManager;
  private BluetoothAdapter bluetoothAdapter;
  private BluetoothGatt bluetoothGatt;
  private BluetoothGattCallback bluetoothGattCallback =
      new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
          super.onConnectionStateChange(gatt, status, newState);

          switch (newState) {
            case BluetoothProfile.STATE_CONNECTED:
              Log.i(TAG, "onConnectionStateChange: Connected to GATT server");
              broadcastAction(ACTION_GATT_CONNECTED, null);
              Log.i(
                  TAG,
                  "onConnectionStateChange: Starting service discovery: "
                      + bluetoothGatt.discoverServices());

              break;
            case BluetoothProfile.STATE_DISCONNECTED:
              Log.i(TAG, "onConnectionStateChange: Disconnected from GATT server");
              broadcastAction(ACTION_GATT_DISCONNECTED, null);
              break;
          }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
          super.onServicesDiscovered(gatt, status);

          if (status == BluetoothGatt.GATT_SUCCESS) {
            BluetoothGattService bleNodeService = gatt.getService(UUID_BLE_NODE_SERVICE);

            if (bleNodeService != null) {
              Log.i(TAG, "onServicesDiscovered: Node service found " + bleNodeService.getUuid());
              BluetoothGattCharacteristic gpsDataChara =
                  bleNodeService.getCharacteristic(UUID_BLE_GPS_DATA_CHARACTERISTIC);

              if (gpsDataChara != null) {
                Log.i(
                    TAG,
                    "onServicesDiscovered: GPS Data characteristic read " + gpsDataChara.getUuid());
                gatt.setCharacteristicNotification(gpsDataChara, true);

                for (BluetoothGattDescriptor descriptor : gpsDataChara.getDescriptors()) {
                  descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                  gatt.writeDescriptor(descriptor);
                }
              }
            } else {
              Log.i(TAG, "onServicesDiscovered: Node service NOT found! ");
            }
            broadcastAction(ACTION_GATT_SERVICES_DISCOVERED, null);
          } else {
            Log.w(TAG, "onServicesDiscovered: " + status);
          }
        }

        @Override
        public void onCharacteristicRead(
            BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
          super.onCharacteristicRead(gatt, characteristic, status);

          if (status == BluetoothGatt.GATT_SUCCESS && characteristic != null) {
            broadcastAction(ACTION_DATA_AVAILABLE, characteristic);
          }
        }

        @Override
        public void onCharacteristicWrite(
            BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
          super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(
            BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
          super.onCharacteristicChanged(gatt, characteristic);

          if (UUID_BLE_GPS_DATA_CHARACTERISTIC.equals(characteristic.getUuid())) {
            final String dataString = characteristic.getStringValue(0);
            handleGpsData(dataString);
          }
          broadcastAction(ACTION_DATA_AVAILABLE, characteristic);
        }
      };
  private NotificationManager notificationManager;

  public BleService() {}

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    createNotificationChannel();

    bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
    bluetoothAdapter = bluetoothManager.getAdapter();
    nodeGpsDataRepository = new NodeGpsDataRepository();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    String bleDeviceAddress = intent.getStringExtra(BLE_DEVICE_ADDRESS);

    if (!isConnected) {
      isConnected = connect(bleDeviceAddress);
      startForeground(NOTIFICATION_BLE_CONNECTED_ID, getNotification());
    }

    return START_NOT_STICKY;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    disconnect();
    close();
    isConnected = false;
    notificationManager.cancel(NOTIFICATION_BLE_CONNECTED_ID);
  }

  private boolean connect(String address) {
    // try to reconnect.
    if (address.equals(currentBleDeviceAddress) && bluetoothGatt != null) {
      if (bluetoothGatt.connect()) {
        Log.i(TAG, "connect: Reconnected to " + bluetoothGatt.getDevice());

        return true;
      }

      return false;
    }

    final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

    bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback);
    currentBleDeviceAddress = address;
    Log.i(TAG, "connect: Create new connection for " + device);

    return true;
  }

  private void disconnect() {
    if (bluetoothGatt == null || bluetoothAdapter == null) {
      Log.w(TAG, "disconnect: bluetoothGatt not initialized");

      return;
    }

    bluetoothGatt.disconnect();
  }

  private void close() {
    if (bluetoothGatt == null) {
      return;
    }

    bluetoothGatt.close();
    bluetoothGatt = null;
  }

  private void handleGpsData(String dataString) {
    nodeGpsDataRepository.push(dataString);

    if (nodeGpsDataRepository.getRepoSize() >= MAX_GPS_DATA_REPO_SIZE) {
      Log.i(TAG, "handleGpsData: Pushing data to db...");

      pushGpsDataToDb(nodeGpsDataRepository.getRepo());
      nodeGpsDataRepository.clearRepo();
    }
  }

  private void pushGpsDataToDb(List<NodeGpsData> gpsData) {
    final List<GpsDataRecord> gpsDataRecords = new ArrayList<>();

    for (NodeGpsData data : gpsData) {
      gpsDataRecords.add(DbUtils.nodeDataToDbEntity(data));
    }

    final AppDatabase appDatabase = AppDatabase.getInstance(getApplicationContext());

    appDatabase.runInTransaction(
        new Runnable() {
          @Override
          public void run() {
            GpsDataRecord[] gpsDataRecordsArray = new GpsDataRecord[gpsDataRecords.size()];
            gpsDataRecordsArray = gpsDataRecords.toArray(gpsDataRecordsArray);
            appDatabase.gpsDataRecordDao().insertAll(gpsDataRecordsArray);

            appDatabase.cleanUp();
          }
        });
  }

  private void broadcastAction(
      BleServiceIntentAction action, BluetoothGattCharacteristic characteristic) {
    Intent intent = new Intent(action.name());

    if (characteristic != null
        && UUID_BLE_GPS_DATA_CHARACTERISTIC.equals(characteristic.getUuid())) {
      final String dataString = characteristic.getStringValue(0);

      Bundle bundle = new Bundle();
      bundle.putString(EXTRA_DATA.name(), dataString);
      intent.putExtras(bundle);
    }

    sendBroadcast(intent);
  }

  private void createNotificationChannel() {
    notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel serviceChannel =
          new NotificationChannel(
              NOTIFICATION_CHANNEL_BLE_ID,
              getString(R.string.ble_notification_channel_name),
              NotificationManager.IMPORTANCE_DEFAULT);

      notificationManager.createNotificationChannel(serviceChannel);
    }
  }

  private Notification getNotification() {
    Intent intent = new Intent(this, MainActivity.class);
    intent.setAction(Intent.ACTION_MAIN);
    intent.addCategory(Intent.CATEGORY_LAUNCHER);

    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
    stackBuilder.addNextIntent(intent);

    PendingIntent openPendingIntent =
        stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

    NotificationCompat.Builder builder =
        new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_BLE_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.ble_notification_text_connected))
            .setOngoing(true)
            .setPriority(Notification.PRIORITY_HIGH)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(openPendingIntent);

    return builder.build();
  }

  public enum BleServiceIntentAction {
    ACTION_GATT_CONNECTED,
    ACTION_GATT_DISCONNECTED,
    ACTION_GATT_SERVICES_DISCOVERED,
    ACTION_DATA_AVAILABLE,
    EXTRA_DATA
  }
}

package com.chehanr.trakr;

import static com.chehanr.trakr.Constants.SYNC_JOB_SCHEDULE_INTERVAL;
import static com.chehanr.trakr.services.BleService.BLE_DEVICE_ADDRESS;
import static com.chehanr.trakr.services.BleService.BleServiceIntentAction;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.chehanr.trakr.services.BleService;
import com.chehanr.trakr.services.SyncJobService;
import com.chehanr.trakr.viewmodels.MainViewModel;

public class MainActivity extends AppCompatActivity {
  private static final String TAG = MainActivity.class.getSimpleName();
  private static final String PREF_SAVED_BLE_DEVICE_ADDRESS = "PREF_SAVED_BLE_DEVICE_ADDRESS";

  private MainViewModel mainViewModel;
  private AppBarConfiguration appBarConfiguration;
  private NavController navController;
  private SharedPreferences sharedPreferences;

  private BroadcastReceiver bleServiceBroadcastReceiver =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          final String action = intent.getAction();

          if (action.equals(BleServiceIntentAction.ACTION_GATT_CONNECTED.name())) {}
          if (action.equals(BleServiceIntentAction.ACTION_GATT_DISCONNECTED.name())) {}
          if (action.equals(BleServiceIntentAction.ACTION_GATT_SERVICES_DISCOVERED.name())) {}
          if (action.equals(BleServiceIntentAction.ACTION_DATA_AVAILABLE.name())) {}
        }
      };

  private static IntentFilter bleServiceIntentFilter() {
    final IntentFilter intentFilter = new IntentFilter();

    intentFilter.addAction(BleServiceIntentAction.ACTION_GATT_CONNECTED.name());
    intentFilter.addAction(BleServiceIntentAction.ACTION_GATT_DISCONNECTED.name());
    intentFilter.addAction(BleServiceIntentAction.ACTION_GATT_SERVICES_DISCOVERED.name());
    intentFilter.addAction(BleServiceIntentAction.ACTION_DATA_AVAILABLE.name());

    return intentFilter;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    navController = Navigation.findNavController(this, R.id.nav_host_fragment);
    appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();

    NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

    mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
    mainViewModel.connectedBleDeviceAddress.observe(
        this,
        new Observer<String>() {
          @Override
          public void onChanged(String connectedBleDeviceAddress) {
            if (connectedBleDeviceAddress != null) {
              startBleService(connectedBleDeviceAddress);
              saveBleDeviceAddress(connectedBleDeviceAddress);
            } else {
              stopBleService();
              saveBleDeviceAddress(null);
            }
          }
        });

    sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);

    String savedBleDeviceAddress = sharedPreferences.getString(PREF_SAVED_BLE_DEVICE_ADDRESS, null);

    if (savedBleDeviceAddress != null) {
      mainViewModel.connectedBleDeviceAddress.setValue(savedBleDeviceAddress);
    }

    ComponentName componentName = new ComponentName(this, SyncJobService.class);

    JobInfo syncJobInfo =
        new JobInfo.Builder(SyncJobService.JOB_ID, componentName)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPeriodic(SYNC_JOB_SCHEDULE_INTERVAL)
            .build();

    JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);

    int resultCode = jobScheduler.schedule(syncJobInfo);

    if (resultCode == JobScheduler.RESULT_SUCCESS) {
      Log.i(TAG, "onCreate: SyncJobService scheduled");
    } else {
      Log.e(TAG, "onCreate: SyncJobService NOT scheduled");
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    registerReceiver(bleServiceBroadcastReceiver, bleServiceIntentFilter());
  }

  @Override
  protected void onPause() {
    super.onPause();
    unregisterReceiver(bleServiceBroadcastReceiver);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  @Override
  public boolean onSupportNavigateUp() {
    return NavigationUI.navigateUp(navController, appBarConfiguration)
        || super.onSupportNavigateUp();
  }

  private void startBleService(String bleDeviceAddress) {
    Intent bleServiceIntent = new Intent(this, BleService.class);
    bleServiceIntent.putExtra(BLE_DEVICE_ADDRESS, bleDeviceAddress);
    ContextCompat.startForegroundService(this, bleServiceIntent);
  }

  private void stopBleService() {
    Intent bleServiceIntent = new Intent(this, BleService.class);
    stopService(bleServiceIntent);
  }

  private void saveBleDeviceAddress(String address) {
    sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString(PREF_SAVED_BLE_DEVICE_ADDRESS, address);
    editor.apply();
  }
}

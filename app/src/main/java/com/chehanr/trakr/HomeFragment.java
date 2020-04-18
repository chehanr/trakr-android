package com.chehanr.trakr;

import static com.chehanr.trakr.Constants.FIREBASE_RDB_GPS_DATA_REF;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.chehanr.trakr.db.AppDatabase;
import com.chehanr.trakr.helpers.FirebaseHelpers;
import com.chehanr.trakr.viewmodels.MainViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.Locale;

public class HomeFragment extends Fragment implements View.OnClickListener {
  private static final String TAG = HomeFragment.class.getSimpleName();
  private NavController navController;
  private MainViewModel mainViewModel;
  private AppDatabase appDatabase;

  private Button connectBtn, syncBtn, clearGpsDataBtn, addFuelLogBtn, fcmSubBtn, fcmUnSubBtn;
  private TextView repoInfoTv, firebaseInfoTv;

  public HomeFragment() {}

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_home, container, false);

    connectBtn = view.findViewById(R.id.connect_b);
    syncBtn = view.findViewById(R.id.sync_b);
    clearGpsDataBtn = view.findViewById(R.id.clear_gps_data_b);
    addFuelLogBtn = view.findViewById(R.id.add_fuel_log_b);
    fcmSubBtn = view.findViewById(R.id.fcm_sub_b);
    fcmUnSubBtn = view.findViewById(R.id.fcm_unsub_b);

    repoInfoTv = view.findViewById(R.id.repo_info_tv);
    firebaseInfoTv = view.findViewById(R.id.firebase_info_tv);

    connectBtn.setOnClickListener(this);
    syncBtn.setOnClickListener(this);
    clearGpsDataBtn.setOnClickListener(this);
    addFuelLogBtn.setOnClickListener(this);
    fcmSubBtn.setOnClickListener(this);
    fcmUnSubBtn.setOnClickListener(this);

    updateConnectBtnText();

    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    navController = Navigation.findNavController(view);

    mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
    mainViewModel.connectedBleDeviceAddress.observe(
        getViewLifecycleOwner(),
        new Observer<String>() {
          @Override
          public void onChanged(String connectedBleDeviceAddress) {
            updateConnectBtnText();
          }
        });

    appDatabase = AppDatabase.getInstance(getContext());

    appDatabase
        .gpsDataRecordDao()
        .getDataCount()
        .observe(
            getActivity(),
            new Observer<Integer>() {
              @Override
              public void onChanged(final Integer count) {

                repoInfoTv.setText(
                    String.format(Locale.ENGLISH, "Local cache has %d GPS logs", count));
              }
            });

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    final DatabaseReference gpsDataDbRef = firebaseDatabase.getReference(FIREBASE_RDB_GPS_DATA_REF);
    Query lastQuery = gpsDataDbRef.orderByChild("createdTimeStamp").limitToLast(1);
    lastQuery.addValueEventListener(
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            long time = 0;

            for (DataSnapshot data : dataSnapshot.getChildren()) {
              time = (long) data.child("createdTimeStamp").getValue();
            }

            final String relTime =
                (String)
                    DateUtils.getRelativeTimeSpanString(
                        time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);

            firebaseInfoTv.setText(String.format(Locale.ENGLISH, "Last synced %s", relTime));
          }

          @Override
          public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.connect_b:
        handleConnectBtnClick();
        break;
      case R.id.sync_b:
        handleSyncBtnClick();
        break;
      case R.id.clear_gps_data_b:
        handleClearGpsDataClick();
        break;
      case R.id.add_fuel_log_b:
        handleAddFuelLogClick();
        break;
      case R.id.fcm_sub_b:
        handleFcmSubscribe(true);
        break;
      case R.id.fcm_unsub_b:
        handleFcmSubscribe(false);
        break;
    }
  }

  private void updateConnectBtnText() {
    if (connectBtn == null || mainViewModel == null) return;

    String address = mainViewModel.connectedBleDeviceAddress.getValue();

    if (address == null) {
      connectBtn.setText(R.string.connect_btn_text_connect);
      return;
    }

    connectBtn.setText(R.string.connect_btn_text_disconnect);
  }

  private void handleConnectBtnClick() {
    Log.d(TAG, "handleSyncBtnClick: Manually connecting/ disconnecting node...");

    if (connectBtn == null || mainViewModel == null) return;

    String address = mainViewModel.connectedBleDeviceAddress.getValue();

    if (address == null) {
      navController.navigate(R.id.action_homeFragment_to_connectFragment);
      return;
    }

    mainViewModel.connectedBleDeviceAddress.setValue(null);
  }

  private void handleSyncBtnClick() {
    Log.d(TAG, "handleSyncBtnClick: Manually performing sync...");
    Toast.makeText(getActivity(), "Syncing...", Toast.LENGTH_SHORT).show();

    FirebaseHelpers.performSync(getContext());
  }

  private void handleClearGpsDataClick() {
    Log.d(TAG, "handleClearGpsDataClick: Manually clearing GPS records table...");
    Toast.makeText(getActivity(), "Clearing GPS data...", Toast.LENGTH_SHORT).show();

    appDatabase.runInTransaction(
        new Runnable() {
          @Override
          public void run() {
            appDatabase.gpsDataRecordDao().deleteAll();

            appDatabase.cleanUp();
          }
        });
  }

  private void handleAddFuelLogClick() {
    navController.navigate(R.id.action_homeFragment_to_fuelLogFragment);
  }

  private void handleFcmSubscribe(boolean subscribe) {
    Log.d(TAG, "handleClearGpsDataClick: Manually setting subscription " + subscribe + "...");

    if (subscribe) {
      Toast.makeText(getActivity(), "Subscribing...", Toast.LENGTH_SHORT).show();

      FirebaseMessaging.getInstance()
          .subscribeToTopic("daily-report")
          .addOnCompleteListener(
              new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                  String msg = "Subscribed to daily reports!";
                  if (!task.isSuccessful()) {
                    msg = "Error subscribing!";
                    Log.e(TAG, "onComplete: Error subscribing", task.getException());
                  }
                  Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                }
              });

      return;
    }

    Toast.makeText(getActivity(), "Unsubscribing...", Toast.LENGTH_SHORT).show();
    FirebaseMessaging.getInstance()
        .unsubscribeFromTopic("daily-report")
        .addOnCompleteListener(
            new OnCompleteListener<Void>() {
              @Override
              public void onComplete(@NonNull Task<Void> task) {
                String msg = "Unsubscribed from daily reports!";
                if (!task.isSuccessful()) {
                  msg = "Error unsubscribing!";
                  Log.e(TAG, "onComplete: Error unsubscribing", task.getException());
                }
                Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
              }
            });
  }
}

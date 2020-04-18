package com.chehanr.trakr;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.chehanr.trakr.adapters.ScanListRvAdapter;
import com.chehanr.trakr.viewmodels.MainViewModel;

public class ConnectFragment extends Fragment {
  private static final String TAG = ConnectFragment.class.getSimpleName();

  private static final int REQUEST_ENABLE_BT = 0;

  private MainViewModel mainViewModel;
  private ScanListRvAdapter scanListRvAdapter;

  private BluetoothAdapter bluetoothAdapter;
  private BluetoothManager bluetoothManager;
  private ScanCallback scanCallback =
      new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
          super.onScanResult(callbackType, result);

          scanListRvAdapter.addDevice(result.getDevice());
          scanListRvAdapter.notifyDataSetChanged();
        }
      };
  private View.OnClickListener scanListRvOnClickListener =
      new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) v.getTag();
          int position = viewHolder.getAdapterPosition();

          BluetoothDevice bluetoothDevice = scanListRvAdapter.getDevice(position);

          Log.d(TAG, "onClick: " + bluetoothDevice.toString());

          bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
          mainViewModel.connectedBleDeviceAddress.setValue(bluetoothDevice.getAddress());
        }
      };

  public ConnectFragment() {}

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
    bluetoothAdapter = bluetoothManager.getAdapter();
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_connect, container, false);

    RecyclerView recyclerView = view.findViewById(R.id.scan_list_rv);

    recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
    scanListRvAdapter = new ScanListRvAdapter();
    scanListRvAdapter.setOnItemClickListener(scanListRvOnClickListener);
    recyclerView.setAdapter(scanListRvAdapter);

    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    final NavController navController = Navigation.findNavController(view);

    mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
    mainViewModel.connectedBleDeviceAddress.observe(
        getViewLifecycleOwner(),
        new Observer<String>() {
          @Override
          public void onChanged(String connectedBleDeviceAddress) {
            if (connectedBleDeviceAddress != null) {
              // Remove self fragment.
              navController.popBackStack();
            }
          }
        });
  }

  @Override
  public void onResume() {
    super.onResume();

    if (!bluetoothAdapter.isEnabled()) {
      if (!bluetoothAdapter.isEnabled()) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, REQUEST_ENABLE_BT);
      }
    }

    scanForDevices();
  }

  private void scanForDevices() {
    if (bluetoothAdapter.getBluetoothLeScanner() != null)
      bluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback);
  }
}

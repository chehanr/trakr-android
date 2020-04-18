package com.chehanr.trakr.adapters;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.chehanr.trakr.R;
import java.util.ArrayList;
import java.util.List;

public class ScanListRvAdapter extends RecyclerView.Adapter<ScanListRvAdapter.ViewHolder> {
  private static final String TAG = ScanListRvAdapter.class.getSimpleName();

  private List<BluetoothDevice> deviceList;
  private View.OnClickListener onClickListener;

  public ScanListRvAdapter() {
    this.deviceList = new ArrayList<>();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View itemView =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.scan_list_rv_item, parent, false);

    return new ViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    holder.deviceNameTv.setText(deviceList.get(position).getName());
  }

  @Override
  public int getItemCount() {
    return deviceList.size();
  }

  public void addDevice(BluetoothDevice device) {
    if (!deviceList.contains(device)) deviceList.add(device);
  }

  public BluetoothDevice getDevice(int position) {
    return deviceList.get(position);
  }

  public void setOnItemClickListener(View.OnClickListener itemClickListener) {
    onClickListener = itemClickListener;
  }

  class ViewHolder extends RecyclerView.ViewHolder {
    TextView deviceNameTv;

    ViewHolder(@NonNull View itemView) {
      super(itemView);
      deviceNameTv = itemView.findViewById(R.id.device_name_tv);

      itemView.setTag(this);
      itemView.setOnClickListener(onClickListener);
    }
  }
}

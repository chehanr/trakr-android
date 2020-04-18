package com.chehanr.trakr.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {
  private static final String TAG = MainViewModel.class.getSimpleName();
  public final MutableLiveData<String> connectedBleDeviceAddress = new MutableLiveData<>();

  public MainViewModel() {
    connectedBleDeviceAddress.setValue(null);
  }
}

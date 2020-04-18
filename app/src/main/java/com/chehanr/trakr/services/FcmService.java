package com.chehanr.trakr.services;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FcmService extends FirebaseMessagingService {
  private static final String TAG = FcmService.class.getSimpleName();

  @Override
  public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
    super.onMessageReceived(remoteMessage);
    Log.i(TAG, "onMessageReceived: Received message from " + remoteMessage.getFrom());
  }
}

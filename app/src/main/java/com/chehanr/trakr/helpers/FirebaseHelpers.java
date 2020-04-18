package com.chehanr.trakr.helpers;

import static com.chehanr.trakr.Constants.FIREBASE_RDB_GPS_DATA_REF;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.chehanr.trakr.db.AppDatabase;
import com.chehanr.trakr.db.GpsDataRecord;
import com.chehanr.trakr.utils.FirebaseUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.List;

public class FirebaseHelpers {
  private static final String TAG = FirebaseHelpers.class.getSimpleName();

  public static void performSync(Context context) {
    final AppDatabase appDatabase = AppDatabase.getInstance(context);
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    final DatabaseReference gpsDataDbRef = firebaseDatabase.getReference(FIREBASE_RDB_GPS_DATA_REF);

    Query lastQuery = gpsDataDbRef.orderByChild("loggedTimeStamp").limitToLast(1);
    lastQuery.addListenerForSingleValueEvent(
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            long time = 0;

            for (DataSnapshot data : dataSnapshot.getChildren()) {
              time = (long) data.child("loggedTimeStamp").getValue();
            }

            List<GpsDataRecord> records = appDatabase.gpsDataRecordDao().getAllDataAfter(time);
            Log.i(
                TAG, "onDataChange: Attempting to push " + records.size() + " records to Firebase");

            String newId;

            for (final GpsDataRecord record : records) {
              newId = gpsDataDbRef.push().getKey();

              gpsDataDbRef
                  .child(newId)
                  .setValue(FirebaseUtils.gpsDataRecordToGpsDataModel(record))
                  .addOnSuccessListener(
                      new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                          Log.d(TAG, "onStartJob: record " + record.toString() + " pushed");
                        }
                      })
                  .addOnFailureListener(
                      new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                          Log.w(TAG, "onStartJob: failed to push", e);
                        }
                      });
            }
          }

          @Override
          public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.w(TAG, "onStartJob: lastQuery cancelled", databaseError.toException());
          }
        });
  }
}

package com.chehanr.trakr;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.chehanr.trakr.firebase.models.FuelData;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FuelLogFragment extends Fragment implements View.OnClickListener {
  private static final String TAG = FuelLogFragment.class.getSimpleName();
  private NavController navController;

  private TextInputLayout fuelVolumeTil, fuelPriceTil;
  private TextInputEditText fuelVolumeTiet, fuelPriceTiet;
  private Button logBtn;

  public FuelLogFragment() {}

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_fuel_log, container, false);

    fuelVolumeTil = view.findViewById(R.id.fuel_volume_til);
    fuelPriceTil = view.findViewById(R.id.fuel_price_til);
    fuelVolumeTiet = view.findViewById(R.id.fuel_volume_tiet);
    fuelPriceTiet = view.findViewById(R.id.fuel_price_tiet);
    logBtn = view.findViewById(R.id.log_b);

    logBtn.setOnClickListener(this);

    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    navController = Navigation.findNavController(view);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.log_b:
        handleLogBtnClick();
        break;
    }
  }

  private void handleLogBtnClick() {
    if (fuelVolumeTiet == null || fuelPriceTiet == null) {
      Toast.makeText(getContext(), "Input empty!", Toast.LENGTH_SHORT).show();
      return;
    }

    float volume = 0f, price = 0f;

    try {
      volume = Float.parseFloat(fuelVolumeTiet.getText().toString());
      price = Float.parseFloat(fuelPriceTiet.getText().toString());
    } catch (NumberFormatException e) {
      Log.e(TAG, "handleLogBtnClick: Error when parsing", e);
      Toast.makeText(getContext(), "Input error!", Toast.LENGTH_SHORT).show();
      return;
    }

    if (volume < 0f) {
      fuelVolumeTil.setError("Enter a valid fuel volume.");
      return;
    }
    if (price < 0f) {
      fuelPriceTil.setError("Enter a valid price.");
      return;
    }

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference fuelDataDbRef =
        firebaseDatabase.getReference(Constants.FIREBASE_RDB_FUEL_DATA_REF);

    String newId = fuelDataDbRef.push().getKey();

    FuelData fuelData = new FuelData(volume, price);

    fuelDataDbRef
        .child(newId)
        .setValue(fuelData)
        .addOnSuccessListener(
            new OnSuccessListener<Void>() {
              @Override
              public void onSuccess(Void aVoid) {
                Log.d(TAG, "handleLogBtnClick: Added fuel data log");
                Toast.makeText(getContext(), "Added fuel log!", Toast.LENGTH_SHORT).show();
                navController.popBackStack();
              }
            })
        .addOnFailureListener(
            new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "handleLogBtnClick: CANNOT add fuel data log", e);
                Toast.makeText(getContext(), "Cannot add fuel log!", Toast.LENGTH_SHORT).show();
              }
            });
  }
}

package com.example.carfinder;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

public class HomeFragment extends Fragment {
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View homeView = inflater.inflate(R.layout.fragment_home, container, false);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);

        final Button findCarButton = homeView.findViewById(R.id.find_car_button);
        final Button saveLocationButton = homeView.findViewById(R.id.save_location_button);

        if (!sharedPreferences.getAll().isEmpty()) {
            findCarButton.setEnabled(true);
        }

        saveLocationButton.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Saving location, please wait...", Toast.LENGTH_SHORT).show();
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, new CancellationTokenSource().getToken())
                        .addOnSuccessListener(location -> {
                            if (location != null) {
                                Log.d(TAG, "onSuccess: LAT: " + location.getLatitude());
                                Log.d(TAG, "onSuccess: LNG: " + location.getLongitude());
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putFloat("latitude", (float) location.getLatitude());
                                editor.putFloat("longitude", (float) location.getLongitude());
                                editor.apply();
                                findCarButton.setEnabled(true);
                                Toast.makeText(requireContext(), "Location saved successfully!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "Location error!", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(getContext(), "Location permissions not granted!", Toast.LENGTH_SHORT).show();
            }
        });

        findCarButton.setOnClickListener(view -> {
            LocatorFragment locatorFragment = new LocatorFragment();
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.activity_main, locatorFragment).addToBackStack(null).commit();
        });

        return homeView;
    }
}

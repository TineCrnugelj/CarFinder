package com.example.carfinder;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Map;

public class LocatorFragment extends Fragment {
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View locatorView = inflater.inflate(R.layout.fragment_locator, container, false);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        //SharedPreferences.Editor editor = sharedPreferences.edit();

        double storedLatitude = Double.parseDouble(sharedPreferences.getString("lat", ""));
        double storedLongitude = Double.parseDouble(sharedPreferences.getString("lan", ""));

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                float[] results = new float[1];
                                System.out.println("Stored LAT: " + storedLatitude);
                                System.out.println("Stored LON: " + storedLongitude);
                                System.out.println("Current LAT: " + location.getLatitude());
                                System.out.println("Current LON: " + location.getLongitude());

                                Location.distanceBetween(location.getLatitude(), location.getLongitude(), storedLatitude, storedLongitude, results);
                                TextView distanceTextView = locatorView.findViewById(R.id.distanceText);

                                distanceTextView.append(results[0] + "m");
                            } else {
                                Toast.makeText(getActivity(), "Location error!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        } else {
            Toast.makeText(getContext(), "Location permissions not granted!", Toast.LENGTH_SHORT).show();
        }

        return locatorView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}

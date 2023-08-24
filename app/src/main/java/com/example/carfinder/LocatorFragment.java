package com.example.carfinder;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;


/*
    System.out.println("Stored LAT: " + storedLatitude);
    System.out.println("Stored LON: " + storedLongitude);
    System.out.println("Current LAT: " + location.getLatitude());
    System.out.println("Current LON: " + location.getLongitude());

    Location.distanceBetween(location.getLatitude(), location.getLongitude(), storedLatitude, storedLongitude, results);


    TextView distanceTextView = locatorView.findViewById(R.id.distanceText);

    distanceTextView.append(results[0] + "m");

*/

public class LocatorFragment extends Fragment implements LocationListener {
    private LocationManager locationManager;
    View locatorView;
    private double currentLatitude;
    private double currentLongitude;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        locatorView = inflater.inflate(R.layout.fragment_locator, container, false);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, LocatorFragment.this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getContext(), "Location permissions not granted!", Toast.LENGTH_SHORT).show();
        }

        return locatorView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);

        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();
        double storedLatitude = sharedPreferences.getFloat("latitude", 0.0f);
        double storedLongitude = sharedPreferences.getFloat("longitude", 0.0f);
        float[] results = new float[1];

        Location.distanceBetween(currentLatitude, currentLongitude, storedLatitude, storedLongitude, results);
        TextView distanceTextView = locatorView.findViewById(R.id.distance);

        String distanceText = String.format("%.1f m", results[0]);
        distanceTextView.setText(distanceText);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }
}

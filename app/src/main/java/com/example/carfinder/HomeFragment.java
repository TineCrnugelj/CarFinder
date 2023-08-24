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
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class HomeFragment extends Fragment implements LocationListener {
    private LocationManager locationManager;
    private boolean locationUpdateReceived = false;

    private double latitude;
    private double longitude;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View homeView = inflater.inflate(R.layout.fragment_home, container, false);

        // Save current location
        final Button saveLocationButton = homeView.findViewById(R.id.save_location_button);
        saveLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // store in sharedPrefs
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    try {
                        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, HomeFragment.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getContext(), "Location permissions not granted!", Toast.LENGTH_SHORT).show();
                }

                if (locationUpdateReceived) {
                    SharedPreferences sharedPreferences = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putFloat("latitude", (float) latitude);
                    editor.putFloat("longitude", (float) longitude);
                    editor.apply();
                    System.out.println("Saved: LAT: " + latitude + " LON: " + longitude);
                    Toast.makeText(requireContext(), "Location saved successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Location data not available yet. Please try again.", Toast.LENGTH_SHORT).show();
                }


            }
        });

        // Navigate to car locator view
        final Button findCarButton = homeView.findViewById(R.id.find_car_button);
        findCarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocatorFragment locatorFragment = new LocatorFragment();
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.activity_main, locatorFragment).addToBackStack(null).commit();
            }
        });

        return homeView;
    }
    @Override
    public void onLocationChanged(@NonNull Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        locationUpdateReceived = true;
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

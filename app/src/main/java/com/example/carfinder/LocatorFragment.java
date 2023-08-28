package com.example.carfinder;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

public class LocatorFragment extends Fragment implements LocationListener {
    SharedPreferences sharedPreferences;
    private ImageView compassImage;
    private float currentDegree = 0f;
    View locatorView;

    private boolean isDarkModeEnabled() {
        int nightModeFlags =
                requireContext().getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        locatorView = inflater.inflate(R.layout.fragment_locator, container, false);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, LocatorFragment.this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getContext(), "Location permissions not granted!", Toast.LENGTH_SHORT).show();
        }

        sharedPreferences = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        compassImage = locatorView.findViewById(R.id.compassImg);

        if (isDarkModeEnabled()) {
            compassImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.compass_needle_dark, null));
        }

        return locatorView;
    }

    private void rotateCompassNeedle(float degrees) {
        // Create a rotation animation
        RotateAnimation rotateAnimation = new RotateAnimation(
                currentDegree,
                degrees,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);

        rotateAnimation.setDuration(200);
        rotateAnimation.setFillAfter(true);
        compassImage.startAnimation(rotateAnimation);
        currentDegree = degrees;
    }

    private double bearing(double startLat, double startLng, double endLat, double endLng) {
        double deltaLng = endLng - startLng;
        double y = Math.sin(Math.toRadians(deltaLng)) * Math.cos(Math.toRadians(endLat));
        double x = Math.cos(Math.toRadians(startLat)) * Math.sin(Math.toRadians(endLat)) - Math.sin(Math.toRadians(startLat)) * Math.cos(Math.toRadians(endLat)) * Math.cos(Math.toRadians(deltaLng));

        return (Math.toDegrees(Math.atan2(y, x)) + 360 ) % 360;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        double storedLatitude = sharedPreferences.getFloat("latitude", 0.0f);
        double storedLongitude = sharedPreferences.getFloat("longitude", 0.0f);
        Log.d(TAG, "Stored LAT: " + storedLatitude + " stored LNG: " + storedLongitude);
        float[] results = new float[1];

        Location.distanceBetween(currentLatitude, currentLongitude, storedLatitude, storedLongitude, results);
        TextView distanceTextView = locatorView.findViewById(R.id.distance);
        TextView loadingTextView = locatorView.findViewById(R.id.loadingText);
        loadingTextView.setText("");

        String distanceText = String.format("%.1f m", results[0]);
        distanceTextView.setText(distanceText);

        double bearing = bearing(currentLatitude, currentLongitude, storedLatitude, storedLongitude);

        rotateCompassNeedle((float) bearing);
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

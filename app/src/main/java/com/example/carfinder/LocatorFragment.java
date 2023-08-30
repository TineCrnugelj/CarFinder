package com.example.carfinder;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

import java.util.Arrays;
import java.util.Locale;

public class LocatorFragment extends Fragment implements LocationListener, SensorEventListener {

    private SensorManager sensorManager;
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    SharedPreferences sharedPreferences;
    private ImageView compassImage;
    private float currentDegree = 0f;
    private float azimuthDegrees = 0.0f;
    View locatorView;

    private boolean isDarkModeEnabled() {
        int nightModeFlags =
                requireContext().getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;

        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    private void rotateCompassNeedle(float degrees) {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        locatorView = inflater.inflate(R.layout.fragment_locator, container, false);
        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);

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

    @Override
    public void onResume() {
        super.onResume();

        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        sensorManager.unregisterListener(this);
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

        String distanceText = String.format(Locale.getDefault(), "%.1f m", results[0]);
        distanceTextView.setText(distanceText);

        double bearing = bearing(currentLatitude, currentLongitude, storedLatitude, storedLongitude);

        rotateCompassNeedle((float) bearing);
    }

    public void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);

        // "rotationMatrix" now has up-to-date information.

        SensorManager.getOrientation(rotationMatrix, orientationAngles);

        azimuthDegrees = (float) Math.toDegrees(orientationAngles[0]);
        Log.d(TAG, "Angle: " + Arrays.toString(orientationAngles));
        // "orientationAngles" now has up-to-date information.
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading,
                    0, accelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading,
                    0, magnetometerReading.length);
        }

        updateOrientationAngles();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}

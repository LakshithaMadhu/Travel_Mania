package com.s22010008.travelmania;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionsHelper {
    private static final String TAG = "PermissionsHelper";
    private static final int PERMISSION_REQUEST_CODE = 100;

    private Activity activity;
    private PermissionCallback permissionCallback;

    public PermissionsHelper(Activity activity, PermissionCallback permissionCallback) {
        this.activity = activity;
        this.permissionCallback = permissionCallback;
    }

    public void requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            } else {
                permissionCallback.onPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        } else {
            // Permission automatically granted on devices below Marshmallow
            permissionCallback.onPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionCallback.onPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION);
            } else {
                Log.d(TAG, "Permission denied: " + permissions[0]);
                // Handle permission denied case
            }
        }
    }

    public interface PermissionCallback {
        void onPermissionGranted(String permission);
    }
}

package com.example.voiseassisttant;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionsManager {
    public static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 100;


    @RequiresApi(api = Build.VERSION_CODES.S)
    public static boolean hasBluetoothPermissions(Context context) {
        // בדיקה אם יש הרשאות מתאימות לבלוטות'
        return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public static void requestBluetoothPermissions(MainActivity activity) {
        // בקשת הרשאות בלוטות'
        ActivityCompat.requestPermissions(activity, new String[]{
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
        }, BLUETOOTH_PERMISSION_REQUEST_CODE);
    }
}
package com.example.voiseassisttant;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SettingsManager {
    private Context context;
    private BluetoothAdapter bluetoothAdapter;

    public SettingsManager(Context context) {
        this.context = context;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }


    public void toggleWifi(boolean enable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10 and above
            Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
            context.startActivity(panelIntent);
            Toast.makeText(context, "אנא הפעל/כבה את ה-Wi-Fi בהגדרות", Toast.LENGTH_LONG).show();
        } else {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CHANGE_WIFI_STATE)
                        != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context, "אין הרשאה להפעיל/לכבות Wi-Fi", Toast.LENGTH_SHORT).show();
                    return;
                }
                wifiManager.setWifiEnabled(enable);
                String status = enable ? "מופעל" : "כבוי";
                Toast.makeText(context, "Wi-Fi " + status, Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void toggleBluetooth(boolean enable) {
        if (bluetoothAdapter == null) {
            Toast.makeText(context, "המכשיר אינו תומך ב-Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "אין הרשאה להפעיל/לכבות Bluetooth", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (enable && !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivity(enableBtIntent);
        } else if (!enable && bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
        }

        // Check the actual state after a short delay
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        boolean actualState = bluetoothAdapter.isEnabled();
                        String status = actualState ? "מופעל" : "כבוי";
                        Toast.makeText(context, "Bluetooth " + status, Toast.LENGTH_SHORT).show();
                    }
                },
                4000); // 2000 milliseconds delay
    }



    public void toggleBatterySaver(boolean enable) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            if (enable && !powerManager.isPowerSaveMode()) {
                Intent intent = new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS);
                context.startActivity(intent);
            } else if (!enable && powerManager.isPowerSaveMode()) {
                Intent intent = new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS);
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "מצב חיסכון בסוללה כבר במצב הרצוי", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void toggleLocation() {
        Activity activity = (Activity) context;

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            return;
        }

        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        context.startActivity(intent);
        Toast.makeText(context, "עבור להגדרות המיקום כדי להפעיל/לכבות", Toast.LENGTH_SHORT).show();
    }
}
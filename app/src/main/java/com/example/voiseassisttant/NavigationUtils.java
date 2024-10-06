package com.example.voiseassisttant;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class NavigationUtils {
    public static void openWaze(Context context, String address) {
        try {
            String uri = "waze://?q=" + Uri.encode(address);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            openGoogleMaps(context, address);
        }
    }

    public static void openGoogleMaps(Context context, String address) {
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(mapIntent);
        } else {
            Toast.makeText(context, "לא ניתן למצוא אפליקציית מפות", Toast.LENGTH_SHORT).show();
        }
    }
}
package com.example.voiseassisttant;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int REQUEST_BLUETOOTH_PERMISSION = 201;
    private static final int REQUEST_NOTIFICATION_LISTENER_PERMISSION = 202;

    private VoiceRecognitionManager voiceRecognitionManager;
    private SettingsManager settingsManager;
    private Button btnStartVoiceRecognition;
    private Button btnStopVoiceRecognition;
    private ProgressBar progressBar;
    private TextView tvRecognizedText;
    private Switch switchDrivingMode;
    private boolean permissionToRecordAccepted = false;
    private boolean isListening = false;
    private Handler handler = new Handler();
    private static final long MAX_SILENCE_TIME = 2000; // 2 שניות
    private static final long MAX_RECORDING_TIME = 10000; // 10 שניות
    private long lastSoundTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        voiceRecognitionManager = new VoiceRecognitionManager(this);
        settingsManager = new SettingsManager(this);

        btnStartVoiceRecognition = findViewById(R.id.btnStartVoiceRecognition);
        btnStopVoiceRecognition = findViewById(R.id.btnStopVoiceRecognition);
        progressBar = findViewById(R.id.progressBar);
        tvRecognizedText = findViewById(R.id.tvRecognizedText);

        checkMicrophonePermission();
        checkBluetoothPermission();

        btnStartVoiceRecognition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permissionToRecordAccepted && !isListening) {
                    startVoiceRecognition();
                } else if (!permissionToRecordAccepted) {
                    Toast.makeText(MainActivity.this, "אין גישה למיקרופון", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnStopVoiceRecognition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isListening) {
                    stopVoiceRecognition();
                }
            }
        });


        switchDrivingMode = findViewById(R.id.switchDrivingMode);
        switchDrivingMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                startDrivingMode();
            } else {
                stopDrivingMode();
            }
        });

        checkNotificationListenerPermission();
    }

    private void checkNotificationListenerPermission() {
        String enabledNotificationListeners = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(getPackageName())) {
            Toast.makeText(this, "נא לאשר גישה להתראות", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        }
    }

    private void startDrivingMode() {
        Intent intent = new Intent(this, YourNotificationListenerService.class);
        startService(intent);
        Toast.makeText(this, "מצב נהיגה הופעל", Toast.LENGTH_SHORT).show();
    }

    private void stopDrivingMode() {
        Intent intent = new Intent(this, YourNotificationListenerService.class);
        stopService(intent);
        Toast.makeText(this, "מצב נהיגה כובה", Toast.LENGTH_SHORT).show();
    }

    private void checkMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        } else {
            permissionToRecordAccepted = true;
        }
    }

    private void checkBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (!permissionToRecordAccepted) {
                    Toast.makeText(this, "אין גישה למיקרופון", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_BLUETOOTH_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // הרשאת Bluetooth התקבלה
                    Toast.makeText(this, "הרשאת Bluetooth התקבלה", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "הרשאת Bluetooth נדחתה", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    private void startVoiceRecognition() {
        isListening = true;
        progressBar.setVisibility(View.VISIBLE);
        tvRecognizedText.setText("");
        btnStartVoiceRecognition.setEnabled(false);
        btnStopVoiceRecognition.setEnabled(true);
        lastSoundTime = System.currentTimeMillis();

        voiceRecognitionManager.startListening(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Toast.makeText(MainActivity.this, "התחל להכתיב", Toast.LENGTH_SHORT).show();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isListening) {
                            stopVoiceRecognition();
                        }
                    }
                }, MAX_RECORDING_TIME);
            }

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {
                if (rmsdB > 1.0f) {
                    lastSoundTime = System.currentTimeMillis();
                } else if (System.currentTimeMillis() - lastSoundTime > MAX_SILENCE_TIME) {
                    stopVoiceRecognition();
                }
            }

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                stopVoiceRecognition();
            }

            @Override
            public void onError(int error) {
                stopVoiceRecognition();
                Toast.makeText(MainActivity.this, "שגיאה בזיהוי דיבור", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String recognizedText = matches.get(0);
                    tvRecognizedText.setText(recognizedText);
                    CommandProcessor.processCommand(MainActivity.this, recognizedText);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void stopVoiceRecognition() {
        if (isListening) {
            isListening = false;
            progressBar.setVisibility(View.GONE);
            btnStartVoiceRecognition.setEnabled(true);
            btnStopVoiceRecognition.setEnabled(false);
            voiceRecognitionManager.stopListening();
            handler.removeCallbacksAndMessages(null);
        }
    }
}


/*
app/
├── java/
│   └── com.example.voiceassistant/
│       ├── MainActivity.java
│       ├── VoiceRecognitionManager.java
│       ├── CommandProcessor.java
│       ├── ServerRequestHandler.java
│       ├── NavigationUtils.java
│       ├── CallUtils.java
│       ├── PermissionManager
│       ├── SettingsManager
│       └── YourNotificationListenerService.java
└── res/
    └── layout/
        └── activity_main.xml
*/
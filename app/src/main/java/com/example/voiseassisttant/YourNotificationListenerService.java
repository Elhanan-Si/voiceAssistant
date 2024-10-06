package com.example.voiseassisttant;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;


import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

public class YourNotificationListenerService extends NotificationListenerService {
    private TextToSpeech tts;
    private Queue<String> messageQueue = new LinkedList<>();
    private boolean isSpeaking = false;
    private String lastMessageId = "";

    @Override
    public void onCreate() {
        super.onCreate();
        initializeTextToSpeech();
        Log.d("StartActivity", "Activity started");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case "PAUSE_TTS":
                    pauseSpeaking();
                    break;
                case "RESUME_TTS":
                    resumeSpeaking();
                    break;
                case "STOP_TTS":
                    stopSpeaking();
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void initializeTextToSpeech() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(new Locale("he", "IL"));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "שפה לא נתמכת");
                }
            } else {
                Log.e("TTS", "אתחול נכשל");
            }
        });

        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                isSpeaking = true;
                showButtonActivity();
            }

            @Override
            public void onDone(String utteranceId) {
                isSpeaking = false;
                hideButtonActivity();
                speakNextMessage();
            }

            @Override
            public void onError(String utteranceId) {
                isSpeaking = false;
                hideButtonActivity();
                speakNextMessage();
            }
        });
    }

    private void showButtonActivity() {
        Intent intent = new Intent(this, ButtonActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void hideButtonActivity() {
        sendBroadcast(new Intent("com.example.voiseassisttant.FINISH_BUTTON_ACTIVITY"));
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if ("com.whatsapp".equals(sbn.getPackageName())) {
            handleWhatsAppNotification(sbn);
        }
    }

    private void handleWhatsAppNotification(StatusBarNotification sbn) {
        Notification notification = sbn.getNotification();
        Bundle extras = notification.extras;
        String title = extras.getString(Notification.EXTRA_TITLE);
        String text = getNotificationText(extras);
        String messageId = sbn.getId() + title + text; // Create a unique ID for this message

        if (title != null && text != null && !messageId.equals(lastMessageId)) {
            lastMessageId = messageId; // Update the last processed message ID
            addMessageToQueue("הודעה חדשה מ" + title + ". " + text);
        }
    }

    private String getNotificationText(Bundle extras) {
        CharSequence[] textLines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
        if (textLines != null && textLines.length > 0) {
            return textLines[textLines.length - 1].toString();
        } else {
            CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);

            if (text != null) {
                return text.toString();
            }

            return null;
        }
    }

    private void addMessageToQueue(String message) {
        messageQueue.add(message);
        if (!isSpeaking) {
            speakNextMessage();
        }
    }

    private void speakNextMessage() {
        if (!messageQueue.isEmpty()) {
            String message = messageQueue.poll();
            isSpeaking = true;
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "MessageId");
        } else {
            isSpeaking = false;
        }
    }

    public void pauseSpeaking() {
        if (tts != null && tts.isSpeaking()) {
            tts.stop();
        }
    }

    public void resumeSpeaking() {
        if (!isSpeaking) {
            speakNextMessage();
        }
    }

    public void stopSpeaking() {
        if (tts != null) {
            tts.stop();
            messageQueue.clear();
        }
        hideButtonActivity();
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}

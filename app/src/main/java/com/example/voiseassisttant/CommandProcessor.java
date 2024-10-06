package com.example.voiseassisttant;

public class CommandProcessor {
    private static SettingsManager settingsManager;
    public static void processCommand(MainActivity activity, String command) {
        settingsManager = new SettingsManager(activity);

        if (command.contains("נווט ל")) {
            String address = command.replace("נווט ל", "").trim();
            NavigationUtils.openWaze(activity, address);
        }

        // הפעלת בלוטוס
        if (command.contains("הפעל בלוטוס") || command.contains("אפל בלוטוס") || command.contains("תפעיל בלוטוס")) {
            settingsManager.toggleBluetooth(true);
        } else if (command.contains("כבה בלוטוס") || command.contains("קבה בלוטוס") || command.contains("תכבה בלוטוס")) {
            settingsManager.toggleBluetooth(false);
        }

        // הפעלת וויפי
        if (command.contains("הפעל wifi") || command.contains("תפעיל wifi")) {
            settingsManager.toggleWifi(true);
        } else if (command.contains("כבה wifi") || command.contains("תכבה wifi")) {
            settingsManager.toggleWifi(false);
        }

        // הפעלת חיסכון בסוללה
        if (command.contains("הפעל חיסכון בסוללה") || command.contains("תפעיל חיסכון בסוללה")) {
            settingsManager.toggleBatterySaver(true);
        } else if (command.contains("כבה חיסכון בסוללה") || command.contains("תכבה חיסכון בסוללה")) {
            settingsManager.toggleBatterySaver(false);
        }

        // הפעלת מיקום
        if (command.contains("הפעל מיקום") || command.contains("כבה מיקום") || command.contains("תפעיל מיקום") || command.contains("תכבה מיקום")) {
            settingsManager.toggleLocation();
        }


        // Add more command processing here in the future
    }
}
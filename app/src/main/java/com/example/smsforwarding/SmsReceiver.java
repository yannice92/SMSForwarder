package com.example.smsforwarding;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;

public class SmsReceiver extends BroadcastReceiver {
    private static final String PREFS_NAME = "TelegramConfig";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Log.d("SMS", "testing");
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Log.d("SMS", "permission granted");
            // Mengambil SMS dari intent
            SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            if (messages != null) {
                StringBuilder messageBuilder = new StringBuilder();
                for (SmsMessage smsMessage : messages) {
                    String smsBody = smsMessage.getMessageBody();
                    String smsAddress = smsMessage.getDisplayOriginatingAddress();
                    String senderName = getContactName(context, smsAddress); // Get the sender's name
                    Log.d("SMS", "From: " + smsAddress + ", Message: " + smsBody);
                    // Append the SMS body to the message builder
                    String senderFull = senderName != null ? senderName + " - " + smsAddress : smsAddress;

                    messageBuilder.append("From: ").append(senderFull).append("\n\n");
                    messageBuilder.append(smsBody).append("\n"); // Add a newline for separation
                    sendMessageToTelegram(context, messageBuilder.toString().trim());
                }
            }
        } else {
            Log.d("SMSReceiver", "Permission to read SMS not granted.");
        }
    }


    @SuppressLint("Range")
    private String getContactName(Context context, String phoneNumber) {
        String contactName = null;
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }
        } catch (Exception e) {
            Log.e("SmsReceiver", "Error retrieving contact name", e);
        }

        return contactName;
    }

    private void sendMessageToTelegram(Context context, String message) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String botToken = preferences.getString("bot_token", "");
        String chatId = preferences.getString("chat_id", "");

        String encodedMessage = message.replace("\n", "%0A"); // Replace newlines with URL-encoded newlines
        String urlString = "https://api.telegram.org/bot" + botToken + "/sendMessage?chat_id=" + chatId + "&text=" + encodedMessage;

        new Thread(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                int StatusCode = conn.getResponseCode(); // Untuk mengirim permintaan
                Log.d("Telegram", "Status Code: " + StatusCode);
                if (StatusCode == HttpURLConnection.HTTP_OK) {
                    Log.d("Telegram", "Message sent: " + message);
                }else {
                    Log.d("Telegram", "Message Failed");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}

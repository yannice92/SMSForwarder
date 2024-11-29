package com.example.smsforwarding;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SmsReceiver extends BroadcastReceiver {
    private static final String PREFS_NAME = "TelegramConfig";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Check if the intent action matches the expected action for SMS_RECEIVED
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.d("SMS", "permission granted");
                // Mengambil SMS dari intent
                SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
                if (messages != null) {
                    StringBuilder messageBodyBuilder = new StringBuilder();
                    for (SmsMessage smsMessage : messages) {
                        String smsBody = smsMessage.getMessageBody();
                        messageBodyBuilder.append(smsBody); // Combine message parts
                    }
                    String smsAddress = messages[0].getDisplayOriginatingAddress(); // Get the sender from the first message
                    String senderName = getContactName(context, smsAddress); // Get the sender's name
                    // Append the SMS body to the message builder
                    String senderFull = senderName != null ? senderName + " - " + smsAddress : smsAddress;
                    Log.d("SMS", "From: " + smsAddress + ", Message: " + messageBodyBuilder.toString().trim());
                    String messageBuilder2 = "From: " + senderFull + "\n\n" +
                            messageBodyBuilder.toString().trim() + "\n"; // Add a newline for separation
                    String combinedMessage = messageBuilder2.trim();

                    sendMessageToTelegram(context, combinedMessage);
                }
            } else {
                Log.d("SMSReceiver", "Permission to read SMS not granted.");
            }
        }else {
            Log.d("SMSReceiver", "Received intent with unexpected action: " + intent.getAction());
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

        //String encodedMessage = message.replace("\n", "%0A"); // Replace newlines with URL-encoded newlines
        Log.d("Telegram", "msg: " + message);
        try {
            String encodedMessage = URLEncoder.encode(message, "UTF-8"); // URL encode the message
            String urlString = "https://api.telegram.org/bot" + botToken + "/sendMessage?chat_id=" + chatId + "&text=" + encodedMessage;
            Log.d("Telegram", urlString);
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
        } catch (UnsupportedEncodingException e) {
            Log.e("SmsReceiver", "Error encoding message", e);
        }
    }
}

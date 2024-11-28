package com.example.smsforwarding;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ConfigActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "TelegramConfig";
    private EditText botTokenEditText;
    private EditText chatIdEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        botTokenEditText = findViewById(R.id.botTokenEditText);
        chatIdEditText = findViewById(R.id.chatIdEditText);
        Button saveButton = findViewById(R.id.saveButton);

        // Load saved configuration
        loadConfig();

        saveButton.setOnClickListener(v -> saveConfig());
    }

    private void loadConfig() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String botToken = preferences.getString("bot_token", "");
        String chatId = preferences.getString("chat_id", "");

        botTokenEditText.setText(botToken);
        chatIdEditText.setText(chatId);
    }

    private void saveConfig() {
        String botToken = botTokenEditText.getText().toString();
        String chatId = chatIdEditText.getText().toString();

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("bot_token", botToken);
        editor.putString("chat_id", chatId);
        editor.apply();

        Toast.makeText(this, "Configuration saved!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
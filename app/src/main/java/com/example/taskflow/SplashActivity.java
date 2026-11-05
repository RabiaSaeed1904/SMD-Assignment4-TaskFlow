package com.example.taskflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate; // Make sure to add this import

public class SplashActivity extends AppCompatActivity {

    private Button btnGetStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Force Light Mode to fix color contrast issues
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initializeViews();
        setupGetStartedButton();
    }

    private void initializeViews() {
        btnGetStarted = findViewById(R.id.btnGetStarted);
    }

    private void setupGetStartedButton() {
        btnGetStarted.setOnClickListener(view -> openHomeScreen());
    }

    private void openHomeScreen() {
        Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
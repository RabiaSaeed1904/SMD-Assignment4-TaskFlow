package com.example.taskflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private Button btnGetStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        Intent intent = new Intent(
                SplashActivity.this,
                HomeActivity.class
        );

        startActivity(intent);
        finish();
    }
}
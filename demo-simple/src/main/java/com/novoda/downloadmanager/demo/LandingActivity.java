package com.novoda.downloadmanager.demo;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        findViewById(R.id.migration).setOnClickListener(view -> navigateTo(MigrationActivity.class));
        findViewById(R.id.downloads).setOnClickListener(view -> navigateTo(MainActivity.class));
    }

    private void navigateTo(Class<?> activityClass) {
        Intent intent = new Intent(getApplicationContext(), activityClass);
        startActivity(intent);
    }
}

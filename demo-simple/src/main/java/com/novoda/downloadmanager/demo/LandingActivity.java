package com.novoda.downloadmanager.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

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

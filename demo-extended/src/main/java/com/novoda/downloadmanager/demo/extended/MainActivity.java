package com.novoda.downloadmanager.demo.extended;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.novoda.downloadmanager.demo.R;
import com.novoda.downloadmanager.demo.extended.batches.BatchDownloadsActivity;
import com.novoda.downloadmanager.demo.extended.delete.DeleteActivity;
import com.novoda.downloadmanager.demo.extended.extra_data.ExtraDataActivity;
import com.novoda.downloadmanager.demo.extended.pause_resume.PauseResumeActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.pause_resume_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PauseResumeActivity.class));
            }
        });

        findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DeleteActivity.class));
            }
        });

        findViewById(R.id.batches_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, BatchDownloadsActivity.class));
            }
        });

        findViewById(R.id.extra_data_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(MainActivity.this, ExtraDataActivity.class));
                    }
                });
    }

}

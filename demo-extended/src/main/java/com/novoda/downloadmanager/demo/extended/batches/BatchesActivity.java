package com.novoda.downloadmanager.demo.extended.batches;

import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.RadioGroup;

import com.novoda.downloadmanager.DownloadManagerBuilder;
import com.novoda.downloadmanager.demo.R;
import com.novoda.downloadmanager.demo.extended.QueryTimestamp;
import com.novoda.downloadmanager.lib.BatchQuery;
import com.novoda.downloadmanager.lib.DownloadManager;

import java.util.ArrayList;
import java.util.List;

public class BatchesActivity extends AppCompatActivity implements QueryForBatchesAsyncTask.Callback {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final QueryTimestamp lastQueryTimestamp = new QueryTimestamp();

    private DownloadManager downloadManager;
    private BatchesAdapter adapter;
    private BatchQuery query = BatchQuery.ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_batches);

        ListView listView = (ListView) findViewById(R.id.show_batches_list);
        downloadManager = DownloadManagerBuilder.from(this)
                .withVerboseLogging()
                .build();
        adapter = new BatchesAdapter(new ArrayList<Batch>());
        listView.setAdapter(adapter);

        listView.setEmptyView(findViewById(R.id.show_batches_no_batches_view));
        RadioGroup queryGroup = (RadioGroup) findViewById(R.id.show_batches_query_radio_group);
        queryGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.show_batches_query_all:
                        query = BatchQuery.ALL;
                        break;
                    case R.id.show_batches_query_successful:
                        query = new BatchQuery.Builder().withStatusFilter(DownloadManager.STATUS_SUCCESSFUL).build();
                        break;
                    case R.id.show_batches_query_pending:
                        query = new BatchQuery.Builder().withStatusFilter(DownloadManager.STATUS_PENDING).build();
                        break;
                    case R.id.show_batches_query_downloading:
                        query = new BatchQuery.Builder().withStatusFilter(DownloadManager.STATUS_RUNNING).build();
                        break;
                    case R.id.show_batches_query_failed:
                        query = new BatchQuery.Builder().withStatusFilter(DownloadManager.STATUS_FAILED).build();
                        break;
                    case R.id.show_batches_query_failed_pending:
                        query = new BatchQuery.Builder().withStatusFilter(DownloadManager.STATUS_FAILED | DownloadManager.STATUS_PENDING).build();
                        break;
                    case R.id.show_batches_query_live:
                        query = new BatchQuery.Builder().withSortByLiveness().build();
                        break;
                    case R.id.show_batches_query_deleting:
                        query = new BatchQuery.Builder().withStatusFilter(DownloadManager.STATUS_DELETING).build();
                        break;
                    default:
                        break;
                }
                queryForBatches(query);
            }
        });
        queryForBatches(BatchQuery.ALL);

    }

    private void queryForBatches(BatchQuery query) {
        QueryForBatchesAsyncTask.newInstance(downloadManager, this).execute(query);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getContentResolver().registerContentObserver(downloadManager.getBatchesUri(), true, updateSelf);
    }

    private final ContentObserver updateSelf = new ContentObserver(handler) {

        @Override
        public void onChange(boolean selfChange) {
            if (lastQueryTimestamp.updatedRecently()) {
                return;
            }
            queryForBatches(query);
            lastQueryTimestamp.setJustUpdated();
        }

    };

    @Override
    protected void onStop() {
        super.onStop();
        getContentResolver().unregisterContentObserver(updateSelf);
    }

    @Override
    public void onQueryResult(List<Batch> batches) {
        adapter.updateBatches(batches);
    }
}

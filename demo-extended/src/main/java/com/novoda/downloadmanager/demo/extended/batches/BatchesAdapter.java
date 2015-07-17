package com.novoda.downloadmanager.demo.extended.batches;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.novoda.downloadmanager.demo.R;

import java.util.List;
import java.util.Locale;

public class BatchesAdapter extends BaseAdapter {
    private final List<Batch> batches;

    public BatchesAdapter(List<Batch> batches) {
        this.batches = batches;
    }

    @Override
    public int getCount() {
        return batches.size();
    }

    @Override
    public Batch getItem(int position) {
        return batches.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = View.inflate(parent.getContext(), R.layout.list_item_batch, null);

        final Batch batch = getItem(position);
        TextView idTextView = (TextView) view.findViewById(R.id.batch_id_text);
        TextView titleTextView = (TextView) view.findViewById(R.id.batch_title_text);
        TextView statusTextView = (TextView) view.findViewById(R.id.batch_status_text);
        TextView totalSizeTextView = (TextView) view.findViewById(R.id.batch_total_size_text);
        TextView currentSizeTextView = (TextView) view.findViewById(R.id.batch_current_size_text);
        TextView extraDataTextView = (TextView) view.findViewById(R.id.batch_extra_data_text);

        idTextView.setText(String.format(Locale.getDefault(), "Id: %d", batch.getId()));
        titleTextView.setText(batch.getTitle());
        String status = String.format(Locale.getDefault(), "Status: %s", batch.getDownloadStatusText());
        statusTextView.setText(status);
        totalSizeTextView.setText(String.format(Locale.getDefault(), "Total size: %d bytes", batch.getTotalBytes()));
        currentSizeTextView.setText(String.format(Locale.getDefault(), "Current size: %d bytes", batch.getCurrentBytes()));
        extraDataTextView.setText(batch.getExtraData());

        return view;
    }

    public void updateBatches(List<Batch> batches) {
        this.batches.clear();
        this.batches.addAll(batches);
        notifyDataSetChanged();
    }
}

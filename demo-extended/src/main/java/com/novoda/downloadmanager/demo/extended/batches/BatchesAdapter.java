package com.novoda.downloadmanager.demo.extended.batches;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.novoda.downloadmanager.demo.R;

import java.util.List;
import java.util.Locale;

public class BatchesAdapter extends RecyclerView.Adapter<BatchesAdapter.ViewHolder> {
    private final List<Batch> batches;

    public BatchesAdapter(List<Batch> batches) {
        this.batches = batches;
    }

    public void updateBatches(List<Batch> batches) {
        this.batches.clear();
        this.batches.addAll(batches);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        return new ViewHolder(View.inflate(viewGroup.getContext(), R.layout.list_item_batch, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Batch batch = batches.get(position);
        viewHolder.idTextView.setText(String.format(Locale.getDefault(), "Id: %d", batch.getId()));
        viewHolder.titleTextView.setText(batch.getTitle());
        String status = String.format(Locale.getDefault(), "Status: %s", batch.getDownloadStatusText());
        viewHolder.statusTextView.setText(status);
        viewHolder.totalSizeTextView.setText(String.format(Locale.getDefault(), "Total size: %d bytes", batch.getTotalBytes()));
        viewHolder.currentSizeTextView.setText(String.format(Locale.getDefault(), "Current size: %d bytes", batch.getCurrentBytes()));
        viewHolder.extraDataTextView.setText(batch.getExtraData());
    }

    @Override
    public int getItemCount() {
        return batches.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView idTextView;
        private final TextView titleTextView;
        private final TextView statusTextView;
        private final TextView totalSizeTextView;
        private final TextView currentSizeTextView;
        private final TextView extraDataTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            idTextView = (TextView) itemView.findViewById(R.id.batch_id_text);
            titleTextView = (TextView) itemView.findViewById(R.id.batch_title_text);
            statusTextView = (TextView) itemView.findViewById(R.id.batch_status_text);
            totalSizeTextView = (TextView) itemView.findViewById(R.id.batch_total_size_text);
            currentSizeTextView = (TextView) itemView.findViewById(R.id.batch_current_size_text);
            extraDataTextView = (TextView) itemView.findViewById(R.id.batch_extra_data_text);
        }
    }
}

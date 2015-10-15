package com.novoda.downloadmanager.demo.extended.batches;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.novoda.downloadmanager.demo.R;
import com.novoda.downloadmanager.demo.extended.BeardDownload;

import java.util.List;
import java.util.Locale;

public class BatchDownloadsAdapter extends RecyclerView.Adapter<BatchDownloadsAdapter.ViewHolder> {
    private final List<BeardDownload> beardDownloads;

    public BatchDownloadsAdapter(List<BeardDownload> beardDownloads) {
        this.beardDownloads = beardDownloads;
    }

    public void updateDownloads(List<BeardDownload> beardDownloads) {
        this.beardDownloads.clear();
        this.beardDownloads.addAll(beardDownloads);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        return new ViewHolder(View.inflate(viewGroup.getContext(), R.layout.list_item_download, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final BeardDownload beardDownload = beardDownloads.get(position);
        viewHolder.titleTextView.setText(beardDownload.getTitle());
        String text = String.format(Locale.getDefault(), "%1$s : %2$s\nBatch %3$d", beardDownload.getDownloadStatusText(), beardDownload.getFileName(), beardDownload.getBatchId());
        viewHolder.locationTextView.setText(text);
    }

    @Override
    public int getItemCount() {
        return beardDownloads.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleTextView;
        private final TextView locationTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView.findViewById(R.id.download_title_text);
            locationTextView = (TextView) itemView.findViewById(R.id.download_location_text);
        }
    }
}

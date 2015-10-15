package com.novoda.downloadmanager.demo.extended.pause_resume;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.novoda.downloadmanager.demo.R;
import com.novoda.downloadmanager.demo.extended.BeardDownload;

import java.util.List;
import java.util.Locale;

public class PauseResumeAdapter extends RecyclerView.Adapter<PauseResumeAdapter.ViewHolder> {
    private final List<BeardDownload> beardDownloads;
    private final Listener listener;

    public PauseResumeAdapter(List<BeardDownload> beardDownloads, Listener listener) {
        this.beardDownloads = beardDownloads;
        this.listener = listener;
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
        viewHolder.root.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(@NonNull View v) {
                        listener.onItemClick(beardDownload);
                    }
                }
        );
    }

    @Override
    public int getItemCount() {
        return beardDownloads.size();
    }

    interface Listener {
        void onItemClick(BeardDownload beardDownload);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final View root;
        private final TextView titleTextView;
        private final TextView locationTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            root = itemView;
            titleTextView = (TextView) itemView.findViewById(R.id.download_title_text);
            locationTextView = (TextView) itemView.findViewById(R.id.download_location_text);
        }
    }
}

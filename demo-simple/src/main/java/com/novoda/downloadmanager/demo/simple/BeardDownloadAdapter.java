package com.novoda.downloadmanager.demo.simple;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.novoda.downloadmanager.demo.R;

import java.util.List;

class BeardDownloadAdapter extends RecyclerView.Adapter<BeardDownloadAdapter.ViewHolder> {
    private final List<BeardDownload> beardDownloads;

    BeardDownloadAdapter(List<BeardDownload> beardDownloads) {
        this.beardDownloads = beardDownloads;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        return new ViewHolder(View.inflate(viewGroup.getContext(), R.layout.list_item_download, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final BeardDownload beardDownload = beardDownloads.get(position);
        viewHolder.titleTextView.setText(beardDownload.getTitle());
        viewHolder.locationTextView.setText(String.format("%s: %s", beardDownload.getDownloadStatusText(), beardDownload.getFileName()));
    }

    @Override
    public int getItemCount() {
        return beardDownloads.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleTextView;
        private final TextView locationTextView;

        ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.download_title_text);
            locationTextView = itemView.findViewById(R.id.download_location_text);
        }
    }
}

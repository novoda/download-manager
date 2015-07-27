package com.novoda.downloadmanager.demo.simple;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.novoda.downloadmanager.demo.R;

import java.util.List;

class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.ViewHolder> {
    private final List<Download> downloads;

    public DownloadAdapter(List<Download> downloads) {
        this.downloads = downloads;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        return new ViewHolder(View.inflate(viewGroup.getContext(), R.layout.list_item_download, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final Download download = downloads.get(position);
        viewHolder.titleTextView.setText(download.getTitle());
        viewHolder.locationTextView.setText(download.getDownloadStatusText() + ": " + download.getFileName());
    }

    @Override
    public int getItemCount() {
        return downloads.size();
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

package com.novoda.downloadmanager.demo.extended.delete;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.novoda.downloadmanager.demo.R;
import com.novoda.downloadmanager.demo.extended.Download;

import java.util.List;
import java.util.Locale;

public class DeleteAdapter extends RecyclerView.Adapter<DeleteAdapter.ViewHolder> {
    private final List<Download> downloads;
    private final Listener listener;

    public DeleteAdapter(List<Download> downloads, Listener listener) {
        this.downloads = downloads;
        this.listener = listener;
    }

    public void updateDownloads(List<Download> downloads) {
        this.downloads.clear();
        this.downloads.addAll(downloads);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        return new ViewHolder(View.inflate(viewGroup.getContext(), R.layout.list_item_download_delete, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final Download download = downloads.get(position);

        viewHolder.titleTextView.setText(download.getTitle());
        String text = String.format(Locale.getDefault(), "%1$s : %2$s\nBatch %3$d", download.getDownloadStatusText(), download.getFileName(), download.getBatchId());
        viewHolder.locationTextView.setText(text);

        if (listener == null) {
            viewHolder.deleteButton.setVisibility(View.GONE);
            viewHolder.deleteButton.setOnClickListener(null);
        } else {
            viewHolder.deleteButton.setVisibility(View.VISIBLE);
            viewHolder.deleteButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(@NonNull View v) {
                            listener.onDelete(download);
                        }
                    }
            );
        }
    }

    @Override
    public int getItemCount() {
        return downloads.size();
    }

    public interface Listener {
        void onDelete(Download download);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleTextView;
        private final TextView locationTextView;
        private final Button deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView.findViewById(R.id.download_title_text);
            locationTextView = (TextView) itemView.findViewById(R.id.download_location_text);
            deleteButton = (Button) itemView.findViewById(R.id.download_delete_button);
        }
    }
}

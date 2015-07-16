package com.novoda.downloadmanager.demo.extended.delete;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.novoda.downloadmanager.demo.R;
import com.novoda.downloadmanager.demo.extended.Download;

import java.util.List;
import java.util.Locale;

public class DeleteAdapter extends BaseAdapter {
    private final List<Download> downloads;
    private final Listener listener;

    public DeleteAdapter(List<Download> downloads) {
        this(downloads, null);
    }

    public DeleteAdapter(List<Download> downloads, Listener listener) {
        this.downloads = downloads;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return downloads.size();
    }

    @Override
    public Download getItem(int position) {
        return downloads.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = View.inflate(parent.getContext(), R.layout.list_item_download_delete, null);

        final Download download = getItem(position);
        TextView titleTextView = (TextView) view.findViewById(R.id.download_title_text);
        TextView locationTextView = (TextView) view.findViewById(R.id.download_location_text);
        Button deleteButton = (Button) view.findViewById(R.id.download_delete_button);

        titleTextView.setText(download.getTitle());
        String text = String.format(Locale.getDefault(), "%1$s : %2$s\nBatch %3$d", download.getDownloadStatusText(), download.getFileName(), download.getBatchId());
        locationTextView.setText(text);

        if (listener == null) {
            deleteButton.setVisibility(View.GONE);
            deleteButton.setOnClickListener(null);
        } else {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(@NonNull View v) {
                    listener.onDelete(download);
                }
            });
        }

        return view;
    }

    public void updateDownloads(List<Download> downloads) {
        this.downloads.clear();
        this.downloads.addAll(downloads);
        notifyDataSetChanged();
    }

    public interface Listener {
        void onDelete(Download download);
    }
}

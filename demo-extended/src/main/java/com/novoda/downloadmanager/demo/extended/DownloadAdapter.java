package com.novoda.downloadmanager.demo.extended;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.novoda.downloadmanager.demo.R;
import com.novoda.notils.caster.Views;

import java.util.List;

class DownloadAdapter extends BaseAdapter {
    private final List<Download> downloads;
    private final Listener listener;

    public DownloadAdapter(List<Download> downloads, Listener listener) {
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
        View view = View.inflate(parent.getContext(), R.layout.list_item_download, null);

        final Download download = getItem(position);
        TextView titleTextView = Views.findById(view, R.id.download_title_text);
        TextView locationTextView = Views.findById(view, R.id.download_location_text);
        Button deleteButton = Views.findById(view, R.id.download_delete_button);

        titleTextView.setText(download.getTitle());
        locationTextView.setText(download.getDownloadStatusText() + " : " + download.getFileName());
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View v) {
                listener.onDelete(download);
            }
        });

        return view;
    }

    interface Listener {
        void onDelete(Download download);
    }
}

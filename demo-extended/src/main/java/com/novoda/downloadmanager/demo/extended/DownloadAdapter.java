package com.novoda.downloadmanager.demo.extended;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.novoda.downloadmanager.demo.R;

import java.util.List;

class DownloadAdapter extends BaseAdapter {
    private final List<DownloadBatch> downloads;

    public DownloadAdapter(List<DownloadBatch> downloads) {
        this.downloads = downloads;
    }

    @Override
    public int getCount() {
        return downloads.size();
    }

    @Override
    public DownloadBatch getItem(int position) {
        return downloads.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = View.inflate(parent.getContext(), R.layout.list_item_download, null);

        DownloadBatch batch = getItem(position);
        TextView titleTextView = (TextView) view.findViewById(R.id.download_title_text);
        TextView locationTextView = (TextView) view.findViewById(R.id.download_location_text);

        titleTextView.setText(batch.getTitle());
        String statusText = batch.getDownloadStatusText();
        locationTextView.setText(statusText + " : " + batch.getFileName());

        return view;
    }
}

package com.novoda.downloadmanager.demo.extended.pause_resume;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.novoda.downloadmanager.demo.R;
import com.novoda.downloadmanager.demo.extended.Download;

import java.util.List;
import java.util.Locale;

public class PauseResumeAdapter extends BaseAdapter {
    private final List<Download> downloads;

    public PauseResumeAdapter(List<Download> downloads) {
        this.downloads = downloads;
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
        TextView titleTextView = (TextView) view.findViewById(R.id.download_title_text);
        TextView locationTextView = (TextView) view.findViewById(R.id.download_location_text);

        titleTextView.setText(download.getTitle());
        String text = String.format(Locale.getDefault(), "%1$s : %2$s\nBatch %3$d", download.getDownloadStatusText(), download.getFileName(), download.getBatchId());
        locationTextView.setText(text);

        return view;
    }

    public void updateDownloads(List<Download> downloads) {
        this.downloads.clear();
        this.downloads.addAll(downloads);
        notifyDataSetChanged();
    }
}

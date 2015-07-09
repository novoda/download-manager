package com.novoda.downloadmanager.demo.extended.extra_data;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.novoda.downloadmanager.demo.R;

import java.util.ArrayList;
import java.util.List;

public class ExtraDataAdapter extends BaseAdapter {
    private final List<Download> downloads;

    public ExtraDataAdapter() {
        this.downloads = new ArrayList<>();
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
        View view = View.inflate(parent.getContext(), R.layout.list_item_extra_data_download, null);

        Download download = getItem(position);
        TextView titleTextView = (TextView) view.findViewById(R.id.download_title_text);
        TextView locationTextView = (TextView) view.findViewById(R.id.download_location_text);

        titleTextView.setText(download.getTitle());
        String text = String.format("%1$s : %2$s", download.getTitle(), download.getExtraData());
        locationTextView.setText(text);

        return view;
    }

    public void updateDownloads(List<Download> downloads) {
        this.downloads.clear();
        this.downloads.addAll(downloads);
        notifyDataSetChanged();
    }
}

package com.novoda.downloadmanager.demo.extended.extra_data;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.novoda.downloadmanager.demo.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExtraDataAdapter extends BaseAdapter {
    private final List<ExtraDataDownload> extraDataDownloads;

    public ExtraDataAdapter() {
        this.extraDataDownloads = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return extraDataDownloads.size();
    }

    @Override
    public ExtraDataDownload getItem(int position) {
        return extraDataDownloads.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = View.inflate(parent.getContext(), R.layout.list_item_extra_data_download, null);

        ExtraDataDownload extraDataDownload = getItem(position);
        TextView titleTextView = (TextView) view.findViewById(R.id.download_title_text);
        TextView locationTextView = (TextView) view.findViewById(R.id.download_location_text);

        titleTextView.setText(extraDataDownload.getTitle());
        String text = String.format(Locale.getDefault(), "%1$s : %2$s", extraDataDownload.getTitle(), extraDataDownload.getExtraData());
        locationTextView.setText(text);

        return view;
    }

    public void updateDownloads(List<ExtraDataDownload> extraDataDownloads) {
        this.extraDataDownloads.clear();
        this.extraDataDownloads.addAll(extraDataDownloads);
        notifyDataSetChanged();
    }
}

package com.novoda.downloadmanager.demo.extended.extra_data;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.novoda.downloadmanager.demo.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExtraDataAdapter extends RecyclerView.Adapter<ExtraDataAdapter.ViewHolder> {
    private final List<ExtraDataDownload> extraDataDownloads;

    public ExtraDataAdapter() {
        this.extraDataDownloads = new ArrayList<>();
    }

    public void updateDownloads(List<ExtraDataDownload> extraDataDownloads) {
        this.extraDataDownloads.clear();
        this.extraDataDownloads.addAll(extraDataDownloads);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        return new ViewHolder(View.inflate(viewGroup.getContext(), R.layout.list_item_extra_data_download, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final ExtraDataDownload extraDataDownload = extraDataDownloads.get(position);
        viewHolder.titleTextView.setText(extraDataDownload.getTitle());
        String text = String.format(Locale.getDefault(), "%1$s : %2$s", extraDataDownload.getTitle(), extraDataDownload.getExtraData());
        viewHolder.locationTextView.setText(text);
    }

    @Override
    public int getItemCount() {
        return extraDataDownloads.size();
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

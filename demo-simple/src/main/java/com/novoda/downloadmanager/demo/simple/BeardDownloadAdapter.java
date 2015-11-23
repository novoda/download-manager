package com.novoda.downloadmanager.demo.simple;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.novoda.downloadmanager.demo.R;
import com.novoda.downloadmanager.domain.Download;
import com.novoda.downloadmanager.domain.DownloadFile;

import java.util.List;

class BeardDownloadAdapter extends RecyclerView.Adapter<BeardDownloadAdapter.ViewHolder> {
    private final List<Download> beardDownloads;

    public BeardDownloadAdapter(List<Download> beardDownloads) {
        this.beardDownloads = beardDownloads;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        return new ViewHolder(View.inflate(viewGroup.getContext(), R.layout.list_item_download, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final Download beardDownload = beardDownloads.get(position);
        viewHolder.statusText.setText(beardDownload.getStatus().name());
        viewHolder.filesText.setText(getFilesCompeted(beardDownload));
        viewHolder.idText.setText("Id : " + beardDownload.getId().toString());
        viewHolder.sizeText.setText(beardDownload.getCurrentSize() + " / " + beardDownload.getTotalSize());
    }

    private String getFilesCompeted(Download beardDownload) {
        int completedFiles = 0;

        for (DownloadFile file : beardDownload.getFiles()) {
            if (file.getStatus() == DownloadFile.FileStatus.COMPLETE) {
                completedFiles++;
            }
        }

        return "Files " + completedFiles + " / " + beardDownload.getFiles().size();
    }

    @Override
    public int getItemCount() {
        return beardDownloads.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView statusText;
        private final TextView filesText;
        private final TextView idText;
        private final TextView sizeText;

        public ViewHolder(View itemView) {
            super(itemView);
            statusText = (TextView) itemView.findViewById(R.id.download_status_text);
            filesText = (TextView) itemView.findViewById(R.id.download_files_text);
            idText = (TextView) itemView.findViewById(R.id.download_id_text);
            sizeText = (TextView) itemView.findViewById(R.id.download_size_text);
        }
    }
}

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

    private final List<Download> downloads;
    private final OnDownloadClickedListener downloadClickedListener;

    public BeardDownloadAdapter(List<Download> downloads, OnDownloadClickedListener downloadClickedListener) {
        this.downloads = downloads;
        this.downloadClickedListener = downloadClickedListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        return new ViewHolder(View.inflate(viewGroup.getContext(), R.layout.list_item_download, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Download download = downloads.get(position);
        viewHolder.bind(download, downloadClickedListener);
    }

    @Override
    public int getItemCount() {
        return downloads.size();
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

        public void bind(final Download download, final OnDownloadClickedListener downloadClickedListener) {
            statusText.setText(download.getStatus().name());
            filesText.setText(getFilesCompeted(download));
            idText.setText("Id : " + download.getId().toString());
            sizeText.setText(download.getCurrentSize() + " / " + download.getTotalSize());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    downloadClickedListener.onDownloadClicked(download);
                }
            });
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

    }

    public interface OnDownloadClickedListener {
        void onDownloadClicked(Download download);
    }

}

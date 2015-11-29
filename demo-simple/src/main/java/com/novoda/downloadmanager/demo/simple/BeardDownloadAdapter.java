package com.novoda.downloadmanager.demo.simple;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.novoda.downloadmanager.demo.R;
import com.novoda.downloadmanager.domain.Download;
import com.novoda.downloadmanager.domain.DownloadFile;

import java.util.ArrayList;
import java.util.List;

class BeardDownloadAdapter extends RecyclerView.Adapter<BeardDownloadAdapter.ViewHolder> {

    private final OnDownloadClickedListener downloadClickedListener;
    private final List<Download> downloads = new ArrayList<>();

    public BeardDownloadAdapter(OnDownloadClickedListener downloadClickedListener) {
        this.downloadClickedListener = downloadClickedListener;
    }

    public void update(List<Download> downloads) {
        this.downloads.clear();
        this.downloads.addAll(downloads);
        notifyDataSetChanged();
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
        private final TextView percentText;

        public ViewHolder(View itemView) {
            super(itemView);
            statusText = (TextView) itemView.findViewById(R.id.download_status_text);
            filesText = (TextView) itemView.findViewById(R.id.download_files_text);
            idText = (TextView) itemView.findViewById(R.id.download_id_text);
            sizeText = (TextView) itemView.findViewById(R.id.download_size_text);
            percentText = (TextView) itemView.findViewById(R.id.download_percent_text);
        }

        public void bind(final Download download, final OnDownloadClickedListener downloadClickedListener) {
            List<DownloadFile> files = download.getFiles();

            statusText.setText(download.getStatus().name());
            filesText.setText(getCurrentFile(files) + " : " + getFilePercentage(files));
            idText.setText("Id : " + download.getId().toString());
            sizeText.setText(download.getCurrentSize() + " / " + download.getTotalSize());
            percentText.setText("" + download.getPercentage() + "%");

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    downloadClickedListener.onDownloadClicked(download);
                }
            });
        }

        private String getFilePercentage(List<DownloadFile> files) {
            for (DownloadFile file : files) {
                if (file.getStatus() == DownloadFile.FileStatus.INCOMPLETE) {
                    return file.getPercentage() + "%";
                }
            }
            // going to assume that all the files have completed
            return "100%";
        }

        private String getCurrentFile(List<DownloadFile> files) {
            for (int i = 0; i < files.size(); i++) {
                DownloadFile file = files.get(i);

                if (file.getStatus() == DownloadFile.FileStatus.INCOMPLETE) {
                    return formatCurrentFile(files, i + 1);
                }
            }
            // going to assume that all the files have completed
            return formatCurrentFile(files, files.size());
        }

        private String formatCurrentFile(List<DownloadFile> files, int currentFilePosition) {
            return "Files " + currentFilePosition + " / " + files.size();
        }

    }

    public interface OnDownloadClickedListener {
        void onDownloadClicked(Download download);
    }

}

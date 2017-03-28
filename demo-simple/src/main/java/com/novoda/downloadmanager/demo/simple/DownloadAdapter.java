package com.novoda.downloadmanager.demo.simple;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.novoda.downloadmanager.demo.R;
import com.novoda.downloadmanager.domain.Download;
import com.novoda.downloadmanager.domain.DownloadFile;
import com.novoda.downloadmanager.domain.DownloadStage;
import com.novoda.notils.logger.simple.Log;

import java.util.ArrayList;
import java.util.List;

class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.ViewHolder> {

    private final OnDownloadClickedListener downloadClickedListener;
    private final List<Download> downloads = new ArrayList<>();

    public DownloadAdapter(OnDownloadClickedListener downloadClickedListener) {
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
        private final View deleteButton;
        private final ImageView pauseResumeButton;

        public ViewHolder(View itemView) {
            super(itemView);
            statusText = (TextView) itemView.findViewById(R.id.download_status_text);
            filesText = (TextView) itemView.findViewById(R.id.download_files_text);
            idText = (TextView) itemView.findViewById(R.id.download_id_text);
            sizeText = (TextView) itemView.findViewById(R.id.download_size_text);
            percentText = (TextView) itemView.findViewById(R.id.download_percent_text);
            deleteButton = itemView.findViewById(R.id.download_delete);
            pauseResumeButton = (ImageView) itemView.findViewById(R.id.download_pause_resume);
        }

        public void bind(final Download download, final OnDownloadClickedListener downloadClickedListener) {
            List<DownloadFile> files = download.getFiles();

            statusText.setText(download.getStatus().name());
            filesText.setText(getFilePercentages(files));
            idText.setText("Id: " + download.getId().asString());
            sizeText.setText("bytes: " + download.getCurrentSize() + " / " + download.getTotalSize());
            percentText.setText("Total: " + download.getPercentage() + "%");

            if (download.getStage() == DownloadStage.COMPLETED) {
                pauseResumeButton.setVisibility(View.GONE);
            } else {
                pauseResumeButton.setVisibility(View.VISIBLE);

                if (download.getStage() == DownloadStage.RUNNING) {
                    pauseResumeButton.setImageResource(R.drawable.ic_pause_black_24dp);
                    pauseResumeButton.setContentDescription(pauseResumeButton.getResources().getString(R.string.button_running_content_description));
                }

                if (download.getStage() == DownloadStage.PAUSED) {
                    pauseResumeButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    pauseResumeButton.setContentDescription(pauseResumeButton.getResources().getString(R.string.button_paused_content_description));
                }

                pauseResumeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (download.getStage() == DownloadStage.RUNNING) {
                            downloadClickedListener.onPauseDownload(download);
                        } else if (download.getStage() == DownloadStage.PAUSED) {
                            downloadClickedListener.onResumeDownload(download);
                        } else {
                            Log.e("Unhandled stage: " + download.getStage());
                        }
                    }
                });
            }

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    downloadClickedListener.onDeleteDownload(download);
                }
            });
        }

        private String getFilePercentages(List<DownloadFile> files) {
            StringBuilder stringBuilder = new StringBuilder();
            for (DownloadFile file : files) {
                stringBuilder
                        .append(file.getFileIdentifier())
                        .append(": ")
                        .append(file.getPercentage())
                        .append("%")
                        .append("\n");
            }
            return stringBuilder.toString();
        }
    }

    public interface OnDownloadClickedListener {
        void onDeleteDownload(Download download);

        void onPauseDownload(Download download);

        void onResumeDownload(Download download);
    }

}

package com.novoda.downloadmanager.demo;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.novoda.downloadmanager.Download;
import com.novoda.downloadmanager.DownloadFile;
import com.novoda.downloadmanager.DownloadStage;
import com.novoda.downloadmanager.demo.R;
import com.novoda.notils.logger.simple.Log;

import java.util.ArrayList;
import java.util.List;

public class DemoView extends FrameLayout {

    private View emptyView;
    private DownloadAdapter adapter;

    private Toast toast;

    public DemoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        View.inflate(getContext(), R.layout.merge_demo, this);

        emptyView = findViewById(R.id.empty_view);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DownloadAdapter();
        recyclerView.setAdapter(adapter);
    }

    public void update(final OnDownloadClickedListener downloadClickedListener) {
        adapter.update(new OnDownloadClickedListener() {
            @Override
            public void onDelete(Download download) {
                downloadClickedListener.onDelete(download);
            }

            @Override
            public void onPause(Download download) {
                toast("Pausing download");
                downloadClickedListener.onPause(download);
            }

            @Override
            public void onResume(Download download) {
                toast("Resuming download");
                downloadClickedListener.onResume(download);
            }
        });
    }

    private void toast(String message) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void display(List<Download> downloads) {
        adapter.update(downloads);
        emptyView.setVisibility(downloads.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private static class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.ViewHolder> {

        private final List<Download> downloads = new ArrayList<>();

        private OnDownloadClickedListener downloadClickedListener = OnDownloadClickedListener.NO_OP;

        void update(OnDownloadClickedListener downloadClickedListener) {
            this.downloadClickedListener = downloadClickedListener;
        }

        void update(List<Download> downloads) {
            this.downloads.clear();
            this.downloads.addAll(downloads);
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
            return new ViewHolder(inflate(viewGroup.getContext(), R.layout.list_item_download, null));
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
            private final TextView progressText;
            private final View deleteButton;
            private final ImageView pauseResumeButton;

            ViewHolder(View itemView) {
                super(itemView);
                statusText = (TextView) itemView.findViewById(R.id.download_status_text);
                filesText = (TextView) itemView.findViewById(R.id.download_files_text);
                idText = (TextView) itemView.findViewById(R.id.download_id_text);
                progressText = (TextView) itemView.findViewById(R.id.download_progress_text);
                deleteButton = itemView.findViewById(R.id.download_delete);
                pauseResumeButton = (ImageView) itemView.findViewById(R.id.download_pause_resume);
            }

            void bind(final Download download, final OnDownloadClickedListener downloadClickedListener) {
                List<DownloadFile> files = download.getFiles();

                statusText.setText(download.getStatus().name());
                filesText.setText(getFilePercentages(files));
                idText.setText("Download ID: " + download.getId().asString());

                String downloadedSize = Formatter.formatShortFileSize(itemView.getContext(), download.getCurrentSize());
                progressText.setText(downloadedSize + " (" + download.getPercentage() + "%)");

                if (download.getStage() == DownloadStage.COMPLETED) {
                    pauseResumeButton.setVisibility(GONE);
                } else {
                    pauseResumeButton.setVisibility(VISIBLE);

                    if (download.getStage() == DownloadStage.RUNNING) {
                        pauseResumeButton.setImageResource(R.drawable.ic_pause_black_24dp);
                        pauseResumeButton.setContentDescription(pauseResumeButton.getResources().getString(R.string.button_running_content_description));
                    }

                    if (download.getStage() == DownloadStage.PAUSED) {
                        pauseResumeButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                        pauseResumeButton.setContentDescription(pauseResumeButton.getResources().getString(R.string.button_paused_content_description));
                    }

                    pauseResumeButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (download.getStage() == DownloadStage.RUNNING) {
                                downloadClickedListener.onPause(download);
                            } else if (download.getStage() == DownloadStage.PAUSED) {
                                downloadClickedListener.onResume(download);
                            } else {
                                Log.e("Unhandled stage: " + download.getStage());
                            }
                        }
                    });
                }

                deleteButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        downloadClickedListener.onDelete(download);
                    }
                });
            }

            private String getFilePercentages(List<DownloadFile> files) {
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < files.size(); i++) {
                    DownloadFile file = files.get(i);
                    stringBuilder
                            .append("File #" + (i + 1) + " ..." + file.getLocalUri().substring(file.getLocalUri().length() - 10, file.getLocalUri().length()))
                            .append(": ")
                            .append(file.getPercentage())
                            .append("%")
                            .append("\n");
                }
                return stringBuilder.toString();
            }
        }
    }

    interface OnDownloadClickedListener {

        OnDownloadClickedListener NO_OP = new OnDownloadClickedListener() {
            @Override
            public void onDelete(Download download) {
            }

            @Override
            public void onPause(Download download) {
            }

            @Override
            public void onResume(Download download) {
            }
        };

        void onDelete(Download download);

        void onPause(Download download);

        void onResume(Download download);
    }
}

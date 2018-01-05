package com.novoda.downloadmanager;

import android.os.Handler;

final class MainThreadPostingCallback {

    interface Action {
        void performAction();
    }

    private MainThreadPostingCallback() {
        // Uses static factory method.
    }

    static MainThreadPostingActionCallback postTo(Handler handler) {
        return new MainThreadPostingActionCallback(handler);
    }

    static class MainThreadPostingActionCallback {

        private final Handler handler;

        MainThreadPostingActionCallback(Handler handler) {
            this.handler = handler;
        }

        void action(final Action action) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    action.performAction();
                }
            });
        }
    }

}

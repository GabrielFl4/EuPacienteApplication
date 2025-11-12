package com.example.eupacienteapplication.util;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;

public class SyncSseClient extends EventSourceListener {

    public interface Callback { void onEvent(String event, String data); }

    private final Handler main = new Handler(Looper.getMainLooper());
    private volatile boolean closedByUser = false;
    private volatile EventSource eventSource;
    private final String url;
    private final Callback callback;
    private final OkHttpClient client;

    public SyncSseClient(String url, Callback callback) {
        this.url = url;
        this.callback = callback;
        this.client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .build();
    }

    public synchronized void start() {
        closedByUser = false;
        if (eventSource != null) { // garante 1 por vez
            try { eventSource.cancel(); } catch (Exception ignored) {}
            eventSource = null;
        }
        Request req = new Request.Builder()
                .url(url)
                .header("Accept","text/event-stream")
                .build();
        eventSource = EventSources.createFactory(client).newEventSource(req, this);
    }

    public synchronized void stop() {
        closedByUser = true;
        if (eventSource != null) {
            try { eventSource.cancel(); } catch (Exception ignored) {}
            eventSource = null;
        }
        main.removeCallbacksAndMessages(null);
    }

    @Override
    public void onEvent(EventSource es, @Nullable String id, @Nullable String type, String data) {
        if (callback != null) callback.onEvent(type != null ? type : "message", data);
    }

    @Override public void onClosed(EventSource es) {
        if (!closedByUser) main.postDelayed(this::start, 1500);
    }
    @Override public void onFailure(EventSource es, @Nullable Throwable t, @Nullable Response r) {
        if (!closedByUser) main.postDelayed(this::start, 1500);
    }
}
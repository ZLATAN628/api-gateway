package com.ycx.core.helper;

import org.asynchttpclient.*;

import java.util.concurrent.CompletableFuture;

public class AsyncHttpHelper {

    private static final class SingletonHolder {
        private static final AsyncHttpHelper INSTANCE = new AsyncHttpHelper();
    }

    public AsyncHttpHelper() {

    }

    public static AsyncHttpHelper getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private AsyncHttpClient client;

    public void initialized(AsyncHttpClient client) {
        this.client = client;
    }

    public CompletableFuture<Response> executeRequest(Request request) {
        ListenableFuture<Response> future = client.executeRequest(request);
        return future.toCompletableFuture();
    }

    public <T> CompletableFuture<T> executeRequest(Request request, AsyncHandler<T> handler) {
        ListenableFuture<T> future = client.executeRequest(request, handler);
        return future.toCompletableFuture();
    }
}

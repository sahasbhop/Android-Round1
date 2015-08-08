package com.github.sahasbhop.hqround1;

import android.content.Context;
import android.text.TextUtils;

import com.github.sahasbhop.flog.FLog;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.github.sahasbhop.hqround1.JsonConstant.JSON_CACHE;
import static com.github.sahasbhop.hqround1.JsonConstant.JSON_URL;

public class PreloadManager {
    private static PreloadManager instance;

    private Bus bus;
    final private LinkedList<String> queue;
    private ExecutorService executor;
    private HashMap<String, String> contents;
    private boolean isEnabled;

    public static PreloadManager getInstance(Context context) {
        if (instance == null) {
            instance = new PreloadManager(context);
        }
        return instance;
    }

    @SuppressWarnings("unused")
    private PreloadManager(Context context) {
        queue = new LinkedList<>();
        executor = Executors.newSingleThreadExecutor();
        contents = new HashMap<>();
        bus = new Bus(ThreadEnforcer.ANY);
    }

    public Bus getBus() {
        return bus;
    }

    public String getCache(String url) {
        if (TextUtils.isEmpty(url)) return null;
        String md5 = md5(url);
        return contents.get(md5);
    }

    public void prioritizeUrl(String url) {
        if (TextUtils.isEmpty(url)) return;

        synchronized (queue) {
            queue.addFirst(url);
        }

        isEnabled = true;
        processNextTask();
    }

    public void handleWebViewList(JSONObject jsonObject) {
        String tag, url;
        JSONObject json;

        for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
            tag = it.next();
            json = jsonObject.optJSONObject(tag);
            boolean cache = json.optBoolean(JSON_CACHE);

            if (!cache) continue;

            url = json.optString(JSON_URL);

            // continue, if URL is empty or it is already queued
            if (TextUtils.isEmpty(url) || queue.contains(url)) continue;

            synchronized (queue) {
                queue.add(url);
            }
        }

        isEnabled = true;

        FLog.d("Process next task");
        processNextTask();
    }

    public void processNextTask() {
        if (!isEnabled) {
            FLog.d("Stopped");
            return;
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                cacheUrl();
            }
        });
    }

    public void stopLoading() {
        isEnabled = false;
    }

    private void cacheUrl() {
        FLog.d("ENTER");

        if (queue.isEmpty()) {
            FLog.i("EXIT (queue is empty)");
            return;
        }

        final String url = queue.poll();
        FLog.v("URL: %s", url);

        if (TextUtils.isEmpty(url)) {
            FLog.w("EXIT (invalid URL)");
            return;
        }

        String md5 = md5(url);
        FLog.v("md5: %s", url);

        if (contents.containsKey(md5)) {
            FLog.w("EXIT (already cached)");
            return;
        }

        try {
            String content = WebViewHelper.downloadContent(url);
            contents.put(md5, content);
            FLog.d("Caching success");

            bus.post(new UrlCacheEvent(url, content));

        } catch (IOException e) {
            FLog.w("Error: %s", e.getMessage());
        }

        FLog.d("Process next task");
        processNextTask();

        FLog.d("EXIT");
    }

    private String md5(String md5) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());

            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < array.length; ++i) {
                builder.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return builder.toString();

        } catch (NoSuchAlgorithmException e) {
            FLog.w("Error: %s", e.getMessage());
        }
        return null;
    }
}

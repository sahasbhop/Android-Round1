package com.github.sahasbhop.hqround1;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.sahasbhop.flog.FLog;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;

public class WebViewActivity extends AppCompatActivity {

    @Bind(R.id.layout_progress) View layoutProgress;
    @Bind(R.id.progress_bar) ProgressBar progressBar;
    @Bind(R.id.text_progress) TextView textProgress;
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.webview) WebView webView;

    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        ButterKnife.bind(this);

        Intent intent = getIntent();

        if (intent == null) {
            supportFinishAfterTransition();
            return;
        }

        String title = intent.getStringExtra("title");
        boolean useCache = intent.getBooleanExtra("cache", false);

        url = intent.getStringExtra("url");
        FLog.d("URL: %s", url);

        if (TextUtils.isEmpty(url)) {
            FLog.w("URI Error!");
            supportFinishAfterTransition();
            return;
        }

        toolbar.setTitle(title);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Set progress bar color
        progressBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.accent), PorterDuff.Mode.SRC_IN);

        // Register OttoBus to receive cache update
        Bus ottoBus = PreloadManager.getInstance(getApplicationContext()).getBus();
        ottoBus.register(this);

        configWebView();

        // Check cache
        if (useCache) {
            PreloadManager preloadManager = PreloadManager.getInstance(getApplicationContext());
            String cache = preloadManager.getCache(url);

            if (!TextUtils.isEmpty(cache)) {
                FLog.d("Load from cache");
                loadUrlFromCache(cache);

            } else {
                FLog.d("Add URL to first queue and wait for the callback");
                preloadManager.prioritizeUrl(url);
            }
        } else {
            layoutProgress.setVisibility(View.VISIBLE);

            FLog.d("Load URL");
            webView.loadUrl(url);
        }
    }

    @Override
    protected void onDestroy() {
        try {
            Bus ottoBus = PreloadManager.getInstance(getApplicationContext()).getBus();
            ottoBus.unregister(this);
        } catch (Exception e) {/*ignored*/}

        super.onDestroy();
    }

    @SuppressWarnings("unused")
    @Subscribe public void onUrlCacheUpdate(UrlCacheEvent event) {
        String eventUrl = event.url;

        if (webView != null && url != null && url.equals(eventUrl)) {
            loadUrlFromCache(event.content);
        }
    }

    private void loadUrlFromCache(String cache) {
        if (webView != null && cache != null) {
            FLog.d("Load data");
            webView.loadDataWithBaseURL(url, cache, "text/html", "utf-8", null);
        }
    }

    private void configWebView() {
        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                String text = String.format("%s %d%%", getString(R.string.loading_content), progress);
                textProgress.setText(text);
                progressBar.setProgress(progress);

                if (progress >= 100) {
                    layoutProgress.setVisibility(View.GONE);
                }
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (isFinishing()) return;
                new AlertDialog.Builder(WebViewActivity.this)
                        .setMessage(description)
                        .setCancelable(false)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                supportFinishAfterTransition();
                            }
                        })
                        .show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

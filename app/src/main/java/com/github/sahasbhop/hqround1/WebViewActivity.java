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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.sahasbhop.flog.FLog;

import butterknife.Bind;
import butterknife.ButterKnife;

public class WebViewActivity extends AppCompatActivity {

    @Bind(R.id.layout_progress) View layoutProgress;
    @Bind(R.id.progress_bar) ProgressBar progressBar;
    @Bind(R.id.text_progress) TextView textProgress;
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.webview) WebView webView;

    private String url;
    private String title;
    private boolean loadFromCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        ButterKnife.bind(this);

        if (validateIntent()) return;
        FLog.d("url: %s", url);

        setupActionBar();

        // Set progress bar color
        progressBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.accent), PorterDuff.Mode.SRC_IN);

        // Register OttoBus to receive cache update
        configWebView();

        if (loadFromCache) {
            FLog.d("Load from cache");
            webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            webView.loadUrl(url);

        } else {
            layoutProgress.setVisibility(View.VISIBLE);

            FLog.d("Load Url");
            webView.loadUrl(url);
        }
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

    private boolean validateIntent() {
        Intent intent = getIntent();

        if (intent == null) {
            supportFinishAfterTransition();
            return true;
        }

        title = intent.getStringExtra("title");
        loadFromCache = intent.getBooleanExtra("cache", false);
        url = intent.getStringExtra("url");

        if (TextUtils.isEmpty(url)) {
            FLog.w("URI Error!");
            supportFinishAfterTransition();
            return true;
        }
        return false;
    }

    private void setupActionBar() {
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void configWebView() {
        webView.getSettings().setAppCachePath(getCacheDir().getAbsolutePath());
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

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

}

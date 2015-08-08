package com.github.sahasbhop.hqround1;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.sahasbhop.flog.FLog;

import org.json.JSONArray;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import static com.github.sahasbhop.hqround1.JsonConstant.*;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.progress_bar) ProgressBar progressBar;
    @Bind(R.id.recycler_view) RecyclerView recyclerView;
    @Bind(R.id.toolbar) Toolbar toolbar;

    enum WebViewListSource {SERVER, LOCAL}

    private AsyncTask<Void, Void, Object> asyncTask;
    private JSONObject data;
    private JSONArray names;
    private WebViewListSource source = WebViewListSource.SERVER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        toolbar.setTitle(R.string.webview_list);
        setSupportActionBar(toolbar);

        boolean resumeStateSuccess = false;

        if (savedInstanceState != null && savedInstanceState.containsKey("data")) {
            String string = savedInstanceState.getString("data");

            try {
                JSONObject json = new JSONObject(string);

                FLog.d("Load data");
                loadData(json);

                resumeStateSuccess = true;

            } catch (Exception e) {
                FLog.w("Error: %s", e.getMessage());
            }
        }

        if (!resumeStateSuccess) {
            FLog.d("Request content");
            requestContent();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (data != null) {
            outState.putString("data", data.toString());
        }
    }

    @Override
    protected void onDestroy() {
        if (asyncTask != null) asyncTask.cancel(true);
        super.onDestroy();
    }

    private void requestContent() {
        if (asyncTask != null) asyncTask.cancel(true);

        asyncTask = new AsyncTask<Void, Void, Object>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected Object doInBackground(Void... params) {
                Object result = null;
                try {
                    switch (source) {
                        case LOCAL:
                            result = WebViewListFactory.fromRaw(getApplicationContext());
                            break;
                        case SERVER:
                            result = WebViewListFactory.fromServer(URL_SOURCE_JSON);
                            break;
                    }
                } catch (Exception e) {
                    result = e;
                }
                return result;
            }

            @Override
            protected void onPostExecute(Object result) {
                super.onPostExecute(result);
                progressBar.setVisibility(View.GONE);

                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        MainActivity.this.supportFinishAfterTransition();
                    }
                };

                // Fail cases
                if (result != null && result instanceof Exception) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage(((Exception) result).getMessage())
                            .setCancelable(false)
                            .setPositiveButton(R.string.ok, listener)
                            .show();
                    return;
                } else if (result == null || !(result instanceof JSONObject)) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("Failed loading content!")
                            .setCancelable(false)
                            .setPositiveButton(R.string.ok, listener)
                            .show();
                    return;
                }

                // Success -> Load data & setup adapter
                FLog.d("Load data");
                loadData((JSONObject) result);
            }
        }.execute();
    }

    private void loadData(JSONObject result) {
        data = result;
        names = result.names();

        LocalAdapter adapter = new LocalAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(adapter);
    }

    private void onWebViewItemClick(int position) {
        FLog.v("position: %d", position);
        if (names == null || position >= names.length()) return;

        String tag = names.optString(position);
        FLog.d("Selected WebView: %s", tag);

        JSONObject json = data.optJSONObject(tag);
        if (json == null) return;

        String url = json.optString(JSON_URL);
        FLog.d("URL: %s", url);

        if (TextUtils.isEmpty(url)) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.url_not_found)
                    .setCancelable(false)
                    .setPositiveButton(R.string.ok, null)
                    .show();
            return;
        }
        // TODO open WebView with specific URL
    }

    public class LocalAdapter extends RecyclerView.Adapter<LocalAdapter.LocalViewHolder> {
        private LocalOnClickListener onClickListener = new LocalOnClickListener();

        @Override
        public LocalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_webview_list_item, parent, false);
            v.setOnClickListener(onClickListener);
            return new LocalViewHolder(v);
        }

        @Override
        public void onBindViewHolder(LocalViewHolder holder, int position) {
            if (names == null || position >= names.length()) return;

            String name = names.optString(position);
            holder.textName.setText(name);
        }

        @Override
        public int getItemCount() {
            return data == null ? 0 : data.length();
        }

        class LocalViewHolder extends RecyclerView.ViewHolder {
            @Bind(R.id.textName) public TextView textName;

            public LocalViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        } // End class LocalViewHolder

        class LocalOnClickListener implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                int position = recyclerView.getChildAdapterPosition(v);
                onWebViewItemClick(position);
            }
        }
    } // End class LocalAdapter
}

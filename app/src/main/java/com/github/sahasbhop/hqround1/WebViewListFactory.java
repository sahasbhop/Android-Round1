package com.github.sahasbhop.hqround1;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.RawRes;

import com.github.sahasbhop.flog.FLog;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

public class WebViewListFactory {

    public static JSONObject fromServer(String url) throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        String string = response.body().string();
        return new JSONObject(string);
    }

    public static JSONObject fromRaw(Context context) throws JSONException {
        String string = getStringContent(context.getResources(), R.raw.webview_list);
        return new JSONObject(string);
    }

    private static String getStringContent(Resources resources, @RawRes int rawResId) {
        InputStream is = resources.openRawResource(rawResId);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];

        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (Exception e) {
            FLog.w("Error: %s", e.toString());
        } finally {
            if (is != null) try {is.close();} catch (IOException e) {/*ignored*/}
        }

        return writer.toString();
    }

}

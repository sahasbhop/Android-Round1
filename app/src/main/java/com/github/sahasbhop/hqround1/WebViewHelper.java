package com.github.sahasbhop.hqround1;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;

import static com.github.sahasbhop.hqround1.JsonConstant.APP_SECRET_KEY;
import static com.github.sahasbhop.hqround1.JsonConstant.CURRENCY_CODE;
import static com.github.sahasbhop.hqround1.JsonConstant.JSON_CACHE;
import static com.github.sahasbhop.hqround1.JsonConstant.JSON_PAGE_TITLE;
import static com.github.sahasbhop.hqround1.JsonConstant.JSON_URL;
import static com.github.sahasbhop.hqround1.JsonConstant.OFFER_ID;
import static com.github.sahasbhop.hqround1.JsonConstant.SELECTED_VOUCHERS;
import static com.github.sahasbhop.hqround1.JsonConstant.USER_ID;

public class WebViewHelper {

    public static String downloadContent(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public static void openWebView(Activity activity, String tag, JSONObject json) {
        if (json == null) throw new IllegalArgumentException();

        String url = json.optString(JSON_URL);

        String title = json.optString(JSON_PAGE_TITLE);
        if (TextUtils.isEmpty(title)) title = tag;

        boolean cache = json.optBoolean(JSON_CACHE);

        url = processUrl(url);

        Intent intent = new Intent(activity, WebViewActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("url", url);
        intent.putExtra("cache", cache);

        activity.startActivity(intent);
    }

    public static String processUrl(String url) {
        return url.replace("{userId}", USER_ID)
                .replace("{appSecretKey}", APP_SECRET_KEY)
                .replace("{currencyCode}", CURRENCY_CODE)
                .replace("{offerId}", OFFER_ID)
                .replace("{selectedVouchers}", SELECTED_VOUCHERS);
    }

}

package com.github.sahasbhop.hqround1;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;

import static com.github.sahasbhop.hqround1.JsonConstant.*;

public class WebViewHelper {

    public static void openWebView(Activity activity, String title, String url) {
        if (TextUtils.isEmpty(url)) throw new IllegalArgumentException();

        url = processUrl(url);
        Intent intent = new Intent(activity, WebViewActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("url", url);
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

package com.github.sahasbhop.hqround1;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.github.sahasbhop.hqround1.JsonConstant.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class WebViewHelperTest extends TestCase {

    @Test public void testReplaceUserId() throws Exception {
        String input = "http://appcontent.hotelquickly.com/en/1/android/credits/empty?userId={userId}&date=2015-8-8&hour=5";
        String expected = "http://appcontent.hotelquickly.com/en/1/android/credits/empty?userId=" + USER_ID + "&date=2015-8-8&hour=5";

        String output = WebViewHelper.processUrl(input);
        assertEquals(expected, output);
    }

    @Test public void testReplaceAllData() throws Exception {
        String input = "http://appcontent.hotelquickly.com/en/1/android/users/{userId}/credits/available?appSecretKey={appSecretKey}&date=2015-8-8&hour=5";
        String expected = "http://appcontent.hotelquickly.com/en/1/android/users/" + USER_ID + "/credits/available?appSecretKey=" + APP_SECRET_KEY + "&date=2015-8-8&hour=5";

        String output = WebViewHelper.processUrl(input);
        assertEquals(expected, output);

        input = "http://appcontent.hotelquickly.com/en/1/android/offers/{offerId}/discount?currencyCode={currencyCode}&appSecretKey={appSecretKey}&selectedVouchers={selectedVouchers}&date=2015-8-8&hour=5";
        expected = "http://appcontent.hotelquickly.com/en/1/android/offers/" + OFFER_ID + "/discount?currencyCode=" + CURRENCY_CODE + "&appSecretKey=" + APP_SECRET_KEY + "&selectedVouchers=" + SELECTED_VOUCHERS + "&date=2015-8-8&hour=5";

        output = WebViewHelper.processUrl(input);
        assertEquals(expected, output);
    }
}
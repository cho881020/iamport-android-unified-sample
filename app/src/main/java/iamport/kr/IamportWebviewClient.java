package iamport.kr;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by constant on 2017-06-23.
 */

public class IamportWebviewClient extends WebViewClient {

    private MainActivity mActivity;

    public IamportWebviewClient(MainActivity activity) {
        mActivity = activity;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (isOverridableUrl(url)) {

            Intent intent = null;
            try {

                if (url.startsWith(Scheme.BANKPAY)) {
                    String params = getRequestParamsOfBankPay(url);
                    intent = new Intent(Intent.ACTION_MAIN);
                    intent.setComponent(new ComponentName("com.kftc.bankpay.android","com.kftc.bankpay.android.activity.MainActivity"));
                    intent.putExtra("requestInfo", params);
                    mActivity.startActivityForResult(intent, mActivity.REQUEST_CODE);
                    return true;
                }

                intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                Uri uri = Uri.parse(intent.getDataString());
                mActivity.startActivity(new Intent(Intent.ACTION_VIEW, uri));
                return true;
            } catch (URISyntaxException e) {
                //e.printStackTrace();
            } catch (ActivityNotFoundException e) {
                return handleNotFoundPaymentScheme(intent);
            }
        }

        return false;
    }

    private boolean isOverridableUrl(String url) {
        String[] schemas = new String[]{"http://", "https://", "javascript://"};
        for (String schema : schemas) {
            if (url.startsWith(schema)) {
                return false;
            }
        }
        return true;
    }

    private boolean handleNotFoundPaymentScheme(Intent intent) {
        if (intent != null) {
            String id = null;
            String scheme = intent.getScheme();
            if (!TextUtils.isEmpty(scheme)) {
                if (scheme.equalsIgnoreCase(Scheme.ISP)) {
                    id = Scheme.PACKAGE_ISP;
                } else if (scheme.equalsIgnoreCase(Scheme.BANKPAY)) {
                    id = Scheme.PACKAGE_BANKPAY;
                }
            }

            if (TextUtils.isEmpty(id)) {
                id = intent.getPackage();
            }

            if (!TextUtils.isEmpty(id)) {
                mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + id)));
                return true;
            }
        }

        return false;
    }

    private String getRequestParamsOfBankPay(String rawUrl) throws URISyntaxException {

        Uri uri = Uri.parse(rawUrl);
        Map<String, String> query = getQuery(uri.getQuery());
        List<String> allowedKeys = Arrays.asList("user_key", "firm_name", "amount", "serial_no", "approve_no", "receipt_yn", "user_key", "callbackparam2", "");
        StringBuilder params = new StringBuilder();

        for (String key : query.keySet()) {
            String value = query.get(key);
            if (allowedKeys.contains(key)) {
                if (key.equals("user_key")) {
                    mActivity.setBankTid(value);
                    params.append(String.format("&%s=%s",key, value));
                }
            }
        }

        params.append(String.format("&%s=%s","callbackparam1", "nothing"));
        params.append(String.format("&%s=%s","callbackparam3", "nothing"));

        return params.toString();
    }

    private Map<String, String> getQuery(String rawQuery) {
        Map<String, String> query = new HashMap<String, String>();
        String[] pairs = rawQuery.split("&");
        for (String pair : pairs) {
            String[] partials = pair.split("=");
            if (partials.length > 2) {
                query.put(partials[0], partials[1]);
            }
        }
        return query;
    }
}

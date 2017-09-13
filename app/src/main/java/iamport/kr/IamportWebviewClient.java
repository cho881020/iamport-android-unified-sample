package iamport.kr;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.provider.Browser;
import android.text.TextUtils;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
                } else if ( url.startsWith(Scheme.LGU_BANKPAY)) {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.putExtra(Browser.EXTRA_APPLICATION_ID, mActivity.getPackageName());
                    mActivity.startActivity(intent);

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

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        view.evaluateJavascript("javascript:alert()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String s) {
                System.out.print("sss");
            }
        });
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
                } else if ( scheme.equalsIgnoreCase(Scheme.LGU_BANKPAY)) {
                    id = Scheme.PACKAGE_LGU_BANKPAY;
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

        mActivity.setBankTid("");
        Uri uri = Uri.parse(rawUrl);
        Set<String> queryNames = uri.getQueryParameterNames();

        StringBuilder ret_data = new StringBuilder();
        List<String> keys = Arrays.asList(new String[]{"firm_name", "amount", "serial_no", "approve_no", "receipt_yn", "user_key", "callbackparam2", ""});

        String v;
        for ( String k : queryNames) {

            if (keys.contains(k)) {
                v = uri.getQueryParameter(k);

                if ("user_key".equals(k)) {
                    mActivity.setBankTid(v);
                }
                ret_data.append("&").append(k).append("=").append(v);
            }
        }

        ret_data.append("&callbackparam1=" + "nothing");
        ret_data.append("&callbackparam3=" + "nothing");

        return ret_data.toString();
    }

}
